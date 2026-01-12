$ErrorActionPreference = "Stop"

function Test-Step {
    param($Name, $Block)
    Write-Host ">>> Executing: $Name" -ForegroundColor Cyan
    try {
        $res = & $Block
        Write-Host "    SUCCESS" -ForegroundColor Green
        return $res
    } catch {
        Write-Host "    FAILED: $_" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            Write-Host "    Response Body: $($reader.ReadToEnd())" -ForegroundColor Yellow
        }
        exit 1
    }
}

$port = "8095"
$random = Get-Random -Minimum 1000 -Maximum 9999

# 1. Register Farm and User
$registerBody = @{
    farm = @{ name = "Smoke Farm $random"; tod = "$random" }
    user = @{ name = "Smoke User $random"; email = "smoke$random@test.com"; cpf = ("{0:D11}" -f $random); password = "password"; confirmPassword = "password" }
    address = @{ street = "Rua Teste"; neighborhood = "Centro"; city = "Cidade Teste"; state = "PB"; zipCode = "58000-000"; country = "Brasil" }
    phones = @(@{ ddd = "83"; number = "9999$random" })
} | ConvertTo-Json -Depth 5

$farmRes = Test-Step "Register Farm & User" {
    Invoke-RestMethod -Method Post -Uri "http://localhost:$port/api/auth/register-farm" -Body $registerBody -ContentType "application/json"
}
$farmId = $farmRes.id
Write-Host "    FarmID: $farmId" -ForegroundColor Gray

# 2. Login
$loginBody = @{ email = "smoke$random@test.com"; password = "password" } | ConvertTo-Json
$loginRes = Test-Step "Login" {
    Invoke-RestMethod -Method Post -Uri "http://localhost:$port/api/auth/login" -Body $loginBody -ContentType "application/json"
}
$token = $loginRes.accessToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host "    Token obtained" -ForegroundColor Gray

# 3. Create Goat
$goatBody = @{
    name = "Smoke Goat"
    registrationNumber = "SG001"
    breed = "SAANEN"
    gender = "FEMEA"
    birthDate = "2020-01-01"
    status = "ATIVO"
    color = "Branca"
} | ConvertTo-Json

$goatRes = Test-Step "Create Goat" {
    Invoke-RestMethod -Method Post -Uri "http://localhost:$port/api/goatfarms/$farmId/goats" -Body $goatBody -Headers $headers -ContentType "application/json"
}
$goatId = $goatRes.registrationNumber # Or id if different
Write-Host "    GoatID: $goatId" -ForegroundColor Gray

# 4. Open Lactation
$openLactationBody = @{
    startDate = "2025-01-01"
} | ConvertTo-Json

$lactationRes = Test-Step "Open Lactation" {
    Invoke-RestMethod -Method Post -Uri "http://localhost:$port/api/goatfarms/$farmId/goats/$goatId/lactations" -Body $openLactationBody -Headers $headers -ContentType "application/json"
}
$lactationId = $lactationRes.id
Write-Host "    LactationID: $lactationId" -ForegroundColor Gray

# 5. Get Active Lactation
$activeRes = Test-Step "Get Active Lactation" {
    Invoke-RestMethod -Method Get -Uri "http://localhost:$port/api/goatfarms/$farmId/goats/$goatId/lactations/active" -Headers $headers
}
if ($activeRes.id -ne $lactationId) { throw "Active lactation ID mismatch" }

# 6. Dry Lactation (Close)
$dryBody = @{
    endDate = "2025-06-01"
} | ConvertTo-Json

$dryRes = Test-Step "Dry Lactation" {
    Invoke-RestMethod -Method Patch -Uri "http://localhost:$port/api/goatfarms/$farmId/goats/$goatId/lactations/$lactationId/dry" -Body $dryBody -Headers $headers -ContentType "application/json"
}
if ($dryRes.status -ne "CLOSED" -and $dryRes.endDate -ne "2025-06-01") { throw "Lactation not closed properly" }

Write-Host ">>> SMOKE TEST PASSED SUCCESSFULLY <<<" -ForegroundColor Green
