# Photon

Photon is an implementation of the Interoperable Master Format (IMF) standard. IMF is a SMPTE standard defined in the
specification st2067-2:2013. Photon offers tools for parsing, interpreting and validating constituent files that make an
Interoperable Master Package (IMP). These include AssetMap (st429-9:2014), PackingList (st429-8:2007), Composition
Playlist (st2067-3:2013), and the essence containing IMF track file (st2067-5:2013) which follows the Material eXchange
Format (MXF) format (st377-1:2011). Specifically, Photon parses and completely reads an MXF file containing a single
audio or video essence as defined by the IMF Essence Component specification (st2067-5:2013) and serializes the metadata
into an IMF Composition Playlist (SMPTE st2067-3:2013).

The goal of the Photon is to provide a simple standardized interface to completely validate an IMP.

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
$ ./gradlew getDependencies
```

It will download all dependencies into ./build/libs directory, where Photon.*.jar is built

Multiple sample applications have been provided with this project (e.g., com.netflix.imflibrary.app.IMFTrackFileReader). You can run them as follows:

```

java -cp target/dependency/*: <fully qualified class name> <inputFile> <workingDirectory>
```
