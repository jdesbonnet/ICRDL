#!/bin/bash
java -classpath ./lib/dom4j-1.6.1.jar:./lib/jaxen-1.1.1.jar:./bin \
	ie.wombat.icrdl.MakeCHeaders $@
