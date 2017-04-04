#!/bin/bash

if [ "$1" = "veb" ]; then 
	mkdir __bin
	javac -d __bin -cp lib/hamcrest-core-1.3.jar:lib/junit-4.11.jar \
		src/test/VEBTest.java \
		src/test/TestRunner.java \
		src/veb/IntegerSet.java \
		src/veb/VEBTree.java
	cd __bin
	java -Xmx4G -cp ../lib/hamcrest-core-1.3.jar:../lib/junit-4.11.jar:./ \
		test.TestRunner
	cd ..
	rm -r __bin
else
	echo unknown option $1
fi