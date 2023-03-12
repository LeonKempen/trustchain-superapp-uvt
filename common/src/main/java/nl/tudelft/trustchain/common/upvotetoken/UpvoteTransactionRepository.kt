package nl.tudelft.trustchain.common.upvotetoken

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import java.math.BigInteger

class UpvoteTransactionRepository(
    val trustChainCommunity: TrustChainCommunity,
    val upvoteGatewayStore: UpvoteGatewayStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun getBalanceChangeForBlock(block: TrustChainBlock?): Long {
        if (block == null) return 0
        return if (
            (listOf(BLOCK_TYPE_TRANSFER).contains(block.type) && block.isProposal)
        ) {
            // block is an upvote
            -(block.transaction[KEY_AMOUNT] as BigInteger).toLong()
        } else if (
            (listOf(BLOCK_TYPE_TRANSFER).contains(block.type) && block.isAgreement)
        ) {
            // block is receiving an upvote
            (block.transaction[KEY_AMOUNT] as BigInteger).toLong()
            // block.transaction[KEY_AMOUNT] as Long
        } else {
            // block does nothing
            0
        }

    }

    fun getMyVerifiedBalance(): Long {
        val mykey = IPv8Android.getInstance().myPeer.publicKey.keyToBin()
        val latestBlock = trustChainCommunity.database.getLatest(mykey) ?: return 0
        return getVerifiedBalanceForBlock(latestBlock, trustChainCommunity.database)!!
    }

    fun getMyBalance(): Long {
        val myKey = IPv8Android.getInstance().myPeer.publicKey.keyToBin()
        val latestBlock = trustChainCommunity.database.getLatest(myKey) ?: return 0
        return getBalanceForBlock(latestBlock, trustChainCommunity.database)!!
    }

    fun getBalanceForBlock(block: TrustChainBlock?, database: TrustChainStore): Long? {
        if (block == null) return null
        if (block.isGenesis)
            return getBalanceChangeForBlock(block)
        if (!UPVOTETOKEN_TYPES.contains(block.type)) return getBalanceForBlock(
            database.getBlockWithHash(
                block.previousHash
            ),
            database
        )
        return if ( // block contains balance (base case)
            (
                listOf(
                    BLOCK_TYPE_TRANSFER,
                ).contains(block.type) && block.isProposal
                )
        ) {
            (block.transaction[KEY_BALANCE] as Long)
        } else if (listOf(
                BLOCK_TYPE_TRANSFER,
            ).contains(block.type) && block.isAgreement
        ) {
            // block is receiving money add it and recurse
            getBalanceForBlock(database.getBlockWithHash(block.previousHash), database)?.plus(
                (block.transaction[KEY_AMOUNT] as BigInteger).toLong()
            )
        } else {
            // bad type that shouldn't exist, for now just ignore and return for next
            getBalanceForBlock(database.getBlockWithHash(block.previousHash), database)
        }
    }

    fun getVerifiedBalanceForBlock(block: TrustChainBlock?, database: TrustChainStore): Long? {
        if (block == null) return null // Missing block
        Log.d("UpvoteTokenBlock", "Validation, Getting balance for block ${block.sequenceNumber}")
        if (block.isGenesis) return 0
        if (!UPVOTETOKEN_TYPES.contains(block.type)) {
            Log.d("UpvoteTokenBlock", "Validation, not upvotetoken ")
            return getVerifiedBalanceForBlock(
                database.getBlockWithHash(
                    block.previousHash
                ),
                database
            )
        }
        if (BLOCK_TYPE_CHECKPOINT == block.type && block.isProposal) {
            // block contains balance but linked block determines verification
            if (database.getLinked(block) != null) { // verified
                Log.d("UpvoteTokenBlock", "Validation, valid checkpoint returning")
                return (block.transaction[KEY_BALANCE] as Long)
            } else {
                Log.d("UpvoteTokenBlock", "Validation, checkpoint missing acceptance")
                return getVerifiedBalanceForBlock(
                    database.getBlockWithHash(block.previousHash),
                    database
                )
            }
        } else if (listOf(
                BLOCK_TYPE_TRANSFER,
            ).contains(block.type) && block.isAgreement
        ) {
            // block is receiving money, but balance is not verified, just recurse
            Log.d("UpvoteTokenBlock", "Validation, receiving token")
            return getVerifiedBalanceForBlock(
                database.getBlockWithHash(block.previousHash),
                database
            )
        } else if (listOf(
                BLOCK_TYPE_TRANSFER
            ).contains(
                block.type
            ) && block.isProposal
        ) {
            Log.d("UpvoteTokenBlock", "Validation, sending token")
            // block is sending money, but balance is not verified, subtract transfer amount and recurse
            val amount =
                (block.transaction[KEY_AMOUNT] as BigInteger).toLong()
            return getVerifiedBalanceForBlock(
                database.getBlockWithHash(block.previousHash),
                database
            )?.minus(
                amount
            )
        } else {
            // bad type that shouldn't exist, for now just ignore and return for next
            Log.d("UpvoteTokenBlock", "Validation, bad type")
            return getVerifiedBalanceForBlock(
                database.getBlockWithHash(block.previousHash),
                database
            )
        }
    }

    fun sendTransferProposal(recipient: ByteArray, amount: Long): Boolean {
        scope.launch {
            sendTransferProposalSync(recipient, amount)
        }
        return true
    }

    fun sendTransferProposalSync(recipient: ByteArray, amount: Long): TrustChainBlock? {
        val transaction = mapOf(
            nl.tudelft.trustchain.common.eurotoken.TransactionRepository.KEY_AMOUNT to BigInteger.valueOf(amount),
            nl.tudelft.trustchain.common.eurotoken.TransactionRepository.KEY_BALANCE to (BigInteger.valueOf(getMyBalance() - amount).toLong())
        )
        return trustChainCommunity.createProposalBlock(
            nl.tudelft.trustchain.common.eurotoken.TransactionRepository.BLOCK_TYPE_TRANSFER, transaction,
            recipient
        )
    }

    fun initTrustChainCommunity() {

    }


    companion object {
        const val BLOCK_TYPE_TRANSFER = "upvotetoken_transfer"
        const val BLOCK_TYPE_CHECKPOINT = "upvotetoken_checkpoint"

        val UPVOTETOKEN_TYPES = listOf(
            BLOCK_TYPE_TRANSFER,
            BLOCK_TYPE_CHECKPOINT
        )

        const val KEY_AMOUNT = "amount"
        const val KEY_BALANCE = "balance"
        const val KEY_TRANSACTION_HASH = "transaction_hash"
        const val KEY_PAYMENT_ID = "payment_id"
    }
}
