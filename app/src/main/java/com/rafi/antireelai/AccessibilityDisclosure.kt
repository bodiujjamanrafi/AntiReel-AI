package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Explicit consent with a bounded reading area and permanently reachable actions. */
@Composable internal fun AccessibilityDisclosure(onAccept:()->Unit,onDecline:()->Unit) {
    val language=LocalUiLanguage.current
    SettingsTheme {
        Dialog(onDismissRequest=onDecline) {
            Surface(shape=RoundedCornerShape(24.dp),color=AppStyle.Surface,contentColor=AppStyle.Text) {
                Column(Modifier.fillMaxWidth().heightIn(max=620.dp).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        AppGlyph(12,AppStyle.Accent,Modifier.size(28.dp))
                        UiText(localized(language,"Accessibility access","অ্যাক্সেসিবিলিটি অ্যাক্সেস"),modifier=Modifier.weight(1f),fontSize=21.sp,fontWeight=FontWeight.SemiBold)
                    }
                    Column(Modifier.weight(1f,false).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        UiText(AccessibilityConsent.disclosure(language),fontSize=14.sp,lineHeight=22.sp,color=AppStyle.Text)
                        UiText(localized(language,"Android will ask for separate approval next. Declining keeps the rest of AntiReel available.","এরপর অ্যান্ড্রয়েড আলাদা অনুমতি চাইবে। না বললেও অ্যান্টিরিলের অন্য অংশ ব্যবহার করা যাবে।"),fontSize=13.sp,lineHeight=20.sp,color=AppStyle.Muted)
                    }
                    HorizontalDivider(color=AppStyle.Outline)
                    Button(onClick=onAccept,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=AppStyle.Accent,contentColor=AppStyle.Background)) {
                        UiText(localized(language,"I agree · open Settings","সম্মত · সেটিংস খুলি"),fontWeight=FontWeight.SemiBold)
                    }
                    TextButton(onClick=onDecline,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){UiText(localized(language,"Not now","এখন নয়"),color=AppStyle.Muted)}
                }
            }
        }
    }
}
