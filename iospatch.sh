#!/bin/sh

if [ -d ios/Frameworks ]; then
	
	if [ ! -d iostemp ]; then
  		mkdir -p iostemp;
	fi

	git clone https://github.com/MappCloud/React-native-plugin --depth 1 --branch=master iostemp
	cp -RPn iostemp/ios/Frameworks ios
	rm -rf iostemp
	echo "Finished pathching iOS"
fi
