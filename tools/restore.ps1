param(
    [Parameter(Mandatory = $true)]
    [string]$Device
)

$ErrorActionPreference = 'Stop'
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $adb)) { $adb = 'adb' }

& $adb connect $Device
& $adb -s $Device shell pm enable com.google.android.tvlauncher
& $adb -s $Device uninstall com.miflow.tv
& $adb -s $Device shell input keyevent KEYCODE_HOME
Write-Host 'Launcher original restaurado.' -ForegroundColor Green
