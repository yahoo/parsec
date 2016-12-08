#!/usr/bin/env bash

PARSEC_RDL_VERSION=$1
DESTINATION_DIR=$2

METADATA_FILE=maven-metadata.xml

BINTRAY_HOST="https://dl.bintray.com/wayne-wu"
BINTRAY_REPO="gradle"
BINTRAY_PATH="com/yahoo/parsec/rdl_bin"
DESTINATION_PATH=$DESTINATION_DIR/src/main/resources/rdl_bin
TMP_PATH=/tmp
DIST_FILE=parsec_rdl.zip

echo "Fetching $DIST_FILE version: $PARSEC_RDL_VERSION"

URL_PATH="$BINTRAY_HOST/$BINTRAY_REPO/$BINTRAY_PATH/$PARSEC_RDL_VERSION/$DIST_FILE"

HTTP_RESPONSE_CODE=`curl -w "%{http_code}" -L "$URL_PATH" -o $DESTINATION_PATH/$DIST_FILE`
if [ "$HTTP_RESPONSE_CODE" -ne 200 ]; then
    echo "ERROR: Could not download $DIST_FILE: $PARSEC_RDL_VERSION"
    echo "HTTP_RESPONSE_CODE: $HTTP_RESPONSE_CODE"
    exit -1
else
    echo "Download Complete. Writing version number $PARSEC_RDL_VERSION to $DESTINATION_PATH/$METADATA_FILE"
    echo $PARSEC_RDL_VERSION > $DESTINATION_PATH/$METADATA_FILE
fi
