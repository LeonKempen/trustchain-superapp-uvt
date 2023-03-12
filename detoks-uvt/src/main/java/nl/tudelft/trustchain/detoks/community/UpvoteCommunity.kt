package nl.tudelft.trustchain.detoks.community

import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.messaging.Packet
import mu.KotlinLogging
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCrawler
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.trustchain.common.upvotetoken.UpvoteTransactionRepository

private val logger = KotlinLogging.logger {}

class UpvoteCommunity(
    settings: TrustChainSettings,
    database: TrustChainStore,
    crawler: TrustChainCrawler = TrustChainCrawler()
) : TrustChainCommunity(settings, database, crawler) {
    override val serviceId = "ee6ce7b5ad81eef11f4fcff335229ba169c03aeb"

    private lateinit var upvoteTransactionRepository: UpvoteTransactionRepository

    init {
        messageHandlers[MessageID.HEART_TOKEN] = ::onHeartTokenPacket
    }

    @JvmName("setTransactionRepository1")
    fun setTransactionRepository(upvoteTransactionRepositoryLocal: UpvoteTransactionRepository) {
        upvoteTransactionRepository = upvoteTransactionRepositoryLocal
    }

    object MessageID {
        const val HEART_TOKEN = 1
    }

    private fun onHeartTokenPacket(packet: Packet){
        val (peer, payload) = packet.getAuthPayload(HeartTokenPayload.Deserializer)
        onHeartToken(peer, payload)
    }

    private fun onHeartToken(peer: Peer, payload: HeartTokenPayload) {
        // do something with the payload
        logger.debug { "-> received heart token with id: ${payload.id}  and token: ${payload.token} from peer with member id: ${peer.mid}" }
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
    fun sendHeartToken(id: String, token: String): String {
        val peer = pickRandomPeer()
        val receiverPublicKey = peer?.publicKey.toString()
        val amount = 1L

        logger.debug { "Reveicer key is $receiverPublicKey" }
        //val key = defaultCryptoProvider.keyFromPublicBin(receiverPublicKey.hexToBytes())
        val balance = upvoteTransactionRepository.getMyBalance();

        logger.debug { "Balance before upvote $balance" }
        logger.debug { "Balance before upvote $balance" }

        val succes = upvoteTransactionRepository.sendTransferProposal(receiverPublicKey.hexToBytes(), amount)


        if (succes) {
            val payload = HeartTokenPayload(id, token)

            val packet = serializePacket(
                MessageID.HEART_TOKEN,
                payload
            )

            if (peer != null) {
                val message = "You/Peer with member id: ${myPeer.mid} is sending a heart token to peer with peer id: ${peer.mid}"
                logger.debug { message }
                send(peer, packet)
                return message
            }
        }




        return "No peer found"
    }

    class Factory(
        private val settings: TrustChainSettings,
        private val database: TrustChainStore,
        private val crawler: TrustChainCrawler = TrustChainCrawler()
    ) : Overlay.Factory<UpvoteCommunity>(UpvoteCommunity::class.java) {
        override fun create(): UpvoteCommunity {
            return UpvoteCommunity(settings, database, crawler)
        }
    }
}
