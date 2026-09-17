package it.palsoftware.pastiera

import android.content.Context
import android.text.InputType
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
            .remove("treat_non_text_fields_as_text")
            .remove("non_text_field_packages")
            .commit()
    }

    @After
    fun tearDown() {
        SettingsManager.getPreferences(context).edit()
            .remove("treat_non_text_fields_as_text")
            .remove("non_text_field_packages")
            .commit()
    }

    private fun shouldTreat(
        packageName: String? = null,
        extraPackageName: String? = null,
        inputType: Int = EditorInfo.TYPE_NULL
    ): Boolean = SettingsManager.shouldTreatNonTextFieldAsText(
        context,
        packageName = packageName,
        extraPackageName = extraPackageName,
        inputType = inputType
    )

    @Test
    fun defaultPackagesIncludeTermuxAndStayOff() {
        assertFalse(SettingsManager.getTreatNonTextFieldsAsText(context))
        assertEquals(
            setOf("com.termux"),
            SettingsManager.getNonTextFieldPackages(context)
        )
    }

    @Test
    fun settingOffKeepsListedPackageDisabled() {
        assertFalse(shouldTreat(packageName = "com.termux"))
    }

    @Test
    fun settingOnTreatsListedPackageTypeNullAsText() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertTrue(shouldTreat(packageName = "com.termux", inputType = EditorInfo.TYPE_NULL))
    }

    @Test
    fun settingOnTreatsListedPackageClassZeroAsText() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertTrue(shouldTreat(packageName = "com.termux", inputType = 0x80090))
    }

    @Test
    fun listedPackageRealTextFieldIsIgnored() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertFalse(
            shouldTreat(
                packageName = "com.termux",
                inputType = InputType.TYPE_CLASS_TEXT
            )
        )
    }

    @Test
    fun unlistedPackageIsIgnoredEvenWhenEnabled() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertFalse(shouldTreat(packageName = "com.google.android.apps.maps"))
    }

    @Test
    fun unlistedPackageClassZeroIsIgnored() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertFalse(
            shouldTreat(
                packageName = "com.android.launcher3",
                inputType = 0x80090
            )
        )
    }

    @Test
    fun classZeroWithoutPackageIsIgnored() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertFalse(shouldTreat(inputType = 0x80090))
    }

    @Test
    fun extraPackageNameCanMatchAllowlist() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertTrue(
            shouldTreat(
                extraPackageName = "com.termux",
                inputType = EditorInfo.TYPE_NULL
            )
        )
    }

    @Test
    fun customPackageListIsExactMatch() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        SettingsManager.setNonTextFieldPackagesRaw(context, "com.termux.nix, org.example.term")
        assertTrue(shouldTreat(packageName = "com.termux.nix"))
        assertTrue(shouldTreat(packageName = "org.example.term"))
        assertFalse(shouldTreat(packageName = "com.termux"))
    }

    @Test
    fun emptyPackageListIsNoOpEvenWhenEnabled() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        SettingsManager.setNonTextFieldPackagesRaw(context, "")
        assertEquals(emptySet<String>(), SettingsManager.getNonTextFieldPackages(context))
        assertFalse(shouldTreat(packageName = "com.termux"))
        assertFalse(shouldTreat(packageName = "com.termux", inputType = 0x80090))
    }

    @Test
    fun whitespaceOnlyPackageListIsNoOp() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        SettingsManager.setNonTextFieldPackagesRaw(context, "  , ; \n ")
        assertEquals(emptySet<String>(), SettingsManager.getNonTextFieldPackages(context))
        assertFalse(shouldTreat(packageName = "com.termux"))
    }

    @Test
    fun prefixDoesNotMatchRelatedPackages() {
        SettingsManager.setTreatNonTextFieldsAsText(context, true)
        assertFalse(
            shouldTreat(
                packageName = "com.termux.styling",
                inputType = EditorInfo.TYPE_NULL
            )
        )
        assertFalse(
            shouldTreat(
                packageName = "com.termux.app",
                inputType = EditorInfo.TYPE_NULL
            )
        )
    }
}
