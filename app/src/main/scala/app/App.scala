package app

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import app.config.AppConfig
import kamon._
import kamon.prometheus.PrometheusReporter
import kamon.akka.http.instrumentation.ServerFlowWrapper
import kamon.system.SystemMetrics
import kamon.trace.Tracer

object App extends App with Routes with Flows {

  Kamon.addReporter(new PrometheusReporter())
  //SystemMetrics.startCollecting()

  implicit val system       = ActorSystem("app-actor-system")
  implicit val materializer = ActorMaterializer()

  val interface = "0.0.0.0"
  val port      = AppConfig.port

  val connectionsSource = Http().bind(interface = interface, port = port)

  val queue = Source
    .queue(10000, OverflowStrategy.backpressure)
    .via(publishRequestToPubSub)
    .toMat(Sink.ignore)(Keep.left)
    .run()

  val flow = connectionsSource
    .mapAsyncUnordered(1024) { conn =>
      logger.trace(s"Created $conn")
      Future { conn.handleWith(ServerFlowWrapper.apply(routes(queue), interface, port)) }
    }
    .recover { case ex => logger.error("Could not start HTTP connection", ex) }

  logger.info("Started app app...")

  Await.ready(flow.runWith(Sink.ignore), Duration.Inf)
}
