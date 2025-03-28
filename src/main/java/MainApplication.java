import TourCatGUI.HomePage;

import javax.swing.*;


// Assuming your application starts in a class named 'MainApplication'
// or perhaps directly in 'HomePage' or similar.
public class MainApplication { // Or HomePage, etc.

    public static void main(String[] args) {
        // --- Set FlatLaf Dark Look and Feel ---
        try {
            // Use FlatDarkLaf.setup() for recommended setup
            // Or UIManager.setLookAndFeel(new FlatDarkLaf());
            // Or UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            System.out.println("FlatLaf Dark theme applied successfully.");
        } catch (Exception ex) { // Catch broader Exception is okay here for setup
            System.err.println("Failed to initialize Flat LaF Dark theme:");
            ex.printStackTrace();
            // Application can continue with the default L&F if FlatLaf fails
        }
        // --- End Look and Feel Setup ---


        // --- Start your GUI on the Event Dispatch Thread (EDT) ---
        SwingUtilities.invokeLater(() -> {
            // Replace this with your actual application starting point
            // For example:
            new HomePage("DefaultUser"); // Or LoginWindow, etc.

            // Or if CatalogView is the first window:
            // new CatalogViewLogic("DefaultUser");
        });
        // --- End GUI Start ---
    }
}