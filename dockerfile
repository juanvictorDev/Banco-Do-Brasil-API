FROM amazoncorretto:21-alpine
RUN apk --no-cache add tzdata
ENV TZ=America/Sao_Paulo
WORKDIR /java
COPY bb-api.jar .
ENTRYPOINT [ "java", "-jar", "bb-api.jar" ]