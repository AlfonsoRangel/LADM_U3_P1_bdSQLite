package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.toast.*
import kotlinx.android.synthetic.main.toast.view.*

class ToastPersonalizado(activity: AppCompatActivity , mensaje: String ) {
    val mensaje = mensaje
    val a = activity

    fun show(){
        try {
            val layout = a.layoutInflater.inflate( R.layout.toast , a.contenedorToast )
            layout.txtToast.setText(mensaje)
            Toast(a).apply {
                duration = Toast.LENGTH_LONG
                setGravity(Gravity.BOTTOM, 0, 18)
                view = layout
            }.show()
        }catch(e:Exception){ Toast.makeText( a , e.message , Toast.LENGTH_LONG).show() }
    }
}