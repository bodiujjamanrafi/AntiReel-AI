package com.rafi.antireelai

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.View
import kotlin.math.*
import com.rafi.antireelai.tracking.LIMIT_WARNING_FRACTION

/** Locally rendered 3D clay bunny: lit ellipsoid meshes, depth sorting and projected facial curves. */
class CompanionView(context: Context) : View(context) {
    var pose=MisoPose.IDLE
        set(value){if(field!=value){field=value;invalidate()}}
    var animated = true
        set(value) { if(field != value) { field=value; invalidate() } }
    var activity = CompanionActivity.STANDBY
        set(value) { if(field!=value) { field=value;invalidate() } }
    var talking=false
        set(value){if(field!=value){field=value;invalidate()}}
    var expressive = true
        set(value) { if(field != value) { field=value; invalidate() } }
    var progress = 0f
        set(value) { if(field != value) { field=value.coerceAtLeast(0f); invalidate() } }
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    private val visibleRect=Rect()
    private val start=SystemClock.uptimeMillis()
    private var reactUntil=0L
    private val bitmapPaint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val bodyDestination=RectF()
    private val faceCos=cos(-.06f)
    private val faceSin=sin(-.06f)
    fun reactToCount() { reactUntil=SystemClock.uptimeMillis()+650; invalidate() }
    override fun onDraw(canvas: Canvas) {
        val now=SystemClock.uptimeMillis()
        val moving=animated && android.animation.ValueAnimator.areAnimatorsEnabled()
        val t=if(moving) (now-start)/1000f else 0f
        val p=if(expressive) progress else 0f
        val mood=if(expressive) CompanionMood.forActivity(p,activity) else CompanionMood.RELAXED
        val reaction=if(moving && now<reactUntil) sin((reactUntil-now)/650f*PI).toFloat() else 0f
        // Body geometry stays in a cached bitmap. Idle motion transforms that image;
        // only small facial paths change per frame.
        val scale=min(width/2.05f,height/2.05f)
        if(width<=0 || height<=0) return
        val body=CompanionBodyCache.body(mood.droopy)
        val side=min(width,height).toFloat()
        val left=(width-side)/2f;val top=height*.55f-side*.55f
        bodyDestination.set(left,top,left+side,top+side)
        canvas.save()
        if(moving) {
            canvas.translate(0f,sin(t*1.7f)*.018f*scale)
            val emphasis=when(pose){MisoPose.CELEBRATING->6f;MisoPose.GREETING->4f;MisoPose.THINKING->1.5f;MisoPose.SURPRISED->.5f;MisoPose.ENCOURAGING->2.5f;MisoPose.HAPPY->3f;MisoPose.LISTENING->.7f;else->0f}
            canvas.rotate(sin(t*4)*emphasis,width/2f,height*.55f)
            canvas.rotate(if(mood==CompanionMood.BREATHE) sin(t*4f)*2.5f else sin(t*.9f)*2f,width/2f,height*.55f)
            val lift=when(pose){MisoPose.CELEBRATING->-abs(sin(t*5f))*.05f;MisoPose.GREETING->sin(t*3f)*.018f;MisoPose.THINKING->-.01f;else->0f}
            canvas.translate(0f,lift*scale)
            val bounce=1f+reaction*.025f+if(pose==MisoPose.CELEBRATING)abs(sin(t*5f))*.025f else 0f
            canvas.scale(bounce,bounce,width/2f,height*.55f)
        }
        canvas.drawBitmap(body,null,bodyDestination,bitmapPaint)
        canvas.translate(width/2f,height*.55f);canvas.scale(scale,scale)
        paint.style=Paint.Style.FILL
        val blink=if(moving && t%5f>4.82f) .10f else 1f
        val sleepy=if(pose==MisoPose.SURPRISED)1.2f else if(mood==CompanionMood.RESTING) .1f else if(mood==CompanionMood.BREATHE) .45f else 1f
        paint.color=0xFF3D2A22.toInt()
        eye(canvas,-.16f,-.12f,.049f,.070f*blink*sleepy)
        eye(canvas,.16f,-.12f,.049f,.070f*blink*sleepy*(1f-reaction*.8f))
        paint.color=0xFFFDF6E8.toInt();eye(canvas,-.171f,-.139f,.012f,.015f*blink);eye(canvas,.149f,-.139f,.012f,.015f*blink)
        paint.color=0xAAFFF6FD.toInt();eye(canvas,-.145f,-.101f,.006f,.007f*blink);eye(canvas,.175f,-.101f,.006f,.007f*blink)
        paint.color=0xBCF38FB4.toInt();eye(canvas,-.27f,-.01f,.058f,.025f);eye(canvas,.27f,-.01f,.058f,.025f)
        paint.color=0xFFB77768.toInt();eye(canvas,0f,-.025f,.025f,.018f)
        paint.color=0xFF35433C.toInt();paint.style=Paint.Style.STROKE;paint.strokeWidth=.014f;paint.strokeCap=Paint.Cap.ROUND
        path.rewind()
        for(i in 0..16) {
            val x=-.065f+i*.13f/16
            val y=when(mood) { CompanionMood.BREATHE -> .085f-sin(i*PI/16).toFloat()*.035f; else -> .04f+sin(i*PI/16).toFloat()*.045f }
            facePoint(x,y);if(i==0) path.moveTo(fx,fy) else path.lineTo(fx,fy)
        };canvas.drawPath(path,paint)
                // Delicate whiskers and tiny cheek-side paw creases.
        paint.color=0x888C6855.toInt();paint.strokeWidth=.006f
        for(sideIndex in 0..1) for(i in 0..2) {
            val side=if(sideIndex==0)-1f else 1f
            path.rewind();facePoint(side*.28f,-.025f+i*.035f);path.moveTo(fx,fy)
            facePoint(side*.40f,-.045f+i*.053f);path.lineTo(fx,fy);canvas.drawPath(path,paint)
        }
        paint.color=0xFF35433C.toInt();paint.strokeWidth=.014f
        if(talking || pose==MisoPose.SURPRISED) {
            paint.style=Paint.Style.FILL;eye(canvas,0f,.065f,.04f,.015f+abs(sin((now-start)/1000f*12f))*.025f);paint.style=Paint.Style.STROKE
        }
        if(mood==CompanionMood.BREATHE) {
            brow(canvas,-.22f,-.09f,.06f)
            brow(canvas,.09f,.22f,-.06f)
        }
        paint.style=Paint.Style.FILL
        canvas.restore()
        if((moving || talking) && isShown && windowVisibility==VISIBLE && getLocalVisibleRect(visibleRect)) {
            if(moving)postInvalidateOnAnimation() else postInvalidateDelayed(33)
        }
    }
    override fun onVisibilityAggregated(isVisible:Boolean) {
        super.onVisibilityAggregated(isVisible)
        if(isVisible)invalidate()
    }
    private var fx=0f;private var fy=0f;private var faceScale=1f
    private fun facePoint(x:Float,y:Float) {
        val dx=x/.53f;val dy=(y+.10f)/.45f
        val z=.032f+.35f*sqrt((1f-dx*dx-dy*dy).coerceAtLeast(.05f))
        val xx=x*faceCos+z*faceSin;val depth=-x*faceSin+z*faceCos
        faceScale=2.8f/(2.8f-depth)
        fx=xx*faceScale;fy=y*faceScale
    }
    private fun eye(canvas:Canvas,x:Float,y:Float,rx:Float,ry:Float) {
        // Native analytic ovals avoid rebuilding 20-point polygons and trigonometry
        // for each eye, cheek and highlight on every animation frame.
        facePoint(x,y)
        canvas.drawOval(fx-rx*faceScale,fy-ry*faceScale,fx+rx*faceScale,fy+ry*faceScale,paint)
    }
    private fun brow(canvas:Canvas,start:Float,end:Float,slope:Float) {
        path.rewind();facePoint(start,-.23f);path.moveTo(fx,fy);facePoint(end,-.23f+slope);path.lineTo(fx,fy);canvas.drawPath(path,paint)
    }
}

enum class CompanionActivity { STANDBY, ACTIVE, BREAK }

enum class CompanionMood(val title:String) {
    RELAXED("Happy and looking out for you"), BREATHE("Angry: time to stop scrolling"), RESTING("Enjoying a little rest");
    val droopy: Boolean get() = this==RESTING
    companion object {
        fun forActivity(progress:Float,activity:CompanionActivity)=if(activity==CompanionActivity.BREAK) RESTING else fromProgress(progress)
        fun fromProgress(progress:Float)=if(progress>=1f) BREATHE else RELAXED }
}

enum class MisoPose { IDLE, GREETING, LISTENING, THINKING, SURPRISED, ENCOURAGING, HAPPY, CELEBRATING }
