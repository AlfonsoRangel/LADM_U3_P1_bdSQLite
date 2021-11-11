package mx.tecnm.tepic.LADM_U3_Practica1_BDSQLite_rentaAutos

class Vehiculo( placa: String , modelo: String , marca: String , anio: Int , tipo: String , costoXdia: Float )
{
    var placa = placa
    var modelo = modelo
    var marca = marca
    var anio = anio
    var tipo = tipo
    var costoXdia = costoXdia
    var id = 0
    var idCliente = 0

    fun getDescripcion() : String {
        return "$marca $modelo ($anio)"
    }
}