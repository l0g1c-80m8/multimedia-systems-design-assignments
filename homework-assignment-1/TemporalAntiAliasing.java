/**
 * This file contains the TemporalAntiAliasing Class for parts 2 and 3 of the assignment
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
 * - handle image creation and manipulation for displaying the video
 *
 * @author rutvikpatel
 */
public class TemporalAntiAliasing {

    private enum RenderLoopTarget {
        LEFT, RIGHT
    }

    private static class RenderLoop {
        // references for game loop design pattern:
        // - https://java-design-patterns.com/patterns/game-loop/#explanation
        // - https://gist.github.com/martincruzot/a55d744a77448f1adaa9

        private enum RenderStatus {
            RUNNING, STOPPED
        }
        private RenderStatus status;
        private Thread renderThread;
        private final int n;
        private long startTime;
        private final VideoDisplay vd;
        private long counter;
        private final double rateOfRotation;
        private final RenderLoopTarget renderTarget;
        private final long renderRate;

        public void run() {
            status = RenderStatus.RUNNING;
            renderThread = new Thread(this::processRenderLoop);
            renderThread.start();
        }

        public void stop() {
            status = RenderStatus.STOPPED;
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isRendering() {
            return status == RenderStatus.RUNNING;
        }

        protected void render() {
            // force an update to the screen
            vd.updateFrames();
        }

        protected void update() {
            double initialRotAngle = Math.toRadians(this.rateOfRotation * ((RENDER_RATE / 1000.0d) * this.counter));
            // If fps match, sample the current original image and construct display image
            // set the new frame for the videos
            if (this.renderTarget == RenderLoopTarget.LEFT)
                vd.setOrig(addSpokesToImage(createEmptyImage(), n, initialRotAngle));
            else
                vd.setSampled(this.vd.getOrig());

            // reset the counter when the image returns to the first frame position to prevent overflow of the counter
            if (initialRotAngle / Math.toRadians(0) == 0.0d)
                this.counter = 0L;
            // update the counter for next iteration
            this.counter++;
        }

        protected void processRenderLoop() {
            while (isRendering()) {
                long finishTime = System.currentTimeMillis();
                long timeElapsed = finishTime - startTime;
                if (timeElapsed > this.renderRate) {
                    update();
                    render();
                    startTime = System.currentTimeMillis();
                }
            }
        }

        RenderLoop(int n, double rateOfRotation, VideoDisplay vd, RenderLoopTarget renderTarget, long renderRate) {
            this.n = n;
            this.renderRate = renderRate;
            this.status = RenderStatus.STOPPED;

            // store the object for VideoDisplay in a handle
            this.vd = vd;

            // create a counter to keep track of the frame to be displayed
            this.counter = 0L;
            // store the rate of rotation/revolutions of the image in radians / sec
            this.rateOfRotation = rateOfRotation;

            // set startTime default to now (first frame can render at higher fps)
            this.startTime = System.currentTimeMillis();

            // set the target for this render loop
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
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        public void updateFrames() {
            lbIm1.setIcon(new ImageIcon(this.orig));
            lbIm2.setIcon(new ImageIcon(this.sampled));
            frame.repaint();
        }
    }

    private final static int ORIG_IMG_WIDTH = 512; // number of pixes in each row of the image
    private final static int ORIG_IMG_HEIGHT = 512; // number of rows in each image
    private final static long RENDER_RATE = 1L; // rate at which to render the images in milliseconds

    /**
     * Create an empty image with only white pixels of given size
     * @return the newly created white image
     */
    private static BufferedImage createEmptyImage() {
        BufferedImage img = new BufferedImage(ORIG_IMG_WIDTH, ORIG_IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for(int y = 0; y < ORIG_IMG_HEIGHT; y++) {
            for(int x = 0; x < TemporalAntiAliasing.ORIG_IMG_WIDTH; x++) {
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
     * @return the image with spokes
     */
    private static BufferedImage addSpokesToImage(BufferedImage img, int n, double initialRotAngle) {
        // define center (origin) about which to rotate the line endpoints and angle (radians) by which to rotate the lines
        int centerX = img.getWidth() / 2;
        int centerY =  img.getHeight() / 2;

        // apply an initial rotation first based on the counter and then draw the line
        // define endpoints for the first line (as determined by the initial rotation on (0, 0))
        int startX = (int)Math.round(
                centerX +
                        -centerX * Math.cos(initialRotAngle) -
                        - centerY * Math.sin(initialRotAngle)
        );
        int startY = (int)Math.round(
                centerY +
                        -centerX * Math.sin(initialRotAngle) +
                        -centerY * Math.cos(initialRotAngle)
        );

        // calculate the rotation angle between the spokes
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
        return img;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("ERR: Expected three arguments (n, s, fps)");
            // Terminate the program unsuccessfully
            exit(1);
        }

        // read command line arguments
        int n = Integer.parseInt(args[0]);
        double s = Double.parseDouble(args[1]);
        double fps = Double.parseDouble(args[2]);

        // create an object for video display class
        VideoDisplay vd = new VideoDisplay(createEmptyImage(), createEmptyImage());

        // create and run the render loops
        RenderLoop rlLeft = new RenderLoop(
                n,
                s * 360.0d,
                vd,
                RenderLoopTarget.LEFT,
                RENDER_RATE
        );
        RenderLoop rlRight = new RenderLoop(
                n,
                0.0d,
                vd,
                RenderLoopTarget.RIGHT,
                Math.round(1000.0d / fps)
        );
        rlLeft.run();
        rlRight.run();
    }
}

