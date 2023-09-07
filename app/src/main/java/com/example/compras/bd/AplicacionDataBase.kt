package com.example.compras.bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =[Compras::class], version = 1)
abstract class AplicacionDataBase : RoomDatabase() {
    abstract fun ComprasDao(): ComprasDAO

    companion object{
        @Volatile
        private var BASE_DATOS: AplicacionDataBase? = null

        fun getInstance(contexto: Context): AplicacionDataBase{
            return BASE_DATOS ?: synchronized(this){
                Room.databaseBuilder(
                    contexto.applicationContext,
                    AplicacionDataBase::class.java,
                    "compras.bd"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also{ BASE_DATOS = it}
            }
        }
    }
}