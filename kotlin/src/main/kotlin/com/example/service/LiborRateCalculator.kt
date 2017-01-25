package com.example.service

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*


class LiborRateCalculator {

    val rates = HashMap<String, Float>();

    fun receiveSubmittedRate(rate: Float, bankName: String) {

        if (rates.containsKey(bankName)) {
            println("Overriding rate for " + bankName)
        }

        rates.put(bankName, rate);

        if (rates.size == 2) {
            println("kicking off calculation")

            val sum = rates.values.sum()
            val fixedRate = sum / 2;
            println("Fixed Rate: " + fixedRate)

            broadcastRate(fixedRate)
        }
    }

    private fun broadcastRate(fixedRate: Float) {

        //start fixed rate flow somehow
    }
}