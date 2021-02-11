package com.spacestar.obit

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.cookies.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Cookie
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val progress: ProgressBar = findViewById(R.id.progress)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            button.isEnabled = false

            progress.progress = 0
            progress.visibility = View.VISIBLE

            lifecycle.coroutineScope.launch {
                val client = HttpClient(OkHttp) {
                    engine {
                        val loggingInterceptor = HttpLoggingInterceptor()
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                        addInterceptor(loggingInterceptor)
                    }

                    install(HttpCookies) {
                        storage = AcceptAllCookiesStorage()
                        loadId()?.let {
                            runBlocking {
                                storage.addCookie(
                                    "hspot.wifi.access5.obit.ru",
                                    Cookie("OBIT_SessID", it)
                                )
                            }
                        }
                    }
                }

                try {
                    var response: String = client.get("http://access.wifi.obit.ru")
                    progress.incrementProgressBy(12)
                    var address = response
                        .substringAfter("<meta http-equiv=\"refresh\" content=\"0; url=")
                        .substringBefore('"')
                    response = client.get(address)
                    progress.incrementProgressBy(15)
                    val addressAjax = "http://hspot.wifi.access5.obit.ru/obit_B2B-FOR-WIFI/" + response
                        .substringAfter("url: '")
                        .substringBefore('\'')
                    address = "http://hspot.wifi.access5.obit.ru/obit_B2B-FOR-WIFI/" + response
                        .substringAfter("<button id='connect_btn' onclick=\"window.location = '")
                        .substringBefore('\'')
                    client.get<HttpResponse>(addressAjax)
                    progress.incrementProgressBy(12)
                    response = client.get(address)
                    progress.incrementProgressBy(19)
                    address = "http://hspot.wifi.access5.obit.ru/obit_B2B-FOR-WIFI/" + response
                        .substringAfter("closeBtn.html('<a id=\"dst\" href=\"")
                        .substringBefore('"')
                    response = client.get(address)
                    progress.incrementProgressBy(29)
                    address = "http://hspot.wifi.access5.obit.ru/obit_B2B-FOR-WIFI/" + response
                        .substringAfter("<meta http-equiv=\"refresh\" content=\"")
                        .substringAfter(';')
                        .substringBefore('"')
                    client.get<HttpResponse>(address)
                    progress.incrementProgressBy(13)
                    Toast.makeText(
                        this@MainActivity,
                        "done",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveId(client.cookies("hspot.wifi.access5.obit.ru")["OBIT_SessID"]!!.value)
                } catch (e: IOException) {
                    progress.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "error",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: ClientRequestException) {
                    progress.progress = 100
                    Toast.makeText(
                        this@MainActivity,
                        "have been connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                client.close()
            }

            button.isEnabled = true
        }
    }

    private fun saveId(id: String) {
        val editor = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
        editor.putString(ID, id)
        editor.apply()
    }

    private fun loadId(): String? {
        val prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getString(ID, null)
    }

    companion object {
        private const val PREFERENCES = "MAIN_ACTIVITY_PREFERENCES"
        private const val ID = "ID"
    }
}