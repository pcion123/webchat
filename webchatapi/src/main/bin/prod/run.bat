@echo off
chcp 65001 >nul
SET APP_HOME=%cd%
SET LOG_HOME=%APP_HOME%\log
SET STARTUP_MODE=PROD WEBCHATAPI
SET APP_CLASS="com.springtest.webchatapi.WebchatapiApplication"
SET JAVA_OPTS=-Xms128M -Xmx128M -Xmn64M -XX:+UseG1GC -XX:MaxGCPauseMillis=200
SET JAVA_OPTS=%JAVA_OPTS% -Dapp.logPath=%LOG_HOME%
SET JAVA_OPTS=%JAVA_OPTS% -Dlogging.config=%APP_HOME%\config\logback.xml
SET CP=%APP_HOME%\lib\*;%APP_HOME%\config

echo %APP_HOME% @ %STARTUP_MODE% start
java %JAVA_OPTS% -cp %CP% %APP_CLASS% %STARTUP_MODE%