[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
      [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar'))
$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.IO.Compression.FileSystem
$source=Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/data/biomemakeover/structures/mansion'
$pack=Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion'
$srcFiles=@(Get-ChildItem $source -Recurse -Filter *.nbt | ForEach-Object {$_.FullName.Substring((Resolve-Path $source).Path.Length).TrimStart('\')})
$packFiles=@(Get-ChildItem $pack -Recurse -Filter *.nbt | ForEach-Object {$_.FullName.Substring((Resolve-Path $pack).Path.Length).TrimStart('\')})
if($srcFiles.Count -ne 168 -or $packFiles.Count -ne 168){throw "Mansion corpus count mismatch source=$($srcFiles.Count) package=$($packFiles.Count)"}
$zip=[IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar)); try {
  $entries=@{}; foreach($e in $zip.Entries){$entries[$e.FullName]=$e}
  foreach($rel in $srcFiles){$jarRel=$rel.Replace('\','/'); $path="data/biomemakeover/structure/mansion/$jarRel"; if(!$entries.ContainsKey($path)){throw "Missing packaged template $path"}
    $srcHash=(Get-FileHash (Join-Path $source $rel) -Algorithm SHA256).Hash; $s=$entries[$path].Open(); $m=[IO.MemoryStream]::new(); try{$s.CopyTo($m)}finally{$s.Dispose()}; $jarHash=([BitConverter]::ToString(([Security.Cryptography.SHA256]::Create()).ComputeHash($m.ToArray())) -replace '-',''); $m.Dispose(); if($srcHash -ne $jarHash){throw "Hash drift $rel source=$srcHash jar=$jarHash"}
  }
} finally {$zip.Dispose()}
if(Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/mansion.json')){throw 'Natural Mansion activation is premature'}
if(Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure_set/mansions.json')){throw 'Mansion structure set activation is premature'}
Write-Output 'STAGE 11A.2 VALIDATION PASSED (168 packaged templates, exact hashes, singular path, inert worldgen)'
