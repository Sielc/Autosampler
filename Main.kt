class AutosamplerTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val autosampler = Autosampler.find() ?: return

            autosampler.calibrateNeedle()
            Thread.sleep(2000)

            autosampler.chooseVial(0)  // Arm home
            Thread.sleep(5000)

            autosampler.chooseVial(21)  // Locate tray and arm to vial 25
            print("Vial is ${autosampler.vial}")
            print("Vial choosing - ${autosampler.vialChoosing}")
            Thread.sleep(5000)

            autosampler.updateState()
            print("Vial is ${autosampler.vial}")
            print("Vial ready - ${autosampler.vialReady}")

            autosampler.moveNeedle(25)  // Needle down 25mm
            print("Needle position is ${autosampler.needle}")
            print("Needle ready - ${autosampler.needleReady}")
            Thread.sleep(5000)

            autosampler.updateState()
            print("Needle position is ${autosampler.needle}")
            print("Needle ready - ${autosampler.needleReady}")

            autosampler.moveNeedle(0)   // Needle up
            Thread.sleep(5000)

            autosampler.chooseVial(0)   // Arm home
            Thread.sleep(5000)

            autosampler.moveNeedle(38)  // Needle down for injection
            Thread.sleep(5000)

            autosampler.moveNeedle(0)   // Needle up
            Thread.sleep(5000)
        }
    }
}
