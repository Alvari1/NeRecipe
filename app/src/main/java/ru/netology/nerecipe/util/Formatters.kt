package ru.netology.nerecipe.util

import java.text.SimpleDateFormat

object Formatters {
    fun getFormattedCounter(amount: Long) = when (amount) {
        in 0..999 -> amount.toString()
        in 1000..1099 -> (amount / 1000).toString().plus("K")
        in 1100..999999 -> {
            val temp = amount / 100
            if (temp.toDouble() % 10 > 0)
                temp.toDouble().div(10).toString().plus("K")
            else (temp / 10).toString().plus("K")
        }
        in 1000000..999999999 -> {
            val temp = amount / 100000
            if (temp.toDouble() % 10 > 0)
                temp.toDouble().div(10).toString().plus("M")
            else (temp / 10).toString().plus("M")
        }
        else -> {
            val temp = amount / 100000000
            if (temp.toDouble() % 10 > 0)
                temp.toDouble().div(10).toString().plus("B")
            else (temp / 10).toString().plus("B")
        }
    }

    fun getFormattedDate(timestamp: Long) =
        run { SimpleDateFormat("dd.MM.yyyy").format(timestamp).toString() }
}
