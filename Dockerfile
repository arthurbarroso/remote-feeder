FROM --platform=linux/amd64 eclipse-temurin:11-alpine

RUN addgroup -S arthur && adduser -S arthur -G arthur
RUN mkdir -p /service && chown -R arthur. /service
USER arthur

RUN mkdir -p /service
WORKDIR /service
COPY releases/feed-0.0.1.jar /service/feed.jar

EXPOSE 400
ENTRYPOINT ["java", "-jar", "/service/feed.jar"]
