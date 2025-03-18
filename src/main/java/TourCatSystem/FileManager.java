package TourCatSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    // Static instance of the singleton
    private static FileManager instance;

    // Resource directory path
    private Path resourceDirectory;

    // Private constructor to prevent instantiation
    private FileManager() {
        // Default resource directory path - can be customized based on needs
        this.resourceDirectory = Paths.get("src", "main", "resources");

        // Create the directory if it doesn't exist
        try {
            if (!Files.exists(resourceDirectory)) {
                Files.createDirectories(resourceDirectory);
            }
        } catch (IOException e) {
            System.err.println("Error creating resource directory: " + e.getMessage());
        }
    }

    // Method to get the singleton instance
    public static synchronized FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    // Get resource directory as Path object
    public Path getResourceDirectory() {
        return resourceDirectory;
    }

    // Get resource directory as File object
    public File getResourceDirectoryAsFile() {
        return resourceDirectory.toFile();
    }

    // Get resource directory as String
    public String getResourceDirectoryPath() {
        return resourceDirectory.toString();
    }

    // Get a specific resource file or directory within the resource directory
    public Path getResource(String resourceName) {
        return resourceDirectory.resolve(resourceName);
    }

    // Set custom resource directory
    public void setResourceDirectory(String directoryPath) {
        this.resourceDirectory = Paths.get(directoryPath);

        // Create the directory if it doesn't exist
        try {
            if (!Files.exists(resourceDirectory)) {
                Files.createDirectories(resourceDirectory);
            }
        } catch (IOException e) {
            System.err.println("Error creating resource directory: " + e.getMessage());
        }
    }

    // Check if a resource exists
    public boolean resourceExists(String resourceName) {
        return Files.exists(getResource(resourceName));
    }
}