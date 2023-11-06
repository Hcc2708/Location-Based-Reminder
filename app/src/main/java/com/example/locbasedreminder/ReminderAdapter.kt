package com.example.locbasedreminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView


class ReminderAdapter(context: Context, reminders: List<Reminder>, private val onDeleteClickListener: (Int) -> Unit) :
    ArrayAdapter<Reminder>(context, 0, reminders) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val reminder : Reminder? = getItem(position)
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        if (reminder == null) {
            val noRemindersView = layoutInflater.inflate(R.layout.item_no_reminder, null)
            return noRemindersView
        }
        val view:View = layoutInflater.inflate(R.layout.itemreminder, null)
        val txt  = view.findViewById<TextView>(R.id.titleTextView)
        txt.text = reminder?.task
        val deleteButton = view.findViewById<Button>(R.id.delete)
        deleteButton.setOnClickListener {
            onDeleteClickListener.invoke(position)
        }
        view.setOnClickListener{

        }
        return view
    }

}
