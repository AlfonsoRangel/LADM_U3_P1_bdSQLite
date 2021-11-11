package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class ControladorVehiculo( activity: AppCompatActivity)
{
    val activity = activity

    fun insertar( vehiculo: Vehiculo): Boolean {
        val tablaVehiculo = BD(activity, "Vehiculo", null, 1).writableDatabase
        val datos = ContentValues()
        datos.put("PLACA", vehiculo.placa)
        datos.put("MARCA", vehiculo.marca)
        datos.put("MODELO", vehiculo.modelo)
        datos.put("ANIO", vehiculo.anio)
        datos.put("TIPO", vehiculo.tipo)
        datos.put("COSTOxDIA", vehiculo.costoXdia)
        datos.put("IDCLIENTE", 0)

        if( tablaVehiculo.insert("Vehiculo",null , datos) == -1L){ return false }
        return true
    }


    fun filtrarPorBusqueda( subcadena : String ): ArrayList<Vehiculo>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo WHERE MARCA LIKE ? OR MODELO LIKE ?"
        val cursor = tablaVehiculo.rawQuery( SQL , arrayOf( "%$subcadena%", "%$subcadena%") )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun filtrarPorBusquedaSinCliente( subcadena : String ): ArrayList<Vehiculo>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo WHERE ( MARCA LIKE ? OR MODELO LIKE ? ) AND IDCLIENTE = 0"
        val cursor = tablaVehiculo.rawQuery( SQL , arrayOf( "%$subcadena%", "%$subcadena%") )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }




    fun getIDsClientesRentando( ): ArrayList<Int>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Int>()
        val SQL = "SELECT DISTINCT IDCLIENTE FROM Vehiculo WHERE IDCLIENTE != 0"
        val cursor = tablaVehiculo.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                resultado.add( cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) ) )
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }

    fun filtrarIDsClientes( listaV: ArrayList<Int> ): ArrayList<Int>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Int>()
        val valores = convertirListaAString(listaV)
        val SQL = "SELECT DISTINCT IDCLIENTE FROM Vehiculo WHERE IDVEHICULO IN(${valores})"
        val cursor = tablaVehiculo.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                resultado.add( cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) ) )
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }






    fun getVehiculosDeUnCliente( idCliente : Int ): ArrayList<Vehiculo>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo WHERE IDCLIENTE = ?"
        val cursor = tablaVehiculo.rawQuery( SQL , arrayOf( idCliente.toString() ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun filtrarTodo( ): ArrayList<Vehiculo>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo"
        val cursor = tablaVehiculo.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun filtrarVehiculosSinCliente( ): ArrayList<Vehiculo>
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo WHERE IDCLIENTE = 0"
        val cursor = tablaVehiculo.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun filtrarPorAniosDeUso( aniosUsoInicial : Int , aniosUsoFinal : Int ): ArrayList<Vehiculo>
    {
        val anioI = Fecha(Calendar.getInstance()).getYear().toInt() - aniosUsoInicial
        val anioF = Fecha(Calendar.getInstance()).getYear().toInt() - aniosUsoFinal
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).readableDatabase
        val resultado = ArrayList<Vehiculo>()
        val SQL = "SELECT * FROM Vehiculo WHERE ANIO BETWEEN ? AND ?"
        val cursor = tablaVehiculo.rawQuery( SQL , arrayOf( anioF.toString() , anioI.toString() ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var vehiculo = Vehiculo(
                    cursor.getString( cursor.getColumnIndex( "PLACA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MODELO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "MARCA" ) ) ,
                    cursor.getInt( cursor.getColumnIndex( "ANIO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TIPO" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTOxDIA" ) )
                )
                vehiculo.id = cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) )
                vehiculo.idCliente = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                resultado.add(vehiculo)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun eliminar (id : Int) : Boolean
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).writableDatabase
        val resultado = tablaVehiculo.delete("Vehiculo","IDVEHICULO=?", arrayOf(id.toString()))
        if (resultado == 0)  return false
        return true
    }


    fun guardarCambios ( vehiculo : Vehiculo)
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).writableDatabase
        val SQL = "UPDATE Vehiculo SET PLACA = ? , MODELO = ? , MARCA = ? , ANIO = ? , TIPO = ? , COSTOxDIA = ? WHERE IDVEHICULO = ?"
        tablaVehiculo.execSQL( SQL , arrayOf(
            vehiculo.placa ,
            vehiculo.modelo ,
            vehiculo.marca ,
            vehiculo.anio ,
            vehiculo.tipo ,
            vehiculo.costoXdia ,
            vehiculo.id
        ))
    }

    fun actualizarCliente ( idVehiculo : Int , idCliente: Int)
    {
        val tablaVehiculo = BD( activity ,"Vehiculo",null,1).writableDatabase
        val SQL = "UPDATE Vehiculo SET IDCLIENTE = ? WHERE IDVEHICULO = ?"
        tablaVehiculo.execSQL( SQL , arrayOf( idCliente , idVehiculo ))
    }



    fun convertirListaAString( lista: ArrayList<Int>) : String {
        var cad = ""
        lista.forEach {
            cad = cad + it.toString() + ","
        }
        return cad.substring( 0 , cad.length - 1 )
    }
}