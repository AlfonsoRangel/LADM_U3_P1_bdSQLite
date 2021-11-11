package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class ControladorRenta( activity: AppCompatActivity)
{
    val activity = activity

    fun insertar( renta: Renta): Boolean {
        val tablaRenta = BD(activity, "Renta", null, 1).writableDatabase
        val datos = ContentValues()
        datos.put("IDVEHICULO", renta.idVehiculo )
        datos.put("FECHA_ENTREGA", renta.fechaEntrega)
        datos.put("FECHA_DEVOLUCION", renta.fechaDevolucion)
        datos.put("COSTO", renta.costo)
        datos.put("ESTATUS", renta.estatus)

        if( tablaRenta.insert("Renta",null , datos) == -1L){ return false }
        return true
    }


    fun getIDsVehiculosActivos( ): ArrayList<Int>
    {
        val tablaRenta = BD( activity ,"Renta",null,1).readableDatabase
        val resultado = ArrayList<Int>()
        val fechaActual = getFechaSQL( Calendar.getInstance() )
        //Toast.makeText(activity , getFechaSQL(fechaActual)  , Toast.LENGTH_LONG).show()
        val SQL = "SELECT DISTINCT IDVEHICULO FROM Renta WHERE ESTATUS = ? AND ? >= FECHA_ENTREGA AND ? <= FECHA_DEVOLUCION"
        val cursor = tablaRenta.rawQuery( SQL , arrayOf( "ACTIVO" , fechaActual , fechaActual ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                resultado.add( cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) ) )
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun getIDsVehiculosVencidos( ): ArrayList<Int>
    {
        val tablaRenta = BD( activity ,"Renta",null,1).readableDatabase
        val resultado = ArrayList<Int>()
        val fechaActual = Calendar.getInstance()
        val SQL = "SELECT DISTINCT IDVEHICULO FROM Renta WHERE ESTATUS = ? AND ? > FECHA_DEVOLUCION"
        val cursor = tablaRenta.rawQuery( SQL , arrayOf( "ACTIVO" , getFechaSQL(fechaActual) ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                resultado.add( cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) ) )
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }



    fun filtrarTodo( ): ArrayList<Renta>
    {
        val tablaRenta = BD( activity ,"Renta",null,1).readableDatabase
        val resultado = ArrayList<Renta>()
        val SQL = "SELECT * FROM Renta"
        val cursor = tablaRenta.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var renta = Renta(
                    cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "FECHA_ENTREGA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "FECHA_DEVOLUCION" ) ) ,
                    cursor.getFloat( cursor.getColumnIndex( "COSTO" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "ESTATUS" ) )
                )
                renta.id = cursor.getInt( cursor.getColumnIndex( "IDRENTA" ) )
                resultado.add(renta)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }

    fun filtrarUnaRenta( idVehiculo: Int ): Renta
    {
        val tablaRenta = BD( activity ,"Renta",null,1).readableDatabase
        val SQL = "SELECT * FROM Renta WHERE IDVEHICULO = ? AND ESTATUS = ?"
        var resultado: Renta? = null
        val cursor = tablaRenta.rawQuery( SQL , arrayOf( idVehiculo.toString() , "ACTIVO" ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            resultado = Renta(
                cursor.getInt( cursor.getColumnIndex( "IDVEHICULO" ) ) ,
                cursor.getString( cursor.getColumnIndex( "FECHA_ENTREGA" ) ) ,
                cursor.getString( cursor.getColumnIndex( "FECHA_DEVOLUCION" ) ) ,
                cursor.getFloat( cursor.getColumnIndex( "COSTO" ) ) ,
                cursor.getString( cursor.getColumnIndex( "ESTATUS" ) )
            )
            return resultado
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado!!
    }



    fun eliminar (idVehiculo : Int) : Boolean
    {
        val tablaRenta = BD( activity ,"Renta",null,1).writableDatabase
        val resultado = tablaRenta.delete("Renta","IDVEHICULO=? AND ESTATUS=?",
            arrayOf(idVehiculo.toString() , "ACTIVO" ))
        if (resultado == 0) return false
        ControladorVehiculo(activity).actualizarCliente( idVehiculo , 0 )
        return true
    }


    fun finalizar ( idVehiculo : Int)
    {
        val tablaRenta = BD( activity ,"Renta",null,1).writableDatabase
        val SQL = "UPDATE Renta SET ESTATUS = ? WHERE IDVEHICULO=? AND ESTATUS=?"
        tablaRenta.execSQL( SQL , arrayOf( "FINALIZADO" , idVehiculo.toString() , "ACTIVO" ))
        ControladorVehiculo(activity).actualizarCliente( idVehiculo , 0 )
    }




    fun getFechaSQL( fecha: Calendar ) : String {
        return getYear(fecha) + "-" + getMes(fecha) + "-" + getDia(fecha)
    }


    fun getDia( fecha: Calendar): String{
        var dia = fecha.get( Calendar.DAY_OF_MONTH ).toString()
        if( dia.toInt() < 10){ dia = "0$dia" }
        return dia
    }

    fun getMes( fecha: Calendar): String{
        var mes = (fecha.get( Calendar.MONTH ) + 1).toString()
        if( mes.toInt() < 10){ mes = "0$mes" }
        return mes
    }

    fun getYear( fecha: Calendar): String{
        return fecha.get( Calendar.YEAR ).toString()
    }
}