package com.rafi.antireelai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Fixed, public destinations only. Never attach profile, usage, logs or device IDs. */
internal object ExternalLinks {
    fun documentIntent(document:LegalDocument)=Intent(Intent.ACTION_VIEW,Uri.parse(document.url)).addCategory(Intent.CATEGORY_BROWSABLE)
    fun supportIntent()=Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:$SUPPORT_EMAIL?subject="+Uri.encode("AntiReel AI support")))
    fun document(context:Context,document:LegalDocument)=open(context,documentIntent(document),"No browser is available. You can read this document here or visit ${document.url}.")
    fun support(context:Context)=open(context,supportIntent(),"No email app is available. Contact $SUPPORT_EMAIL.")
    private fun open(context:Context,intent:Intent,message:String):Boolean {
        if(context !is Activity)intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching{context.startActivity(intent);true}.getOrElse {
            Toast.makeText(context,message,Toast.LENGTH_LONG).show();false
        }
    }
}
