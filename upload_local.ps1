# Upload dist/ jars to CurseForge locally (no GitHub Actions). NightBeam publishes to CurseForge only.
# Tokens from env or C:\Users\Meher\Desktop\local.env (also NightBeam-Knowledge-Base/secrets/local.env)
#
# Usage:
#   .\upload_local.ps1
#   .\upload_local.ps1 -Version 3.0.0
#   .\upload_local.ps1 -Version 3.0.0 -DryRun

param(
    [string]$Version = "3.0.0",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$notes = Join-Path $root "patchnotes\$Version.md"
if (-not (Test-Path $notes)) {
    $notes = Join-Path $root "Ascendant-Mobs-$Version-PatchNotes.md"
}
if (-not (Test-Path $notes)) {
    $notes = Join-Path $root "PATCH_NOTES.md"
}

$nodeArgs = @("scripts/upload_platforms.mjs", "--version", $Version, "--changelog-file", $notes)
if ($DryRun) { $nodeArgs += "--dry-run" }

Write-Host "=== Local upload Remnants: RPG Mobs v$Version ===" -ForegroundColor Green
node @nodeArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done. Verify:" -ForegroundColor Cyan
Write-Host "  https://www.curseforge.com/minecraft/mc-mods/rpg-mob-leveling-system/files"
