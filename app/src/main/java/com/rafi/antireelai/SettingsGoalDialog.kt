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
                DailyGoalField(input,{input=it})
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    listOf(20,30,50,100).forEach {value->
                        OutlinedButton(onClick={input=value.toString()},colors=ButtonDefaults.outlinedButtonColors(containerColor=if(goal==value)AppStyle.AccentSoft else androidx.compose.ui.graphics.Color.Transparent,contentColor=if(goal==value)AppStyle.Accent else AppStyle.Text),border=androidx.compose.foundation.BorderStroke(1.dp,if(goal==value)AppStyle.Accent else AppStyle.Outline),modifier=Modifier.weight(1f).heightIn(min=48.dp),contentPadding=PaddingValues(0.dp)){UiText("$value",fontSize=14.sp)}
                    }
                }
            }},
            confirmButton={TextButton(onClick={goal?.let(onSave)},enabled=goal!=null){UiText("Save daily goal")}},
            dismissButton={TextButton(onClick=onDismiss){UiText("Cancel")}})
    }
}

@Composable internal fun DailyGoalField(input:String,onChange:(String)->Unit) {
    val valid=parseDailyGoal(input)!=null
    OutlinedTextField(input,{onChange(it.take(32))},label={UiText("Reels per day")},
        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,isError=!valid,
        modifier=Modifier.fillMaxWidth(),shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=AppStyle.Muted.copy(alpha=.75f),
            focusedBorderColor=AppStyle.Accent,focusedLabelColor=AppStyle.Accent,
            focusedTextColor=AppStyle.Text,unfocusedTextColor=AppStyle.Text,cursorColor=AppStyle.Accent),
        supportingText={UiText(if(valid)"Enter your own limit" else "Enter a positive whole number.",fontSize=12.sp,color=if(valid)AppStyle.Muted else MaterialTheme.colorScheme.error)})
}
