package fr.peaky.photographieproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.peaky.photographieproject.R


class SpinnerAdapter(context: Context, private val resource: Int, private val list: Array<String>) :
    ArrayAdapter<String>(context, resource, list) {

    private val inflater = LayoutInflater.from(context)


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return bindView(position, parent)
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = inflater.inflate(resource, parent, true)
        return bindView(position, view)
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): String? {
        return list[position]
    }

    private fun bindView(
        position: Int,
        view: View
    ): View {
        val label = view.findViewById<TextView>(R.id.spinnerLabel)
        label.text = list[position]
        return view
    }

}