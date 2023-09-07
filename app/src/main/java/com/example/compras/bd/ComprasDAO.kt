package com.example.compras.bd

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ComprasDAO {

    @Query("SELECT * FROM compras ORDER BY realizada")
    fun findAll(): List<Compras>

    @Query("SELECT COUNT(*) FROM compras")
    fun contar(): Int

    @Insert
    fun insertar(compras: Compras):Long

    @Update
    fun actualizar(compras: Compras)

    @Delete
    fun eliminar(compras: Compras)
}