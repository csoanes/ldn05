package com.example.model

/**
 * Created by Roberto on 24/01/2017.
 */
import java.util.*

data class EvidenceTrades(val DealNum: String, val Rate: Float,val Notional: Float)

data class RateSubmission(val submissionID: Int,
                         val Rate: Float,
                         val Volume: Float)