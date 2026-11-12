[CmdletBinding()]
param(
  [string]$AccountBaseUrl = 'http://localhost:8081',
  [string]$TransactionBaseUrl = 'http://localhost:8082'
)
$ErrorActionPreference = 'Stop'
$id = '11111111-1111-4111-8111-111111111111'
$unknown = '99999999-9999-4999-8999-999999999999'
$temp = [System.IO.Path]::GetTempFileName()
$tempBody = [System.IO.Path]::GetTempFileName()

function Check-Http {
  param([string]$Title, [int]$Expected, [string]$Pattern, [string]$Url,
        [string]$Method = 'GET', [string]$Body = '', [string[]]$ExtraHeaders = @())
  $argsCurl = @('-sS', '--max-time', '8', '-o', $temp, '-w', '%{http_code}', '-X', $Method)
  foreach ($h in $ExtraHeaders) { $argsCurl += @('-H', $h) }
  if ($Body) {
    [System.IO.File]::WriteAllText($tempBody, $Body, (New-Object System.Text.UTF8Encoding($false)))
    $argsCurl += @('-H', 'Content-Type: application/json', '--data-binary', "@$tempBody")
  }
  $argsCurl += $Url
  $status = (& curl.exe @argsCurl)
  if ($LASTEXITCODE -ne 0) { throw "Falló curl.exe en $Title" }
  $payload = Get-Content $temp -Raw
  if ([int]$status -ne $Expected -or -not $payload.Contains($Pattern)) {
    throw "${Title}: HTTP=$status esperado=$Expected; patrón=$Pattern; cuerpo=$payload"
  }
  Write-Host "OK  $Title ($status)" -ForegroundColor Green
}

try {
  Write-Host '=== LAB02 Validación HTTP (perfil dev) ===' -ForegroundColor Cyan
  Check-Http 'Status cuentas' 200 '"status":"UP"' "$AccountBaseUrl/internal/status"
  Check-Http 'Status transacciones' 200 '"status":"UP"' "$TransactionBaseUrl/internal/status"
  Check-Http 'GET cuenta' 200 '"status":"ACTIVE"' "$AccountBaseUrl/api/accounts/$id"
  Check-Http 'POST cuenta' 201 '"balance":"0.00"' "$AccountBaseUrl/api/accounts" 'POST' '{"holderName":"Cliente Prueba LAB02","currency":"COP"}'
  Check-Http 'Moneda invalida' 400 '"code":"INVALID_REQUEST"' "$AccountBaseUrl/api/accounts" 'POST' '{"holderName":"Cliente","currency":"USD"}'
  Check-Http 'Movimientos de demo' 200 '"type":"DEPOSIT"' "$TransactionBaseUrl/api/transactions/account/$id"
  Check-Http 'WebClient OK' 200 '"exists":true' "$TransactionBaseUrl/internal/verify-account/$id" 'GET' '' @('X-Correlation-Id: lab02-check-001')
  Check-Http 'WebClient 404' 404 '"code":"ACCOUNT_NOT_FOUND"' "$TransactionBaseUrl/internal/verify-account/$unknown"
  Write-Host '=== VALIDACION HTTP COMPLETADA ===' -ForegroundColor Green
  Write-Host 'Revisar aparte headers X-Correlation-Id y logs de ambos servicios.'
} finally {
  Remove-Item $temp, $tempBody -ErrorAction SilentlyContinue
}
