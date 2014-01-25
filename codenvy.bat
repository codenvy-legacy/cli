@echo off
SET JAVA_LOCATION="%JAVA_HOME%"
SET JRE_HOME=%JAVA_HOME%
cmd /c "%JAVA_LOCATION%\bin\java -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*"
REM exit /b %errorlevel%