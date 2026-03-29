param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$FarmId = 17,
    [string]$Email = "albertovilar1@gmail.com",
    [string]$Password = "132747",
    [string]$GoatId = "QAT03281450"
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

$activeLactation = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/lactations/active" -Headers $headers
Assert-StatusCode -Label "Busca da lactacao ativa" -Response $activeLactation -ExpectedStatusCode 200

$stamp = Get-Date -Format "yyyyMMddHHmmss"
$today = Get-Date
$scheduledDate = $today.ToString("yyyy-MM-dd")
$performedAt = $today.AddMinutes(-5).ToString("yyyy-MM-ddTHH:mm:ss")
$futureReferenceDate = $today.AddDays(3).ToString("yyyy-MM-dd")

$createEvent = Invoke-GoatFarmApi -Method Post -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/health-events" -Headers $headers -Body @{
    type = "MEDICACAO"
    title = "Tratamento QA carencia $stamp"
    description = "Smoke operacional de carencia sanitaria"
    scheduledDate = $scheduledDate
    notes = "Criado automaticamente pelo smoke de carencia"
    productName = "Produto QA Carencia"
    activeIngredient = "Principio QA"
    dose = 2.5
    doseUnit = "ML"
    route = "VO"
    batchNumber = "QA-$stamp"
    withdrawalMilkDays = 2
    withdrawalMeatDays = 2
}
Assert-StatusCode -Label "Criacao do evento sanitario" -Response $createEvent -ExpectedStatusCode 201

$eventId = $createEvent.Json.id
if ($null -eq $eventId) {
    throw "Nao foi possivel obter o id do evento sanitario criado."
}

$doneEvent = Invoke-GoatFarmApi -Method Patch -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/health-events/$eventId/done" -Headers $headers -Body @{
    performedAt = $performedAt
    responsible = "Smoke QA"
    notes = "Evento marcado como realizado para ativar carencia"
}
Assert-StatusCode -Label "Conclusao do evento sanitario" -Response $doneEvent -ExpectedStatusCode 200

$eventDetail = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/health-events/$eventId" -Headers $headers
Assert-StatusCode -Label "Detalhe do evento sanitario" -Response $eventDetail -ExpectedStatusCode 200

if (-not $eventDetail.Json.milkWithdrawalActive) {
    throw "O evento realizado nao marcou carencia ativa de leite."
}

if (-not $eventDetail.Json.meatWithdrawalActive) {
    throw "O evento realizado nao marcou carencia ativa de carne."
}

$withdrawalStatus = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/health-events/withdrawal-status" -Headers $headers
Assert-StatusCode -Label "Status de carencia da cabra" -Response $withdrawalStatus -ExpectedStatusCode 200

if (-not $withdrawalStatus.Json.hasActiveMilkWithdrawal) {
    throw "A cabra nao retornou carencia ativa de leite."
}

if (-not $withdrawalStatus.Json.hasActiveMeatWithdrawal) {
    throw "A cabra nao retornou carencia ativa de carne."
}

$farmAlerts = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/health-events/alerts" -Headers $headers
Assert-StatusCode -Label "Alertas sanitarios da fazenda" -Response $farmAlerts -ExpectedStatusCode 200

$milkAlert = $farmAlerts.Json.milkWithdrawalTop | Where-Object { $_.eventId -eq $eventId } | Select-Object -First 1
if ($null -eq $milkAlert) {
    throw "O evento nao apareceu no alerta farm-level de carencia de leite."
}

$meatAlert = $farmAlerts.Json.meatWithdrawalTop | Where-Object { $_.eventId -eq $eventId } | Select-Object -First 1
if ($null -eq $meatAlert) {
    throw "O evento nao apareceu no alerta farm-level de carencia de carne."
}

$blockedMilkProduction = Invoke-GoatFarmApi -Method Post -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/milk-productions" -Headers $headers -Body @{
    date = $scheduledDate
    shift = "MORNING"
    volumeLiters = 1.25
    notes = "Smoke bloqueado por carencia ativa"
}
Assert-StatusCode -Label "Bloqueio de producao durante carencia de leite" -Response $blockedMilkProduction -ExpectedStatusCode 422

if ($blockedMilkProduction.Raw -notlike "*carencia ativa*") {
    throw "A resposta de bloqueio de leite nao mencionou carencia ativa."
}

$futureStatus = Invoke-GoatFarmApi -Method Get -Uri "$BaseUrl/api/v1/goatfarms/$FarmId/goats/$GoatId/health-events/withdrawal-status?referenceDate=$futureReferenceDate" -Headers $headers
Assert-StatusCode -Label "Status futuro sem carencia" -Response $futureStatus -ExpectedStatusCode 200

if ($futureStatus.Json.hasActiveMilkWithdrawal -or $futureStatus.Json.hasActiveMeatWithdrawal) {
    throw "A carencia deveria estar expirada na referencia futura $futureReferenceDate."
}

Write-Host "Health withdrawal smoke concluido com sucesso."
Write-Host "Cabra validada: $GoatId"
Write-Host "Evento realizado: $eventId"
Write-Host "Fim da carencia de leite: $(($withdrawalStatus.Json.milkWithdrawal.withdrawalEndDate))"
Write-Host "Fim da carencia de carne: $(($withdrawalStatus.Json.meatWithdrawal.withdrawalEndDate))"
