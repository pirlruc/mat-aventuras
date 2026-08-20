package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.EngineKind

class EnginePluginContractTest {
    @Test
    fun processAndClassNamesMatchKind() {
        assertEquals(EnginePluginContract.PROCESS_ENGINE_2D, EnginePluginContract.processFor(EngineKind.TWO_D))
        assertEquals(EnginePluginContract.PROCESS_ENGINE_3D, EnginePluginContract.processFor(EngineKind.THREE_D))
        assertEquals(
            EnginePluginContract.PLUGIN_RUNNER_CLASS,
            EnginePluginContract.pluginClassName(EngineKind.TWO_D),
        )
        assertEquals(
            EnginePluginContract.PLUGIN_KART_CLASS,
            EnginePluginContract.pluginClassName(EngineKind.THREE_D),
        )
    }

    @Test
    fun isolatedProcessRequiredForPluginsAndAll3d() {
        assertFalse(EnginePluginContract.requiresIsolatedProcess(EngineKind.TWO_D, usingPlugin = false))
        assertTrue(EnginePluginContract.requiresIsolatedProcess(EngineKind.TWO_D, usingPlugin = true))
        assertTrue(EnginePluginContract.requiresIsolatedProcess(EngineKind.THREE_D, usingPlugin = false))
        assertTrue(EnginePluginContract.requiresIsolatedProcess(EngineKind.THREE_D, usingPlugin = true))
    }

    @Test
    fun internetAndNetworkPermissionsAreForbidden() {
        assertTrue(EnginePluginContract.isForbiddenPermission("android.permission.INTERNET"))
        assertTrue(EnginePluginContract.isForbiddenPermission("INTERNET"))
        assertTrue(EnginePluginContract.isForbiddenPermission("android.permission.ACCESS_NETWORK_STATE"))
        assertTrue(EnginePluginContract.isForbiddenPermission("ACCESS_WIFI_STATE"))
        assertTrue(EnginePluginContract.isForbiddenPermission("CHANGE_NETWORK_STATE"))
        assertFalse(EnginePluginContract.isForbiddenPermission("android.permission.VIBRATE"))
        assertTrue(EnginePluginContract.manifestAllowed(emptySet()))
        assertTrue(EnginePluginContract.manifestAllowed(setOf("VIBRATE")))
        assertFalse(EnginePluginContract.manifestAllowed(setOf("android.permission.INTERNET")))
    }

    @Test
    fun launchExtrasCarryMascotAndName() {
        val extras = EnginePluginContract.launchExtras("hero_pup", "Ana")
        assertEquals("hero_pup", extras[EnginePluginContract.EXTRA_MASCOT])
        assertEquals("Ana", extras[EnginePluginContract.EXTRA_NAME])
        assertEquals("finished", EnginePluginContract.RESULT_FINISHED)
        assertEquals("godot", EnginePluginContract.ADOPTED_ENGINE)
        assertTrue(EnginePluginContract.isIsolatedProcessName("pt.mataventuras.app:engine3d"))
        assertTrue(EnginePluginContract.isIsolatedProcessName("pt.mataventuras.app:engine2d"))
        assertFalse(EnginePluginContract.isIsolatedProcessName("pt.mataventuras.app"))
    }

    @Test
    fun resolverPrefersPluginWhenPresentOtherwiseNative() {
        val nativeTwo = "native.Two"
        val nativeThree = "native.Three"
        val always = { _: String -> true }
        val never = { _: String -> false }
        assertEquals(
            EnginePluginContract.PLUGIN_RUNNER_CLASS,
            EnginePluginResolver.classNameFor(EngineKind.TWO_D, always, nativeTwo, nativeThree),
        )
        assertEquals(
            EnginePluginContract.PLUGIN_KART_CLASS,
            EnginePluginResolver.classNameFor(EngineKind.THREE_D, always, nativeTwo, nativeThree),
        )
        assertEquals(
            nativeTwo,
            EnginePluginResolver.classNameFor(EngineKind.TWO_D, never, nativeTwo, nativeThree),
        )
        assertEquals(
            nativeThree,
            EnginePluginResolver.classNameFor(EngineKind.THREE_D, never, nativeTwo, nativeThree),
        )
    }

    @Test
    fun inputMapMatchesNativeKartBands() {
        assertEquals(-1f, EngineInputMap.steerFromNormalizedX(0.1f), 0.001f)
        assertEquals(0f, EngineInputMap.steerFromNormalizedX(0.5f), 0.001f)
        assertEquals(1f, EngineInputMap.steerFromNormalizedX(0.9f), 0.001f)
        assertTrue(EngineInputMap.isBoostBand(0.34f))
        assertTrue(EngineInputMap.isBoostBand(0.66f))
        assertFalse(EngineInputMap.isBoostBand(0.33f))
    }
}
