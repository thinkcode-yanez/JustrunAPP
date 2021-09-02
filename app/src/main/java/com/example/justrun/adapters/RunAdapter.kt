package com.example.justrun.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.justrun.R
import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.config.db.Run
import com.example.justrun.config.repositories.MainRepository
import com.example.justrun.databinding.ItemRunBinding
import com.example.justrun.stuff.TrackingUtility
import com.example.justrun.ui.DetailsActivity
import com.example.justrun.ui.RunsActivity
import com.example.justrun.viewmodels.MainViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter(private val dataSet: List<Run>?) :
    RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_run, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val item = dataSet?.get(position)
        viewHolder.enlazarItem(item!!)


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet!!.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemRunBinding.bind(view)
        var context = view.context
        var mainRepository = MainRepository()


        fun enlazarItem(item: Run) {
            Glide.with(itemView).load(item.img).into(binding.ivRunImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = item.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            val avgspeed = "${item.avgSpeedKmh}km/h"
            binding.tvAvgSpeed.text = avgspeed

            val distanceKm = "${item.distanceMeters / 1000f}km"
            binding.tvDistance.text = distanceKm

            binding.tvTime.text = TrackingUtility.setFormatOfStopWathch(item.timeInMillis)

            binding.ivRunImage.setOnClickListener {
                val intent = Intent(itemView.context, DetailsActivity::class.java)
                intent.putExtra("dataId", "${item.id}")
                Log.d("id", "${item.id}")
                context.startActivity(intent)


            }


        }

    }

}

