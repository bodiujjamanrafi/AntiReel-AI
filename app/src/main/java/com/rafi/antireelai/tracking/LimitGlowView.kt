package com.rafi.antireelai.tracking

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import kotlin.math.*

/** Cached red veil and text, with a slow, low-amplitude halo. Never flashes or takes touch focus. */
internal class LimitGlowView(context:Context):View(context) {
    var originY=160f
        set(value){if(field!=value){field=value;shaderWidth=0;invalidate()}}
    var progress=0f
        set(value){if(field!=value){field=value;updateMessage();invalidate()}}
    var reducedMotion=false
        set(value){if(field!=value){field=value;invalidate()}}
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val halo=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeWidth=2*resources.displayMetrics.density;color=0xFFFFC2CF.toInt()}
    private val titlePaint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.WHITE;textSize=26*resources.displayMetrics.scaledDensity;typeface=Typeface.create("sans-serif",Typeface.BOLD)}
    private val bodyPaint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply{color=0xFFFFE8ED.toInt();textSize=15*resources.displayMetrics.scaledDensity}
    private var title:StaticLayout?=null
    private var body:StaticLayout?=null
    private var messageMode= -1
    private var textWidth=0
    private var shaderWidth=0
    private var shaderHeight=0
    private val started=SystemClock.uptimeMillis()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS}
    override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int){super.onSizeChanged(w,h,oldw,oldh);updateMessage()}
    private fun updateMessage() {
        val mode=when{progress>1f->3;progress>=1f->2;progress>=LIMIT_CURTAIN_FRACTION->1;else->0}
        val available=min((width-48*resources.displayMetrics.density).toInt(),(340*resources.displayMetrics.density).toInt()).coerceAtLeast(1)
        if(mode==messageMode && textWidth==available)return
        messageMode=mode;textWidth=available
        val heading=if(mode>=2) "That's enough scrolling." else if(mode==1) "Let's stop here." else "Your time matters."
        val detail=if(mode>=2) "Your daily reel limit is reached.\nOpen AntiReel and take a breather." else "You're close to your daily reel limit.\nOpen AntiReel for a break."
        fun layout(text:String,p:TextPaint)=StaticLayout.Builder.obtain(text,0,text.length,p,available).setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(false).setLineSpacing(5*resources.displayMetrics.density,1f).build()
        title=layout(heading,titlePaint);body=layout(detail,bodyPaint)
    }
    override fun onDraw(canvas:Canvas) {
        val cover=limitCoverOpacity(progress)
        if(cover==0f)return
        if(shaderWidth!=width || shaderHeight!=height){
            shaderWidth=width;shaderHeight=height
            paint.shader=RadialGradient(width/2f,originY,height.toFloat().coerceAtLeast(1f),intArrayOf(0xFFAA1841.toInt(),0xFF74112E.toInt(),0xFF400C20.toInt()),floatArrayOf(0f,.4f,1f),Shader.TileMode.CLAMP)
        }
        val animated= !reducedMotion && android.animation.ValueAnimator.areAnimatorsEnabled()
        val elapsed=(SystemClock.uptimeMillis()-started)/1000f
        val reveal=if(animated)(elapsed/.55f).coerceIn(0f,1f) else 1f
        // The video stays fully obscured from 95%; only the decorative halo gently breathes.
        paint.alpha=(255*cover*reveal).toInt()
        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint)
        val breath=if(animated)((sin(elapsed*2*PI/4)+1)/2).toFloat() else .5f
        halo.alpha=(12+breath*16).toInt()
        val radius=width*(.2f+breath*.045f)
        canvas.drawCircle(width/2f,originY,radius,halo)
        canvas.drawCircle(width/2f,originY,radius+32*resources.displayMetrics.density,halo)
        val titleLayout=title;val bodyLayout=body
        if(titleLayout!=null && bodyLayout!=null){
            val gap=16*resources.displayMetrics.density
            val y=(height-titleLayout.height-bodyLayout.height-gap)/2f
            canvas.save();canvas.translate((width-textWidth)/2f,y)
            titleLayout.draw(canvas);canvas.translate(0f,titleLayout.height+gap);bodyLayout.draw(canvas);canvas.restore()
        }
        // 20 fps is sufficient for a four-second halo cycle; no layout or shader rebuild per frame.
        if(animated && isShown && windowVisibility==VISIBLE)postInvalidateDelayed(50)
    }
}
