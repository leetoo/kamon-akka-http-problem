package app.config

import com.wacai.config.annotation._

object AppConfig extends app

@conf
trait app {

  val parallelism = 2
  val port        = 8700

  val pubsub = new {
    val topic = "pubsub_topic"
  }
}
