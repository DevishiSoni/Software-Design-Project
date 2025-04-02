# =========================================================================
# Build Script for TourCat Java Application
#
# Prerequisites:
# 1. Maven installed and 'mvn' command in PATH (or update $mavenCommand).
# 2. Launch4j installed (update $launch4jCliPath).
# 3. PowerShell Execution Policy allows running local scripts.
# =========================================================================

# --- Configuration ---

# Path to your Maven project root (where pom.xml is located)
# $PSScriptRoot is the directory where this script resides.
$projectRoot = $PSScriptRoot

# Maven command (use 'mvn.cmd' if 'mvn' isn't directly in PATH)
$mavenCommand = "mvn"

# Full path to the Launch4j Command-Line executable (launch4jc.exe)
# !! IMPORTANT: UPDATE THIS PATH !!
$launch4jCliPath = "C:\Program Files (x86)\Launch4j\launch4jc.exe"

# Path to your Launch4j configuration file (relative to $projectRoot)
$launch4jConfig = Join-Path $projectRoot "build\launch4j.xml"

# Expected location of the final JAR after Maven build (relative to $projectRoot)
$fatJarPath = Join-Path $projectRoot "target\tourcat-1.0.jar"

# Expected location of the final EXE after Launch4j build (relative to $projectRoot)
$outputExePath = Join-Path $projectRoot "target\TourCat.exe"

# --- End Configuration ---

# Function to print messages
function Write-Log {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Function to check if the last command was successful
function Check-Success {
    param(
        [string]$StepName
    )
    if (-not $?) {
        Write-Log "Error: '$StepName' failed." -Color Red
        # You might want more specific error handling based on $LASTEXITCODE for executables
        exit 1 # Exit the script on failure
    } else {
        Write-Log "'$StepName' completed successfully." -Color Green
    }
}

# --- Build Steps ---

Write-Log "Starting TourCat Build Process..." -Color Cyan
Write-Log "Project Root: $projectRoot"

# 1. Navigate to Project Directory
Write-Log "Changing directory to $projectRoot"
try {
    Set-Location $projectRoot -ErrorAction Stop
} catch {
    Write-Log "Error: Could not change directory to '$projectRoot'. Please check the path." -Color Red
    exit 1
}

# 2. Run Maven Build (Clean and Package with Shade plugin)
Write-Log "Running Maven build (mvn clean package)..." -Color Yellow
& $mavenCommand clean package

# Check if Maven build succeeded
Check-Success "Maven Build"

# Verify Fat JAR exists
if (-not (Test-Path $fatJarPath -PathType Leaf)) {
     Write-Log "Error: Expected Fat JAR not found at '$fatJarPath' after Maven build." -Color Red
     exit 1
} else {
     Write-Log "Fat JAR found at '$fatJarPath'."
}


# 3. Run Launch4j Build
Write-Log "Running Launch4j to create EXE..." -Color Yellow

# Verify Launch4j CLI path exists
if (-not (Test-Path $launch4jCliPath -PathType Leaf)) {
    Write-Log "Error: Launch4j CLI not found at '$launch4jCliPath'." -Color Red
    Write-Log "Please update the `$launch4jCliPath variable in the script." -Color Red
    exit 1
}

# Verify Launch4j config file exists
if (-not (Test-Path $launch4jConfig -PathType Leaf)) {
    Write-Log "Error: Launch4j config file not found at '$launch4jConfig'." -Color Red
    Write-Log "Please check the `$launch4jConfig variable and the file location." -Color Red
    exit 1
}

# Execute Launch4j Command Line
& $launch4jCliPath $launch4jConfig

# Check if Launch4j succeeded (using $LASTEXITCODE is often more reliable for executables)
if ($LASTEXITCODE -ne 0) {
    Write-Log "Error: Launch4j build failed. Exit code: $LASTEXITCODE" -Color Red
    exit 1
} else {
     Check-Success "Launch4j Build" # Use previous function for consistent success message
}

# Verify EXE exists
if (-not (Test-Path $outputExePath -PathType Leaf)) {
     Write-Log "Warning: Expected EXE not found at '$outputExePath' after Launch4j build. Check Launch4j logs/config." -Color Yellow
     # Decide if this is a fatal error - maybe Launch4j put it elsewhere?
     # exit 1
} else {
     Write-Log "Output EXE created at '$outputExePath'."
}


# --- Completion ---
Write-Log "-----------------------------------------"
Write-Log "Build process completed successfully!" -Color Green
Write-Log "Executable should be located at: $outputExePath" -Color Green
Write-Log "-----------------------------------------"

# Optional: Navigate back to original directory if needed
# Pop-Location

exit 0 # Explicitly exit with success code