package protocole

import urldsl.language.QueryParameters.dummyErrorImpl._

object Users {

  final val userName = param[String]("userName")
  final val randomKey = param[String]("randomKey")

}
