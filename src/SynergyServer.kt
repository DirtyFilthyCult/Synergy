package se.joepocalyp

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import se.joepocalyp.PinchfistIntegration.pinchfist
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

        routing {
            get("/") {
                call.respondText("All hail Cronkhina-chan, grandmaster of the weebs.", contentType = ContentType.Text.Plain)
            }

            pinchfist()
        }
    }
}