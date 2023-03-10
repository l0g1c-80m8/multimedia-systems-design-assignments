/**
 * This file contains the Compression Class for assignment 2
 *
 */

// swing imports
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
import java.util.stream.IntStream;

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

    SingleChannelImageParser(String imagePath, int resHeight, int resWidth) {
        this.imagePath = imagePath;
        this.resHeight = resHeight;
        this.resWidth = resWidth;
    }

    public ArrayList<ArrayList<Integer>> parseImage() {
        System.out.println("Begin Image Parsing \n");
        Path path = Paths.get(imagePath);
        try {
            byte[] pixels =  Files.readAllBytes(path);
            ArrayList<ArrayList<Integer>> image = new ArrayList<>();
            for (int i = 0; i < resHeight; i++) {
                ArrayList<Integer> imageRow = new ArrayList<>();
                for (int j = 0; j < resWidth; j++) {
                    imageRow.add(0x000000ff & pixels[i * resWidth + j]);
                }
                image.add(imageRow);
            }
            System.out.println("Image Parsing Complete\n");
            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Quantize {
    private final ArrayList<ArrayList<Integer>> image;
    private final int n;
    private final int m;
    private static final float DIFF_THRESHOLD = 0.1f;
    private final int resHeight;
    private final int resWidth;

    interface EuclideanDist {
        double getDist(Vector<Integer> vec1, Vector<Integer> vec2);
    }

    private static final EuclideanDist distInst = ($1, $2) -> Math.pow(
            IntStream.range(0, $1.size())
                    .reduce(
                            0,
                            (acc, idx) ->
                                    (int) (acc + Math.pow($1.get(idx) - $2.get(idx), 2))
                    )
            , 0.5);

    Quantize(ArrayList<ArrayList<Integer>> image, int n, int m, int resHeight, int resWidth) {
        this.image = image;
        this.n = n;
        this.m = m;
        this.resHeight = resHeight;
        this.resWidth = resWidth;
    }

    private ArrayList<Vector<Integer>> getVectorizedImage() {
        ArrayList<Vector<Integer>> vectorizedImg = new ArrayList<>();
        if (this.m == 2) {
            for (int i = 0; i < resHeight; i++) {
                for (int j = 0; j < resWidth; j += this.m) {
                    vectorizedImg.add(
                            new Vector<>(Arrays.asList(
                                    this.image.get(i).get(j),
                                    this.image.get(i).get(j + 1)
                            ))
                    );
                }
            }
        } else {
            int side = (int) Math.pow(this.m, 0.5);
            for (int i = 0; i < resHeight; i += side) {
                for (int j = 0; j < resWidth; j += side) {
                    Vector<Integer> vector = new Vector<>(this.m);
                    for (int deltaI = 0; deltaI < side; deltaI++) {
                        for (int deltaJ = 0; deltaJ < side; deltaJ++) {
                            vector.add(this.image.get(i + deltaI).get(j + deltaJ));
                        }
                    }
                    vectorizedImg.add(vector);
                }
            }
        }
        return vectorizedImg;
    }

    private ArrayList<Vector<Integer>> initCodeBookVectors(ArrayList<Vector<Integer>> vectorizedImg) {
        ArrayList<Vector<Integer>> codebookVectors = (ArrayList<Vector<Integer>>) vectorizedImg
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(codebookVectors);
        return new ArrayList<>( codebookVectors.subList(0, this.n));
    }

    private int getNearestCodeVecIndex(
            Vector<Integer> vector,
            ArrayList<Vector<Integer>> codebookVectors
    ) {
        return codebookVectors
                .stream()
                .parallel()
                .reduce(
                        ($1, $2) ->
                                distInst.getDist(vector, $1) < distInst.getDist(vector, $2)
                                        ? $1 : $2
                )
                .map(codebookVectors::indexOf)
                .orElse(-1);
    }

    private HashMap<Integer, ArrayList<Vector<Integer>>> updateClusters(
            ArrayList<Vector<Integer>> codebookVectors,
            ArrayList<Vector<Integer>> vectorizedImg
    ) {
        HashMap<Integer, ArrayList<Vector<Integer>>> codebookClusters = new HashMap<>(codebookVectors.size());
        for (Vector<Integer> vector : vectorizedImg) {
            int idx = getNearestCodeVecIndex(vector, codebookVectors);
            if (codebookClusters.containsKey(idx)) {
                codebookClusters.get(idx).add(vector);
            } else {
                codebookClusters.put(idx, new ArrayList<>(Collections.singletonList(vector)));
            }
        }
        return codebookClusters;
    }

    private ArrayList<ArrayList<Integer>> getQuantizedImage(
            ArrayList<Vector<Integer>> vectorizedImage,
            ArrayList<Vector<Integer>> codebookVectors
    ) {
        ArrayList<ArrayList<Integer>> quantizedImage = IntStream
                .range(0, resHeight)
                .boxed()
                .map(rowIdx -> new ArrayList<>(
                        IntStream
                                .range(0, resWidth)
                                .boxed()
                                .map(colIdx -> 0)
                                .collect(Collectors.toList())
                )).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Vector<Integer>> quantizedVectors = (ArrayList<Vector<Integer>>) vectorizedImage
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
                    return (Vector<Integer>) codebookVectors.get(minIdx).clone();
                })
                .collect(Collectors.toList());

        int side = (int) Math.sqrt(this.m);
        if (this.m == 2) {
            for (int i = 0; i < quantizedVectors.size(); i++) {
                quantizedImage.get((this.m * i) / resWidth).set((this.m * i) % resWidth, quantizedVectors.get(i).get(0));
                quantizedImage.get((this.m * i) / resWidth).set((this.m * i) % resWidth + 1, quantizedVectors.get(i).get(1));
            }
        } else {
            for (int i = 0; i < quantizedVectors.size(); i++) {
                for (int deltaX = 0; deltaX < side; deltaX++) {
                    for (int deltaY = 0; deltaY < side; deltaY++) {
                        quantizedImage
                                .get(side * ((side * i) / resWidth) + deltaX)
                                .set(
                                        (side * i) % resWidth + deltaY,
                                        quantizedVectors.get(i).get(side * deltaX + deltaY)
                                );
                    }
                }
            }
        }
        return quantizedImage;
    }

    private double updateCodebookVectors(
            ArrayList<Vector<Integer>> codebookVectors, HashMap<Integer,
            ArrayList<Vector<Integer>>> codebookClusters
    ) {
        double diff = 0.0f;
        for (HashMap.Entry<Integer, ArrayList<Vector<Integer>>> entry : codebookClusters.entrySet()) {
            Vector<Integer> centroid = entry
                    .getValue()
                    .stream()
                    .reduce(($1, $2) -> new Vector<>(IntStream
                            .range(0, $1.size())
                            .boxed()
                            .map(idx -> $1.get(idx) + $2.get(idx))
                            .collect(Collectors.toCollection(Vector::new))
                    ))
                    .map(vector -> vector
                            .stream()
                            .map(component -> Math.round((float) component / entry.getValue().size()))
                            .collect(Collectors.toCollection(Vector::new))
                    )
                    .orElse(IntStream
                            .range(0, entry.getValue().size())
                            .boxed()
                            .map(idx -> 0)
                            .collect(Collectors.toCollection(Vector::new))
                    );
            diff += distInst.getDist(centroid, codebookVectors.get(entry.getKey()));
            codebookVectors.set(entry.getKey(), centroid);
        }
        return diff / codebookVectors.size();
    }

    ArrayList<ArrayList<Integer>> getQuantized() {
        System.out.println("Begin Quantization\n");
        ArrayList<Vector<Integer>> vectorizedImg = getVectorizedImage();
        ArrayList<Vector<Integer>> codebookVectors = initCodeBookVectors(vectorizedImg);

        double avgDiff;
        int iter = 1;
        do {
            HashMap<Integer, ArrayList<Vector<Integer>>> codebookClusters = updateClusters(codebookVectors, vectorizedImg);
            avgDiff = updateCodebookVectors(codebookVectors, codebookClusters);
            System.out.printf("Iteration %o:\nCodebook: %s\nAverage Difference: %f%n\n", iter, codebookVectors, avgDiff);
            iter += 1;
        } while (avgDiff > DIFF_THRESHOLD);
        System.out.println("Quantization Complete\n");

        return getQuantizedImage(vectorizedImg, codebookVectors);
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

    private static BufferedImage getImageFromBitmap(ArrayList<ArrayList<Integer>> bitmap) {
        BufferedImage image = new BufferedImage(bitmap.get(0).size(), bitmap.size(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < bitmap.size(); i++) {
            for (int j = 0; j < bitmap.get(i).size(); j ++) {
                int intensity = 0xff000000
                        | ((bitmap.get(i).get(j) & 0xff))
                        | ((bitmap.get(i).get(j) & 0xff) << 8)
                        | (bitmap.get(i).get(j) & 0xff);
                image.setRGB(j, i, intensity);
            }
        }
        return image;
    }

    private static final int RES_HEIGHT = 288;
    private static final int RES_WIDTH = 352;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("ERR: Expected three arguments (image path, m, n)");
            exit(1);
        }

        System.out.println("Begin Process\n");
        ArrayList<ArrayList<Integer>> orig = new SingleChannelImageParser(
                args[0],
                RES_HEIGHT,
                RES_WIDTH
        ).parseImage();
        ArrayList<ArrayList<Integer>> quantized = new Quantize(
                orig,
                Integer.parseInt(args[2]),
                Integer.parseInt(args[1]),
                RES_HEIGHT,
                RES_WIDTH
        ).getQuantized();
        new ImageDisplay(
                getImageFromBitmap(orig),
                getImageFromBitmap(quantized)
        ).showImg();
        System.out.println("End Process\n");
    }
}
