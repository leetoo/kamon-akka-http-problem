package app

import scala.concurrent.duration._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, RestartFlow }
import com.typesafe.scalalogging.StrictLogging
import app.config.AppConfig

trait Flows extends StrictLogging {

  protected[app] def publishRequestToPubSub(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Flow[HttpRequest, String, NotUsed] =
    Flow[HttpRequest].map { req =>
      ""
    }

}
