package nl.tudelft.trustchain.detoks

import android.os.Bundle
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import nl.tudelft.trustchain.common.BaseActivity

class DeToksActivity : BaseActivity() {
    override val navigationGraph = R.navigation.nav_graph_detoks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar!!.hide()

        val db = Firebase.firestore

        val city = hashMapOf(
            "name" to "Los Angeles",
            "state" to "CA",
            "country" to "USA"
        )

        db.collection("cities").document("LA")
            .set(city)
            .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error writing document", e) }
    }
}
