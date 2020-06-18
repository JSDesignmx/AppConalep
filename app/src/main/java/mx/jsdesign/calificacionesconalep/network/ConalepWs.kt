package mx.jsdesign.calificacionesconalep.network

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import mx.jsdesign.calificacionesconalep.BuildConfig
import mx.jsdesign.calificacionesconalep.JsApp
import mx.jsdesign.calificacionesconalep.R
import mx.jsdesign.calificacionesconalep.models.SchoolGrade
import mx.jsdesign.calificacionesconalep.models.WsResponse
import org.ksoap2.SoapEnvelope
import org.ksoap2.SoapFault
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class ConalepWs {

    companion object {

        private suspend fun postSoap(
            soapObject: SoapObject,
            soapAction: String,
            context: Context
        ): String =

            withContext(Dispatchers.IO) {

                var result: String

                soapObject.addProperty(
                    context.getString(R.string.uuid),
                    JsApp.remoteConfig[context.getString(R.string.uuid)].asString()
                )

                val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                envelope.setOutputSoapObject(soapObject)
                envelope.dotNet = false

                val httpTransportSE = HttpTransportSE(JsApp.remoteConfig[context.getString(R.string.url)].asString())

                try {
                    httpTransportSE.debug = BuildConfig.DEBUG
                    httpTransportSE.call(soapAction, envelope)
                    result = envelope.response.toString()
                } catch (sp: SoapFault) {
                    FirebaseCrashlytics.getInstance().recordException(sp)
                    result = sp.faultstring

                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    result = e.message ?: context.getString(R.string.unknown_error)
                }

                result
            }

        @UnstableDefault
        suspend fun getSchoolGrades(id: String, context: Context): WsResponse =

            withContext(Dispatchers.Default) {

                val wsResponse = WsResponse()
                var success = false

                val soapObject = SoapObject(JsApp.remoteConfig[context.getString(R.string.soap_namespace)].asString(), JsApp.remoteConfig[context.getString(
                                    R.string.get_grades_method)].asString()).apply {
                    addProperty(context.getString(R.string.studentIdparam), id)
                }

                var result = postSoap(soapObject, JsApp.remoteConfig[context.getString(R.string.soap_action_get_grades)].asString(), context)

                val emptyResponse = context.getString(R.string.empty_response)
                if (result == emptyResponse  || !result.startsWith("[{")) {
                    if (result == emptyResponse)
                        result = context.getString(R.string.data_no_available)
                } else try {
                    wsResponse.schoolGrades = Json.parse(SchoolGrade.serializer().list, result)
                    success = true
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    result = e.message ?: context.getString(R.string.error_parsing_data)
                }

                wsResponse.success = success

                if (!success)
                    wsResponse.message = result

                wsResponse
            }
    }
}