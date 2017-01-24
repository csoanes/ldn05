package com.example.api

import com.example.contract.RateSubmissionContract
import com.example.contract.RateSubmissionState
import com.example.flow.RateSubmissionFlow
import com.example.flow.RateSubmissionFlowResult
import com.example.model.RateSubmission
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

//val NOTARY_NAME = "Controller"

// This API is accessible from /api/example. All paths specified below are relative to it.
@Path("rate")
class RateSubmissionApi(val services: CordaRPCOps) {
    val myLegalName: String = services.nodeIdentity().legalIdentity.name

    /**
     * Returns the party name of the node providing this end-point.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService], the names can be used to look-up identities
     * by using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers() = mapOf("peers" to services.networkMapUpdates().first
            .map { it.legalIdentity.name }
            .filter { it != myLegalName && it != NOTARY_NAME })

    /**
     * Displays all purchase order states that exist in the vault.
     */
    @GET
    @Path("submitted-rates")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSubmittedRates() = services.vaultAndUpdates().first

    /**
     * This should only be called from the 'buyer' node. It initiates a flow to agree a purchase order with a
     * seller. Once the flow finishes it will have written the purchase order to ledger. Both the buyer and the
     * seller will be able to see it when calling /api/example/purchase-orders on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */
    @PUT
    @Path("{party}/submit-rate")
    fun createRateSubmission(rateSubmission: RateSubmission, @PathParam("party") partyName: String): Response {
        val otherParty = services.partyFromName(partyName)
        if (otherParty == null) {
            return Response.status(Response.Status.BAD_REQUEST).build()
        }

        val state = RateSubmissionState(
                rateSubmission,
                services.nodeIdentity().legalIdentity,
                otherParty,
                Date(),
                "LIBOR",
                "EUR",
                RateSubmissionContract())

        // The line below blocks and waits for the future to resolve.
        val result: RateSubmissionFlowResult = services
                .startFlow(RateSubmissionFlow::Initiator, state, otherParty)
                .returnValue
                .toBlocking()
                .first()

        when (result) {
            is RateSubmissionFlowResult.Success ->
                return Response
                        .status(Response.Status.CREATED)
                        .entity(result.message)
                        .build()
            is RateSubmissionFlowResult.Failure ->
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(result.message)
                        .build()
        }
    }
}