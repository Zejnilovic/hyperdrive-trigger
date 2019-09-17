package za.co.absa.hyperdrive.trigger.models.tables

import play.api.libs.json.JsValue
import za.co.absa.hyperdrive.trigger.models.tables.JDBCProfile.profile._
import za.co.absa.hyperdrive.trigger.models.{Event, Sensor}
import za.co.absa.hyperdrive.trigger.models.tables.JdbcTypeMapper._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

final class EventTable(tag: Tag) extends Table[Event](tag, _tableName = "event") {

  def sensorEventId: Rep[String] = column[String]("sensor_event_id", O.Length(70), O.Unique)
  def sensorId: Rep[Long] = column[Long]("sensor_id")
  def payload: Rep[JsValue] = column[JsValue]("payload")
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc, O.SqlType("BIGSERIAL"))

  def sensor_fk: ForeignKeyQuery[SensorTable, Sensor] =
    foreignKey("event_sensor_fk", sensorId, TableQuery[SensorTable])(_.id)

  def * : ProvenShape[Event] = (sensorEventId, sensorId, payload, id) <> (
    eventTuple =>
      Event.apply(
        sensorEventId = eventTuple._1,
        sensorId = eventTuple._2,
        payload = eventTuple._3,
        id = eventTuple._4
      ),
      Event.unapply
  )

}