package com.miquido.stravapoc.library.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.miquido.stravapoc.library.`data`.db.dao.WorkoutResultDao
import com.miquido.stravapoc.library.`data`.db.dao.WorkoutResultDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDatabase_Impl : AppDatabase() {
  private val _workoutResultDao: Lazy<WorkoutResultDao> = lazy {
    WorkoutResultDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2,
        "ce0ea48dd72e731997d881c0135533a1", "f160df7de7713d228dabbad741772c58") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routeId` TEXT NOT NULL, `routeName` TEXT NOT NULL, `activityType` TEXT NOT NULL, `totalDistanceKm` REAL NOT NULL, `totalDurationSeconds` INTEGER NOT NULL, `laps` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `trackedPointsJson` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ce0ea48dd72e731997d881c0135533a1')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `workout_results`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsWorkoutResults: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutResults.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("routeId", TableInfo.Column("routeId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("routeName", TableInfo.Column("routeName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("activityType", TableInfo.Column("activityType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("totalDistanceKm", TableInfo.Column("totalDistanceKm", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("totalDurationSeconds", TableInfo.Column("totalDurationSeconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("laps", TableInfo.Column("laps", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutResults.put("trackedPointsJson", TableInfo.Column("trackedPointsJson",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutResults: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutResults: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWorkoutResults: TableInfo = TableInfo("workout_results", _columnsWorkoutResults,
            _foreignKeysWorkoutResults, _indicesWorkoutResults)
        val _existingWorkoutResults: TableInfo = read(connection, "workout_results")
        if (!_infoWorkoutResults.equals(_existingWorkoutResults)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_results(com.miquido.stravapoc.library.data.db.entity.WorkoutResultEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutResults + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutResults)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "workout_results")
  }

  public override fun clearAllTables() {
    super.performClear(false, "workout_results")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WorkoutResultDao::class, WorkoutResultDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun workoutResultDao(): WorkoutResultDao = _workoutResultDao.value
}
