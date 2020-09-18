FROM openjdk:8 as photon_builder
WORKDIR /source
ADD . .
RUN ./gradlew build
RUN ./gradlew getDependencies

FROM openjdk:8 as photon_runtime
WORKDIR /source/build/libs
COPY --from=photon_builder /source/build/libs/* ./
RUN echo '#!/bin/bash\njava -cp /source/build/libs/*: com.netflix.imflibrary.app.IMPAnalyzer "$@"' > /usr/local/bin/IMPAnalyzer && \
    chmod +x /usr/local/bin/IMPAnalyzer && \
    echo '#!/bin/bash\njava -cp /source/build/libs/*: com.netflix.imflibrary.app.IMFTrackFileCPLBuilder "$@"' > /usr/local/bin/IMFTrackFileCPLBuilder && \
    chmod +x /usr/local/bin/IMFTrackFileCPLBuilder && \
    echo '#!/bin/bash\njava -cp /source/build/libs/*: com.netflix.imflibrary.app.IMFTrackFileReader "$@"' > /usr/local/bin/IMFTrackFileReader && \
    chmod +x /usr/local/bin/IMFTrackFileReader && \
    echo '#!/bin/bash\njava -cp /source/build/libs/*: com.netflix.imflibrary.app.IMPFixer "$@"' > /usr/local/bin/IMPFixer && \
    chmod +x /usr/local/bin/IMPFixer
