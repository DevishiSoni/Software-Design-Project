package TourCatGUI;

import TourCatData.LocationData; // Assuming DTO package
import TourCatData.FileManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DetailView extends JDialog { // JDialog is often better for temporary detail views

    // Could inject FileManager, or use getInstance() for now
    private final FileManager fileManager = FileManager.getInstance();

    public DetailView(Frame owner, LocationData data) { // Pass parent frame for modality
        super(owner, "Location Details: " + data.getName(), true); // Modal dialog
        // Or: public LocationDetailView(LocationData data) { setTitle(...); } if using JFrame

        setSize(400, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Dispose only this dialog

        // --- Text Panel ---
        JPanel textPanel = new JPanel(new GridLayout(0, 1, 5, 5)); // Flexible rows, add gaps
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        textPanel.add(createStyledLabel("Name: " + data.getName()));
        textPanel.add(createStyledLabel("City: " + data.getCity()));
        textPanel.add(createStyledLabel("Province: " + data.getProvince()));
        textPanel.add(createStyledLabel("Category: " + data.getCategory()));
        add(textPanel, BorderLayout.NORTH);

        // --- Image Panel ---
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200)); // Suggest size
        loadAndSetImage(imageLabel, data.getId());
        add(imageLabel, BorderLayout.CENTER);

        pack(); // Adjust size to fit components
        setLocationRelativeTo(owner); // Center relative to parent
    }

    // Helper for consistent label styling
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14)); // Example style
        return label;
    }

    // Image loading logic extracted
    private void loadAndSetImage(JLabel imageLabel, String id) {
        // Construct potential paths
        File pngFile = fileManager.getImageFile(id + ".png");
        File jpgFile = fileManager.getImageFile(id + ".jpg");
        File imageFile = null;

        if (pngFile.exists()) {
            imageFile = pngFile;
        } else if (jpgFile.exists()) {
            imageFile = jpgFile;
        }

        System.out.println("Attempting to load image for ID " + id + ": " + (imageFile != null ? imageFile.getAbsolutePath() : "Not Found"));

        if (imageFile != null && imageFile.exists()) {
            try {
                ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                // Scale gracefully
                int width = 250; // Max width
                int height = 250; // Max height
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();

                if (imgWidth > width || imgHeight > height) {
                    float scale = Math.min((float) width / imgWidth, (float) height / imgHeight);
                    int newWidth = (int) (imgWidth * scale);
                    int newHeight = (int) (imgHeight * scale);
                    Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imageLabel.setIcon(icon); // Use original if small enough
                }
                imageLabel.setText(null); // Clear any previous text
            } catch (Exception e) {
                System.err.println("Error loading image: " + imageFile.getAbsolutePath() + " - " + e.getMessage());
                imageLabel.setText("Error Loading Image");
                imageLabel.setIcon(null);
            }
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setIcon(null);
        }
    }
}