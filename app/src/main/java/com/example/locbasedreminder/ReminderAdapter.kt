package com.example.locbasedreminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

//import kotlinx.android.synthetic.main.item_reminder.view.*

class ReminderAdapter(context: Context, reminders: List<Reminder>) :
    ArrayAdapter<Reminder>(context, 0, reminders) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val reminder : Reminder? = getItem(position)
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val view:View = layoutInflater.inflate(R.layout.itemreminder, null)
        val txt  = view.findViewById<TextView>(R.id.titleTextView)
        txt.text = reminder?.title
        return view
    }
}
