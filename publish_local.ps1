# Local build + GitHub release for Ascendant-Mobs MultiLoader.
# Modrinth/CurseForge (upload-only, no CI build):
#   gh workflow run publish.yml --repo MeherBenSalem/Ascendant-Mobs -f version=X.Y.Z
#
# Usage:
#   .\publish_local.ps1
#   .\publish_local.ps1 -Version 2.0.2 -SkipBuild

param(
    [string]$Version = "",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

if (-not $Version) {
    $gp = Get-Content (Join-Path $root "1.21.1\gradle.properties") | Where-Object { $_ -match '^version=' }
    if ($gp -match '^version=(.+)$') { $Version = $Matches[1].Trim() }
}
if (-not $Version) { throw "Could not resolve version from 1.21.1/gradle.properties" }

Write-Host "=== Ascendant-Mobs local publish v$Version ===" -ForegroundColor Green

$distDir = Join-Path $root "dist"
if (-not $SkipBuild) {
    New-Item -ItemType Directory -Force -Path $distDir | Out-Null
    $jdk17 = "C:\Program Files\Java\jdk-17"
    $jdk21 = if (Test-Path "C:\Program Files\Java\jdk-21.0.11") {
        "C:\Program Files\Java\jdk-21.0.11"
    } else {
        "C:\Program Files\Java\jdk-21"
    }

    Write-Host "Building 1.20.1 (Forge + Fabric)..." -ForegroundColor Cyan
    $env:JAVA_HOME = $jdk17
    $env:Path = "$jdk17\bin;" + $env:Path
    Set-Location (Join-Path $root "1.20.1")
    .\gradlew.bat clean build --no-daemon
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Get-ChildItem "fabric\build\libs","forge\build\libs" -Filter "rpgmoblevelingsystem-*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc|dev' } |
        ForEach-Object { Copy-Item $_.FullName $distDir -Force }

    Write-Host "Building 1.21.1 (NeoForge + Fabric)..." -ForegroundColor Cyan
    $env:JAVA_HOME = $jdk21
    $env:Path = "$jdk21\bin;" + $env:Path
    Set-Location (Join-Path $root "1.21.1")
    .\gradlew.bat clean build --no-daemon
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Get-ChildItem "fabric\build\libs","neoforge\build\libs" -Filter "rpgmoblevelingsystem-*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc|dev' } |
        ForEach-Object { Copy-Item $_.FullName $distDir -Force }

    $jdk25 = if (Test-Path "C:\Program Files\Java\jdk-25") {
        "C:\Program Files\Java\jdk-25"
    } elseif (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-25") {
        "C:\Program Files\Eclipse Adoptium\jdk-25"
    } else {
        $null
    }
    if ($jdk25) {
        Write-Host "Building 26.3 (NeoForge + Fabric)..." -ForegroundColor Cyan
        $env:JAVA_HOME = $jdk25
        $env:Path = "$jdk25\bin;" + $env:Path
        Set-Location (Join-Path $root "26.3")
        .\gradlew.bat clean build --no-daemon
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        Get-ChildItem "fabric\build\libs","neoforge\build\libs" -Filter "rpgmoblevelingsystem-*.jar" -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch 'sources|javadoc|dev' } |
            ForEach-Object { Copy-Item $_.FullName $distDir -Force }
    } else {
        Write-Host "Skipping 26.3 build (JDK 25 not found). Copy jars from 26.3/*/build/libs or releases/." -ForegroundColor Yellow
    }

    Set-Location $root
}

if (-not (Test-Path $distDir)) { throw "dist/ missing - run build first" }
$jars = Get-ChildItem $distDir -Filter "rpgmoblevelingsystem-*.jar" | Where-Object { $_.Name -match "-$Version\.jar$" }
if ($jars.Count -eq 0) { throw "No rpgmoblevelingsystem-*-$Version.jar in dist/" }

$patchNotes = Join-Path $root "Ascendant-Mobs-$Version-PatchNotes.md"
$notesFile = if (Test-Path $patchNotes) { $patchNotes } else { $null }

$tag = "v$Version"
$releaseExists = $false
try {
    gh release view $tag --repo MeherBenSalem/Ascendant-Mobs 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $releaseExists = $true }
} catch {
    $releaseExists = $false
}

if ($releaseExists) {
    Write-Host "GitHub release $tag exists - uploading assets..." -ForegroundColor Yellow
    foreach ($jar in $jars) {
        gh release upload $tag $jar.FullName --repo MeherBenSalem/Ascendant-Mobs --clobber
    }
} else {
    $ghArgs = @("release", "create", $tag)
    foreach ($jar in $jars) { $ghArgs += $jar.FullName }
    $ghArgs += @("--repo", "MeherBenSalem/Ascendant-Mobs", "--title", "Ascendant-Mobs $Version")
    if ($notesFile) {
        $ghArgs += @("--notes-file", $notesFile)
    } else {
        $ghArgs += @("--notes", "Ascendant-Mobs $Version")
    }
    & gh @ghArgs
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host ""
Write-Host "Jars in dist/ ($($jars.Count)):" -ForegroundColor Cyan
$jars | ForEach-Object { Write-Host "  $($_.Name)" }

Write-Host ""
Write-Host "Modrinth + CurseForge (local upload from dist/):" -ForegroundColor Yellow
Write-Host "  .\upload_local.ps1 -Version $Version"
