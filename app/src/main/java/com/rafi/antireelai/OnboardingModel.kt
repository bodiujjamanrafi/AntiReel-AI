package com.rafi.antireelai

internal data class OnboardingQuestion(val en:String,val bn:String,val options:List<String>,val banglaOptions:List<String>)
// Stable IDs retain compatibility with saved answers; removed prompts are never shown.
internal val onboardingQuestionIds=listOf(0,2,8,11)
internal val onboardingQuestions=mapOf(
    0 to OnboardingQuestion("What should I call you?","আমি তোমাকে কী নামে ডাকব?",listOf(),listOf()),
    2 to OnboardingQuestion("Which platforms do you watch short videos on most?","কোন অ্যাপে বেশি ছোট ভিডিও দেখো?",listOf("Instagram","Facebook","YouTube Shorts","TikTok","Other"),listOf("ইনস্টাগ্রাম","ফেসবুক","ইউটিউব শর্টস","টিকটক","অন্যান্য")),
    8 to OnboardingQuestion("How many reels would you like each day?","প্রতিদিন কতটি রিলস দেখতে চাও?",listOf("5 reels","10 reels","20 reels","30 reels","50 reels","Custom number"),listOf("৫টি রিলস","১০টি রিলস","২০টি রিলস","৩০টি রিলস","৫০টি রিলস","নিজে সংখ্যা লিখি")),
    11 to OnboardingQuestion("Ready to take control of your scrolling?","নিজের স্ক্রল করার অভ্যাস নিয়ন্ত্রণে প্রস্তুত?",listOf("Yes, let’s do this!","Start small","Review my goals"),listOf("হ্যাঁ, শুরু করি!","অল্প করে শুরু করি","লক্ষ্যগুলো দেখি"))
)
internal fun localized(language:String,en:String,bn:String)=if(language=="bn")bn else en
internal fun validName(value:String)=value.trim().length in 1..40 && value.trim().all{it.isLetterOrDigit() || Character.getType(it) in listOf(Character.NON_SPACING_MARK.toInt(),Character.COMBINING_SPACING_MARK.toInt()) || it in " '-’"}
internal fun validMinutes(value:String)=parseMinutes(value)!=null
internal fun validReels(value:String)=value.toIntOrNull()?.let{it in 1..10000}==true
internal fun parseMinutes(value:String):Int? {
    val parts=value.trim().split(':')
    val result=if(parts.size==2){val h=parts[0].toIntOrNull() ?:return null;val m=parts[1].toIntOrNull() ?:return null;if(h !in 0..24 || m !in 0..59)return null;h*60+m}else value.toIntOrNull()
    return result?.takeIf{it in 1..1440}
}
internal fun validOnboardingAnswer(step:Int,name:String,choices:List<Int>,custom:String):Boolean {
    if(step==0)return validName(name)
    val question=onboardingQuestions[step] ?:return false
    if(choices.isEmpty() || choices.any{it !in question.options.indices})return false
    val other=when(step){8->5;2->4;else->-1}
    return if(other !in choices)true else when(step){8->validReels(custom);else->custom.trim().length in 1..100}
}
internal fun onboardingAnswerLabel(step:Int,name:String,choices:List<Int>,custom:String,language:String):String {
    if(step==0)return name.trim()
    val question=onboardingQuestions.getValue(step)
    val other=when(step){8->5;2->4;else->-1}
    return choices.joinToString(" · "){index->
        if(index==other)custom.trim() else localized(language,question.options.getOrElse(index){""},question.banglaOptions.getOrElse(index){""})
    }
}
internal fun onboardingPose(step:Int,choices:List<Int>,responding:Boolean):MisoPose = when {
    step==12->MisoPose.CELEBRATING
    step==-3->MisoPose.GREETING
    !responding->MisoPose.LISTENING
    step==8->MisoPose.ENCOURAGING
    step==11->MisoPose.CELEBRATING
    else->MisoPose.HAPPY
}

internal data class OnboardingProfile(val name:String,val language:String,val minutes:Int,val reels:Int,val platforms:Set<String>,val reminderStyle:Int,val muted:Boolean,val draft:String) {
    val valid get()=validName(name) && language in setOf("en","bn") && minutes in 0..1440 && reels in 1..10000 && reminderStyle in 0..3 && platforms.all{id->com.rafi.antireelai.platforms.any{it.id==id}}
}

// Old drafts resume at the next retained prompt without renumbering saved answers.
internal fun restoredOnboardingStep(step:Int):Int = if(step in 0..11) onboardingQuestionIds.firstOrNull{it>=step} ?:12 else step
internal fun nextOnboardingStep(step:Int)=when(step){-1->-4;-4->-3;-3->0;else->onboardingQuestionIds.firstOrNull{it>step} ?:12}
internal fun previousOnboardingStep(step:Int)=when(step){0->-3;-3->-4;-4->-1;else->onboardingQuestionIds.lastOrNull{it<step} ?: -1}
internal fun canGoBackInOnboarding(step:Int)=step>=0 || step==-3 || step==-4
