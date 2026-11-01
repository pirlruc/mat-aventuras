package pt.mataventuras.app

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.plugin.KartPluginActivity
import pt.mataventuras.plugin.RunnerPluginActivity

/**
 * Launches every prize on a device and checks the boot log.
 * Screenshots are written under /sdcard/Download for the host to pull.
 */
@RunWith(AndroidJUnit4::class)
class PrizeDeviceTest {
    @Test
    fun everyPrizeLogsTheBootOverlay() {
        val prizes =
            listOf(
                Prize("runner", RunnerPluginActivity::class.java, "RUNNER", AgeBand.THREE_TO_FIVE.name),
                Prize("climb", RunnerPluginActivity::class.java, "CLIMB", AgeBand.THREE_TO_FIVE.name),
                Prize("chomp", RunnerPluginActivity::class.java, "CHOMP", AgeBand.THREE_TO_FIVE.name),
                Prize("invaders", RunnerPluginActivity::class.java, "INVADERS", AgeBand.SIX_TO_EIGHT.name),
                Prize("kart", KartPluginActivity::class.java, "KART", AgeBand.SIX_TO_EIGHT.name),
            )
        prizes.forEach { prize ->
            stopEngines()
            shell("logcat -c")
            launch(prize)
            Thread.sleep(8_000)
            shell("screencap -p /sdcard/Download/mat-prize-${prize.name}.png")
            val log = shell("logcat -d")
            assertTrue("${prize.name}\n${log.takeLast(800)}", log.contains("MatPrize"))
        }
    }

    private fun stopEngines() {
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        listOf(":engine2d", ":engine3d").forEach { suffix ->
            val pid = shell("pidof $pkg$suffix").trim().substringBefore(' ')
            if (pid.isNotBlank()) shell("kill -9 $pid")
        }
        Thread.sleep(500)
    }

    private fun launch(prize: Prize) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.startActivity(
            Intent(context, prize.type)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("scene", prize.scene)
                .putExtra("mascot", "speedy_hedgehog")
                .putExtra("name", "Ana")
                .putExtra("age", prize.age),
        )
    }

    private fun shell(command: String): String {
        val descriptor =
            InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand(command)
        return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }

    private data class Prize(
        val name: String,
        val type: Class<*>,
        val scene: String,
        val age: String,
    )
}
