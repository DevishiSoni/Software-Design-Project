package TourCatGUI; // Assuming this is the correct package

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException; // Import IOException
import java.net.URL;       // Import URL
import javax.imageio.ImageIO;

// Keep this class separate unless it's ONLY used by HomePage
class BackgroundPanel extends JPanel {
    private BufferedImage image;
    private float alpha; // Transparency level (0.0 - 1.0)

    /**
     * Creates a panel with a background image loaded from a URL.
     *
     * @param imageURL The URL pointing to the image resource.
     * @param alpha    The transparency level (0.0f to 1.0f).
     */
    public BackgroundPanel (URL imageURL, float alpha) { // Changed parameter type
        this.alpha = alpha;
        if (imageURL == null) {
            System.err.println("Error: Background image URL is null.");
            return; // Cannot load image
        }
        try {
            // Load the image directly from the URL
            image = ImageIO.read(imageURL);
            if (image == null) {
                System.err.println("Error: ImageIO.read returned null for URL: " + imageURL);
            }
        } catch (IOException e) { // Catch IOException
            System.err.println("Error loading background image from URL: " + imageURL);
            e.printStackTrace();
            // image will remain null, paintComponent will handle it
        }
    }

    @Override
    protected void paintComponent (Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Apply transparency and draw the image (existing logic is fine)
            Graphics2D g2d = (Graphics2D) g;
            // Ensure alpha is within valid range
            float effectiveAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, effectiveAlpha));
            // Draw image scaled to fit the panel
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            // Restore default composite
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            // Optional: Draw a placeholder or error message if image failed to load
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.drawString("Background image failed to load", 10, 20);
        }
    }

    // Removed the main method from here, testing should be separate
} 