FROM openjdk:8

WORKDIR /source

ADD . .

RUN ./gradlew build
RUN ./gradlew getDependencies

CMD ["java", "-cp", "/source/build/libs/*:", "com.netflix.imflibrary.app.IMPAnalyzer"]
