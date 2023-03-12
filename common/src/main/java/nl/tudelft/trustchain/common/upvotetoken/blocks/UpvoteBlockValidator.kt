package nl.tudelft.trustchain.common.upvotetoken.blocks

import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.ipv8.attestation.trustchain.validation.TransactionValidator
import nl.tudelft.ipv8.attestation.trustchain.validation.ValidationResult
import nl.tudelft.trustchain.common.eurotoken.TransactionRepository
import nl.tudelft.trustchain.common.eurotoken.blocks.EuroTokenBaseValidator

@OptIn(ExperimentalUnsignedTypes::class)
open class UpvoteTokenBaseValidator(val transactionRepository: TransactionRepository) :
    TransactionValidator {

    private fun getBlockBeforeOrRaise(block: TrustChainBlock, database: TrustChainStore): TrustChainBlock? {
        if (block.isGenesis) {
            return null
        }

        return database.getBlockWithHash(block.previousHash)
            ?: throw EuroTokenBaseValidator.PartialPrevious("Missing previous block")
    }

    override fun validate(block: TrustChainBlock, database: TrustChainStore): ValidationResult {
//        try {
//            validateUpvoteToken(block, database)
//        } catch (e: EuroTokenBaseValidator.Invalid) {
//            return ValidationResult.Invalid(listOf(e.TYPE, e.message ?: ""))
//        } catch (e: EuroTokenBaseValidator.PartialPrevious) {
//            return ValidationResult.PartialPrevious
//        } catch (e: EuroTokenBaseValidator.MissingBlocks) {
//            return ValidationResult.MissingBlocks(e.blockRanges)
//        }
        return ValidationResult.Valid
    }

//    fun validateTransaction(transaction: TrustChainTransaction): Boolean {
//        val publicKey = transaction["publicKey"]
//        val bitcoinAddress = transaction["bitcoinAddress"]
//        val name = transaction["name"]
//        val biography = transaction["biography"]
//        val socials = transaction["socials"]
//        val protocolVersion = transaction["protocolVersion"]
//
//        return (
//            publicKey is String && publicKey.isNotEmpty() && transaction.containsKey("publicKey") &&
//                bitcoinAddress is String && bitcoinAddress.isNotEmpty() && transaction.containsKey("bitcoinAddress") &&
//                name is String && name.isNotEmpty() && transaction.containsKey("name") &&
//                biography is String && biography.isNotEmpty() && transaction.containsKey("biography") &&
//                socials is String && socials.isNotEmpty() && transaction.containsKey("socials") &&
//                protocolVersion is String && protocolVersion.isNotEmpty() && protocolVersion == Constants.PROTOCOL_VERSION
//            )
//    }
}
