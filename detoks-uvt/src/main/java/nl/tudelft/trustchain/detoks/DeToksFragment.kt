package nl.tudelft.trustchain.detoks

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_detoks.*
import mu.KotlinLogging
import nl.tudelft.trustchain.common.ui.BaseFragment
import nl.tudelft.trustchain.detoks.token.ProposalToken
import nl.tudelft.trustchain.detoks.token.UpvoteToken
import nl.tudelft.trustchain.detoks.trustchain.Balance
import java.io.File
import java.io.FileOutputStream

class DeToksFragment : BaseFragment(R.layout.fragment_detoks) {
    private lateinit var torrentManager: TorrentManager
    private lateinit var balance: Balance
    private lateinit var upvoteToken: UpvoteToken
    private lateinit var proposalToken: ProposalToken
    private val logger = KotlinLogging.logger {}
    private var previousVideoAdapterIndex = 0

    private val torrentDir: String
        get() = "${requireActivity().cacheDir.absolutePath}/torrent"
    private val mediaCacheDir: String
        get() = "${requireActivity().cacheDir.absolutePath}/media"
    private val postVideosDir: String
        get() = "${requireActivity().cacheDir.absolutePath}/postVideos"

    private fun cacheDefaultTorrent() {
        try {
            val dir1 = File(mediaCacheDir)
            if (!dir1.exists()) {
                dir1.mkdirs()
            }
            val dir2 = File(torrentDir)
            if (!dir2.exists()) {
                dir2.mkdirs()
            }
            val file = File("$torrentDir/$DEFAULT_TORRENT_FILE")
            if (!file.exists()) {
                val outputStream = FileOutputStream(file)
                val ins = requireActivity().resources.openRawResource(R.raw.detoks)
                outputStream.write(ins.readBytes())
                ins.close()
                outputStream.close()
            }

            val catfile = File("$torrentDir/$DEFAULT_POST_VIDEO")
            if (!catfile.exists()) {
                val outputStream = FileOutputStream(catfile)
                val ins = requireActivity().resources.openRawResource(R.raw.cat)
                outputStream.write(ins.readBytes())
                ins.close()
                outputStream.close()
            }
//
//            val arcanefile = File("$torrentDir/arcane.torrent")
//            if (arcanefile.exists()) {
//                arcanefile.delete()
//            }
//
            val dir3 = File(postVideosDir)
            if (!dir3.exists()) {
                dir3.mkdirs()
            }
            val file2 = File("$postVideosDir/$DEFAULT_POST_VIDEO")
            if (!file2.exists()) {
                val outputStream = FileOutputStream(file2)
                val ins = requireActivity().resources.openRawResource(R.raw.cat)
                outputStream.write(ins.readBytes())
                ins.close()
                outputStream.close()
            }
        } catch (e: Exception) {
            Log.e("DeToks", "Failed to cache default torrent: $e")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cacheDefaultTorrent()
        torrentManager = TorrentManager(
            File("${requireActivity().cacheDir.absolutePath}/media"),
            File("${requireActivity().cacheDir.absolutePath}/torrent"),
            File("${requireActivity().cacheDir.absolutePath}/postVideos"),
            DEFAULT_CACHING_AMOUNT
        )

        upvoteToken = UpvoteToken(-100, "", "", "") //TODO: make constructor with no parameters for initialisation
        proposalToken = ProposalToken()
        balance = Balance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerVideos.adapter = VideosAdapter(torrentManager, upvoteToken, proposalToken, balance)
        viewPagerVideos.currentItem = 0
        onPageChangeCallback()
    }

    /**
     * This functions allows for looping back to start of the video pool.
     */
    private fun onPageChangeCallback() {
        viewPagerVideos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (viewPagerVideos.currentItem - previousVideoAdapterIndex) {
                        1 -> torrentManager.notifyIncrease()
                        -1 -> torrentManager.notifyDecrease()
                        0 -> {} // Do nothing
                        else -> {
                            logger.error { "Something went wrong with the video adapter index" }
                        }
                    }
                    previousVideoAdapterIndex = viewPagerVideos.currentItem
                }
            }
        })
    }

    companion object {
        const val DEFAULT_CACHING_AMOUNT = 1
        const val DEFAULT_TORRENT_FILE = "detoks.torrent"
        const val DEFAULT_POST_VIDEO = "cat.torrent"
//        const val DEFAULT_POST_VIDEO2 = "pexels10.torrent"
//        const val DEFAULT_POST_VIDEO3 = "blueparrot.torrent"
    }
}
