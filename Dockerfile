FROM openjdk:8

ADD . /source

WORKDIR /source

RUN ./gradlew build
RUN ./gradlew getDependencies

ENV IMP_PATH=/media
CMD cd /source/build/libs && java -cp /source/build/libs/*: com.netflix.imflibrary.app.IMPAnalyzer $IMP_PATH
