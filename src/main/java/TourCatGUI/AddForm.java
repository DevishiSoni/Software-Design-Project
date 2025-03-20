package TourCatGUI;

import TourCatSystem.ChangeDatabase;
import TourCatSystem.FileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddForm extends JFrame {

    public JTextField nameField, cityField, provinceField, categoryField;
    public JButton selectImageButton, submitButton, cancelButton;
    public JLabel chosenImageLabel, submissionReplyLabel;

    File saveFile = FileManager.getInstance().getResourceFile("test.csv");
    File imageFile = null;
    String imageName = "";
    String fileType = "";

    public AddForm(String username) {
        setTitle("Add Form");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        nameField = new JTextField();
        cityField = new JTextField();
        provinceField = new JTextField();
        categoryField = new JTextField();
        selectImageButton = new JButton("Select Image");
        submitButton = new JButton("Submit");
        cancelButton = new JButton("Cancel");
        chosenImageLabel = new JLabel("Chosen Image: " + imageName);

        JLabel intro = new JLabel("Please enter the name and location of the landmark to be added");
        intro.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        submissionReplyLabel = new JLabel(" ");
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 15));

        add(intro);
        add(new JLabel("Landmark name:"));
        add(nameField);
        add(new JLabel("Landmark city:"));
        add(cityField);
        add(new JLabel("Landmark province:"));
        add(provinceField);
        add(new JLabel("Landmark category:"));
        add(categoryField);
        add(new JLabel("Select image of form .jpg or .png:"));
        add(selectImageButton);
        add(chosenImageLabel);
        add(submissionReplyLabel);
        add(submitButton);
        add(cancelButton);

        submitButton.addActionListener(e ->
        {
            addFieldsToFile(saveFile);
        });

        cancelButton.addActionListener(e -> {
            new HomePage(username);
            dispose();
        });

        selectImageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imageFile = chooser.getSelectedFile();
                imageName = imageFile.getName();
                fileType = imageName.split("\\.")[1];
                chosenImageLabel.setText("Chosen image: " + imageName);
            }
        });
    }

    private void addFieldsToFile(File f) {

        String id = String.format("%05d",ChangeDatabase.getMaxId(f) + 1);
        String name = nameField.getText();
        String location = cityField.getText();
        String province = provinceField.getText();
        String category = categoryField.getText();

        if (!isNameLocationValid()){
            submissionReplyLabel.setText("Please enter the requested information of the landmark");
            return;
        }

        if (!isImageValid(imageName)){
            submissionReplyLabel.setText("Please select a valid image");
            return;
        }

        try {
            BufferedImage bimg = ImageIO.read(imageFile);
            File outputfile = new File(FileManager.getInstance().createImagePath(id+"."+fileType));
            ImageIO.write(bimg,fileType, outputfile);
        } catch (IOException e) {
            System.out.println("Image could not be read");
        }

        ArrayList<String> newLandmark = new ArrayList<String>();
        newLandmark.add(id);
        newLandmark.add(name);
        newLandmark.add(location);
        newLandmark.add(province);
        newLandmark.add(category);
        newLandmark.add(fileType);

        boolean success = ChangeDatabase.addToFile(newLandmark, f.getAbsolutePath());

        if (!success) {
            submissionReplyLabel.setText("Failed to add location to the database");
            return;
        }

        submissionReplyLabel.setText("Location successfully added to the database");
        nameField.setText("");
        cityField.setText("");
        provinceField.setText("");
        categoryField.setText("");
        imageFile = null;
        imageName = "";
        chosenImageLabel.setText("Chosen Image: " + imageName);
    }

    boolean isNameLocationValid()
    {
        return !nameField.getText().isBlank() && !cityField.getText().isBlank();
    }

    boolean isImageValid(String imageName){
        return imageName.matches("[a-zA-Z]+\\.(jpg|png)");
    }

    public void setSaveFile(File file){
        this.saveFile = file;
    }

    public String getSaveFileAbsPath(){
        return saveFile.getAbsolutePath();
    }

    public static void main(String[] args) {
        AddForm af = new AddForm("tester");

        af.setSaveFile(FileManager.getInstance(true).getResourceFile("test2.csv"));

        af.setVisible(true);
    }
}
