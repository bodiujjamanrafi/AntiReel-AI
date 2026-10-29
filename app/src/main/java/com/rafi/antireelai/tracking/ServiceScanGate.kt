package com.rafi.antireelai.tracking

/** Invalidates asynchronous evidence when the connection or user intent changes. */
internal class ServiceScanGate {
    @Volatile
    var connected=false
        private set
    private val epoch=java.util.concurrent.atomic.AtomicLong()
    fun connect(){connected=true;invalidate()}
    fun disconnect(){connected=false;invalidate()}
    fun invalidate(){epoch.incrementAndGet()}
    fun ticket()=epoch.get()
    fun accepts(ticket:Long)=connected && ticket==epoch.get()
}
