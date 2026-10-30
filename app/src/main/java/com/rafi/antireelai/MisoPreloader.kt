package com.rafi.antireelai

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable internal fun MisoPreloader() {
    Box(Modifier.fillMaxSize().safeDrawingPadding().testTag("miso-preloader"),contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)) {
            Image(painterResource(R.drawable.miso_brand_png),contentDescription="Miso app logo",modifier=Modifier.size(160.dp))
            CircularProgressIndicator(Modifier.size(20.dp),color=AppStyle.Accent,strokeWidth=2.dp)
        }
    }
}
