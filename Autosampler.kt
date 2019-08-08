package com.sielc.hardware

import com.fazecast.jSerialComm.SerialPort


const val SERAIL = "A1"
const val VIAL = "E1"
const val NEEDLE = "F1"


class Autosampler(port: SerialPort) : SerialDeviceBase(port) {
    val serial: String = get(SERAIL)
    
    var vial: Int? = null
        set(vial) {
            field = null
            this.vialChoosing = false
            this.vialReady = false
            this.trayError = false
            this.armError = false
            when {
                vial == null -> {               // Unknown vial state
                }
                vial in 10000..11111 -> {       // Tray or arm error
                    if (vial % 10 == 1) {           // 1***1 Tray error
                        this.trayError = true
                        this.armError = false
                    }
                    if ((vial / 10) % 10 == 1) {    // 1**1* Arm error
                        this.trayError = false
                        this.armError = true
                    }
                }
                vial > 20000 -> {               // Moving to vial or moving home
                    field = vial - 20000
                    this.vialChoosing = true
                }
                vial in 0..40 -> {              // Fixed on vial or fixed at home
                    field = vial
                    this.vialReady = true
                }
            }
        }
    var vialReady: Boolean = false
    var vialChoosing: Boolean = false
    var trayError: Boolean = false
    var armError: Boolean = false

    var needle: Int? = null
        set(needle) {
            field = null
            this.needleMovingUp = false
            this.needleMovingDown = false
            this.needleReady = false
            this.needleError = false
            when {
                needle == null -> {               // Unknown needle state
                }
                needle in 10000..11111 -> {       // Needle error
                    this.needleError = true
                }
                needle > 30000 -> {               // Moving down
                    field = needle - 30000
                    this.needleMovingDown = true
                }
                needle > 20000 -> {               // Moving up
                    field = needle - 20000
                    this.needleMovingUp = true
                }
                needle in 0..38 -> {              // Fixed on vial or fixed at home
                    field = needle
                    this.needleReady = true
                }
            }
        }
    var needleMovingUp: Boolean = false
    var needleMovingDown: Boolean = false
    var needleReady: Boolean = false
    var needleError: Boolean = false

    var stateStr: String = ""

    // Vial

    fun chooseVial(vial: Int) {  // 0 - go home; [1, 40] - choose vial
        println("Autosampler.chooseVial($vial)")
        this.vial = set(VIAL, vial)
    }

    fun checkVial(): Int? {
        println("Autosampler.checkVial()")
        vial = getInt(VIAL)
        return this.vial
    }

    // Needle

    fun calibrateNeedle() {
        println("Autosampler.calibrateNeedle()")
        needle = set(NEEDLE, 10001)
    }

    fun moveNeedle(position: Int) {  // In millimeters: 0 - highest; 38 - lowest
        println("Autosampler.moveNeedle($position)")
        needle = set(NEEDLE, position)
    }

    fun abortNeedle() {
        println("Autosampler.abortNeedle()")
        needle = set(NEEDLE, 10002)
    }

    fun checkNeedle(): Int? {
        println("Autosampler.checkNeedle()")
        needle = getInt(NEEDLE)
        return this.needle
    }

    // State

    fun updateState(): String {
        checkVial()
        checkNeedle()
        when {
            trayError -> {
                stateStr = "Tray error"
            }
            armError -> {
                stateStr = "Arm error"
            }
            vialChoosing -> {
                stateStr = "Searching vial"
            }
            vialReady -> {
                stateStr = "Vial selected"
            }
            else -> {
                stateStr = ""
            }
        }
        return stateStr
    }

    override fun toString(): String {
        return "Autosampler(stateStr='$stateStr')"
    }

    companion object {
        fun find(): Autosampler? {
            val port = findPortByDescription("Sielc Autosampler V1.0")
            if (port != null) {
                return Autosampler(port)
            }
            return null
        }
    }
}
