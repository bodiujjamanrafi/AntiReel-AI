package com.rafi.antireelai

/** Increment when the disclosed accessibility data use changes. Old choices are
 * retained on disk, but do not authorize tracking under a different disclosure. */
internal object AccessibilityConsent {
    const val VERSION=2
    const val KEY="accessibilityDisclosureVersion"
    fun current(accepted:Boolean,version:Int)=accepted && version==VERSION
    fun disclosure(language:String)=localized(language,
        "AntiReel uses Android Accessibility while Miso is running to inspect visible accessibility labels/content, video metadata and scroll/page events in your selected social apps. Other visible text can be encountered while checking for a player and is discarded. It uses this information on your phone to estimate reels and time, recognize repeats, and display Miso and the counter over those apps. At your chosen limit, AntiReel requests media pause, returns to Home and shows a warning. Counts, durations, platform choices and settings are saved locally; screen text and video identifiers are not saved or sent to us. Opt-in Android diagnostics contain numeric event states, not screen content. You can decline, pause Miso, or revoke accessibility in Android Settings at any time. Android Home, Back and uninstall remain available.",
        "মিসো চালু থাকলে অ্যান্টিরিল অ্যান্ড্রয়েড অ্যাক্সেসিবিলিটি ব্যবহার করে তোমার বাছাই করা সোশ্যাল অ্যাপের দৃশ্যমান লেখা বা কনটেন্ট, ভিডিওর তথ্য এবং স্ক্রল বা পেজ পরিবর্তনের ঘটনা পড়ে। প্লেয়ার খোঁজার সময় অন্য দৃশ্যমান লেখাও সাময়িকভাবে পড়তে পারে; তা ফেলে দেওয়া হয়। ফোনেই এই তথ্য দিয়ে রিলস ও সময় অনুমান করে, পুনরাবৃত্তি শনাক্ত করে এবং অ্যাপের ওপর মিসো ও কাউন্টার দেখায়। তোমার নির্ধারিত সীমায় মিডিয়া থামানোর অনুরোধ করে, হোমে ফেরে এবং সতর্কতা দেখায়। সংখ্যা, সময়, অ্যাপ বাছাই ও সেটিংস ফোনে থাকে; পর্দার লেখা ও ভিডিওর পরিচয় সংরক্ষণ করা বা আমাদের পাঠানো হয় না। ঐচ্ছিক অ্যান্ড্রয়েড ডায়াগনস্টিকে শুধু সংখ্যাভিত্তিক ঘটনার অবস্থা থাকে। তুমি না বলতে, মিসো থামাতে বা অ্যান্ড্রয়েড সেটিংসে অ্যাক্সেস বন্ধ করতে পারো। হোম, ব্যাক ও আনইনস্টল সবসময় ব্যবহার করা যায়।")
}
