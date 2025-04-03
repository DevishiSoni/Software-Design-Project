param (
    [string]$baseDirectory = ".",  # Default to current directory
    [string]$outputFile = "all_code.txt"
)

# Get all .java files recursively from the base directory
Get-ChildItem -Path $baseDirectory -Recurse -Filter *.java |
        Get-Content |
        Set-Content $outputFile
