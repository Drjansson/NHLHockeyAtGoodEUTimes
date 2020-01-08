package se.drjansson.nhlhockeyatgoodtimes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    val baseURL = "https://statsapi.web.nhl.com/api/v1/schedule?teamId=21&startDate=2020-01-18&endDate=2020-01-31"
    private var client = OkHttpClient()
    private var txt : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnMain)
        txt = findViewById(R.id.textView)

        btn.setOnClickListener {
            getJson()
        }

    }

    private fun getJson(){
       val request = Request.Builder().url(baseURL).build()

       client.newCall(request).enqueue(object: Callback{
          override fun onFailure(call: Call, e: IOException) {
             Log.e("TAG", "Could not fetch JSON")
          }

          override fun onResponse(call: Call, response: Response) {
             val result = response.body!!.string()
             val gson = GsonBuilder().create()
             val matches = gson.fromJson(result, Matches::class.java)
             val date:String = matches.dates[0].date
             val time:String = matches.dates[0].games[0].gameDate
             val awayTeam:String = matches.dates[0].games[0].teams.away.team.name
             val homeTeam:String = matches.dates[0].games[0].teams.home.team.name

             val game = "$date $homeTeam-$awayTeam"

             runOnUiThread {
                txt!!.text = game
             }
          }

       })

    }


    class Matches(val dates: List<Info>)

    class Info(val date: String, val games: List<Games>)

    class Games(val gameDate: String, val teams: Teams)

    class Teams(val away: Lag, val home: Lag )

    class Lag(val team: Sista)
    class Sista(val name: String)

}
