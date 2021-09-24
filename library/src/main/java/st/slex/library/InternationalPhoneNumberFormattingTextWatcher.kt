package st.slex.library

import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import java.util.*

class InternationalPhoneNumberFormattingTextWatcher @JvmOverloads constructor(
    regionCode: String = Locale.getDefault().country
) : TextWatcher {

    private val formatter = PhoneNumberUtilInstanceProvider.get().getAsYouTypeFormatter(regionCode)

    private var selfChange = false
    private var stopFormatting = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (selfChange || stopFormatting) return
        if (count > 0 && hasSeparator(s, start, count)) stopFormatting()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (selfChange || stopFormatting) return
        if (count > 0 && hasSeparator(s, start, count)) stopFormatting()
    }

    @Synchronized
    override fun afterTextChanged(s: Editable) {
        if (selfChange) return

        if (!s.startsWith("+")) {
            selfChange = true
            s.replace(0, s.length, "+${s.toString().replaceFirst("+", "")}")
            Selection.setSelection(s, s.length)
            selfChange = false
        }

        if (stopFormatting) {
            stopFormatting = s.isNotEmpty() && s.toString() != "+"
            return
        }

        val formatted = reformat(s, Selection.getSelectionEnd(s))
        formatted?.let {
            val rememberedPos: Int = formatter.rememberedPosition
            selfChange = true
            s.replace(0, s.length, it)
            if (it == s.toString()) Selection.setSelection(s, rememberedPos)
            selfChange = false
        }

        TtsSpanHelper.addTtsSpan(s, 0, s.length)
    }

    private fun reformat(s: CharSequence, cursor: Int): String? {
        val curIndex = cursor - 1
        var formatted: String? = null
        formatter.clear()
        var lastNonSeparator = 0.toChar()
        var hasCursor = false
        val len = s.length
        for (i in 0 until len) {
            val c = s[i]
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator.code != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                    hasCursor = false
                }
                lastNonSeparator = c
            }
            if (i == curIndex) hasCursor = true
        }
        if (lastNonSeparator.code != 0) formatted =
            getFormattedNumber(lastNonSeparator, hasCursor)
        return formatted
    }

    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean) =
        if (hasCursor) formatter.inputDigitAndRememberPosition(lastNonSeparator) else formatter.inputDigit(
            lastNonSeparator
        )

    private fun stopFormatting() {
        stopFormatting = true
        formatter.clear()
    }

    private fun hasSeparator(s: CharSequence, start: Int, count: Int): Boolean {
        for (i in start until start + count) {
            val c = s[i]
            if (!PhoneNumberUtils.isNonSeparator(c)) return true
        }
        return false
    }
}