@echo off
chcp 65001 > nul
echo ====================================================
echo RECONECTAR DISPOSITIVO ANDROID (XIAOMI)
echo ====================================================
echo.
echo Reestableciendo la redirección de puertos...
echo.

set ADB_PATH="%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe"

if not exist %ADB_PATH% (
    :: Intentar buscar en la ruta estándar del usuario rafav
    set ADB_PATH="C:\Users\rafav\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

%ADB_PATH% devices

echo.
echo Ejecutando 'adb reverse'...
%ADB_PATH% reverse tcp:8080 tcp:8080
%ADB_PATH% reverse tcp:3080 tcp:8080

echo.
echo ====================================================
echo ¡Conexión restablecida con éxito!
echo Ya puedes usar la aplicación en tu móvil.
echo ====================================================
echo.
pause
