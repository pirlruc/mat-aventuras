package pt.mataventuras.app

import android.os.Looper
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import pt.mataventuras.app.engine.EngineAttachRetries
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.GodotBridge
import pt.mataventuras.app.engine.GodotRewardBinder
import pt.mataventuras.app.engine.PrizeBoot
import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.plugin.RunnerPluginActivity

@RunWith(RobolectricTestRunner::class)
class PrizeBootTest {
    @Test
    fun overlayRetriesThenOpensTheCanvasHost() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeBand.THREE_TO_FIVE,
                    Mascot.SPEEDY_HEDGEHOG,
                    "Ana",
                    game = RewardGame.RUNNER,
                ),
            ).setup()
        val activity = controller.get()
        assertTrue(GodotBridge.youngBand(activity))
        assertEquals("♥♥♥", GodotBridge.livesLine(activity, 3, 3))
        PrizeBoot.markLoading(activity)
        PrizeBoot.timedOut(activity)
        var scenes = 0
        GodotRewardBinder.bindRunner(activity, embed = true) { _, scene ->
            scenes += 1
            assertTrue(scene.contains("runner"))
        }
        assertEquals(1, scenes)
        val status = activity.window.decorView.findViewWithTag<TextView>("prize-boot-status")
        assertEquals(PrizeHud.LOADING_LINE, status.text.toString())
        PrizeBoot.arm(activity)
        shadowOf(Looper.getMainLooper()).idleFor(PrizeBoot.TIMEOUT_MS + 50, TimeUnit.MILLISECONDS)
        val retry = activity.window.decorView.findViewWithTag<Button>("prize-boot-retry")
        val back = activity.window.decorView.findViewWithTag<Button>("prize-boot-back")
        assertEquals(View.VISIBLE, retry.visibility)
        retry.performClick()
        assertEquals(2, scenes)
        PrizeBoot.markLoading(activity)
        assertEquals(View.GONE, retry.visibility)
        GodotBridge.prizeReady(activity)
        val root = activity.window.decorView.findViewWithTag<View>("prize-boot-root")
        assertEquals(View.GONE, root.visibility)
        PrizeBoot.timedOut(activity)
        assertEquals(View.VISIBLE, back.visibility)
        back.performClick()
        assertNotNull(activity.loop)
        PrizeBoot.hide(activity)
        PrizeBoot.cancel(activity)
        controller.destroy()
    }

    @Test
    fun secondShowReusesTheOverlay() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeBand.THREE_TO_FIVE, Mascot.HERO_PUP, "Ana", game = RewardGame.CLIMB),
            ).setup()
        val activity = controller.get()
        PrizeBoot.ready(activity)
        PrizeBoot.keepAboveEngine(activity)
        PrizeBoot.markLoading(activity)
        PrizeBoot.timedOut(activity)
        PrizeBoot.hide(activity)
        PrizeBoot.show(activity, "🐶", onRetry = {}, onBack = {})
        PrizeBoot.show(activity, "🐶", onRetry = {}, onBack = {})
        activity.findViewById<ViewGroup>(android.R.id.content).addView(SurfaceView(activity))
        PrizeBoot.keepAboveEngine(activity)
        val status = activity.window.decorView.findViewWithTag<TextView>("prize-boot-status")
        assertEquals(PrizeHud.LOADING_LINE, status.text.toString())
        activity.acceptRelaunch(activity.intent)
        controller.destroy()
    }

    @Test
    fun olderBandKeepsTheVidasLine() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeBand.SIX_TO_EIGHT,
                    Mascot.HERO_PUP,
                    "Rui",
                    game = RewardGame.INVADERS,
                ),
            ).setup()
        val activity = controller.get()
        assertFalse(GodotBridge.youngBand(activity))
        assertEquals("Vidas 1/5", GodotBridge.livesLine(activity, 1, 5))
        val kart =
            Robolectric.buildActivity(
                pt.mataventuras.plugin.KartPluginActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeBand.SIX_TO_EIGHT,
                    Mascot.BRAVE_PLUMBER,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.THREE_D,
                ),
            ).setup()
        kart.get().acceptRelaunch(kart.get().intent)
        assertNotNull(kart.get().nativeSession)
        kart.destroy()
        controller.destroy()
    }

    @Test
    fun destroyDropsDelayedAttach() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller =
            Robolectric.buildActivity(
                RunnerPluginActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeBand.THREE_TO_FIVE, Mascot.PINK_PIGLET, "Eva", game = RewardGame.CHOMP),
            ).setup()
        val activity = controller.get()
        val view = View(activity)
        var ran = false
        val listener =
            View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                ran = true
            }
        val retry = Runnable { ran = true }
        val force = Runnable { ran = true }
        view.postDelayed(retry, 1_000)
        view.postDelayed(force, 2_000)
        EngineAttachRetries.track(activity, view, listener, retry, force)
        controller.destroy()
        shadowOf(Looper.getMainLooper()).idleFor(3, TimeUnit.SECONDS)
        assertFalse(ran)
        EngineAttachRetries.cancel(activity)
        PrizeBoot.cancel(activity)
    }
}
