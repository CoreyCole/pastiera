package it.palsoftware.pastiera

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CommitTextOnNullFieldTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val inputConnection: InputConnection = mock(InputConnection::class.java)

    @Before
    fun setUp() {
        SettingsManager.getPreferences(context).edit()
            .remove("commit_text_on_null_fields")
            .remove("commit_text_null_packages")
            .commit()
    }

    @After
    fun tearDown() {
        SettingsManager.getPreferences(context).edit()
            .remove("commit_text_on_null_fields")
            .remove("commit_text_null_packages")
            .commit()
    }

    @Test
    fun defaultPackagesIncludeTermuxAndStayOff() {
        assertFalse(SettingsManager.getCommitTextOnNullFields(context))
        assertEquals(
            setOf("com.termux", "com.termux.nix", "com.termux.app", "com.termux.styling"),
            SettingsManager.getCommitTextNullPackages(context)
        )
    }

    @Test
    fun settingOffKeepsTermuxOnNoEditablePath() {
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = EditorInfo.TYPE_NULL
        }
        assertFalse(SettingsManager.shouldCommitTextOnNullField(context, info, inputConnection))
    }

    @Test
    fun settingOnCommitsMappedKeysForTermuxTypeNull() {
        SettingsManager.setCommitTextOnNullFields(context, true)
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = EditorInfo.TYPE_NULL
        }
        assertTrue(SettingsManager.shouldCommitTextOnNullField(context, info, inputConnection))
    }

    @Test
    fun settingOnCommitsMappedKeysForTermuxCharBasedInput() {
        SettingsManager.setCommitTextOnNullFields(context, true)
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = 0x80090
        }
        assertTrue(SettingsManager.shouldCommitTextOnNullField(context, info, inputConnection))
    }

    @Test
    fun unlistedPackageIsIgnoredEvenWhenEnabled() {
        SettingsManager.setCommitTextOnNullFields(context, true)
        val info = EditorInfo().apply {
            packageName = "com.google.android.apps.maps"
            inputType = EditorInfo.TYPE_NULL
        }
        assertFalse(SettingsManager.shouldCommitTextOnNullField(context, info, inputConnection))
    }

    @Test
    fun missingInputConnectionStillForcesCommitTextForTermux() {
        SettingsManager.setCommitTextOnNullFields(context, true)
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = EditorInfo.TYPE_NULL
        }
        assertTrue(SettingsManager.shouldCommitTextOnNullField(context, info, null))
    }

    @Test
    fun charBasedInputForcesCommitTextEvenWithoutPackage() {
        SettingsManager.setCommitTextOnNullFields(context, true)
        val info = EditorInfo().apply {
            inputType = 0x80090
        }
        assertTrue(SettingsManager.shouldCommitTextOnNullField(context, info, inputConnection))
    }
}
