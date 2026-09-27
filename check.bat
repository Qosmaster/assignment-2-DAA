@echo off
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo JDK was not found. Install a JDK and add its bin folder to PATH.
    pause
    exit /b 1
)
if not exist out mkdir out
javac -Xlint:all -d out src\*.java
if errorlevel 1 goto error
java -cp out Demo
if errorlevel 1 goto error
java -cp out Tests
if errorlevel 1 goto error
echo.
echo The code passed. Existing benchmark results were not changed.
pause
exit /b 0
:error
echo.
echo A command failed. Read the error above.
pause
exit /b 1
