package com.levi.meditime.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.levi.meditime.domain.model.Country
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

import androidx.compose.ui.res.stringResource
import com.levi.meditime.R

@Composable
fun InternationalPhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    selectedCountry: Country,
    onCountryClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val context = LocalContext.current
    val phoneUtil = remember { PhoneNumberUtil.createInstance(context) }
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow digits
            if (newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        modifier = modifier,
        label = { Text(stringResource(R.string.phone_number)) },
        leadingIcon = {
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled) { onCountryClick() }
                    .padding(start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedCountry.flag, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(4.dp))
                Text(selectedCountry.dialCode, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        isError = isError,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

fun validatePhoneNumber(context: android.content.Context, phoneNumber: String, countryCode: String): Boolean {
    return try {
        val phoneUtil = PhoneNumberUtil.createInstance(context)
        val numberProto = phoneUtil.parse(phoneNumber, countryCode)
        phoneUtil.isValidNumber(numberProto)
    } catch (e: Exception) {
        false
    }
}
