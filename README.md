# Photon

Photon is an implementation of the Material Exchange Format (MXF) standard. MXF is a SMPTE standard defined in the
specification SMPTE st0377-1:2011. Photon parses and completely reads an MXF file containing a single essence
as defined by the IMF Essence Component (SMPTE st2067-5:2013) and serializes the metadata into an IMF Composition
Playlist (SMPTE st2067-3:2013).

The goal of the Photon is to provide a simple standardized interface to completely interpret an MXF essence.

## Build

Photon can be built very easily by using the included gradle wrapper. Having downloaded the sources, simply invoke the
following commands inside the folder containing the sources:

$ ./gradlew clean
$ ./gradlew build

## JDK requirements

Although Photon can be built using JDK-7, it is recommended to use JDK-8 or later.

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
    <artifactId>MXFLibrary</artifactId>
    <version>0.0.1</version>
</dependency>
```
and for Ivy:

```xml
<dependency org="com.netflix.photon" name="MXFLibrary" rev="0.0.1" />
```

If you need to download the jars instead of using a build system, create a Maven pom file like the following with the desired version:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.netflix.photon.download</groupId>
  <artifactId>photon-download</artifactId>
  <version>1.0-snapshot</version>
  <name>Simple POM to download MXFLibrary and dependencies</name>
  <url>https://github.com/Netflix/photon</url>
  <dependencies>
    <dependency>
      <groupId>com.netflix.photon</groupId>
      <artifactId>MXFLibrary</artifactId>
      <version>0.0.1</version>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
</project>
```

Then execute:

```
mvn -f photon-download.pom.xml dependency:copy-dependencies
```

It will download MXFLibrary-*.jar and its dependencies into ./target/dependency/.

Two sample applications have been provided with this project. You can run them as follows:

```
java -cp target/dependency/*: com.netflix.imflibrary.app.IMFEssenceComponentReader <inputFile> <workingDirectory>
```

