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
    private BufferedImage image;

    SingleChannelImageParser(String imagePath, int resHeight, int resWidth) {
        this.imagePath = imagePath;
        this.resHeight = resHeight;
        this.resWidth = resWidth;
        initImage();
    }

    void initImage() {
        image = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < resHeight; y++) {
            for(int x = 0; x < resWidth; x++) {
                // byte a = (byte) 255;
                byte r = (byte) 255;
                byte g = (byte) 255;
                byte b = (byte) 255;

                // set all channels to 1 for white image
                int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                image.setRGB(x, y, pix);
            }
        }
    }

    void parseImage() {
        Path path = Paths.get(imagePath);
        try {
            byte[] fileContents =  Files.readAllBytes(path);

            for (int i = 0; i < fileContents.length; i++) {
                int intensity = fileContents[i];
                int pixel = 0xff000000 | ((intensity & 0xff)) | ((intensity & 0xff) << 8) | (intensity & 0xff);
                image.setRGB(i % resWidth, (int) Math.floor((float)(i / resWidth)), pixel);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BufferedImage getParsedImage() { return image; }
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
        BufferedImage parsedImg = scip.getParsedImage();
        ImageDisplay id = new ImageDisplay(parsedImg);
        id.showImg();
    }
}
