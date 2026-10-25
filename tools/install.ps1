param(
    [Parameter(Mandatory = $true)]
    [string]$Device
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $adb)) { $adb = 'adb' }

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
}

Push-Location $root
try {
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'A compilação falhou.' }

    & $adb connect $Device
    & $adb -s $Device install -r '.\app\build\outputs\apk\debug\app-debug.apk'
    & $adb -s $Device shell input keyevent KEYCODE_HOME
    Write-Host 'MiFlow TV instalado. Escolha-o como launcher na televisão.' -ForegroundColor Green
}
finally {
    Pop-Location
}
