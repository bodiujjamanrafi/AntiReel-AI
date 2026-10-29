package com.rafi.antireelai

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/** Observes the in-memory preference write immediately, independently of page-level refresh batching. */
@Composable private fun booleanPreference(prefs:SharedPreferences,key:String,default:Boolean):MutableState<Boolean> {
    val value=remember(prefs,key){mutableStateOf(prefs.getBoolean(key,default))}
    DisposableEffect(prefs,key) {
        val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,changed->if(changed==key || changed==null)value.value=prefs.getBoolean(key,default)}
        prefs.registerOnSharedPreferenceChangeListener(listener)
        value.value=prefs.getBoolean(key,default)
        onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}
    }
    return value
}
@Composable internal fun SettingsPreferenceSwitch(store:TrackerStore,key:String,title:String,detail:String,glyph:Int,modifier:Modifier=Modifier,default:Boolean=true,inverted:Boolean=false,onChange:(Boolean)->Unit) {
    val value=booleanPreference(store.prefs,key,default)
    SettingsSwitch(title,detail,glyph,if(inverted)!value.value else value.value,modifier) {
        // Read the current state inside the event, including multiple taps before the next frame.
        val next=if(inverted)value.value else !value.value
        value.value=if(inverted)!next else next
        onChange(next)
        value.value=store.prefs.getBoolean(key,default)
    }
}
