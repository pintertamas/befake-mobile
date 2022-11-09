package com.pintertamas.befake.service

import com.pintertamas.befake.constant.Constants
import java.sql.Timestamp

class Functions {
    companion object {
        fun calculateLateness(time1: String, time2: String): String {
            return calculateTimePassed(time1, time2) + " late"
        }

        fun calculateTimeAgo(time1: String): String {
            val now = Timestamp(System.currentTimeMillis())
            val time = Constants.convertStringToTimestamp(time1)
            return timeStampDiff(time, now) + " ago"
        }

        private fun calculateTimePassed(time1: String, time2: String): String {
            val beFakeTime: Timestamp = Constants.convertStringToTimestamp(time1)
            val postingTime: Timestamp = Constants.convertStringToTimestamp(time2)
            return timeStampDiff(beFakeTime, postingTime)
        }

        private fun timeStampDiff(earlier: Timestamp, later: Timestamp): String {
            val diff: Long = later.time - earlier.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return if (days > 1) "$days days"
            else if (days > 0) "$days day"
            else if (hours > 1) "$hours hours"
            else if (hours > 0) "$hours hour"
            else if (minutes > 1) "$minutes minutes"
            else if (minutes > 0) "$minutes minute"
            else if (seconds > 1) "$seconds seconds"
            else "$seconds second"
        }
    }
}