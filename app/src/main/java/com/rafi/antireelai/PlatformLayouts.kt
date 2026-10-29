package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun PlatformSummary(selected:Int,installed:Int,onSelected:()->Unit,onInstalled:()->Unit) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(24.dp,Alignment.CenterHorizontally)) {
        listOf(Triple(selected,"Selected",onSelected),Triple(installed,"Installed",onInstalled)).forEach{(count,label,action)->
            TextButton(onClick=action,contentPadding=PaddingValues(horizontal=8.dp,vertical=10.dp)) {
                UiText("$count",fontSize=25.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
                Spacer(Modifier.width(7.dp))
                UiText(label,fontSize=15.sp,color=AppStyle.Text)
            }
        }
    }
}
@Composable internal fun PlatformGroup(title:String,apps:List<Platform>,selected:Set<String>,installed:Set<String>,counts:List<Int>,running:Boolean,status:(String)->String,onToggle:(Platform,Boolean)->Unit,onOpen:(Platform)->Unit,onAction:()->Unit,action:String) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){UiText(title,fontSize=17.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));TextButton(onClick=onAction){UiText(action,fontSize=12.sp)}}
        Column {
            apps.forEach{p->
                val isInstalled=p.id in installed
                val isSelected=p.id in selected
                Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                    PlatformBrand(p,Modifier.size(40.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        UiText(p.name,fontSize=15.sp,fontWeight=FontWeight.Medium,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        UiText(if(isSelected && running && isInstalled)status(p.id) else if(isInstalled)"Installed" else p.subtitle,fontSize=11.sp,color=AppStyle.Muted,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        if(isSelected)UiText("${counts.getOrElse(platforms.indexOf(p)){0}} reels today",fontSize=11.sp,color=AppStyle.Muted)
                    }
                    Row(Modifier.width(130.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        PlatformSelection(isSelected,p.name){onToggle(p,it)}
                        TextButton(onClick={onOpen(p)},modifier=Modifier.width(70.dp).height(44.dp),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(0.dp),colors=ButtonDefaults.textButtonColors(contentColor=AppStyle.Text)) {
                            UiText(if(isInstalled)"Open" else "Install",fontSize=12.sp,fontWeight=FontWeight.Medium,maxLines=1)
                        }
                    }
                }
            }
        }
    }
}

/** A fixed-width, labelled-by-semantics selection control keeps every action aligned. */
@Composable private fun PlatformSelection(selected:Boolean,name:String,onChange:(Boolean)->Unit) {
    val tint=if(selected)AppStyle.Teal else AppStyle.Muted
    Box(Modifier.size(52.dp,44.dp).clip(RoundedCornerShape(12.dp))
        .toggleable(value=selected,interactionSource=remember{MutableInteractionSource()},indication=null,role=Role.Checkbox,onValueChange=onChange).semantics{contentDescription="Track $name"},contentAlignment=Alignment.Center) {
        AppGlyph(if(selected)20 else 25,tint,Modifier.size(21.dp))
    }
}

internal enum class PlatformFilter(val label:String) { ALL("All"), SELECTED("Selected"), INSTALLED("Installed"), SOCIAL("Social"), VIDEO("Video") }
internal fun filterPlatforms(catalog:List<Platform>,query:String,filter:PlatformFilter,selected:Set<String>,installed:Set<String>):List<Platform> = catalog.filter {p->
    (query.isBlank() || "${p.name} ${p.subtitle}".contains(query.trim(),ignoreCase=true)) && when(filter) {
        PlatformFilter.ALL->true
        PlatformFilter.SELECTED->p.id in selected
        PlatformFilter.INSTALLED->p.id in installed
        PlatformFilter.SOCIAL->p.kind==PlatformKind.SOCIAL
        PlatformFilter.VIDEO->p.kind==PlatformKind.VIDEO
    }
}
