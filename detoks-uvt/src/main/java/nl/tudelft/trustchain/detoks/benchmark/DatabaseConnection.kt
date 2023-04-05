package nl.tudelft.trustchain.detoks.benchmark

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.detoks.community.UpvoteCommunity

class DatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Add a new benchmark result to the Firestore database for keeping track of
     * benchmark performance across different emulators.
     */
    fun addBenchmarkResult(title: String, runs: Int, startTime: Long, endTime: Long) {
        val upvoteCommunity = IPv8Android.getInstance().getOverlay<UpvoteCommunity>()
        val myPubKey = upvoteCommunity?.myPeer?.publicKey.toString()
        val delta = endTime - startTime
        val timePerRun = delta / runs.toFloat()
        val benchmarkResult = hashMapOf(
            "timestamp" to FieldValue.serverTimestamp(),
            "pub_key" to myPubKey,
            "title" to title,
            "runs" to runs,
            "start_time" to startTime,
            "end_time" to endTime,
            "total_time" to delta,
            "time_per_run" to timePerRun
        )

        db.collection("benchmark_results")
            .add(benchmarkResult)
            .addOnSuccessListener { Log.i("DeToks", "Successfully added benchmark result: $benchmarkResult") }
            .addOnFailureListener { e -> Log.i("DeToks", "Error adding benchmark result: $e")}
    }
}
