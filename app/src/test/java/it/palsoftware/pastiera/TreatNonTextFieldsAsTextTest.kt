package it.palsoftware.pastiera

import android.content.Context
import android.view.inputmethod.EditorInfo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TreatNonTextFieldsAsTextTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

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
        assertFalse(SettingsManager.getTreatNonTextFieldsAsText(context))
        assertEquals(
            setOf("com.termux", "com.termux.nix", "com.termux.app", "com.termux.styling"),
            SettingsManager.getNonTextFieldPackages(context)
        )
    }

    @Test
    fun settingOffKeepsListedPackageDisabled() {
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = EditorInfo.TYPE_NULL
        }
        assertFalse(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun settingOnTreatsListedPackageTypeNullAsText() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = EditorInfo.TYPE_NULL
        }
        assertTrue(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun settingOnTreatsListedPackageClassZeroAsText() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            packageName = "com.termux"
            inputType = 0x80090
        }
        assertTrue(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun unlistedPackageIsIgnoredEvenWhenEnabled() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            packageName = "com.google.android.apps.maps"
            inputType = EditorInfo.TYPE_NULL
        }
        assertFalse(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun unlistedPackageClassZeroIsIgnored() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            packageName = "com.android.launcher3"
            inputType = 0x80090
        }
        assertFalse(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun classZeroWithoutPackageIsIgnored() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            inputType = 0x80090
        }
        assertFalse(SettingsManager.shouldTreatNonTextFieldAsText(context, info))
    }

    @Test
    fun extraPackageNameCanMatchAllowlist() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        val info = EditorInfo().apply {
            inputType = EditorInfo.TYPE_NULL
        }
        assertTrue(
            SettingsManager.shouldTreatNonTextFieldAsText(
                context,
                info,
                extraPackageName = "com.termux.nix"
            )
        )
    }
}
