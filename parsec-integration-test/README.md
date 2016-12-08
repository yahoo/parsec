#Integration Test

Parsec's integration test will test on the current working versions of the subprojects. Thus, it requires the subprojects
to be installed/deployed to the local Maven repository. However the `integrationTest` task has dependency on all subprojects'
`install` task, thus it should be installed automatically when running integration test.

The integration test does not like running with Gradle Daemon, as it will interfere with the subprocesses in tasks. Please
disable Gradle Daemon before running the integration test. If the integration test hangs after jetty starts running, you
can stop the server by running `$ gradle appStop -Pprofile=it` at the test directory.


