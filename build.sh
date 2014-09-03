#!/bin/bash
# TODO: replace with ANT make file
if [ ! -e bin ] ; then
	mkdir bin
fi
javac -classpath ./lib/dom4j-1.6.1.jar:./lib/jaxen-1.1.1.jar \
	-sourcepath ./src \
	-d ./bin \
	-target 1.7 -source 1.7 \
	./src/ie/wombat/icrdl/MakeCHeaders.java

