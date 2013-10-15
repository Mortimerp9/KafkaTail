Kafka Tail
==========

This is a little webapp that you can run locally to monitor the tail of your kafka queues. It is somehow equivalent to doing a `tail -f` on a file: you select a topic, and you will see the latest messages on the queue.

**This is still a work in progress**

Usage
-----

This is not yet distributed as a package, so you will need `scala` and `sbt` on your machine, then you can clone this repository, type `sbt run` on the command line and open your browser at [http://localhost:8000/index.html](http://localhost:8000/index.html).
