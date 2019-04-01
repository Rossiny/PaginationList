package br.com.rossiny.topgames.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Game : Serializable {

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("popularity")
    @Expose
    var popularity: Int? = null

    @SerializedName("_id")
    @Expose
    var id: Long? = null

    @SerializedName("giantbomb_id")
    @Expose
    var giantbombId: Long? = null

    @SerializedName("box")
    @Expose
    var box: UrlSizedImage? = null

    @SerializedName("logo")
    @Expose
    var logo: UrlSizedImage? = null

    @SerializedName("_links")
    @Expose
    var links: Any? = null

    @SerializedName("localized_name")
    @Expose
    var localizedName: String? = null

    @SerializedName("locale")
    @Expose
    var locale: String? = null

}
