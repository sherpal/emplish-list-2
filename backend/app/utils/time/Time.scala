package utils.time

import java.time.{LocalDateTime, ZoneId, ZoneOffset}

object Time {

  final val zoneOffset: ZoneOffset = ZoneOffset.UTC
  final val zoneId = ZoneId.of(zoneOffset.getId)

  def dateNow: LocalDateTime = LocalDateTime.now(zoneId)
  def epochSecond: Long = dateNow.toEpochSecond(ZoneOffset.UTC)

}
