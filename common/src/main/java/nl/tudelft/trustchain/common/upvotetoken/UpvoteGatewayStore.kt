package nl.tudelft.trustchain.common.upvotetoken

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import nl.tudelft.common.sqldelight.Database
import nl.tudelft.ipv8.keyvault.PublicKey
import nl.tudelft.ipv8.keyvault.defaultCryptoProvider

open class UpvoteGatewayStore(val database: Database) {
    fun addGateway(
        publicKey: PublicKey,
        name: String,
        ip: String,
        port: Long,
        preferred: Boolean = false
    ) {
        if (preferred) {
            clearPreferred()
        }
        database.dbGatewayQueries.addGateway(
            name,
            publicKey.keyToBin(),
            ip,
            port,
            if (preferred) 1L else 0L
        )
    }

    fun updateGateway(
        publicKey: PublicKey,
        name: String,
        ip: String,
        port: Long,
        preferred: Boolean = false
    ) {
        if (preferred) {
            clearPreferred()
        }
        database.dbGatewayQueries.addGateway(
            name,
            publicKey.keyToBin(),
            ip,
            port,
            if (preferred) 1L else 0L
        )
    }

    private fun clearPreferred() {
        for (pref in getPreferred()) {
            updateGateway(pref.publicKey, pref.name, pref.ip, pref.port, false)
        }
    }

    fun setPreferred(gateway: UpvoteGateway) {
        clearPreferred()
        updateGateway(gateway.publicKey, gateway.name, gateway.ip, gateway.port, true)
    }

    fun getPreferred(): List<UpvoteGateway> {
        return database.dbGatewayQueries.getPreffered { name, public_key, ip, port, preferred ->
            val publicKey = defaultCryptoProvider.keyFromPublicBin(public_key)
            UpvoteGateway(name, publicKey, ip, port, preferred == 1L)
        }.executeAsList()
    }

    fun getGatewayFromPublicKey(publicKey: ByteArray): UpvoteGateway? {
        val gateway =
            database.dbGatewayQueries.getGateway(publicKey).executeAsOneOrNull()
        return if (gateway != null) {
            UpvoteGateway(
                gateway.name,
                defaultCryptoProvider.keyFromPublicBin(gateway.public_key),
                gateway.ip,
                gateway.port,
                gateway.preferred == 1L
            )
        } else null
    }

    fun getGatewayFromPublicKey(publicKey: PublicKey): UpvoteGateway? {
        val gateway =
            database.dbGatewayQueries.getGateway(publicKey.keyToBin()).executeAsOneOrNull()
        return if (gateway != null) {
            UpvoteGateway(
                gateway.name,
                defaultCryptoProvider.keyFromPublicBin(gateway.public_key),
                gateway.ip,
                gateway.port,
                gateway.preferred == 1L
            )
        } else null
    }

    fun getGateways(): List<UpvoteGateway> {
        return database.dbGatewayQueries.getAll { name, public_key, ip, port, preferred ->
            val publicKey = defaultCryptoProvider.keyFromPublicBin(public_key)
            UpvoteGateway(name, publicKey, ip, port, preferred == 1L)
        }.executeAsList()
    }

    fun deleteGateway(gateway: UpvoteGateway) {
        database.dbGatewayQueries.deleteGateway(gateway.publicKey.keyToBin())
    }

    companion object {
        private lateinit var instance: UpvoteGatewayStore
        fun getInstance(context: Context): UpvoteGatewayStore {
            if (!Companion::instance.isInitialized) {
                instance = SqlGatewayStore(context)
            }
            return instance
        }
    }
}

class SqlGatewayStore(context: Context) : UpvoteGatewayStore(
    Database(AndroidSqliteDriver(Database.Schema, context, "common2.db"))
)
