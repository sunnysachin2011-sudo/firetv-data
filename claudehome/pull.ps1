# Pull the whole Claude Home source folder from GitHub into C:\Android\claudehome
# Usage: pull.ps1 [commit-sha]   (a sha avoids GitHub's download cache; default: newest commit on main)
param([string]$ref = '')
$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'
if (-not $ref) {
  $ref = (Invoke-RestMethod -UseBasicParsing -Headers @{ 'Cache-Control'='no-cache' } 'https://api.github.com/repos/sunnysachin2011-sudo/firetv-data/commits/main').sha
}
$tmp = Join-Path $env:TEMP 'firetv-data-src'
$zip = "$tmp.zip"
if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
Invoke-WebRequest -UseBasicParsing "https://codeload.github.com/sunnysachin2011-sudo/firetv-data/zip/$ref" -OutFile $zip
Expand-Archive $zip -DestinationPath $tmp -Force
$src = Get-ChildItem $tmp -Directory | Select-Object -First 1 | ForEach-Object { Join-Path $_.FullName 'claudehome' }
foreach ($d in 'src','res') { $p = "C:\Android\claudehome\$d"; if (Test-Path $p) { Remove-Item $p -Recurse -Force } }
Copy-Item "$src\*" 'C:\Android\claudehome' -Recurse -Force
Remove-Item $zip -Force; Remove-Item $tmp -Recurse -Force
"pulled $((Get-ChildItem 'C:\Android\claudehome\src','C:\Android\claudehome\res' -Recurse -File).Count) source files at $($ref.Substring(0,7))"
