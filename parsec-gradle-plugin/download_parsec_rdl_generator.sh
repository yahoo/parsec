#!/usr/bin/env bash

PARSEC_RDL_GENERATOR_VERSION=$1
DESTINATION_DIR=$2

METADATA_FILE=metadata.xml

TMP_PATH=/tmp
URL_PATH="https://github.com/yahoo/parsec-rdl-gen/releases/download/v$PARSEC_RDL_GENERATOR_VERSION/rdl-gen.zip"

echo "Fetching $URL_PATH version: $PARSEC_RDL_GENERATOR_VERSION"

DIST_FILE="rdl-gen.zip"
DESTINATION_PATH="$DESTINATION_DIR/src/main/resources/rdl-gen"

HTTP_RESPONSE_CODE=`curl -w "%{http_code}" -L "$URL_PATH" -o $DESTINATION_PATH/$DIST_FILE`
if [ "$HTTP_RESPONSE_CODE" -ne 200 ]; then
    echo "ERROR: Could not download $DIST_FILE: $PARSEC_RDL_VERSION"
    echo "HTTP_RESPONSE_CODE: $HTTP_RESPONSE_CODE"
    exit -1
else
    echo "Download Complete. Writing version number $PARSEC_RDL_VERSION to $DESTINATION_PATH/$METADATA_FILE"
    echo $PARSEC_RDL_GENERATOR_VERSION > $DESTINATION_PATH/$METADATA_FILE
fi
