package app

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{ RequestContext, Route }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.SourceQueue
import com.typesafe.scalalogging.StrictLogging
import kamon.akka.http.TracingDirectives
import spray.json._

trait Routes extends StrictLogging with TracingDirectives {

  private[app] def now() = DateTime.now

  private[this] def missingParamRejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[MissingQueryParamRejection] { missingParamRejections =>
        complete(
          (StatusCodes.BadRequest,
           JsObject(
             "error" -> JsString(s"One of the required query parameters '${missingParamRejections.map(_.parameterName).mkString(", ")}' not found")
           )))
      }
      .result()

  def routes(queue: SourceQueue[HttpRequest])(implicit materializer: ActorMaterializer): Route = {
    get {
      path("health") { complete("OK") }
    }
  }

}
