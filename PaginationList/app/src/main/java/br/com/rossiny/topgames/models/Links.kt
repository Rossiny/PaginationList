package br.com.rossiny.topgames.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Links {

    @SerializedName("self")
    @Expose
    var self: String? = null

    @SerializedName("next")
    @Expose
    var next: String? = null

}