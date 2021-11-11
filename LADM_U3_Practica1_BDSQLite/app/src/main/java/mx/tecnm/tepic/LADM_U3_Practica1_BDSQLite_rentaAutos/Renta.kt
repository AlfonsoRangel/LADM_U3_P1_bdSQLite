package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

class Renta( idVehiculo: Int? , fechaEntrega: String? , fechaDevolucion: String? , costo: Float? , estatus: String? )
{
    var id = 0
    var idVehiculo = idVehiculo
    var fechaEntrega = fechaEntrega
    var fechaDevolucion = fechaDevolucion
    var costo = costo
    var estatus = estatus
}