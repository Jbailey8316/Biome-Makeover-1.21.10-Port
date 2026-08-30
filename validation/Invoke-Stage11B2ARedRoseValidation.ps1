param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$items = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$sounds = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMSounds.java') -Raw
$song = Join-Path $Root 'src/main/resources/data/biomemakeover/jukebox_song/red_rose.json'
$model = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item/red_rose_music_disk.json'
$texture = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/item/music_disc_red_rose.png'
$audio = Join-Path $Root 'src/main/resources/assets/biomemakeover/sounds/red_rose.ogg'
if ($items -notmatch 'RED_ROSE_MUSIC_DISK') { throw 'Red Rose item registration missing' }
if ($items -notmatch 'RED_ROSE_SONG') { throw 'Red Rose song key missing' }
if ($sounds -notmatch 'register\("red_rose"\)') { throw 'Red Rose sound registration missing' }
if (!(Test-Path $song) -or !(Test-Path $model) -or !(Test-Path $texture) -or !(Test-Path $audio)) { throw 'Red Rose asset missing' }
$songJson = Get-Content $song -Raw | ConvertFrom-Json
if ($songJson.comparator_output -ne 2 -or $songJson.length_in_seconds -ne 135.0 -or $songJson.sound_event -ne 'biomemakeover:red_rose') { throw 'Red Rose song contract mismatch' }
if ((Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/mansion/good.json') -Raw) -notmatch 'red_rose_music_disk') { throw 'Mansion Red Rose loot reference missing' }
Write-Output 'STAGE 11B.2A RED ROSE VALIDATION PASSED (item/song/assets/comparator=2/duration=135s)'
Write-Output 'MANSION_REMAINING_DEPENDENCIES: biomemakeover:cladded_boots, biomemakeover:cladded_chestplate, biomemakeover:cladded_leggings, biomemakeover:crude_cladding'
