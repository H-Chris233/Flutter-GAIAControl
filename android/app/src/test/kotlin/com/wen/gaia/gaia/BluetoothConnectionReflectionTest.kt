package com.wen.gaia.gaia

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothConnectionReflectionTest {
    private class ConnectedTarget {
        @Suppress("unused")
        fun isConnected(): Boolean = true
    }

    private class DisconnectedTarget {
        @Suppress("unused")
        fun isConnected(): Boolean = false
    }

    private class MissingMethodTarget

    private class WrongReturnTypeTarget {
        @Suppress("unused")
        fun isConnected(): String = "yes"
    }

    @Test
    fun `returns true when reflection target reports connected`() {
        assertTrue(BluetoothConnectionReflection.isConnected(ConnectedTarget()))
    }

    @Test
    fun `returns false when reflection target reports disconnected`() {
        assertFalse(BluetoothConnectionReflection.isConnected(DisconnectedTarget()))
    }

    @Test(expected = NoSuchMethodException::class)
    fun `throws when reflection method is missing`() {
        BluetoothConnectionReflection.isConnected(MissingMethodTarget())
    }

    @Test
    fun `returns false when reflection method returns non boolean`() {
        assertFalse(BluetoothConnectionReflection.isConnected(WrongReturnTypeTarget()))
    }
}
