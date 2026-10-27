@echo off
REM Runs every demo program under code\ and prints OK / FAILED for each.
REM Usage: code\run-all.cmd        (output of each program is hidden; run a file directly to see it)
setlocal enabledelayedexpansion
cd /d "%~dp0"
set /a ok=0, failed=0
for /r %%f in (*.java) do (
    java "%%f" > nul 2>&1
    if !errorlevel! equ 0 (
        set /a ok+=1
        echo OK      %%~nxf
    ) else (
        set /a failed+=1
        echo FAILED  %%~nxf
    )
)
echo.
echo %ok% OK, %failed% FAILED
