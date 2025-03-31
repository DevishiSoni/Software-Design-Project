package TourCatData;

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

    private final boolean testMode;  // Made final so it cannot be changed after initialization

    // Private constructor with testMode parameter
    private FileManager(boolean testMode) {
        this.testMode = testMode;  // Assign testMode early

        // Set resource directory based on mode
        this.resourceDirectory = testMode
                ? Paths.get("src", "test", "resources")
                : Paths.get("src", "main", "resources");

        // Create the directory if it doesn't exist
        try {
            if (!Files.exists(resourceDirectory)) {
                Files.createDirectories(resourceDirectory);
            }
        } catch (IOException e) {
            System.err.println("Error creating resource directory: " + e.getMessage());
        }
    }

    // Method to get the singleton instance with specific mode
    public static synchronized FileManager getInstance(boolean testMode) {
        if (instance == null || instance.testMode != testMode) {
            instance = new FileManager(testMode);
        }
        return instance;
    }

    // For backward compatibility, default to production mode
    public static synchronized FileManager getInstance() {
        return getInstance(instance != null && instance.testMode);
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

    public File getImageResourceFolder()
    {
        return getResourceFile("image");
    }

    // Check if a resource exists
    public boolean resourceExists(String resourceName) {
        return Files.exists(getResource(resourceName));
    }

    // Get current mode
    public boolean isTestMode() {
        return testMode;
    }

    public File getResourceFile(String fileName)
    {
        return new File(getResourceDirectoryPath() + File.separator + fileName);
    }

    public File getImageFile(String imageName)
    {
        return getResourceFile("image" + File.separator + imageName);
    }

    public File getDatabaseFile()
    {
        return getResourceFile("database.csv");
    }
}
