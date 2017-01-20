#!/usr/bin/env bash

PARSEC_RDL_VERSION="$1"
DESTINATION_DIR=$2
OS_NAME=$3

METADATA_FILE=metadata.xml
BASE_PATH="https://github.com/ardielle/ardielle-tools/releases/download"
DESTINATION_PATH="$DESTINATION_DIR/src/main/resources/rdl-bin"

if [ "$OS_NAME" == "darwin" ]; then
    DOWNLOAD_FILE="rdl-$PARSEC_RDL_VERSION-darwin.zip"
    DIST_FILE="rdl.zip"
else
    DOWNLOAD_FILE="rdl-$PARSEC_RDL_VERSION-linux.tgz"
    DIST_FILE="rdl.tgz"
fi

URL_PATH="$BASE_PATH/v$PARSEC_RDL_VERSION/$DOWNLOAD_FILE"

echo "Fetching $DOWNLOAD_FILE version: $PARSEC_RDL_VERSION"

HTTP_RESPONSE_CODE=`curl -w "%{http_code}" -L "$URL_PATH" -o $DESTINATION_PATH/$DIST_FILE`
if [ "$HTTP_RESPONSE_CODE" -ne 200 ]; then
    echo "ERROR: Could not download $DIST_FILE: $PARSEC_RDL_VERSION"
    echo "HTTP_RESPONSE_CODE: $HTTP_RESPONSE_CODE"
    exit -1
else
    echo "Download Complete. Writing version number $PARSEC_RDL_VERSION to $DESTINATION_PATH/$METADATA_FILE"
    echo $PARSEC_RDL_VERSION > $DESTINATION_PATH/$METADATA_FILE
fi
