# Photon

Photon is an implementation of the Material Exchange Format (MXF) standard. MXF is a SMPTE standard defined in the
specification SMPTE st0377-1:2011. Photon parses and completely reads an MXF file containing a single essence
as defined by the IMF Essence Component (SMPTE st2067-5:2013) and serializes the metadata into an IMF Composition
Playlist (SMPTE st2067-3:2013).

The goal of the Photon is to provide a simple standardized interface to completely interpret an MXF essence.

## Build

Photon can be built very easily by using the included gradle wrapper. Simply invoke the following
commands inside the folder containing the sources:

$ ./gradlew clean
$ ./gradlew build

## JDK requirements

Although Photon can be built using JDK-7, it is recommended to use JDK-8 or later.

## Full Documentation

- [Wiki](https://github.com/Netflix/photon/wiki)
- [Javadoc](http://netflix.github.io/photon/)

