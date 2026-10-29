package com.rafi.antireelai

import com.rafi.antireelai.tracking.ServiceScanGate
import org.junit.Assert.*
import org.junit.Test

class ServiceScanGateTest {
    @Test fun notConnectedCannotAcceptAnInspection() {
        val gate=ServiceScanGate()
        assertFalse(gate.accepts(gate.ticket()))
    }
    @Test fun interruptionOrPreferenceChangeRejectsAlreadyQueuedResults() {
        val gate=ServiceScanGate();gate.connect()
        val before=gate.ticket();assertTrue(gate.accepts(before))
        gate.invalidate()
        assertFalse(gate.accepts(before));assertTrue(gate.accepts(gate.ticket()))
    }
    @Test fun unbindRejectsPendingLimitScreenAndInspectionUntilReconnect() {
        val gate=ServiceScanGate();gate.connect();val before=gate.ticket()
        gate.disconnect()
        assertFalse(gate.connected);assertFalse(gate.accepts(before));assertFalse(gate.accepts(gate.ticket()))
        gate.connect()
        assertTrue(gate.connected);assertTrue(gate.accepts(gate.ticket()));assertFalse(gate.accepts(before))
    }
    @Test fun repeatedConnectionInvalidatesThePreviousConnectionWork() {
        val gate=ServiceScanGate();gate.connect();val before=gate.ticket()
        gate.connect()
        assertFalse(gate.accepts(before));assertTrue(gate.accepts(gate.ticket()))
    }
}
