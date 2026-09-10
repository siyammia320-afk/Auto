package com.example.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CountryProfile(
    val iso: String,
    val flag: String,
    val countryName: String,
    val timeZoneId: String,
    val acceptLanguage: String,
    val firstNames: List<String>,
    val lastNames: List<String>
)

object TimezoneHelper {

    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    private val countryTimezoneMap = mapOf(
        "BD" to "Asia/Dhaka",
        "CI" to "Africa/Abidjan",
        "US" to "America/New_York",
        "IN" to "Asia/Kolkata",
        "PK" to "Asia/Karachi",
        "GB" to "Europe/London",
        "FR" to "Europe/Paris",
        "DE" to "Europe/Berlin",
        "NG" to "Africa/Lagos",
        "AF" to "Asia/Kabul",
        "MA" to "Africa/Casablanca",
        "DZ" to "Africa/Algiers",
        "AE" to "Asia/Dubai",
        "SA" to "Asia/Riyadh",
        "CA" to "America/Toronto",
        "MY" to "Asia/Kuala_Lumpur",
        "ID" to "Asia/Jakarta",
        "PH" to "Asia/Manila",
        "MG" to "Indian/Antananarivo",
        "AM" to "Asia/Yerevan",
        "TG" to "Africa/Lome",
        "UA" to "Europe/Kyiv",
        "RU" to "Europe/Moscow",
        "TR" to "Europe/Istanbul",
        "EG" to "Africa/Cairo",
        "ZA" to "Africa/Johannesburg",
        "KE" to "Africa/Nairobi",
        "GH" to "Africa/Accra",
        "VN" to "Asia/Ho_Chi_Minh",
        "TH" to "Asia/Bangkok",
        "MM" to "Asia/Yangon",
        "BR" to "America/Sao_Paulo",
        "MX" to "America/Mexico_City",
        "CO" to "America/Bogota",
        "ES" to "Europe/Madrid",
        "IT" to "Europe/Rome",
        "NL" to "Europe/Amsterdam",
        "PL" to "Europe/Warsaw"
    )

    private val countryNamesMap = mapOf(
        "880" to Pair("🇧🇩", "Bangladesh"),
        "1" to Pair("🇺🇸", "USA/Canada"),
        "225" to Pair("🇨🇮", "Ivory Coast"),
        "44" to Pair("🇬🇧", "United Kingdom"),
        "91" to Pair("🇮🇳", "India"),
        "92" to Pair("🇵🇰", "Pakistan"),
        "234" to Pair("🇳🇬", "Nigeria"),
        "33" to Pair("🇫🇷", "France"),
        "49" to Pair("🇩🇪", "Germany"),
        "93" to Pair("🇦🇫", "Afghanistan"),
        "212" to Pair("🇲🇦", "Morocco"),
        "213" to Pair("🇩🇿", "Algeria")
    )

    private val internationalFirstNames = listOf(
        "Alex", "David", "John", "Michael", "Daniel", "James", "Gabriel", "Lucas", "Paul", "Thomas",
        "Marco", "Robert", "Chris", "Kevin", "Leo", "Samuel", "Eric", "Brian", "Denis", "Victor"
    )
    private val internationalLastNames = listOf(
        "Smith", "Miller", "Taylor", "Wilson", "Brown", "Martin", "Anderson", "Thomas", "Moore", "Jackson",
        "Harris", "Clark", "Lewis", "Robinson", "Walker", "Young", "Allen", "King", "Wright", "Scott"
    )

    fun cleanDigits(number: String): String {
        return number.replace(Regex("\\D"), "")
    }

    fun isoToEmoji(isoCode: String): String {
        if (isoCode.length != 2) return "📱"
        val upper = isoCode.uppercase(Locale.US)
        val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    /**
     * Automatic country ISO detection using Google libphonenumber, just like the Python reference.
     */
    fun detectCountryIso(phoneNumber: String): String {
        val digits = cleanDigits(phoneNumber)
        if (digits.isEmpty()) return "US"

        // Local Bangladesh pattern
        if (digits.startsWith("880") || (digits.startsWith("01") && digits.length == 11)) {
            return "BD"
        }
        if (digits.startsWith("225")) return "CI"
        if (digits.startsWith("44")) return "GB"
        if (digits.startsWith("91")) return "IN"
        if (digits.startsWith("92")) return "PK"
        if (digits.startsWith("234")) return "NG"

        // Try libphonenumber parse
        try {
            val parseTarget = if (phoneNumber.trim().startsWith("+")) phoneNumber.trim() else "+$digits"
            val parsed: PhoneNumber = phoneUtil.parse(parseTarget, null)
            val region = phoneUtil.getRegionCodeForNumber(parsed)
            if (!region.isNullOrBlank() && region.length == 2 && region != "ZZ") {
                return region.uppercase(Locale.US)
            }
        } catch (e: Exception) {
            // Ignore parse exception
        }

        // Check if starts with 1 for US
        if (digits.startsWith("1")) return "US"

        return "US"
    }

    /**
     * Returns country flag and country name.
     * If the country cannot be reliably detected, returns Pair("📱", "") so NO wrong country is shown.
     */
    fun getCountryInfo(rangeCode: String): Pair<String, String> {
        val digits = cleanDigits(rangeCode)
        if (digits.isEmpty()) return Pair("📱", "")

        // Bangladesh local format
        if (digits.startsWith("88") || digits.startsWith("01")) {
            return Pair("🇧🇩", "Bangladesh")
        }

        // Check common map first
        for (len in 4 downTo 1) {
            if (digits.length >= len) {
                val prefix = digits.substring(0, len)
                countryNamesMap[prefix]?.let { return it }
            }
        }

        // Use libphonenumber to automatically resolve the country
        try {
            val parseTarget = "+$digits"
            val parsed = phoneUtil.parse(parseTarget, null)
            val region = phoneUtil.getRegionCodeForNumber(parsed)
            if (!region.isNullOrBlank() && region.length == 2 && region != "ZZ") {
                val upperIso = region.uppercase(Locale.US)
                val countryName = Locale("", upperIso).getDisplayCountry(Locale.ENGLISH)
                val flag = isoToEmoji(upperIso)
                if (countryName.isNotBlank()) {
                    return Pair(flag, countryName)
                }
            }
        } catch (e: Exception) {
            // Fall through
        }

        // If unknown, return phone emoji and empty country name to avoid showing fake country!
        return Pair("📱", "")
    }

    fun getTimezoneForIso(isoCode: String): String {
        val upperIso = isoCode.uppercase(Locale.US)
        // Check Android ICU TimeZone for accurate timezone of any country in the world
        try {
            val available = android.icu.util.TimeZone.getAvailableIDs(upperIso)
            if (!available.isNullOrEmpty() && available[0].isNotBlank()) {
                return available[0]
            }
        } catch (e: Throwable) {
            // Fallback
        }
        return countryTimezoneMap[upperIso] ?: "UTC"
    }

    fun getProfile(phoneNumberOrRange: String): CountryProfile {
        val iso = detectCountryIso(phoneNumberOrRange)
        val (flag, countryName) = getCountryInfo(phoneNumberOrRange)
        val resolvedCountryName = if (countryName.isNotEmpty()) countryName else Locale("", iso).getDisplayCountry(Locale.ENGLISH)
        val resolvedFlag = if (flag != "📱") flag else isoToEmoji(iso)
        val tzId = getTimezoneForIso(iso)

        val acceptLang = when (iso) {
            "BD" -> "bn-BD,bn;q=0.9,en-US;q=0.8,en;q=0.7"
            "CI" -> "fr-CI,fr-FR;q=0.9,fr;q=0.8,en-US;q=0.7"
            "FR" -> "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"
            "UA" -> "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7"
            "IN" -> "en-IN,en-GB;q=0.9,hi-IN;q=0.8,hi;q=0.7"
            "PK" -> "ur-PK,ur;q=0.9,en-US;q=0.8,en;q=0.7"
            "NG" -> "en-NG,en-GB;q=0.9,en;q=0.8"
            "DE" -> "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7"
            "RU" -> "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"
            "TR" -> "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7"
            else -> "en-US,en;q=0.9"
        }

        val firstNames = when (iso) {
            "BD" -> listOf("Tanvir", "Araft", "Siam", "Rahim", "Karim", "Rakib", "Hasan", "Nayeem", "Shakil", "Mahmud", "Foysal", "Sakib", "Tamim", "Riad", "Emon")
            "CI" -> listOf("Kouame", "Koffi", "Konan", "Jean", "Yao", "Mamadou", "Ibrahim", "Abdoulaye", "Bakary", "Seydou", "Adama", "Oumar", "Amadou")
            else -> internationalFirstNames
        }

        val lastNames = when (iso) {
            "BD" -> listOf("Bhai", "Khan", "Ahmed", "Chowdhury", "Hossain", "Islam", "Sheikh", "Rahman", "Miah", "Uddin", "Ali", "Hasan")
            "CI" -> listOf("Traore", "Kouassi", "Diallo", "Bamba", "Ouattara", "Coulibaly", "Bakayoko", "Toure", "Kone", "Cisse", "Diarra")
            else -> internationalLastNames
        }

        return CountryProfile(
            iso = iso,
            flag = resolvedFlag,
            countryName = resolvedCountryName,
            timeZoneId = tzId,
            acceptLanguage = acceptLang,
            firstNames = firstNames,
            lastNames = lastNames
        )
    }

    fun getCountryTime(phoneNumber: String): Pair<String, String> {
        val iso = detectCountryIso(phoneNumber)
        val tzName = getTimezoneForIso(iso)
        return try {
            val tz = TimeZone.getTimeZone(tzName)
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                timeZone = tz
            }
            Pair(sdf.format(Date()), tzName)
        } catch (e: Exception) {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            Pair(sdf.format(Date()), "UTC")
        }
    }

    fun extractOtpFromText(text: String): String {
        val cleanText = text.replace("-", "").replace(" ", "")
        val patterns = listOf(
            Regex("\\b(\\d{8})\\b"),
            Regex("\\b(\\d{7})\\b"),
            Regex("\\b(\\d{6})\\b"),
            Regex("\\b(\\d{5})\\b"),
            Regex("\\b(\\d{4})\\b"),
            Regex("\\b(\\d{3})\\b"),
            Regex("code[:\\s]*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("OTP[:\\s]*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("(\\d+)")
        )
        for (p in patterns) {
            val match = p.find(cleanText)
            if (match != null) {
                val grp = match.groupValues.getOrNull(1) ?: match.value
                if (grp.length >= 3) {
                    return grp
                }
            }
        }
        return "N/A"
    }
}
