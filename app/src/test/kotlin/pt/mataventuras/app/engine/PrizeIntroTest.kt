package pt.mataventuras.app.engine

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.DEBUG_WIN_TAG
import pt.mataventuras.app.debugInstantWin
import pt.mataventuras.domain.engine.PrizeHud

@RunWith(RobolectricTestRunner::class)
class PrizeIntroTest {
    @Test
    fun jogarRemovesTheCard() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        PrizeIntro.attach(activity, "KART", embed = false)
        assertNull(activity.window.decorView.findViewWithTag<View>("prize-intro"))
        PrizeIntro.attach(activity, "KART", embed = true)
        assertTrue(PrizeIntro.isHolding(activity))
        PrizeIntro.attach(activity, "KART", embed = true)
        val card = activity.window.decorView.findViewWithTag<ViewGroup>("prize-intro")
        assertNotNull(card)
        val button =
            (0 until card!!.childCount)
                .map { card.getChildAt(it) }
                .filterIsInstance<Button>()
                .single()
        assertEquals(PrizeHud.PLAY_LABEL, button.text.toString())
        (card.parent as ViewGroup).removeView(card)
        button.performClick()
        PrizeIntro.attach(activity, "RUNNER", embed = true)
        val again = activity.window.decorView.findViewWithTag<ViewGroup>("prize-intro")
        val live =
            (0 until again!!.childCount)
                .map { again.getChildAt(it) }
                .filterIsInstance<Button>()
                .single()
        live.performClick()
        assertNull(activity.window.decorView.findViewWithTag<View>("prize-intro"))
        assertFalse(PrizeIntro.isHolding(activity))
        PrizeIntro.release(activity)
        assertFalse(PrizeIntro.isHolding(activity))
    }

    @Test
    fun debugWinAppearsAfterJogarOnlyOnDebugBuilds() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        var wins = 0
        debugInstantWin = false
        PrizeIntro.attach(activity, "KART", embed = true, onDebugWin = { wins += 1 })
        clickPlay(activity)
        assertNull(activity.window.decorView.findViewWithTag<Button>(DEBUG_WIN_TAG))
        assertEquals(0, wins)
        debugInstantWin = true
        PrizeIntro.attach(activity, "RUNNER", embed = true, onDebugWin = { wins += 1 })
        clickPlay(activity)
        val win = activity.window.decorView.findViewWithTag<Button>(DEBUG_WIN_TAG)
        assertNotNull(win)
        assertEquals(DEBUG_WIN_TAG, win!!.text.toString())
        win.performClick()
        assertEquals(1, wins)
        PrizeIntro.showDebugWin(activity) { wins += 1 }
        assertEquals(1, wins)
        debugInstantWin = true
    }

    private fun clickPlay(activity: Activity) {
        val card = activity.window.decorView.findViewWithTag<ViewGroup>("prize-intro")
        val play =
            (0 until card!!.childCount)
                .map { card.getChildAt(it) }
                .filterIsInstance<Button>()
                .single { it.text.toString() == PrizeHud.PLAY_LABEL }
        play.performClick()
    }
}
