package pt.mataventuras.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Process
import android.util.Log
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
 * Launches every prize on a device. Ready means the Godot scene called
 * [pt.mataventuras.app.engine.GodotBridge.prizeReady], and the frame is no
 * longer the blue boot overlay.
 */
@RunWith(AndroidJUnit4::class)
class PrizeDeviceTest {
    @Test
    fun everyPrizeBecomesReady() {
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
                "${prize.name} did not become ready\n${log.takeLast(2_000)}",
                log.contains("boot-ready") && !log.contains("boot-timeout"),
            )
            assertRendered(capture(prize.name), prize.name)
        }
    }

    private fun stopEngines() {
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        // pidof matches the 15-character comm, which truncates :engine2d.
        shell("ps -A").lineSequence().forEach { line ->
            if (!line.contains("$pkg:engine")) return@forEach
            val pid =
                line
                    .trim()
                    .split(Regex("\\s+"))
                    .getOrNull(1)
                    ?.toIntOrNull()
                    ?: return@forEach
            Log.i("MatPrize", "stop $pid")
            Process.killProcess(pid)
        }
        val deadline = System.currentTimeMillis() + 3_000L
        while (System.currentTimeMillis() < deadline && shell("ps -A").contains("$pkg:engine")) {
            Thread.sleep(200)
        }
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
            log = shell("logcat -d -s MatPrize:I godot:I AndroidRuntime:E")
            if (log.contains("boot-ready") || log.contains("boot-timeout")) return log
            Thread.sleep(400)
        }
        org.junit.Assert.fail("$name never logged the boot overlay\n${log.takeLast(1_200)}")
        return log
    }

    private fun capture(name: String): Bitmap? {
        shell("screencap -p /data/local/tmp/mat-006-$name.png")
        val descriptor =
            InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("cat /data/local/tmp/mat-006-$name.png")
        return FileInputStream(descriptor.fileDescriptor).use { BitmapFactory.decodeStream(it) }
    }

    private fun assertRendered(
        bitmap: Bitmap?,
        name: String,
    ) {
        assertTrue("$name screenshot missing", bitmap != null && bitmap.width > 32 && bitmap.height > 32)
        val frame = bitmap ?: return
        val stepX = (frame.width / 8).coerceAtLeast(1)
        val stepY = (frame.height / 8).coerceAtLeast(1)
        var other = 0
        var y = 0
        while (y < frame.height) {
            var x = 0
            while (x < frame.width) {
                if (!bootOverlayPixel(frame.getPixel(x, y))) other += 1
                x += stepX
            }
            y += stepY
        }
        assertTrue("$name is still the boot overlay", other > 0)
    }

    /**
     * Boot blue is #1E88E5 and the overlay text is white. A prize frame has
     * some other colour.
     */
    private fun bootOverlayPixel(pixel: Int): Boolean {
        val red = (pixel shr 16) and 0xff
        val green = (pixel shr 8) and 0xff
        val blue = pixel and 0xff
        val bootBlue = red in 20..45 && green in 120..155 && blue in 210..245
        val ink = red > 230 && green > 230 && blue > 230
        return bootBlue || ink
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
