package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_pantalla_cliente.*
import kotlinx.android.synthetic.main.activity_pantalla_cliente.botonAgregar
import kotlinx.android.synthetic.main.activity_pantalla_cliente.botonRegresar
import kotlinx.android.synthetic.main.activity_pantalla_cliente.txtTitulo
import kotlinx.android.synthetic.main.toast.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

class PantallaCliente : AppCompatActivity() {

    var cliente = Cliente( "" , "" , "" )
    var listaVehiculos = ArrayList<Vehiculo>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_cliente)

        //=============================================
        //  ESTABLECER DATOS INICIALES
        //=============================================
        if( intent.extras!!.getBoolean("esNuevoRegistro") ) {
            val p1 = botonGuardar.layoutParams as LinearLayout.LayoutParams
            p1.width = 0
            p1.height = 0
            botonGuardar.layoutParams = p1

            val p2 = botonEliminar.layoutParams as LinearLayout.LayoutParams
            p2.width = 0
            p2.height = 0
            botonEliminar.layoutParams = p2

            val p3 = botonDescargar.layoutParams as LinearLayout.LayoutParams
            p3.width = 0
            p3.height = 0
            p3.setMargins(0,0,0,0)
            botonDescargar.layoutParams = p3

            txtTitulo.setText( "Nuevo Cliente" )
        }
        else {
            try {
                val p1 = botonAgregar.layoutParams as LinearLayout.LayoutParams
                p1.width = 0
                p1.height = 0
                botonAgregar.layoutParams = p1

                txtTitulo.setText( "Datos del Cliente" )

                cliente.nombre = intent.extras!!.getString("nombre")!!
                cliente.telefono = intent.extras!!.getString("telefono")!!
                cliente.numero_licencia = intent.extras!!.getString("numeroLicencia")!!
                cliente.id = intent.extras!!.getInt("id")!!

                campoNombre.setText( cliente.nombre)
                campoTelefono.setText( cliente.telefono )
                campoNoLicencia.setText( cliente.numero_licencia )

                // BUSCAR VEHICULOS QUE ESTE RENTANDO
                listaVehiculos = ControladorVehiculo(this).getVehiculosDeUnCliente( cliente.id )
                listaVehiculos.forEach{
                    addVehiculo( it )
                }
            } catch( er: Exception){ ToastPersonalizado(this,er.message!!).show() }
        }


        //=============================================
        //  EVENTOS
        //=============================================
        botonRegresar.setOnClickListener {
            val lanzar = Intent(this@PantallaCliente , PantallaVerClientes::class.java)
            startActivity(lanzar)
            finish()
        }

        botonDescargar.setOnClickListener {
            if( ActivityCompat.checkSelfPermission( this ,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
            {
                ToastPersonalizado( this , "Debes Aceptar el Permiso de Escritura en la " +
                        "Tarjeta SD para poder descargar el archivo de Clientes" ).show()
            }
            else {
                val rutaSD = Environment.getExternalStorageDirectory().absolutePath
                val rutaCarpeta = rutaSD + "/LADM_U3_P1_SQLite"
                val carpeta = File(rutaSD, "LADM_U3_P1_SQLite")
                var carpetaCreada = false
                if (!carpeta.exists()) {
                    // SI NO EXISTE
                    carpetaCreada = carpeta.mkdirs()
                    if (carpetaCreada) {
                        guardarCSV(rutaCarpeta)
                    } else {
                        ToastPersonalizado( this , "NO SE PUDO CREAR LA CARPETA \nEs posible que la app NO tenga permiso").show()
                    }
                } else {
                    // SI YA ESTA CREADA LA CARPETA
                    guardarCSV(rutaCarpeta)
                }
            }
        }


        // BOTON AGREGAR
        botonAgregar.setOnClickListener {
            if( validarCampos() ){
                val insertado = ControladorCliente( this ).insertar(
                    Cliente(
                        campoNombre.text.toString() ,
                        campoNoLicencia.text.toString(),
                        campoTelefono.text.toString()
                    )
                )
                if(insertado)
                {
                    ToastPersonalizado(this,"Cliente Agregado").show()
                    campoNombre.setText( "" )
                    campoNoLicencia.setText( "" )
                    campoTelefono.setText( "" )
                }
                else{ ToastPersonalizado(this,"Algo salio mal. Intentelo de Nuevo").show() }
            }
        }


        // BOTON ELIMINAR
        botonEliminar.setOnClickListener {
            if( listaVehiculos.size != 0 ){
                ToastPersonalizado(this,"No es posible eliminar este Cliente debido a que tiene " +
                        "vehiculos en renta").show()
            }
            else {
                val eliminado = ControladorCliente(this).eliminar(cliente.id)
                if (eliminado) {
                    ToastPersonalizado(this,"Cliente Eliminado").show()
                    val lanzar = Intent(this@PantallaCliente, PantallaVerClientes::class.java)
                    startActivity(lanzar)
                    finish()
                } else {
                    ToastPersonalizado(this,"Algo salio mal. Intentelo de nuevo").show()
                }
            }
        }


        // BOTON GUARDAR CAMBIOS
        botonGuardar.setOnClickListener {
            if( validarCampos() ){
                cliente.nombre = campoNombre.text.toString()
                cliente.numero_licencia = campoNoLicencia.text.toString()
                cliente.telefono = campoTelefono.text.toString()
                ControladorCliente( this ).guardarCambios( cliente )
                ToastPersonalizado(this,"Cliente Actualizado").show()
            }
        }


        //========================================
        // BOTONES DE AYUDA
        //========================================
        ayuda_campoNombre.setOnClickListener {
            ToastPersonalizado( this , "Nombre Completo del Cliente" ).show()
        }
        ayuda_campoNoLicencia.setOnClickListener {
            ToastPersonalizado( this , "Numero de Licencia del Cliente" ).show()
        }
        ayuda_campoTel.setOnClickListener {
            ToastPersonalizado( this , "Telefono del Cliente" ).show()
        }

        // MENSAJE INICIAL DE PANTALLA
        if(listaVehiculos.size != 0) {
            AlertDialog.Builder(this)
                .setTitle("Atencion !!!")
                .setMessage("Puedes dar clic en un vehiculo que esta siendo rentado por el cliente para ver la informacion de la renta")
                .setPositiveButton("OK") { view, d ->
                    view.dismiss()
                }
                .show()
        }
    }





    fun guardarCSV( rutaCarpeta: String ) {
        try
        {
            var nombreArchivo = "Cliente (" +
                    Fecha(Calendar.getInstance()).getFechaCompleta().replace(":","") + ")"

            // DATOS DEL CLIENTE
            var contenido =  "ID Cliente,Nombre,Numero de Licencia,Telefono\n" +
                    cliente.id + "," + cliente.nombre + "," + cliente.numero_licencia + "," + cliente.obtenerTelefono() + "\n\n\n" +
                    "ID Vehiculo,Placa,Marca,Modelo,Year,Tipo,Costo Por Dia\n"

            listaVehiculos.forEach {
                contenido = contenido + it.id + "," + it.placa + "," + it.marca + "," + it.modelo + "," + it.anio.toString() +
                "," + it.tipo + "," + it.costoXdia.toString() + "\n"
            }
            val file = File( rutaCarpeta , "$nombreArchivo.csv")
            val fichero = OutputStreamWriter( FileOutputStream( file ) )
            fichero.write( contenido )
            fichero.flush()
            fichero.close()
            val ventana = AlertDialog.Builder(this)
                .setTitle( "Archivo CSV generado !!!" )
                .setMessage("Nombre del Archivo:\n $nombreArchivo\n\nUbicado en la Tarjeta SD dentro de la carpeta\n\nLADM_U3_P1_SQLite")
                .setPositiveButton("OK"){ view , d ->
                    view.dismiss()
                }
                .show()
        }
        catch (error2: Exception) { ToastPersonalizado( this , error2.message!! ).show() }
    }


    fun validarCampos(): Boolean {
        val txtNombre = campoNombre.text.toString()
        val txtNoLicencia = campoNoLicencia.text.toString()
        val txtTel = campoTelefono.text.toString()

        if(txtNombre.isEmpty()){
            ToastPersonalizado(this,"Introduzca el Nombre del Cliente").show()
            return false
        }
        if(txtNoLicencia.isEmpty()){
            ToastPersonalizado(this,"Introduzca el Numero de Licencia del Cliente").show()
            return false
        }
        if(txtTel.isEmpty()){
            ToastPersonalizado(this,"Introduzca el Telefono del Cliente").show()
            return false
        }
        try {
            val t = txtTel.toLong()
            if( txtTel.length != 10 ) {
                ToastPersonalizado(this,"El Telefono debe tener 10 DIGITOS").show()
                return false
            }
        } catch( e: Exception ){
            ToastPersonalizado(this,"El Telefono debe tener Solamente DIGITOS").show()
            return false
        }
        return true
    }


    fun addVehiculo( vehiculo : Vehiculo )
    {
        // LINEAR LAYOUT PRINCIPAL
        val contenedorMain = LinearLayout(this)
        contenedorVehiculos.addView(contenedorMain)

        val pm1 = contenedorMain.layoutParams as LinearLayout.LayoutParams
        pm1.width = LinearLayout.LayoutParams.MATCH_PARENT
        pm1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        pm1.bottomMargin = 12
        contenedorMain.layoutParams = pm1

        contenedorMain.orientation = LinearLayout.VERTICAL
        contenedorMain.setPadding( 3 , 4 , 0 , 4 )
        contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )

        // LINEAR LAYOUT VEHICULO
        val contenedorV = LinearLayout(this)
        contenedorMain.addView(contenedorV)

        val p1 = contenedorV.layoutParams as LinearLayout.LayoutParams
        p1.width = LinearLayout.LayoutParams.MATCH_PARENT
        p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p1.bottomMargin = 1
        contenedorV.layoutParams = p1
        contenedorV.orientation = LinearLayout.HORIZONTAL

        contenedorV.setOnClickListener {
            // MOSTRAR DATOS DE LA RENTA
            val renta = ControladorRenta(this).filtrarUnaRenta( vehiculo.id )
            val fechaE = Fecha(Calendar.getInstance()).convertirFechaSQL(renta.fechaEntrega!!)
            val fechaD = Fecha(Calendar.getInstance()).convertirFechaSQL(renta.fechaDevolucion!!)
            ToastPersonalizado(this,
                "Fecha de Entrega:\n${fechaE}\n\n" +
                        "Fecha de Devolucion:\n${fechaD}\n\n" +
                        "Costo de la Renta:\n$ ${renta.costo.toString()}"
            ).show()
        }


        //=================================================
        // IMAGE VIEW CARRO
        //=================================================
        val imagenCarro = ImageView( this )
        contenedorV.addView( imagenCarro )

        val p2 = imagenCarro.layoutParams as LinearLayout.LayoutParams
        p2.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p2.height = LinearLayout.LayoutParams.MATCH_PARENT
        p2.gravity = Gravity.CENTER_VERTICAL
        imagenCarro.layoutParams = p2

        imagenCarro.setImageResource( R.drawable.icono_carro )
        imagenCarro.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeOscuro ) )
        imagenCarro.setColorFilter( ContextCompat.getColor( this , R.color.verdeLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )


        //=================================================
        // LINEAR LAYOUT INFORMACION
        //=================================================
        val contenedorTexto = LinearLayout(this)
        contenedorV.addView(contenedorTexto)

        val p3 = contenedorTexto.layoutParams as LinearLayout.LayoutParams
        p3.width = 1
        p3.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p3.weight = 2f
        contenedorTexto.layoutParams = p3
        contenedorTexto.orientation = LinearLayout.VERTICAL

        //=================================================
        // TEXT VIEW ( MARCA MODELO (AÑO) )
        //=================================================
        val txtDatos = TextView( this )
        contenedorTexto.addView( txtDatos )

        val p4 = txtDatos.layoutParams as LinearLayout.LayoutParams
        p4.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.gravity = Gravity.CENTER
        txtDatos.layoutParams = p4

        txtDatos.setPadding( 3 , 0 , 3 , 3 )
        txtDatos.setText( vehiculo.getDescripcion() )
        txtDatos.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtDatos.setTypeface(Typeface.DEFAULT_BOLD)
        txtDatos.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        //=================================================
        // TEXT VIEW PLACA
        //=================================================
        val txtPlaca = TextView( this )
        contenedorTexto.addView( txtPlaca )

        val p5 = txtPlaca.layoutParams as LinearLayout.LayoutParams
        p5.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p5.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p5.gravity = Gravity.CENTER
        txtPlaca.layoutParams = p5

        txtPlaca.setPadding( 3 , 0 , 3 , 0 )
        txtPlaca.setText( vehiculo.placa )
        txtPlaca.setTextSize( TypedValue.COMPLEX_UNIT_SP , 20f )
        txtPlaca.setTypeface(Typeface.DEFAULT_BOLD)
        txtPlaca.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        //=================================================
        // TEXT VIEW TIPO
        //=================================================
        val txtTipo = TextView( this )
        contenedorTexto.addView( txtTipo )

        val p6 = txtTipo.layoutParams as LinearLayout.LayoutParams
        p6.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.gravity = Gravity.CENTER
        txtTipo.layoutParams = p6

        txtTipo.setPadding( 3 , 0 , 3 , 0 )
        txtTipo.setText( vehiculo.tipo )
        txtTipo.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtTipo.setTypeface(Typeface.DEFAULT_BOLD)
        txtTipo.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        // LINEAR LAYOUT BOTONES
        val contenedorB = LinearLayout(this)
        contenedorMain.addView(contenedorB)

        val p7 = contenedorB.layoutParams as LinearLayout.LayoutParams
        p7.width = LinearLayout.LayoutParams.MATCH_PARENT
        p7.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p7.bottomMargin = 1
        p7.rightMargin = 4
        p7.leftMargin = 4
        p7.topMargin = 10
        contenedorB.layoutParams = p7
        contenedorB.orientation = LinearLayout.HORIZONTAL

        //=================================================
        // BOTON ELIMINAR
        //=================================================
        val botonE = TextView( this )
        contenedorB.addView( botonE )

        val p8 = botonE.layoutParams as LinearLayout.LayoutParams
        p8.width = 1
        p8.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p8.weight = 2f
        p8.rightMargin = 6
        botonE.layoutParams = p8

        botonE.setPadding( 12 , 10 , 12 , 10 )
        botonE.setText( "Cancelar Renta" )
        botonE.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        botonE.setTypeface(Typeface.DEFAULT_BOLD)
        botonE.gravity = Gravity.CENTER
        botonE.setTextColor( ContextCompat.getColor( this , R.color.rojoLigero ) )
        botonE.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        botonE.setOnClickListener {
            if( ControladorRenta(this).eliminar( vehiculo.id ) ){
                ToastPersonalizado(this,"La Renta de este Vehiculo se ha Eliminado").show()
                contenedorVehiculos.removeView( contenedorMain )
                listaVehiculos.remove(vehiculo)
            }
            else {
                ToastPersonalizado(this,"Algo salio mal. Vuelva a Intentarlo").show()
            }
        }

        //=================================================
        // BOTON CONFIRMAR
        //=================================================
        val botonOK = TextView( this )
        contenedorB.addView( botonOK )

        val p9 = botonOK.layoutParams as LinearLayout.LayoutParams
        p9.width = 1
        p9.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p9.weight = 2f
        botonOK.layoutParams = p9

        botonOK.setPadding( 12 , 10 , 12 , 10 )
        botonOK.setText( "Finalizar Renta" )
        botonOK.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        botonOK.setTypeface(Typeface.DEFAULT_BOLD)
        botonOK.gravity = Gravity.CENTER
        botonOK.setTextColor( ContextCompat.getColor( this , R.color.rojoLigero ) )
        botonOK.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        botonOK.setOnClickListener {
            ControladorRenta(this).finalizar( vehiculo.id )
            ToastPersonalizado(this,"La Renta de este Vehiculo se ha Finalizado y quedara almacenado en la BD").show()
            contenedorVehiculos.removeView( contenedorMain )
            listaVehiculos.remove(vehiculo)
        }
    }

}