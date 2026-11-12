$ErrorActionPreference = 'Stop'

function Test-ServiceStatus {
    param([string]$ServiceName, [int]$Port)
    $url = "http://localhost:$Port/internal/status"
    $result = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 6
    if ($result.service -ne $ServiceName) { throw "Nombre inesperado en ${url}: $($result.service)" }
    if ($result.status -ne 'UP') { throw "Estado inesperado en ${url}: $($result.status)" }
    if ($result.environment -ne 'dev') { throw "Ambiente inesperado en ${url}: $($result.environment)" }
    Write-Host "OK ${ServiceName}: UP (dev)" -ForegroundColor Green
}

Test-ServiceStatus -ServiceName 'account-service' -Port 8081
Test-ServiceStatus -ServiceName 'transaction-service' -Port 8082
Write-Host 'OK: ambos servicios responden correctamente' -ForegroundColor Green
