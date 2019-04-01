package br.com.rossiny.topgames

import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import br.com.rossiny.topgames.api.GameApi
import br.com.rossiny.topgames.api.GameService
import br.com.rossiny.topgames.models.GamePackage
import br.com.rossiny.topgames.models.Result
import br.com.rossiny.topgames.utils.PreferencesHelper
import br.com.rossiny.topgames.utils.TopGamesAdapterCallback
import br.com.rossiny.topgames.utils.TopGamesScrollListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.util.concurrent.TimeoutException

class MainActivity : AppCompatActivity(), TopGamesAdapterCallback {

    internal var adapter: TopGamesAdapter? = null
    internal var layoutManager: GridLayoutManager? = null

    internal var rv: RecyclerView? = null
    internal var swipeRefresh: SwipeRefreshLayout? = null
    internal var progressBar: ProgressBar? = null
    internal var errorLayout: LinearLayout? = null
    internal var btnRetry: Button? = null
    internal var txtError: TextView? = null

    private var loading = false
    private var lastPage = false

    private var currentPage = PAGE_START

    private var gameService: GameService? = null

    private lateinit var preferencesHelper: PreferencesHelper

    private val isNetworkConnected: Boolean
        get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesHelper = PreferencesHelper(this)

        rv = this.findViewById(R.id.main_recycler)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        progressBar = findViewById(R.id.main_progress)
        errorLayout = findViewById(R.id.error_layout)
        btnRetry = findViewById(R.id.error_btn_retry)
        txtError = findViewById(R.id.error_txt_cause)

        adapter = TopGamesAdapter(this)

        layoutManager = GridLayoutManager(this, 2)
        rv?.layoutManager = layoutManager
        rv?.itemAnimator = DefaultItemAnimator()

        rv?.adapter = adapter

        rv?.addOnScrollListener(object : TopGamesScrollListener(layoutManager!!) {

            override val totalPageCount: Int
                get() = currentPage

            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                loading = true
                currentPage += 1

                loadNextPage()
            }
        })

        swipeRefresh?.setOnRefreshListener {
            preferencesHelper.results = ""
            adapter?.clear()
            adapter?.notifyDataSetChanged()
            loading = false
            loadFirstPage()
        }

        gameService = GameApi.getClient()?.create()

        if (preferencesHelper.results.isEmpty()) {
            loadFirstPage()
        } else {
            val gson = Gson()
            val gameList: List<GamePackage> = gson.fromJson(preferencesHelper.results,
                object : TypeToken<List<GamePackage>>() {}.type)
            hideErrorView()
            progressBar?.visibility = View.GONE
            adapter?.addAll(gameList)
            adapter?.addLoadingFooter()
        }

        btnRetry?.setOnClickListener { loadFirstPage() }

    }


    private fun loadFirstPage() {
        hideErrorView()

        callTopRatedGamesApi(0)?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh?.isRefreshing = false
                hideErrorView()

                val results = fetchResults(response)
                progressBar?.visibility = View.GONE
                adapter?.addAll(results)
                saveData()

                adapter?.addLoadingFooter()
//                if (currentPage <= TOTAL_PAGES) adapter?.addLoadingFooter()
//                else lastPage = true
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                t.printStackTrace()
                showErrorView(t)
            }
        })
    }

    private fun saveData() {
        val gson = Gson()
        val results = gson.toJson(adapter?.getAllItems())
        preferencesHelper.results = results
    }

    private fun fetchResults(response: Response<Result>): List<GamePackage> {
        val result = response.body()
        result?.top?.also {
            return it
        }

        return arrayListOf()
    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNext: $currentPage")

        adapter?.itemCount?.let { callTopRatedGamesApi(it - 1) }?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh?.isRefreshing = false
                adapter?.removeLoadingFooter()
                loading = false

                val results = fetchResults(response)
                adapter?.addAll(results)
                saveData()

                adapter?.addLoadingFooter()
//                if (currentPage != TOTAL_PAGES)
//                    adapter?.addLoadingFooter()
//                else
//                    lastPage = true
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                t.printStackTrace()
                loading = false
                adapter?.showRetry(true, fetchErrorMessage(t))
            }
        })
    }


    private fun callTopRatedGamesApi(offset: Int): Call<Result>? {
        return gameService?.getTopRatedGames(offset)
    }


    override fun retryPageLoad() {
        loadNextPage()
    }


    private fun showErrorView(throwable: Throwable) {

        if (errorLayout?.visibility == View.GONE) {
            errorLayout?.visibility = View.VISIBLE
            progressBar?.visibility = View.GONE

            txtError?.text = fetchErrorMessage(throwable)
        }
    }

    private fun fetchErrorMessage(throwable: Throwable): String {
        var errorMsg = resources.getString(R.string.error_msg_unknown)

        if (!isNetworkConnected) {
            errorMsg = resources.getString(R.string.error_msg_no_internet)
        } else if (throwable is TimeoutException) {
            errorMsg = resources.getString(R.string.error_msg_timeout)
        }

        return errorMsg
    }


    private fun hideErrorView() {
        if (errorLayout?.visibility == View.VISIBLE) {
            errorLayout?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
        }
    }

    companion object {

        private val TAG = "MainActivity"

        private val PAGE_START = 1
    }
}