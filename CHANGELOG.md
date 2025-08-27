## Changes from Photon v4.x to v5

- updated to Gradle v8.5
- migrated from JDK8 to JDK11
- migrated from java.io to java.nio
- migrated from javax to jakarta (where applicable)
- added S3 support via aws-java-nio-spi-for-s3
- consolidated ApplicationComposition, AbstractApplicationComposition and IMFCompositionPlaylistype into IMFCompositionPlaylist
- moved validation code into new package com.netflix.imflibrary.validation
- separated validations for (revisions of) applications, core constraints, cpl and plug-ins
- introduced interface ConstraintsValidator and factory ConstraintsValidatorFactory
- replaced use of SequenceTypeEnum with Strings for SequenceType/Namespace for easier extensibility
- renamed IMPAnalyzer.analyzePackage to IMPAnalyzer.analyzeDelivery to reflect actual scope
- consolidated MXF utility methods in new class MXFUtils
- moved some virtual track validation methods to ConstraintsValidatorUtils, some to core constraints validation classes, and deleted others
