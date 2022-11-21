#!/bin/sh

VERSION=$1

if [[ -z $VERSION ]];
then
  echo '请输入版本号'
  exit
fi

./gradlew clean build -b build.gradle -x test

docker build -t write-es-test:$1 .
