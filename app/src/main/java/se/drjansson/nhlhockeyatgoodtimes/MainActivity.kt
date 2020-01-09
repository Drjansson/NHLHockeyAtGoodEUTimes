package se.drjansson.nhlhockeyatgoodtimes

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var client = OkHttpClient()
    private var txt : TextView? = null
    private var dateFrom : EditText? = null
    private var dateTo : EditText? = null
    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        populateSpinner()
        initializeUI()
    }

    private fun populateSpinner() {

    }

    private fun initializeUI() {
        txt = findViewById(R.id.txtNextGame)
        btnMain.setOnClickListener {
            getJson()
        }

        txtStartDate.inputType = InputType.TYPE_NULL
        txtStartDate.setOnClickListener(dateClickListener)
        txtEndDate.inputType = InputType.TYPE_NULL
        txtEndDate?.setOnClickListener(dateClickListener)

        btnClearFrom.setOnClickListener {
            dateFrom?.setText("")
            startDate = ""
        }
        btnClearTo.setOnClickListener {
            dateTo?.setText("")
            endDate = ""
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        //recyclerView.adapter = MainAdapter()

    }

    private val dateClickListener = View.OnClickListener { origView ->
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH)
        val d = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, day ->
            val month = monthOfYear +1
            var monthstr = ""
            if(monthOfYear<10)
                monthstr = "0"
            // Display Selected date in textbox
            if (origView?.id == R.id.txtStartDate) {
                startDate = "$year-$monthstr$month-$day"
                Log.e("TAG",startDate)
                txtStartDate.setText("$day, $month, $year")
            } else if (origView?.id == R.id.txtEndDate) {
                endDate = "$year-$monthstr$month-$day"
                txtEndDate.setText("$day, $month, $year")
            }
        },y , m, d)

        dpd.show()

    }


    private fun getJson(){
        var baseURL = "https://statsapi.web.nhl.com/api/v1/schedule?teamId=21"
        baseURL += "&startDate=$startDate&endDate=$endDate"
        Log.e("TAG", baseURL)
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

                val format = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US
                )
                format.timeZone = TimeZone.getTimeZone("UTC")

                val datum = format.parse(time)

                Log.v("Time", "time: $time")
                val cal = format.calendar
                val tid = cal.time
                Log.v("Time", "time2: $format")


                runOnUiThread {
                    recyclerView.adapter = MainAdapter(matches)
                    txt!!.text = game
                }
            }

        })

    }

    class Matches(val totalGames: Int, val dates: List<Info>)
    class Info(val date: String, val games: List<Games>)
    class Games(val gameDate: String, val teams: Teams)
    class Teams(val away: Lag, val home: Lag )
    class Lag(val team: Sista)
    class Sista(val name: String)

}
