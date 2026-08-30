[CmdletBinding()]
param(
    [string]$Root = (Get-Location).Path,
    [string]$ReferenceRoot = ''
)
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($ReferenceRoot)) {
    $ReferenceRoot = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/data/biomemakeover/structures/mansion'
}

if (!(Test-Path $ReferenceRoot)) { throw "Mansion reference root is missing: $ReferenceRoot" }
$structureJson = Join-Path (Split-Path $ReferenceRoot -Parent) '..\worldgen\structure\mansion.json'
if (!(Test-Path $structureJson)) { throw "Mansion structure JSON is missing: $structureJson" }
$structure = Get-Content $structureJson -Raw | ConvertFrom-Json

function Find-Bytes([byte[]]$Data, [byte[]]$Pattern) {
    for ($i = 0; $i -le $Data.Length - $Pattern.Length; $i++) {
        $ok = $true
        for ($j = 0; $j -lt $Pattern.Length; $j++) { if ($Data[$i + $j] -ne $Pattern[$j]) { $ok = $false; break } }
        if ($ok) { return $i }
    }
    return -1
}
function Read-NbtHeader([string]$Path) {
    $input = [IO.File]::OpenRead($Path); $gzip = [IO.Compression.GzipStream]::new($input, [IO.Compression.CompressionMode]::Decompress)
    $memory = [IO.MemoryStream]::new()
    try { $gzip.CopyTo($memory); $bytes = $memory.ToArray() } finally { $gzip.Dispose(); $input.Dispose(); $memory.Dispose() }
    $dvPattern = [byte[]](0,11,68,97,116,97,86,101,114,115,105,111,110)
    $dv = Find-Bytes $bytes $dvPattern
    if ($dv -lt 0 -or $dv + $dvPattern.Length + 4 -gt $bytes.Length) { throw "DataVersion missing in $Path" }
    $version = [BitConverter]::ToInt32(@($bytes[($dv + $dvPattern.Length)..($dv + $dvPattern.Length + 3)]) , 0)
    if ([BitConverter]::IsLittleEndian) { $version = [System.Net.IPAddress]::NetworkToHostOrder($version) }
    $sizePattern = [byte[]](9,0,4,115,105,122,101,3,0,0,0,3)
    $sz = Find-Bytes $bytes $sizePattern
    if ($sz -lt 0 -or $sz + $sizePattern.Length + 12 -gt $bytes.Length) { throw "size missing in $Path" }
    $dims = 0..2 | ForEach-Object {
        $offset = $sz + $sizePattern.Length + ($_ * 4)
        $n = [BitConverter]::ToInt32(@($bytes[$offset..($offset + 3)]), 0)
        if ([BitConverter]::IsLittleEndian) { $n = [System.Net.IPAddress]::NetworkToHostOrder($n) }
        $n
    }
    [pscustomobject]@{ DataVersion = $version; Width = $dims[0]; Height = $dims[1]; Depth = $dims[2]; Bytes = (Get-Item $Path).Length; Hash = (Get-FileHash $Path -Algorithm SHA256).Hash }
}

$files = @(Get-ChildItem $ReferenceRoot -Recurse -File -Filter '*.nbt')
$refs = @()
function Collect-Ids([object]$Node) {
    if ($null -eq $Node) { return }
    if ($Node -is [array]) { foreach ($item in $Node) { Collect-Ids $item }; return }
    if ($Node -is [string] -and $Node -like 'biomemakeover:mansion/*') { $script:refs += $Node; return }
    if ($Node -is [pscustomobject]) { foreach ($property in $Node.PSObject.Properties) { Collect-Ids $property.Value } }
}
Collect-Ids $structure.templates
$refs = @($refs | Sort-Object -Unique)
$rows = foreach ($file in $files | Sort-Object FullName) {
    $relative = $file.FullName.Substring($ReferenceRoot.Length).TrimStart('\','/') -replace '\\','/'
    $id = 'biomemakeover:mansion/' + ($relative -replace '\.nbt$','')
    $category = if ($relative.Contains('/')) { $relative.Split('/')[0] } else { 'root' }
    $header = Read-NbtHeader $file.FullName
    [pscustomobject]@{ Path = $relative; Id = $id; Category = $category; Referenced = ($refs -contains $id); DataVersion = $header.DataVersion; Dimensions = "$($header.Width)x$($header.Height)x$($header.Depth)"; Bytes = $header.Bytes; SHA256 = $header.Hash }
}
$orphan = @($rows | Where-Object { !$_.Referenced })
$duplicates = @($rows | Group-Object SHA256 | Where-Object Count -gt 1)
Write-Output "STAGE 11A MANSION INVENTORY: templates=$($rows.Count) active_unique=$($refs.Count) orphan=$($orphan.Count) duplicate_hash_groups=$($duplicates.Count)"
$rows | Group-Object Category | Sort-Object Name | ForEach-Object { Write-Output " category=$($_.Name) count=$($_.Count)" }
$rows | Group-Object DataVersion | Sort-Object Name | ForEach-Object { Write-Output " dataversion=$($_.Name) count=$($_.Count)" }
if ($orphan.Count) { Write-Output ' orphan_paths:'; $orphan | ForEach-Object { Write-Output "  $($_.Path)" } }
$rows | ForEach-Object { Write-Output "$($_.Path)`t$($_.DataVersion)`t$($_.Dimensions)`t$($_.Referenced)`t$($_.SHA256)" }
if ($rows.Count -ne 168 -or $refs.Count -ne 165 -or $orphan.Count -ne 3 -or $duplicates.Count -ne 0) {
    throw 'Mansion inventory contract differs from the final pinned source tree'
}
Write-Output 'STAGE 11A MANSION INVENTORY PASSED'
