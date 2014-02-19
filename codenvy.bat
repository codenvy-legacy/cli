@echo off
SETLOCAL
set CLI_HOME=c:\codenvy\cli_install
cmd /c %JAVA_HOME%\bin\java -jar %CLI_HOME%\target\codenvy-cli-0.1-SNAPSHOT.jar %*
exit /b %errorlevel%