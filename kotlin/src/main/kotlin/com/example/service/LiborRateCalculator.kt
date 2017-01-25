package com.example.service

import java.util.*


class LiborRateCalculator() {

    val rates = HashMap<String, Float>();

    fun receiveSubmittedRate(rate: Float, bankName: String) :FixedRateResult {

        if (rates.containsKey(bankName)) {
            println("Overriding rate for " + bankName)
        }

        rates.put(bankName, rate);

        println("rates maps $rates")
        if (rates.size == 2) {
            println("kicking off calculation")

            val sum = rates.values.sum()
            val fixedRate = sum / 2;
            println("Fixed Rate: " + fixedRate)

            rates.clear()
            return FixedRateResult(true, fixedRate)
        } else {
            return FixedRateResult(false, 0f)
        }
    }

    class FixedRateResult(val isReady: Boolean, val rate: Float) {

    }
}