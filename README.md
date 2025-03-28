
# Photon

Photon is a Java implementation of the [Interoperable Master Format (IMF)](https://www.smpte.org/standards/st2067) standard. Photon offers tools for parsing, interpreting and validating constituent files that make an Interoperable Master Package (IMP). These include:

- AssetMap (ST 429-9)
- PackingList (ST 429-8) 
- Composition Playlist (ST 2067-3) 
- IMF Track Files (ST 2067-5)

Photon parses and reads IMF track files and serializes the metadata into the IMF Composition Playlist structure. Currently, Photon provides support for 
- IMF Application #2E (ST 2067-21)
- Application #5 ACES (ST 2067-50)
- Immersive Audio Bitstream (IAB) Level 0 Plug-in (ST 2067-201).
- Audio with Frame-based S-ADM Metadata Plug-in (ST 2067-203).

The goal of the Photon is to provide a simple standardized interface to completely validate an IMP.

## Build

### JDK requirements

Photon can be built using JDK-11.

### Gradle
Photon can be built very easily by using the included Gradle wrapper. Having downloaded the sources, simply invoke the following commands inside the folder containing the sources:

Linux/macOS:
```
$ ./gradlew clean
$ ./gradlew build
$ ./gradlew getDependencies
```

Windows:
```
$ gradlew.bat clean
$ gradlew.bat build
$ gradlew.bat getDependencies
```

> [!NOTE]
> `getDependencies` downloads all dependencies into the `./build/libs` directory.

## Binaries
Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netflix.photon%22).

Change history and version numbers are available at [CHANGELOG.md](https://github.com/Netflix/photon/blob/master/CHANGELOG.md).

Example for Maven:

```xml
<dependency>
    <groupId>com.netflix.photon</groupId>
    <artifactId>Photon</artifactId>
    <version>4.10.8</version>
</dependency>
```
and for Ivy:

```xml
<dependency org="com.netflix.photon" name="Photon" rev="4.10.8" />
```

## Documentation

### Sample Applications
Multiple sample applications have been provided with this project (e.g., com.netflix.imflibrary.app.IMFTrackFileReader). Having obtained the dependencies, you can run an application as follows:

#### Linux/macOS:
```
java -cp "./build/libs/*:" <fully qualified class name> <arguments>
```
Example:
```
// Analyze an IMF Delivery locally
java -cp "./build/libs/*:" com.netflix.imflibrary.app.IMPAnalyzer local_folder_path

// Analyze an IMF Delivery in a S3 bucket
java -cp "./build/libs/*:" com.netflix.imflibrary.app.IMPAnalyzer s3://path/to/IMFDelivery/

// Analyze an individual IMF asset (e.g. AssetMap, PKL, CPL, MXF Track File)
java -cp "./build/libs/*:" com.netflix.imflibrary.app.IMPAnalyzer local_file_path
```

#### Windows:
```
java -cp build\libs\*; <fully qualified class name> <arguments>
```
Example:
```
// Analyze an IMF Delivery locally
java -cp build\libs\*; com.netflix.imflibrary.app.IMPAnalyzer IMP_folder_path
```
### S3 Access
Photon supports S3 URIs through [aws-java-nio-spi-for-s3](https://github.com/awslabs/aws-java-nio-spi-for-s3). No Photon-specific setup is needed, instead the S3 CLI config and credentials are used directly (see [here](https://docs.aws.amazon.com/cli/v1/userguide/cli-configure-files.html) for instructions).

Example (Linux/macOS):
```
// Analyze an IMF Delivery in a S3 bucket
java -cp "./build/libs/*:" com.netflix.imflibrary.app.IMPAnalyzer s3://imf-plugfest-imf-plugfest/plugfest_2024_11/source_test_vectors/From_Colorfront/HTJ2K/plugest_FTR_C_EN-XX_US-NR_51_UHD_20241105_OV/
```


### API and Developer Documentation

API documentation is available via [Javadoc](http://netflix.github.io/photon/).

More information is available in the [Wiki](https://github.com/Netflix/photon/wiki).
