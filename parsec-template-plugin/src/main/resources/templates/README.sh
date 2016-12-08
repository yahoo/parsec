#!/bin/sh

CURRENT=$(date -u '+%F %T %Z')

if [ -z ${VERSION} ]; then
    echo "ERROR: No version number defined";
    exit 1;
fi

cat <<EOF

${PRODUCT_NAME}

Version ${VERSION} (${CURRENT})
  * Init

EOF