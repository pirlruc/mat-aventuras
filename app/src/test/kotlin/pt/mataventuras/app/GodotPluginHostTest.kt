package pt.mataventuras.app

import android.content.pm.PackageManager
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
import org.robolectric.android.controller.ActivityController
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.engine.Kart3dActivity
import pt.mataventuras.app.engine.NativeKartHost
import pt.mataventuras.app.engine.Platformer2dActivity
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
        assertEquals(
            android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY,
            NativeKartHost.renderMode(embed = false),
        )
        assertEquals(
            android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY,
            NativeKartHost.renderMode(embed = true),
        )
    }

    @Test
    fun pluginActivitiesUseNativeFallbackAndKeepExtras() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartIntent =
            EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.MISCHIEVOUS_ALIEN, "Rui")
        assertEquals(EnginePluginContract.PLUGIN_KART_CLASS, kartIntent.component!!.className)
        val kartController = Robolectric.buildActivity(KartPluginActivity::class.java, kartIntent).setup()
        val kart = kartController.get()
        assertNotNull(kart.nativeSession)
        assertEquals("mischievous_alien" to "Rui", kart.extrasSnapshot())
        kart.nativeSession!!.handleTouch(0.1f, MotionEvent.ACTION_DOWN)
        kart.nativeSession!!.handleTouch(0.5f, MotionEvent.ACTION_DOWN)
        kart.completeRewardOnUi(true)
        destroy(kartController)

        val runnerIntent =
            EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.HERO_PUP, "Ana")
        assertEquals(EnginePluginContract.PLUGIN_RUNNER_CLASS, runnerIntent.component!!.className)
        val runnerController =
            Robolectric.buildActivity(RunnerPluginActivity::class.java, runnerIntent).setup()
        val runner = runnerController.get()
        assertNotNull(runner.loop)
        runner.loop!!.jumping = true
        repeat(8) { runner.loop!!.tick() }
        assertEquals("hero_pup" to "Ana", runner.extrasSnapshot())
        runner.completeReward(ok = false)
        destroy(runnerController)
    }

    @Test
    fun binderEmbedHookDoesNotConstructGodot() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartController =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, "Rui"),
            ).setup()
        val kart = kartController.get()
        var kartScene = ""
        GodotRewardBinder.bindKart(kart, embed = true) { _, scene -> kartScene = scene }
        assertEquals(GodotRuntime.SCENE_KART, kartScene)
        GodotRewardBinder.bindKart(kart, embed = false)
        assertNotNull(kart.nativeSession)
        destroy(kartController)

        val runnerController =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, "Ana"),
            ).setup()
        val runner = runnerController.get()
        var runnerScene = ""
        GodotRewardBinder.bindRunner(runner, embed = true) { _, scene -> runnerScene = scene }
        assertEquals(GodotRuntime.SCENE_RUNNER, runnerScene)
        GodotRewardBinder.bindRunner(runner, embed = false)
        assertNotNull(runner.loop)
        destroy(runnerController)
    }

    @Test
    fun bridgeReadsExtrasAndFinishesOnUiThread() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartController =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, "Rui"),
            ).setup()
        val kart = kartController.get()
        assertEquals("brave_plumber", GodotBridge.mascotCode(kart))
        assertEquals("Rui", GodotBridge.childName(kart))
        GodotBridge.finish(kart, true)
        destroy(kartController)
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
        val appInfo = ctx.packageManager.getApplicationInfo(ctx.packageName, 0)
        assertEquals(0, appInfo.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP)
        val activities =
            ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
                .activities
                ?.toList()
                .orEmpty()
        assertTrue(activities.any { it.name == EnginePluginContract.PLUGIN_KART_CLASS })
        assertTrue(activities.any { it.name == EnginePluginContract.PLUGIN_RUNNER_CLASS })
        assertTrue(
            activities
                .filter { it.name.contains("Plugin") || it.name.endsWith("Kart3dActivity") }
                .all { EnginePluginContract.isIsolatedProcessName(it.processName.orEmpty()) },
        )
    }

    @Test
    fun isolatedProcessNameSkipsRoomAndNativeActivitiesStillStart() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertFalse(EnginePluginContract.isIsolatedProcessName(currentProcessName()))
        assertTrue(shouldOpenContainer("pt.mataventuras.app"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine3d"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine2d"))
        currentProcessName()
        currentProcessName("pt.mataventuras.app:engine3d")
        assertEquals(
            "pt.mataventuras.app:engine3d",
            resolveProcessName(sdk = 26, procCmdline = "pt.mataventuras.app:engine3d\u0000"),
        )
        assertFalse(
            shouldOpenContainer(
                resolveProcessName(sdk = 26, procCmdline = "pt.mataventuras.app:engine2d\u0000"),
            ),
        )
        assertEquals("pkg", parseProcCmdline("pkg\u0000"))
        assertEquals("", procCmdlineOrEmpty { throw IllegalStateException("proc") })
        assertEquals("ok", procCmdlineOrEmpty { "ok" })
        assertEquals("device", resolveProcessName(sdk = 28, api28Name = "device"))
        assertTrue(resolveProcessName(sdk = 26).isNotBlank())
        assertTrue(hostProcessName().isNotBlank())
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        assertFalse(app.bindHostGraph("pt.mataventuras.app:engine3d"))
        val nativeKartController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup()
        nativeKartController.get().closeFinished()
        nativeKartController.get().pauseEngineSurface()
        nativeKartController.get().resumeEngineSurface()
        nativeKartController.get().pauseableSurface =
            android.opengl.GLSurfaceView(nativeKartController.get())
        nativeKartController.get().pauseEngineSurface()
        nativeKartController.get().resumeEngineSurface()
        destroy(nativeKartController)
        val namelessController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup"),
            ).setup()
        assertEquals("", namelessController.get().childName())
        destroy(namelessController)
        val nativeRunnerController =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                android.content.Intent(ctx, Platformer2dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup()
        nativeRunnerController.get().completeReward(ok = true)
        destroy(nativeRunnerController)
    }

    private fun destroy(controller: ActivityController<*>) {
        controller.pause().stop().destroy()
    }
}
