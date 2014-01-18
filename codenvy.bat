@echo off
SET JAVA_LOCATION="c:\Program Files\Java\jdk1.7.0_07\"
SET JRE_HOME="c:\Program Files\Java\jdk1.7.0_07"
REM cmd /c "%JAVA_LOCATION%\bin\java -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*"
%JAVA_LOCATION%\bin\java -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*
exit /b %errorlevel%