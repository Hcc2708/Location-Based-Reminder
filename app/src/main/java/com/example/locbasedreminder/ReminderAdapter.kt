package com.example.locbasedreminder

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.startActivity
import androidx.core.app.ActivityCompat.startActivityForResult

//import kotlinx.android.synthetic.main.item_reminder.view.*

class ReminderAdapter(context: Context, reminders: List<Reminder>, private val onDeleteClickListener: (Int) -> Unit) :
    ArrayAdapter<Reminder>(context, 0, reminders) {
    lateinit var reminder: Reminder
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        reminder = getItem(position)!!
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val view:View = layoutInflater.inflate(R.layout.itemreminder, null)
        val txt  = view.findViewById<TextView>(R.id.titleTextView)
        txt.text = reminder.task
        val deleteButton = view.findViewById<ImageButton>(R.id.delete)
        deleteButton.setOnClickListener {
            onDeleteClickListener.invoke(position)
        }

        txt.setOnClickListener {


            val mapIntent =Intent(context, ReminderLocation::class.java)
            mapIntent.putExtra("chosen_location1", reminder.location)
            mapIntent.putExtra("task", reminder.task)
            context.startActivity(mapIntent)
        }


        return view
    }
}
