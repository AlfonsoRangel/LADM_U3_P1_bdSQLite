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
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.*
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.ayuda_campoBuscar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.botonAgregar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.botonBuscar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.botonDescargar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.botonFiltrar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.botonRegresar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.campoBuscar
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.contenedorFiltro
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.contenedorResultado
import kotlinx.android.synthetic.main.activity_pantalla_ver_vehiculos.txtTitulo
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList


class PantallaVerVehiculos : AppCompatActivity() {

    var aniosDeUsoI = ArrayList<String>()
    var aniosDeUsoF = ArrayList<String>()
    var verFiltro = false
    var listaVehiculos = ArrayList<Vehiculo>()

    val RESULTADOS_POR_BUSQUEDA = 1
    val RESULTADOS_POR_ANIOS = 2
    var tipoDeResultado = RESULTADOS_POR_BUSQUEDA



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_ver_vehiculos)

        //=============================================
        //  ESTABLECER DATOS INICIALES
        //=============================================
        val anio = Fecha(Calendar.getInstance()).getYear().toInt()
        (1990..(anio-1)).forEach {
            aniosDeUsoI.add( 0 , ( anio - it).toString() )
        }
        (1991..anio).forEach {
            aniosDeUsoF.add( 0 , ( anio - it).toString() )
        }
        aniosDeUsoI.add( 0 , "Mas Reciente" )
        aniosDeUsoF.add( 0 , "Mas Viejo" )
        comboAnioInicial.adapter = ArrayAdapter<String>( this , R.layout.item_combo , aniosDeUsoI)
        comboAnioFinal.adapter = ArrayAdapter<String>( this , R.layout.item_combo , aniosDeUsoF)

        mostrarFiltro()
        contenedorResultado.removeAllViews()
        listaVehiculos = ControladorVehiculo( this ).filtrarTodo()
        listaVehiculos.forEach {
            addVehiculo( it )
        }


        //=============================================
        //  EVENTOS
        //=============================================
        botonRegresar.setOnClickListener {
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
            val lanzar = Intent(this@PantallaVerVehiculos , PantallaVehiculo::class.java)
            lanzar.putExtra( "esNuevoRegistro" , true )
            startActivity(lanzar)
            finish()
        }


        botonBuscar.setOnClickListener {
            contenedorResultado.removeAllViews()
            if( campoBuscar.text.isEmpty() ) {
                // DEVOLVER todoo
                listaVehiculos = ControladorVehiculo( this ).filtrarTodo()
                listaVehiculos.forEach {
                    addVehiculo( it )
                }
                tipoDeResultado = RESULTADOS_POR_BUSQUEDA
                verFiltro = !verFiltro
                mostrarFiltro()
                ToastPersonalizado(this,"Busqueda Realizada").show()
            }
            else {
                // DEVOLVER COINCIDENCIAS
                listaVehiculos = ControladorVehiculo( this ).filtrarPorBusqueda( campoBuscar.text.toString() )
                listaVehiculos.forEach {
                    addVehiculo( it )
                }
                tipoDeResultado = RESULTADOS_POR_BUSQUEDA
                verFiltro = !verFiltro
                mostrarFiltro()
                ToastPersonalizado(this,"Busqueda Realizada").show()
            }
        }


        botonBuscarPorAnios.setOnClickListener {
            var anioI = 0
            if( comboAnioInicial.selectedItemPosition != 0 ){ anioI = aniosDeUsoI.get( comboAnioInicial.selectedItemPosition ).toInt() }
            var anioF = aniosDeUsoF.get( aniosDeUsoF.size - 1 ).toInt() - 1
            if( comboAnioFinal.selectedItemPosition != 0 ){ anioF = aniosDeUsoF.get( comboAnioFinal.selectedItemPosition ).toInt() }

            if( anioI > anioF ) {
                ToastPersonalizado( this , "La Cantidad de Años de Uso Minimo DEBE Ser MENOR a la Cantidad de Años de Uso Maximo").show()
            }
            else {
                // DEVOLVER COINCIDENCIAS
                contenedorResultado.removeAllViews()
                listaVehiculos = ControladorVehiculo( this ).filtrarPorAniosDeUso( anioI , anioF )
                listaVehiculos.forEach {
                    addVehiculo( it )
                }
                verFiltro = !verFiltro
                mostrarFiltro()
                tipoDeResultado = RESULTADOS_POR_ANIOS
            }
        }


        //========================================
        // BOTONES DE AYUDA
        //========================================
        ayuda_campoBuscar.setOnClickListener {
            ToastPersonalizado( this , "Marca o Modelo del Vehiculo a Buscar").show()
        }
        ayuda_comboAnioI.setOnClickListener {
            ToastPersonalizado( this , "Cuantos Años de Uso Minimo desea que tenga el Vehiculo?").show()
        }
        ayuda_comboAnioF.setOnClickListener {
            ToastPersonalizado( this , "Cuantos Años de Uso Maximo desea que tenga el Vehiculo?").show()
        }

    }





    fun guardarCSV( rutaCarpeta: String ) {
        try
        {
            var nombreArchivo = ""
            when( tipoDeResultado )
            {
                RESULTADOS_POR_BUSQUEDA -> { nombreArchivo = "Lista de Vehiculos por Busqueda"}
                RESULTADOS_POR_ANIOS -> { nombreArchivo = "Lista de Vehiculos por Anios de Uso" }
            }
            nombreArchivo = nombreArchivo + " (" +
                    Fecha(Calendar.getInstance()).getFechaCompleta().replace(":","") + ")"

            // OBTENER VEHICULOS
            if( listaVehiculos.size != 0 ) {
                var contenido =
                    "ID Vehiculo,Placa,Marca,Modelo,Year,Tipo,Costo por Dia,ID Cliente" + "\n"
                listaVehiculos.forEach {
                    contenido =
                        contenido + it.id + "," + it.placa + "," + it.marca + "," + it.modelo + "," +
                                it.anio.toString() + "," + it.tipo + "," + it.costoXdia.toString() + "," + it.idCliente + "\n"
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
                ToastPersonalizado(this,"Lista de Vehiculos Vacia. Cambie el filtro de la busqueda o " +
                        "registre un Nuevo Vehiculo").show()
            }
        }
        catch (error2: Exception) { ToastPersonalizado( this , error2.message!! ).show() }
    }


    fun mostrarFiltro()
    {
        if(verFiltro){
            val p1 = contenedorFiltro.layoutParams as LinearLayout.LayoutParams
            p1.height = LinearLayout.LayoutParams.WRAP_CONTENT
            p1.topMargin = 30
            contenedorFiltro.layoutParams = p1

            val p2 = contenedorResultado.layoutParams as LinearLayout.LayoutParams
            p2.height = 0
            contenedorResultado.layoutParams = p2

            txtTitulo.setText( "Filtro de Vehiculos" )
            txtTitulo.setOnClickListener {
                ToastPersonalizado(this , "Establecer Filtro de Vehiculos" ).show()
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

            txtTitulo.setText( "Lista de Vehiculos" )
            txtTitulo.setOnClickListener {
                ToastPersonalizado(this , "Lista de Vehiculos Filtrados" ).show()
            }

            botonFiltrar.setColorFilter(
                ContextCompat.getColor( this , R.color.rojoLigero ) ,
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }


    fun addVehiculo( vehiculo : Vehiculo )
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
            val lanzar = Intent(this@PantallaVerVehiculos , PantallaVehiculo::class.java)
            lanzar.putExtra( "esNuevoRegistro" , false )
            lanzar.putExtra( "marca" , vehiculo.marca )
            lanzar.putExtra( "modelo" , vehiculo.modelo )
            lanzar.putExtra( "placa" , vehiculo.placa )
            lanzar.putExtra( "anio" , vehiculo.anio )
            lanzar.putExtra( "tipo" , vehiculo.tipo )
            lanzar.putExtra( "costoXdia" , vehiculo.costoXdia )
            lanzar.putExtra( "id" , vehiculo.id )
            lanzar.putExtra( "idCliente" , vehiculo.idCliente )
            startActivity(lanzar)
            finish()
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
        if( vehiculo.idCliente != 0 )
        {
            imagenCarro.setBackgroundColor( ContextCompat.getColor( this , R.color.verdeOscuro ) )
            imagenCarro.setColorFilter( ContextCompat.getColor( this , R.color.verdeLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )
        }
        else{
            imagenCarro.setBackgroundColor( ContextCompat.getColor( this , R.color.rojoOscuro ) )
            imagenCarro.setColorFilter( ContextCompat.getColor( this , R.color.rojoLigero ) , android.graphics.PorterDuff.Mode.SRC_IN )
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

    }
}