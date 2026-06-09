package com.miquido.stravapoc.library.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.miquido.stravapoc.library.`data`.db.entity.WorkoutResultEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class WorkoutResultDao_Impl(
  __db: RoomDatabase,
) : WorkoutResultDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutResultEntity: EntityInsertAdapter<WorkoutResultEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutResultEntity = object : EntityInsertAdapter<WorkoutResultEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `workout_results` (`id`,`routeId`,`routeName`,`activityType`,`totalDistanceKm`,`totalDurationSeconds`,`laps`,`timestamp`,`trackedPointsJson`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutResultEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.routeId)
        statement.bindText(3, entity.routeName)
        statement.bindText(4, entity.activityType)
        statement.bindDouble(5, entity.totalDistanceKm)
        statement.bindLong(6, entity.totalDurationSeconds)
        statement.bindLong(7, entity.laps.toLong())
        statement.bindLong(8, entity.timestamp)
        statement.bindText(9, entity.trackedPointsJson)
      }
    }
  }

  public override suspend fun insert(entity: WorkoutResultEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfWorkoutResultEntity.insertAndReturnId(_connection, entity)
    _result
  }

  public override fun getAllAsFlow(): Flow<List<WorkoutResultEntity>> {
    val _sql: String = "SELECT * FROM workout_results ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("workout_results")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRouteId: Int = getColumnIndexOrThrow(_stmt, "routeId")
        val _columnIndexOfRouteName: Int = getColumnIndexOrThrow(_stmt, "routeName")
        val _columnIndexOfActivityType: Int = getColumnIndexOrThrow(_stmt, "activityType")
        val _columnIndexOfTotalDistanceKm: Int = getColumnIndexOrThrow(_stmt, "totalDistanceKm")
        val _columnIndexOfTotalDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "totalDurationSeconds")
        val _columnIndexOfLaps: Int = getColumnIndexOrThrow(_stmt, "laps")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTrackedPointsJson: Int = getColumnIndexOrThrow(_stmt, "trackedPointsJson")
        val _result: MutableList<WorkoutResultEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutResultEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRouteId: String
          _tmpRouteId = _stmt.getText(_columnIndexOfRouteId)
          val _tmpRouteName: String
          _tmpRouteName = _stmt.getText(_columnIndexOfRouteName)
          val _tmpActivityType: String
          _tmpActivityType = _stmt.getText(_columnIndexOfActivityType)
          val _tmpTotalDistanceKm: Double
          _tmpTotalDistanceKm = _stmt.getDouble(_columnIndexOfTotalDistanceKm)
          val _tmpTotalDurationSeconds: Long
          _tmpTotalDurationSeconds = _stmt.getLong(_columnIndexOfTotalDurationSeconds)
          val _tmpLaps: Int
          _tmpLaps = _stmt.getLong(_columnIndexOfLaps).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTrackedPointsJson: String
          _tmpTrackedPointsJson = _stmt.getText(_columnIndexOfTrackedPointsJson)
          _item =
              WorkoutResultEntity(_tmpId,_tmpRouteId,_tmpRouteName,_tmpActivityType,_tmpTotalDistanceKm,_tmpTotalDurationSeconds,_tmpLaps,_tmpTimestamp,_tmpTrackedPointsJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): WorkoutResultEntity? {
    val _sql: String = "SELECT * FROM workout_results WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRouteId: Int = getColumnIndexOrThrow(_stmt, "routeId")
        val _columnIndexOfRouteName: Int = getColumnIndexOrThrow(_stmt, "routeName")
        val _columnIndexOfActivityType: Int = getColumnIndexOrThrow(_stmt, "activityType")
        val _columnIndexOfTotalDistanceKm: Int = getColumnIndexOrThrow(_stmt, "totalDistanceKm")
        val _columnIndexOfTotalDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "totalDurationSeconds")
        val _columnIndexOfLaps: Int = getColumnIndexOrThrow(_stmt, "laps")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTrackedPointsJson: Int = getColumnIndexOrThrow(_stmt, "trackedPointsJson")
        val _result: WorkoutResultEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRouteId: String
          _tmpRouteId = _stmt.getText(_columnIndexOfRouteId)
          val _tmpRouteName: String
          _tmpRouteName = _stmt.getText(_columnIndexOfRouteName)
          val _tmpActivityType: String
          _tmpActivityType = _stmt.getText(_columnIndexOfActivityType)
          val _tmpTotalDistanceKm: Double
          _tmpTotalDistanceKm = _stmt.getDouble(_columnIndexOfTotalDistanceKm)
          val _tmpTotalDurationSeconds: Long
          _tmpTotalDurationSeconds = _stmt.getLong(_columnIndexOfTotalDurationSeconds)
          val _tmpLaps: Int
          _tmpLaps = _stmt.getLong(_columnIndexOfLaps).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTrackedPointsJson: String
          _tmpTrackedPointsJson = _stmt.getText(_columnIndexOfTrackedPointsJson)
          _result =
              WorkoutResultEntity(_tmpId,_tmpRouteId,_tmpRouteName,_tmpActivityType,_tmpTotalDistanceKm,_tmpTotalDurationSeconds,_tmpLaps,_tmpTimestamp,_tmpTrackedPointsJson)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM workout_results WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
