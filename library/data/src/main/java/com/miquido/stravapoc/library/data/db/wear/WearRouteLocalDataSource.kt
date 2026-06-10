package com.miquido.stravapoc.library.data.db.wear

import javax.inject.Inject

class WearRouteLocalDataSource @Inject constructor(private val dao: WearRouteDao) {

    suspend fun getAll(): List<WearRouteEntity> = dao.getAll()

    suspend fun getByActivityType(type: String): List<WearRouteEntity> = dao.getByActivityType(type)

    suspend fun getById(id: String): WearRouteEntity? = dao.getById(id)

    suspend fun insert(entity: WearRouteEntity) = dao.insert(entity)
}
