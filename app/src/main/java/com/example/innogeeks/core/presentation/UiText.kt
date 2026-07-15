package com.example.innogeeks.core.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

// A "ticket" describing text to display, WITHOUT resolving it yet. Lets a ViewModel
// decide what to show (holding only an Int id, no Context) while the UI resolves it
// to a real String at the last moment. This is how we keep Context out of ViewModels.
sealed interface UiText {

    // Text that is already a real string (e.g. a message from the server). No lookup.
    data class DynamicString(val value : String) : UiText

    // A reference into res/strings.xml by id (+ optional format args). Holds only an
    // Int, so it can be created anywhere — including a Context-free ViewModel.
    // Plain `class` (not data class): Array has no structural equals, so data-class
    // equality would be misleading here.
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