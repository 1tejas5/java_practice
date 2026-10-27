@echo off
REM ---------------------------------------------------------------
REM  MiniBank build + run (Windows, works even if PowerShell scripts are blocked)
REM    run.cmd          guided demo
REM    run.cmd cli      interactive banking console
REM    run.cmd test     unit tests
REM ---------------------------------------------------------------
setlocal
cd /d "%~dp0"

if exist out\classes rmdir /s /q out\classes
dir /s /b src\*.java test\*.java > out-sources.txt

javac -d out\classes @out-sources.txt
set BUILD=%ERRORLEVEL%
del out-sources.txt
if not "%BUILD%"=="0" (
    echo Build failed.
    exit /b 1
)

if /i "%1"=="test" (
    java -cp out\classes com.minibank.testing.TestRunner
) else (
    java -cp out\classes com.minibank.App %*
)
