# Upload dist/ jars to Modrinth + CurseForge locally (no GitHub Actions).
# Tokens from env or C:\Users\Meher\Desktop\local.env (also NightBeam-Knowledge-Base/secrets/local.env)
#
# Usage:
#   .\upload_local.ps1
#   .\upload_local.ps1 -Version 2.0.2
#   .\upload_local.ps1 -Version 2.0.2 -CurseForgeOnly

param(
    [string]$Version = "2.0.2",
    [switch]$CurseForgeOnly,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$notes = Join-Path $root "Ascendant-Mobs-$Version-PatchNotes.md"
if (-not (Test-Path $notes)) {
    $notes = Join-Path $root "RPG-Mob-Leveling-System-$Version-PatchNotes.md"
}

$nodeArgs = @("scripts/upload_platforms.mjs", "--version", $Version, "--changelog-file", $notes)
if ($CurseForgeOnly) { $nodeArgs += "--curseforge-only" }
if ($DryRun) { $nodeArgs += "--dry-run" }

Write-Host "=== Local upload RPG Mob Leveling System v$Version ===" -ForegroundColor Green
node @nodeArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done. Verify:" -ForegroundColor Cyan
Write-Host "  https://modrinth.com/mod/rpg-mob-leveling-system/versions"
Write-Host "  https://www.curseforge.com/minecraft/mc-mods/rpg-mob-leveling-system/files"
