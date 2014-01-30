@echo off
SETLOCAL
set JAVA_HOME="%JAVA_HOME%"
cmd /c "%JAVA_HOME%\bin\java -jar target\$JAR_FILE$ %*"
exit /b %errorlevel%