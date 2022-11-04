package com.pintertamas.befake.constant

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.kishandonga.csbx.CustomSnackbar
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.CustomErrorSnackbarBinding
import com.pintertamas.befake.databinding.CustomSuccessSnackbarBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat

class Constants {
    companion object {
        fun showSuccessSnackbar(
            context: Context,
            layoutInflater: LayoutInflater,
            successMessage: String
        ) {
            val binding: CustomSuccessSnackbarBinding =
                CustomSuccessSnackbarBinding.inflate(layoutInflater)
            binding.message.text = successMessage
            val sb = CustomSnackbar(context)
            sb.customView(binding.root)
            sb.message(successMessage)
            sb.duration(Toast.LENGTH_SHORT)
            sb.show()
        }

        fun showErrorSnackbar(
            context: Context,
            layoutInflater: LayoutInflater,
            errorMessage: String
        ) {
            val binding: CustomErrorSnackbarBinding =
                CustomErrorSnackbarBinding.inflate(layoutInflater)
            binding.message.text = errorMessage
            CustomSnackbar(context).show {
                customView(binding.root)
                message(errorMessage)
                duration(Toast.LENGTH_SHORT)
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun convertStringToTimestamp(beFakeTimeString: String): Timestamp {
            val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            val sdf = SimpleDateFormat(pattern)
            val parsedDate = sdf.parse(beFakeTimeString)
            return Timestamp(parsedDate?.time ?: 0)
        }
    }
}