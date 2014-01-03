@echo off
cmd /c "java -jar target\codenvy-cli-1.0-SNAPSHOT-jar-with-dependencies.jar %*"
exit /b %errorlevel%