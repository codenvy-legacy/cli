@echo off
SET JAVA_LOCATION="c:\Program Files\Java\jdk1.7.0_07\"
SET JRE_HOME="c:\Program Files\Java\jdk1.7.0_07"
cmd /c "%JAVA_LOCATION%\bin\java -DproxySet=true -DproxyHost=127.0.0.1 -DproxyPort=8888 -Djavax.net.ssl.trustStore=c:\codenvy\FiddlerKeystore -Djavax.net.ssl.trustStorePassword=grouch -Djava.util.logging.config.file=logging.properties -jar target\codenvy-cli-1.0-SNAPSHOT.jar %*"
exit /b %errorlevel%