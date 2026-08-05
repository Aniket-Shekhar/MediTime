package com.levi.meditime.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levi.meditime.presentation.theme.Radius

@Composable
fun OtpInputField(
    otpText: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    isError: Boolean = false,
    autoFocus: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = otpText,
        onValueChange = {
            if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(otpLength) { index ->
                    OtpBox(
                        index = index,
                        text = otpText,
                        isError = isError
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpBox(
    index: Int,
    text: String,
    isError: Boolean
) {
    val char = if (index < text.length) text[index].toString() else ""
    val isFocused = text.length == index
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val isAccessibilityMode = com.levi.meditime.presentation.theme.LocalAccessibilityMode.current
    Box(
        modifier = Modifier
            .size(if (isAccessibilityMode) 56.dp else 48.dp)
            .border(
                width = if (isFocused || isError) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(Radius.small)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isAccessibilityMode) 24.sp else 20.sp,
                textAlign = TextAlign.Center
            ),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
