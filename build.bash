#!/bin/bash

# =========================================================================
# Build Script for TourCat Java Application (macOS/Linux Version)
#
# Prerequisites:
# 1. Maven installed and 'mvn' command in PATH.
# 2. Launch4j installed (update launch4j_cli_path).
# 3. This script made executable (chmod +x build.sh).
# =========================================================================

# --- Configuration ---

# Determine the script's absolute directory (project root)
script_dir=$(cd "$(dirname "$0")" && pwd)
project_root="$script_dir"

# Maven command
maven_command="mvn"

# Full path to the Launch4j Command-Line executable
# !! IMPORTANT: UPDATE THIS PATH for your macOS/Linux install !!
# Examples:
# launch4j_cli_path="/Applications/Launch4j.app/Contents/MacOS/launch4jc" # If installed as an App
# launch4j_cli_path="/usr/local/bin/launch4jc" # If linked/installed globally
# launch4j_cli_path="$HOME/launch4j/launch4jc" # If downloaded manually
launch4j_cli_path="/path/to/your/launch4j/launch4jc" # <-- EXAMPLE PATH! Change this!

# Path to your Launch4j configuration file (relative to $project_root)
launch4j_config="$project_root/build/launch4j.xml" # <-- ADJUST if your XML is elsewhere

# Expected location of the final JAR after Maven build (relative to $project_root)
fat_jar_path="$project_root/target/tourcat-1.0.jar"

# Expected location of the final EXE after Launch4j build (relative to $project_root)
# Note: Launch4j will still create a .exe file even on macOS/Linux
output_exe_path="$project_root/target/TourCat.exe"

# --- End Configuration ---

# --- Helper Functions ---

# ANSI Color Codes
COLOR_RESET="\033[0m"
COLOR_RED="\033[0;31m"
COLOR_GREEN="\033[0;32m"
COLOR_YELLOW="\033[0;33m"
COLOR_CYAN="\033[0;36m"

# Function to print messages
log_message() {
    local message="$1"
    local color="${2:-$COLOR_RESET}" # Default to no color
    echo -e "${color}${message}${COLOR_RESET}"
}

# Function to check if the last command was successful
check_success() {
    local exit_code=$?
    local step_name="$1"
    if [ $exit_code -ne 0 ]; then
        log_message "Error: '$step_name' failed with exit code $exit_code." "$COLOR_RED"
        exit 1 # Exit the script on failure
    else
        log_message "'$step_name' completed successfully." "$COLOR_GREEN"
    fi
}

# --- Build Steps ---

log_message "Starting TourCat Build Process..." "$COLOR_CYAN"
log_message "Project Root: $project_root"

# 1. Navigate to Project Directory
log_message "Changing directory to $project_root"
cd "$project_root" || { log_message "Error: Could not change directory to '$project_root'." "$COLOR_RED"; exit 1; }

# 2. Run Maven Build (Clean and Package with Shade plugin)
log_message "Running Maven build ($maven_command clean package)..." "$COLOR_YELLOW"
"$maven_command" clean package
check_success "Maven Build"

# Verify Fat JAR exists
if [ ! -f "$fat_jar_path" ]; then
     log_message "Error: Expected Fat JAR not found at '$fat_jar_path' after Maven build." "$COLOR_RED"
     exit 1
else
     log_message "Fat JAR found at '$fat_jar_path'."
fi


# 3. Run Launch4j Build
log_message "Running Launch4j to create EXE..." "$COLOR_YELLOW"

# Verify Launch4j CLI path exists
if [ ! -f "$launch4j_cli_path" ]; then
    log_message "Error: Launch4j CLI not found at '$launch4j_cli_path'." "$COLOR_RED"
    log_message "Please update the 'launch4j_cli_path' variable in the script." "$COLOR_RED"
    exit 1
fi
# Also check if it's executable
if [ ! -x "$launch4j_cli_path" ]; then
    log_message "Error: Launch4j CLI found at '$launch4j_cli_path' but is not executable." "$COLOR_RED"
    log_message "Try running: chmod +x '$launch4j_cli_path'" "$COLOR_YELLOW"
    exit 1
fi


# Verify Launch4j config file exists
if [ ! -f "$launch4j_config" ]; then
    log_message "Error: Launch4j config file not found at '$launch4j_config'." "$COLOR_RED"
    log_message "Please check the 'launch4j_config' variable and the file location." "$COLOR_RED"
    exit 1
fi

# Execute Launch4j Command Line
"$launch4j_cli_path" "$launch4j_config"
check_success "Launch4j Build"

# Verify EXE exists (Launch4j output)
if [ ! -f "$output_exe_path" ]; then
     log_message "Warning: Expected EXE not found at '$output_exe_path' after Launch4j build. Check Launch4j logs/config." "$COLOR_YELLOW"
     # Decide if this is a fatal error - maybe Launch4j put it elsewhere?
     # exit 1
else
     log_message "Output EXE created at '$output_exe_path'."
fi


# --- Completion ---
log_message "-----------------------------------------"
log_message "Build process completed successfully!" "$COLOR_GREEN"
log_message "Windows Executable should be located at: $output_exe_path" "$COLOR_GREEN"
log_message "(Note: This .exe is intended for Windows and won't run natively on macOS/Linux)" "$COLOR_YELLOW"
log_message "-----------------------------------------"

exit 0 # Explicitly exit with success code