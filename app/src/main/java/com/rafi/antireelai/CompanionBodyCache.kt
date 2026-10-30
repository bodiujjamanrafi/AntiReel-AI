package com.rafi.antireelai

import android.graphics.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

/** Two shared body textures; resizing a mascot never rebuilds or rasterizes its mesh. */
internal object CompanionBodyCache {
    private const val RESOLUTION=640
    private val textures=ConcurrentHashMap<Boolean,Bitmap>()
    private val lock=Any()
    private val warming=AtomicBoolean(false)
    private var renderer:MisoBodyRenderer?=null
    fun warm() {
        if(warming.compareAndSet(false,true))Thread({body(false);body(true)},"Miso body warmup").apply{isDaemon=true;priority=Thread.NORM_PRIORITY-1}.start()
    }
    fun body(resting:Boolean):Bitmap= textures[resting] ?: synchronized(lock) {
        textures[resting] ?: (renderer ?: MisoBodyRenderer().also{renderer=it}).render(RESOLUTION,resting).also {
            textures[resting]=it
            if(textures.size==2)renderer=null
        }
    }
}

/** No View, Context or Activity is retained; the warmup can run off the UI thread. */
internal class MisoBodyRenderer(private val yaw:Float=-.06f) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    private val tilt=0f
    private val bob=0f
    private val comparator=Comparator<Triangle>{a,b->a.depth.compareTo(b.depth)}
    private val vertices=ArrayList<Vertex>()
    private val triangles=ArrayList<Triangle>()
    private class Vertex(val x:Float,val y:Float,val z:Float,val nx:Float,val ny:Float,val nz:Float,val part:Int) { var px=0f;var py=0f;var depth=0f;var normalZ=0f;var light=1f }
    private class Triangle(val a:Vertex,val b:Vertex,val c:Vertex,val color:Int) { var depth=0f }

    fun render(size:Int,resting:Boolean):Bitmap {
        ensureGeometry()
        return Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888).apply{density=Bitmap.DENSITY_NONE}.also {
            val canvas=Canvas(it);canvas.translate(size/2f,size*.55f)
            val scale=size/2.05f;canvas.scale(scale,scale);drawBody(canvas,resting)
        }
    }
    private fun ensureGeometry() {
        if(vertices.isNotEmpty())return
        val cream=0xFFF294BB.toInt(); val inner=0xFFFFD4E5.toInt()
        sphere(0f,.34f,-.08f,.35f,.31f,.29f,cream,0,8,16)
        sphere(-.255f,-.64f,-.01f,.14f,.30f,.13f,cream,1,8,12)
        sphere(.30f,-.61f,-.01f,.15f,.29f,.13f,cream,2,8,12)
        sphere(-.255f,-.655f,.095f,.078f,.20f,.045f,inner,1,6,10)
        sphere(.30f,-.63f,.095f,.085f,.20f,.045f,inner,2,6,10)
        sphere(-.25f,.58f,.10f,.17f,.10f,.20f,cream,0,6,12)
        sphere(.25f,.58f,.10f,.17f,.10f,.20f,cream,0,6,12)
        // The former forward, high arm caps intersected the cheeks after projection.
        // Keep both complete rounded arms/paws, but tuck their upper caps beneath
        // the face. The head supplies the natural rounded cheeks.
        sphere(-.33f,.25f,.18f,.14f,.17f,.13f,cream,3,8,14)
        sphere(.33f,.25f,.18f,.14f,.17f,.13f,cream,4,8,14)
        sphere(0f,-.10f,.02f,.53f,.45f,.35f,cream,0,22,44)
        // A tiny sculpted flower clip replaces the hat; the asymmetric ears stay visible.
        for(i in 0..4) {
            val angle=i*2*PI/5
            sphere(-.37f+cos(angle).toFloat()*.062f,-.39f+sin(angle).toFloat()*.062f,.28f,.052f,.052f,.038f,0xFFFFF0CF.toInt(),5,5,8)
        }
        sphere(-.37f,-.39f,.325f,.035f,.035f,.025f,0xFFFFDCA4.toInt(),5,5,8)
        // The head already supplies the rounded cheek volume. These two former
        // under-eye overlays intersected it and exposed dark folded facets.
        // Facial blush is still painted by CompanionView; neither arm is removed.
    }
    private fun sphere(cx:Float,cy:Float,cz:Float,rx:Float,ry:Float,rz:Float,color:Int,part:Int,rings:Int,segments:Int) {
        val base=vertices.size
        for(i in 0..rings) for(j in 0..segments) {
            val a=PI*i/rings;val b=2*PI*j/segments
            val x=(sin(a)*cos(b)).toFloat();val y=cos(a).toFloat();val z=(sin(a)*sin(b)).toFloat()
            val cheek=if(part==0 && cy<0f) 1f+.075f*(1f-y)*.5f else 1f
            val earLean=if(part==1) -.055f*y*y else if(part==2) .10f*y else 0f
            vertices.add(Vertex(cx+rx*x*cheek+earLean,cy+ry*y,cz+rz*z,x,y,z,part))
        }
        for(i in 0 until rings) for(j in 0 until segments) {
            val a=base+i*(segments+1)+j;val b=a+segments+1
            triangles.add(Triangle(vertices[a],vertices[b],vertices[a+1],color))
            triangles.add(Triangle(vertices[a+1],vertices[b],vertices[b+1],color))
        }
    }
    private fun drawBody(canvas:Canvas,resting:Boolean) {
        paint.style=Paint.Style.FILL;paint.color=0x18000000
        canvas.drawOval(-.38f,.67f,.38f,.73f,paint)
        val c=cos(yaw);val s=sin(yaw);val ct=cos(tilt);val st=sin(tilt)
        for(v in vertices) {
            var x=v.x;var y=v.y
            if(resting && (v.part==1 || v.part==2)) y+=.045f
            val xx=x*c+v.z*s;val z=-x*s+v.z*c
            val perspective=2.8f/(2.8f-z)
            v.px=(xx*ct-y*st)*perspective;v.py=(xx*st+y*ct+bob)*perspective;v.depth=z
            val nx=v.nx*c+v.nz*s;val nz=-v.nx*s+v.nz*c
            v.normalZ=nz
            v.light=(.70f+max(0f,-nx*.35f-v.ny*.5f+nz*.75f)*.27f+max(0f,nz).pow(18f)*.08f).coerceIn(.65f,1.07f)
        }
        for(f in triangles) f.depth=(f.a.depth+f.b.depth+f.c.depth)/3f
        triangles.sortWith(comparator)
        for(f in triangles) {
            if(f.a.normalZ+f.b.normalZ+f.c.normalZ<=-.1f) continue
            val light=(f.a.light+f.b.light+f.c.light)/3f
            paint.color=Color.rgb((Color.red(f.color)*light).toInt().coerceIn(0,255),(Color.green(f.color)*light).toInt().coerceIn(0,255),(Color.blue(f.color)*light).toInt().coerceIn(0,255))
            path.rewind();path.moveTo(f.a.px,f.a.py);path.lineTo(f.b.px,f.b.py);path.lineTo(f.c.px,f.c.py);path.close();canvas.drawPath(path,paint)
        }
    }
}
