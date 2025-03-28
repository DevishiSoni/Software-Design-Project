import TourCatGUI.HomePage;

import javax.swing.*;


public class MainApplication {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            System.out.println("FlatLaf Dark theme applied successfully.");
        } catch (Exception ex) { // Catch broader Exception is okay here for setup
            System.err.println("Failed to initialize Flat LaF Dark theme:");
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new HomePage("DefaultUser"); // Or LoginWindow, etc.
       });
    }
}