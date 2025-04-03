import TourCatGUI.HomePage; // Ensure correct import
import javax.swing.*;

public class MainApplication {

    public static void main(String[] args) {
        // Apply FlatLaf Look and Feel (optional but recommended for better UI)
        try {
            // You can choose other FlatLaf themes: FlatDarkLaf, FlatIntelliJLaf, FlatMacDarkLaf etc.
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            System.out.println("FlatLaf Light theme applied successfully.");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.err.println("Failed to initialize FlatLaf theme. Using default Look and Feel.");
            e.printStackTrace(); // Log the error for debugging
        }

        // Use SwingUtilities.invokeLater to ensure GUI creation happens on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Instantiate HomePage directly, passing a default or null username
            // since login is removed.
            new HomePage("DefaultUser"); // Or pass null if preferred: new HomePage(null);
        });
    }
}