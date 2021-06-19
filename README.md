## ScalaJS/Akka-http/Sloth RPC/BooPickle/Ajax project

This minimal ScalaJS Akka-Http project is an example of using [Sloth](https://github.com/cornerman/sloth) and [Boopickle](https://boopickle.suzaku.io) for type safe and fast RPC using Ajax to pull data from server to client both sharing the same API. Sloth allows to use any Functor server return type - here a `Future` is used.

The client initiates the Ajax call by serializing the API method arguments and sending them to the server. The Server then de-serializes the arguments and calls the requested method. The result of this call is serialized and sent back as the Ajax response to the client where it is de-serialized.

Since we use `Future`s as the higher-kind return type in our API, the result from calling the methods on the server can both return successful data, but also failed futures containing `Throwable` exceptions. Both are serialized as `ByteBuffer`s. This makes it a bit trickier on the client side where we need to know if the byte data should be de-serialized to a successful future of data matching the API method result type - OR a failed future with a Throwable! 

Optimally, we would like to receive a response header telling us if data should be de-serialized as an exception or successful data. For some reason I couldn't get this to work, so as a temporary solution, I added a single bit in the beginning of the ByteBuffer with a 1 for Throwable and 0 for successful data that I then remove before de-serializing on the client.

If there's a more straightforward way to accomplish this exercise, I'd appreciate feedback!

To test, first download this repo and start the Akka-Http server in one process:

    git clone https://github.com/marcgrue/scalajs-rpc-exception-test.git
    cd scalajs-rpc-exception-test
    sbt rpcJVM/run

In another process, run 

    sbt test

Enjoy!