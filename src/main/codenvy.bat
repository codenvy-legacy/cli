@echo off
set JAVA_HOME="%JAVA_HOME%"
cmd /c "%JAVA_HOME%\bin\java -jar $JAR_FILE$ %*"
exit /b %errorlevel%