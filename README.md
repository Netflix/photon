
# Photon

Photon is a Java implementation of the [Interoperable Master Format (IMF)](https://www.smpte.org/standards/st2067) standard. Photon offers tools for parsing, interpreting and validating constituent files that make an Interoperable Master Package (IMP). These include:

- AssetMap (ST 429-9)
- PackingList (ST 429-8) 
- Composition Playlist (ST 2067-3) 
- IMF track files (ST 2067-5)

Photon parses and reads IMF track files and serializes the metadata into the IMF Composition Playlist structure. Currently, Photon provides support for IMF Application #2E (ST 2067-21) and Application #5 ACES (ST 2067-50), and the Immersive Audio Bitstream (IAB) Plug-in (ST 2067-201).

The goal of the Photon is to provide a simple standardized interface to completely validate an IMP.

## Build

### JDK requirements

Photon can be built using JDK-8. Support for earlier jdk versions has not been tested and/or verified.

### Gradle
Photon can be built very easily by using the included Gradle wrapper. Having downloaded the sources, simply invoke the following commands inside the folder containing the sources:

```
$ ./gradlew clean
$ ./gradlew build
```

For Windows
```
$ gradlew.bat clean
$ gradlew.bat build
```

## Full Documentation

- [Wiki](https://github.com/Netflix/photon/wiki)
- [Javadoc](http://netflix.github.io/photon/)

## Binaries
Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netflix.photon%22).

Change history and version numbers are available at [CHANGELOG.md](https://github.com/Netflix/photon/blob/master/CHANGELOG.md).

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

It will download all dependencies into ./build/libs directory, where Photon.*.jar is built. Multiple sample applications have been provided with this project (e.g., com.netflix.imflibrary.app.IMFTrackFileReader). Having obtained the dependencies, you can run an application as follows:

```
java -cp ./build/libs/*: <fully qualified class name> <zero or more arguments>
```
E.g.,
```
java -cp ./build/libs/*: com.netflix.imflibrary.st0429_9.AssetMap asset_map_file_path
```
```
java -cp ./build/libs/*: com.netflix.imflibrary.st0429_8.PackingList packing_list_file_path
```
```
java -cp ./build/libs/*: com.netflix.imflibrary.st2067_2.Composition composition_playlist_file_path
```
```
java -cp ./build/libs/*: com.netflix.imflibrary.app.IMPAnalyzer IMP_folder_path
```

For Windows please refer to the following examples

To download all dependencies, you just have to run:

```
$ gradlew.bat getDependencies
```

It will download all dependencies into build\libs directory, where Photon.*.jar is built. Multiple sample applications have been provided with this project (e.g., com.netflix.imflibrary.app.IMFTrackFileReader). Having obtained the dependencies, you can run an application as follows:

```
java -cp build\libs\*; <fully qualified class name> <zero or more arguments>
```
E.g.,
```
java -cp build\libs\*; com.netflix.imflibrary.st0429_9.AssetMap asset_map_file_path
```
```
java -cp build\libs\*; com.netflix.imflibrary.st0429_8.PackingList packing_list_file_path
```
```
java -cp build\libs\*; com.netflix.imflibrary.st2067_2.Composition composition_playlist_file_path
```
```
java -cp build\libs\*; com.netflix.imflibrary.app.IMPAnalyzer IMP_folder_path
```
