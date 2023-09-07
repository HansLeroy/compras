package com.example.compras.bd

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Compras (
    @PrimaryKey(autoGenerate = true) val id:Int,
    var compras:String,
    var realizada:Boolean
)