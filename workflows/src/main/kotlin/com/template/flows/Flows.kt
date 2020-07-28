package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class EchoInitiator(val message: String, val counterParty: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val party = serviceHub.identityService.partiesFromName(counterParty, false).single()
        val counterPartySession = initiateFlow(party)
        counterPartySession.send(message)
        System.out.println("sent message: \"$message\" to ${party.name}")
        val reverse = counterPartySession.receive<String>().unwrap { it }
        System.out.println("received reverse echo: \"$reverse\"")
        return reverse
    }
}

@InitiatedBy(EchoInitiator::class)
class EchoResponder(val counterPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val message = counterPartySession.receive<String>().unwrap { it }
        System.out.println("received message \"$message\" from ${counterPartySession.counterparty.name}")
        val reverse = message.reversed()
        counterPartySession.send(reverse)
        System.out.println("returned reverse \"$reverse\"")
    }
}
