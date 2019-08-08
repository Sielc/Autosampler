package com.sielc.hardware

import com.fazecast.jSerialComm.SerialPort
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError
import org.apache.log4j.config.PropertySetterException
import java.util.concurrent.TimeoutException
import javax.naming.NameNotFoundException


// Classes to support DOMP protocol


class VarInt(name: String, val valueInt: Int) : VarStr(name, valueInt.toString())


open class VarStr(val name: String, val value: String) {
    companion object {
        fun parse(buffer: ByteArray): VarStr {
            return parse(buffer.toString(Charsets.US_ASCII))
        }

        fun parse(buffer: String): VarStr {
            val name = buffer.substring(3, 5).toUpperCase()
            val delimiter = buffer.substring(5, 6)
            var value = buffer.substring(6, buffer.length - 1)

            when (delimiter) {
                "!" -> {
                    println("Set $name exception $value")
                    throw PropertySetterException(value)
                }
                "=" -> {
                    println("$name = $value")
                    val valueInt = value.toIntOrNull()
                    return if (valueInt != null)
                        VarInt(name, valueInt)
                    else
                        VarStr(name, value)
                }
                "/" -> {
                    value = value.substring(1, buffer.length - 2)
                    println("$name = $value")
                    val valueInt = value.toIntOrNull()
                    return if (valueInt != null)
                        VarInt(name, valueInt)
                    else
                        VarStr(name, value)
                }
                else -> throw Exception("Unknown delimiter $delimiter")
            }
        }
    }
}


open class SerialDeviceBase(var port: SerialPort) {
    class DeviceDisconnected : Exception("Port is closed")

    var dead: Boolean = false

    fun set(name: String, value: String): String {
        write(name, value)
        val v = readBlockingInt()
        if (v.name != name) throw NameNotFoundException("Returned variable ${v.name} is not $name")
        return v.value
    }

    fun set(name: String, value: Int): Int {
        write(name, value)
        val v = readBlockingInt()
        if (v.name != name) throw NameNotFoundException("Returned variable ${v.name} is not $name")
        return v.valueInt
    }

    fun get(name: String): String {
        val write = (">1 $name?\r").toByteArray(Charsets.US_ASCII)
        port.writeBytes(write, write.size.toLong())
        val v = readBlocking()
        if (v.name != name) throw NameNotFoundException("Returned variable ${v.name} is not $name")
        return v.value
    }

    fun getInt(name: String): Int {
        val write = (">1 $name?\r").toByteArray(Charsets.US_ASCII)
        port.writeBytes(write, write.size.toLong())
        val v = readBlockingInt()
        if (v.name != name) throw NameNotFoundException("Returned variable ${v.name} is not $name")
        return v.valueInt
    }

    private fun write(name: String, value: Int) {
        val write = (">1 $name=$value\r").toByteArray(Charsets.US_ASCII)
        port.writeBytes(write, write.size.toLong())
    }

    private fun write(name: String, value: String) {
        val write = (">1 $name/\"$value\"\r").toByteArray(Charsets.US_ASCII)
        port.writeBytes(write, write.size.toLong())
    }

    private fun readBlocking(trials: Int = 20): VarStr {
        var countdown = trials
        var available = 0
        var buffer = ""
        while (available == 0 && countdown-- > 0 && (buffer.isEmpty() || buffer[buffer.length - 1] != '\r')) {
            Thread.sleep(2)
            available = port.bytesAvailable()
            print("$available available ")
            when {
                available < 0 -> {
                    dead = true
                    throw DeviceDisconnected()
                }
                available > 0 -> {
                    val readBuffer = ByteArray(available)
                    val numRead = port.readBytes(readBuffer, available.toLong())
                    val read = readBuffer.toString(Charsets.US_ASCII)
                    println("read $numRead -> $read. ")
                    buffer += read
                }
            }
        }
        if (buffer.isNotEmpty() && buffer[buffer.length - 1] == '\r') return VarStr.parse(buffer)
        throw TimeoutException("$trials trials failed")
    }

    private fun readBlockingInt(trials: Int = 10): VarInt {
        return when (val v = readBlocking(trials)) {
            is VarInt -> v
            else -> throw TypeCheckError(ErrorMsg("Not Int"))
        }
    }
}
