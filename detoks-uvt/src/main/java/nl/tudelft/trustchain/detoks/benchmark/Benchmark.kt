package nl.tudelft.trustchain.detoks.benchmark

import android.util.Log
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.detoks.community.UpvoteCommunity
import nl.tudelft.trustchain.detoks.recommendation.Recommender


class Benchmark {
    companion object {
        private val runs: Int = 5

        private var isInitialized: Boolean = false
        private var runBenchmark: Boolean = false

        fun initialize(run: Boolean) {
            if (isInitialized)
                return
            runBenchmark = run
            isInitialized = true

            if (runBenchmark) {
                // Run the benchmark for creating proposal blocks, used when uploading a new video
                createProposalBlocks()

                // Run the benchmark for creating upvote tokens, used when liking a video
                sendUpvoteTokens()

                // Run the benchmark for creating recommendations for new videos
                createRecommendations()
            }
        }

        fun createProposalBlocks() {
        }

        fun sendUpvoteTokens() {
        }

        fun createRecommendations() {
            val n = 100
            Log.i("DeToksBenchmark", "Recommender Benchmark")
            timeRun(n, "Create Recommendations", Recommender::getNextRecommendation)
            timeRun(n, "Create MOST LIKED Recommendations", Recommender::recommendMostLiked)
            timeRun(n, "Create RANDOM Recommendations", Recommender::recommendRandom)
            timeRun(n, "Create PEER Recommendations", Recommender::requestRecommendations)
        }

        /**
         * Execute a function and show the timing results of all the runs and the time
         * per single run.
         */
        private fun timeRun(runs: Int, title: String, func: () -> (Unit)) {
            val start = System.currentTimeMillis()
            for (i in 0 .. runs) {
                func()
            }
            val delta = System.currentTimeMillis() - start
            val doneString = "${"%.2f".format(delta.toFloat())}ms"
            Log.i("DeToksBenchmark", "\t$title done in $doneString")
            val timePerRunString = "${"%.1f".format(delta / runs.toFloat())}ms"
            Log.i("DeToksBenchmark", "\t\tTime/Run: $timePerRunString (n = $runs)")
        }
    }
}
