@echo off

set FILE=assembly\target\codenvy-*
for /F %%i in ("%FILE%") do (set VERSION=%%~nxi)

set ASSEMBLY_BIN_DIR=assembly\target\%VERSION%\%VERSION%\bin

IF exist %ASSEMBLY_BIN_DIR% (
    SET CDIR=%CD%
    cd %ASSEMBLY_BIN_DIR%
    call codenvy.bat %1 %2 %3 %4 %5 %6 %7 %8
    chdir %CDIR%
) else (
    echo The command 'mvn clean install' needs to be run first. This will build the CLI assembly
)