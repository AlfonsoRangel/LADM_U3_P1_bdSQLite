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
import kotlinx.android.synthetic.main.activity_pantalla_ver_clientes.*
import kotlinx.android.synthetic.main.activity_pantalla_ver_clientes.botonAgregar
import kotlinx.android.synthetic.main.activity_pantalla_ver_clientes.botonRegresar
import kotlinx.android.synthetic.main.toast.*
import kotlinx.android.synthetic.main.toast.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

class PantallaVerClientes : AppCompatActivity() {

    var verFiltro = false
    var tipos = ArrayList<String>()
    val RESULTADOS_POR_BUSQUEDA = 1
    val RESULTADOS_CLIENTES_NO_RENTANDO = 2
    val RESULTADOS_CLIENTES_CON_PERIODO_VIGENTE = 3
    val RESULTADOS_CLIENTES_CON_PERIODO_VENCIDO = 4
    var tipoDeBusqueda = RESULTADOS_POR_BUSQUEDA
    var listaClientes = ArrayList<Cliente>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_ver_clientes)

        //=============================================
        //  ESTABLECER DATOS INICIALES
        //=============================================
        tipos.add("Clientes que NO estan Rentando")
        tipos.add("Clientes con Periodo de Renta Vigente")
        tipos.add("Clientes con Periodo de Renta Vencidos")
        comboBuscar.adapter = ArrayAdapter<String>( this , R.layout.item_combo , tipos)

        mostrarFiltro()
        contenedorResultado.removeAllViews()
        listaClientes = ControladorCliente( this ).filtrarTodo()
        listaClientes.forEach {
            addCliente( it )
        }


        //=============================================
        //  EVENTOS
        //=============================================
        // BOTON REGRESAR
        botonRegresar.setOnClickListener {
            //val lanzar = Intent(this@PantallaVerClientes , MainActivity::class.java)
            //startActivity(lanzar)
            finish()
        }


        botonFiltrar.setOnClickListener {
            verFiltro = !verFiltro
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


        botonAgregar.setOnClickListener {
            val lanzar = Intent(this@PantallaVerClientes , PantallaCliente::class.java)
            lanzar.putExtra( "esNuevoRegistro" , true )
            startActivity(lanzar)
            finish()
        }


        // FILTRAR POR BUSQUEDA
        botonBuscar.setOnClickListener {
            contenedorResultado.removeAllViews()
            tipoDeBusqueda = RESULTADOS_POR_BUSQUEDA
            if( campoBuscar.text.isEmpty() ) {
                // DEVOLVER todoo
                listaClientes = ControladorCliente( this ).filtrarTodo()
                listaClientes.forEach {
                    addCliente( it )
                }
                verFiltro = !verFiltro
                mostrarFiltro()
            }
            else {
                // DEVOLVER COINCIDENCIAS
                listaClientes = ControladorCliente( this ).filtrarPorBusqueda( campoBuscar.text.toString() )
                listaClientes.forEach {
                    addCliente( it )
                }
                verFiltro = !verFiltro
                mostrarFiltro()
            }
        }


        botonComboBuscar.setOnClickListener {
            contenedorResultado.removeAllViews()
            when( comboBuscar.selectedItemPosition ) {
                0 -> {
                    //Clientes que NO estan Rentando
                    tipoDeBusqueda = RESULTADOS_CLIENTES_NO_RENTANDO
                    listaClientes = ControladorCliente(this).filtrarClientesInactivos()
                    listaClientes.forEach {
                        addCliente( it )
                    }
                    verFiltro = !verFiltro
                    mostrarFiltro()
                }
                1 -> {
                    //Clientes con Periodo de Renta Vigente
                    tipoDeBusqueda = RESULTADOS_CLIENTES_CON_PERIODO_VIGENTE
                    listaClientes = ControladorCliente(this).filtrarClientesActivos()
                    listaClientes.forEach {
                        addCliente( it )
                    }
                    verFiltro = !verFiltro
                    mostrarFiltro()
                }
                2 -> {
                    //Clientes con Periodo de Renta Vencidos
                    tipoDeBusqueda = RESULTADOS_CLIENTES_CON_PERIODO_VENCIDO
                    listaClientes = ControladorCliente(this).filtrarClientesVencidos()
                    listaClientes.forEach {
                        addCliente( it )
                    }
                    verFiltro = !verFiltro
                    mostrarFiltro()
                }
            }
        }


        //========================================
        // BOTONES DE AYUDA
        //========================================
        ayuda_campoBuscar.setOnClickListener {
            ToastPersonalizado( this , "Nombre o Apellido del Cliente a Buscar" ).show()
        }
        ayuda_comboBuscar.setOnClickListener {
            ToastPersonalizado( this , "Que Tipo de Clientes desea Buscar?" ).show()
        }
    }







    fun guardarCSV( rutaCarpeta: String ) {
        try
        {
            var nombreArchivo = ""
            when( tipoDeBusqueda )
            {
                RESULTADOS_POR_BUSQUEDA -> { nombreArchivo = "Lista de Clientes por Busqueda"}
                RESULTADOS_CLIENTES_NO_RENTANDO -> { nombreArchivo = "Lista de Clientes NO rentando" }
                RESULTADOS_CLIENTES_CON_PERIODO_VIGENTE -> { nombreArchivo = "Lista de Clientes con Periodo Vigente" }
                RESULTADOS_CLIENTES_CON_PERIODO_VENCIDO -> { nombreArchivo = "Lista de Clientes con Periodo Vencido" }
            }
            nombreArchivo = nombreArchivo + " (" +
                    Fecha(Calendar.getInstance()).getFechaCompleta().replace(":","") + ")"

            // OBTENER CLIENTES
            var contenido =  "ID,Nombre,Numero de Licencia,Telefono" + "\n"
            listaClientes.forEach {
                contenido = contenido + it.id + "," + it.nombre + "," + it.numero_licencia + "," + it.obtenerTelefono() + "\n"
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





    fun mostrarFiltro()
    {
        if(verFiltro){
            val p1 = contenedorFiltro.layoutParams as LinearLayout.LayoutParams
            p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
            p1.topMargin = 20
            contenedorFiltro.layoutParams = p1

            val p2 = contenedorResultado.layoutParams as LinearLayout.LayoutParams
            p2.height = 0
            contenedorResultado.layoutParams = p2

            txtTitulo.setText( "Filtro de Clientes" )
            txtTitulo.setOnClickListener {
                ToastPersonalizado( this ,"Establecer Filtro de Clientes" ).show()
            }


            botonFiltrar.setColorFilter(
                ContextCompat.getColor( this , R.color.verdeLigero ) ,
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        else{
            val p1 = contenedorFiltro.layoutParams as LinearLayout.LayoutParams
            p1.height = 0
            p1.topMargin = 0
            contenedorFiltro.layoutParams = p1

            val p2 = contenedorResultado.layoutParams as LinearLayout.LayoutParams
            p2.height = LinearLayout.LayoutParams.WRAP_CONTENT
            contenedorResultado.layoutParams = p2

            txtTitulo.setText( "Lista de Clientes" )
            txtTitulo.setOnClickListener {
                ToastPersonalizado( this ,"Lista de Clientes Filtrados" ).show()
            }

            botonFiltrar.setColorFilter(
                ContextCompat.getColor( this , R.color.rojoLigero ) ,
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }



    fun addCliente( cliente : Cliente )
    {
        // LINEAR LAYOUT PRINCIPAL
        val contenedorMain = LinearLayout(this)
        contenedorResultado.addView(contenedorMain)

        val p1 = contenedorMain.layoutParams as LinearLayout.LayoutParams
        p1.width = LinearLayout.LayoutParams.MATCH_PARENT
        p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p1.bottomMargin = 12
        contenedorMain.layoutParams = p1

        contenedorMain.orientation = LinearLayout.HORIZONTAL
        contenedorMain.setPadding( 3 , 4 , 0 , 4 )
        contenedorMain.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoLigero ) )

        contenedorMain.setOnClickListener {
            val lanzar = Intent(this@PantallaVerClientes , PantallaCliente::class.java)
            lanzar.putExtra( "esNuevoRegistro" , false )
            lanzar.putExtra( "nombre" , cliente.nombre )
            lanzar.putExtra( "telefono" , cliente.telefono )
            lanzar.putExtra( "numeroLicencia" , cliente.numero_licencia )
            lanzar.putExtra( "id" , cliente.id )
            startActivity(lanzar)
            finish()
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
        val txtDatos = TextView( this )
        contenedorTexto.addView( txtDatos )

        val p4 = txtDatos.layoutParams as LinearLayout.LayoutParams
        p4.width = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.height = LinearLayout.LayoutParams.WRAP_CONTENT
        p4.gravity = Gravity.CENTER
        txtDatos.layoutParams = p4

        txtDatos.setPadding( 3 , 0 , 3 , 3 )
        txtDatos.setText( cliente.nombre )
        txtDatos.setTextSize( TypedValue.COMPLEX_UNIT_SP , 17f )
        txtDatos.setTypeface(Typeface.DEFAULT_BOLD)
        txtDatos.setTextColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )

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

    }
}