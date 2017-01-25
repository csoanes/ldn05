package com.example.plugin

import com.esotericsoftware.kryo.Kryo
import com.example.api.RateSubmissionApi
import com.example.contract.RateSubmissionContract
import com.example.contract.RateSubmissionState
import com.example.flow.RateSubmissionFlow
import com.example.flow.RateSubmissionFlowResult
//import com.example.model.EvidenceTrades
import com.example.model.RateSubmission
import com.example.service.RateSubmissionService
import net.corda.core.crypto.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.node.PluginServiceHub
import java.util.*
import java.util.function.Function

class RateSubmissionPlugin : CordaPluginRegistry() {
    /** A list of classes that expose web APIs. */
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::RateSubmissionApi))
    /**
     * A list of flows required for this CorDapp.
     *
     * Any flow which is invoked from from the web API needs to be registered as an entry into this Map. The Map
     * takes the form of:
     *
     *      Name of the flow to be invoked -> Set of the parameter types passed into the flow.
     *
     * E.g. In the case of this CorDapp:
     *
     *      "ExampleFlow.Initiator" -> Set(PurchaseOrderState, Party)
     *
     * This map also acts as a white list. Such that, if a flow is invoked via the API and not registered correctly
     * here, then the flow state machine will _not_ invoke the flow. Instead, an exception will be raised.
     */
    override val requiredFlows: Map<String, Set<String>> = mapOf(
            RateSubmissionFlow.Initiator::class.java.name to setOf(RateSubmissionState::class.java.name, Party::class.java.name)
    )
    /**
     * A list of long lived services to be hosted within the node. Typically you would use these to register flow
     * factories that would be used when an initiating party attempts to communicate with our node using a particular
     * flow. See the [ExampleService.Service] class for an implementation which sets up a
     */
    override val servicePlugins: List<Function<PluginServiceHub, out Any>> = listOf(Function(RateSubmissionService::Service))
    /** A list of directories in the resources directory that will be served by Jetty under /web */
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the exampleWeb directory in resources to /web/example
            "rate" to javaClass.classLoader.getResource("rateWeb").toExternalForm()
    )

    /**
     * Register required types with Kryo (our serialisation framework).
     */
    override fun registerRPCKryoTypes(kryo: Kryo): Boolean {
        kryo.register(RateSubmissionState::class.java)
        kryo.register(RateSubmissionContract::class.java)
        kryo.register(RateSubmission::class.java)
 //       kryo.register(EvidenceTrades::class.java)
        kryo.register(Date::class.java)
        kryo.register(RateSubmissionFlowResult.Success::class.java)
        kryo.register(RateSubmissionFlowResult.Failure::class.java)
        return true
    }
}
