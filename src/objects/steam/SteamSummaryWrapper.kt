package se.joepocalyp.objects.steam

import se.joepocalyp.objects.steam.SteamPlayerSummary
import se.joepocalyp.objects.steam.SteamSummaryResponse
import java.io.Serializable

data class SteamSummaryWrapper(val response: SteamSummaryResponse): Serializable