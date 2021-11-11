package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.ContentValues
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ControladorCliente( activity: AppCompatActivity) {
    val activity = activity

    fun insertar( cliente: Cliente): Boolean {
        val tablaCliente = BD(activity, "Cliente", null, 1).writableDatabase
        val datos = ContentValues()
        datos.put("NOMBRE", cliente.nombre)
        datos.put("TELEFONO", cliente.telefono)
        datos.put("NOLICENCIA", cliente.numero_licencia)

        if( tablaCliente.insert("Cliente",null , datos) == -1L){ return false }
        return true
    }


    fun filtrarClientesInactivos( ): ArrayList<Cliente>
    {
        val resultado = ArrayList<Cliente>()
        val listaClientesActivos = ControladorVehiculo(activity).getIDsClientesRentando()
        //Toast.makeText(activity , listaClientesActivos.size.toString() , Toast.LENGTH_LONG).show()
        if( listaClientesActivos.size == 0 ) { return filtrarTodo() }

        // EXCLUIR CLIENTES ACTIVOS
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        try {
            val valores = convertirListaAString(listaClientesActivos)
            val SQL = "SELECT * FROM Cliente WHERE IDCLIENTE NOT IN (${valores})"
            val cursor = tablaCliente.rawQuery(SQL, null)
            if(cursor.moveToFirst()){
                //si se posiciona en un primer renglon, SI se obtuvo resultado
                do {
                    //leer la data
                    var cliente = Cliente(
                        cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                        cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                        cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
                    )
                    cliente.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                    resultado.add(cliente)
                }while(cursor.moveToNext())
            }
            else{
                //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
            }
        }
        catch(e: Exception){ Toast.makeText(activity , e.message , Toast.LENGTH_LONG).show() }
        return resultado
    }

    fun filtrarClientesActivos( ): ArrayList<Cliente>
    {
        val resultado = ArrayList<Cliente>()
        val listaVehiculos = ControladorRenta(activity).getIDsVehiculosActivos()
        //Toast.makeText(activity , listaVehiculos.size.toString() , Toast.LENGTH_LONG).show()
        if( listaVehiculos.size == 0 ) { return resultado }

        val listaClientes = ControladorVehiculo(activity).filtrarIDsClientes(listaVehiculos)
        if( listaClientes.size == 0 ) { return resultado }

        // OBTENER LA INFO DE LOS CLIENTES DE LA LISTA
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        val valores = convertirListaAString(listaClientes)
        val SQL = "SELECT * FROM Cliente WHERE IDCLIENTE IN(${valores})"
        val cursor = tablaCliente.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var cliente = Cliente(
                    cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
                )
                cliente.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                cliente.tieneVehiculosEnRenta = true
                resultado.add(cliente)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }

    fun filtrarClientesVencidos( ): ArrayList<Cliente>
    {
        val resultado = ArrayList<Cliente>()
        val listaVehiculos = ControladorRenta(activity).getIDsVehiculosVencidos()
        if( listaVehiculos.size == 0 ) { return resultado }

        val listaClientes = ControladorVehiculo(activity).filtrarIDsClientes(listaVehiculos)
        if( listaClientes.size == 0 ) { return resultado }

        // OBTENER LA INFO DE LOS CLIENTES DE LA LISTA
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        val SQL = "SELECT * FROM Cliente WHERE IDCLIENTE IN(?)"
        val cursor = tablaCliente.rawQuery( SQL , arrayOf( convertirListaAString(listaClientes) ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var cliente = Cliente(
                    cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
                )
                cliente.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                cliente.tieneVehiculosEnRenta = true
                resultado.add(cliente)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }




    fun filtrarPorBusqueda( subcadena : String ): ArrayList<Cliente>
    {
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        val resultado = ArrayList<Cliente>()
        val SQL = "SELECT * FROM Cliente WHERE NOMBRE LIKE ?"
        val cursor = tablaCliente.rawQuery( SQL , arrayOf( "%$subcadena%" ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var cliente = Cliente(
                    cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
                )
                cliente.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                val listaV = ControladorVehiculo(activity).getVehiculosDeUnCliente(cliente.id)
                if( listaV.size != 0 ){ cliente.tieneVehiculosEnRenta = true }
                resultado.add(cliente)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun filtrarUnCliente( idCliente: Int ): Cliente
    {
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        val SQL = "SELECT * FROM Cliente WHERE IDCLIENTE = ?"
        var resultado: Cliente? = null
        val cursor = tablaCliente.rawQuery( SQL , arrayOf( idCliente.toString() ) )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            resultado = Cliente(
                cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
            )
            resultado.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
            return resultado
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado!!
    }


    fun filtrarTodo( ): ArrayList<Cliente>
    {
        val tablaCliente = BD( activity ,"Cliente",null,1).readableDatabase
        val resultado = ArrayList<Cliente>()
        val SQL = "SELECT * FROM Cliente"
        val cursor = tablaCliente.rawQuery( SQL , null )
        if(cursor.moveToFirst()){
            //si se posiciona en un primer renglon, SI se obtuvo resultado
            do {
                //leer la data
                var cliente = Cliente(
                    cursor.getString( cursor.getColumnIndex( "NOMBRE" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "NOLICENCIA" ) ) ,
                    cursor.getString( cursor.getColumnIndex( "TELEFONO" ) )
                )
                cliente.id = cursor.getInt( cursor.getColumnIndex( "IDCLIENTE" ) )
                val listaV = ControladorVehiculo(activity).getVehiculosDeUnCliente(cliente.id)
                if( listaV.size != 0 ){ cliente.tieneVehiculosEnRenta = true }
                resultado.add(cliente)
            }while(cursor.moveToNext())
        }else{
            //si moveToFirst regresa falso, entra al ELSE que significa que no hubo ningun resultado
        }
        return resultado
    }


    fun eliminar (id : Int) : Boolean
    {
        val tablaCliente = BD( activity ,"Cliente",null,1).writableDatabase
        val resultado = tablaCliente.delete("Cliente","IDCLIENTE=?", arrayOf(id.toString()))
        if (resultado == 0)  return false
        return true
    }


    fun guardarCambios ( cliente : Cliente)
    {
        val tablaCliente = BD( activity ,"Cliente",null,1).writableDatabase
        val SQL = "UPDATE Cliente SET NOMBRE = ? , TELEFONO = ? , NOLICENCIA = ? WHERE IDCLIENTE = ?"
        tablaCliente.execSQL( SQL , arrayOf(
            cliente.nombre ,
            cliente.telefono ,
            cliente.numero_licencia ,
            cliente.id
        ))
    }



    fun convertirListaAString( lista: ArrayList<Int>) : String {
        var cad = ""
        lista.forEach {
            cad = cad + it.toString() + ","
        }
        //Toast.makeText(activity , cad.substring( 0 , cad.length - 1 ) , Toast.LENGTH_LONG).show()
        return cad.substring( 0 , cad.length - 1 )
    }
}