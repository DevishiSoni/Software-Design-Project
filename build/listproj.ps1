# Get the project directory from the first argument or use the current directory by default
$projectDir = if ($args.Count -gt 0) { Resolve-Path $args[0] } else { Get-Location }

# Get all files in the directory and its subdirectories
$files = Get-ChildItem -Path $projectDir -File -Recurse

# Output file names
$files | ForEach-Object { $_.FullName }
