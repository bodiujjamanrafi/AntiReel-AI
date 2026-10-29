package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared previews keep onboarding and settings on the same persisted theme values. */
@Composable internal fun ThemeChoices(mode:String,language:String,onChange:(String)->Unit) {
    val deviceDark=isSystemInDarkTheme()
    Row(Modifier.fillMaxWidth().selectableGroup(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        listOf("system","light","dark").forEach {key->
            val selected=mode==key
            val preview=AppStyle.colorsFor(if(key=="system")deviceDark else key=="dark")
            Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).selectable(selected,role=Role.RadioButton,onClick={onChange(key)}).testTag("theme-$key").padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Canvas(Modifier.fillMaxWidth(.86f).height(76.dp)) {
                    drawRoundRect(preview.background,cornerRadius=androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()))
                    drawCircle(preview.accent,8.dp.toPx(),Offset(size.width*.25f,size.height*.32f))
                    drawRoundRect(preview.text.copy(alpha=.8f),Offset(size.width*.43f,size.height*.25f),Size(size.width*.36f,4.dp.toPx()),androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                    drawRoundRect(preview.muted.copy(alpha=.4f),Offset(size.width*.16f,size.height*.62f),Size(size.width*.68f,4.dp.toPx()),androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                    drawRoundRect(preview.muted.copy(alpha=.2f),Offset(size.width*.16f,size.height*.77f),Size(size.width*.45f,3.dp.toPx()),androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                }
                Text(when(key){"system"->localized(language,"Device","ফোনের থিম");"light"->localized(language,"Light","হালকা");else->localized(language,"Dark","গাঢ়")},fontSize=14.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Normal,color=AppStyle.Text,maxLines=2,minLines=2,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Box(Modifier.width(26.dp).height(3.dp).background(if(selected)AppStyle.Accent else Color.Transparent,RoundedCornerShape(2.dp)))
            }
        }
    }
}
