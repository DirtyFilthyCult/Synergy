package se.joepocalyp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import se.joepocalyp.routing.SteamRoute.steam
import se.joepocalyp.api.APIRoute.api
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyStore

object SynergyServer {
    @JvmStatic fun main(args: Array<String>) {
        val keyStorePassword = File("/etc/synergy/keystorepassword.txt").readText().toCharArray()
        val keyStore = KeyStore.getInstance("PKCS12")

        FileInputStream("/etc/synergy/dirtyfilthycult.jks").use { keyStoreData -> keyStore.load(keyStoreData, keyStorePassword) }

        val env = applicationEngineEnvironment {
            module {
                try {
                    module()
                } catch(_: IOException) {}
            }

            connector {
                host = "23.254.203.251"
                port = 80
            }

            sslConnector(keyStore = keyStore,
                    keyAlias = "www.dirtyfilthycu.lt",
                    keyStorePassword = {keyStorePassword},
                    privateKeyPassword = {keyStorePassword}) {
                host = "23.254.203.251"
                port = 443
                // TODO: Another path to make customizable through JVM arguments (?)
                // (Especially for use in non-Linux/ext4 environments in the case of a server migration)
                keyStorePath = File("/etc/synergy/dirtyfilthycult.jks")
            }
        }

        embeddedServer(Netty, env).start(true)
    }

    private fun Application.module() {
        // Automatically redirect HTTP to HTTPS
        install(HttpsRedirect)

        // Ensure connections are routed to HTTPS in the future
        install(HSTS)
        install(StatusPages)

        install(AutoHeadResponse)

        install(ContentNegotiation) {
            gson()
        }

        install(CachingHeaders) {
            val noCache = CachingOptions(CacheControl.NoCache(CacheControl.Visibility.Public))
            val lazy = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 10)) // 10 minutes
            val superLazy = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 60)) // 1 hour
            options { outgoingContent ->
                when (outgoingContent.contentType?.withoutParameters()) {
                    ContentType.Text.Html -> noCache
                    ContentType.Text.CSS -> lazy
                    ContentType.Text.JavaScript -> lazy
                    ContentType.Image.SVG -> superLazy
                    ContentType.Image.JPEG -> superLazy
                    ContentType.Image.PNG -> superLazy
                    ContentType("image", "fav") -> superLazy
                    else -> null
                }
            }
        }

        routing {
            api()
            steam()

            static("/") {
                resources("dist")
                resource("/", "dist/index.html")
            }
        }
    }
}