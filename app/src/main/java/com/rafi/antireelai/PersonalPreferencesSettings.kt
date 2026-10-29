package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun PersonalPreferencesSettings(store:TrackerStore,revision:Int,tap:()->Unit,onReview:()->Unit) {
    @Suppress("UNUSED_VARIABLE") val refreshed=revision
    val language=store.prefs.getString("language","en") ?: "en"
    val name=store.prefs.getString("userName","").orEmpty()
    SettingsGroup {
        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Mascot(store,store.total(),Modifier.size(64.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(name.ifBlank{localized(language,"Your profile","তোমার পরিচিতি")},fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
                SettingsText("Your preferences stay on this phone.")
            }
        }
    }
    SettingsGroup("Language") {
        Column(Modifier.selectableGroup()) {
            listOf("en" to "English","bn" to "বাংলা").forEachIndexed {index,(id,label)->
                Row(Modifier.fillMaxWidth().selectable(language==id,role=Role.RadioButton,onClick={tap();store.prefs.edit().putString("language",id).apply()}).testTag("language-$id").heightIn(min=56.dp).padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AppGlyph(11,AppStyle.Accent,Modifier.size(22.dp))
                    Text(label,fontSize=16.sp,color=AppStyle.Text,modifier=Modifier.weight(1f))
                    RadioButton(language==id,onClick=null)
                }
                if(index==0)SettingsRule()
            }
        }
    }
    SettingsStats(Triple(if(store.dailyMinutes>0)"${store.dailyMinutes} min" else "Not set","Daily time",24),Triple("${store.baseGoal}","Reels per day",23))
    SettingsGroup("Preferences") {
        SettingsPreferenceSwitch(store,"onboardingMuted","Miso's voice","Greetings and spoken reminders",6,default=false,inverted=true,modifier=Modifier.testTag("preference-voice")){enabled->tap();store.prefs.edit().putBoolean("onboardingMuted",!enabled).putBoolean("voiceEnabled",enabled).putBoolean("goalReminders",enabled).apply()}
        SettingsRule()
        SettingsRow("Edit my answers",18,"Review your goals and preferences",onClick={tap();onReview()})
    }
}
