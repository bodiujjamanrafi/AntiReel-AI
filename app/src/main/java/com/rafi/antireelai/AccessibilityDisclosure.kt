package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Standalone prominent disclosure; dismissal/back is never consent. */
@Composable internal fun AccessibilityDisclosure(onAccept:()->Unit,onDecline:()->Unit) {
    val language=LocalUiLanguage.current
    SettingsTheme {
        AlertDialog(onDismissRequest=onDecline,
            title={UiText(localized(language,"Accessibility access","অ্যাক্সেসিবিলিটি অ্যাক্সেস"))},
            text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                UiText(AccessibilityConsent.disclosure(language))
                UiText(localized(language,"Android will ask for separate approval next. Declining keeps the rest of AntiReel available.","এরপর অ্যান্ড্রয়েড আলাদা অনুমতি চাইবে। না বললেও অ্যান্টিরিলের অন্য অংশ ব্যবহার করা যাবে।"))
            }},
            confirmButton={TextButton(onClick=onAccept){UiText(localized(language,"I agree · open Settings","সম্মত · সেটিংস খুলি"))}},
            dismissButton={TextButton(onClick=onDecline){UiText(localized(language,"Not now","এখন নয়"))}})
    }
}
