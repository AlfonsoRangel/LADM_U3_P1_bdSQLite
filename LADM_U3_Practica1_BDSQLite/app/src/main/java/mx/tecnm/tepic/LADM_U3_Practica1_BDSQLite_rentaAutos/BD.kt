package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BD(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int
    ) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(bd: SQLiteDatabase) {
        bd.execSQL(
            "CREATE TABLE Cliente( " +
                    "IDCLIENTE integer primary key autoincrement, " +
                    "NOMBRE varchar(200) ," +
                    "TELEFONO varchar(200) ," +
                    "NOLICENCIA varchar(200)" +
                ")"
        )

        bd.execSQL(
            "CREATE TABLE Vehiculo( " +
                    "IDVEHICULO integer primary key autoincrement, " +
                    "PLACA varchar(200) ," +
                    "MARCA varchar(200) ," +
                    "MODELO varchar(200) ," +
                    "ANIO integer ," +
                    "TIPO varchar(200) ," +
                    "COSTOxDIA FLOAT ," +
                    "IDCLIENTE integer ," +
                    "CONSTRAINT fk_cliente FOREIGN KEY (IDCLIENTE) REFERENCES Cliente(IDCLIENTE)" +
                ")"
        )

        bd.execSQL(
            "CREATE TABLE Renta( " +
                    "IDRENTA integer primary key autoincrement, " +
                    "IDVEHICULO integer ," +
                    "FECHA_ENTREGA text ," +
                    "FECHA_DEVOLUCION text ," +
                    "COSTO float ," +
                    "ESTATUS varchar(200)" +
                ")"
        )
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            //se ejecuta siempre que, tras una actualizacion de la app, haya un cambio en la version


        }

}