<#
.SYNOPSIS
Lists the directory structure and prints the content of all files within a specified project directory.

.DESCRIPTION
This script recursively traverses a given directory path. It prints the names of directories
and the names and full content of files found within that path and its subdirectories.

.PARAMETER ProjectPath
The path to the root directory of the project you want to inspect.
Defaults to the current directory if not specified.

.EXAMPLE
.\PrintProjectContents.ps1 -ProjectPath "C:\Users\YourUser\MyProject"
Lists structure and file contents for the MyProject directory.

.EXAMPLE
.\PrintProjectContents.ps1
Lists structure and file contents for the current directory.

.NOTES
Author: AI Assistant
Date:   [Current Date]
Consider potential performance impact on very large projects or files.
Binary files will likely produce unreadable output.
File encoding is assumed to be UTF-8. Modify Get-Content's -Encoding parameter if needed.
#>
param(
    # The path to the project directory. Defaults to the current directory.
    [Parameter(Mandatory=$false, Position=0)]
    [ValidateScript({Test-Path $_ -PathType Container})] # Ensure the path exists and is a directory
    [string]$ProjectPath = "."
)

# --- Script Body ---
try {
    # Resolve the provided path to an absolute path for clarity and consistency
    $ResolvedPath = (Resolve-Path -Path $ProjectPath -ErrorAction Stop).Path
    Write-Host "Inspecting Project Directory: $ResolvedPath" -ForegroundColor Cyan
    Write-Host ("=" * 60) # Header separator
}
catch {
    Write-Error "Error: The specified path '$ProjectPath' is not a valid directory or could not be accessed. $($_.Exception.Message)"
    # Exit the script if the path is invalid
    exit 1
}

try {
    # Get all items (files and directories) recursively
    # -ErrorAction SilentlyContinue will skip files/dirs it can't access (e.g., permissions) without stopping the script
    $allItems = Get-ChildItem -Path $ResolvedPath -Recurse -Force -ErrorAction SilentlyContinue
                               # -Force includes hidden items (like .git folders, though content might be binary/unhelpful)
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

Write-Host ""
Write-Host "Finished processing directory: $ResolvedPath" -ForegroundColor Cyan