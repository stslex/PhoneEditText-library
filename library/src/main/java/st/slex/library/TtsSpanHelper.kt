package st.slex.library

import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.style.TtsSpan

internal object TtsSpanHelper {

    private val phoneNumberUtil = PhoneNumberUtilInstanceProvider.get()

    fun addTtsSpan(s: Spannable, start: Int, endExclusive: Int) = s.setSpan(
        createTtsSpan(s.subSequence(start, endExclusive).toString()),
        start,
        endExclusive,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    private fun createTtsSpan(phoneNumberString: String): TtsSpan {
        val phoneNumber = runCatching { phoneNumberUtil.parse(phoneNumberString, null) }.getOrNull()
        return TtsSpan.TelephoneBuilder().apply {
            phoneNumber?.let {
                if (it.hasCountryCode()) setCountryCode(it.countryCode.toString())
                setNumberParts(it.nationalNumber.toString())
            } ?: setNumberParts(splitAtNonNumerics(phoneNumberString))
        }.build()
    }

    private fun splitAtNonNumerics(number: CharSequence) = StringBuilder(number.length).apply {
        number.asIterable().forEach { append(if (PhoneNumberUtils.is12Key(it)) it else " ") }
    }.toString().replace(" +".toRegex(), " ").trim()
}