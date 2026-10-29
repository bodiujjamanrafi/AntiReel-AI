package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private data class LegalText(val updated:String,val summary:String,val sections:List<Pair<String,List<String>>>)

/** Native, selectable bundled documents remain readable without a network/browser. */
@Composable internal fun LegalReader(document:LegalDocument,onBack:()->Unit) {
    val context=LocalContext.current
    var failed by remember(document){mutableStateOf(false)}
    val text by produceState<LegalText?>(null,document) {
        value=withContext(Dispatchers.IO){runCatching {
            val json=JSONObject(context.assets.open(document.asset).bufferedReader().use{it.readText()})
            val sections=json.getJSONArray("sections")
            LegalText(json.getString("updated"),json.getString("summary"),List(sections.length()){i->
                val section=sections.getJSONObject(i);val paragraphs=section.getJSONArray("paragraphs")
                section.getString("title") to List(paragraphs.length()){paragraphs.getString(it)}
            })
        }.getOrNull()}
        failed=value==null
    }
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        SettingsPageHeader(document.title,onBack)
        SettingsGroup {SettingsInset {
            UiText(document.url,fontSize=13.sp,color=AppStyle.Muted)
            SettingsAction("Open public page",{ExternalLinks.document(context,document)})
            if(document==LegalDocument.SUPPORT)SettingsAction("Email support",{ExternalLinks.support(context)},primary=true)
        }}
        val content=text
        if(content==null)SettingsText(if(failed)"This document could not load. Contact $SUPPORT_EMAIL." else "Loading document…")
        else SelectionContainer {
            Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                SettingsText("Updated ${content.updated}")
                SettingsText(content.summary)
                content.sections.forEach{(heading,paragraphs)->
                    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        UiText(heading,fontSize=17.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
                        paragraphs.forEach{SettingsText(it)}
                    }
                }
            }
        }
    }
}
