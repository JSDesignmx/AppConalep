package mx.jsdesign.calificacionesconalep

import android.annotation.SuppressLint
import android.content.Intent
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import mx.jsdesign.calificacionesconalep.models.SchoolGrade

@UnstableDefault
fun Intent.putSchoolGradeData(data: List<SchoolGrade>) {
    putExtra(
        SchoolGrade::class.java.simpleName,
        Json.stringify(SchoolGrade.serializer().list, data)
    )
}

@UnstableDefault
fun Intent.getSchoolGradeData(): List<SchoolGrade>? {
    val stringExtra = getStringExtra(SchoolGrade::class.java.simpleName)
    return if (stringExtra != null) Json.parse(SchoolGrade.serializer().list, stringExtra) else null
}

fun formatName(name: String?): String{
    if(name == null || name.isEmpty() || !name.contains("*"))
        return ""
    val array = name.split("*")

    if(array.size != 2) return name

    return "${array[1].trim()} ${array[0].trim()}".capitalizeWords()
}

@SuppressLint("DefaultLocale")
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }