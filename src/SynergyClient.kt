package se.joepocalyp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import se.joepocalyp.objects.steam.LeaderboardEntry
import se.joepocalyp.objects.steam.SteamPlayerSummary
import se.joepocalyp.objects.steam.SteamSummaryWrapper
import java.io.File
import java.io.StringReader
import java.util.NoSuchElementException
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.text.get

object SynergyClient {
    @JvmStatic val gson: Gson = GsonBuilder().create()
    @JvmStatic private val steamUserKey = File("/etc/synergy/userkey.txt").readText()

    @JvmStatic val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }

    @JvmStatic fun getSteamSummaries(ids: String): List<SteamPlayerSummary> {
        return runBlocking {
            val request = client.submitForm<HttpResponse>(
                url = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/",
                encodeInQuery = true,
                formParameters = Parameters.build {
                    append("key", steamUserKey)
                    append("steamids", ids)
                }
            )
            return@runBlocking gson.fromJson(request.readText(), SteamSummaryWrapper::class.java).response.players
        }
    }

    @JvmStatic fun getLeaderboard(id: String): List<LeaderboardEntry> {
        return runBlocking {
            val entryList: MutableList<LeaderboardEntry> = mutableListOf()
            val request: HttpResponse = client.get {
                url("https://steamcommunity.com/stats/410900/leaderboards/$id?xml=1&start=1&end=20")
            }

            val xml: Document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(InputSource(StringReader(request.readText())))

            val entries = xml.getElementsByTagName("entry")
            iterable(entries).map{(it as Element)}.map {
                LeaderboardEntry(
                    it.getElementsByTagName("steamid").item(0).textContent,
                    it.getElementsByTagName("score").item(0).textContent.toInt(),
                    it.getElementsByTagName("rank").item(0).textContent.toInt()
                )
            }.forEach(entryList::add)
            return@runBlocking entryList.toList()
        }
    }

    private fun iterable(nodeList: NodeList): Iterable<Node> {
        return Iterable {
            object : MutableIterator<Node> {
                private var index = 0
                override fun hasNext(): Boolean {
                    return index < nodeList.length
                }

                override fun next(): Node {
                    if (!hasNext()) throw NoSuchElementException()
                    return nodeList.item(index++)
                }
                override fun remove() {
                    throw UnsupportedOperationException("Elements cannot be removed from a NodeList.")
                }
            }
        }
    }
}