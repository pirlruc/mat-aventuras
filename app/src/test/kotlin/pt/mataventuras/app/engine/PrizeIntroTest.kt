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
}
