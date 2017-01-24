package com.example.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RateSubmissionSchema

// TODO: Add schema for purchase order items.
object RateSubmissionSchemaV1 : MappedSchema(
        schemaFamily = RateSubmissionSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentRateSubmissionSchema::class.java)) {
    @Entity
    @Table(name = "RateSubmission_order_states")
    class PersistentRateSubmissionSchema(
            @Column(name = "ratesubmission_id")
            var rateSubmissionId: Int,

            @Column(name = "submitter_name")
            var submitterName: String,

            @Column(name = "calculator_name")
            var calculatorName: String,

            @Column(name = "rate")
            var rate: Float,

            @Column(name = "rate_date")
            var rateDate: Date,

            @Column(name = "rateType")
            var rateType: String,

            @Column(name = "currency")
            var currency: String,

            @Column(name = "evidenceTrades")
            var evidenceTrades: String

    ) : PersistentState()
}