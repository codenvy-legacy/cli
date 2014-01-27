@echo off
SET JAVA_LOCATION="%JAVA_HOME%"
SET JRE_HOME=%JAVA_HOME%
cmd /c "%JAVA_LOCATION%\bin\java -jar $JAR_FILE$ %*"
exit /b %errorlevel%