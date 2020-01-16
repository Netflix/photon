FROM openjdk:8

WORKDIR /source

ADD . .

RUN ./gradlew build
RUN ./gradlew getDependencies

ENTRYPOINT ["java", "-cp", "./build/libs/*:", "com.netflix.imflibrary.app.IMPAnalyzer"]
