package se.joepocalyp.objects.steam

import se.joepocalyp.objects.steam.SteamPlayerSummary
import java.io.Serializable

data class SteamSummaryResponse(val players: ArrayList<SteamPlayerSummary>): Serializable