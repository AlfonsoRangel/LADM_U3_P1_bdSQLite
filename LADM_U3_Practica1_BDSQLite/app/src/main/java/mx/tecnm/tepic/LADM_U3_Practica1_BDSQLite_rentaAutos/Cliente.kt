package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

class Cliente( nombre: String , numero_licencia: String , telefono: String ) {
    var nombre = nombre
    var numero_licencia = numero_licencia
    var telefono = telefono
    var id = 0
    var tieneVehiculosEnRenta = false

    fun obtenerTelefono(): String {
        return telefono.substring(0,3) + "-" + telefono.substring(3,6) + "-" + telefono.substring(6,telefono.length)
    }
}