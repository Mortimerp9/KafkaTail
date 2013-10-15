import java.util.concurrent.Executors
import java.util.Properties
import kafka.consumer.{Consumer, ConsumerConfig}
import kafka.utils.Utils
import unfiltered.netty.Http
import collection.JavaConverters._

class KafkaTopic(zk: String, topic: String, func: kafka.message.Message => Unit) {
  // specify some consumer properties
  val props = new Properties()
  props.put("zk.connect", zk)
  props.put("zk.connectiontimeout.ms", "1000000")
  props.put("groupid", "kafkatail")

  // Create the connection to the cluster
  val consumerConfig = new ConsumerConfig(props)
  val consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig)

  val topicMessageStreams =
    consumerConnector.createMessageStreams(Map(topic -> new Integer(4)).asJava)
  val streams = topicMessageStreams.get(topic)

  val executor = Executors.newFixedThreadPool(4)

  for (stream <- streams.asScala) {
    executor.submit(new Runnable() {
      def run() {
        for (msg <- stream.asScala) {
          func(msg)
        }
      }
    })
  }

  def shutdown() = {
    consumerConnector.shutdown()
    executor.shutdownNow()
  }

}

object App {

  import unfiltered.netty.websockets._
  import unfiltered.util._
  import scala.collection.mutable.ConcurrentMap
  import unfiltered.response.ResponseString

  case class SocketTopic(socket: WebSocket, topicMap: ConcurrentMap[String, KafkaTopic] = new java.util.concurrent.ConcurrentHashMap[String, KafkaTopic].asScala) {
    def add(topic: String, kafka: KafkaTopic) {
      topicMap.put(topic, kafka)
    }
    def remove(topic: String) {
      topicMap.get(topic).foreach(_.shutdown())
    }
  }

  val AddReg = "(.*)\\|(.*)".r

  def main(args: Array[String]) {
    require(args.length > 0, "missing Zookeeper connection details")
    val sockets: ConcurrentMap[Int, SocketTopic] =
      new java.util.concurrent.ConcurrentHashMap[Int, SocketTopic].asScala

    def websock(h: Http) {
      unfiltered.netty.Http(5679).handler(unfiltered.netty.websockets.Planify({
        case _ => {
          case Open(s) =>
            sockets += (s.channel.getId.intValue -> SocketTopic(s))
            s.send("sys|hola!")
          case Message(s, Text(AddReg(command, topic))) =>
            command match {
              case "add" =>
                sockets.get(s.channel.getId.intValue) foreach {
                  case st@SocketTopic(_, topicMap) if !topicMap.contains(topic) =>
                    println("adding %s to topics".format(topic))
                    st.add(topic, new KafkaTopic(args(0), topic, {
                      msg =>
                        val txt = Utils.toString(msg.payload, "UTF-8")
                        s.send("%s|%s|%s".format(topic, System.currentTimeMillis, txt))
                    }))
                  case _ =>
                }
              case "remove" =>
                sockets.get(s.channel.getId.intValue) foreach {
                  case st: SocketTopic =>
                    println("removing %s".format(topic))
                    st.remove(topic)
                }
            }
          case Close(s) =>
            sockets.get(s.channel.getId.intValue) match {
              case Some(SocketTopic(_, topicMap)) =>
                topicMap.foreach {
                  case (t, k) => k.shutdown()
                }
              case None => //???
            }
            sockets -= s.channel.getId.intValue
          case Error(s, e) =>
            e.printStackTrace
        }
      })
        .onPass(_.sendUpstream(_))
      )
        .handler(unfiltered.netty.cycle.Planify {
        case _ => ResponseString("not a websocket")
      })
        .run

    }

    val root = this.getClass.getResource("kafkatail/")
    unfiltered.netty.Http(8000)
      .resources(root) //whatever is not matched by our filter will be served from the resources folder (html, css, ...)
      .run(websock)

  }
}
