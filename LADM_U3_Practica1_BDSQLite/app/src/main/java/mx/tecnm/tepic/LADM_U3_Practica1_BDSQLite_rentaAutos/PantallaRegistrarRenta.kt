package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.activity_pantalla_registrar_renta.*
import kotlinx.android.synthetic.main.activity_pantalla_registrar_renta.botonDescargar
import kotlinx.android.synthetic.main.activity_pantalla_registrar_renta.botonRegresar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

class PantallaRegistrarRenta : AppCompatActivity() , DatePickerDialog.OnDateSetListener {

    val CONTENEDOR_VEHICULO = 1
    val CONTENEDOR_CLIENTE = 2
    val CONTENEDOR_DETALLES = 3
    var contenedorActual = 1

    var ultimoClienteSeleccionado: LinearLayout? = null
    var ultimoVehiculoSeleccionado: LinearLayout? = null
    var actualClienteSeleccionado: LinearLayout? = null
    var actualVehiculoSeleccionado: LinearLayout? = null

    var vehiculoSeleccionado: Vehiculo? = null
    var clienteSeleccionado: Cliente? = null

    var fechaActual: Calendar? = null
    var fechaEntregaSeleccionada: Calendar? = null
    var fechaDevolucionSeleccionada: Calendar? = null
    val FECHA_ENTREGA = 1
    val FECHA_DEVOLUCION = 2
    var selectorFecha = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_registrar_renta)

        //=============================================
        //  ESTABLECER DATOS INICIALES
        //=============================================
        mostrarFiltro()

        contenedorRClientes.removeAllViews()
        val listaClientes = ControladorCliente( this ).filtrarTodo()
        listaClientes.forEach {
            addCliente( it )
        }

        contenedorRVehiculos.removeAllViews()
        val listaVehiculos = ControladorVehiculo( this ).filtrarVehiculosSinCliente()
        listaVehiculos.forEach {
            addVehiculo( it )
        }


        //=============================================
        //  EVENTOS
        //=============================================
        botonRegresar.setOnClickListener {
            finish()
        }

        botonPanelVehiculo.setOnClickListener {
            contenedorActual = CONTENEDOR_VEHICULO
            mostrarFiltro()
        }

        botonPanelCliente.setOnClickListener {
            contenedorActual = CONTENEDOR_CLIENTE
            mostrarFiltro()
        }

        botonPanelDetalles.setOnClickListener {
            contenedorActual = CONTENEDOR_DETALLES
            mostrarFiltro()
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
                //val ruta = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath + "/LADM_U3_P1_SQLite"
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


        botonBuscarV.setOnClickListener {
            contenedorRVehiculos.removeAllViews()
            ultimoVehiculoSeleccionado = null
            actualVehiculoSeleccionado = null
            if( campoBuscarV.text.isEmpty() ) {
                // DEVOLVER todoo
                val listaVehiculos = ControladorVehiculo( this ).filtrarVehiculosSinCliente()
                listaVehiculos.forEach {
                    addVehiculo( it )
                }
                ToastPersonalizado(this, "Busqueda Realizada").show()
            }
            else {
                // DEVOLVER COINCIDENCIAS
                val listaVehiculos = ControladorVehiculo( this ).filtrarPorBusquedaSinCliente( campoBuscarV.text.toString() )
                listaVehiculos.forEach {
                    addVehiculo( it )
                }
                ToastPersonalizado(this, "Busqueda Realizada").show()
            }
        }


        botonBuscarC.setOnClickListener {
            contenedorRClientes.removeAllViews()
            ultimoClienteSeleccionado = null
            actualClienteSeleccionado = null
            if( campoBuscarC.text.isEmpty() ) {
                // DEVOLVER todoo
                val listaClientes = ControladorCliente( this ).filtrarTodo()
                listaClientes.forEach {
                    addCliente( it )
                }
                ToastPersonalizado(this, "Busqueda Realizada").show()
            }
            else {
                // DEVOLVER COINCIDENCIAS
                val listaClientes = ControladorCliente( this ).filtrarPorBusqueda( campoBuscarC.text.toString() )
                listaClientes.forEach {
                    addCliente( it )
                }
                ToastPersonalizado(this, "Busqueda Realizada").show()
            }
        }


        botonFechaEntrega.setOnClickListener {
            fechaActual = Calendar.getInstance()
            selectorFecha = FECHA_ENTREGA
            DatePickerDialog( this , this ,
                Fecha(fechaActual!!).getYear().toInt() ,
                Fecha(fechaActual!!).getMes().toInt()-1 ,
                Fecha(fechaActual!!).getDia().toInt()
            ).show()
        }


        botonFechaDevolucion.setOnClickListener {
            fechaActual = Calendar.getInstance()
            selectorFecha = FECHA_DEVOLUCION
            DatePickerDialog( this , this ,
                Fecha(fechaActual!!).getYear().toInt() ,
                Fecha(fechaActual!!).getMes().toInt()-1 ,
                Fecha(fechaActual!!).getDia().toInt()
            ).show()
        }


        botonRegistrar.setOnClickListener {
            if( validarCampos() )
            {
                val renta = Renta(
                    vehiculoSeleccionado!!.id ,
                    Fecha(fechaEntregaSeleccionada!!).getFechaSQL() ,
                    Fecha(fechaDevolucionSeleccionada!!).getFechaSQL() ,
                    calcularCosto().toFloat() ,
                    "ACTIVO"
                )
                if( ControladorRenta(this).insertar( renta ) ){
                    ControladorVehiculo(this).actualizarCliente( vehiculoSeleccionado!!.id , clienteSeleccionado!!.id )
                    ToastPersonalizado(this,"Renta de Vehiculo Guardado con Exito").show()
                    finish()
                }
                else{
                    ToastPersonalizado(this,"Algo salio mal. Vuelve a Intentarlo").show()
                }
            }
        }


        //========================================
        // BOTONES DE AYUDA
        //========================================
        ayuda_campoBuscarV.setOnClickListener {
            ToastPersonalizado( this , "Marca o Modelo del Vehiculo a Rentar\n\n" +
                    "(Solamente aplica para Vehiculos que NO se estan Rentando)" ).show()
        }
        ayuda_campoBuscarC.setOnClickListener {
            ToastPersonalizado( this , "Nombre o Apellido del Cliente que va a Rentar un Vehiculo" ).show()
        }
        ayuda_txtFechaEntrega.setOnClickListener {
            ToastPersonalizado( this , "Que Dia se entregara el Vehiculo al Cliente?\n\n" +
                    "(Elige dando clic en el boton calendario)" ).show()
        }
        ayuda_txtFechaDevolucion.setOnClickListener {
            ToastPersonalizado( this , "Cual es el ultimo Dia del periodo de la Renta del Vehiculo?\n\n" +
                    "(Elige dando clic en el boton calendario)" ).show()
        }
        ayuda_txtCosto.setOnClickListener {
            ToastPersonalizado( this , "Costo total de la Renta\n\n" +
                    "(Se calcula automaticamente en base al periodo de la renta y el precio del Vehiculo)" ).show()
        }

    }





    fun guardarCSV( rutaCarpeta: String ) {
        try
        {
            var nombreArchivo = "Historial de Rentas (" +
                    Fecha(Calendar.getInstance()).getFechaCompleta().replace(":","") + ")"

            // VEHICULO
            var contenido = "ID Renta,ID Vehiculo,Fecha de Entrega,Fecha de Devolucion,Costo,Estatus\n"
            val listaRentas = ControladorRenta(this).filtrarTodo()
            if( listaRentas.size != 0 ) {
                listaRentas.forEach {
                    contenido = contenido + it.id + "," + it.idVehiculo + "," +
                            Fecha(Calendar.getInstance()).convertirFechaSQL(it.fechaEntrega!!) + "," +
                            Fecha(Calendar.getInstance()).convertirFechaSQL(it.fechaDevolucion!!) + "," +
                            it.costo.toString() + "," + it.estatus + "\n"
                }
                val file = File(rutaCarpeta, "$nombreArchivo.csv")
                val fichero = OutputStreamWriter(FileOutputStream(file))
                fichero.write(contenido)
                fichero.flush()
                fichero.close()
                val ventana = AlertDialog.Builder(this)
                    .setTitle("Archivo CSV generado !!!")
                    .setMessage("Nombre del Archivo:\n $nombreArchivo\n\nUbicado en la Tarjeta SD dentro de la carpeta\n\nLADM_U3_P1_SQLite")
                    .setPositiveButton("OK") { view, d ->
                        view.dismiss()
                    }
                    .show()
            }
            else {
                ToastPersonalizado(this, "No existe ninguna Renta almacenada").show()
            }
        }
        catch (error2: Exception) { ToastPersonalizado( this , error2.message!! ).show() }
    }


    fun validarCampos() : Boolean {
        if( actualVehiculoSeleccionado == null ){
            ToastPersonalizado(this,"Debes Seleccionar el Vehiculo a Rentar").show()
            return false
        }
        if( actualClienteSeleccionado == null ){
            ToastPersonalizado(this,"Debes Seleccionar el Cliente que va a Rentar un Vehiculo").show()
            return false
        }
        if( fechaEntregaSeleccionada == null ){
            ToastPersonalizado(this,"Debes Seleccionar la Fecha de Entrega").show()
            return false
        }
        if( fechaDevolucionSeleccionada == null ){
            ToastPersonalizado(this,"Debes Seleccionar la Fecha de Devolucion").show()
            return false
        }
        if( fechaEntregaSeleccionada!!.compareTo(fechaDevolucionSeleccionada!!) >= 0 ) {
            ToastPersonalizado(this,"La Fecha de Entrega Debe ser MENOR que la Fecha de Devolucion").show()
            return false
        }
        return true
    }


    fun calcularCosto() : Double {
        if( fechaEntregaSeleccionada != null && fechaDevolucionSeleccionada != null && vehiculoSeleccionado != null )
        {
            if( fechaEntregaSeleccionada!!.compareTo(fechaDevolucionSeleccionada!!) < 0 )
            {
                 val dif = fechaDevolucionSeleccionada!!.timeInMillis - fechaEntregaSeleccionada!!.timeInMillis
                val dias = Math.floor( dif.toDouble() / (1000 * 60 * 60 * 24 ).toDouble() )
                val costo = dias * vehiculoSeleccionado!!.costoXdia
                return costo
            }
            return 0.0
        }
        return 0.0
    }


    override fun onDateSet(v: DatePicker?, year: Int, mes: Int, dia: Int) {
        if( selectorFecha == FECHA_ENTREGA ) {
            fechaEntregaSeleccionada = Calendar.getInstance()
            fechaEntregaSeleccionada!!.set( year , mes , dia )
            txtFechaEntrega.setText( Fecha(fechaEntregaSeleccionada!!).getFecha() )
            txtCosto.setText( calcularCosto().toString() )
        }
        else {
            fechaDevolucionSeleccionada = Calendar.getInstance()
            fechaDevolucionSeleccionada!!.set( year , mes , dia )
            txtFechaDevolucion.setText( Fecha(fechaDevolucionSeleccionada!!).getFecha() )
            txtCosto.setText( calcularCosto().toString() )
        }
    }


    fun mostrarFiltro()
    {
        when(contenedorActual) {
            CONTENEDOR_VEHICULO -> {
                val p1 = contenedorVehiculo.layoutParams as LinearLayout.LayoutParams
                p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
                p1.topMargin = 18
                p1.bottomMargin = 12
                contenedorVehiculo.layoutParams = p1
                botonPanelVehiculo.setColorFilter(
                    ContextCompat.getColor( this , R.color.verdeNormal ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p2 = contenedorCliente.layoutParams as LinearLayout.LayoutParams
                p2.height = 0
                p2.topMargin = 0
                p2.bottomMargin = 0
                contenedorCliente.layoutParams = p2
                botonPanelCliente.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p3 = contenedorDetalles.layoutParams as LinearLayout.LayoutParams
                p3.height = 0
                p3.topMargin = 0
                p3.bottomMargin = 0
                contenedorDetalles.layoutParams = p3
                botonPanelDetalles.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            CONTENEDOR_CLIENTE -> {
                val p1 = contenedorCliente.layoutParams as LinearLayout.LayoutParams
                p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
                p1.topMargin = 18
                p1.bottomMargin = 12
                contenedorCliente.layoutParams = p1
                botonPanelCliente.setColorFilter(
                    ContextCompat.getColor( this , R.color.verdeNormal ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p2 = contenedorVehiculo.layoutParams as LinearLayout.LayoutParams
                p2.height = 0
                p2.topMargin = 0
                p2.bottomMargin = 0
                contenedorVehiculo.layoutParams = p2
                botonPanelVehiculo.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p3 = contenedorDetalles.layoutParams as LinearLayout.LayoutParams
                p3.height = 0
                p3.topMargin = 0
                p3.bottomMargin = 0
                contenedorDetalles.layoutParams = p3
                botonPanelDetalles.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            CONTENEDOR_DETALLES -> {
                val p1 = contenedorDetalles.layoutParams as LinearLayout.LayoutParams
                p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
                p1.topMargin = 18
                p1.bottomMargin = 12
                contenedorDetalles.layoutParams = p1
                botonPanelDetalles.setColorFilter(
                    ContextCompat.getColor( this , R.color.verdeNormal ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p2 = contenedorCliente.layoutParams as LinearLayout.LayoutParams
                p2.height = 0
                p2.topMargin = 0
                p2.bottomMargin = 0
                contenedorCliente.layoutParams = p2
                botonPanelCliente.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val p3 = contenedorVehiculo.layoutParams as LinearLayout.LayoutParams
                p3.height = 0
                p3.topMargin = 0
                p3.bottomMargin = 0
                contenedorVehiculo.layoutParams = p3
                botonPanelVehiculo.setColorFilter(
                    ContextCompat.getColor( this , R.color.rojoOscuro ) ,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }

    }



    fun addCliente( cliente : Cliente )
    {
        // LINEAR LAYOUT PRINCIPAL
        val contenedorMain = LinearLayout(this)
        contenedorRClientes.addView(contenedorMain)

        val p1 = contenedorMain.layoutParams as LinearLayout.LayoutParams
        p1.width = LinearLayout.LayoutParams.MATCH_PARENT
        p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p1.bottomMargin = 12
        contenedorMain.layoutParams = p1

        contenedorMain.orientation = LinearLayout.HORIZONTAL
        contenedorMain.setPadding( 3 , 4 , 0 , 4 )
        contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )

        contenedorMain.setOnClickListener {
            ultimoClienteSeleccionado = actualClienteSeleccionado
            actualClienteSeleccionado = contenedorMain
            clienteSeleccionado = cliente
            if( ultimoClienteSeleccionado != null ){
                ultimoClienteSeleccionado!!.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )
            }
            contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeLigero ) )
            contenedorActual = CONTENEDOR_DETALLES
            mostrarFiltro()
        }

        //=================================================
        // IMAGE VIEW CLIENTE
        //=================================================
        val imagenCliente = ImageView( this )
        contenedorMain.addView( imagenCliente )

        val p2 = imagenCliente.layoutParams as LinearLayout.LayoutParams
        p2.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p2.height = LinearLayout.LayoutParams.MATCH_PARENT
        p2.gravity = Gravity.CENTER_VERTICAL
        imagenCliente.layoutParams = p2

        imagenCliente.setImageResource( R.drawable.icono_cliente )
        if( cliente.tieneVehiculosEnRenta )
        {
            imagenCliente.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeOscuro ) )
            imagenCliente.setColorFilter( ContextCompat.getColor( this , R.color.verdeLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )
        }
        else{
            imagenCliente.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )
            imagenCliente.setColorFilter( ContextCompat.getColor( this , R.color.rojoLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )
        }

        //=================================================
        // LINEAR LAYOUT INFORMACION
        //=================================================
        val contenedorTexto = LinearLayout(this)
        contenedorMain.addView(contenedorTexto)

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
        // TEXT VIEW TELEFONO
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
        // TEXT VIEW NUMERO DE LICENCIA
        //=================================================
        val txtLicencia = TextView( this )
        contenedorTexto.addView( txtLicencia )

        val p6 = txtLicencia.layoutParams as LinearLayout.LayoutParams
        p6.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p6.gravity = Gravity.CENTER
        txtLicencia.layoutParams = p6

        txtLicencia.setPadding( 3 , 0 , 3 , 0 )
        txtLicencia.setText( cliente.numero_licencia)
        txtLicencia.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtLicencia.setTypeface(Typeface.DEFAULT_BOLD)
        txtLicencia.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )
    }


    fun addVehiculo( vehiculo : Vehiculo)
    {
        // LINEAR LAYOUT PRINCIPAL
        val contenedorMain = LinearLayout(this)
        contenedorRVehiculos.addView(contenedorMain)

        val p1 = contenedorMain.layoutParams as LinearLayout.LayoutParams
        p1.width = LinearLayout.LayoutParams.MATCH_PARENT
        p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p1.bottomMargin = 12
        contenedorMain.layoutParams = p1

        contenedorMain.orientation = LinearLayout.HORIZONTAL
        contenedorMain.setPadding( 3 , 4 , 0 , 4 )
        contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )

        contenedorMain.setOnClickListener {
            ultimoVehiculoSeleccionado = actualVehiculoSeleccionado
            actualVehiculoSeleccionado = contenedorMain
            vehiculoSeleccionado = vehiculo
            if( ultimoVehiculoSeleccionado != null ){
                ultimoVehiculoSeleccionado!!.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )
            }
            contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeLigero ) )
            contenedorActual = CONTENEDOR_CLIENTE
            txtCosto.setText( calcularCosto().toString() )
            mostrarFiltro()
        }

        //=================================================
        // IMAGE VIEW CARRO
        //=================================================
        val imagenCarro = ImageView( this )
        contenedorMain.addView( imagenCarro )

        val p2 = imagenCarro.layoutParams as LinearLayout.LayoutParams
        p2.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p2.height = LinearLayout.LayoutParams.MATCH_PARENT
        p2.gravity = Gravity.CENTER_VERTICAL
        imagenCarro.layoutParams = p2

        imagenCarro.setImageResource( R.drawable.icono_carro )
        imagenCarro.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )
        imagenCarro.setColorFilter( ContextCompat.getColor( this , R.color.rojoLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )


        //=================================================
        // LINEAR LAYOUT INFORMACION
        //=================================================
        val contenedorTexto = LinearLayout(this)
        contenedorMain.addView(contenedorTexto)

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
    }

}