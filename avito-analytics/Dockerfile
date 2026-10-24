FROM amazoncorretto:21-alpine AS builder
WORKDIR /application
COPY target/*.jar app.jar
# распаковываем jar на 4 изолированных слоя зависимостей и кода
RUN java -Djarmode=layertools -jar app.jar extract

FROM amazoncorretto:21-alpine
WORKDIR /application

# Настройки часового пояса
ENV TZ=Europe/Moscow
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# Копируем слои строго в порядке частоты их изменения
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]