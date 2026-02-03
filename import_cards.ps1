$sourceDir = "app\Cartas"
$destDir = "app\src\main\res\drawable"

# Ensure destination exists
if (-not (Test-Path -Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}

$rankMap = @{
    "10" = "t"; "a" = "a"; "k" = "k"; "q" = "q"; "j" = "j";
    "2" = "2"; "3" = "3"; "4" = "4"; "5" = "5"; "6" = "6"; "7" = "7"; "8" = "8"; "9" = "9"
}

$suitMap = @{
    "clubs" = "c"; "diamonds" = "d"; "hearts" = "h"; "spades" = "s"
}

$count = 0

Get-ChildItem -Path $sourceDir -Directory | ForEach-Object {
    $folderName = $_.Name
    $suitCode = $suitMap[$folderName.ToLower()]
    
    if ($null -ne $suitCode) {
        Write-Host "Processing $folderName ($suitCode)..."
        
        Get-ChildItem -Path $_.FullName -Filter "*.png" | ForEach-Object {
            $filename = $_.Name
            $baseName = $_.BaseName # "10c"
            
            # Remove suit char from end if present
            if ($baseName.EndsWith($suitCode)) {
                $rankPart = $baseName.Substring(0, $baseName.Length - 1)
            } else {
                $rankPart = $baseName
            }
            
            $newRank = $rankMap[$rankPart.ToLower()]
            
            if ($null -ne $newRank) {
                $newName = "card_" + $newRank + "_" + $suitCode + ".png"
                $destPath = Join-Path $destDir $newName
                
                Copy-Item -Path $_.FullName -Destination $destPath -Force
                Write-Host "Imported $newName"
                $count++
            } else {
                Write-Host "Unknown rank: $rankPart in $filename"
            }
        }
    }
}

Write-Host "Imported $count cards."
