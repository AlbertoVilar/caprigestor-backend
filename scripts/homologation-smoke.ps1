param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Email = "albertovilar1@gmail.com",
    [string]$Password = "132747"
)

$ErrorActionPreference = "Stop"

$health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get
if ($health.status -ne "UP") {
    throw "Health check retornou status diferente de UP."
}

$loginBody = @{
    email = $Email
    password = $Password
} | ConvertTo-Json

$login = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $login.accessToken

if ([string]::IsNullOrWhiteSpace($token)) {
    throw "Nao foi possivel obter accessToken no login."
}

$headers = @{
    Authorization = "Bearer $token"
}

$me = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/me" -Method Get -Headers $headers
$farms = Invoke-RestMethod -Uri "$BaseUrl/api/v1/goatfarms?page=0&size=1" -Method Get -Headers $headers

Write-Host "Homologation smoke concluido com sucesso."
Write-Host "Health: $($health.status)"
Write-Host "Usuario autenticado: $($me.email)"
Write-Host "Fazendas retornadas: $($farms.totalElements)"
