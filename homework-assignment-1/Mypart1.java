/**
 * This file contains the SpatialAntiAliasing Class for part 1 of the assignment
 *
 */

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import static java.lang.System.exit;
// import static java.lang.System.out;

/**
 * Main class for the program:
 * - control sequence of execution
 * - handle image creation and manipulation
 *
 * @author rutvikpatel
 */
public class Mypart1 {

    /**
     * Nested class to render the images to the swing frame
     * @param orig original image to be displayed in the frame
     * @param sampled the sampled image to be displayed in the frame
     */
    private record ImageDisplay(BufferedImage orig, BufferedImage sampled) {

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

    /**
     * Create an empty image with only white pixels of given size
     * @param width width of the image - number of pixels in each line
     * @param height height of the image - number of lines in the image
     * @return the newly created white image
     */
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

    /**
     * Add a line to the image
     *
     * @param image image to which line needs to be added
     * @param x1 start of line (x coordinate)
     * @param y1 start of line (y coordinate)
     * @param x2 end of line (x coordinate)
     * @param y2 end of line (y coordinate)
     */
    private static void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawLine(x1, y1, x2, y2);
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Add the n lines (spokes) to the image
     *
     * @param img image that need to be manipulated
     * @param n number of lines to be added to the image
     */
    private static void addSpokesToImage(BufferedImage img, int n) {
        // define center (origin) about which to rotate the line endpoints and angle (radians) by which to rotate the lines
        int centerX = img.getWidth() / 2;
        int centerY =  img.getHeight() / 2;

        // define endpoints for the first line (ends are always at the center)
        int startX = 0;
        int startY = 0;
        double rotAngle = Math.toRadians((double)360 / n);

        // draw n lines starting with a line across the primary diagonal
        for (int i = 0; i < n; i++) {
            drawLine(img, startX, startY, centerX, centerY);

            // transform line coordinates by rotAngle about the origin to get new line endpoints
            int updatedStartX = (int)Math.round(
                    centerX +
                            (startX - centerX) * Math.cos(rotAngle) -
                            (startY - centerY) * Math.sin(rotAngle)
            );
            int updatedStartY = (int)Math.round(
                    centerY +
                            (startX - centerX) * Math.sin(rotAngle) +
                            (startY - centerY) * Math.cos(rotAngle)
            );

            startX = updatedStartX;
            startY = updatedStartY;
        }

    }

    /**
     * Perform the sampling on original image to fill in values in the new image
     * @param orig original image used to sample
     * @param sampled sampled image generated as a result of sampling
     * @param scaleFactor the factor by which the image is scaled down
     * @param antiAliasing boolean parameter to determine is anti-aliasing is to be performed or not
     */
    private static void sampleImage(BufferedImage orig, BufferedImage sampled, float scaleFactor, int antiAliasing) {
        for (int x = 0; x < sampled.getWidth(); x++) {
            for (int y = 0; y < sampled.getHeight(); y++) {
                int pix; // pixel is black by default
                if (antiAliasing == 1) {
                    // explore a 3x3 neighborhood to compute average as the sampled pixel value
                    int scaledX = (int)(x / scaleFactor);
                    int scaledY = (int)(y / scaleFactor);
                    int samplesExplored = 0;

                    // variables to store averages for individual channels
                    int r = 0x00;
                    int g = 0x00;
                    int b = 0x00;

                    for (int deltaX = -1; deltaX < 2; deltaX++) {
                        for (int deltaY = -1; deltaY < 2; deltaY++) {
                            int origSampleX = scaledX + deltaX;
                            int origSampleY = scaledY + deltaY;

                            if (origSampleX < 0 ||
                                    origSampleX >= orig.getWidth() ||
                                    origSampleY < 0 ||
                                    origSampleY >= orig.getHeight()
                            )
                                continue;

                            int samplePix = orig.getRGB(origSampleX, origSampleY);
                            // here int size is assumed to be 4 bytes
                            r += (samplePix >> 16 & 0x00ff);
                            g += (samplePix >> 8 & 0x0000ff);
                            b += (samplePix & 0x000000ff);
                            samplesExplored += 1;
                        }
                    }

                    byte rByte = (byte)(Math.min(255, r / samplesExplored));
                    byte gByte = (byte)(Math.min(255, g / samplesExplored));
                    byte bByte = (byte)(Math.min(255, b / samplesExplored));
                    pix = 0xff000000 | ((rByte & 0xff) << 16) | ((gByte & 0xff) << 8) | (bByte & 0xff);

                } else {
                    pix = orig.getRGB((int)(x / scaleFactor), (int)(y / scaleFactor));
                }
                sampled.setRGB(x, y, pix);
            }
        }
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

        // create original image
        BufferedImage orig = createEmptyImage(ORIG_IMG_WIDTH, ORIG_IMG_HEIGHT);
        addSpokesToImage(orig, n);

        // create an image using original image scaled by s
        BufferedImage sampled = createEmptyImage((int)(ORIG_IMG_WIDTH * s), (int)(ORIG_IMG_HEIGHT * s));
        sampleImage(orig, sampled, s, a);

        ImageDisplay out = new ImageDisplay(orig, sampled);
        out.showImg();

        // Terminate the program successfully
        //        exit(0);
    }
}