package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if( ActivityCompat.checkSelfPermission( this ,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) , 1)
        }




        itemVerVehiculos.setOnClickListener {
            val lanzar = Intent(this@MainActivity , PantallaVerVehiculos::class.java)
            startActivity(lanzar)
        }

        itemVerClientes.setOnClickListener {
            val lanzar = Intent(this@MainActivity , PantallaVerClientes::class.java)
            startActivity(lanzar)
        }

        itemRegistrarRenta.setOnClickListener {
            val lanzar = Intent(this@MainActivity , PantallaRegistrarRenta::class.java)
            startActivity(lanzar)
        }


        // MENSAJE INICIAL DE PANTALLA
        AlertDialog.Builder(this)
            .setTitle( "App Renta de Autos" )
            .setMessage("A traves de esta App, un encargado de un negocio que se dedica a rentar autos" +
                    " puede:\n\n" +
                    "1) llevar el control de los vehiculos del negocio (añadir, editar, eliminar o buscar alguno en especifico" +
                    " o por años de uso) accediendo a la primera opcion del menu\n\n" +
                    "2) llevar el control de los clientes del negocio (añadir, editar, eliminar o buscar alguno en especifico" +
                    " o segun su situacion de rentas de vehiculos) accediendo a la segunda opcion del menu\n\n" +
                    "3) Registrar una nueva renta de vehiculo (se debera elegir el auto, el cliente y las fechas del " +
                    "periodo de la renta) accediendo a la tercera opcion del menu")
            .setPositiveButton("OK"){ view , d ->
                view.dismiss()
            }
            .show()
    }
}