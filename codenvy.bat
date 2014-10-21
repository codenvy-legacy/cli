@echo off
set DIRNAME=%~dp0%
set FILE=%DIRNAME%\assembly\target\codenvy-*
for /F %%i in ("%FILE%") do (set VERSION=%%~nxi)

set ASSEMBLY_BIN_DIR=%DIRNAME%\assembly\target\%VERSION%\%VERSION%\bin

IF exist %ASSEMBLY_BIN_DIR% (
    call %ASSEMBLY_BIN_DIR%\codenvy.bat %1 %2 %3 %4 %5 %6 %7 %8
) else (
    echo The command 'mvn clean install' needs to be run first. This will build the CLI assembly
)