import com.example.myapplication.message.NVClass
import com.example.myapplication.message.NVFlags
import com.example.myapplication.message.NVOperators
import com.example.myapplication.message.ReturnCode
import java.lang.IllegalArgumentException

/**
 * raw:
 *  Class	8-bit	A class of messages that belong to a similar use case
 *  ID	8-bit	Identifier of a specific message
 *  Flags	4-bit	Bit fields to exchange information
 *  Operator	4-bit	Action to perform, e.g., get, set, etc.
 *  Payload Length	8 or 16-bit	Length of the message payload in bytes. Length defined by flags
 */
class NVMessageHeader {

    val nvClass: NVClass
    val nvId: Int
    val nvFlags: ArrayList<NVFlags>
    val nvOperator: NVOperators
    val nvPayloadLength: Int
    val nvHeaderLength: Int
    var nvResultCode: ReturnCode? = null
    var nvIndex: Int? = null


    constructor(
        nvClass: NVClass,
        nvId: Int,
        nvOperator: NVOperators,
        nvPayloadLength: Int,
        encrypted: Boolean = false,
        nvResultCode: ReturnCode = ReturnCode.SUCCESS
    ) {
        this.nvClass = nvClass
        this.nvId = nvId
        this.nvOperator = nvOperator
        this.nvPayloadLength = nvPayloadLength
        this.nvFlags = ArrayList()
        val headerLength: Int
        if (nvPayloadLength > 0xFF) {
            this.nvFlags.add(NVFlags.ZEROTH_LENGTH)
            headerLength = 5
        } else {
            headerLength = 4
        }
        if (this.nvOperator == NVOperators.RESULT) {
            this.nvHeaderLength = headerLength + 1
        }else{
            this.nvHeaderLength = headerLength
        }
        if (encrypted) {
            this.nvFlags.add(NVFlags.FIRST_ENCRYPTED)
        }
        this.nvResultCode = nvResultCode
    }

    /**
     * from Raw data
     */
    constructor(bytes: ByteArray) {
        val classValue = bytes[0].toPositiveInt()
        val idValue = bytes[1].toPositiveInt()
        val flagValue = bytes[2].toPositiveInt() and 0xF
        val operatorValue = (bytes[2].toPositiveInt() shr 4) and 0xF

        this.nvClass = NVClass.values().find { it.value == classValue }
            ?: throw IllegalArgumentException("no value for class")
        this.nvId = idValue
        this.nvFlags = ArrayList(NVFlags.values().filter { (it.value and flagValue) == it.value })
        this.nvOperator = NVOperators.values().find { it.value == operatorValue }
            ?: throw IllegalArgumentException("no value for operator")
        val headerLength: Int
        if (nvFlags.contains(NVFlags.ZEROTH_LENGTH)) {
            nvPayloadLength = bytes[3].toPositiveInt()
            nvIndex = bytes[4].toPositiveInt()
            headerLength = 5
        } else {
            nvPayloadLength = bytes[3].toPositiveInt()
            headerLength = 4
        }
        if (this.nvOperator == NVOperators.RESULT) {
            this.nvHeaderLength = headerLength + 1
            this.nvResultCode = ReturnCode.values().find { it.value == bytes[headerLength].toPositiveInt() }
                ?: throw IllegalArgumentException("no value for return code")
        } else {
            this.nvHeaderLength = headerLength
        }
    }

    /**
     * To Header Bytes
     */
    fun toBytes(): ByteArray {
        val bytes = ByteArray(this.nvHeaderLength)
        bytes[0] = this.nvClass.value.toByte()
        bytes[1] = this.nvId.toByte()
        bytes[2] = (getFlagValue() or ((this.nvOperator.value shl 4) and 0xF0)).toByte()
        if (nvHeaderLength == 4) {
            bytes[3] = (this.nvPayloadLength and 0xFF).toByte()
        } else {
            bytes[3] = (this.nvPayloadLength and 0xFF).toByte()
            bytes[4] = (this.nvPayloadLength shr 8 and 0xFF).toByte()
        }
        if (this.nvOperator == NVOperators.RESULT) {
            bytes[bytes.lastIndex] = this.nvResultCode!!.value.toByte()
        }
        return bytes
    }

    /**
     * Get flag value
     */
    private fun getFlagValue(): Int {
        val value = 0
        for (nvFlag in this.nvFlags) {
            when (nvFlag) {
                NVFlags.ZEROTH_LENGTH -> value or 1
                NVFlags.FIRST_ENCRYPTED -> value or 2
            }
        }
        return value
    }

    private fun Byte.toPositiveInt() = toInt() and 0xFF

}