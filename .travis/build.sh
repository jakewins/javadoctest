#!/bin/bash

tags=`git tag -l --contains HEAD`

if [[ $tags =~ .*release.* ]]
then
  # Travis got a release tag pushed to it, build and push it to public repositories!
  echo "====== Releasing ======"

  # Make GPG key available
  echo $GPG_KEY > secret.gpg.key
  gpg --allow-secret-key-import --import secret.gpg.key

  # Build & deploy
  # mvn clean deploy -P release --settings .travis/settings.xml
  mvn clean install --settings .travis/settings.xml
else
  mvn install -DskipTests
  mvn test
fi