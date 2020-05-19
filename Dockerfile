FROM openjdk:14-jdk
VOLUME /tmp
EXPOSE 2002
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
ENTRYPOINT exec java $JAVA_OPTS_SOCIAL -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.datasource.username=${POSTGRES_USERNAME} --spring.datasource.password=${POSTGRES_PASSWORD} --spring.datasource.url=${POSTGRES_URL} --spring.rabbitmq.host=${RABBITMQ_HOST} --spring.rabbitmq.port=${RABBITMQ_PORT} --spring.rabbitmq.username=${RABBITMQ_USERNAME} --spring.rabbitmq.password=${RABBITMQ_PASSWORD} --eureka.client.service-url.defaultZone=${EUREKA_SERVICE_URL}
