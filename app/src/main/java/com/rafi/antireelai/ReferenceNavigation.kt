package com.rafi.antireelai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class AppDestination(val label:String) { Today("Today"), Platforms("Platforms"), Activity("Activity"), Settings("Settings"), Miso("Miso") }
internal data class AppPalette(val background:Color,val surface:Color,val text:Color,val muted:Color,val accent:Color,val control:Color,val soft:Color,val outline:Color)
internal object AppStyle {
    val ReelTop get()=if(dark)Color(0xFF64213F) else Color(0xFF922A50)
    val ReelBottom get()=if(dark)Color(0xFF301725) else Color(0xFF681C3A)
    val Light=AppPalette(Color(0xFFF7F4F8),Color(0xFFFFFCFF),Color(0xFF241B2B),Color(0xFF716477),Color(0xFFA31B50),Color(0xFF8A254D),Color(0xFFF5DDE8),Color(0xFFDED4E0))
    val Dark=AppPalette(Color(0xFF080A10),Color(0xFF15151F),Color(0xFFF6F1F7),Color(0xFFB7B2C4),Color(0xFFFF70A5),Color(0xFFFFA1C4),Color(0xFF332035),Color(0xFF3A303F))
    var dark by mutableStateOf(false)
    fun colorsFor(dark:Boolean)=if(dark)Dark else Light
    private val palette get()=colorsFor(dark)
    val Background get()=palette.background
    val Surface get()=palette.surface
    val Text get()=palette.text
    val Muted get()=palette.muted
    val Accent get()=palette.accent
    val Control get()=palette.control
    val AccentSoft get()=palette.soft
    val Frame get()=if(dark)Color(0xFF44454D) else Color(0xFF2B2830).copy(alpha=.24f)
    val Outline get()=palette.outline
    val Teal get()=if(dark)Color(0xFF69DBC0) else Color(0xFF006C56)
    val Blue get()=if(dark)Color(0xFF9ABFFF) else Color(0xFF2B53A3)
    val Coral get()=if(dark)Color(0xFFFF9D86) else Color(0xFFA63D28)
    val Violet get()=if(dark)Color(0xFFC098FF) else Color(0xFF7342B6)
    val Gold get()=if(dark)Color(0xFFFFC078) else Color(0xFF8F5515)
    val Cyan get()=if(dark)Color(0xFF7BD7F4) else Color(0xFF1A647D)
}

private val NavigationDestinations=listOf(AppDestination.Today,AppDestination.Platforms,AppDestination.Miso,AppDestination.Activity,AppDestination.Settings)

/** Draw the notch in the draw phase; navigation never measures on each animation tick. */
@Composable internal fun ReferenceNavigation(selected:Int,reducedMotion:Boolean,onSelect:(Int)->Unit) {
    val reduced=motionReduced(reducedMotion)
    val destinations=NavigationDestinations
    val displayIndex=destinations.indexOfFirst{it.ordinal==selected}.coerceAtLeast(0)
    val motion:androidx.compose.animation.core.FiniteAnimationSpec<Float> = remember(reduced){if(reduced)tween(0) else androidx.compose.animation.core.spring(dampingRatio=1f,stiffness=700f)}
    val position=animateFloatAsState(displayIndex.toFloat(),motion,label="notch")
    val path=remember{Path()}
    val topColor=AppStyle.ReelTop;val bottomColor=AppStyle.ReelBottom
    val fill=remember(topColor,bottomColor){Brush.linearGradient(listOf(topColor,bottomColor))}
    val glass=remember{Brush.verticalGradient(listOf(Color.White.copy(alpha=.28f),Color.White.copy(alpha=.08f)))}
    val density=androidx.compose.ui.platform.LocalDensity.current
    val outline=remember(density){androidx.compose.ui.graphics.drawscope.Stroke(with(density){1.dp.toPx()})}
    val circleOutline=remember(density){androidx.compose.ui.graphics.drawscope.Stroke(with(density){.8f.dp.toPx()})}
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=18.dp).padding(bottom=8.dp).height(102.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val cell=size.width/AppDestination.entries.size
            val center=cell*(position.value+.5f)
            val top=28.dp.toPx();val r=20.dp.toPx();val reach=minOf(43.dp.toPx(),cell*.48f);val depth=35.dp.toPx()
            val p=path.apply {
                reset();moveTo(r,top);lineTo(center-reach,top)
                cubicTo(center-reach*.75f,top,center-reach*.82f,top+depth,center,top+depth)
                cubicTo(center+reach*.82f,top+depth,center+reach*.75f,top,center+reach,top)
                lineTo(size.width-r,top);quadraticTo(size.width,top,size.width,top+r)
                lineTo(size.width,size.height-r);quadraticTo(size.width,size.height,size.width-r,size.height)
                lineTo(r,size.height);quadraticTo(0f,size.height,0f,size.height-r)
                lineTo(0f,top+r);quadraticTo(0f,top,r,top);close()
            }
            drawPath(p,fill)
            drawPath(p,glass,style=outline)
            drawCircle(Color(0xFFFFE5EE),27.dp.toPx(),Offset(center,top-2.dp.toPx()))
            drawCircle(Color.White.copy(alpha=.65f),27.dp.toPx(),Offset(center,top-2.dp.toPx()),style=circleOutline)
        }
        Row(Modifier.fillMaxSize()) {
            destinations.forEach {destination->NavigationItem(destination,destination.ordinal==selected,reduced,motion,onSelect)}
        }
    }
}

/** Icon-color recomposition stays inside one tab; it cannot rebuild the bar or page. */
@Composable private fun RowScope.NavigationItem(destination:AppDestination,active:Boolean,reducedMotion:Boolean,motion:androidx.compose.animation.core.FiniteAnimationSpec<Float>,onSelect:(Int)->Unit) {
    val language=LocalUiLanguage.current
    val lift=animateFloatAsState(if(active)-24f else 0f,motion,label="icon lift")
    val color by animateColorAsState(if(active)AppStyle.ReelTop else Color(0xFFFFDFEB),tween(if(reducedMotion)0 else 140),label="icon color")
    val labelColor by animateColorAsState(if(active)Color(0xFFFFEFF5) else Color(0xFFFFEFF5).copy(alpha=.9f),tween(if(reducedMotion)0 else 140),label="label color")
    val density=androidx.compose.ui.platform.LocalDensity.current
    Column(Modifier.weight(1f).fillMaxHeight().selectable(selected=active,interactionSource=remember{MutableInteractionSource()},indication=null,role=Role.Tab,onClick={onSelect(destination.ordinal)}).semantics{contentDescription=UiCopy.translate(destination.label,language)}.padding(top=38.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.graphicsLayer{translationY=with(density){lift.value.dp.toPx()}}){AppGlyph(if(destination==AppDestination.Miso)8 else destination.ordinal,color,Modifier.size(28.dp))}
        Spacer(Modifier.height(8.dp))
        UiText(destination.label,color=labelColor,fontSize=11.sp,fontWeight=if(active)FontWeight.Bold else FontWeight.SemiBold)
    }
}

/** Shared, resolution independent icons for navigation and home actions. */
@Composable internal fun AppGlyph(index:Int,color:Color,modifier:Modifier=Modifier) {
    Canvas(modifier.size(24.dp)) {
        val w=size.width
        val stroke=androidx.compose.ui.graphics.drawscope.Stroke(w*.065f,cap=StrokeCap.Round,join=StrokeJoin.Round)
        fun point(x:Float,y:Float)=Offset(x*w,y*w)
        when(index) {
            0 -> { val p=Path().apply{moveTo(w*.12f,w*.44f);lineTo(w*.5f,w*.13f);lineTo(w*.88f,w*.44f);moveTo(w*.23f,w*.39f);lineTo(w*.23f,w*.86f);lineTo(w*.43f,w*.86f);lineTo(w*.43f,w*.62f);lineTo(w*.59f,w*.62f);lineTo(w*.59f,w*.86f);lineTo(w*.78f,w*.86f);lineTo(w*.78f,w*.39f)};drawPath(p,color,style=stroke) }
            1 -> for(x in 0..1) for(y in 0..1) drawRoundRect(color,point(.13f+x*.44f,.13f+y*.44f),androidx.compose.ui.geometry.Size(w*.30f,w*.30f),androidx.compose.ui.geometry.CornerRadius(w*.07f),style=stroke)
            2 -> {drawLine(color,point(.15f,.85f),point(.88f,.85f),strokeWidth=stroke.width);for(i in 0..2)drawLine(color,point(.28f+i*.24f,.69f),point(.28f+i*.24f,listOf(.46f,.19f,.33f)[i]),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            3 -> {drawCircle(color,w*.30f,style=stroke);drawCircle(color,w*.10f,style=stroke);for(i in 0..7){val a=i*Math.PI/4;drawLine(color,point(.5f+kotlin.math.cos(a).toFloat()*.32f,.5f+kotlin.math.sin(a).toFloat()*.32f),point(.5f+kotlin.math.cos(a).toFloat()*.41f,.5f+kotlin.math.sin(a).toFloat()*.41f),strokeWidth=stroke.width,cap=StrokeCap.Round)}}
            14 -> {val p=Path().apply{moveTo(w*.33f,w*.18f);lineTo(w*.80f,w*.5f);lineTo(w*.33f,w*.82f);close()};drawPath(p,color,style=stroke)}
            15 -> {val p=Path().apply{moveTo(w*.5f,w*.88f);lineTo(w*.5f,w*.48f);cubicTo(w*.2f,w*.5f,w*.12f,w*.2f,w*.12f,w*.12f);cubicTo(w*.45f,w*.12f,w*.52f,w*.3f,w*.5f,w*.48f);cubicTo(w*.48f,w*.3f,w*.7f,w*.12f,w*.9f,w*.14f);cubicTo(w*.88f,w*.4f,w*.72f,w*.55f,w*.5f,w*.48f)};drawPath(p,color,style=stroke)}
            16 -> {val p=Path().apply{moveTo(w*.66f,w*.12f);cubicTo(w*.1f,w*.08f,w*.06f,w*.90f,w*.65f,w*.87f);cubicTo(w*.82f,w*.86f,w*.91f,w*.73f,w*.91f,w*.64f);cubicTo(w*.48f,w*.8f,w*.31f,w*.35f,w*.66f,w*.12f);close()};drawPath(p,color,style=stroke)}
            17 -> {drawRoundRect(color,point(.28f,.08f),androidx.compose.ui.geometry.Size(w*.44f,w*.84f),androidx.compose.ui.geometry.CornerRadius(w*.07f),style=stroke);drawLine(color,point(.43f,.79f),point(.57f,.79f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            18 -> {for(i in 0..2){val y=.25f+i*.25f;drawLine(color,point(.12f,y),point(.88f,y),strokeWidth=stroke.width);drawCircle(AppStyle.Surface,w*.07f,point(if(i==1).65f else .35f,y));drawCircle(color,w*.07f,point(if(i==1).65f else .35f,y),style=stroke)}}
            7 -> { // Standard appearance / half-light symbol.
                drawCircle(color,w*.34f,style=stroke)
                drawArc(color,-90f,180f,true,point(.16f,.16f),androidx.compose.ui.geometry.Size(w*.68f,w*.68f))
            }
            19 -> { // Sun: circle with eight rays.
                drawCircle(color,w*.18f,style=stroke)
                for(i in 0..7){val a=i*Math.PI/4;val x=kotlin.math.cos(a).toFloat();val y=kotlin.math.sin(a).toFloat();drawLine(color,point(.5f+x*.29f,.5f+y*.29f),point(.5f+x*.42f,.5f+y*.42f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            }
            20 -> { // Check mark.
                val path=Path().apply{moveTo(w*.19f,w*.5f);lineTo(w*.41f,w*.72f);lineTo(w*.82f,w*.25f)};drawPath(path,color,style=stroke)
            }
            21 -> {drawArc(color,-55f,290f,false,point(.18f,.20f),androidx.compose.ui.geometry.Size(w*.64f,w*.64f),style=stroke);drawLine(color,point(.5f,.09f),point(.5f,.47f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            25 -> {drawLine(color,point(.5f,.23f),point(.5f,.77f),strokeWidth=stroke.width,cap=StrokeCap.Round);drawLine(color,point(.23f,.5f),point(.77f,.5f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            23 -> { // Video player: standard play symbol in a portrait player.
                drawRoundRect(color,point(.19f,.10f),androidx.compose.ui.geometry.Size(w*.62f,w*.8f),androidx.compose.ui.geometry.CornerRadius(w*.10f),style=stroke)
                val p=Path().apply{moveTo(w*.43f,w*.34f);lineTo(w*.66f,w*.5f);lineTo(w*.43f,w*.66f);close()};drawPath(p,color)
            }
            24 -> { // Hourglass: the remaining daily allowance.
                drawLine(color,point(.24f,.12f),point(.76f,.12f),strokeWidth=stroke.width,cap=StrokeCap.Round)
                drawLine(color,point(.24f,.88f),point(.76f,.88f),strokeWidth=stroke.width,cap=StrokeCap.Round)
                val p=Path().apply{moveTo(w*.30f,w*.15f);lineTo(w*.30f,w*.30f);lineTo(w*.65f,w*.69f);lineTo(w*.70f,w*.85f);moveTo(w*.70f,w*.15f);lineTo(w*.70f,w*.30f);lineTo(w*.35f,w*.69f);lineTo(w*.30f,w*.85f)};drawPath(p,color,style=stroke)
                drawLine(color,point(.4f,.74f),point(.6f,.74f),strokeWidth=stroke.width,cap=StrokeCap.Round)
            }
            22 -> {drawLine(color,point(.37f,.24f),point(.37f,.76f),strokeWidth=stroke.width,cap=StrokeCap.Round);drawLine(color,point(.63f,.24f),point(.63f,.76f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            8 -> { // Miso's asymmetric ears, flower and happy face, in an unfilled sketch.
                val left=Path().apply{moveTo(w*.29f,w*.40f);cubicTo(w*.15f,w*.05f,w*.34f,w*.01f,w*.43f,w*.33f)}
                val right=Path().apply{moveTo(w*.60f,w*.33f);cubicTo(w*.59f,w*.04f,w*.79f,w*.06f,w*.78f,w*.42f)}
                drawPath(left,color,style=stroke);drawPath(right,color,style=stroke)
                drawOval(color,point(.14f,.31f),androidx.compose.ui.geometry.Size(w*.73f,w*.54f),style=stroke)
                drawCircle(color,w*.026f,point(.37f,.55f));drawCircle(color,w*.026f,point(.64f,.55f))
                val smile=Path().apply{moveTo(w*.43f,w*.68f);quadraticTo(w*.51f,w*.77f,w*.59f,w*.68f)};drawPath(smile,color,style=stroke)
                for(i in 0..4){val angle=i*Math.PI*2/5;drawCircle(color,w*.028f,point(.25f+kotlin.math.cos(angle).toFloat()*.047f,.37f+kotlin.math.sin(angle).toFloat()*.047f),style=androidx.compose.ui.graphics.drawscope.Stroke(w*.025f))}
            }
            9 -> {drawCircle(color,w*.07f,point(.5f,.15f));drawLine(color,point(.16f,.34f),point(.84f,.34f),strokeWidth=stroke.width,cap=StrokeCap.Round);drawLine(color,point(.5f,.29f),point(.5f,.58f),strokeWidth=stroke.width);drawLine(color,point(.5f,.58f),point(.28f,.88f),strokeWidth=stroke.width,cap=StrokeCap.Round);drawLine(color,point(.5f,.58f),point(.72f,.88f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            10 -> {drawOval(color,point(.15f,.13f),androidx.compose.ui.geometry.Size(w*.70f,w*.24f),style=stroke);drawArc(color,0f,180f,false,point(.15f,.39f),androidx.compose.ui.geometry.Size(w*.70f,w*.24f),style=stroke);drawArc(color,0f,180f,false,point(.15f,.65f),androidx.compose.ui.geometry.Size(w*.70f,w*.24f),style=stroke);drawLine(color,point(.15f,.25f),point(.15f,.77f),strokeWidth=stroke.width);drawLine(color,point(.85f,.25f),point(.85f,.77f),strokeWidth=stroke.width)}
            11 -> {drawCircle(color,w*.38f,style=stroke);drawCircle(color,w*.032f,point(.5f,.3f));drawLine(color,point(.5f,.46f),point(.5f,.72f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
            12 -> {val p=Path().apply{moveTo(w*.5f,w*.1f);lineTo(w*.83f,w*.24f);lineTo(w*.78f,w*.6f);quadraticTo(w*.70f,w*.80f,w*.5f,w*.91f);quadraticTo(w*.30f,w*.80f,w*.22f,w*.6f);lineTo(w*.17f,w*.24f);close()};drawPath(p,color,style=stroke);drawLine(color,point(.36f,.49f),point(.47f,.6f),strokeWidth=stroke.width);drawLine(color,point(.47f,.6f),point(.66f,.38f),strokeWidth=stroke.width)}
            13 -> {val p=Path().apply{moveTo(w*.2f,w*.72f);quadraticTo(w*.29f,w*.63f,w*.29f,w*.40f);cubicTo(w*.29f,w*.1f,w*.71f,w*.1f,w*.71f,w*.40f);quadraticTo(w*.71f,w*.63f,w*.8f,w*.72f);close()};drawPath(p,color,style=stroke);drawArc(color,0f,180f,false,point(.4f,.73f),androidx.compose.ui.geometry.Size(w*.2f,w*.15f),style=stroke)}
            5 -> {drawCircle(color,w*.36f,style=stroke);drawCircle(color,w*.20f,style=stroke);drawCircle(color,w*.045f)}
            6 -> {val p=Path().apply{moveTo(w*.15f,w*.38f);lineTo(w*.34f,w*.38f);lineTo(w*.56f,w*.19f);lineTo(w*.56f,w*.81f);lineTo(w*.34f,w*.62f);lineTo(w*.15f,w*.62f);close()};drawPath(p,color,style=stroke);drawArc(color,-50f,100f,false,topLeft=point(.45f,.2f),size=androidx.compose.ui.geometry.Size(w*.43f,w*.60f),style=stroke)}
            4 -> {drawCircle(color,w*.36f,style=stroke);drawLine(color,point(.41f,.34f),point(.41f,.66f),strokeWidth=stroke.width,cap=StrokeCap.Round);drawLine(color,point(.59f,.34f),point(.59f,.66f),strokeWidth=stroke.width,cap=StrokeCap.Round)}
        }
    }
}
