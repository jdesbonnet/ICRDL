#!/bin/bash
# TODO: replace with ANT make file
mkdir bin
javac -classpath ./lib/dom4j-1.6.1.jar:./lib/jaxen-1.1.1.jar \
	-sourcepath ./src \
	-d ./bin \
	./src/ie/wombat/icrdl/MakeCHeaders.java

