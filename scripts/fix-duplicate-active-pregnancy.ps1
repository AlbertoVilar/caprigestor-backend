param(
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "caprigestor_dev",
    [string]$User = "admin"
)

$scriptPath = Join-Path $PSScriptRoot "..\src\main\resources\db\manual\datafix_duplicate_active_pregnancy.sql"

if (-not (Test-Path $scriptPath)) {
    Write-Error "Arquivo SQL de data-fix não encontrado em $scriptPath"
    exit 1
}

$psql = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psql) {
    Write-Host "psql não encontrado no PATH."
    Write-Host "Você pode executar o script manualmente com algo como:"
    Write-Host "  docker exec -it <container_postgres> psql -U $User -d $Database -f /caminho/no/container/datafix_duplicate_active_pregnancy.sql"
    exit 1
}

$securePassword = Read-Host "Senha do usuário $User" -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
)

$env:PGPASSWORD = $plainPassword

& $psql.Path "-h" $Host "-p" $Port "-U" $User "-d" $Database "-f" $scriptPath

$exitCode = $LASTEXITCODE

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($exitCode -eq 0) {
    Write-Host "Data-fix executado com sucesso. Agora rode a aplicação normalmente."
} else {
    Write-Error "psql retornou código de saída $exitCode."
    exit $exitCode
}

