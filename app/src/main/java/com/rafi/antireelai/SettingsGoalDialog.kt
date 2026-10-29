package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Retains the existing goal editor callbacks; other screens keep their original dialog. */
@Composable internal fun SettingsGoalDialog(initial:Int,onDismiss:()->Unit,onSave:(Int)->Unit) {
    var input by remember(initial){mutableStateOf(initial.toString())}
    val goal=parseDailyGoal(input)
    SettingsTheme {
        AlertDialog(onDismissRequest=onDismiss,
            title={UiText("Your daily boundary",fontSize=22.sp)},
            text={Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                SettingsText("Choose any positive whole number of reels across your selected apps.")
                OutlinedTextField(input,{if(it.length<=10 && it.all{c->c in '0'..'9'})input=it},label={UiText("Reels per day")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,isError=goal==null,modifier=Modifier.fillMaxWidth(),supportingText={if(goal==null)SettingsText("Enter a positive whole number.")})
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    listOf(20,30,50,100).forEach {value->
                        OutlinedButton(onClick={input=value.toString()},modifier=Modifier.weight(1f).heightIn(min=48.dp),contentPadding=PaddingValues(0.dp)){UiText("$value",fontSize=14.sp)}
                    }
                }
            }},
            confirmButton={TextButton(onClick={goal?.let(onSave)},enabled=goal!=null){UiText("Save daily goal")}},
            dismissButton={TextButton(onClick=onDismiss){UiText("Cancel")}})
    }
}
