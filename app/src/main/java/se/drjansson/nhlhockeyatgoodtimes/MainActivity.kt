package se.drjansson.nhlhockeyatgoodtimes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    val baseURL = "https://statsapi.web.nhl.com/api/v1/schedule?teamId=21&startDate=2020-01-18&endDate=2020-01-31"
    private var client = OkHttpClient()
    private var txt : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnMain)
        var txt = findViewById<TextView>(R.id.textView)

        btn.setOnClickListener {
            GlobalScope.launch{
                networkCallMain()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private suspend fun networkCallMain() {
        val result = networkCallHelper()
        withContext(Dispatchers.Main) {
            Log.i("LOG", result)

            val gson = GsonBuilder().create()
            val matches = gson.fromJson(result, Matches::class.java)

            txt!!.text = result
        }
    }

    private suspend fun networkCallHelper(): String {
        return withContext(Dispatchers.Default) {
            val request = Request.Builder()
                .url(baseURL)
                .build()

            val response = client.newCall(request).execute()
            return@withContext response.body!!.string()
        }
    }

    class Matches(val dates: List<Info>)

    class Info(val date: String, val totalItems: Int)

}
