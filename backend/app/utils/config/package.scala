package utils

import zio.Has

package object config {

  type Configuration = Has[Configuration.Service]

}
