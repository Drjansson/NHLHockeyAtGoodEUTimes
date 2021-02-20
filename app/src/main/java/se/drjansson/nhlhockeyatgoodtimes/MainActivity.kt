package se.drjansson.nhlhockeyatgoodtimes

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val TEAM_LIST = "team_list"
    private val TEAM_LIST_CACHE = "team_list_cache"
    private var client = OkHttpClient()
    private val gson = GsonBuilder().create()
    private var txt: TextView? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var today: Date? = null
    private var selectedTeamID: Int = 0
    private lateinit var aa: ArrayAdapter<Team>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH)
        val d = c.get(Calendar.DAY_OF_MONTH)
        today = Date(y,m,d)

        populateSpinner()
        initializeUI()
    }

    private fun populateSpinner() {
        val sharedPrefs = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedString = sharedPrefs.getString(TEAM_LIST, null)
        var resetCache = sharedPrefs.getInt(TEAM_LIST_CACHE, 10)

        spinTeamSelect.onItemSelectedListener = spinSelectedListener

        if(savedString != null && resetCache < 10) {
            val listOfTeams: GetTeams
            try {
                listOfTeams = gson.fromJson(savedString, GetTeams::class.java)
            } catch (e : Exception){
                getTeams()
                return
            }
            setSpinnerItems(listOfTeams.teams)

            //TODO: Make this changeable in the future.
            setPreChosenTeam("Colorado Avalanche")

            resetCache++
            val editor = sharedPrefs.edit()
            editor.putInt(TEAM_LIST_CACHE, resetCache)
            editor.apply()
        } else
            getTeams()

    }

    private fun setSpinnerItems(spinnerList: List<Team>) {
        // Create an ArrayAdapter using a simple spinner layout and languages array
        aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        spinTeamSelect!!.adapter = aa

    }

    private fun setPreChosenTeam(team: String){
        var j = 0
        for (i in 0..spinTeamSelect.count){
            if(spinTeamSelect.getItemAtPosition(i).toString().equals(team, true)) {
                j = i
                break
            }
        }
        spinTeamSelect.setSelection(j)
    }

    private fun initializeUI() {
        txt = findViewById(R.id.txtNextGame)
        btnMain.setOnClickListener {
            getGameInfo()
        }

        txtStartDate.inputType = InputType.TYPE_NULL
        txtStartDate.setOnClickListener(dateClickListener)
        txtEndDate.inputType = InputType.TYPE_NULL
        txtEndDate?.setOnClickListener(dateClickListener)

        btnClearFrom.setOnClickListener {
            txtStartDate?.setText("")
            startDate = null
        }
        btnClearTo.setOnClickListener {
            txtEndDate?.setText("")
            endDate = null
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private val dateClickListener = View.OnClickListener { origView ->
        val y:Int; val m:Int; val d:Int
        if (origView?.id == R.id.txtStartDate && startDate != null) {
            y= startDate!!.year
            m= startDate!!.month-1
            d= startDate!!.day

        }else if (origView?.id == R.id.txtEndDate && endDate != null){
            y= endDate!!.year
            m= endDate!!.month-1
            d= endDate!!.day
        }else {
            y= today!!.year
            m= today!!.month
            d= today!!.day
        }


        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, day ->
            val month = monthOfYear +1
            // Display Selected date in textbox
            if (origView?.id == R.id.txtStartDate) {
                startDate = Date(year,month,day)
                txtStartDate.setText(startDate.toString())
            } else if (origView?.id == R.id.txtEndDate) {
                endDate = Date(year,month,day)
                txtEndDate.setText(endDate.toString())
            }
        },y , m, d)

        dpd.show()

    }

    private val spinSelectedListener = object: OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            selectedTeamID = aa.getItem(position)?.id ?: 0
        }
        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    private fun getGameInfo(){
        var baseURL = "https://statsapi.web.nhl.com/api/v1/schedule?"
        baseURL += "teamId="+selectedTeamID+"&startDate="+startDate.toString()+"&endDate="+endDate.toString()
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

                val nextGame = "$date $homeTeam - $awayTeam"

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
                    val earliestStartTime = 11
                    val latestStartTime = 22
                    if(start in earliestStartTime..latestStartTime)
                        match.games[0].earlyStartTime = true
                }

                runOnUiThread {
                    recyclerView.adapter = MainAdapter(matches)
                    txt!!.text = nextGame
                }

            }

        })

    }
    private fun getTeams() : List<Team>{
        val baseURL = "https://statsapi.web.nhl.com/api/v1/teams"
        Log.e("TAG", baseURL)
        val request = Request.Builder().url(baseURL).build()

        var receivedTeams: List<Team> = ArrayList()

        client.newCall(request).
            enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "Could not fetch TEAMS")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                val gson = GsonBuilder().create()
                val teams = gson.fromJson(result, GetTeams::class.java)
                receivedTeams = teams.teams

                val sharedPrefs = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()
                editor.putString(TEAM_LIST, gson.toJson(receivedTeams))
                editor.putInt(TEAM_LIST_CACHE, 0)
                editor.apply()

                runOnUiThread {
                    setSpinnerItems(receivedTeams)
                }
            }
        })
        return receivedTeams
    }




    class GetTeams(val teams: List<Team>)
    class Team(val name: String, val id: Int){
        override fun toString(): String {
            return name
        }
    }

    class Matches(val totalGames: Int, val dates: List<Info>)
    class Info(val date: String, val games: List<Games>)
    class Games(val gameDate: String, val teams: Teams, var earlyStartTime : Boolean = false, var cal : Calendar)
    class Teams(val away: Lag, val home: Lag )
    class Lag(val team: Name)
    class Name(val name: String)

    class Date(var year: Int, var month: Int, var day: Int){
        override fun toString(): String {
            var monthstr = ""
            if(month<10)
                monthstr = "0"
            return "$year-$monthstr$month-$day"
        }
    }

}
