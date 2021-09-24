import android.text.Editable
import android.text.TextWatcher
import st.slex.library.PhoneNumberUtilInstanceProvider
import st.slex.library.PhoneTextInputEditText

object PhoneHelper {
    const val EMOJI_COLOR = 0xff000000.toInt()

    private const val UNKNOWN_REGION = "ZZ"

    private val phoneNumberUtil = PhoneNumberUtilInstanceProvider.get()

    fun watchPhoneNumber(editText: PhoneTextInputEditText, onCountryChanged: (String) -> Unit) {
        var currentCountryCode = editText.initialCountryCode
        runCatching {
            currentCountryCode = phoneNumberUtil.parse(editText.text, null).countryCode
        }

        onCountryChanged(
            regionCodeToEmoji(
                phoneNumberUtil.getRegionCodeForCountryCode(
                    currentCountryCode
                )
            )
        )

        editText.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.contains(" ")) {
                        val countryCode =
                            runCatching { s.substring(1, s.indexOf(" ")).toInt() }.getOrNull()
                                ?: return
                        if (currentCountryCode == countryCode) return

                        val regionCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode)

                        if (regionCode == UNKNOWN_REGION) return

                        currentCountryCode = countryCode
                        onCountryChanged(regionCodeToEmoji(regionCode))
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) =
                    Unit
            }
        )
    }

    private fun regionCodeToEmoji(regionCode: String): String {
        val firstLetter = Character.codePointAt(regionCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(regionCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}