package nl.tudelft.trustchain.common.upvotetoken

import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.keyvault.PublicKey
import java.util.*

data class UpvoteTransaction(
    val block: TrustChainBlock,
    val sender: PublicKey,
    val receiver: PublicKey,
    val amount: Long,
    val type: String,
    val outgoing: Boolean,
    val timestamp: Date
) {

    override fun equals(other: Any?): Boolean {
        return other is UpvoteTransaction &&
            other.block == block
    }
}
