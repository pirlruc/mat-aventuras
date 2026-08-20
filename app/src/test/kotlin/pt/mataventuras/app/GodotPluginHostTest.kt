package pt.mataventuras.app

import android.content.pm.PackageManager
import android.os.Build
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.Kart3dActivity
import pt.mataventuras.app.engine.Platformer2dActivity
import pt.mataventuras.app.shouldOpenContainer
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.plugin.KartPluginActivity
import pt.mataventuras.plugin.RunnerPluginActivity

@RunWith(RobolectricTestRunner::class)
class GodotPluginHostTest {
    @Test
    fun runtimeSkipsGodotOnRobolectricAndEmbedsOnDeviceFingerprint() {
        assertFalse(GodotRuntime.shouldEmbed("robolectric"))
        assertFalse(GodotRuntime.shouldEmbed("generic/robolectric/sdk"))
        assertTrue(GodotRuntime.shouldEmbed("google/pixel/release-keys"))
        assertFalse(GodotRuntime.shouldEmbed())
        assertTrue(GodotRuntime.isRobolectricFingerprint("Robolectric"))
        assertFalse(GodotRuntime.isRobolectricFingerprint("user/release-keys"))
        assertEquals(
            listOf("--path", ".", "--scene", GodotRuntime.SCENE_KART),
            GodotRuntime.commandLineFor(GodotRuntime.SCENE_KART),
        )
        assertEquals(
            listOf("--path", ".", "--scene", GodotRuntime.SCENE_RUNNER),
            GodotRuntime.commandLineFor(GodotRuntime.SCENE_RUNNER),
        )
        assertEquals("MatAventuras", GodotRuntime.PLUGIN_NAME)
        assertEquals("res://kart.tscn", GodotRuntime.SCENE_KART)
        assertEquals("res://runner.tscn", GodotRuntime.SCENE_RUNNER)
    }

    @Test
    fun pluginActivitiesUseNativeFallbackAndKeepExtras() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartIntent =
            EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.MISCHIEVOUS_ALIEN, "Rui")
        assertEquals(EnginePluginContract.PLUGIN_KART_CLASS, kartIntent.component!!.className)
        val kart =
            Robolectric.buildActivity(KartPluginActivity::class.java, kartIntent).setup().get()
        assertNotNull(kart.nativeSession)
        assertEquals("mischievous_alien" to "Rui", kart.extrasSnapshot())
        kart.nativeSession!!.handleTouch(0.1f, MotionEvent.ACTION_DOWN)
        kart.nativeSession!!.handleTouch(0.5f, MotionEvent.ACTION_DOWN)
        kart.completeRewardOnUi(true)

        val runnerIntent =
            EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.HERO_PUP, "Ana")
        assertEquals(EnginePluginContract.PLUGIN_RUNNER_CLASS, runnerIntent.component!!.className)
        val runner =
            Robolectric.buildActivity(RunnerPluginActivity::class.java, runnerIntent).setup().get()
        assertNotNull(runner.loop)
        runner.loop!!.jumping = true
        repeat(8) { runner.loop!!.tick() }
        assertEquals("hero_pup" to "Ana", runner.extrasSnapshot())
        runner.completeReward(ok = false)
    }

    @Test
    fun binderEmbedHookDoesNotConstructGodot() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kart =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, "Rui"),
            ).setup().get()
        var kartScene = ""
        GodotRewardBinder.bindKart(kart, embed = true) { _, scene -> kartScene = scene }
        assertEquals(GodotRuntime.SCENE_KART, kartScene)
        GodotRewardBinder.bindKart(kart, embed = false)
        assertNotNull(kart.nativeSession)

        val runner =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, "Ana"),
            ).setup().get()
        var runnerScene = ""
        GodotRewardBinder.bindRunner(runner, embed = true) { _, scene -> runnerScene = scene }
        assertEquals(GodotRuntime.SCENE_RUNNER, runnerScene)
        GodotRewardBinder.bindRunner(runner, embed = false)
        assertNotNull(runner.loop)
    }

    @Test
    fun bridgeReadsExtrasAndFinishesOnUiThread() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kart =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, "Rui"),
            ).setup().get()
        assertEquals("brave_plumber", GodotBridge.mascotCode(kart))
        assertEquals("Rui", GodotBridge.childName(kart))
        GodotBridge.finish(kart, true)
    }

    @Test
    fun godotProjectIsPackagedWithCompatibilityRenderer() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val project = ctx.assets.open("project.godot").bufferedReader().readText()
        assertTrue(project.contains("use_hidden_project_data_directory=false"))
        assertTrue(project.contains("gl_compatibility"))
        assertTrue(project.contains("import_etc2_astc=true"))
        ctx.assets.open("kart.tscn").close()
        ctx.assets.open("runner.tscn").close()
        ctx.assets.open("kart.gd").close()
        ctx.assets.open("runner.gd").close()
        ctx.assets.open("host.gd").close()
    }

    @Test
    fun mergedManifestStaysLocalOnly() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info =
            ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS)
        val perms = info.requestedPermissions?.toList().orEmpty()
        assertTrue(EnginePluginContract.manifestAllowed(perms))
        assertFalse(perms.any { it.endsWith("INTERNET") })
    }

    @Test
    fun isolatedProcessNameSkipsRoomAndNativeActivitiesStillStart() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertFalse(EnginePluginContract.isIsolatedProcessName(currentProcessName(ctx)))
        assertTrue(shouldOpenContainer("pt.mataventuras.app"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine3d"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine2d"))
        currentProcessName(ctx, sdk = Build.VERSION_CODES.O)
        currentProcessName(ctx, sdk = Build.VERSION_CODES.P)
        val nativeKart =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup().get()
        nativeKart.closeFinished()
        val nativeRunner =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                android.content.Intent(ctx, Platformer2dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup().get()
        nativeRunner.completeReward(ok = true)
    }
}
