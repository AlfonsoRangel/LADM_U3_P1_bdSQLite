package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.*
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.botonAgregar
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.botonDescargar
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.botonEliminar
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.botonGuardar
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.botonRegresar
import kotlinx.android.synthetic.main.activity_pantalla_vehiculo.txtTitulo
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

class PantallaVehiculo : AppCompatActivity() {

    var anios = ArrayList<String>()
    var tipos = ArrayList<String>()
    var marcas = ArrayList<String>()
    var vehiculo = Vehiculo( "" , "" , "" , 0 , "" , 0f )
    var cliente = Cliente("" , "" , "")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_vehiculo)

        //=============================================
        //  ESTABLECER DATOS INICIALES
        //=============================================
        val year = Fecha(Calendar.getInstance()).getYear().toInt()
        (1990..year).forEach {
            anios.add( 0 , it.toString())
        }
        tipos.add("Manual")
        tipos.add("Automatico")

        marcas.add("Nissan")
        marcas.add("Toyota")
        marcas.add("Chevrolet")
        marcas.add("Audi")
        marcas.add("KIA")
        marcas.add("BMW")
        marcas.add("Ford")
        marcas.add("Volkswagen")

        comboMarca.adapter = ArrayAdapter<String>( this , R.layout.item_combo , marcas)
        comboAnio.adapter = ArrayAdapter<String>( this , R.layout.item_combo , anios)
        comboTipo.adapter = ArrayAdapter<String>( this , R.layout.item_combo , tipos)

        // EVALUAR SI ES NUEVO REGISTRO O MOSTRAR DATOS DE UN VEHICULO
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

            txtTitulo.setText( "Nuevo Vehiculo" )
        }
        else {
            try {
                val p1 = botonAgregar.layoutParams as LinearLayout.LayoutParams
                p1.width = 0
                p1.height = 0
                botonAgregar.layoutParams = p1

                txtTitulo.setText( "Datos del Vehiculo" )

                vehiculo.placa = intent.extras!!.getString("placa")!!
                vehiculo.modelo = intent.extras!!.getString("modelo")!!
                vehiculo.costoXdia = intent.extras!!.getFloat("costoXdia")!!
                vehiculo.marca = intent.extras!!.getString("marca")!!
                vehiculo.anio = intent.extras!!.getInt("anio")!!
                vehiculo.tipo = intent.extras!!.getString("tipo")!!
                vehiculo.id = intent.extras!!.getInt("id")!!
                vehiculo.idCliente = intent.extras!!.getInt("idCliente")!!

                campoPlaca.setText( vehiculo.placa )
                campoModelo.setText( vehiculo.modelo )
                campoCosto.setText( vehiculo.costoXdia.toString() )
                comboMarca.setSelection(marcas.indexOf(vehiculo.marca) )
                comboAnio.setSelection(anios.indexOf(vehiculo.anio.toString()) )
                comboTipo.setSelection(tipos.indexOf(vehiculo.tipo) )

                if( vehiculo.idCliente != 0 ) {
                    cliente = ControladorCliente(this).filtrarUnCliente( vehiculo.idCliente )
                    if( cliente != null ) { addCliente() }
                    else{ cliente = Cliente("","","") }
                }
            } catch( er: Exception){ ToastPersonalizado(this , er.message!!).show() }
        }


        //=============================================
        //  EVENTOS
        //=============================================
        botonRegresar.setOnClickListener {
            val lanzar = Intent(this@PantallaVehiculo , PantallaVerVehiculos::class.java)
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
                val insertado = ControladorVehiculo( this ).insertar(
                    Vehiculo(
                        campoPlaca.text.toString() ,
                        campoModelo.text.toString(),
                        marcas.get( comboMarca.selectedItemPosition ) ,
                        anios.get( comboAnio.selectedItemPosition ).toInt() ,
                        tipos.get( comboTipo.selectedItemPosition ) ,
                        campoCosto.text.toString().toFloat()
                    )
                )
                if(insertado)
                {
                    ToastPersonalizado(this,"Vehiculo Agregado").show()
                    campoPlaca.setText( "" )
                    campoModelo.setText( "" )
                    campoCosto.setText( "" )
                    comboMarca.setSelection( 0 )
                    comboAnio.setSelection( 0 )
                    comboTipo.setSelection( 0 )
                }
                else{ ToastPersonalizado(this,"Algo salio mal. Intentelo de Nuevo").show() }
            }
        }


        // BOTON ELIMINAR
        botonEliminar.setOnClickListener {
            if( cliente.id != 0 ){
                ToastPersonalizado(this,"No es posible eliminar este Vehiculo debido a que esta " +
                        "siendo rentado por un cliente").show()
            }
            else {
                val eliminado = ControladorVehiculo(this).eliminar(vehiculo.id)
                if (eliminado) {
                    ToastPersonalizado(this,"Vehiculo Eliminado").show()
                    val lanzar = Intent(this@PantallaVehiculo, PantallaVerVehiculos::class.java)
                    startActivity(lanzar)
                    finish()
                } else {
                    ToastPersonalizado(this,"Algo salio mal. Intentelo de Nuevo").show()
                }
            }
        }


        // BOTON GUARDAR CAMBIOS
        botonGuardar.setOnClickListener {
            if( validarCampos() ){
                vehiculo.placa = campoPlaca.text.toString()
                vehiculo.modelo = campoModelo.text.toString()
                vehiculo.marca = marcas.get( comboMarca.selectedItemPosition )
                vehiculo.anio = anios.get( comboAnio.selectedItemPosition ).toInt()
                vehiculo.tipo = tipos.get( comboTipo.selectedItemPosition )
                vehiculo.costoXdia = campoCosto.text.toString().toFloat()
                ControladorVehiculo( this ).guardarCambios( vehiculo )
                ToastPersonalizado(this,"Vehiculo Actualizado").show()
            }
        }


        //========================================
        // BOTONES DE AYUDA
        //========================================
        ayuda_campoPlaca.setOnClickListener {
            ToastPersonalizado( this , "Placa del Vehiculo" ).show()
        }
        ayuda_campoModelo.setOnClickListener {
            ToastPersonalizado( this , "Modelo del Vehiculo" ).show()
        }
        ayuda_comboMarca.setOnClickListener {
            ToastPersonalizado( this , "Marca del Vehiculo" ).show()
        }
        ayuda_comboAnio.setOnClickListener {
            ToastPersonalizado( this , "Año en el cual se compro el Vehiculo" ).show()
        }
        ayuda_comboTipo.setOnClickListener {
            ToastPersonalizado( this , "Tipo de Vehiculo" ).show()
        }
        ayuda_campoCosto.setOnClickListener {
            ToastPersonalizado( this , "Costo por dia que se rente el Vehiculo" ).show()
        }

        // MENSAJE INICIAL DE PANTALLA
        if( cliente.id != 0 ) {
            AlertDialog.Builder(this)
                .setTitle("Atencion !!!")
                .setMessage("Puedes dar clic en el cliente que esta rentando el vehiculo para ver la informacion de la renta")
                .setPositiveButton("OK") { view, d ->
                    view.dismiss()
                }
                .show()
        }
    }





    fun guardarCSV( rutaCarpeta: String ) {
        try
        {
            var nombreArchivo = "Vehiculo (" +
                    Fecha(Calendar.getInstance()).getFechaCompleta().replace(":","") + ")"

            // DATOS DEL VEHICULO
            var contenido = "ID Vehiculo,Placa,Marca,Modelo,Year,Tipo,Costo Por Dia\n" +
                    vehiculo.id + "," + vehiculo.placa + "," + vehiculo.marca + "," + vehiculo.modelo + "," +
                    vehiculo.anio.toString() + "," + vehiculo.tipo + "," + vehiculo.costoXdia.toString() + "\n\n\n"

            // DATOS DEL CLIENTE
            contenido =  contenido + "ID Cliente,Nombre,Numero de Licencia,Telefono\n"
            if( cliente.id != 0 ) {
                contenido = contenido + cliente.id + "," + cliente.nombre + "," + cliente.numero_licencia + "," +
                        cliente.obtenerTelefono()
            }
            val file = File( rutaCarpeta , "$nombreArchivo.csv")
            val fichero = OutputStreamWriter( FileOutputStream( file ) )
            fichero.write( contenido )
            fichero.flush()
            fichero.close()
            AlertDialog.Builder(this)
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
        val txtPlaca = campoPlaca.text.toString()
        val txtModelo = campoModelo.text.toString()
        val txtCosto = campoCosto.text.toString()

        if(txtPlaca.isEmpty()){
            ToastPersonalizado(this, "Introduzca la Placa del Vehiculo").show()
            return false
        }
        if(txtModelo.isEmpty()){
            ToastPersonalizado(this, "Introduzca el Modelo del Vehiculo").show()
            return false
        }
        if(txtCosto.isEmpty()){
            ToastPersonalizado(this, "Introduzca el Costo Por Dia Rentado del Vehiculo").show()
            return false
        }
        try {
            val costo = txtCosto.toFloat()
            if( costo <= 0 ) {
                ToastPersonalizado(this, "El Costo Por Dia Rentado NO debe ser Negativo o Cero").show()
                return false
            }
        } catch( e: Exception ){
            ToastPersonalizado(this, "El Costo Por Dia Rentado NO es NUMERO").show()
            return false
        }
        return true
    }


    fun addCliente( )
    {
        // LINEAR LAYOUT PRINCIPAL
        val contenedorMain = LinearLayout(this)
        contenedorCliente.addView(contenedorMain)

        val pm1 = contenedorMain.layoutParams as LinearLayout.LayoutParams
        pm1.width = LinearLayout.LayoutParams.MATCH_PARENT
        pm1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        pm1.bottomMargin = 12
        contenedorMain.layoutParams = pm1

        contenedorMain.orientation = LinearLayout.VERTICAL
        contenedorMain.setPadding( 3 , 4 , 0 , 4 )
        contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )

        // LINEAR LAYOUT CLIENTE
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
        // IMAGE VIEW CLIENTE
        //=================================================
        val imagenCliente = ImageView( this )
        contenedorV.addView( imagenCliente )

        val p2 = imagenCliente.layoutParams as LinearLayout.LayoutParams
        p2.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p2.height = LinearLayout.LayoutParams.MATCH_PARENT
        p2.gravity = Gravity.CENTER_VERTICAL
        imagenCliente.layoutParams = p2

        imagenCliente.setImageResource( R.drawable.icono_cliente )
        imagenCliente.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeOscuro ) )
        imagenCliente.setColorFilter( ContextCompat.getColor( this , R.color.verdeLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )


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
        // TEXT VIEW ( NOMBRE )
        //=================================================
        val txtNombre = TextView( this )
        contenedorTexto.addView( txtNombre )

        val p4 = txtNombre.layoutParams as LinearLayout.LayoutParams
        p4.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.gravity = Gravity.CENTER
        txtNombre.layoutParams = p4

        txtNombre.setPadding( 3 , 0 , 3 , 3 )
        txtNombre.setText( cliente.nombre )
        txtNombre.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtNombre.setTypeface(Typeface.DEFAULT_BOLD)
        txtNombre.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )


        //=================================================
        // TEXT VIEW (TELEFONO)
        //=================================================
        val txtTel = TextView( this )
        contenedorTexto.addView( txtTel )

        val p5 = txtTel.layoutParams as LinearLayout.LayoutParams
        p5.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p5.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p5.gravity = Gravity.CENTER
        txtTel.layoutParams = p5

        txtTel.setPadding( 3 , 0 , 3 , 0 )
        txtTel.setText( cliente.obtenerTelefono() )
        txtTel.setTextSize( TypedValue.COMPLEX_UNIT_SP , 20f )
        txtTel.setTypeface(Typeface.DEFAULT_BOLD)
        txtTel.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

        //=================================================
        // TEXT VIEW (NUMERO DE LICENCIA)
        //=================================================
        val txtLicencia = TextView( this )
        contenedorTexto.addView( txtLicencia )

        val p6 = txtLicencia.layoutParams as LinearLayout.LayoutParams
        p6.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.gravity = Gravity.CENTER
        txtLicencia.layoutParams = p6

        txtLicencia.setPadding( 3 , 0 , 3 , 0 )
        txtLicencia.setText( cliente.numero_licencia )
        txtLicencia.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtLicencia.setTypeface(Typeface.DEFAULT_BOLD)
        txtLicencia.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

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
                contenedorCliente.removeView( contenedorMain )
                cliente = Cliente("","","")
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
            contenedorCliente.removeView( contenedorMain )
            cliente = Cliente("","","")
        }
    }
}