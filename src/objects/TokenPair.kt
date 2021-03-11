package se.joepocalyp.objects

import java.io.Serializable

data class TokenPair(val authToken: String, val steamID: String) : Serializable
