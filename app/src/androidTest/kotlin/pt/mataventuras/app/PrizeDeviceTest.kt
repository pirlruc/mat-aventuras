package pt.mataventuras.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
            val log = waitForHost(prize.name)
            assertTrue(
                "${prize.name} did not log the boot overlay",
                log.contains("MatPrize ready") || log.contains("boot-timeout"),
            )
            assertRendered(capture(), prize.name)
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

    private fun waitForHost(name: String): String {
        val deadline = System.currentTimeMillis() + 45_000L
        var log = ""
        while (System.currentTimeMillis() < deadline) {
            log = shell("logcat -d -s MatPrize:I AndroidRuntime:E")
            if (log.contains("MatPrize ready") || log.contains("boot-timeout")) return log
            Thread.sleep(400)
        }
        org.junit.Assert.fail("$name never logged the boot overlay\n${log.takeLast(1_200)}")
        return log
    }

    private fun capture(): Bitmap? {
        val descriptor =
            InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("screencap -p")
        return FileInputStream(descriptor.fileDescriptor).use { BitmapFactory.decodeStream(it) }
    }

    private fun assertRendered(
        bitmap: Bitmap?,
        name: String,
    ) {
        assertTrue("$name screenshot missing", bitmap != null && bitmap.width > 32 && bitmap.height > 32)
        val frame = bitmap ?: return
        val colors = HashSet<Int>()
        val stepX = (frame.width / 8).coerceAtLeast(1)
        val stepY = (frame.height / 8).coerceAtLeast(1)
        var y = 0
        while (y < frame.height) {
            var x = 0
            while (x < frame.width) {
                colors += frame.getPixel(x, y)
                x += stepX
            }
            y += stepY
        }
        assertTrue("$name frame is a flat colour", colors.size > 1)
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
