package se.drjansson.nhlhockeyatgoodtimes

import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.match_row.view.*
import java.util.*

class MainAdapter( val matches: MainActivity.Matches) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.match_row, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.view.setBackgroundColor(Color.WHITE)
        val awayTeam = matches.dates[position].games[0].teams.away.team.name
        val homeTeam = matches.dates[position].games[0].teams.home.team.name
        //val date = matches.dates[position].date
//        val time = matches.dates[position].games[0].gameDate
        val date = "" + matches.dates[position].games[0].cal.get(Calendar.YEAR) + "-"+
                "" + (matches.dates[position].games[0].cal.get(Calendar.MONTH)+1) + "-" +
                "" + matches.dates[position].games[0].cal.get(Calendar.DAY_OF_MONTH)
        val minute = matches.dates[position].games[0].cal.get(Calendar.MINUTE)
        val time = "" + matches.dates[position].games[0].cal.get(Calendar.HOUR_OF_DAY) + ":" +
                if (minute < 10 ) "0$minute" else minute
        holder.view.txtHomeTeam.text = String.format("{} - {}", homeTeam, awayTeam)
        holder.view.txtDate.setText(String.format("{} \t {}", date, time))

        if(matches.dates[position].games[0].earlyStartTime)
            holder.view.setBackgroundColor(Color.GREEN)

        holder.view.txtDate.inputType = InputType.TYPE_NULL
    }
    override fun getItemCount(): Int {
        return matches.totalGames
    }
}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)