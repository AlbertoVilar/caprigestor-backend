param(
    [Parameter(Mandatory = $true)]
    [string]$InputFile,
    [string]$Database = "caprigestor_dev",
    [string]$Container = "caprigestor-postgres",
    [string]$User = "admin",
    [string]$Password = "admin123",
    [switch]$RecreateDatabase
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $InputFile)) {
    throw "Arquivo de backup não encontrado: $InputFile"
}

$resolvedInput = (Resolve-Path $InputFile).Path
$fileName = [System.IO.Path]::GetFileName($resolvedInput)
$tempPath = "/tmp/$fileName"

if ($RecreateDatabase) {
    docker exec -e PGPASSWORD=$Password $Container psql -v ON_ERROR_STOP=1 -U $User -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$Database' AND pid <> pg_backend_pid();"
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao encerrar conexões abertas do banco '$Database'."
    }

    docker exec -e PGPASSWORD=$Password $Container dropdb -U $User --if-exists $Database
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao remover o banco '$Database'."
    }

    docker exec -e PGPASSWORD=$Password $Container createdb -U $User $Database
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao recriar o banco '$Database'."
    }
}

docker cp $resolvedInput "${Container}:$tempPath"
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao copiar '$resolvedInput' para o container '$Container'."
}

docker exec -e PGPASSWORD=$Password $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -f $tempPath
if ($LASTEXITCODE -ne 0) {
    docker exec $Container rm -f $tempPath | Out-Null
    throw "Falha ao restaurar o backup em '$Database'."
}

docker exec $Container rm -f $tempPath | Out-Null

Write-Host "Restore concluído no banco '$Database' a partir de '$resolvedInput'."
