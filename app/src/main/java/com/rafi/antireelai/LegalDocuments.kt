package com.rafi.antireelai

internal enum class LegalDocument(val slug:String,val title:String,val glyph:Int) {
    PRIVACY("privacy","Privacy Policy",12),
    TERMS("terms","Terms of Service",11),
    SECURITY("security","Security & Permissions",9),
    SUPPORT("support","Help & Support",11);
    val url:String get()="https://legal.imrafi.bond/$slug"
    val asset:String get()="legal/$slug.json"
}
internal const val SUPPORT_EMAIL="hello@imrafi.com"
