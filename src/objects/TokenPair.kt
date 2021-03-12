package se.joepocalyp.objects

import io.ktor.application.*
import java.io.Serializable

data class TokenPair(val authToken: String, val steamID: String) : Serializable