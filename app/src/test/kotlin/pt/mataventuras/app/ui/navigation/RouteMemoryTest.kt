package pt.mataventuras.app.ui.navigation

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class RouteMemoryTest {
    @Test
    fun savedHandleRestoresTheOpenLesson() {
        val saved = SavedStateHandle()
        RouteMemory(saved).remember(RouteMemory.LESSON, 7, LearningModule.ADDITION.name)
        val memory = RouteMemory(saved)
        assertEquals(RouteMemory.LESSON, memory.route())
        assertEquals(7L, memory.profileId())
        assertEquals(LearningModule.ADDITION.name, memory.moduleName())
        val lesson = RouteMemory.destination(memory.route(), memory.moduleName())
        assertEquals(LearningModule.ADDITION, (lesson as Destination.Lesson).module)
        assertTrue(RouteMemory.destination(RouteMemory.HOME, "") is Destination.Home)
        assertTrue(RouteMemory.destination(RouteMemory.LEADERBOARD, "") is Destination.Leaderboard)
        assertTrue(RouteMemory.destination(RouteMemory.PARENTS, "") is Destination.Parents)
        RouteMemory(saved).remember(RouteMemory.HOME, 7)
        assertEquals(RouteMemory.HOME, RouteMemory(saved).route())
        assertEquals("", RouteMemory(saved).moduleName())
        assertEquals(RouteMemory.HOME, RouteMemory.routeOf(Destination.Home))
        assertEquals("", RouteMemory.moduleOf(Destination.Home))
        assertEquals(RouteMemory.LEADERBOARD, RouteMemory.routeOf(Destination.Leaderboard))
        assertEquals(RouteMemory.PARENTS, RouteMemory.routeOf(Destination.Parents))
        assertEquals(RouteMemory.SELECTION, RouteMemory.routeOf(Destination.Selection))
        assertEquals(RouteMemory.LESSON, RouteMemory.routeOf(Destination.Lesson(LearningModule.COUNTING)))
        val unknown = RouteMemory.destination(RouteMemory.LESSON, "NO_SUCH")
        assertEquals(LearningModule.COUNTING, (unknown as Destination.Lesson).module)
        assertTrue(RouteMemory.destination("other", "") is Destination.Selection)
        assertEquals(0L, RouteMemory(SavedStateHandle()).profileId())
    }
}
