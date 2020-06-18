package mx.jsdesign.calificacionesconalep.models

import kotlinx.serialization.Serializable

@Serializable
data class WsResponse(
    var message: String? = "",
    var success: Boolean = false,
    var schoolGrades: List<SchoolGrade>? = emptyList()

)