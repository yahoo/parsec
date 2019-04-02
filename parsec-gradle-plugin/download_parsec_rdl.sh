#!/usr/bin/env bash

PARSEC_RDL_VERSION="$1"
PARSEC_RDL_EXEC_FILE_VERSION="$2"
DESTINATION_DIR=$3

METADATA_FILE=metadata.xml
BASE_PATH="https://github.com/ardielle/ardielle-tools/releases/download"
DESTINATION_PATH="$DESTINATION_DIR/src/main/resources/rdl-bin"

download_rdl () {
    DOWNLOAD_FILE=$1
    DIST_FILE=$2

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
}

download_rdl "rdl_${PARSEC_RDL_EXEC_FILE_VERSION}_darwin.zip" "rdl.zip"
download_rdl "rdl_${PARSEC_RDL_EXEC_FILE_VERSION}_linux.tgz" "rdl.tgz"
