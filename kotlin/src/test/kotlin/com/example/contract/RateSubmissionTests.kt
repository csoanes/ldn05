package com.example.contract

import com.example.model.*
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.Party
import net.corda.core.crypto.composite
import net.corda.core.crypto.generateKeyPair
import net.corda.core.utilities.TEST_TX_TIME
import net.corda.testing.*
import org.junit.Test
import java.security.KeyPair
import java.time.Duration
import java.util.*

class RateSubmissionTests {

    val CS_KEY: KeyPair by lazy { generateKeyPair() }
    val CS_PUBKEY: CompositeKey get() = CS_KEY.public.composite
    val CREDIT_SUISSE: Party get() = Party("Credit Suisse", CS_PUBKEY)

    val CALCULATOR_KEY: KeyPair by lazy { generateKeyPair() }
    val CALCULATOR_PUBKEY: CompositeKey get() = CALCULATOR_KEY.public.composite
    val CALCULATOR: Party get() = Party("Calculator", CALCULATOR_PUBKEY)

    @Test
    fun `transaction must be timestamped`() {
//        val address = Address("London", "UK")
//        val items = listOf(Item("Hammer", 1))
//        val deliveryTime = TEST_TX_TIME.plus(Duration.ofDays(7))arty
//        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
//        val evidence = listOf(EvidenceTrades("123", 1.3f, 200f));
        val Rate = 1.2345f
        val rateSubmission = RateSubmission(1, Rate, 1000f/*, evidence*/)
        ledger {
            transaction {
                output { RateSubmissionState(rateSubmission, CREDIT_SUISSE, CALCULATOR, Date(), "LIBOR", "EUR", RateSubmissionContract()) }
                `fails with`("must be timestamped")
                timestamp(TEST_TX_TIME)
                command(CALCULATOR_PUBKEY, CS_PUBKEY) { RateSubmissionContract.Commands.Place() }
                verifies()
            }
        }
    }

    @Test
    fun `transaction must include place command`() {
        val address = Address("London", "UK")
        val items = listOf(Item("Hammer", 1))
        val deliveryTime = TEST_TX_TIME.plus(Duration.ofDays(7))
        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
        ledger {
            transaction {
                output { PurchaseOrderState(purchaseOrder, MINI_CORP, MEGA_CORP, PurchaseOrderContract()) }
                timestamp(TEST_TX_TIME)
                `fails with`("Required com.example.contract.PurchaseOrderContract.Commands.Place command")
                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { PurchaseOrderContract.Commands.Place() }
                verifies()
            }
        }
    }

    @Test
    fun `buyer must sign transaction`() {
        val address = Address("London", "UK")
        val items = listOf(Item("Hammer", 1))
        val deliveryTime = TEST_TX_TIME.plus(Duration.ofDays(7))
        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
        ledger {
            transaction {
                output { PurchaseOrderState(purchaseOrder, MINI_CORP, MEGA_CORP, PurchaseOrderContract()) }
                timestamp(TEST_TX_TIME)
                command(MINI_CORP_PUBKEY) { PurchaseOrderContract.Commands.Place() }
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `seller must sign transaction`() {
        val address = Address("London", "UK")
        val items = listOf(Item("Hammer", 1))
        val deliveryTime = TEST_TX_TIME.plus(Duration.ofDays(7))
        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
        ledger {
            transaction {
                output { PurchaseOrderState(purchaseOrder, MINI_CORP, MEGA_CORP, PurchaseOrderContract()) }
                timestamp(TEST_TX_TIME)
                command(MEGA_CORP_PUBKEY) { PurchaseOrderContract.Commands.Place() }
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `cannot place empty orders`() {
        val address = Address("London", "UK")
        val items = emptyList<Item>()
        val deliveryTime = TEST_TX_TIME.plus(Duration.ofDays(7))
        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
        ledger {
            transaction {
                output { PurchaseOrderState(purchaseOrder, MINI_CORP, MEGA_CORP, PurchaseOrderContract()) }
                timestamp(TEST_TX_TIME)
                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { PurchaseOrderContract.Commands.Place() }
                `fails with`("must order at least one type of item")
            }
        }
    }

    @Test
    fun `cannot place historical orders`() {
        val address = Address("London", "UK")
        val items = listOf(Item("Hammer", 1))
        val deliveryTime = TEST_TX_TIME.minus(Duration.ofDays(7))
        val purchaseOrder = PurchaseOrder(1, Date(deliveryTime.toEpochMilli()), address, items)
        ledger {
            transaction {
                output { PurchaseOrderState(purchaseOrder, MINI_CORP, MEGA_CORP, PurchaseOrderContract()) }
                timestamp(TEST_TX_TIME)
                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { PurchaseOrderContract.Commands.Place() }
                `fails with`("delivery date must be in the future")
            }
        }
    }
}
