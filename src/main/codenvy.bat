@echo off
SETLOCAL
set CLI_HOME=%CLI_HOME%
cmd /c %JAVA_HOME%\bin\java -jar %CLI_HOME%\target\$JAR_FILE$ %*
exit /b %errorlevel%