package se.joepocalyp

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import se.joepocalyp.SynergyClient.client
import se.joepocalyp.objects.TokenPair
import se.joepocalyp.util.SteamOpenID
import java.util.*

object PinchfistIntegration {
    private val openID = SteamOpenID()

    @JvmStatic fun Route.pinchfist() {
        route("/pinchfist") {
            get("/login") {
                if(call.request.userAgent()?.toLowerCase()?.contains("discord") == true) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }
                if(call.request.queryParameters["token"] == null) {
                    call.respond(HttpStatusCode.BadRequest, "Token required")
                    return@get
                }
                call.respondRedirect(openID.login("http://www.dirtyfilthycu.lt/pinchfist/auth?token=${UUID.fromString(call.request.queryParameters["token"])}")!!)
                println("Initiating Pinchfist login.")
            }

            get("/auth") {
                val token: UUID?
                try {
                    token = UUID.fromString(call.request.queryParameters["token"])
                } catch(e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid token"); return@get
                }

                val map = mutableMapOf<String, String>()
                call.request.queryParameters.forEach {key: String, value: List<String> -> map[key] = value[0]}

                val user: String?
                try {
                    user = openID.verify("http://www.dirtyfilthycu.lt/pinchfist/auth?token=${UUID.fromString(call.request.queryParameters["token"])}", map)
                } catch(e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Steam OpenID verification failed; contact Joepocalypse if this persists"); return@get
                }

                println("User detected for Pinchfist: $user")
                if(user != null && token != null) {
                    println("User valid.")
                    client.post<TokenPair> {
                        url("http://127.0.0.1:5555/authPassthrough")
                        contentType(ContentType.Application.Json)
                        body = TokenPair(authToken = token.toString(), steamID = user)
                        println("Posted to Pinchfist on localhost")
                        call.respondText("Success! Please check your Discord DMs.", status = HttpStatusCode.OK)
                    }
                } else {
                    println("Token null, ignoring.")
                }
            }
        }
    }
}