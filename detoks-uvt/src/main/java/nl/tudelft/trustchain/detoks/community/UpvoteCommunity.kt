package nl.tudelft.trustchain.detoks.community

import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.messaging.Packet
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class UpvoteCommunity() : Community(){
    /**
     * serviceId is a randomly generated hex string with length 40
     */
    override val serviceId = "ee6ce7b5ad81eef11f4fcff335229ba169c03aeb"

    init {
        messageHandlers[MessageID.HEART_TOKEN] = ::onHeartTokenPacket
    }

    object MessageID {
        const val HEART_TOKEN = 1
    }

    private fun onHeartTokenPacket(packet: Packet){
        val (peer, payload) = packet.getAuthPayload(UpvoteTokenPayload.Deserializer)
        onHeartToken(peer, payload)
    }

    private fun onHeartToken(peer: Peer, payload: UpvoteTokenPayload) {
        // do something with the payload
        logger.debug { "-> received upvote token with id: ${payload.token_id} from peer with member id: ${peer.mid}" }
    }

    /**
     * Selects a random Peer from the list of known Peers
     * @returns A random Peer or null if there are no known Peers
     */
    private fun pickRandomPeer(): Peer? {
        val peers = getPeers()
        if (peers.isEmpty()) return null
        return peers.random()
    }

    /**
     * Sends a HeartToken to a random Peer
     */
    fun sendHeartToken(token_id: String, date: String, public_key_miner: String, video_id: String): String {
        val payload = UpvoteTokenPayload(token_id, date, public_key_miner, video_id)

        val packet = serializePacket(
            MessageID.HEART_TOKEN,
            payload
        )

        val peer = pickRandomPeer()

        if (peer != null) {
            val message = "You/Peer with member id: ${myPeer.mid} is sending a heart token to peer with peer id: ${peer.mid}"
            logger.debug { message }
            send(peer, packet)
            return message
        }

        return "No peer found"
    }

    class Factory(
        // add parameters needed by the constructor of UpvoteCommunity if needed
    ) : Overlay.Factory<UpvoteCommunity>(UpvoteCommunity::class.java) {
        override fun create(): UpvoteCommunity {
            return UpvoteCommunity()
        }
    }
}
