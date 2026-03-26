param(
    [string]$Database = "caprigestor_dev",
    [string]$Container = "caprigestor-postgres",
    [string]$User = "admin",
    [string]$Password = "admin123",
    [string]$OutputDir = ".\backups"
)

$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$fileName = "{0}-{1}.sql" -f $Database, $timestamp
$outputPath = Join-Path $OutputDir $fileName
$tempPath = "/tmp/$fileName"

docker exec -e PGPASSWORD=$Password $Container pg_dump -U $User -d $Database --no-owner --no-privileges -Fp -f $tempPath
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar backup do banco '$Database' no container '$Container'."
}

docker cp "${Container}:$tempPath" $outputPath
if ($LASTEXITCODE -ne 0) {
    docker exec $Container rm -f $tempPath | Out-Null
    throw "Falha ao copiar o backup para '$outputPath'."
}

docker exec $Container rm -f $tempPath | Out-Null

$resolvedOutput = (Resolve-Path $outputPath).Path
Write-Host "Backup concluído: $resolvedOutput"
