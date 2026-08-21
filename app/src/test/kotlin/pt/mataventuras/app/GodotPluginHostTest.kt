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
import pt.mataventuras.domain.engine.RewardGame
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
        assertTrue(GodotRuntime.isSurfaceReady(320, 200))
        assertFalse(GodotRuntime.isSurfaceReady(16, 720))
        assertFalse(GodotRuntime.isSurfaceReady(1280, 8))
        assertEquals(32, GodotRuntime.SURFACE_MIN_PX)
        assertFalse(GodotRuntime.commandLineFor().contains("--path"))
        assertFalse(GodotRuntime.commandLineFor().contains("--scene"))
        assertEquals("command_line_params", GodotRuntime.EXTRA_COMMAND_LINE)
        assertTrue(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = false,
                finishing = false,
                destroyed = false,
                fromRelaunch = false,
            ),
        )
        assertFalse(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = true,
                finishing = false,
                destroyed = false,
                fromRelaunch = false,
            ),
        )
        assertFalse(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = false,
                finishing = true,
                destroyed = false,
                fromRelaunch = false,
            ),
        )
        assertFalse(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = false,
                finishing = false,
                destroyed = true,
                fromRelaunch = false,
            ),
        )
        assertFalse(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = false,
                finishing = false,
                destroyed = false,
                fromRelaunch = true,
            ),
        )
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals("MatAventuras", GodotRuntime.PLUGIN_NAME)
        assertEquals("res://kart.tscn", GodotRuntime.SCENE_KART)
        assertEquals("res://runner.tscn", GodotRuntime.SCENE_RUNNER)
        assertEquals("res://invaders.tscn", GodotRuntime.SCENE_INVADERS)
        assertEquals("res://chomp.tscn", GodotRuntime.SCENE_CHOMP)
        assertEquals("res://climb.tscn", GodotRuntime.SCENE_CLIMB)
        assertTrue(NativeKartHost.hudLines(pt.mataventuras.app.engine.OffroadRacerLoop()).first.startsWith("Volta"))
    }

    @Test
    fun pluginActivitiesUseNativeFallbackAndKeepExtras() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartIntent =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.MISCHIEVOUS_ALIEN,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.THREE_D,
            )
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
            EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.HERO_PUP, "Ana", game = RewardGame.RUNNER)
        assertEquals(EnginePluginContract.PLUGIN_RUNNER_CLASS, runnerIntent.component!!.className)
        val runnerController =
            Robolectric.buildActivity(RunnerPluginActivity::class.java, runnerIntent).setup()
        val runner = runnerController.get()
        assertNotNull(runner.loop)
        runner.loop!!.jumping = true
        repeat(8) { runner.loop!!.tick() }
        assertEquals("hero_pup" to "Ana", runner.extrasSnapshot())
        runner.completeReward(ok = false)
        assertFalse(runner.requestEngineRestart())
        destroy(runnerController)

        val sevenPlatform =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.HERO_PUP,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.TWO_D,
            )
        assertEquals(EnginePluginContract.PLUGIN_RUNNER_CLASS, sevenPlatform.component!!.className)
    }

    @Test
    fun binderEmbedHookDoesNotConstructGodot() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartController =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.SEVEN_YEARS,
                    Mascot.BRAVE_PLUMBER,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.THREE_D,
                ),
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
                EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, "Ana", game = RewardGame.RUNNER),
            ).setup()
        val runner = runnerController.get()
        var runnerScene = ""
        GodotRewardBinder.bindRunner(runner, embed = true) { _, scene -> runnerScene = scene }
        assertEquals(GodotRuntime.SCENE_RUNNER, runnerScene)
        GodotRewardBinder.bindRunner(runner, embed = false)
        assertNotNull(runner.loop)
        destroy(runnerController)

        val climbController =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.THREE_YEARS,
                    Mascot.HERO_PUP,
                    "Ana",
                    game = RewardGame.CLIMB,
                ),
            ).setup()
        assertNotNull(climbController.get().climb)
        var climbScene = ""
        GodotRewardBinder.bindRunner(climbController.get(), embed = true) { _, scene -> climbScene = scene }
        assertEquals(GodotRuntime.SCENE_CLIMB, climbScene)
        destroy(climbController)
        val invadersController =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.SEVEN_YEARS,
                    Mascot.MISCHIEVOUS_ALIEN,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.TWO_D,
                    RewardGame.INVADERS,
                ),
            ).setup()
        assertNotNull(invadersController.get().invaders)
        assertEquals(RewardGame.INVADERS.name, invadersController.get().sceneCode())
        destroy(invadersController)
        val chompController =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.THREE_YEARS,
                    Mascot.PINK_PIGLET,
                    "Eva",
                    game = RewardGame.CHOMP,
                ),
            ).setup()
        assertNotNull(chompController.get().chomp)
        destroy(chompController)
    }

    @Test
    fun pluginRestartReturnsHostRelaunchExtrasOnce() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartIntent =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.MISCHIEVOUS_ALIEN,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.THREE_D,
            )
        val controller = Robolectric.buildActivity(KartPluginActivity::class.java, kartIntent).setup()
        val kart = controller.get()
        assertFalse(kart.isGodotRelaunch())
        assertTrue(kart.onEngineForceQuit())
        assertTrue(kart.isRewardSettled())
        assertTrue(kart.isFinishing)
        assertFalse(kart.requestEngineRestart())
        assertFalse(kart.completeReward(ok = true))
        assertFalse(kart.onEngineForceQuit())
        destroy(controller)
        val relaunch =
            EngineLauncher.relaunchIntent(
                ctx,
                EngineLauncher.restartResultIntent(
                    EnginePluginContract.PLUGIN_KART_CLASS,
                    "mischievous_alien",
                    "Rui",
                ),
            )!!
        assertTrue(relaunch.getBooleanExtra(EngineLauncher.EXTRA_GODOT_RELAUNCH, false))
        val relaunched = Robolectric.buildActivity(KartPluginActivity::class.java, relaunch).setup()
        assertTrue(relaunched.get().isGodotRelaunch())
        assertFalse(relaunched.get().onEngineForceQuit())
        assertFalse(relaunched.get().isRewardSettled())
        assertFalse(
            GodotRuntime.shouldRestartHost(
                alreadyRestarted = false,
                finishing = false,
                destroyed = false,
                fromRelaunch = relaunched.get().isGodotRelaunch(),
            ),
        )
        destroy(relaunched)
    }

    @Test
    fun bridgeReadsExtrasAndFinishesOnUiThread() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val kartController =
            Robolectric.buildActivity(
                KartPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.SEVEN_YEARS,
                    Mascot.BRAVE_PLUMBER,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.THREE_D,
                ),
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
        assertTrue(project.contains("GL Compatibility"))
        assertTrue(project.contains("boot_splash/show_image=false"))
        assertTrue(project.contains("res://boot.tscn"))
        assertTrue(project.contains("import_etc2_astc=true"))
        assertTrue(project.contains("default_clear_color"))
        assertTrue(project.contains("aspect=\"expand\""))
        assertTrue(project.contains("mode=\"disabled\""))
        val bootScene = ctx.assets.open("boot.tscn").bufferedReader().readText()
        assertTrue(bootScene.contains("res://boot.gd"))
        val boot = ctx.assets.open("boot.gd").bufferedReader().readText()
        assertTrue(boot.contains("get_visible_rect"))
        assertTrue(boot.contains("DisplayServer"))
        assertTrue(boot.contains("_fit_pixels"))
        assertFalse(boot.contains("force_draw"))
        assertTrue(boot.contains("change_scene_to_file"))
        assertTrue(boot.contains("Host.finish"))
        assertTrue(boot.contains("ResourceLoader.exists"))
        assertTrue(boot.contains("CONTENT_SCALE_MODE_DISABLED"))
        val kartScene = ctx.assets.open("kart.tscn").bufferedReader().readText()
        assertTrue(kartScene.contains("type=\"Node2D\""))
        ctx.assets.open("runner.tscn").close()
        val kartScript = ctx.assets.open("kart.gd").bufferedReader().readText()
        assertTrue(kartScript.contains("minf(delta"))
        assertTrue(kartScript.contains("_update_hud(boost_timer > 0.0)"))
        assertTrue(kartScript.contains("Arcos"))
        assertTrue(kartScript.contains("_draw_meta"))
        assertTrue(kartScript.contains("DRAW_AHEAD"))
        assertTrue(kartScript.contains("META"))
        assertTrue(kartScript.contains("_draw_bands"))
        assertTrue(kartScript.contains("_draw_strip"))
        assertTrue(kartScript.contains("_draw_hills"))
        assertTrue(kartScript.contains("_cache_palette"))
        assertTrue(kartScript.contains("_steer_at"))
        assertTrue(kartScript.contains("_draw_rivals"))
        assertTrue(kartScript.contains("DEADZONE"))
		assertTrue(kartScript.contains("Host.style_hud"))
        val runnerScript = ctx.assets.open("runner.gd").bufferedReader().readText()
        assertTrue(runnerScript.contains("minf(delta"))
        assertTrue(runnerScript.contains("in_pit_fall"))
        assertTrue(runnerScript.contains("_add_part"))
        assertTrue(runnerScript.contains("JUMP_FLICK"))
        assertTrue(runnerScript.contains("flick_y"))
        assertTrue(runnerScript.contains("_run_from"))
        assertTrue(runnerScript.contains("_bump_enemies"))
        ctx.assets.open("invaders.tscn").close()
        ctx.assets.open("chomp.tscn").close()
        ctx.assets.open("climb.tscn").close()
        val invadersScript = ctx.assets.open("invaders.gd").bufferedReader().readText()
        assertTrue(invadersScript.contains("Vidas"))
        assertTrue(invadersScript.contains("LIVES_MAX"))
        assertFalse(invadersScript.contains("hits >= 8"))
        val hostScript = ctx.assets.open("host.gd").bufferedReader().readText()
        assertTrue(hostScript.contains("screen_size"))
        assertTrue(hostScript.contains("create_timer"))
        val chompScript = ctx.assets.open("chomp.gd").bufferedReader().readText()
        assertTrue(chompScript.contains("bolinhas"))
        val climbScript = ctx.assets.open("climb.gd").bufferedReader().readText()
        assertTrue(climbScript.contains("barris") || climbScript.contains("Letras"))
        ctx.assets.open("host.gd").close()
        assertEquals("res://kart.tscn", GodotBridge.rewardScene(""))
        assertEquals("res://runner.tscn", GodotBridge.rewardScene(GodotRuntime.SCENE_RUNNER))
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
                .filter {
                    it.name.contains("Plugin") ||
                        it.name.endsWith("Kart3dActivity") ||
                        it.name.endsWith("Platformer2dActivity")
                }
                .all { EnginePluginContract.isIsolatedProcessName(it.processName.orEmpty()) },
        )
        assertFalse(activities.any { it.name.contains("ProcessPhoenix") })
        val providers =
            ctx.packageManager
                .getPackageInfo(ctx.packageName, PackageManager.GET_PROVIDERS)
                .providers
                ?.toList()
                .orEmpty()
        assertFalse(providers.any { it.name.contains("FileProvider") })
    }

    @Test
    fun isolatedProcessNameSkipsRoomAndNativeActivitiesStillStart() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertFalse(EnginePluginContract.isIsolatedProcessName(currentProcessName()))
        assertTrue(shouldOpenContainer("pt.mataventuras.app"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine3d"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:engine2d"))
        assertFalse(shouldOpenContainer("pt.mataventuras.app:phoenix"))
        assertFalse(shouldOpenContainer(""))
        assertFalse(shouldOpenContainer("   "))
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
        assertFalse(app.bindHostGraph(""))
        val nativeKartController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup()
        nativeKartController.get().closeFinished()
        assertTrue(nativeKartController.get().isRewardSettled())
        assertFalse(nativeKartController.get().completeReward(ok = false))
        nativeKartController.get().stopEngineSurface()
        nativeKartController.get().completeRewardOnUi(true)
        nativeKartController.pause()
        nativeKartController.resume()
        nativeKartController.get().pauseEngineSurface()
        nativeKartController.get().resumeEngineSurface()
        nativeKartController.get().pauseableSurface =
            android.opengl.GLSurfaceView(nativeKartController.get()).also { view ->
                view.setEGLContextClientVersion(1)
                view.setRenderer(
                    object : android.opengl.GLSurfaceView.Renderer {
                        override fun onSurfaceCreated(
                            gl: javax.microedition.khronos.opengles.GL10?,
                            config: javax.microedition.khronos.egl.EGLConfig?,
                        ) = Unit

                        override fun onSurfaceChanged(
                            gl: javax.microedition.khronos.opengles.GL10?,
                            width: Int,
                            height: Int,
                        ) = Unit

                        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) = Unit
                    },
                )
            }
        nativeKartController.get().pauseEngineSurface()
        nativeKartController.get().resumeEngineSurface()
        nativeKartController.get().stopEngineSurface()
        destroy(nativeKartController)
        assertTrue(nativeKartController.get().isDestroyed)
        nativeKartController.get().completeRewardOnUi(ok = false)
        assertFalse(nativeKartController.get().completeReward(ok = true))
        val namelessController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup"),
            ).setup()
        assertEquals("", namelessController.get().childName())
        namelessController.get().finish()
        assertFalse(namelessController.get().completeReward(ok = true))
        destroy(namelessController)
        assertTrue(namelessController.get().isDestroyed)
        assertFalse(namelessController.get().completeReward(ok = false))
        val nativeRunnerController =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                android.content.Intent(ctx, Platformer2dActivity::class.java)
                    .putExtra(EngineLauncher.EXTRA_MASCOT, "hero_pup")
                    .putExtra(EngineLauncher.EXTRA_NAME, "Ana"),
            ).setup()
        nativeRunnerController.get().completeReward(ok = true)
        assertTrue(nativeRunnerController.get().isRewardSettled())
        assertFalse(nativeRunnerController.get().completeReward(ok = false))
        destroy(nativeRunnerController)
        nativeRunnerController.get().completeRewardOnUi(ok = true)
        assertFalse(nativeRunnerController.get().completeReward(ok = true))
    }

    private fun destroy(controller: ActivityController<*>) {
        controller.pause().stop().destroy()
    }
}
