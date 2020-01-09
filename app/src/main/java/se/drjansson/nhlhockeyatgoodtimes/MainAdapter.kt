package se.drjansson.nhlhockeyatgoodtimes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.match_row.view.*

class MainAdapter( val matches: MainActivity.Matches) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.match_row, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val awayTeam:String = matches.dates[position].games[0].teams.away.team.name
        val homeTeam:String = matches.dates[position].games[0].teams.home.team.name
        val date:String = matches.dates[position].date
        val time:String = matches.dates[position].games[0].gameDate
        holder.view.txtHomeTeam.text = homeTeam
        holder.view.txtAwayTeam.text = awayTeam
        holder.view.txtDate.setText(date)
        holder.view.txtTime.setText(time)
        holder.view.setBackgroundColor(Color.GREEN)
    }
    override fun getItemCount(): Int {
        return matches.totalGames
    }
}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view){

}