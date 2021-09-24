package st.slex.library

import android.content.Context
import android.telephony.TelephonyManager
import android.text.InputType
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import java.util.*

open class PhoneTextInputEditText : TextInputEditText {

    private val phoneNumberUtil = PhoneNumberUtilInstanceProvider.get()

    var initialCountryCode = -1
        private set

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        setInitialCountryCode(attrs)
        addTextChangedListener(InternationalPhoneNumberFormattingTextWatcher())
        inputType = InputType.TYPE_CLASS_PHONE
    }

    private fun setInitialCountryCode(attrs: AttributeSet?) {
        attrs?.let {
            with(
                context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.PhoneTextInputEditText,
                    0,
                    0
                )
            ) {
                try {
                    initialCountryCode =
                        getInt(R.styleable.PhoneTextInputEditText_phone_initialCountryCode, -1)
                    val initialRegionCode =
                        getString(R.styleable.PhoneTextInputEditText_phone_initialRegionCode)
                            ?.uppercase(Locale.ROOT)

                    initialCountryCode = when {
                        initialCountryCode != -1 -> initialCountryCode
                        initialRegionCode != null -> phoneNumberUtil.getCountryCodeForRegion(
                            initialRegionCode
                        )
                        else -> resolveInitialCountryCode()
                    }
                } finally {
                    recycle()
                }
            }
        }
        setCountryCode(initialCountryCode)
    }

    private fun resolveInitialCountryCode() =
        phoneNumberUtil.getCountryCodeForRegion(networkCountry()).takeIf { it != 0 }
            ?: phoneNumberUtil.getCountryCodeForRegion(Locale.getDefault().country)

    private fun networkCountry() =
        (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso.uppercase(
            Locale.ROOT
        )

    fun setCountryCode(countryCode: Int) {
        setText("")
        val text = "+$countryCode"
        setText(text)
        setSelection(text.length)
    }

    fun setRegionCode(regionCode: String) {
        setCountryCode(phoneNumberUtil.getCountryCodeForRegion(regionCode.uppercase(Locale.ROOT)))
    }

    fun isTextValidInternationalPhoneNumber() =
        runCatching {
            phoneNumberUtil.isValidNumber(
                phoneNumberUtil.parse(
                    text,
                    null
                )
            )
        }.getOrNull() == true

    fun setInternationalPhoneNumber(phoneNumber: String) {
        setText("")
        setText(phoneNumber)
        setSelection(text!!.length)
    }
}