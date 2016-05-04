# Photon

Photon is an implementation of the Material Exchange Format (MXF) standard. MXF is a SMPTE standard defined in the
specification SMPTE st0377-1:2011. Photon parses and completely reads an MXF file containing a single audio or video essence
as defined by the IMF Essence Component (SMPTE st2067-5:2013) and serializes the metadata into an IMF Composition
Playlist (SMPTE st2067-3:2013).

The goal of the Photon is to provide a simple standardized interface to completely interpret an MXF essence.

## Build

Photon can be built very easily by using the included Gradle wrapper. Having downloaded the sources, simply invoke the
following commands inside the folder containing the sources:

$ ./gradlew clean
$ ./gradlew build

## JDK requirements

Photon can be built using JDK-8. Support for earlier jdk versions has not been tested and/or verified.

## Full Documentation

- [Wiki](https://github.com/Netflix/photon/wiki)
- [Javadoc](http://netflix.github.io/photon/)

## Binaries
Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netflix.photon%22).

Change history and version numbers => [CHANGELOG.md](https://github.com/Netflix/photon/blob/master/CHANGELOG.md)

Example for Maven:

```xml
<dependency>
    <groupId>com.netflix.photon</groupId>
    <artifactId>Photon</artifactId>
    <version>0.1.1</version>
</dependency>
```
and for Ivy:

```xml
<dependency org="com.netflix.photon" name="Photon" rev="0.1.1" />
```

If you need to download all dependencies, you just have to run:

```
gradle getDependencies
```

It will download all dependencies into ./build/libs directory, where Photon.*.jar is builded

Two sample applications have been provided with this project. You can run them as follows:

```
java -cp build/libs/*: com.netflix.imflibrary.app.IMFEssenceComponentReader <inputFile> <workingDirectory>
```

