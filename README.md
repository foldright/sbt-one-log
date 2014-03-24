# sbt-one-log
sbt-one-log is a sbt plugin make log dependency easy.

## why sbt-one-log
Scala can leverage lots of perfect Java lib, but it's chaotic with the Log libs in Java world.
looking at the Log lib below:

* java.util.logging
* commons-logging
* commons-logging-api
* log4j
* slf4j
* logback
* log4j 2
* ...

of course, you can keep you project dependency cleanly with one or two log lib (e.g. slf4j and logback)

but sometime your other dependency is out of control. 
e.g. if you dependency with Apache httpclient lob which dependeny with commons-logging, you will log with commons-logging

also, you can add jcl-over-slf4j and exclud commons-logging explicitly in libraryDependencies setting.
A better way is to explicitly declare dependency commons-logging with the sepcial version 99-empty.

and sbt-one-log comes to free your hands.

## usage 
tbc...
