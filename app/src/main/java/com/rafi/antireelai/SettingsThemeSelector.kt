package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Settings-only selector; onboarding retains its existing presentation. */
@Composable internal fun SettingsThemeSelector(mode:String,onChange:(String)->Unit) {
    val deviceDark=isSystemInDarkTheme()
    Row(Modifier.fillMaxWidth().selectableGroup(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("system" to "Device","light" to "Light","dark" to "Dark").forEach { (key,label)->
            val selected=mode==key
            val palette=AppStyle.colorsFor(if(key=="system")deviceDark else key=="dark")
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if(selected)AppStyle.AccentSoft else AppStyle.Background)
                .border(if(selected)1.5f.dp else .7f.dp,if(selected)AppStyle.Accent else AppStyle.Frame,RoundedCornerShape(12.dp))
                .selectable(selected,role=Role.RadioButton,onClick={onChange(key)}).testTag("theme-$key").padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Column(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(8.dp)).background(palette.background).padding(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(12.dp).background(palette.accent,RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth(.8f).height(4.dp).background(palette.text.copy(alpha=.6f),RoundedCornerShape(2.dp)))
                    Box(Modifier.fillMaxWidth().height(12.dp).background(palette.surface,RoundedCornerShape(4.dp)))
                }
                UiText(label,fontSize=14.sp,lineHeight=20.sp,minLines=2,textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Normal,color=AppStyle.Text)
                RadioButton(selected,onClick=null,modifier=Modifier.size(24.dp))
            }
        }
    }
}
