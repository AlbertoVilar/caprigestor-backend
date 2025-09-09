# Script de Teste do Fluxo Wizard Refatorado
# Testa a orquestracao sequencial: usuario -> endereco -> telefone -> fazenda

Write-Host "Iniciando teste do fluxo wizard refatorado..." -ForegroundColor Green
Write-Host "Testando orquestracao sequencial com endpoints atomicos" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"

# Dados de teste únicos
$userData = @{
    name = "João Silva Teste $timestamp"
    email = "joao.teste$timestamp@email.com"
    cpf = "12345678901"
    password = "senha123"
    confirmPassword = "senha123"
    roles = @("ROLE_OPERATOR")
} | ConvertTo-Json

$addressData = @{
    street = "Rua das Flores, 123"
    neighborhood = "Centro"
    city = "Campina Grande"
    state = "PB"
    postalCode = "58400-000"
    country = "Brasil"
} | ConvertTo-Json

$phoneData = @{
    ddd = "83"
    number = "987654321"
} | ConvertTo-Json

try {
    Write-Host "`nPasso 1: Criando usuario..." -ForegroundColor Yellow
    Write-Host "Dados: $($userData.Replace('`n','').Replace(' ',''))" -ForegroundColor Gray
    
    $userResponse = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -ContentType "application/json" -Body $userData
    $userId = $userResponse.id
    
    Write-Host "Usuario criado com ID: $userId" -ForegroundColor Green
    
    Write-Host "`nPasso 2: Criando endereco..." -ForegroundColor Yellow
    Write-Host "Dados: $($addressData.Replace('`n','').Replace(' ',''))" -ForegroundColor Gray
    
    $addressResponse = Invoke-RestMethod -Uri "$baseUrl/address" -Method POST -ContentType "application/json" -Body $addressData
    $addressId = $addressResponse.id
    
    Write-Host "Endereco criado com ID: $addressId" -ForegroundColor Green
    
    Write-Host "`nPasso 3: Criando telefone..." -ForegroundColor Yellow
    Write-Host "Dados: $($phoneData.Replace('`n','').Replace(' ',''))" -ForegroundColor Gray
    
    $phoneResponse = Invoke-RestMethod -Uri "$baseUrl/phones" -Method POST -ContentType "application/json" -Body $phoneData
    $phoneId = $phoneResponse.id
    
    Write-Host "Telefone criado com ID: $phoneId" -ForegroundColor Green
    
    Write-Host "`nPasso 4: Criando fazenda com IDs coletados..." -ForegroundColor Yellow
    
    $farmData = @{
        name = "Fazenda Teste Wizard $timestamp"
        tod = "WIZ$($timestamp.Substring($timestamp.Length-2))"
        userId = $userId
        addressId = $addressId
        phoneIds = @($phoneId)
    } | ConvertTo-Json
    
    Write-Host "Dados: $($farmData.Replace('`n','').Replace(' ',''))" -ForegroundColor Gray
    
    $farmResponse = Invoke-RestMethod -Uri "$baseUrl/goatfarms" -Method POST -ContentType "application/json" -Body $farmData
    
    Write-Host "`nSUCESSO! Fluxo wizard completo executado com exito!" -ForegroundColor Green
    Write-Host "Resumo dos IDs criados:" -ForegroundColor Cyan
    Write-Host "   Usuario: $userId" -ForegroundColor White
    Write-Host "   Endereco: $addressId" -ForegroundColor White
    Write-Host "   Telefone: $phoneId" -ForegroundColor White
    Write-Host "   Fazenda: $($farmResponse.id)" -ForegroundColor White
    
    Write-Host "`nA orquestracao sequencial funcionou perfeitamente!" -ForegroundColor Green
    Write-Host "O frontend pode usar este padrao em vez do endpoint /goatfarms/full" -ForegroundColor Green
    
} catch {
    Write-Host "`nERRO no teste do fluxo wizard:" -ForegroundColor Red
    Write-Host "Detalhes: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Código HTTP: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Resposta do servidor: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Nao foi possivel ler a resposta de erro" -ForegroundColor Red
        }
    }
    
    Write-Host "`nVerificacoes sugeridas:" -ForegroundColor Yellow
    Write-Host "   1. O servidor esta rodando em $baseUrl?" -ForegroundColor White
    Write-Host "   2. Os endpoints estao configurados corretamente?" -ForegroundColor White
    Write-Host "   3. Ha conflitos de dados unicos (email, CPF, TOD)?" -ForegroundColor White
}

Write-Host "`nTeste finalizado." -ForegroundColor Cyan