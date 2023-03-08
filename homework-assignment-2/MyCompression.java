/**
 * This file contains the Compression Class for assignment 2
 *
 */

// swing imports
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

// other imports
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

class ImageDisplay {
    private final BufferedImage orig;
    private final BufferedImage quantized;

    ImageDisplay(BufferedImage orig, BufferedImage quantized) {
        this.orig = orig;
        this.quantized = quantized;
    }
    public void showImg() {

        // Create a new
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbText1 = new JLabel("Original image (Left)");
        JLabel lbText2 = new JLabel("Quantized image (Right)");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);
        lbText2.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbIm1 = new JLabel(new ImageIcon(orig));
        JLabel lbIm2 = new JLabel(new ImageIcon(quantized));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(20, 40, 20, 40);
        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(lbText2, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(lbIm2, c);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class SingleChannelImageParser {
    private final String imagePath;
    private final int resHeight;
    private final int resWidth;
    private byte[] pixels;

    SingleChannelImageParser(String imagePath, int resHeight, int resWidth) {
        this.imagePath = imagePath;
        this.resHeight = resHeight;
        this.resWidth = resWidth;
        parseImage();
    }

    void parseImage() {
        Path path = Paths.get(imagePath);
        try {
            pixels =  Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BufferedImage getParsedImageAsGray() {
        BufferedImage image = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < pixels.length; i++) {
            int intensity = 0xff000000 | ((pixels[i] & 0xff)) | ((pixels[i] & 0xff) << 8) | (pixels[i] & 0xff);
            image.setRGB(i % resWidth, (int) Math.floor((float)(i / resWidth)), intensity);
        }
        return image;
    }

    ArrayList<Pair<Float, Float>> getParsedImageInR2() {
        ArrayList<Pair<Float, Float>> imgVectors = new ArrayList<>();
        for (int i = 0; i < pixels.length; i += 2) {
            imgVectors.add(new Pair<>((float) (0x000000ff & pixels[i]), (float) (0x000000ff & pixels[i + 1])));
        }
        return imgVectors;
    }
}

class Quantize {
    private final ArrayList<Pair<Float, Float>> imgVectors;
    private final int n;
    private static final float DIFF_THRESHOLD = 0.1f;
    private final int resHeight;
    private final int resWidth;

    interface EuclideanDist {
        double getDist(Pair<Float, Float> vec1, Pair<Float, Float> vec2);
    }

    private static final EuclideanDist distInst = ($1, $2) -> Math.pow(
            Math.pow($1.getKey() - $2.getKey(), 2)
                    + Math.pow($1.getValue() - $2.getValue(), 2)
            , 0.5);

    Quantize(ArrayList<Pair<Float, Float>> imgVectors, int n, int resHeight, int resWidth) {
        this.imgVectors = imgVectors;
        this.n = n;
        this.resHeight = resHeight;
        this.resWidth = resWidth;
    }

    private int getNearestCodeVecIndex(Pair<Float, Float> vec, ArrayList<Pair<Float, Float>> codebookVectors) {
        return codebookVectors
                .stream()
                .parallel()
                .reduce(
                        ($1, $2) ->
                                distInst.getDist(vec, $1) < distInst.getDist(vec, $2)
                                        ? $1 : $2
                )
                .map(codebookVectors::indexOf)
                .orElse(-1);
    }

    private ArrayList<Pair<Float, Float>> initCodebookVectors() {
        // initialize the initial codebook vectors as random vectors from the vectors in the image
        ArrayList<Pair<Float, Float>> codebookVectors =
                (ArrayList<Pair<Float, Float>>)
                        imgVectors
                                .stream()
                                .distinct()
                                .collect(Collectors.toList());
        Collections.shuffle(codebookVectors);
        return new ArrayList<>( codebookVectors.subList(0, n));
    }

    private HashMap<Integer, ArrayList<Pair<Float, Float>>> createClusters(ArrayList<Pair<Float, Float>> codebookVectors) {
        // assign each vector to the nearest codebook vector
        HashMap<Integer, ArrayList<Pair<Float, Float>>> codebookClusters = new HashMap<>(codebookVectors.size());
        for (Pair<Float, Float> vec : imgVectors) {
            int idx = getNearestCodeVecIndex(vec, codebookVectors);
            if (codebookClusters.containsKey(idx)) {
                codebookClusters.get(idx).add(vec);
            } else {
                codebookClusters.put(idx, new ArrayList<>(Collections.singletonList(vec)));
            }
        }
        return codebookClusters;
    }

    private double updateCodebookVectors(ArrayList<Pair<Float, Float>> codebookVectors, HashMap<Integer, ArrayList<Pair<Float, Float>>> codebookClusters) {
        // based on assignment, update the codebook vectors
        double avgDiff = 0.0f;
        for (HashMap.Entry<Integer, ArrayList<Pair<Float, Float>>> entry : codebookClusters.entrySet()) {
            Pair<Float, Float> centroid = entry
                    .getValue()
                    .stream()
                    .reduce(
                            ($1, $2) -> new Pair<>($1.getKey() + $2.getKey(), $1.getValue() + $2.getValue())
                    )
                    .map(centroidOpt -> new Pair<>(centroidOpt.getKey() / entry.getValue().size(), centroidOpt.getValue() / entry.getValue().size()))
                    .orElse(new Pair<>(0.0f, 0.0f));
            avgDiff += distInst.getDist(centroid, codebookVectors.get(entry.getKey()));
            codebookVectors.set(entry.getKey(), centroid);
        }
        avgDiff /= codebookVectors.size();
        return avgDiff;
    }

    private ArrayList<Pair<Integer, Integer>> quantizedVectors(ArrayList<Pair<Float, Float>> codebookVectors) {
        return (ArrayList<Pair<Integer, Integer>>) imgVectors
                .stream()
                .map($ -> {
                    double minDist = Double.MAX_VALUE;
                    int minIdx = -1;
                    for (int i = 0; i < codebookVectors.size(); i++) {
                        if (distInst.getDist(codebookVectors.get(i), $) < minDist) {
                            minDist = distInst.getDist(codebookVectors.get(i), $);
                            minIdx = i;
                        }
                    }
                    return new Pair<>(codebookVectors.get(minIdx).getKey().intValue(), codebookVectors.get(minIdx).getValue().intValue());
                }).collect(Collectors.toList());
    }

    private BufferedImage vectorToImg(ArrayList<Pair<Integer, Integer>> vectors) {
        ArrayList<Integer> pixels = new ArrayList<>();
        for (Pair<Integer, Integer> vector: vectors) {
            pixels.add(vector.getKey());
            pixels.add(vector.getValue());
        }
        BufferedImage image = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < pixels.size(); i++) {
            int intensity = 0xff000000 | ((pixels.get(i) & 0xff)) | ((pixels.get(i) & 0xff) << 8) | (pixels.get(i) & 0xff);
            image.setRGB(i % resWidth, (int) Math.floor((float)(i / resWidth)), intensity);
        }
        return image;
    }

    public BufferedImage getQuantize() {
        // initialize the initial codebook vectors as random vectors from the vectors in the image
        ArrayList<Pair<Float, Float>> codebookVectors = initCodebookVectors();

        double avgDiff;
        int iter = 1;
        do {
            HashMap<Integer, ArrayList<Pair<Float, Float>>> codebookClusters = createClusters(codebookVectors);
            avgDiff = updateCodebookVectors(codebookVectors, codebookClusters);
            System.out.printf("Iteration %o:\nCodebook: %s\nAverage Difference: %f%n\n", iter, codebookVectors, avgDiff);
            iter += 1;
        } while (avgDiff > DIFF_THRESHOLD);
        return vectorToImg(quantizedVectors(codebookVectors));
    }
}


/**
 * Main class for the program:
 * - control sequence of execution
 * - handle image compression
 *
 * @author rutvikpatel
 */
public class MyCompression {

    private static final int RES_HEIGHT = 288;
    private static final int RES_WIDTH = 352;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("ERR: Expected three arguments (image, m, n)");
            // Terminate the program unsuccessfully
            exit(1);
        }

        String srcImgPath = args[0];
        int m = Integer.parseInt(args[1]);
        int n = Integer.parseInt(args[2]);

        SingleChannelImageParser scip = new SingleChannelImageParser(srcImgPath, RES_HEIGHT, RES_WIDTH);
        scip.parseImage();
        Quantize q = new Quantize(scip.getParsedImageInR2(), n, RES_HEIGHT, RES_WIDTH);
        ImageDisplay id = new ImageDisplay(scip.getParsedImageAsGray(), q.getQuantize());
        id.showImg();
    }
}
