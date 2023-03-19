package nl.tudelft.trustchain.detoks.gossiper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.detoks.DeToksCommunity
import nl.tudelft.trustchain.detoks.gossiper.messages.NetworkSizeMessage
import kotlin.random.Random.Default.nextDouble

class NetworkSizeGossiper(override val delay: Long,
                          override val blocks: Int,
                          override val peers: Int,
                          private val leaders: Int
) : Gossiper() {

    private var firstCycle = true

    override fun startGossip(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            while (coroutineScope.isActive) {
                if (firstCycle) {
                    firstCycle = false
                    delay(System.currentTimeMillis() % delay)
                }
                gossip()
                delay(delay)
            }
        }
    }

    override suspend fun gossip() {
        val deToksCommunity = IPv8Android.getInstance().getOverlay<DeToksCommunity>()!!

        if (leaderEstimates.isNotEmpty())
            networkSizeEstimate = (1 / leaderEstimates.maxOfOrNull { it.second }!!).toInt()

        awaitingResponse.clear()

        val chanceLeader = leaders / networkSizeEstimate
        leaderEstimates = if (nextDouble() < chanceLeader)
            listOf(Pair(deToksCommunity.myPeer.mid, 1.0))
        else listOf()

        val randomPeers = pickRandomN(deToksCommunity.getPeers(), peers)
        randomPeers.forEach {
            awaitingResponse.add(it as Peer)
            deToksCommunity.gossipWith(
                it,
                NetworkSizeMessage(leaderEstimates),
                DeToksCommunity.MESSAGE_NETWORK_SIZE_ID
            )
        }
    }

    companion object {
        var networkSizeEstimate = 1

        private var leaderEstimates = listOf<Pair<String, Double>>()

        private val awaitingResponse = mutableListOf<Peer>()

        /**
         * Applies counting algorithm to determine network size.
         */
        fun receivedData(msg: NetworkSizeMessage, peer: Peer) {
            if (!awaitingResponse.contains(peer)) {
                val deToksCommunity = IPv8Android.getInstance().getOverlay<DeToksCommunity>()!!
                deToksCommunity.gossipWith(
                    peer,
                    NetworkSizeMessage(leaderEstimates),
                    DeToksCommunity.MESSAGE_NETWORK_SIZE_ID
                )
            }

            val myKeys = leaderEstimates.map { it.first }
            val otherKeys = msg.entries.map { it.first }

            val myUnique = leaderEstimates
                .filter { !otherKeys.contains(it.first) }
                .map { Pair(it.first, it.second / 2) }

            val otherUnique = msg.entries
                .filter { !myKeys.contains(it.first) }
                .map { Pair(it.first, it.second / 2) }

            val shared = leaderEstimates
                .filter { otherKeys.contains(it.first) }
                .map {
                    Pair(
                        it.first, it.second + (
                            msg.entries.find { it1 -> it1.first == it.first }?.second ?: 0.0
                            )
                    )
                }

            leaderEstimates = listOf(myUnique, otherUnique, shared).flatten()
        }
    }
}
