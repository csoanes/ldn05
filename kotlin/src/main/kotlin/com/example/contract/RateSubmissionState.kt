package com.example.contract

import com.example.contract.PurchaseOrderContract.Commands
import com.example.model.RateSubmission
import com.example.schema.RateSubmissionSchemaV1
import net.corda.core.contracts.Command
import net.corda.core.contracts.DealState
import net.corda.core.contracts.TransactionType
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey
import java.util.*

/**
 * The state object which we will use the record the agreement of a valid purchase order issued by a buyer to a seller.
 *
 * There are a few key state interfaces. The most fundamental of which is [ContractState]. We have defined other
 * interfaces for different requirements. In this case we are implementing a [DealState] which defines a few helper
 * properties and methods for managing states pertaining to deals.
 *
 * @param po details of the purchase order
 * @param buyer the party issuing the purchase order
 * @param seller the party receiving and approving the purchase order
 * @param contract a reference to the contract code which governs how this state object can behave given particular
 * transaction types.
 * @param linearId Unique id shared by all [LinearState] states throughout history within the vaults of all parties.
 */
data class RateSubmissionState(val rateSubmission: RateSubmission,
                              val Submitter: Party,
                              val Calculator: Party,
                               val RateDate: Date,
                               val RateType: String,
                               val Currency: String,
                              override val contract: RateSubmissionContract,
                              override val linearId: UniqueIdentifier = UniqueIdentifier(rateSubmission.submissionID.toString())):
        DealState, QueryableState {
    /** Another ref field, for matching with data in external systems. In this case the external Id is the po number. */
    override val ref: String get() = linearId.externalId!!
    /** List of parties involved in this particular deal */
    override val parties: List<Party> get() = listOf(Submitter, Calculator)
    /** The public keys of the parties that are able to consume this state in a valid transaction. */
    override val participants: List<CompositeKey> get() = parties.map { it.owningKey }

    /**
     * This returns true if the state should be tracked by the vault of a particular node. In this case the logic is
     * simple; track this state if we are one of the involved parties.
     */
    override fun isRelevant(ourKeys: Set<PublicKey>): Boolean {
        val partyKeys = parties.flatMap { it.owningKey.keys }
        return ourKeys.intersect(partyKeys).isNotEmpty()
    }

    /**
     * Helper function to generate a new Issue() purchase order transaction. For more details on building transactions
     * see the API for [TransactionBuilder] in the JavaDocs.
     *
     * https://docs.corda.net/api/net.corda.core.transactions/-transaction-builder/index.html
     * */
    override fun generateAgreement(notary: Party): TransactionBuilder {
        return TransactionType.General.Builder(notary)
                .withItems(this, Command(Commands.Place(), participants))
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        // TODO: Deal with the one to many relationship between POs and Items.
        return when (schema) {
            is RateSubmissionSchemaV1 -> RateSubmissionSchemaV1.PersistentRateSubmissionSchema(
                    rateSubmissionId = this.rateSubmission.submissionID,
                    submitterName = this.Submitter.name,
                    calculatorName = this.Calculator.name,
                    rate = rateSubmission.Rate,
                    rateDate = this.RateDate,
                    rateType = this.RateType,
                    currency = this.Currency,
                    evidenceTrades = ""
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RateSubmissionSchemaV1)
}
