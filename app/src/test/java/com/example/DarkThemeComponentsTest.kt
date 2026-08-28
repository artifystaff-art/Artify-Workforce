package com.example

import androidx.compose.ui.graphics.Color
import com.example.ui.components.*
import com.example.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DarkThemeComponentsTest {

    @Test
    fun `test DarkThemeCardVariant values`() {
        val variants = DarkThemeCardVariant.values()
        assertEquals(5, variants.size)
        assertNotNull(DarkThemeCardVariant.Surface)
        assertNotNull(DarkThemeCardVariant.Elevated)
        assertNotNull(DarkThemeCardVariant.Outlined)
        assertNotNull(DarkThemeCardVariant.Accent)
        assertNotNull(DarkThemeCardVariant.Subtle)
    }

    @Test
    fun `test DarkThemeButtonVariant values`() {
        val variants = DarkThemeButtonVariant.values()
        assertEquals(6, variants.size)
        assertNotNull(DarkThemeButtonVariant.Primary)
        assertNotNull(DarkThemeButtonVariant.Secondary)
        assertNotNull(DarkThemeButtonVariant.Success)
        assertNotNull(DarkThemeButtonVariant.Warning)
        assertNotNull(DarkThemeButtonVariant.Error)
        assertNotNull(DarkThemeButtonVariant.Neutral)
    }

    @Test
    fun `test DarkThemeButtonSize values`() {
        val sizes = DarkThemeButtonSize.values()
        assertEquals(3, sizes.size)
        assertEquals(36, DarkThemeButtonSize.Small.height.value.toInt())
        assertEquals(46, DarkThemeButtonSize.Medium.height.value.toInt())
        assertEquals(54, DarkThemeButtonSize.Large.height.value.toInt())
    }

    @Test
    fun `test DarkThemeStatusType values`() {
        val types = DarkThemeStatusType.values()
        assertEquals(6, types.size)
        assertNotNull(DarkThemeStatusType.Success)
        assertNotNull(DarkThemeStatusType.Warning)
        assertNotNull(DarkThemeStatusType.Error)
        assertNotNull(DarkThemeStatusType.Primary)
        assertNotNull(DarkThemeStatusType.Secondary)
        assertNotNull(DarkThemeStatusType.Neutral)
    }
}
