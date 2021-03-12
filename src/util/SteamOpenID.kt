package se.joepocalyp.util

import org.openid4java.association.AssociationException
import org.openid4java.consumer.ConsumerException
import org.openid4java.consumer.ConsumerManager
import org.openid4java.discovery.DiscoveryException
import org.openid4java.discovery.DiscoveryInformation
import org.openid4java.message.MessageException
import org.openid4java.message.ParameterList
import java.util.regex.Pattern

class SteamOpenID {
    private val manager: ConsumerManager
    private val STEAM_REGEX = Pattern.compile("(\\d+)")
    private var discovered: DiscoveryInformation? = null

    /**
     * Perform a login then redirect to the callback url. When the
     * callback url is opened, you are responsible for
     * verifying the OpenID login.
     *
     * @param callbackUrl A String of a url that this login page should
     * take you to. This should be an absolute URL.
     * @return Returns the URL of the OpenID login page. You should
     * redirect your user to this.
     */
    fun login(callbackUrl: String?): String? {
        if (discovered == null) {
            return null
        }
        try {
            val authReq = manager.authenticate(discovered, callbackUrl)
            return authReq.getDestinationUrl(true)
        } catch (e: MessageException) {
            e.printStackTrace()
        } catch (e: ConsumerException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Verify the SteamRoute OpenID Login
     *
     * @param receivingUrl The url that received the Login (this should be the
     * same as the callbackUrl that you used in
     * the [.login] method.
     * @param responseMap  A [Map] that contains the response values from the login.
     * @return Returns the SteamRoute Community ID as a string.
     */
    fun verify(receivingUrl: String?, responseMap: Map<*, *>?): String? {
        if (discovered == null) {
            return null
        }
        val responseList = ParameterList(responseMap)
        try {
            val verification = manager.verify(receivingUrl, responseList, discovered)
            val verifiedId = verification.verifiedId
            if (verifiedId != null) {
                val id = verifiedId.identifier
                val matcher = STEAM_REGEX.matcher(id)
                if (matcher.find()) {
                    println()
                    return matcher.group(1)
                }
            }
        } catch (e: MessageException) {
            e.printStackTrace()
        } catch (e: DiscoveryException) {
            e.printStackTrace()
        } catch (e: AssociationException) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val STEAM_OPENID = "http://steamcommunity.com/openid"
    }

    init {
        System.setProperty(
            "org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.NoOpLog"
        )
        manager = ConsumerManager()
        manager.maxAssocAttempts = 0
        discovered = try {
            manager.associate(manager.discover(STEAM_OPENID))
        } catch (e: DiscoveryException) {
            e.printStackTrace()
            null
        }
    }
}