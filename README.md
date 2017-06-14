# mi-psl
Semestral work for MI-PSL (programing in Scala) course. Simple program for applying depth based blur (portions of image are blurred based on their distance from camera) to images. For this you'll need image with corresponding depth map. Some nice examples can be found [here](http://vision.middlebury.edu/stereo/).

## Requirements

* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)*
* [sbt](http://www.scala-sbt.org/)

* If you want to use different JDK, like [OpenJDK](http://openjdk.java.net/), you may have to install JavaFX library separately.

## Build and run

```
$ git clone https://github.com/ggljzr/mi-psl
$ sbt run
```

This should collect all dependencies, build and run the program.
