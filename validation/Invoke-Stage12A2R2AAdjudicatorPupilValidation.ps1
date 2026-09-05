param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$current = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/entity/adjudicator_eyes.png'
$released = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/assets/biomemakeover/textures/entity/adjudicator_eyes.png'
$a = [System.Drawing.Bitmap]::new($current)
$b = [System.Drawing.Bitmap]::new($released)
if ($a.Width -ne 64 -or $a.Height -ne 64) { throw 'Pupil texture dimensions are not 64x64' }
if ($b.Width -ne $a.Width -or $b.Height -ne $a.Height) { throw 'Released/current pupil texture dimensions differ' }
foreach ($point in @(@(9,16), @(14,16))) {
    $c = $a.GetPixel($point[0], $point[1])
    if ($c.A -eq 0 -or $c.R -lt 180 -or $c.G -lt 180 -or $c.B -lt 180) { throw "Light eye pixel changed at $($point -join ',')" }
}
foreach ($point in @(@(10,16), @(13,16))) {
    $c = $a.GetPixel($point[0], $point[1])
    if ($c.A -eq 0 -or $c.R -gt 64 -or $c.G -gt 64 -or $c.B -gt 64) { throw "Dark pupil pixel missing at $($point -join ',')" }
}
if ((Get-FileHash $current -Algorithm SHA256).Hash -eq (Get-FileHash $released -Algorithm SHA256).Hash) { throw 'Current pupil texture still matches released texture' }
$a.Dispose(); $b.Dispose()
Write-Output 'STAGE 12A.2-R2A ADJUDICATOR PUPIL VALIDATION PASSED (2 dark pupil pixels, released eye layout preserved)'
