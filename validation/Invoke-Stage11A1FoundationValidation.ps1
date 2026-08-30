[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
      [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Require-Text([string]$path,[string]$text) {
    if (!(Select-String -LiteralPath (Join-Path $Root $path) -Pattern $text -SimpleMatch -Quiet)) { throw "Missing '$text' in $path" }
}
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMStructures.java' 'BiomeMakeover.id("mansion")'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMStructures.java' 'MANSION_PIECE'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java' 'MapCodec<MansionFeature>'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java' 'addAdditionalSaveData'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java' 'Rotation'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionGrid.java' 'class MansionGrid'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionLayoutFoundation.java' 'CELL_XZ = 12'
Require-Text 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionLayoutFoundation.java' 'CELL_Y = 7'
if (!(Test-Path $Jar)) { throw "Missing candidate JAR: $Jar" }
$entries = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar)).Entries.FullName
foreach ($required in @('party/lemons/biomemakeover/worldgen/mansion/MansionFeature.class',
                         'party/lemons/biomemakeover/worldgen/mansion/MansionTemplates.class',
                         'party/lemons/biomemakeover/worldgen/mansion/MansionDetails.class')) {
    if ($entries -notcontains $required) { throw "Foundation class absent from JAR: $required" }
}
if ($entries -match '^data/biomemakeover/(structure|structures)/mansion/') { throw 'Mansion template corpus activated in 11A.1' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/mansion.json')) { throw 'Mansion worldgen activated before 11A.3' }
if (Select-String -Path (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Pattern 'handleDataMarker' -Quiet) {
    $text = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
    if ($text -match 'spawnBoss|setLootTable|Tapestry|Adjudicator') { throw '11A.1 contains deferred marker gameplay' }
}
Write-Output 'STAGE 11A.1 FOUNDATION VALIDATION PASSED (codec, piece serialization, grid constants, inert activation)'
