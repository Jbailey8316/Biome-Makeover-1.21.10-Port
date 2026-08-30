param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$mansion = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion'
$required = @('MansionLayout.java','MansionGrid.java','MansionMath.java','MansionTemplateType.java','RoomLayout.java','RoomType.java','TemplateGetter.java')
foreach ($name in $required) { if (-not (Test-Path (Join-Path $mansion $name))) { throw "Missing layout class: $name" } }
$rooms = @(Get-ChildItem (Join-Path $mansion 'room') -Filter *.java)
if ($rooms.Count -lt 7) { throw "Expected complete room implementation set" }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
$orphans = @('wall/outer/base/wall_outer_base_1.nbt','wall/outer/wall_outer_1.nbt','wall/outer/wall_window_3.nbt')
foreach ($orphan in $orphans) { if (-not (Test-Path (Join-Path $Root "src/main/resources/data/biomemakeover/structure/mansion/$orphan"))) { throw "Missing orphan: $orphan" } }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure_set/mansions.json')) { throw 'Natural Mansion structure-set activated prematurely' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/mansion.json')) { throw 'Natural Mansion structure activated prematurely' }
$allJava = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter *.java | Get-Content -Raw) -join "`n"
if ($allJava -match 'party\.lemons\.taniwha') { throw 'Taniwha runtime dependency detected' }
Write-Output "STAGE 11A.3.1 VALIDATION PASSED (templates=$($templates.Count), rooms=$($rooms.Count))"
