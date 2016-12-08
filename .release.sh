#!/usr/bin/env bash

TRAVIS_BRANCH=$1
#TODO: add condition for owner's repo only
MASTER="master"

if [ "$TRAVIS_BRANCH" == "$MASTER" ] ; then
    ./gradlew :release
else
    echo "This branch: $TRAVIS_BRANCH, will not be released"
fi