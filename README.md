# scalajs-rpc-exception-test

Trying to find out how to receive a failed Future with Sloth/Boopickle rpc.

To test, first download this repo and start the Akka-Http server in one process:

    git clone https://github.com/marcgrue/scalajs-rpc-exception-test.git
    cd scalajs-rpc-exception-test
    sbt rpcJVM/run

In another process, run `sbt test` which on the JVM platform runs as expected. On the JS platform, the rpc call fails to receive a failed Future with an ArithmeticException("/ by zero"):

    java.lang.AssertionError: assertion failed: ==> assertion failed: 15 != expected a failed Future with the ArithmeticException...

Instead the result has been deserialized to '15' which is the index number of the ArithmeticException of the CompositePickler[Throwable] in [boopickle.CompositePicklers](https://github.com/suzaku-io/boopickle/blob/master/boopickle/shared/src/main/scala/boopickle/CompositePicklers.scala#L110)

Is there a way to get the expected ArithmeticException with the rpc call?