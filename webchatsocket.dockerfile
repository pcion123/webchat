FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace/webchatsocket

COPY webchatsocket/.mvn/ .mvn/
COPY webchatsocket/mvnw webchatsocket/pom.xml ./
RUN chmod +x mvnw \
	&& ./mvnw -B -Pdev -DskipTests dependency:go-offline

COPY webchatsocket/src ./src
RUN ./mvnw -B -Pdev -DskipTests clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=dev \
	APP_LOG_PATH=/var/log/webchatsocket \
	LOG_PATH=/var/log/webchatsocket \
	JAVA_OPTS="-Xms128M -Xmx128M -Xmn64M -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
	WEBCHAT_SOCKET_PORT=9092 \
	WEBCHAT_SOCKET_NODE_ID=local \
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
	WEBCHAT_JWT_SECRET=webchat-dev-jwt-secret-32-bytes-minimum

RUN groupadd --system webchat \
	&& useradd --system --gid webchat --home-dir /app --shell /usr/sbin/nologin webchat \
	&& mkdir -p /app/lib /app/config /var/log/webchatsocket \
	&& chown -R webchat:webchat /app /var/log/webchatsocket

COPY --from=build /workspace/webchatsocket/target/webchatsocket-0.0.1-SNAPSHOT.jar /app/lib/webchatsocket.jar
COPY --from=build /workspace/webchatsocket/target/lib/ /app/lib/
COPY --from=build /workspace/webchatsocket/target/config/ /app/config/

USER webchat

EXPOSE 9092

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Dapp.logPath=${LOG_PATH} -Dlogging.config=/app/config/logback.xml -cp '/app/lib/*:/app/config' com.springtest.webchatsocket.WebchatsocketApplication \"$@\"", "--"]
CMD ["DEV", "WEBCHATSOCKET"]
