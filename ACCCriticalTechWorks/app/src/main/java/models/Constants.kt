package models

import java.util.*

@Suppress("UNUSED_PARAMETER")
class Constants {

    companion object {
        const val MAIN_SHARED_PREFERENCES: String = "ACCCTW"
        const val LANGUAGE_PREF_KEY: String = "language_pref"
        const val NEWS_SOURCE_CODE: String = "news_source_code"
        const val DEFAULT_SOURCE_CODE: String ="bbc-news"

        val availableLanguages = arrayOf("English", "Português")
        val availableLanguagesCodes = arrayOf("en", "pt")

        var cSelectedLanguage: String = ""
        var cSelectedNewsSourceCode: String = ""
        var cSearchBySourceCode: Boolean = true

        var availableCountrys: Array<Locale>
            get() {
                return arrayOf(Locale("ae"), Locale("ar"), Locale("at"), Locale("au"),
                    Locale("be"), Locale("bg"), Locale("br"), Locale("ca"), Locale("ch"),
                    Locale("cn"), Locale("co"), Locale("cu"), Locale("cz"), Locale("de"),
                    Locale("eg"), Locale("fr"), Locale("gb"), Locale("gr"), Locale("hk"),
                    Locale("hu"), Locale("id"), Locale("ie"), Locale("il"), Locale("in"),
                    Locale("it"), Locale("jp"), Locale("kr"), Locale("lt"), Locale("lv"),
                    Locale("ma"), Locale("mx"), Locale("my"), Locale("ng"), Locale("nl"),
                    Locale("no"), Locale("nz"), Locale("ph"), Locale("pl"), Locale("pt"),
                    Locale("ro"), Locale("rs"), Locale("ru"), Locale("sa"), Locale("se"),
                    Locale("sg"), Locale("si"), Locale("sk"), Locale("th"), Locale("tr"),
                    Locale("tw"), Locale("ua"), Locale("us"), Locale("ve"), Locale("za"))
            }
            set(value) {}
    }

}