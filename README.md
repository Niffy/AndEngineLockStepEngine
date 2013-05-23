#Currently in development.

Using NIO selectors to create a p2p network.


First all clients will connect to one common client (Client A)
Then in future Client A will inform all other clients of a new client joining, so they can establish a connection between each other. In theory it may be possible to connect to any other client for this information.  We'll see how it goes.

The networking consists of 4 threads
CommunicationHandler which manage the selector threads.
ServerSelector = TCP Server
ClientSelector = TCP Client
UDPSelector = UDP client.

The reason why TCP server and tcp client are not in one thread is due to being unable to combine the two operations in a standard java project. I had problems in trying to finish the connection of. So for simplity and time issues, I just seperated the functions out. In Future I'll try and merge the two threads and perhaps merge in the UDP operations as well.

So Client will connect to a server and to send packets.

The server will accept connections and to receive packets.

UDP client is connectionless so these issues do not occur.

##Why NIO and not Sockets or ASyncTask?

I found the combination of Sockets with thread handlers to be tricky and messy when wanting to use [Handlers](http://developer.android.com/reference/android/os/Handler.html "Handlers") 

What handlers do is allow you to have a message queue (I did find a good website which explained it)
Basicly to communicate from one thread to another, you just posted messages to their handlers. These messages would be placed into a queue to process. Ideally they are used to post runnables.
So you set up a thread and then just let the thread loop handling these messages.

To create a handler you would call something like

```
Handler handler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
        //Handle msg
    }
};
```

However, since Andengine runs on an opengl thread, the thread doesn't have a looper, so how on earth can you have a thread with a handler without a looper? Easy, just create it on the Android UI thread.

**BUT** since the thread was doing networking stuff with sockets, running this code on Android honneycomb and above would result in an ```android.os.NetworkOnMainThreadException``` error.

**Dam, how do you solve this?**

Easy, in the thread run method you call

```
Looper.prepare();
Handler handler = new Handler(Looper.myLooper()){
    @Override
    public void handleMessage(Message msg) {
        //Handle msg
    }
};
//Do Socket stuff
Looper.loop();
```

**wrong!!!** You cannot block on a socket and process messages, you would have to interleave between the two tasks. Which for me sounded pretty hard and convoluted (Coming from the man with 3 selector threads!...)

###Solution###

Use non blocking NIO selectors and don't use handlers on the selector threads!

See [James Greenfield amazing ROX nio tutorial](http://rox-xmlrpc.sourceforge.net/niotut/ "ROX Tutorial") You will find the network code in the project is heavily based on his code. Which solves so many problems.  Sure it adds some concurrency blah blah into the mix, but its pretty simple and should cope.

The added bounus is this should scale quite well, using TCP sockets would have meant creating more threads, where with selectors we still have the same number of threads no matter how many connections we have.

In the selector threads you manage in the run method the requests and selector wake up calls.

So now a message is sent to the CommunicationHandler thread, which then calls the methods on the selector threads, which queues the task and gets executed.

###Why not ASyncTask###
ASyncTask is probably great for many people, but it seems a class which in its intent is good, but does seem to break some rules. You can search google for Aysnc memory leaks, weak references. 

