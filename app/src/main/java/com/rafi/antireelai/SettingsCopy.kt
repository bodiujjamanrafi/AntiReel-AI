package com.rafi.antireelai

/** Settings additions share the app's existing Bengali/English language source. */
internal object SettingsCopy {
    val bn=mapOf(
        "Saved preference. Allow Android notifications for delivery." to "পছন্দ সংরক্ষিত। বিজ্ঞপ্তি পেতে অ্যান্ড্রয়েডের অনুমতি দাও।", "Android permission is needed for break notifications." to "বিরতির বিজ্ঞপ্তির জন্য অ্যান্ড্রয়েডের অনুমতি দরকার।",
        "Current value" to "বর্তমান মান", "Activity data" to "ব্যবহারের তথ্য", "Saved on this phone. No cloud account." to "এই ফোনে সংরক্ষিত। ক্লাউড অ্যাকাউন্ট নেই।", "These switches save your preferences. Android notification permission is required for delivery." to "এই সুইচগুলো তোমার পছন্দ সংরক্ষণ করে। বিজ্ঞপ্তি পেতে অ্যান্ড্রয়েডের অনুমতি দরকার।",
        "Overlay controls" to "মিসোর নিয়ন্ত্রণ",
        "Preferences" to "পছন্দ", "Theme" to "থিম", "Display" to "পর্দা", "Daily boundaries" to "দৈনিক সীমা", "At your boundary" to "সীমায় পৌঁছালে", "Saved" to "সংরক্ষিত",
        "Voice & greetings" to "কণ্ঠ ও স্বাগত", "Limit warnings" to "সীমার সতর্কতা", "Recognition" to "শনাক্তকরণ", "Android access" to "অ্যান্ড্রয়েড অনুমতি", "Service" to "পরিষেবা", "Enabled" to "চালু", "Off" to "বন্ধ", "Connected" to "সংযুক্ত", "Not connected" to "সংযুক্ত নয়",
        "Days with records" to "হিসাব রাখা দিন", "Local storage" to "ফোনে সংরক্ষণ", "History retention" to "ইতিহাস সংরক্ষণ", "Local settings & activity" to "ফোনের সেটিংস ও ব্যবহার", "Danger zone" to "ডেটা মোছা",
        "Troubleshooting" to "সমস্যার সমাধান", "Counting issues" to "গণনার সমস্যা", "Voice problems" to "কণ্ঠের সমস্যা", "Notification problems" to "বিজ্ঞপ্তির সমস্যা", "Quick actions" to "দ্রুত পদক্ষেপ",
        "Recorded time" to "রেকর্ড করা সময়", "How tracking works" to "ট্র্যাকিং যেভাবে কাজ করে", "Time & attention" to "সময় ও মনোযোগ", "Reels & repeat detection" to "রিলস ও পুনরাবৃত্তি শনাক্তকরণ", "Your data" to "তোমার ডেটা", "On-device storage" to "ফোনেই সংরক্ষণ", "Selected apps only" to "শুধু বেছে নেওয়া অ্যাপ", "What AntiReel saves" to "অ্যান্টিরিল যা সংরক্ষণ করে", "What stays private" to "যা গোপন থাকে", "Android permissions" to "অ্যান্ড্রয়েডের অনুমতি", "Android" to "অ্যান্ড্রয়েড",
        "Live preview" to "সরাসরি নমুনা", "Selection" to "নির্বাচন", "How the extra allowance works" to "অতিরিক্ত সীমা যেভাবে কাজ করে", "Not set" to "নির্ধারিত নয়", "Daily time" to "দৈনিক সময়", "Allowed today" to "আজকের সীমা",
        "Device follows your phone’s appearance. Your choice applies throughout AntiReel." to "ফোনের থিম বেছে নিলে ফোনের চেহারা অনুসরণ করবে। তোমার পছন্দ পুরো অ্যান্টিরিলে প্রযোজ্য।",
        "Text, controls and Miso adapt to your selected theme." to "লেখা, নিয়ন্ত্রণ ও মিসো তোমার বেছে নেওয়া থিম অনুসরণ করে।",
        "0–1440 minutes. Set 0 for no time boundary." to "০–১৪৪০ মিনিট। সময়ের সীমা না রাখতে ০ লেখো।",
        "Changes to your regular goal apply tomorrow while this allowance is active." to "এই অতিরিক্ত সীমা চালু থাকলে নিয়মিত লক্ষ্যের পরিবর্তন আগামীকাল প্রযোজ্য হবে।",
        "Available after reaching your daily limit. Your history stays intact. Once these 30 reels are used, the boundary stays in place until tomorrow." to "দৈনিক সীমায় পৌঁছালে পাওয়া যাবে। ইতিহাস অক্ষত থাকবে। অতিরিক্ত ৩০টি রিলস শেষ হলে আগামীকাল পর্যন্ত সীমা থাকবে।",
        "Changes to your regular goal apply tomorrow while this allowance is active. Your time boundary still applies." to "অতিরিক্ত সীমা চালু থাকলে নিয়মিত লক্ষ্যের পরিবর্তন আগামীকাল প্রযোজ্য হবে। সময়ের সীমা বহাল থাকবে।",
        "Hear Miso automatically on first launch." to "প্রথমবার অ্যাপ খোলায় মিসোর কথা শুনবে।", "Miso speaks near, at and beyond your goal." to "লক্ষ্যের কাছে, লক্ষ্যে ও তা পেরোলে মিসো কথা বলে।",
        "A red warning from 80%. At your limit, counting stops." to "৮০% থেকে লাল সতর্কতা। সীমায় পৌঁছালে গণনা বন্ধ।",
        "Distinct sounds near and at your boundary." to "সীমার কাছে ও সীমায় পৌঁছালে আলাদা শব্দ।", "A countdown and chime when your break ends." to "বিরতির সময়ের হিসাব ও শেষে শব্দ।",
        "Android notifications are enabled. Sound follows channel settings and Do Not Disturb." to "অ্যান্ড্রয়েডের বিজ্ঞপ্তি চালু আছে। শব্দ চ্যানেলের সেটিংস ও Do Not Disturb অনুসরণ করে।",
        "Up to 30 days of local activity." to "ফোনে সর্বোচ্চ ৩০ দিনের ব্যবহার।", "No cloud database or account is used." to "ক্লাউড ডেটাবেস বা অ্যাকাউন্ট ব্যবহার হয় না।",
        "Storage size includes your local settings and activity file." to "সংরক্ষণের আকারে ফোনের সেটিংস ও ব্যবহার ফাইল অন্তর্ভুক্ত।",
        "Delete recorded activity. Your settings and daily goal stay in place." to "রেকর্ড করা ব্যবহার মুছবে। সেটিংস ও দৈনিক লক্ষ্য অপরিবর্তিত থাকবে।",
        "Review Android’s Accessibility service switch for AntiReel. The optional shortcut alone does not start tracking. If access is restricted, review Accessibility help before changing device settings." to "অ্যান্ড্রয়েডে অ্যান্টিরিলের অ্যাক্সেসিবিলিটি পরিষেবার সুইচ দেখো। শুধু ঐচ্ছিক শর্টকাট চালালে ট্র্যাকিং শুরু হয় না। অনুমতি সীমিত থাকলে ফোনের সেটিংস বদলানোর আগে অ্যাক্সেসিবিলিটির সহায়তা দেখো।",
        "No account, advertising or internet permission." to "অ্যাকাউন্ট, বিজ্ঞাপন বা ইন্টারনেটের অনুমতি নেই।", "Accessibility access can be revoked at any time." to "অ্যাক্সেসিবিলিটির অনুমতি যেকোনো সময় বন্ধ করা যায়।",
        "AntiReel saves your local profile and onboarding choices, settings, daily reel counts, recorded video-screen duration and connection status." to "অ্যান্টিরিল ফোনে তোমার পরিচিতি ও শুরুর পছন্দ, সেটিংস, দৈনিক রিলস, ভিডিও পর্দার সময় ও সংযোগের অবস্থা রাখে।",
        "It never saves captions, messages, screenshots or video. Only your selected platforms are inspected." to "ক্যাপশন, বার্তা, স্ক্রিনশট বা ভিডিও সংরক্ষণ করে না। শুধু বেছে নেওয়া অ্যাপ দেখা হয়।",
        "Accessibility requires your approval in Android Settings. The break timer uses Android alarms; system battery restrictions can delay its notification." to "অ্যাক্সেসিবিলিটির জন্য অ্যান্ড্রয়েড সেটিংসে তোমার অনুমোদন দরকার। বিরতির টাইমার অ্যান্ড্রয়েডের অ্যালার্ম ব্যবহার করে; ব্যাটারির নিয়ন্ত্রণে বিজ্ঞপ্তি দেরি হতে পারে।",
        "Permissions, notifications and system settings" to "অনুমতি, বিজ্ঞপ্তি ও সিস্টেমের সেটিংস", "min" to "মিনিট", "reels" to "রিলস"
    )
}
