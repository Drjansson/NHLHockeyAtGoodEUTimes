package se.drjansson.nhlhockeyatgoodtimes

import android.app.DatePickerDialog
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.text.InputType
import android.util.Log
import android.view.View
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
                val pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
                val result = response.body!!.string()
                val gson = GsonBuilder().create()
                val matches = gson.fromJson(result, Matches::class.java)

                val format = SimpleDateFormat(
                    pattern, Locale.US
                )
                format.timeZone = TimeZone.getTimeZone("UTC")

                val date = matches.dates[0].date
//                val time = matches.dates[0].games[0].gameDate
                val awayTeam = matches.dates[0].games[0].teams.away.team.name
                val homeTeam = matches.dates[0].games[0].teams.home.team.name

                val nextGame = "$date $homeTeam-$awayTeam"

                for(match in matches.dates){
                    val time:String = match.games[0].gameDate
                    val dateobj = format.parse(time)
                    val calendar: Calendar = GregorianCalendar()
                    calendar.time = dateobj
                    calendar.timeZone = TimeZone.getTimeZone("Europe/Stockholm")
                    match.games[0].cal = calendar

                    /*Log.e("TAG", "år " +match.games[0].cal.get(Calendar.YEAR)  )
                    Log.e("TAG", "Mån " +match.games[0].cal.get(Calendar.MONTH)  )
                    Log.e("TAG", "Dag " +match.games[0].cal.get(Calendar.DAY_OF_MONTH)  )
                    Log.e("TAG", "Tim " +match.games[0].cal.get(Calendar.HOUR_OF_DAY)  )
                    Log.e("TAG", "Min " +match.games[0].cal.get(Calendar.MINUTE)  )*/
                    val start = match.games[0].cal.get(Calendar.HOUR_OF_DAY)
                    if(start in 11..22)
                        match.games[0].before10 = true
                }

                runOnUiThread {
                    recyclerView.adapter = MainAdapter(matches)
                    txt!!.text = nextGame
                }

            }

        })

    }

    class Matches(val totalGames: Int, val dates: List<Info>)
    class Info(val date: String, val games: List<Games>)
    class Games(val gameDate: String, val teams: Teams, var before10 : Boolean = false, var cal : Calendar)
    class Teams(val away: Lag, val home: Lag )
    class Lag(val team: Sista)
    class Sista(val name: String)

}
