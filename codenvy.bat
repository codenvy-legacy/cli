@echo off
SETLOCAL
set CLI_HOME=%CLI_HOME%
cmd /c %JAVA_HOME%\bin\java -jar %CLI_HOME%\target\codenvy-cli-0.2-SNAPSHOT.jar %*
exit /b %errorlevel%