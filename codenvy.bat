@echo off
SET JAVA_LOCATION="c:\Program Files\Java\jdk1.7.0_07\"
SET JRE_HOME="c:\Program Files\Java\jdk1.7.0_07"
cmd /c "%JAVA_LOCATION%\bin\java -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*"
REM %JAVA_LOCATION%\bin\java -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*
REM exit /b %errorlevel%