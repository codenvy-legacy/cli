@echo off
set JAVA_LOCATION="%JAVA_HOME%"
cmd /c "%JAVA_LOCATION%\bin\java -jar $JAR_FILE$ %*"
exit /b %errorlevel%