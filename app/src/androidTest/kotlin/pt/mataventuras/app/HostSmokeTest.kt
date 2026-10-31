package pt.mataventuras.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pt.mataventuras.plugin.KartPluginActivity
import pt.mataventuras.plugin.RunnerPluginActivity
import java.io.FileInputStream

/**
 * Device smoke for the Compose host and the two Godot plugin processes.
 * Plugin activities run in `:engine2d` and `:engine3d`, so the instrumentation
 * process cannot launch them with [ActivityScenario].
 */
@RunWith(AndroidJUnit4::class)
class HostSmokeTest {
    @Test
    fun mainActivityReachesResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    fun runnerPluginProcessStarts() {
        assertPluginProcess(RunnerPluginActivity::class.java, ":engine2d")
    }

    @Test
    fun kartPluginProcessStarts() {
        assertPluginProcess(KartPluginActivity::class.java, ":engine3d")
    }

    private fun assertPluginProcess(type: Class<*>, suffix: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.startActivity(
            Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        val process = "${context.packageName}$suffix"
        val deadline = System.currentTimeMillis() + 8_000L
        var pid = ""
        while (System.currentTimeMillis() < deadline && pid.isBlank()) {
            pid = shell("pidof $process").trim()
            if (pid.isBlank()) Thread.sleep(250)
        }
        assertTrue("expected $process", pid.isNotBlank())
    }

    private fun shell(command: String): String {
        val descriptor =
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand(command)
        return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }
}
