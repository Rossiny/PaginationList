package br.com.rossiny.topgames.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UrlSizedImage :Serializable {

    @SerializedName("large")
    @Expose
    var large: String? = null

    @SerializedName("medium")
    @Expose
    var medium: String? = null

    @SerializedName("small")
    @Expose
    var small: String? = null

    @SerializedName("template")
    @Expose
    var template: String? = null
}