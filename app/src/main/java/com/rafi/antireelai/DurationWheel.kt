package com.rafi.antireelai

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

/** A bounded native scrolling wheel; text entry remains available for accessibility. */
@Composable internal fun DurationWheel(value:String,language:String,onHaptic:()->Unit={},onValue:(String)->Unit) {
    val duration=parseMinutes(value) ?:30
    var hours by remember{mutableIntStateOf(duration/60)}
    var minutes by remember{mutableIntStateOf(duration%60)}
    LaunchedEffect(value){parseMinutes(value)?.let{hours=it/60;minutes=it%60}}
    LaunchedEffect(Unit){if(parseMinutes(value)==null)onValue("0:30")}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp)) {
        MinuteWheel(localized(language,"Hours","ঘণ্টা"),0..24,hours,Modifier.weight(1f)) {if(hours!=it)onHaptic();hours=it;if(it==24)minutes=0;onValue("$hours:${minutes.toString().padStart(2,'0')}")}
        MinuteWheel(localized(language,"Minutes","মিনিট"),if(hours==24)0..0 else 0..59,minutes,Modifier.weight(1f)) {if(minutes!=it)onHaptic();minutes=it;onValue("$hours:${minutes.toString().padStart(2,'0')}")}
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun MinuteWheel(label:String,range:IntRange,value:Int,modifier:Modifier,onValue:(Int)->Unit) {
    val state=rememberLazyListState(initialFirstVisibleItemIndex=(value-range.first).coerceIn(0,range.count()-1))
    val latest by rememberUpdatedState(onValue)
    val scope=rememberCoroutineScope()
    LaunchedEffect(range,value){
        val target=(value-range.first).coerceIn(0,range.count()-1)
        if(!state.isScrollInProgress && state.firstVisibleItemIndex!=target)state.requestScrollToItem(target)
    }
    LaunchedEffect(state,range) {
        snapshotFlow {
            if(state.isScrollInProgress)null else state.layoutInfo.let{layout->
                val middle=(layout.viewportStartOffset+layout.viewportEndOffset)/2
                layout.visibleItemsInfo.minByOrNull{abs(it.offset+it.size/2-middle)}?.index?.plus(range.first)
            }
        }.distinctUntilChanged().collect{it?.let{selected->if(selected in range)latest(selected)}}
    }
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(label,fontSize=13.sp,color=AppStyle.Muted)
        LazyColumn(state=state,flingBehavior=rememberSnapFlingBehavior(state),contentPadding=PaddingValues(vertical=48.dp),modifier=Modifier.fillMaxWidth().height(144.dp).semantics{contentDescription="$label, $value"},horizontalAlignment=Alignment.CenterHorizontally) {
            items(range.toList(),key={it}) {number->
                Box(Modifier.fillMaxWidth().height(48.dp).clickable{scope.launch{state.animateScrollToItem(number-range.first)}},contentAlignment=Alignment.Center) {
                    Text(number.toString().padStart(2,'0'),fontSize=if(number==value)27.sp else 19.sp,fontWeight=if(number==value)FontWeight.Bold else FontWeight.Normal,color=if(number==value)AppStyle.Teal else AppStyle.Muted.copy(alpha=.6f))
                }
            }
        }
    }
}
