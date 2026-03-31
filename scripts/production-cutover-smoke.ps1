param(
    [Parameter(Mandatory = $true)]
    [string]$PublicBaseUrl,

    [Parameter(Mandatory = $true)]
    [string]$AdminEmail,

    [Parameter(Mandatory = $true)]
    [string]$AdminPassword,

    [long]$CaprilVilarFarmId = 1,
    [long]$AltoParaisoFarmId = 14,
    [string]$GoatId = "1643213001",
    [string]$DailyDate = "2026-03-30"
)

$ErrorActionPreference = "Stop"

function Trim-TrailingSlash {
    param([string]$Value)
    return $Value.TrimEnd("/")
}

function Invoke-JsonGet {
    param(
        [string]$Url,
        [hashtable]$Headers = @{}
    )
    return Invoke-RestMethod -Method Get -Uri $Url -Headers $Headers
}

function Invoke-JsonPost {
    param(
        [string]$Url,
        [object]$Body,
        [hashtable]$Headers = @{}
    )
    return Invoke-RestMethod -Method Post -Uri $Url -Headers $Headers -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 10)
}

function Assert-Equal {
    param(
        [object]$Actual,
        [object]$Expected,
        [string]$Message
    )
    if ($Actual -ne $Expected) {
        throw "$Message. Esperado: $Expected. Obtido: $Actual."
    }
}

$base = Trim-TrailingSlash $PublicBaseUrl
$apiV1 = "$base/api/v1"

$summary = [ordered]@{}

try {
    try {
        Invoke-WebRequest -Method Get -Uri "$base/actuator" -UseBasicParsing | Out-Null
        throw "Falha: /actuator respondeu publicamente."
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        Assert-Equal $status 403 "/actuator deve ficar bloqueado"
        $summary.actuator = 403
    }

    $health = Invoke-RestMethod -Method Get -Uri "$base/actuator/health"
    Assert-Equal $health.status "UP" "/actuator/health deve retornar UP"
    $summary.health = $health.status

    $publicFarm = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId"
    $summary.publicFarmName = $publicFarm.nome
    if ($publicFarm.user.email -or $publicFarm.user.cpf) {
        throw "Payload publico da fazenda vazou email ou cpf."
    }

    $publicFarmAltoParaiso = Invoke-JsonGet -Url "$apiV1/goatfarms/$AltoParaisoFarmId"
    $summary.publicSecondFarmName = $publicFarmAltoParaiso.nome

    $login = Invoke-JsonPost -Url "$apiV1/auth/login" -Body @{
        email    = $AdminEmail
        password = $AdminPassword
    }
    if (-not $login.accessToken) {
        throw "Login nao retornou accessToken."
    }
    $summary.login = "ok"

    $authHeaders = @{
        Authorization = "Bearer $($login.accessToken)"
    }

    $me = Invoke-JsonGet -Url "$apiV1/auth/me" -Headers $authHeaders
    $summary.me = $me.email

    $managedFarm = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId" -Headers $authHeaders
    if (-not $managedFarm.user.email) {
        throw "Payload privado da fazenda gerida nao retornou email do dono."
    }
    $summary.managedFarmEmail = $managedFarm.user.email

    $goats = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/goats?page=0&size=5"
    $summary.goatsPageLoaded = $true
    $summary.totalGoats = $goats.totalElements

    $goat = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/goats/$GoatId"
    $summary.goatLoaded = $goat.registrationNumber

    $pregnancies = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/goats/$GoatId/reproduction/pregnancies?page=0&size=5"
    $summary.pregnanciesLoaded = $true

    $lactations = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/goats/$GoatId/lactations?page=0&size=5"
    $summary.lactationsLoaded = $true

    $healthEvents = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/goats/$GoatId/health-events?page=0&size=5"
    $summary.healthLoaded = $true

    $consolidated = Invoke-JsonGet -Url "$apiV1/goatfarms/$CaprilVilarFarmId/milk-consolidated-productions/daily?date=$DailyDate" -Headers $authHeaders
    $summary.consolidatedLoaded = $true
    $summary.consolidatedRegistered = $consolidated.registered

    [pscustomobject]$summary | ConvertTo-Json -Depth 10
}
catch {
    Write-Error $_
    exit 1
}
