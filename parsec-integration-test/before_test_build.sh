check_jetty_is_alive() {
    local wait_time=$1
    local flag=1
    for h in `seq 1 $wait_time`; do
        sleep 1;
        jetty_proc=$(ps auxwww | grep jettyRun | grep `whoami` | grep -v "grep jettyRun")
        if [ $? -eq 0 ]; then
            ## grep pattern is both compatible with mac and linux
            port_listen=$(netstat -na | grep "[.:]8080" | grep LISTEN)
            if [ $? -eq 0 ]; then
                echo "[INFO] Detected that jetty is running.."
                flag=0;
                break;
            fi
        fi
    done
    return $flag;
}

echo "[INFO] Starting.."

ARTIFACT_ID=parsec_sample_webapp
TEST_BASE_DIR=$1
CURRENT_DIR=$2
TEST_SOURCE_DIR="$TEST_BASE_DIR/$ARTIFACT_ID"
WAIT_TIME=100
GRADLE=$CURRENT_DIR/../gradlew

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
cd $TEST_SOURCE_DIR && $GRADLE jettyRun -Pprofile=it --stacktrace &

#Check for jetty
check_jetty_is_alive $WAIT_TIME
if [ $? -ne 0 ]; then
    echo "[ERROR] jettyRun process is still not alive after waiting for $WAIT_TIME secs .."
    exit -1
else
    echo "[INFO] jetty is running. Exiting with 0 .."
    exit 0
fi
