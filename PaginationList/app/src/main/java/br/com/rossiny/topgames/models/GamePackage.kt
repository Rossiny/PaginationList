package br.com.rossiny.topgames.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GamePackage : Serializable {

    @SerializedName("game")
    @Expose
    var game: Game? = null

    @SerializedName("viewers")
    @Expose
    var viewers: Int? = null

    @SerializedName("channels")
    @Expose
    var channels: Int? = null
}