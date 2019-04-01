package br.com.rossiny.topgames.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Result {

    @SerializedName("_total")
    @Expose
    var total: Int? = null

    @SerializedName("_links")
    @Expose
    var links: Links? = null

    @SerializedName("top")
    @Expose
    var top: ArrayList<GamePackage>? = null




}