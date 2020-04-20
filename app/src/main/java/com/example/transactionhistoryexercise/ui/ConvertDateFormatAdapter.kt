package com.example.transactionhistoryexercise.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat

class ConvertDateFormatAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("bindServerDate")
        fun convertFormat(textView: TextView, date: String) {
            val fromServer = SimpleDateFormat("dd/MM/yyyy")
            val myFormat = SimpleDateFormat("dd MMM yyyy")
            val newDate = myFormat.format(fromServer.parse(date)!!)
            textView.text = newDate
        }
    }
}