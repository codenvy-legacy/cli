@echo off
SETLOCAL
set JAVA_HOME="%JAVA_HOME%"
set CLI_HOME="c:\codenvy\cli"
cmd /c "%JAVA_HOME%\bin\java -jar %CLI_HOME%\target\codenvy-cli-0.1-SNAPSHOT.jar %*"
exit /b %errorlevel%