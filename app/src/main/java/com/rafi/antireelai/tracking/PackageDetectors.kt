package com.rafi.antireelai.tracking

/** Package-specific adapters reuse the established session/repeat gate, not a swipe counter. */
internal object PackageDetectors {
    fun recognize(detector:String?,nodes:List<ReelNode>):ReelFrame=when(detector) {
        null->ReelFrame(false,null,"unsupported_detector")
        "youtube"->YouTubeShortsRecognition.recognize(nodes)
        "facebook_lite"->ReelFrame(false,null,"unsupported_detector")
        else->ReelRecognition.recognize(detector,nodes)
    }
}
/** A paused/disabled companion needs no four-second polling; preferences/alarms restart it. */
internal fun trackingHeartbeatRequired(running:Boolean,consent:Boolean,hasDetector:Boolean,paused:Boolean)=
    running && consent && hasDetector && !paused
