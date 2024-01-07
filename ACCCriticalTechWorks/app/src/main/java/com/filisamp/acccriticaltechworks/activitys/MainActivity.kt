package com.filisamp.acccriticaltechworks.activitys

import adapter.NewsAdapter
import adapter.PopupAdapter
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.filisamp.acccriticaltechworks.R
import kotlinx.coroutines.*
import logic.Core.Companion.checkAndSetLanguage
import logic.RetrofitHelper.NewsApiClient.fetchNewsForAsync
import logic.RetrofitHelper.NewsApiClient.fetchNewsForCountryAsync
import logic.RetrofitHelper.NewsApiClient.fetchNewsForSourceAsync
import logic.RetrofitHelper.NewsApiClient.fetchSourcesAsync
import models.Article
import models.Constants
import models.Constants.Companion.availableLanguagesCodes
import models.Constants.Companion.cSelectedLanguage


class MainActivity : AppCompatActivity() {
    //region vars
    private lateinit var mContext: Context

    private lateinit var mPbNews: ProgressBar
    private lateinit var mRvNewsArea: RecyclerView
    private lateinit var adapter: NewsAdapter

    private lateinit var mSPreferences: SharedPreferences
    private lateinit var mEditor: SharedPreferences.Editor

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val sourcesIdsList = mutableListOf<String>()
    private val sourcesNamesList = mutableListOf<String>()

    private lateinit var biometricPrompt: BiometricPrompt
    private val BIOMETRIC_PERMISSION_REQUEST_CODE = 100
    var haveBiometricPermission:Boolean = false
    //end region

    //region override system
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        mRvNewsArea = findViewById(R.id.rv_main_news)
        mPbNews = findViewById(R.id.pb_main_news)

        checkBiometricPermission()
    }

    override fun onResume() {
        checkAndSetLanguage(mContext)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        if (menu != null) {
            MenuCompat.setGroupDividerEnabled(menu, true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val view: View
        return when (item.itemId) {
            R.id.action_setSource -> {
                if(sourcesNamesList.size>0) {
                    showPopup(mRvNewsArea, sourcesNamesList, R.id.action_setSource)
                } else {
                    Toast.makeText(this, "No sources on app. Try reset the app to update source list.", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.action_setCountry -> {
                showPopup(mRvNewsArea, Constants.availableCountrys.map { it.displayLanguage }, R.id.action_setCountry)
                true
            }
            R.id.action_freeSearch -> {
                showInputDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BIOMETRIC_PERMISSION_REQUEST_CODE -> {
                haveBiometricPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    //end region

    //region methods
    private fun checkBiometricPermission() {
        val biometricPermission = Manifest.permission.USE_BIOMETRIC
        val permissionResult = ContextCompat.checkSelfPermission(this, biometricPermission)
        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            if(haveBiometricPermission && isOnline()){
                checkBiometricAvailability()
            } else if(isOnline()){
                initialize()
            } else {
                Toast.makeText(this, "No internet permission.", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(biometricPermission), BIOMETRIC_PERMISSION_REQUEST_CODE)
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                fingerPrint()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "No biometric hardware available", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware unavailable", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometric enrolled. Please check settings", Toast.LENGTH_SHORT).show()
            }
        }
        if(isOnline()){
            initialize()
        } else {
            finish()
        }
    }

    private fun fingerPrint(){
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if(isOnline())
                    initialize()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Title")
            .setSubtitle("Authenticate using your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun initialize(){
        mSPreferences = mContext.getSharedPreferences(Constants.MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        mEditor = mSPreferences.edit()
        Constants.cSelectedNewsSourceCode = mSPreferences.getString(Constants.NEWS_SOURCE_CODE, Constants.DEFAULT_SOURCE_CODE).toString()

        supportActionBar?.title = Constants.cSelectedNewsSourceCode.uppercase()

        adapter = NewsAdapter(arrayListOf(), mContext)
        mRvNewsArea.adapter = adapter
        adapter.notifyDataSetChanged()

        mPbNews.visibility = View.VISIBLE

        if(cSelectedLanguage.isNullOrEmpty())
            cSelectedLanguage = availableLanguagesCodes[0]

        coroutineScope.launch {
            runBlocking {
                getSourcesAsync()
                val preferSourceCode = sourcesIdsList.indexOf(Constants.cSelectedNewsSourceCode)
                getNewsForSourceAsync(preferSourceCode)
            }
            mPbNews.visibility = View.GONE
        }

        val callbackBackPress = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedAction()
            }
        }
        onBackPressedDispatcher.addCallback(mContext as MainActivity, callbackBackPress)
    }

    private suspend fun getSourcesAsync(){
        val sources = fetchSourcesAsync()
        sources?.sources?.forEach { source ->
            sourcesIdsList.add(source.id)
            sourcesNamesList.add(source.name)
        }
    }

    private suspend fun getNewsForSourceAsync(pIndex: Int){
        if(sourcesIdsList.size > 0){
            val newsFromSource = fetchNewsForSourceAsync(sourcesIdsList[pIndex])
            if (newsFromSource == null) {
                runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
            }
            newsFromSource?.articles?.let { articles ->
                if(articles.isNullOrEmpty()){
                    runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
                }
                else{
                    updateAdapter(articles)
                }
            }
        } else {
            clearAdapter()
            runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
        }
    }

    private suspend fun getNewsForCountryAsync(pCountry: String){
        val newsFromCountry = fetchNewsForCountryAsync(pCountry)
        if (newsFromCountry == null) {
            runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
        }
        newsFromCountry?.articles?.let { articles ->
            if(articles.isNullOrEmpty()){
                runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
            }
            else{
                updateAdapter(articles)
            }
        }
    }

    private suspend fun getNewsForAsync(pText: String){
        val newsFromCountry = fetchNewsForAsync(pText)
        if (newsFromCountry == null) {
            runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
        }
        newsFromCountry?.articles?.let { articles ->
            if(articles.isNullOrEmpty()){
                runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.news_no_news_fetched), Toast.LENGTH_SHORT).show() }
            }
            else{
                updateAdapter(articles)
            }
        }
    }

    private fun clearAdapter(){
        runOnUiThread {
            supportActionBar?.title = getString(R.string.app_name)
            adapter.clearData()
        }
    }

    private fun updateAdapter(pArticles:List<Article>){
        runOnUiThread {
            if (Constants.cSearchBySourceCode)
                supportActionBar?.title = Constants.cSelectedNewsSourceCode.uppercase()
            else
                supportActionBar?.title = getString(R.string.app_name)

            val sortedArticles = pArticles.sortedByDescending { it.publishedAt }
            if(adapter == null) {
                adapter = NewsAdapter(sortedArticles, mContext)
                mRvNewsArea.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                adapter.applyNewData(pArticles)
            }

            mRvNewsArea.scrollToPosition(0)
        }
    }

    private fun showPopup(view: View, itemList: List<String>, action: Int) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)

        val displayMetrics = resources.displayMetrics
        val isPortrait = displayMetrics.heightPixels > displayMetrics.widthPixels

        val width = if (isPortrait) {
            (displayMetrics.widthPixels * 0.8).toInt()
        } else {
            (displayMetrics.heightPixels * 0.6).toInt()
        }

        val height = if (isPortrait) {
            (displayMetrics.heightPixels * 0.8).toInt()
        } else {
            (displayMetrics.widthPixels * 0.8).toInt()
        }

        val popupWindow = PopupWindow(popupView, width, height, true)

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.popupRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val adapter = PopupAdapter(itemList, object : PopupAdapter.OnItemClickListener {
            override fun onItemClick(item: Int) {
                clearAdapter()
                mPbNews.visibility = View.VISIBLE
                when(action){
                    R.id.action_setSource -> {
                        coroutineScope.launch {
                            runBlocking {
                                if(sourcesIdsList[item] != Constants.cSelectedNewsSourceCode){
                                    mEditor.putString(Constants.NEWS_SOURCE_CODE, sourcesIdsList[item])
                                    mEditor.apply()
                                    Constants.cSelectedNewsSourceCode = sourcesIdsList[item]
                                }
                                Constants.cSearchBySourceCode = true
                                getNewsForSourceAsync(item)
                            }
                            mPbNews.visibility = View.GONE
                        }
                    }
                    R.id.action_setCountry -> {
                        coroutineScope.launch {
                            runBlocking {
                                Constants.cSearchBySourceCode = false
                                getNewsForCountryAsync(Constants.availableCountrys[item].language)
                            }
                            mPbNews.visibility = View.GONE
                        }
                    }
                }
                popupWindow.dismiss()
            }
        })
        recyclerView.adapter = adapter

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(getString(R.string.news_popup_search_text))

        val input = EditText(mContext)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.news_popup_search)) { dialog: DialogInterface, _: Int ->
            clearAdapter()
            mPbNews.visibility = View.VISIBLE
            coroutineScope.launch {
                runBlocking {
                    Constants.cSearchBySourceCode = false
                    getNewsForAsync(input.text.toString())
                }
                mPbNews.visibility = View.GONE
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.app_cancel)) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun onBackPressedAction() {
        AlertDialog.Builder(mContext)
            .setTitle(getString(R.string.app_exit_title))
            .setMessage(getString(R.string.app_exit_description))
            .setPositiveButton(getString(R.string.app_yes)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.app_no), null)
            .show()
    }
    //end region
}