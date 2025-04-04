package TourCatSystem;

import TourCatGUI.Forms.AddFormLogic;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AppDataManager {

    public static final String INTERNAL_DB_PATH = "/database.csv";
    public static final String WRITABLE_DB_FILENAME = "userdata_database.csv";
    private static final String WRITABLE_IMAGE_DIRNAME = "images"; // Subdirectory for images


    public static final File writableDatabaseFile; // Path to the writable database

    static {
        try {
            writableDatabaseFile = initializeWritableDatabase();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Path writableImageDirectory = writableDatabaseFile.getParentFile().toPath().resolve(WRITABLE_IMAGE_DIRNAME);

    /**
     * Copies the selected image file to the application's writable image folder,
     * renaming it based on the location's ID.
     *
     * @param sourceImageFile The image file selected by the user.
     * @param locationId      The ID assigned to the new location (used for filename).
     * @return true if the image was copied successfully, false otherwise.
     */
    public static boolean saveImageToWritableLocation (File sourceImageFile, String locationId) throws Exception { // Renamed for clarity
        try {
            // Destination is the writableImageDirectory determined in constructor
            if (!Files.exists(writableImageDirectory)) {
                Files.createDirectories(writableImageDirectory); // Ensure it exists
                System.out.println("Re-created missing writable image directory: " + writableImageDirectory);
            }

            // Determine the file extension
            String extension = FilenameUtils.getExtension(sourceImageFile.getName());
            if (extension == null || extension.isEmpty()) {
                System.err.println("Warning: Selected image has no extension. Defaulting to .jpg");
                extension = "jpg"; // Default extension or handle differently
            }

            // Create the destination filename (e.g., "00015.png")
            String destinationFilename = locationId + "." + extension.toLowerCase();
            Path destinationPath = writableImageDirectory.resolve(destinationFilename);

            // Copy the file, replacing if it somehow already exists
            Files.copy(sourceImageFile.toPath(), destinationPath,
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Image successfully saved to writable location: " + destinationPath);
            return true;

        } catch (IOException e) {
            System.err.println("Error saving image file to " + writableImageDirectory + ": " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error Saving to file: " + e.getMessage());
        } catch (Exception e) { // Catch unexpected errors
            System.err.println("Unexpected error saving image: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Unexpected Error saving image" + e.getMessage());
        }
    }


    private static File initializeWritableDatabase () throws IOException, URISyntaxException {
        Path applicationDirectory = getApplicationDirectory();
        Path externalDbPath = applicationDirectory.resolve(WRITABLE_DB_FILENAME);
        File externalDbFile = externalDbPath.toFile();

        if (!externalDbFile.exists()) {
            System.out.println("Writable database not found at " + externalDbPath + ". Copying default...");
            URL internalDbUrl = AppDataManager.class.getResource(INTERNAL_DB_PATH);
            if (internalDbUrl == null) {
                throw new IOException("Could not find internal resource: " + INTERNAL_DB_PATH);
            }

            try (InputStream internalStream = AppDataManager.class.getResourceAsStream(INTERNAL_DB_PATH)) {
                if (internalStream == null) {
                    throw new IOException("Could not open internal resource stream: " + INTERNAL_DB_PATH);
                }
                Files.createDirectories(externalDbPath.getParent());
                Files.copy(internalStream, externalDbPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Default database copied to: " + externalDbPath);
            } catch (IOException e) {
                throw new IOException("Failed to copy internal database to " + externalDbPath, e);
            }
        } else {
            System.out.println("Using existing writable database at: " + externalDbPath);
        }
        return externalDbFile;
    }

    /**
     * Helper to get the directory where the JAR/application is running.
     * Falls back to user home directory if running location is problematic.
     */
    private static Path getApplicationDirectory () throws URISyntaxException {
        try {
            Path jarPath = Paths.get(AddFormLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(jarPath)) {
                return jarPath; // IDE
            }
            return jarPath.getParent(); // JAR
        } catch (Exception e) {
            System.err.println("Warning: Could not determine application directory. Falling back to user home. Error: " + e.getMessage());
            String userHome = System.getProperty("user.home");
            Path userHomePath = Paths.get(userHome, "TourCatData"); // Subfolder
            try {
                Files.createDirectories(userHomePath);
            } catch (IOException ioException) {
                System.err.println("Error creating fallback directory: " + ioException.getMessage());
                return Paths.get("").toAbsolutePath(); // Last resort CWD
            }
            return userHomePath;
        }
    }
}
