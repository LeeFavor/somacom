FROM openjdk:11-jre-slim VOLUME ["/upload"] COPY app.war app.war ENV TZ=Asia/Seoul ENTRYPOINT ["java","-jar","app.war"]

