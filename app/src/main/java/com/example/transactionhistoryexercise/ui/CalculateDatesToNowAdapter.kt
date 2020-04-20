package com.example.transactionhistoryexercise.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.transactionhistoryexercise.R
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat

class CalculateDatesToNowAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("calculateServerDate")
        fun calculateDates(textView: TextView, date: String) {
            val fromServer = DateTimeFormat.forPattern(textView.context.getString(R.string.ddMMyyy_format))
            val dateFromServer = fromServer.parseDateTime(date)
            val days = Days.daysBetween(dateFromServer.toLocalDate(), DateTime().toLocalDate()).days
            if (days == 0) textView.text = textView.context.getString(R.string.now) else textView.text =
                textView.context.getString(R.string.days_ago, days)
        }
    }
}

