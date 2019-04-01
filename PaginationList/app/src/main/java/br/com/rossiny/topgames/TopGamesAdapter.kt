package br.com.rossiny.topgames

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.rossiny.topgames.models.GamePackage
import br.com.rossiny.topgames.utils.TopGamesAdapterCallback
import br.com.rossiny.topgames.utils.TopGamesGlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

open class TopGamesAdapter internal constructor(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var games: MutableList<GamePackage> = arrayListOf()

    private var isLoadingAdded = false
    private var retryPageLoad = false

    private val mCallback: TopGamesAdapterCallback = context as TopGamesAdapterCallback

    private var errorMsg: String? = null

    val isEmpty: Boolean
        get() = itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder?
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.item_list, parent, false)
                viewHolder = MovieVH(viewItem)
            }
            else -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val game = games[position].game

        game?.also {
            when (getItemViewType(position)) {

                ITEM -> {
                    val movieVH = holder as MovieVH
                    movieVH.mPosterImg.setOnClickListener {
                        context.startActivity(DetailActivity.newIntent(context, games[position]))
                    }

                    movieVH.mGameTitle.text = game.name

                    // load movie thumbnail
                    game.box?.large?.let {
                        TopGamesGlideModule.loadImage(it, context)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any,
                                    target: Target<Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // TODO: 2/16/19 Handle failure
                                    movieVH.mProgress.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // image ready, hide progress now
                                    movieVH.mProgress.visibility = View.GONE
                                    return false   // return false if you want Glide to handle everything else.
                                }
                            })
                            .into(movieVH.mPosterImg)
                    }
                }

                LOADING -> {
                    val loadingVH = holder as LoadingVH

                    if (retryPageLoad) {
                        loadingVH.mErrorLayout.visibility = View.VISIBLE
                        loadingVH.mProgressBar.visibility = View.GONE

                        loadingVH.mErrorTxt.text = if (errorMsg != null)
                            errorMsg
                        else
                            context.getString(R.string.error_msg_unknown)

                    } else {
                        loadingVH.mErrorLayout.visibility = View.GONE
                        loadingVH.mProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return games.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == games.size - 1 && isLoadingAdded)
            LOADING
        else ITEM
    }


    fun add(r: GamePackage) {
        games.add(r)
        notifyItemInserted(games.size - 1)
    }

    fun addAll(gameResults: List<GamePackage>) {
        for (result in gameResults) {
            add(result)
        }
    }

    fun remove(r: GamePackage?) {
        val position = games.indexOf(r)
        if (position > -1) {
            games.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(GamePackage())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = games.size - 1
        val result = getItem(position)

        if (result != null) {
            games.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): GamePackage? {
        return games[position]
    }

    fun getAllItems(): List<GamePackage> {
        return games
    }

    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(games.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


    protected inner class MovieVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mGameTitle: TextView = itemView.findViewById(R.id.game_title)
        val mPosterImg: ImageView = itemView.findViewById(R.id.game_poster)
        val mProgress: ProgressBar = itemView.findViewById(R.id.movie_progress)
    }


    protected inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mProgressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progress)
        val mRetryBtn: ImageButton = itemView.findViewById(R.id.loadmore_retry)
        val mErrorTxt: TextView = itemView.findViewById(R.id.loadmore_errortxt)
        val mErrorLayout: LinearLayout = itemView.findViewById(R.id.loadmore_errorlayout)

        init {
            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {

                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {

        // View Types
        private const val ITEM = 0
        private const val LOADING = 1

        private const val BASE_URL_IMG = "https://image.tmdb.org/t/p/w200"
    }

}
