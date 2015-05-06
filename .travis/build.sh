#!/bin/bash

if [[ $TRAVIS_BRANCH == 'stable' ]]
  echo "Releasing!"
  mvn install -DskipTests
  mvn test
else
  mvn install -DskipTests
  mvn test
fi