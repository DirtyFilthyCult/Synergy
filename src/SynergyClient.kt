package se.joepocalyp

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*

object SynergyClient {
    @JvmStatic val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }
}