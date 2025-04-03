package TourCatSystem; // Or a more general utility package like TourCatUtil

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;

/**
 * Manages paths for application data files, ensuring they are stored
 * in a writable location (typically user home directory).
 * Handles initialization by copying default resources if writable files don't exist.
 */
public class AppDataManager {
    // --- Configuration Constants ---
    private static final String APP_DATA_SUBDIR_NAME = ".TourCatData"; // Subdirectory in user home (leading dot for *nix hidden)
    public static final String WRITABLE_DB_FILENAME = "userdata_database.csv";
    public static final String WRITABLE_CREDENTIALS_FILENAME = "user_credentials.txt";
    public static final String WRITABLE_IMAGE_DIRNAME = "images";

    // Internal resource paths (relative to classpath root)
    public static final String INTERNAL_DB_PATH = "/database.csv"; // Leading slash is important
    public static final String INTERNAL_CREDENTIALS_PATH = "/default_credentials.txt";
    public static final String INTERNAL_IMAGE_RESOURCE_PATH_PREFIX = "/image/"; // For finding default/built-in images

    private static Path appDataDirectory = null; // Cache the directory path

    /**
     * Gets the application's writable data directory (e.g., ~/.TourCatData).
     * Creates it if it doesn't exist.
     *
     * @return The Path to the application data directory.
     * @throws IOException If the directory cannot be created.
     */
    public static synchronized Path getAppDataDirectory() throws IOException {
        if (appDataDirectory == null) {
            String userHome = System.getProperty("user.home");
            if (userHome == null || userHome.isEmpty()) {
                throw new IOException("Could not determine user home directory.");
            }
            Path homePath = Paths.get(userHome);
            appDataDirectory = homePath.resolve(APP_DATA_SUBDIR_NAME);
            try {
                if (!Files.exists(appDataDirectory)) {
                    Files.createDirectories(appDataDirectory);
                    System.out.println("Created application data directory: " + appDataDirectory);
                } else {
                    System.out.println("Using existing application data directory: " + appDataDirectory);
                }
            } catch (IOException e) {
                System.err.println("FATAL: Could not create application data directory: " + appDataDirectory);
                throw e; // Re-throw critical error
            }
        }
        return appDataDirectory;
    }

    /**
     * Gets the full path to the writable database file.
     * Does NOT guarantee the file exists; use initializeResourceFile for that.
     *
     * @return Path to the writable database file.
     * @throws IOException If the app data directory cannot be determined or created.
     */
    public static Path getWritableDatabasePath() throws IOException {
        return getAppDataDirectory().resolve(WRITABLE_DB_FILENAME);
    }

    /**
     * Gets the full path to the writable credentials file.
     * Does NOT guarantee the file exists; use initializeResourceFile for that.
     *
     * @return Path to the writable credentials file.
     * @throws IOException If the app data directory cannot be determined or created.
     */
    public static Path getWritableCredentialsPath() throws IOException {
        return getAppDataDirectory().resolve(WRITABLE_CREDENTIALS_FILENAME);
    }

    /**
     * Gets the full path to the directory where user-uploaded images are stored.
     * Ensures the directory exists.
     *
     * @return Path to the writable images directory.
     * @throws IOException If the directory cannot be determined or created.
     */
    public static Path getWritableImagesDirectory() throws IOException {
        Path imageDir = getAppDataDirectory().resolve(WRITABLE_IMAGE_DIRNAME);
        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
            System.out.println("Created writable image directory: " + imageDir);
        }
        return imageDir;
    }

    /**
     * Ensures a writable resource file exists at the target path.
     * If it doesn't exist, copies it from the internal classpath resource.
     *
     * @param targetWritablePath The desired path for the writable file (e.g., from getWritableDatabasePath()).
     * @param internalResourcePath The path to the default resource within the classpath (e.g., "/database.csv").
     * @throws IOException If the resource cannot be found, copied, or directories created.
     */
    public static void initializeResourceFile(Path targetWritablePath, String internalResourcePath) throws IOException {
        if (!Files.exists(targetWritablePath)) {
            System.out.println("Writable file not found at " + targetWritablePath + ". Initializing from internal resource...");

            URL internalResourceUrl = AppDataManager.class.getResource(internalResourcePath);
            if (internalResourceUrl == null) {
                // Option 1: Throw if essential (like database)
                throw new IOException("Critical: Could not find internal resource: " + internalResourcePath);
                // Option 2: Log warning and maybe create empty file if appropriate
                // System.err.println("Warning: Could not find internal resource: " + internalResourcePath + ". Creating empty file.");
                // Files.createFile(targetWritablePath); // Creates an empty file
                // return;
            }

            // Ensure parent directory exists before copying
            Path parentDir = targetWritablePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (InputStream internalStream = AppDataManager.class.getResourceAsStream(internalResourcePath)) {
                if (internalStream == null) {
                    // This check is slightly redundant if getResource worked, but good practice
                    throw new IOException("Critical: Could not open internal resource stream: " + internalResourcePath);
                }
                Files.copy(internalStream, targetWritablePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Resource '" + internalResourcePath + "' copied to: " + targetWritablePath);
            } catch (IOException e) {
                System.err.println("FATAL: Failed to copy internal resource '" + internalResourcePath + "' to " + targetWritablePath);
                throw new IOException("Failed to initialize resource file: " + targetWritablePath.getFileName(), e);
            }
        } else {
            // System.out.println("Using existing writable file: " + targetWritablePath); // Optional: Less verbose logging
        }
    }

    /**
     * Helper to get a resource URL from the classpath.
     * Ensures the path starts with '/'.
     *
     * @param classpathResourcePath Path relative to classpath root (e.g., "/image/icon.png").
     * @return The URL or null if not found.
     */
    public static URL getResourceUrl(String classpathResourcePath) {
        if (classpathResourcePath == null || !classpathResourcePath.startsWith("/")) {
            System.err.println("Warning: Classpath resource path should start with '/': " + classpathResourcePath);
            // Optionally prepend '/' if missing, but it's better to fix the call site
            // if (classpathResourcePath != null) classpathResourcePath = "/" + classpathResourcePath; else return null;
        }
        return AppDataManager.class.getResource(classpathResourcePath);
    }

    /**
     * Helper to get a resource InputStream from the classpath.
     * Ensures the path starts with '/'.
     *
     * @param classpathResourcePath Path relative to classpath root (e.g., "/data/config.xml").
     * @return The InputStream or null if not found.
     */
    public static InputStream getResourceStream(String classpathResourcePath) {
        if (classpathResourcePath == null || !classpathResourcePath.startsWith("/")) {
            System.err.println("Warning: Classpath resource path should start with '/': " + classpathResourcePath);
        }
        return AppDataManager.class.getResourceAsStream(classpathResourcePath);
    }
}