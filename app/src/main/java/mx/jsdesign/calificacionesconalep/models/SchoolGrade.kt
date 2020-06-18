package mx.jsdesign.calificacionesconalep.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolGrade(
    val claveUnidadAdministrativa: String = "",
    val nombreUnidadAdministrativa: String = "",
    val matriculaAlumno: String = "",
    val clavePlanEstudio: String = "",
    val periodoEscolar: String = "",
    val calendario: String = "",
    val claveModulo: String = "",
    val nombreModulo: String = "",
    val grado: String = "",
    val calificacionFinal: String = "",
    val porcentajeAlcanzado: String = "",
    val nombrePlanEstudio: String = "",
    val nombreAlumno: String = "",
    val indicadoresModulo: String = "",
    val indicadoresEvaluadosModulo: String = "",
    val nombrePSP: String = ""
)