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
import java.util.ArrayList;
import java.util.Random;

import static java.lang.System.exit;
// import static java.lang.System.out;

class ImageDisplay {
    private final BufferedImage image;

    ImageDisplay(BufferedImage image) {
        this.image = image;
    }
    public void showImg() {

        // Create a new
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbText1 = new JLabel("Original image (Left)");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbIm1 = new JLabel(new ImageIcon(image));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx =1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(20, 40, 20, 40);
        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

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

    ArrayList<Pair<Integer, Integer>> getParsedImageInR2() {
        ArrayList<Pair<Integer, Integer>> imgVectors = new ArrayList<>();
        for (int i = 0; i < pixels.length; i += 2) {
            imgVectors.add(new Pair<>(0x000000ff & pixels[i], 0x000000ff & pixels[i + 1]));
        }
        return imgVectors;
    }
}

class Quantize {
    private final ArrayList<Pair<Integer, Integer>> imgVectors;
    private final int n;

    Quantize(ArrayList<Pair<Integer, Integer>> imgVectors, int n) {
        this.imgVectors = imgVectors;
        this.n = n;
    }

    public void quantize() {
        ArrayList<Pair<Integer, Integer>> codebookVectors = new ArrayList<>(n);
        Random prng = new Random();
        for (int i = 0; i < n; i++) {
            int idx = prng.nextInt(imgVectors.size());
            Pair<Integer, Integer> vec = imgVectors.get(idx);
            codebookVectors.add(new Pair<>(vec.getKey(), vec.getValue()));
        }
        System.out.println(codebookVectors.size());
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
        BufferedImage parsedImg = scip.getParsedImageAsGray();
        ImageDisplay id = new ImageDisplay(parsedImg);
        id.showImg();
        Quantize q = new Quantize(scip.getParsedImageInR2(), n);
        q.quantize();
    }
}
