package logic

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import models.Constants
import models.Constants.Companion.LANGUAGE_PREF_KEY
import models.Constants.Companion.MAIN_SHARED_PREFERENCES
import models.Constants.Companion.availableLanguagesCodes
import java.util.*

@Suppress("DEPRECATION")
class Core {

    companion object {
        @JvmStatic
        fun setLanguage(context: Context, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                restartIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(restartIntent)
            }, 100)
        }

        fun checkAndSetLanguage(pContext: Context){
            val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pContext.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                pContext.resources.configuration.locale
            }
            val locateCode = currentLocale.language
            if(availableLanguagesCodes.contains(locateCode) && languageIsDifrentFromPrevious(pContext, locateCode))
                setLanguage(pContext, locateCode)
        }

        private fun languageIsDifrentFromPrevious(context: Context, languageCode: String): Boolean {
            var sPreferences: SharedPreferences = context.getSharedPreferences(MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val savedLanguageCode = sPreferences.getString(LANGUAGE_PREF_KEY, availableLanguagesCodes[0])
            if(savedLanguageCode.equals(languageCode))
                return false
            else {
                val editor = sPreferences.edit()
                editor.putString(LANGUAGE_PREF_KEY, languageCode)
                editor.apply()
                Constants.cSelectedLanguage = languageCode
                return true;
            }
        }
    }
}