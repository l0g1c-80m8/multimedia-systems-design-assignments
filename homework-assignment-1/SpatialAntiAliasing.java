/**
 * This file contains the SpatialAntiAliasing Class for part 1 of the assignment
 *
 */

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import static java.lang.System.exit;

/**
 * Main class for the program:
 * - control sequence of execution
 * - handle image creation and manipulation
 *
 * @author rutvikpatel
 */
public class SpatialAntiAliasing {
    private record ImageDisplay(BufferedImage orig, BufferedImage sampled) {

        /**
         * @param orig    the originally constructed image to be displayed on the left
         * @param sampled the sampled image to be displayed on the right
         */
        private ImageDisplay {
        }

            /**
             * Render the two images side by side in a swing frame
             */
            public void showImg() {

                // Create a new
                JFrame frame = new JFrame();
                GridBagLayout gLayout = new GridBagLayout();
                frame.getContentPane().setLayout(gLayout);

                JLabel lbText1 = new JLabel("Original image (Left)");
                JLabel lbText2 = new JLabel("Sampled image (Right)");
                lbText1.setHorizontalAlignment(SwingConstants.CENTER);
                lbText2.setHorizontalAlignment(SwingConstants.CENTER);

                JLabel lbIm1 = new JLabel(new ImageIcon(orig));
                JLabel lbIm2 = new JLabel(new ImageIcon(sampled));

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 0.5;
                c.gridx = 0;
                c.gridy = 0;
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
    private static BufferedImage createEmptyImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                // byte a = (byte) 255;
                byte r = (byte) 255;
                byte g = (byte) 255;
                byte b = (byte) 255;

                // set all channels to 1 for white image
                int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                img.setRGB(x, y, pix);
            }
        }

        return img;
    }

    private static void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawLine(x1, y1, x2, y2);
        g.drawImage(image, 0, 0, null);
    }

    private static void addSpokesToImage(BufferedImage img, int n) {
        drawLine(img, 0, 0, img.getWidth() - 1, img.getHeight() - 1);
    }

    private final static int ORIG_IMG_WIDTH = 512;
    private final static int ORIG_IMG_HEIGHT = 512;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("ERR: Expected three arguments (n, s, a)");
            // Terminate the program unsuccessfully
            exit(1);
        }

        int n = Integer.parseInt(args[0]);
        float s = Float.parseFloat(args[1]);
        int a = Integer.parseInt(args[2]);

        BufferedImage orig = createEmptyImage(ORIG_IMG_WIDTH, ORIG_IMG_HEIGHT);
        addSpokesToImage(orig, n);

        ImageDisplay out = new ImageDisplay(orig, orig);
        out.showImg();

        // Terminate the program successfully
        //        exit(0);
    }
}
