package com.rafi.antireelai

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class InstalledPlatformCandidate(val packageName:String,val name:String) {
    fun preview()=Platform("picker.$packageName",name,"","",setOf(packageName),detectorId=null)
}
internal object InstalledCustomApps {
    /** Queried only in the user-opened picker; no broad inventory or unselected list is saved. */
    fun query(context:Context,catalog:List<Platform>):List<InstalledPlatformCandidate> {
        val pm=context.packageManager
        val visible=catalog.filter{it.available}.flatMap{it.packages}.toSet()
        val videoIntent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val packages=pm.queryIntentActivities(videoIntent,0).map{it.activityInfo.packageName}.toSet()+CustomPlatforms.YOUTUBE
        return packages.filter{it!=context.packageName && it!=CustomPlatforms.REMOVED_FACEBOOK_LITE && it !in visible}.mapNotNull {pkg->
            runCatching {
                @Suppress("DEPRECATION") val app=pm.getApplicationInfo(pkg,0)
                if(pm.getLaunchIntentForPackage(pkg)==null ||
                    (app.flags and ApplicationInfo.FLAG_SYSTEM!=0 && pkg!=CustomPlatforms.YOUTUBE))null
                else InstalledPlatformCandidate(pkg,pm.getApplicationLabel(app).toString())
            }.getOrNull()
        }.sortedBy{it.name.lowercase()}
    }
}
internal const val YOUTUBE_LIMITATION="AntiReels AI cannot currently guarantee accurate YouTube Shorts counting because YouTube's interface and accessibility behavior may prevent reliable detection. Some Shorts may not be counted. You can still add YouTube, but its tracking will be experimental."

@Composable internal fun CustomPlatformConfirmation(app:InstalledPlatformCandidate,onCancel:()->Unit,onConfirm:()->Unit) {
    val youtube=app.packageName==CustomPlatforms.YOUTUBE
    val unsupported=localized(LocalUiLanguage.current,
        "${app.name} has no validated short-video detector. You can add it to your platforms, but tracking will remain unavailable. No reels or viewing time will be counted for this app.",
        "${app.name}-এর জন্য যাচাই করা ছোট ভিডিও শনাক্তকারী নেই। অ্যাপটি তালিকায় যোগ করা যাবে, তবে এই অ্যাপের রিলস বা দেখার সময় গণনা করা হবে না।")
    AlertDialog(onDismissRequest=onCancel,containerColor=AppStyle.Surface,
        title={UiText(if(youtube)"YouTube Shorts Tracking Limitation" else "Add custom platform",fontSize=20.sp,color=AppStyle.Text)},
        text={UiText(if(youtube)YOUTUBE_LIMITATION else unsupported,fontSize=14.sp,color=AppStyle.Muted)},
        dismissButton={TextButton(onClick=onCancel){UiText("Cancel")}},
        confirmButton={TextButton(onClick=onConfirm){UiText(if(youtube)"Add anyway" else "Add platform",color=AppStyle.Accent)}})
}

@Composable internal fun CustomPlatformPicker(store:TrackerStore,onDismiss:()->Unit) {
    val context=LocalContext.current
    var query by remember{mutableStateOf("")}
    var chosen by remember{mutableStateOf<InstalledPlatformCandidate?>(null)}
    val apps by produceState<List<InstalledPlatformCandidate>?>(null) {
        value=withContext(Dispatchers.IO){InstalledCustomApps.query(context,store.catalog)}
    }
    val visible=remember(apps,query){apps?.filter{"${it.name} ${it.packageName}".contains(query.trim(),true)}}
    AlertDialog(onDismissRequest=onDismiss,containerColor=AppStyle.Surface,
        title={UiText("Add custom platform",fontSize=20.sp,color=AppStyle.Text)},
        text={Column(Modifier.fillMaxWidth().heightIn(max=480.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            UiText("Choose an installed app. Only your confirmed choice is saved on this device. Apps without a detector cannot be tracked.",fontSize=13.sp,color=AppStyle.Muted)
            OutlinedTextField(query,{query=it},label={UiText("Search installed apps")},singleLine=true,modifier=Modifier.fillMaxWidth())
            if(visible==null)LinearProgressIndicator(Modifier.fillMaxWidth(),color=AppStyle.Accent)
            else if(visible.isEmpty())UiText("No matching installed apps. Try another name.",fontSize=13.sp,color=AppStyle.Muted)
            else LazyColumn(Modifier.weight(1f,false)) {
                items(visible,key={it.packageName}){app->
                    Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable{chosen=app}.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        PlatformBrand(app.preview(),Modifier.size(36.dp))
                        Column(Modifier.weight(1f)) {
                            UiText(app.name,fontSize=15.sp,color=AppStyle.Text)
                            UiText(if(app.packageName==CustomPlatforms.YOUTUBE)"Experimental tracking" else "Unsupported tracking",fontSize=12.sp,color=AppStyle.Muted)
                        }
                    }
                }
            }
        }},confirmButton={TextButton(onClick=onDismiss){UiText("Done")}})
    chosen?.let {app->CustomPlatformConfirmation(app,{chosen=null}){
        store.addCustomPlatform(app.packageName,app.name);chosen=null;onDismiss()
    }}
}
