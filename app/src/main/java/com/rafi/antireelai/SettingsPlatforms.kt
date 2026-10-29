package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The same Platforms destination and callbacks, presented compactly when entered from Settings. */
@Composable internal fun SettingsPlatforms(query:String,onQuery:(String)->Unit,filter:PlatformFilter,onFilter:(PlatformFilter)->Unit,selected:Set<String>,installed:Set<String>,counts:List<Int>,running:Boolean,status:(String)->String,onToggle:(Platform,Boolean)->Unit,onOpen:(Platform)->Unit,onBack:()->Unit,onSelectInstalled:()->Unit,onClear:()->Unit,onSelectVisible:()->Unit) {
    val visible=filterPlatforms(platforms,query,filter,selected,installed)
    SettingsTheme {
        Column(Modifier.widthIn(max=640.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            SettingsPageHeader("Manage platforms",onBack)
            SettingsGroup {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick={onFilter(PlatformFilter.SELECTED)},modifier=Modifier.weight(1f).heightIn(min=56.dp)){UiText("${selected.size}  ");UiText("Selected",fontSize=15.sp)}
                    TextButton(onClick={onFilter(PlatformFilter.INSTALLED)},modifier=Modifier.weight(1f).heightIn(min=56.dp)){UiText("${installed.size}  ");UiText("Installed",fontSize=15.sp)}
                }
                SettingsRule()
                SettingsInset {OutlinedTextField(query,onQuery,placeholder={UiText("Search platforms…")},singleLine=true,modifier=Modifier.fillMaxWidth())}
            }
            LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                items(PlatformFilter.entries,key={it.name}){item->FilterChip(modifier=Modifier.heightIn(min=48.dp),selected=filter==item,onClick={onFilter(item)},label={UiText(item.label)})}
            }
            SettingsGroup("Available platforms") {
                if(visible.isEmpty())SettingsInset {SettingsText("No matching platforms")}
                visible.forEachIndexed {index,p->
                    if(index>0)SettingsRule()
                    Row(Modifier.fillMaxWidth().heightIn(min=72.dp).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        PlatformBrand(p,Modifier.size(32.dp))
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            UiText(p.name,fontSize=15.sp,color=AppStyle.Text)
                            SettingsText(if(p.id in selected && running && p.id in installed)status(p.id) else if(p.id in installed)"Installed" else p.subtitle)
                            if(p.id in selected)SettingsText("${counts.getOrElse(platforms.indexOf(p)){0}} reels today")
                        }
                        Checkbox(p.id in selected,{onToggle(p,it)},modifier=Modifier.semantics{contentDescription="Track ${p.name}"})
                        TextButton(onClick={onOpen(p)},modifier=Modifier.widthIn(min=48.dp).heightIn(min=48.dp),contentPadding=PaddingValues(horizontal=4.dp)){UiText(if(p.id in installed)"Open" else "Install",fontSize=14.sp)}
                    }
                }
            }
            SettingsGroup("Selection") {
                SettingsRow("Select installed",1,onClick=onSelectInstalled)
                SettingsRule()
                SettingsRow("Select all",20,onClick=onSelectVisible)
                SettingsRule()
                SettingsRow("Clear selection",25,onClick=onClear)
            }
        }
    }
}
