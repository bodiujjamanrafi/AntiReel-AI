package com.rafi.antireelai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Setup and Settings use the same reviewed, bundled legal content. */
@Composable internal fun MisoLegalPage(title:String,onBack:()->Unit,modifier:Modifier=Modifier) {
    val document=if(title=="Terms of use")LegalDocument.TERMS else LegalDocument.PRIVACY
    SettingsTheme {Column(modifier.verticalScroll(rememberScrollState())){LegalReader(document,onBack)}}
}
