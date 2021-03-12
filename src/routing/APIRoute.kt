package se.joepocalyp.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import se.joepocalyp.objects.NewsEntry

object APIRoute {
    @JvmStatic fun Route.api() {
        route("/routing") {
            get("/news") {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 15
                println(limit)
                val news = listOf(NewsEntry("Heading One", "Text One <i>italic</i><b>bold</b>"),
                    NewsEntry("Heading Two", "Text Two <i>italic</i><b>bold</b>"))

                call.respond(HttpStatusCode.OK, news)
            }

            route("/steam") {
                get("/top") {

                }
                get("/user") {
                    val params = call.request.queryParameters
                    params["steamID"]
                }
            }
        }
    }
}