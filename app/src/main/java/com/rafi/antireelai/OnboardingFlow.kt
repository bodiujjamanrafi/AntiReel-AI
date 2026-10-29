package com.rafi.antireelai

import androidx.activity.compose.BackHandler
import android.content.SharedPreferences
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import org.json.JSONObject

@Composable internal fun OnboardingFlow(store:TrackerStore,saveProfile:suspend (OnboardingProfile)->Boolean={profile->withContext(Dispatchers.IO){store.saveOnboardingProfile(profile)}},hapticFeedback:(()->Unit)?=null,startReview:Boolean=false,onExit:(()->Unit)?=null,onFinished:()->Unit) {
    val draft=remember{runCatching{JSONObject(store.prefs.getString("onboardingDraft","{}")!!)}.getOrDefault(JSONObject())}
    var step by remember{mutableIntStateOf(if(startReview)12 else restoredOnboardingStep(store.prefs.getInt("onboardingStep",-2)))}
    var language by remember{mutableStateOf(store.prefs.getString("language","en") ?: "en")}
    var theme by remember{mutableStateOf(store.themeMode)}
    val view=LocalView.current
    fun tap(){if(store.haptics){if(hapticFeedback!=null)hapticFeedback() else view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)}}
    var name by remember{mutableStateOf(draft.optString("name"))}
    var answers by remember{mutableStateOf((0..11).associateWith{draft.optString("a$it")})}
    var custom by remember{mutableStateOf((0..11).associateWith{draft.optString("c$it")})}
    var muted by remember{mutableStateOf(store.prefs.getBoolean("onboardingMuted",false))}
    var busy by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf(false)}
    var foreground by remember{mutableStateOf(true)}
    var editingReview by remember{mutableStateOf(false)}
    var review by remember{mutableStateOf(false)}
    var complete by remember{mutableStateOf(false)}
    var dialogue by remember{mutableStateOf("")}
    var reacting by remember{mutableStateOf(false)}
    val contentScroll=rememberScrollState()
    val pageOpacity=remember{Animatable(1f)}
    val introScale=remember{Animatable(.92f)}
    LaunchedEffect(Unit){introScale.animateTo(1f,tween(if(store.reducedMotion)0 else 500))}
    val speaker=remember{MisoSpeaker(store.contextForSpeech)}
    val scope=rememberCoroutineScope()
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    fun tr(en:String,bn:String)=localized(language,en,bn)
    fun snapshot()=JSONObject().apply{put("name",name);answers.forEach{(k,v)->put("a$k",v)};custom.forEach{(k,v)->put("c$k",v)}}.toString()
    suspend fun save():Boolean {
        val state=snapshot();val position=step;val lang=language;val mute=muted
        return withContext(Dispatchers.IO){runCatching{store.prefs.edit().putString("onboardingDraft",state).putInt("onboardingStep",position).putString("language",lang).putBoolean("onboardingMuted",mute).commit()}.getOrDefault(false)}
    }
    fun choice(index:Int)=answers[index].orEmpty().split(',').mapNotNull{it.toIntOrNull()}
    fun minutes():Int=store.dailyMinutes
    fun reels():Int=listOf(5,10,20,30,50).getOrNull(choice(8).firstOrNull() ?: -1) ?: custom[8]?.toIntOrNull() ?: 0
    fun answerValid(index:Int)=validOnboardingAnswer(index,name,choice(index),custom[index].orEmpty())
    fun valid()=step !in 0..11 || answerValid(step)
    fun incomplete()=onboardingQuestionIds.firstOrNull{!answerValid(it)}
    LaunchedEffect(step,review) {
        contentScroll.scrollTo(0)
        if(!store.reducedMotion){pageOpacity.snapTo(.94f);pageOpacity.animateTo(1f,tween(120))}
    }
    LaunchedEffect(answers,custom) {reacting=true;delay(750);reacting=false}
    DisposableEffect(lifecycle){val observer=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_PAUSE){foreground=false;speaker.stop()};if(e==Lifecycle.Event.ON_RESUME)foreground=true};lifecycle.addObserver(observer);onDispose{lifecycle.removeObserver(observer);speaker.close()}}
    LaunchedEffect(name,answers,custom,language,muted,step){delay(250);save()}
    LaunchedEffect(step,review,language,speaker.ready,muted){
        speaker.stop()
        dialogue=when{step==-2->"antireel";step==-1->tr("Choose your language","তোমার ভাষা বেছে নাও");step==-4->tr("Choose your theme","তোমার থিম বেছে নাও");review || step==12->tr("Your plan, ${name.trim()}.","তোমার পরিকল্পনা, ${name.trim()}।");step in 0..11->onboardingQuestions.getValue(step).let{tr(it.en,it.bn)};else->tr("Hi, I'm Miso. Let's make room for your time.","হাই, আমি মিসো। চলো তোমার সময়ের যত্ন নিই।")}
        if(foreground && !muted && (step>=0 || step==-3))speaker.say(dialogue,language)
        if(step==-2){delay(if(store.reducedMotion)100 else 650);step=-1}
    }
    fun back(){speaker.stop();if(startReview){if(step==12 || review)onExit?.invoke() else {step=12;review=false;editingReview=false}} else if(review)review=false else step=previousOnboardingStep(step)}
    BackHandler(enabled=startReview || canGoBackInOnboarding(step) || review){if(startReview && step !in 0..12)onExit?.invoke() else back()}
    Surface(color=AppStyle.Background,contentColor=AppStyle.Text,modifier=Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick={tap();back()},enabled=canGoBackInOnboarding(step) || review){Text(tr("Back","ফিরে যাই"))}
                Spacer(Modifier.weight(1f))
                TextButton(onClick={tap();muted=!muted;speaker.stop()}){Text(if(muted)tr("Voice off","কণ্ঠ বন্ধ") else tr("Voice on","কণ্ঠ চালু"))}
            }
            if(step in 0..11){LinearProgressIndicator(progress={(onboardingQuestionIds.indexOf(step)+1)/onboardingQuestionIds.size.toFloat()},modifier=Modifier.fillMaxWidth());Text(tr("Question ${onboardingQuestionIds.indexOf(step)+1} of ${onboardingQuestionIds.size}","প্রশ্ন ${onboardingQuestionIds.indexOf(step)+1} / ${onboardingQuestionIds.size}"),fontSize=12.sp,color=AppStyle.Muted)}
            Column(Modifier.weight(1f).verticalScroll(contentScroll).graphicsLayer{alpha=pageOpacity.value},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                AndroidView(factory={CompanionView(it)},update={it.animated=!store.reducedMotion;it.talking=speaker.speaking;it.progress=0f;it.pose=onboardingPose(if(review)12 else step,choice(step),busy || reacting)},modifier=Modifier.size(if(step==-2)190.dp else if(step==12 || review)140.dp else 112.dp).graphicsLayer{scaleX=if(step==-2)introScale.value else 1f;scaleY=scaleX})
                Text(dialogue,fontSize=25.sp,lineHeight=34.sp,fontWeight=FontWeight.SemiBold)
                if(speaker.unavailable)Text(tr("An offline voice for this language isn't installed. You can continue without speech.","এই ভাষার অফলাইন কণ্ঠ নেই। কণ্ঠ ছাড়াও এগিয়ে যেতে পারো।"),fontSize=12.sp,color=AppStyle.Muted)
                if(review || step==12) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                        if(minutes()>0) {
                            Column(horizontalAlignment=Alignment.CenterHorizontally){AppGlyph(24,AppStyle.Teal,Modifier.size(24.dp));Text("${minutes()}",fontSize=32.sp,fontWeight=FontWeight.Bold);Text(tr("minutes daily","মিনিট প্রতিদিন"),fontSize=12.sp,color=AppStyle.Muted)}
                        }
                        Column(horizontalAlignment=Alignment.CenterHorizontally){AppGlyph(23,AppStyle.Blue,Modifier.size(24.dp));Text("${reels()}",fontSize=32.sp,fontWeight=FontWeight.Bold);Text(tr("reels daily","রিলস প্রতিদিন"),fontSize=12.sp,color=AppStyle.Muted)}
                    }
                    Text(tr("Miso pauses your feed at your daily limit.","দৈনিক সীমায় পৌঁছালে মিসো রিলস থেকে বিরতি নিতে বলবে।"),fontSize=13.sp,lineHeight=20.sp,color=AppStyle.Muted)
                    onboardingQuestionIds.forEach{i->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp,AppStyle.Frame,RoundedCornerShape(12.dp)).clickable{tap();editingReview=true;review=false;step=i}.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                Text(onboardingQuestions.getValue(i).let{tr(it.en,it.bn)},fontSize=13.sp,lineHeight=20.sp,color=AppStyle.Muted)
                                Text(onboardingAnswerLabel(i,name,choice(i),custom[i].orEmpty(),language),fontSize=16.sp,fontWeight=FontWeight.Medium)
                            }
                            Text(tr("Edit","বদলাই"),fontSize=13.sp,color=AppStyle.Teal)
                        }
                    }
                } else if(step==-1){listOf("bn" to "বাংলা","en" to "English").forEach{(id,label)->OutlinedButton(onClick={tap();language=id},modifier=Modifier.fillMaxWidth().heightIn(min=64.dp),border=BorderStroke(if(language==id)2.dp else 1.dp,if(language==id)AppStyle.Accent else AppStyle.Frame)){Text(label,fontSize=21.sp)}}}
                else if(step==-4) {
                    Text(tr("Make AntiReel comfortable for you. You can change this later in Settings.","তোমার পছন্দমতো অ্যান্টিরিল সাজাও। পরে সেটিংস থেকে বদলাতে পারো।"),fontSize=14.sp,lineHeight=22.sp,color=AppStyle.Muted)
                    ThemeChoices(theme,language){tap();theme=it;store.themeMode=it}
                }
                else if(step==0)OutlinedTextField(name,{name=it.take(60)},label={Text(tr("Your name","তোমার নাম"))},supportingText={Text(tr("1–40 letters; spaces, apostrophes and hyphens allowed","১–৪০ অক্ষর; ফাঁক, হাইফেন ও অ্যাপস্ট্রফি ব্যবহার করা যায়"))},singleLine=true,modifier=Modifier.fillMaxWidth())
                else if(step in 1..11) {
                    val q=onboardingQuestions.getValue(step)
                    q.options.forEachIndexed{i,label->
                        val selected=i in choice(step)
                        val multi=step==2
                        val choose={tap();val current=choice(step);answers=answers+(step to (if(multi)if(selected)current-i else current+i else listOf(i)).joinToString(","))}
                        val control=if(multi)Modifier.toggleable(selected,role=Role.Checkbox,onValueChange={choose()}) else Modifier.selectable(selected,role=Role.RadioButton,onClick=choose)
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(if(selected)AppStyle.AccentSoft else AppStyle.Surface)
                            .border(if(selected)1.2f.dp else .6f.dp,if(selected)AppStyle.Accent.copy(alpha=.65f) else AppStyle.Frame.copy(alpha=.5f),RoundedCornerShape(17.dp))
                            .then(control).heightIn(min=58.dp).padding(horizontal=12.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            if(multi)Checkbox(selected,onCheckedChange=null) else RadioButton(selected,onClick=null)
                            Text(tr(label,q.banglaOptions[i]),fontSize=15.sp,lineHeight=22.sp,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Normal,modifier=Modifier.weight(1f))
                        }
                    }
                    val other=when(step){8->5;2->4;else->-1}
                    if(other in choice(step))OutlinedTextField(custom[step].orEmpty(),{custom=custom+(step to it.take(100))},label={Text(if(step in listOf(1,7))tr("Minutes or hours:minutes (1–1440 min)","মিনিট অথবা ঘণ্টা:মিনিট (১–১৪৪০ মিনিট)") else if(step==8)tr("Reels (1–10000)","রিলস (১–১০০০০)") else tr("Tell Miso","মিসোকে বলো"))},keyboardOptions=KeyboardOptions(keyboardType=if(step in listOf(1,7,8))KeyboardType.Text else KeyboardType.Text),modifier=Modifier.fillMaxWidth())
                }
            }
            if(error)Text(tr("Couldn't save. Please try again.","সংরক্ষণ হয়নি। আবার চেষ্টা করো।"),color=MaterialTheme.colorScheme.error)
            Box(Modifier.fillMaxWidth().heightIn(min=64.dp).testTag("onboarding-footer"),contentAlignment=Alignment.BottomCenter) {
            if(step!=-2)Button(enabled=!busy && valid(),onClick={
                if(busy)return@Button
                tap()
                if(review){review=false;step=12;return@Button}
                if(step==11 && 2 in choice(11)){review=true;return@Button}
                if(step==12){val missing=incomplete();if(missing!=null){step=missing;error=false}else complete=true;return@Button}
                busy=true;speaker.stop();scope.launch {
                    if(save()) {
                        if(editingReview){editingReview=false;review=true;step=12}else step=nextOnboardingStep(step);error=false
                    }else error=true
                    busy=false
                }
            },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("onboarding-continue")){Text(tr(if(busy)"Saving…" else if(step==12)"Finish" else "Continue",if(busy)"সংরক্ষণ হচ্ছে…" else if(step==12)"শেষ করি" else "এগিয়ে যাই"))}
            }
        }
        if(complete)PlanReadyDialog(name.trim(),minutes(),reels(),language,store.reducedMotion,busy,
            onReview={tap();complete=false},onConfirm={
                tap()
                val missing=incomplete()
                if(missing!=null){complete=false;step=missing;error=false}else {
                    val profile=OnboardingProfile(name.trim(),language,minutes(),reels(),choice(2).mapNotNull{listOf("instagram","facebook","youtube","tiktok").getOrNull(it)}.toSet(),store.prefs.getString("reminderStyle","0")?.toIntOrNull()?.coerceIn(0,3) ?:0,muted,snapshot())
                    busy=true;scope.launch{
                        val ok=runCatching{saveProfile(profile)}.getOrDefault(false)
                        busy=false
                        if(ok)onFinished() else {error=true;complete=false}
                    }
                }
            })
    }
}
