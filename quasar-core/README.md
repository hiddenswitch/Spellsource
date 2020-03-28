# quasar-core fork

This is a fork of Parallel Universe's Quasar fibers library for Java, updated to work only with Java 11 without modules. It fixes important bugs related to package relocation, removes the kryo-based serialization (since it was buggy), fixes an issue with timeouts, fixes an issue with stack sizes, and adds support for suspendable iterators.

To instrument a module, add `apply from: '../gradle/instrument.gradle'` to its **build.gradle** file. This will also create an `uninstrumentedJars` configuration that can be used when instrumentation is not possible, as with ahead-of-time compiled binaries.