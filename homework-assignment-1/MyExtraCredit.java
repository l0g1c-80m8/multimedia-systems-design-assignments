/**
 * This file contains the TemporalAntiAliasing Class for part 3 of the assignment
 *
 * Here part 1 is combined with part 2 of the assignment along with temporal anti-aliasing
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.System.*;

/**
 * Main class for the program:
 * - control sequence of execution
 * - handle image creation and manipulation for displaying the video
 *
 * @author rutvikpatel
 */
public class MyExtraCredit {

    private enum RenderLoopTarget {
        LEFT, RIGHT
    }

    /**
     * Class to control the generation of original video and sampling for the sampled video
     */
    private static class RenderLoop {
        // references for game loop design pattern:
        // - https://java-design-patterns.com/patterns/game-loop/#explanation
        // - https://gist.github.com/martincruzot/a55d744a77448f1adaa9

        private enum RenderStatus {
            RUNNING
        }
        private RenderStatus status;
        private final int n;
        private final double s;
        private final double fps;
        private final int a;
        private final double sf;
        private final VideoDisplay vd;
        private final RenderLoopTarget renderTarget;

        /**
         * Method used to create a runnable instance for the class
         */
        public void run() {
            status = RenderStatus.RUNNING;
            Thread renderThread = new Thread(this::processRenderLoop);
            renderThread.start();
        }

        /**
         * Check if the thread is in RUNNING state
         * @return (thread in RUNNING state?)
         */
        public boolean isRendering() {
            return status == RenderStatus.RUNNING;
        }

        /**
         * ask video display handle to update the contents in the frame
         */
        protected void render() {
            // force an update to the screen
            vd.updateFrames(this.renderTarget);
        }

        /**
         * Execute image update
         * @param offsetAngle angle by which to offset the first angle in the image
         */
        protected void update(double offsetAngle) {
            // always update the original video frame
            this.vd.setOrig(addSpokesToImage(createEmptyImage(ORIG_IMG_HEIGHT, ORIG_IMG_WIDTH), this.n, offsetAngle));
            // only update the render video frame when it is to be sampled
            if (this.renderTarget == RenderLoopTarget.RIGHT)
                this.vd.setSampled(
                        sampleImage(
                                this.vd.getOrig(),
                                createEmptyImage(
                                        (int)(ORIG_IMG_HEIGHT * sf),
                                        (int)(ORIG_IMG_WIDTH * sf)
                                ),
                                this.sf,
                                this.a
                        )
                );
        }

        private double nanoToSeconds(double nanoTime) {
            return nanoTime / 1e9;
        }

        private double secondsToNano(double seconds) {
            return seconds * 1e9;
        }

        /**
         * Runner method for the instance used to instantiate a thread with the render loop
         */
        protected void processRenderLoop() {
            double updateInterval = LOOP_DELAY;
            if (this.renderTarget == RenderLoopTarget.RIGHT)
                updateInterval = secondsToNano(1.0d / this.fps);
            long previousTime = nanoTime();
            double errorAcc = 0.0d;
            double totalTime = 0.0d;

            while (isRendering()) {
                long currentTime = nanoTime();
                long frameTime = currentTime - previousTime;
                errorAcc += frameTime;
                while (errorAcc >= updateInterval) {
                    update((2 * Math.PI * this.s * nanoToSeconds(totalTime)) % (2 * Math.PI));
                    render();
                    errorAcc -= updateInterval;
                    totalTime += updateInterval;
                }
                previousTime = currentTime;
            }
        }

        /**
         * Constructor for the RenderLoop class
         * @param n the number of lines for each frame of the video
         * @param s number of rotations/revolutions per second for original video
         * @param fps sampling rate for the sampled video
         * @param a apply anti-aliasing or not
         * @param sf scale factor of the sampled video
         * @param vd VideoDisplay instance to apply updates and render
         * @param renderTarget the target for the render loop (original/sampled)
         */
        RenderLoop(
                int n,
                double s,
                double fps,
                int a,
                double sf,
                VideoDisplay vd,
                RenderLoopTarget renderTarget
        ) {
            this.vd = vd;
            this.n = n;
            this.s = s;
            this.fps = fps;
            this.a = a;
            this.sf = sf;
            this.renderTarget = renderTarget;
        }
    }

    /**
     * Nested class to render the videos to the swing frame side-by-side
     */
    private static class VideoDisplay {

        private BufferedImage orig;
        private BufferedImage sampled;
        private JFrame frame;
        private JLabel lbIm1;
        private JLabel lbIm2;

        /**
         * Constructor for the inner class to hold the images (frames) to be rendered at the moment for the video
         * @param orig Starting frame for the source video
         * @param sampled Starting frame for the sampled video
         */
        private VideoDisplay(BufferedImage orig, BufferedImage sampled) {
            this.orig = orig;
            this.sampled = sampled;
            initializeFrame();
        }

        /**
         * Initialize the JLabel image icons that will be rendered
         */
        private void initializeFrame() {
            frame = new JFrame();
            GridBagLayout gLayout = new GridBagLayout();
            frame.getContentPane().setLayout(gLayout);

            JLabel lbText1 = new JLabel("Original video (Left)");
            JLabel lbText2 = new JLabel("Sampled video (Right)");
            lbText1.setHorizontalAlignment(SwingConstants.CENTER);
            lbText2.setHorizontalAlignment(SwingConstants.CENTER);

            lbIm1 = new JLabel(new ImageIcon(orig));
            lbIm2 = new JLabel(new ImageIcon(sampled));

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

        /**
         * Setter for the orig member identifier
         * @param orig new frame for the left video
         */
        public void setOrig(BufferedImage orig) {
            this.orig = orig;
        }

        /**
         * Getter for the orig member identifier
         */
        public BufferedImage getOrig() {
            return this.orig;
        }

        /**
         * Setter for the sampled member identifier
         * @param sampled new frame for the right video
         */
        public void setSampled(BufferedImage sampled) {
            this.sampled = sampled;
        }

        /**
         * Update the frames displayed in the frames object
         */
        public void updateFrames(RenderLoopTarget renderLoopTarget) {
            if (renderLoopTarget == RenderLoopTarget.LEFT)
                lbIm1.setIcon(new ImageIcon(this.orig));
            else lbIm2.setIcon(new ImageIcon(this.sampled));
            frame.repaint();
        }
    }

    private final static int ORIG_IMG_WIDTH = 512; // number of pixes in each row of the image
    private final static int ORIG_IMG_HEIGHT = 512; // number of rows in each image
    private final static double LOOP_DELAY = 1e7; // time before successive renders in ms (10 ns expressed as 1e7ms)

    /**
     * Create an empty image with only white pixels of given size
     * @return the newly created white image
     */
    private static BufferedImage createEmptyImage(int height, int width) {
        BufferedImage img = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

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
    private static void drawLine(BufferedImage image, int x1, int y1, int x2, int y2, Color color) {
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
        g.drawLine(x1, y1, x2, y2);
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Add the n lines (spokes) to the image
     *
     * @param img image that need to be manipulated
     * @param n number of lines to be added to the image
     * @return the image with spokes
     */
    private static BufferedImage addSpokesToImage(BufferedImage img, int n, double offsetAngle) {
        // define center (origin) about which to rotate the line endpoints and angle (radians) by which to rotate the lines
        int centerX = img.getWidth() / 2;
        int centerY =  img.getHeight() / 2;

        // apply an initial rotation first based on the counter and then draw the line
        // define endpoints for the first line (as determined by the initial rotation on (0, 0))
        int startX = (int)Math.round(
                centerX +
                        -centerX * Math.cos(offsetAngle) -
                        - centerY * Math.sin(offsetAngle)
        );
        int startY = (int)Math.round(
                centerY +
                        -centerX * Math.sin(offsetAngle) +
                        -centerY * Math.cos(offsetAngle)
        );

        // draw the first line with red color to test
        drawLine(img, startX, startY, centerX, centerY, Color.RED);

        // calculate the rotation angle between the spokes
        double rotAngle = 2 * Math.PI / n;

        // draw n lines starting with a line across the primary diagonal
        for (int i = 1; i < n; i++) {

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

            drawLine(img, startX, startY, centerX, centerY, Color.BLACK);
        }
        return img;
    }

    /**
     * Perform the sampling on original image to fill in values in the new image
     * @param orig original image used to sample
     * @param sampled sampled image generated as a result of sampling
     * @param scaleFactor the factor by which the image is scaled down
     * @param antiAliasing boolean parameter to determine is anti-aliasing is to be performed or not
     */
    private static BufferedImage sampleImage(BufferedImage orig, BufferedImage sampled, double scaleFactor, int antiAliasing) {
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
        return sampled;
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            out.println("ERR: Expected three arguments (n, s, fps, a, sf)");
            // Terminate the program unsuccessfully
            exit(1);
        }

        // read command line arguments
        int n = Integer.parseInt(args[0]);
        double s = Double.parseDouble(args[1]);
        double fps = Double.parseDouble(args[2]);
        int a = Integer.parseInt(args[3]);
        double sf = Double.parseDouble(args[4]);

        // create an object for video display class
        VideoDisplay vd = new VideoDisplay(
                createEmptyImage(ORIG_IMG_HEIGHT, ORIG_IMG_WIDTH),
                createEmptyImage((int)(ORIG_IMG_HEIGHT * sf), (int)(ORIG_IMG_WIDTH * sf))
        );

        // create and run the render loops
        Runnable leftRunner = new RenderLoop(n, s, fps, a, sf, vd, RenderLoopTarget.LEFT)::run;
        Runnable rightRunner = new RenderLoop(n, s, fps, a, sf, vd, RenderLoopTarget.RIGHT)::run;
        leftRunner.run();
        rightRunner.run();
    }
}

