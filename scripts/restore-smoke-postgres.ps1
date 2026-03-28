param(
    [string]$SourceDatabase = "caprigestor_dev",
    [string]$TargetDatabase = "caprigestor_restore_smoke",
    [string]$Container = "caprigestor-postgres",
    [string]$User = "admin",
    [string]$Password = "admin123",
    [string]$OutputDir = ".\backups"
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptRoot
$backupScript = Join-Path $scriptRoot "backup-postgres.ps1"
$restoreScript = Join-Path $scriptRoot "restore-postgres.ps1"
$resolvedOutputDir = Join-Path $repoRoot $OutputDir

Write-Host "Iniciando restore smoke do banco '$SourceDatabase' para '$TargetDatabase'..."

& $backupScript -Database $SourceDatabase -Container $Container -User $User -Password $Password -OutputDir $resolvedOutputDir
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar backup do banco '$SourceDatabase'."
}

$backupFile = Get-ChildItem -Path $resolvedOutputDir -Filter "$SourceDatabase-*.sql" |
    Sort-Object LastWriteTimeUtc -Descending |
    Select-Object -First 1

if (-not $backupFile) {
    throw "Nenhum backup foi encontrado em '$resolvedOutputDir' para o banco '$SourceDatabase'."
}

& $restoreScript -InputFile $backupFile.FullName -Database $TargetDatabase -Container $Container -User $User -Password $Password -RecreateDatabase
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao restaurar o backup em '$TargetDatabase'."
}

Push-Location $repoRoot
try {
    & .\mvnw.cmd `
        "-Dflyway.url=jdbc:postgresql://localhost:5432/$TargetDatabase" `
        "-Dflyway.user=$User" `
        "-Dflyway.password=$Password" `
        "flyway:validate"
    if ($LASTEXITCODE -ne 0) {
        throw "Falha no flyway:validate do banco '$TargetDatabase'."
    }
} finally {
    Pop-Location
}

Write-Host "Restore smoke concluido com sucesso."
Write-Host "Backup validado: $($backupFile.FullName)"
Write-Host "Banco validado: $TargetDatabase"
