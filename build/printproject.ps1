<#
.SYNOPSIS
Lists the directory structure and prints the content of text-based files within a specified project directory,
ignoring common binary/compiled file types, including Java specifics (.class, .jar, .war, .ear).

.DESCRIPTION
This script recursively traverses a given directory path. It prints the names of directories
and the names of all files. For files NOT matching a predefined list of binary/compiled extensions
(which now includes .class, .jar, .war, .ear by default), it also prints their full content.

.PARAMETER ProjectPath
The path to the root directory of the project you want to inspect.
Defaults to the current directory if not specified.

.PARAMETER IgnoredExtensions
(Optional) An array of file extensions (e.g., '.dll') to ignore when printing content.
If provided, this list *replaces* the default list. If omitted, the comprehensive default list (including Java types) is used.

.EXAMPLE
.\PrintProjectContents_JavaAware.ps1 -ProjectPath "C:\Users\YourUser\MyJavaProject"
Lists structure and text file contents for the Java project, automatically skipping .class, .jar, etc.

.EXAMPLE
.\PrintProjectContents_JavaAware.ps1
Lists structure and text file contents for the current directory, skipping default binary/compiled/Java types.

.EXAMPLE
.\PrintProjectContents_JavaAware.ps1 -IgnoredExtensions @('.log', '.tmp')
Lists structure and content, *only* ignoring .log and .tmp files (overrides the default list).

.NOTES
Author: AI Assistant
Date:   [Current Date]
Consider potential performance impact on very large projects or text files.
File encoding for read files is assumed to be UTF-8. Modify Get-Content's -Encoding parameter if needed.
The default list of ignored extensions includes common binaries and Java compiled/archive types.
#>
param(
    # The path to the project directory. Defaults to the current directory.
    [Parameter(Mandatory=$false, Position=0)]
    [ValidateScript({Test-Path $_ -PathType Container})] # Ensure the path exists and is a directory
    [string]$ProjectPath = ".",

    # Optional array of file extensions to ignore (content will not be printed). Overrides the default list if provided.
    [Parameter(Mandatory=$false)]
    [string[]]$IgnoredExtensions
)

# --- Configuration ---

# Define default extensions to ignore if none are provided via the parameter.
# Includes common binaries AND Java compiled/archive types.
$DefaultIgnoredExtensions = @(
    # Java Specific
    '.class',   # Java bytecode
    '.jar',     # Java Archive
    '.war',     # Web Application Archive
    '.ear',     # Enterprise Application Archive

    # General Compiled Code & Libraries
    '.dll',     # Windows Dynamic Link Library
    '.exe',     # Windows Executable
    '.so',      # Linux Shared Object
    '.o',       # Compiled object file
    '.obj',     # Compiled object file (Windows)
    '.pyc',     # Python compiled bytecode
    '.pdb',     # Program Database (debugging symbols)
    '.lib',     # Static Library
    '.a',       # Static Library (Unix-like)
    '.dylib',   # macOS Dynamic Library

    # Archives (often contain binaries or aren't meant to be read as text)
    '.zip',
    '.rar',
    '.gz',
    '.tar',
    '.7z',

    # Binary Data / Media Files
    '.bin',
    '.dat',
    '.png',
    '.jpg',
    '.jpeg',
    '.gif',
    '.bmp',
    '.tiff',
    '.ico',
    '.svg',     # Often complex XML, can be large/less useful raw
    '.pdf',
    '.doc',
    '.docx',
    '.xls',
    '.xlsx',
    '.ppt',
    '.pptx',
    '.mp3',
    '.wav',
    '.mp4',
    '.avi',
    '.mov',
    '.wmv',
    '.flv',
    '.iso',     # Disc Image
    '.vmdk',    # Virtual Machine Disk
    '.sqlite',  # Database files
    '.db'       # Database files
)

# Use provided extensions if available (overriding default), otherwise use defaults
# Ensure all extensions in the list are lowercase for consistent matching
if ($PSBoundParameters.ContainsKey('IgnoredExtensions')) {
    Write-Host "Using provided list of ignored extensions." -ForegroundColor DarkYellow
    $EffectiveIgnoredExtensions = $IgnoredExtensions | ForEach-Object { $_.ToLowerInvariant() }
} else {
    Write-Host "Using default list of ignored extensions (includes binaries, archives, Java compiled types)." -ForegroundColor DarkGray
    $EffectiveIgnoredExtensions = $DefaultIgnoredExtensions # Already lowercase
}


# --- Script Body ---
try {
    # Resolve the provided path to an absolute path for clarity and consistency
    $ResolvedPath = (Resolve-Path -Path $ProjectPath -ErrorAction Stop).Path
    Write-Host "Inspecting Project Directory: $ResolvedPath" -ForegroundColor Cyan
    Write-Host "Ignoring content for extensions: $($EffectiveIgnoredExtensions -join ', ')" -ForegroundColor DarkGray
    Write-Host ("=" * 60) # Header separator
}
catch {
    Write-Error "Error: The specified path '$ProjectPath' is not a valid directory or could not be accessed. $($_.Exception.Message)"
    # Exit the script if the path is invalid
    exit 1
}

try {
    # Get all items (files and directories) recursively
    $allItems = Get-ChildItem -Path $ResolvedPath -Recurse -Force -ErrorAction SilentlyContinue
} catch {
    Write-Error "An unexpected error occurred while listing items in '$ResolvedPath': $($_.Exception.Message)"
    exit 1
}


# Process each item found
foreach ($item in $allItems) {
    # Calculate relative path for cleaner display
    $relativePath = $item.FullName.Substring($ResolvedPath.Length).TrimStart('\/')

    if ($item.PSIsContainer) {
        # It's a Directory
        Write-Host ""
        Write-Host "Directory: $relativePath" -ForegroundColor Yellow
        Write-Host ("-" * (11 + $relativePath.Length)) # Underline the directory path
    } else {
        # It's a File
        Write-Host ""
        Write-Host "File: $relativePath" -ForegroundColor Green
        Write-Host ("-" * (6 + $relativePath.Length)) # Underline the file path

        # Check if the file extension is in the list of extensions to ignore (case-insensitive)
        $fileExtensionLower = $item.Extension.ToLowerInvariant()
        if ($EffectiveIgnoredExtensions -contains $fileExtensionLower) {
             Write-Host "[Skipping content - Ignored Extension ($($item.Extension))]" -ForegroundColor DarkGray
             Write-Host ("=" * 60) # Separator
        }
        else {
             # Extension is NOT ignored, proceed to read and print content
             Write-Host "--- Content Start ---" -ForegroundColor Gray
             try {
                 # Get the entire file content as a single string (-Raw)
                 # Use -Encoding UTF8 as a default, change if needed (e.g., Default, ASCII, Unicode, UTF32)
                 # Use -ErrorAction Stop inside the Try to ensure the Catch block triggers on read errors
                 $content = Get-Content -Path $item.FullName -Raw -Encoding UTF8 -ErrorAction Stop
                 # Print the content
                 Write-Host $content
             } catch {
                 # Write a warning if a specific file cannot be read
                 Write-Warning "   *** Error reading file '$($item.FullName)': $($_.Exception.Message) ***"
                 Write-Host "[Content Could Not Be Read]" -ForegroundColor Red
             }
             Write-Host "--- Content End ---" -ForegroundColor Gray
             Write-Host ("=" * 60) # Separator after each file's content
        }
    }
}

Write-Host ""
Write-Host "Finished processing directory: $ResolvedPath" -ForegroundColor Cyan