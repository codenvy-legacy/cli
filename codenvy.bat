@echo off
SET JAVA_LOCATION="%JAVA_HOME%"
cmd /c "%JAVA_LOCATION%\bin\java -jar target\codenvy-cli-0.1-SNAPSHOT.jar %*"
exit /b %errorlevel%