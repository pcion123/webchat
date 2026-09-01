#!/bin/bash
set -e

pid=$(ps -ef | grep WEBCHATSOCKET | grep '/bin/java' | grep -v grep | awk '{print $2}')
if [ -n "$pid" ]; then
  echo "get pid=$pid and killing"
  if ps -p "$pid" > /dev/null; then
    kill -TERM "$pid"
  fi
  sleep 10
fi

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
JAVA_PATH="${JAVA_HOME}/bin"
LOG_HOME="/var/log/webchatsocket"
STARTUP_MODE="PROD WEBCHATSOCKET"
SERVER_CLASS="com.springtest.webchatsocket.WebchatsocketApplication"
JAVA_OPTS="-Xms128M -Xmx128M -Xmn64M -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JAVA_OPTS="${JAVA_OPTS} -Dapp.logPath=${LOG_HOME}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.config=${APP_HOME}/config/logback.xml"
CP="${APP_HOME}/lib/*:${APP_HOME}/config"

echo "${APP_HOME} @ ${STARTUP_MODE} start"
${JAVA_PATH}/java ${JAVA_OPTS} -cp "${CP}" ${SERVER_CLASS} ${STARTUP_MODE}
