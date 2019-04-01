package br.com.rossiny.topgames

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import br.com.rossiny.topgames.models.GamePackage
import br.com.rossiny.topgames.utils.TopGamesGlideModule

class DetailActivity : AppCompatActivity() {

    lateinit var gameImage: ImageView
    lateinit var gameName: TextView
    lateinit var gameChannels: TextView
    lateinit var gameViewers: TextView

    var gamePackage: GamePackage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        gameImage = findViewById(R.id.game_image)
        gameName = findViewById(R.id.game_name)
        gameChannels = findViewById(R.id.game_channels)
        gameViewers = findViewById(R.id.game_viewers)

        if (intent.hasExtra(INTENT_GAME_PARAM)) {
            gamePackage = intent.getSerializableExtra(INTENT_GAME_PARAM) as GamePackage?
            gamePackage?.also {
                fillScreen(it)
            }
        }
    }

    private fun fillScreen(gamePackage: GamePackage) {
        gamePackage.game?.also {
            it.box?.large?.let { url ->
                TopGamesGlideModule.loadImage(url, this).into(gameImage)
            }

            gameName.text = getString(R.string.name_, it.name)
            gameChannels.text = getString(R.string.channels_, gamePackage.channels)
            gameViewers.text = getString(R.string.viewers_, gamePackage.viewers)
        }
    }

    companion object {

        private const val INTENT_GAME_PARAM = "game_param"

        fun newIntent(context: Context, gamePackage: GamePackage): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(INTENT_GAME_PARAM, gamePackage)
            return intent
        }
    }
}
