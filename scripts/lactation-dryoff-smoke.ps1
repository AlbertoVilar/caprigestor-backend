param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$FarmId = 17,
    [string]$Email = "albertovilar1@gmail.com",
    [string]$Password = "132747",
    [string]$ActiveGoatId = "QAT03281450",
    [string]$DriedGoatId = "QA0328145701"
)

$ErrorActionPreference = "Stop"

function Invoke-GoatFarmApi {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    $jsonBody = $null
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10
    }

    try {
        if ($null -ne $jsonBody) {
            $response = Invoke-WebRequest -Method $Method -Uri $Uri -Headers $Headers -ContentType "application/json; charset=utf-8" -Body $jsonBody -UseBasicParsing
        } else {
            $response = Invoke-WebRequest -Method $Method -Uri $Uri -Headers $Headers -UseBasicParsing
        }

        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($response.Content)) {
            try {
                $json = $response.Content | ConvertFrom-Json
            } catch {
                $json = $null
            }
        }

        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Raw = $response.Content
            Json = $json
        }
    } catch {
        $webResponse = $_.Exception.Response
        if ($null -eq $webResponse) {
            throw
        }

        $reader = New-Object System.IO.StreamReader($webResponse.GetResponseStream())
        $content = $reader.ReadToEnd()
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try {
                $json = $content | ConvertFrom-Json
            } catch {
                $json = $null
            }
        }

        return [pscustomobject]@{
            StatusCode = [int]$webResponse.StatusCode
            Raw = $content
            Json = $json
        }
    }
}

function Assert-StatusCode {
    param(
        [string]$Label,
        $Response,
        [int]$ExpectedStatusCode
    )

    if ($Response.StatusCode -ne $ExpectedStatusCode) {
        throw "$Label retornou status $($Response.StatusCode). Esperado: $ExpectedStatusCode. Body: $($Response.Raw)"
    }
}

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

$summary = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$ActiveGoatId/lactations/active/summary" -Headers $headers
Assert-StatusCode -Label "Resumo da lactacao ativa" -Response $summary -ExpectedStatusCode 200

if (-not $summary.Json.pregnancy.dryOffRecommendation) {
    throw "A cabra ativa nao retornou recomendacao de secagem."
}

if ([string]::IsNullOrWhiteSpace($summary.Json.pregnancy.recommendedDryOffDate)) {
    throw "A cabra ativa nao retornou recommendedDryOffDate."
}

$dryOffAlerts = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/milk/alerts/dry-off" -Headers $headers
Assert-StatusCode -Label "Alertas de secagem por fazenda" -Response $dryOffAlerts -ExpectedStatusCode 200

$dryAlertForActiveGoat = $dryOffAlerts.Json.alerts | Where-Object { $_.goatId -eq $ActiveGoatId } | Select-Object -First 1
if ($null -eq $dryAlertForActiveGoat) {
    throw "A cabra ativa nao apareceu no alerta de secagem da fazenda."
}

$activeAfterDry = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$DriedGoatId/lactations/active" -Headers $headers
Assert-StatusCode -Label "Busca de lactacao ativa da cabra seca" -Response $activeAfterDry -ExpectedStatusCode 404

$historyAfterDry = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$DriedGoatId/lactations?page=0&size=1" -Headers $headers
Assert-StatusCode -Label "Historico da cabra seca" -Response $historyAfterDry -ExpectedStatusCode 200

$latestLactation = $historyAfterDry.Json.content | Select-Object -First 1
if ($null -eq $latestLactation) {
    throw "A cabra seca nao retornou historico de lactacao."
}

if ($latestLactation.status -ne "DRY") {
    throw "A ultima lactacao da cabra seca deveria estar em DRY, mas retornou $($latestLactation.status)."
}

$milkDuringDry = Invoke-GoatFarmApi -Method Post -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$DriedGoatId/milk-productions" -Headers $headers -Body @{
    date = "2026-03-28"
    shift = "AFTERNOON"
    volumeLiters = 1.10
    notes = "Smoke bloqueado durante secagem"
}
Assert-StatusCode -Label "Bloqueio de producao durante secagem" -Response $milkDuringDry -ExpectedStatusCode 422

if ($milkDuringDry.Raw -notlike "*Nao ha lactacao ativa para registrar producao de leite.*") {
    throw "A mensagem de bloqueio de producao durante secagem nao foi a esperada."
}

$newLactationWhilePregnant = Invoke-GoatFarmApi -Method Post -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$DriedGoatId/lactations" -Headers $headers -Body @{
    startDate = "2026-03-28"
}
Assert-StatusCode -Label "Bloqueio de nova lactacao com prenhez ativa" -Response $newLactationWhilePregnant -ExpectedStatusCode 422

if ($newLactationWhilePregnant.Raw -notlike "*Nao e permitido abrir nova lactacao enquanto houver prenhez ativa apos secagem confirmada.*") {
    throw "A mensagem de bloqueio de nova lactacao nao foi a esperada."
}

$resumeWhilePregnant = Invoke-GoatFarmApi -Method Patch -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$DriedGoatId/lactations/$($latestLactation.id)/resume" -Headers $headers
Assert-StatusCode -Label "Bloqueio de retomada com prenhez ativa" -Response $resumeWhilePregnant -ExpectedStatusCode 422

if ($resumeWhilePregnant.Raw -notlike "*Nao e permitido retomar lactacao com prenhez ativa.*") {
    throw "A mensagem de bloqueio de retomada nao foi a esperada."
}

Write-Host "Lactation dry-off smoke concluido com sucesso."
Write-Host "Cabra ativa com secagem recomendada: $ActiveGoatId -> $($summary.Json.pregnancy.recommendedDryOffDate)"
Write-Host "Cabra seca bloqueada corretamente: $DriedGoatId"

