package com.example.transactionhistoryexercise.ui

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

class LocationIconDisplayAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("displayLocation")
        fun displayLocationIcon(imageView: ImageView, description: String) {
            if (description.startsWith("Wdl")) {
                imageView.visibility = View.VISIBLE
            } else {
                imageView.visibility = View.INVISIBLE
            }
        }
    }
}