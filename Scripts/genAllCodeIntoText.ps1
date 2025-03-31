param (
    [string]$SaveFileName = "all_code.txt"
)

Get-ChildItem -Recurse -Filter *.java | Get-Content | Set-Content $SaveFileName
