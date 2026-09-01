FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace/webchatapi

COPY webchatapi/.mvn/ .mvn/
COPY webchatapi/mvnw webchatapi/pom.xml ./
RUN chmod +x mvnw \
	&& ./mvnw -B -Pdev -DskipTests dependency:go-offline

COPY webchatapi/src ./src
RUN ./mvnw -B -Pdev -DskipTests clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=dev \
	APP_LOG_PATH=/var/log/webchatapi \
	LOG_PATH=/var/log/webchatapi \
	JAVA_OPTS="-Xms128M -Xmx128M -Xmn64M -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
	WEBCHATAPI_PORT=9091 \
	MYSQL_URL="jdbc:mysql://localhost:3306/webchat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8" \
	MYSQL_USERNAME=webuser \
	MYSQL_PASSWORD=webpass \
	REDIS_HOST=localhost \
	REDIS_PORT=6379 \
	RABBITMQ_HOST=localhost \
	RABBITMQ_PORT=5672 \
	RABBITMQ_USERNAME=webuser \
	RABBITMQ_PASSWORD=webpass \
	RABBITMQ_VHOST=/ \
	WEBCHAT_SOCKET_API_BASE_URL=http://localhost:9092 \
	WEBCHAT_JWT_SECRET=webchat-dev-jwt-secret-32-bytes-minimum

RUN groupadd --system webchat \
	&& useradd --system --gid webchat --home-dir /app --shell /usr/sbin/nologin webchat \
	&& mkdir -p /app/lib /app/config /var/log/webchatapi \
	&& chown -R webchat:webchat /app /var/log/webchatapi

COPY --from=build /workspace/webchatapi/target/webchatapi-0.0.1-SNAPSHOT.jar /app/lib/webchatapi.jar
COPY --from=build /workspace/webchatapi/target/lib/ /app/lib/
COPY --from=build /workspace/webchatapi/target/config/ /app/config/

USER webchat

EXPOSE 9091

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Dapp.logPath=${LOG_PATH} -Dlogging.config=/app/config/logback.xml -cp '/app/lib/*:/app/config' com.springtest.webchatapi.WebchatapiApplication \"$@\"", "--"]
CMD ["DEV", "WEBCHATAPI"]
