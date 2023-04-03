package nl.tudelft.trustchain.detoks

import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.detoks.community.UpvoteCommunity
import nl.tudelft.trustchain.detoks.recommendation.Recommender
import nl.tudelft.trustchain.detoks.token.ProposalToken
import nl.tudelft.trustchain.detoks.token.UpvoteToken
import nl.tudelft.trustchain.detoks.trustchain.Balance


class VideosAdapter(
    private val torrentManager: TorrentManager,
    private val upvoteToken: UpvoteToken,
    private val proposalToken: ProposalToken,
    private val balance: Balance,
    private val onPlaybackError: (() -> Unit)? = null,
    private val videoScaling: Boolean = false,
) :
    RecyclerView.Adapter<VideosAdapter.VideoViewHolder?>() {
    private val mVideoItems: List<VideoItem> =
        List(100) { VideoItem(torrentManager::provideContent) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video, parent, false),
            videoScaling,
            torrentManager, upvoteToken, proposalToken, balance)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        Log.i("DeToks", "onBindViewHolder: $position")
        holder.setVideoData(mVideoItems[position], position, onPlaybackError)
    }

    override fun getItemCount(): Int {
        return mVideoItems.size
    }

    class VideoViewHolder(itemView: View,
                          private val videoScaling: Boolean = false,
                          private val torrentManager: TorrentManager, private val upvoteToken: UpvoteToken, private val proposalToken: ProposalToken, private val balance: Balance) :
        RecyclerView.ViewHolder(itemView) {
        var mVideoView: VideoView
        var txtTitle: TextView
        var txtDesc: TextView
        var mProgressBar: ProgressBar
        var proposalBlockHash: TextView
        var videoID: TextView
        var videoPostedOn: TextView

        // TODO: remove this, for testing of Recommender only
        var recommendMostLikedButton: Button
        var recommendRandomButton: Button

        var proposalSendButton: Button
        var tokensSent: TextView
        var tokensReceived: TextView
        var tokensBalance: TextView
        var peerId: TextView

        init {
            mVideoView = itemView.findViewById(R.id.videoView)
            txtTitle = itemView.findViewById(R.id.txtTitle)
            txtDesc = itemView.findViewById(R.id.txtDesc)
            mProgressBar = itemView.findViewById(R.id.progressBar)
            proposalBlockHash = itemView.findViewById(R.id.proposalBlockHash)
            videoID = itemView.findViewById(R.id.videoID)
            videoPostedOn = itemView.findViewById(R.id.videoPostedOn)
            recommendMostLikedButton = itemView.findViewById(R.id.recommendMostLiked)
            recommendRandomButton = itemView.findViewById(R.id.recommendRandom)
            proposalSendButton = itemView.findViewById(R.id.proposalMockButton)
            tokensSent = itemView.findViewById(R.id.tokensSentValue)
            tokensReceived = itemView.findViewById(R.id.tokensReceivedValue)
            tokensBalance = itemView.findViewById(R.id.tokensBalanceValue)
            peerId = itemView.findViewById(R.id.ownPeerAddress)

            upvoteToken.setLikeListener(itemView, videoID, proposalBlockHash)
            proposalToken.setPostVideoListener(proposalSendButton, itemView, torrentManager)
            balance.checkTokenBalance(tokensSent, tokensReceived, tokensBalance, peerId)
            balance.dailyBalanceCheckpoint(tokensSent, tokensReceived, tokensBalance, peerId)



            // TODO: remove this, for testing of Recommender only
            recommendMostLikedButton.setOnClickListener { Recommender.recommendMostLiked() }
            recommendRandomButton.setOnClickListener { Recommender.recommendRandom() }
            recommendMostLikedButton.setOnClickListener { Recommender.requestRecommendations()}

            val location = IntArray(2)
            proposalSendButton.getLocationOnScreen(location)
            Log.i("DeToks", "Proposal button X: ${location[0]} Y: ${location[1]}")
        }

        fun getLocationOnScreen(view: View): Point {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return Point(location[0], location[1])
        }

        fun setVideoData(item: VideoItem, position: Int, onPlaybackError: (() -> Unit)? = null) {
            CoroutineScope(Dispatchers.Main).launch {
                val content = item.content(position, 10000)
                txtTitle.text = content.fileName
                txtDesc.text = content.torrentName
                mVideoView.setVideoPath(content.fileURI)
                proposalBlockHash.text = content.proposalBlockHash
                videoPostedOn.text = content.videoPostedOn
                videoID.text = content.videoID
                Log.i("DeToks", "Received content: ${content.fileURI}")
                mVideoView.setOnPreparedListener { mp ->
                    mProgressBar.visibility = View.GONE
                    mp.start()
                    if (videoScaling) {
                        val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
                        val screenRatio = mVideoView.width / mVideoView.height.toFloat()
                        val scale = videoRatio / screenRatio
                        if (scale >= 1f) {
                            mVideoView.scaleX = scale
                        } else {
                            mVideoView.scaleY = 1f / scale

                        }
                    }
                }
                mVideoView.setOnCompletionListener { mp -> mp.start() }
                mVideoView.setOnErrorListener { p1, what, extra ->
                    Log.i("DeToks", "onError: $p1, $what, $extra")
                    if (onPlaybackError != null) {
                        onPlaybackError()
                        true
                    } else {
                        true
                    }
                }
            }
        }
    }
}

class VideoItem(val content: suspend (Int, Long) -> TorrentMediaInfo)
