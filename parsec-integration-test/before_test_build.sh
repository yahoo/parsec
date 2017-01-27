echo "[INFO] Starting.."

ARTIFACT_ID=parsec_sample_webapp
TEST_BASE_DIR=$1
CURRENT_DIR=$2
TEST_SOURCE_DIR="$TEST_BASE_DIR/$ARTIFACT_ID"
GRADLE="$CURRENT_DIR/../gradlew"

##Kill jetty process
echo "[INFO] kill jetty process .."
ps auxwww | grep jettyRun | grep `whoami` | xargs kill

##build sample project
echo "[INFO] generate test project .."
cd $TEST_BASE_DIR && $GRADLE createParsecProject -PgroupId=com.example -PartifactId=parsec_sample_webapp

##parse rdl and code generated
echo "[INFO] generate code & inject test implementation to sample handler .. "
cp $CURRENT_DIR/src/integration-test/resources/sample.rdl $TEST_SOURCE_DIR/src/main/rdl/
cd $TEST_SOURCE_DIR && $GRADLE parsec-generate -Pprofile=it
cp -f $CURRENT_DIR/src/integration-test/resources/SampleHandlerImpl.java $TEST_SOURCE_DIR/src/main/java/com/example/

##run local jetty
echo "[INFO] start run jetty .. "
cd $TEST_SOURCE_DIR && $GRADLE appBeforeIntegrationTest --info --debug

