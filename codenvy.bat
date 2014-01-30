@echo off
set JAVA_HOME="%JAVA_HOME%"
cmd /c "%JAVA_HOME%\bin\java -jar target\codenvy-cli-0.1-SNAPSHOT.jar %*"
exit /b %errorlevel%