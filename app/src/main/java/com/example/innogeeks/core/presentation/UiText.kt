package com.example.innogeeks.core.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

// Describes text to display without resolving it, so ViewModels never need a Context.
sealed interface UiText {

    // Text that is already a real string (e.g. a message from the server). No lookup.
    data class DynamicString(val value : String) : UiText

    // A strings.xml reference by id plus format args; a plain class because Array has no structural equals.
    class StringResource(
        @StringRes val id: Int,
        val args : Array<Any> = arrayOf()
    ) : UiText

    // Resolve inside Compose — stringResource grabs the Context implicitly. The common one.
    @Composable
    fun asString() : String{
        return when(this){
            is UiText.DynamicString -> this.value
            is UiText.StringResource -> stringResource(this.id, *this.args)
        }
    }

    // Resolve outside Compose (Toast/notification) where you must pass a Context in.
    // *this.args spreads the array into the vararg formatArgs getString expects.
    fun asString(context: Context): String{
        return when(this){
            is UiText.DynamicString -> this.value
            is UiText.StringResource -> context.getString(this.id,*this.args)
        }
    }
}