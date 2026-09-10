package com.example.data.api

import android.util.Base64
import com.example.data.model.ActiveNumber
import com.example.data.model.ConnectionInfo
import com.example.data.model.CreatedAccount
import com.example.data.model.LiveService
import com.example.data.model.OtpItem
import com.example.util.TimezoneHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ApiClient {

    private const val API_BASE_URL = "https://api.2oo9.cloud/MXS47FLFX0U/tnevs/@public/api"
    private const val API_KEY = "MFSCNKJSFBI"

    private val baseClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchLiveServices(): List<LiveService> = withContext(Dispatchers.IO) {
        val url = "$API_BASE_URL/liveaccess"
        val request = Request.Builder()
            .url(url)
            .addHeader("mauthapi", API_KEY)
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        try {
            baseClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    if (json.optJSONObject("meta")?.optInt("code") == 200) {
                        val servicesArray = json.optJSONObject("data")?.optJSONArray("services")
                        if (servicesArray != null && servicesArray.length() > 0) {
                            val list = mutableListOf<LiveService>()
                            for (i in 0 until servicesArray.length()) {
                                val obj = servicesArray.getJSONObject(i)
                                val sid = obj.optString("sid", "Service")
                                val rangesArray = obj.optJSONArray("ranges")
                                val ranges = mutableListOf<String>()
                                if (rangesArray != null) {
                                    for (j in 0 until rangesArray.length()) {
                                        ranges.add(rangesArray.getString(j))
                                    }
                                }
                                list.add(LiveService(sid, ranges))
                            }
                            return@withContext list
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // No demo fallback; return empty list if no live services are returned by API
        emptyList()
    }

    suspend fun fetchNumber(rangeCode: String): String? = withContext(Dispatchers.IO) {
        val rid = rangeCode.replace("XXX", "").replace("X", "").trim()
        if (rid.isEmpty()) return@withContext null
        val url = "$API_BASE_URL/getnum"
        val jsonPayload = JSONObject().apply { put("rid", rid) }.toString()
        val body = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("mauthapi", API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            baseClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val json = JSONObject(respBody)
                    if (json.optJSONObject("meta")?.optInt("code") == 200) {
                        val numData = json.optJSONObject("data")
                        if (numData != null) {
                            val fullNumber = numData.optString("full_number").ifEmpty {
                                numData.optString("no_plus_number")
                            }
                            if (fullNumber.isNotEmpty()) {
                                return@withContext fullNumber.replace("+", "").trim()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun checkOtp(activeNumbers: Map<String, ActiveNumber>): List<OtpItem> = withContext(Dispatchers.IO) {
        val url = "$API_BASE_URL/success-otp"
        val request = Request.Builder()
            .url(url)
            .addHeader("mauthapi", API_KEY)
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        val results = mutableListOf<OtpItem>()
        try {
            baseClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val json = JSONObject(respBody)
                    if (json.optJSONObject("meta")?.optInt("code") == 200) {
                        val otpsArray = json.optJSONObject("data")?.optJSONArray("otps")
                        if (otpsArray != null) {
                            val addedKeys = mutableSetOf<String>()
                            val addedOtps = mutableSetOf<String>()
                            for (phone in activeNumbers.keys) {
                                val cleanPhone = TimezoneHelper.cleanDigits(phone)
                                for (i in 0 until otpsArray.length()) {
                                    val item = otpsArray.getJSONObject(i)
                                    val otpNumber = TimezoneHelper.cleanDigits(item.optString("number", ""))
                                    val isMatch = cleanPhone.isNotEmpty() && (cleanPhone == otpNumber ||
                                        (cleanPhone.length >= 8 && otpNumber.length >= 8 && (cleanPhone.endsWith(otpNumber) || otpNumber.endsWith(cleanPhone))))
                                    if (isMatch) {
                                        val message = item.optString("message", "")
                                        val otpCode = TimezoneHelper.extractOtpFromText(message)
                                        if (otpCode != "N/A") {
                                            val key = "${cleanPhone}_$otpCode"
                                            if (!addedKeys.contains(key) && !addedOtps.contains(otpCode)) {
                                                addedKeys.add(key)
                                                addedOtps.add(otpCode)
                                                results.add(
                                                    OtpItem(
                                                        phone = phone,
                                                        otp = otpCode,
                                                        message = message,
                                                        service = activeNumbers[phone]?.service ?: "SMS"
                                                    )
                                                )
                                            }
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }

    private fun generateRandomToken(length: Int = 24): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun resolveProxyCode(phoneNumber: String, masterProxy: String): String {
        if (masterProxy.isBlank()) return "Direct (No Proxy)"
        var dynamicProxy = masterProxy.trim()
        val isoCode = TimezoneHelper.detectCountryIso(phoneNumber).uppercase()

        // Replace all common dynamic proxy patterns with the country ISO code of the phone number
        dynamicProxy = dynamicProxy
            .replace(Regex("-region-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "-region-$isoCode")
            .replace(Regex("_region-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "_region-$isoCode")
            .replace(Regex("-country-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "-country-$isoCode")
            .replace(Regex("_country-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "_country-$isoCode")
            .replace(Regex("-zone-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "-zone-$isoCode")
            .replace(Regex("-cc-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "-cc-$isoCode")
            .replace(Regex("(?<=[^A-Za-z0-9]|^)country-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "country-$isoCode")
            .replace(Regex("(?<=[^A-Za-z0-9]|^)region-[A-Za-z0-9]+", RegexOption.IGNORE_CASE), "region-$isoCode")
            .replace(Regex("country=([A-Za-z0-9]+)", RegexOption.IGNORE_CASE), "country=$isoCode")
            .replace(Regex("country_code=([A-Za-z0-9]+)", RegexOption.IGNORE_CASE), "country_code=$isoCode")

        return dynamicProxy
    }

    fun getConnectionInfo(phoneNumber: String, masterProxy: String): ConnectionInfo {
        val profile = TimezoneHelper.getProfile(phoneNumber)
        val (localTimeStr, tzName) = TimezoneHelper.getCountryTime(phoneNumber)
        val regionStr = "${profile.flag} ${profile.countryName} (${profile.iso})"
        val proxyUsed = resolveProxyCode(phoneNumber, masterProxy)

        return ConnectionInfo(
            timezone = "$tzName ($localTimeStr)",
            region = regionStr,
            proxyCode = proxyUsed
        )
    }

    private fun buildOkHttpClientWithProxy(phoneNumber: String, masterProxy: String): OkHttpClient {
        val dynamicProxy = resolveProxyCode(phoneNumber, masterProxy)
        if (dynamicProxy == "Direct (No Proxy)") return baseClient

        return try {
            var cleanProxy = dynamicProxy.removePrefix("http://").removePrefix("https://").removePrefix("socks5://").removePrefix("socks4://")
            var user: String? = null
            var pass: String? = null

            if (cleanProxy.contains("@")) {
                val parts = cleanProxy.split("@", limit = 2)
                val creds = parts[0].split(":", limit = 2)
                user = creds.getOrNull(0)
                pass = creds.getOrNull(1)
                cleanProxy = parts[1]
            }

            val hostPort = cleanProxy.split(":", limit = 2)
            val host = hostPort[0]
            val port = hostPort.getOrNull(1)?.toIntOrNull() ?: 8080

            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
            val builder = baseClient.newBuilder()
                .proxy(proxy)
                .connectTimeout(35, TimeUnit.SECONDS)
                .readTimeout(35, TimeUnit.SECONDS)

            if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                builder.proxyAuthenticator(object : Authenticator {
                    override fun authenticate(route: Route?, response: Response): Request? {
                        val credential = Credentials.basic(user, pass)
                        return response.request.newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build()
                    }
                })
            }
            builder.build()
        } catch (e: Exception) {
            baseClient
        }
    }

    suspend fun createFacebookAccount(
        phone: String,
        userPassword: String,
        masterProxy: String
    ): CreatedAccount = withContext(Dispatchers.IO) {
        val connInfo = getConnectionInfo(phone, masterProxy)
        var cleanTarget = TimezoneHelper.cleanDigits(phone)
        if (cleanTarget.startsWith("01") && cleanTarget.length == 11) {
            cleanTarget = "88$cleanTarget"
        }

        val (localTimeStr, tzName) = TimezoneHelper.getCountryTime(phone)
        val currentEpoch = System.currentTimeMillis() / 1000L

        // Dynamic mutation token
        val tokenJson = "{\"type\":0,\"creation_time\":$currentEpoch,\"callsite_id\":907924402948058}"
        val tokenB64 = Base64.encodeToString(tokenJson.toByteArray(), Base64.NO_WRAP)

        val regUrl = "https://limited.facebook.com/reg/submit/?privacy_mutation_token=$tokenB64&app_id=103&multi_step_form=1&skip_suma=0&shouldForceMTouch=1"

        val profile = TimezoneHelper.getProfile(phone)
        val fName = profile.firstNames.random()
        val lName = profile.lastNames.random()
        val bMonth = Random.nextInt(1, 13).toString()
        val bDay = Random.nextInt(1, 29).toString()
        val bYear = Random.nextInt(1992, 2003).toString()
        val birthdayStr = "$bDay/$bMonth/$bYear"
        val genderChoice = Random.nextInt(1, 3).toString()

        val freshDatr = generateRandomToken(24)
        val freshLsd = "AdR${generateRandomToken(24)}"
        val regImpressionId = UUID.randomUUID().toString()
        val loggerId = UUID.randomUUID().toString()

        val submitLabel = if (profile.iso == "FR" || profile.iso == "CI") "S’inscrire" else "Sign Up"

        val formBuilder = FormBody.Builder()
            .add("ccp", "2")
            .add("reg_instance", freshDatr)
            .add("submission_request", "true")
            .add("helper", "")
            .add("reg_impression_id", regImpressionId)
            .add("ns", "1")
            .add("zero_header_af_client", "")
            .add("app_id", "103")
            .add("logger_id", loggerId)
            .add("field_names[0]", "firstname")
            .add("firstname", fName)
            .add("lastname", lName)
            .add("field_names[1]", "birthday_wrapper")
            .add("birthday_day", bDay)
            .add("birthday_month", bMonth)
            .add("birthday_year", bYear)
            .add("age_step_input", "")
            .add("did_use_age", "false")
            .add("field_names[2]", "reg_email__")
            .add("reg_email__", cleanTarget)
            .add("field_names[3]", "sex")
            .add("sex", genderChoice)
            .add("preferred_pronoun", "")
            .add("custom_gender", "")
            .add("field_names[4]", "reg_passwd__")
            .add("reg_passwd__", userPassword)
            .add("was_shown_name_suggestions", "false")
            .add("did_use_suggested_name", "false")
            .add("use_custom_gender", "false")
            .add("guid", "")
            .add("pre_form_step", "")
            .add("submit", submitLabel)
            .add("fb_dtsg", "NAfz_j-tS0v0g6o1-nLZtYbZFRuP0And2bmBtZmdEzVSKIUwOOQYWRw:0:0")
            .add("jazoest", "25007")
            .add("lsd", freshLsd)
            .add("__dyn", "1Z3pawlEnwm8_Bg9ppoW5UdE4a2i5U4e0C86u7E39x60zU3ex608ewk9E4W0pKq0FE6S0x81vohw73wGwcq1GwqU2YwbK0oi0zE1jU1soG0hi0Lo6-0Co1kU1UU3jwea")
            .add("__csr", "")
            .add("__hsdp", "")
            .add("__hblp", "")
            .add("__sjsp", "")
            .add("__req", "h")
            .add("__fmt", "1")
            .add("__a", "AYxS8muEVn-ODQJuXD-1E9P-X6DeKx9xXbytgEBqvjCfOeJISIFfc_FCz1RF1WwsVevfy00NfyqQEvWL0eolfWbXRSyETZNyVOI")
            .add("__user", "0")

        val client = buildOkHttpClientWithProxy(phone, masterProxy)

        val request = Request.Builder()
            .url(regUrl)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12; itel S665L Build/SP1A.210812.016) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.7977.64 Mobile Safari/537.36")
            .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("sec-ch-ua-platform", "\"Android\"")
            .addHeader("sec-ch-ua", "\"Chromium\";v=\"152\", \"Not?A_Brand\";v=\"24\", \"Android WebView\";v=\"152\"")
            .addHeader("x-response-format", "JSONStream")
            .addHeader("sec-ch-ua-mobile", "?1")
            .addHeader("x-asbd-id", "359341")
            .addHeader("x-fb-lsd", freshLsd)
            .addHeader("x-requested-with", "XMLHttpRequest")
            .addHeader("origin", "https://limited.facebook.com")
            .addHeader("sec-fetch-site", "same-origin")
            .addHeader("sec-fetch-mode", "cors")
            .addHeader("sec-fetch-dest", "empty")
            .addHeader("referer", "https://limited.facebook.com/reg/?is_two_steps_login=0&cid=103&refsrc=deprecated&soft=hjk")
            .addHeader("accept-language", profile.acceptLanguage)
            .addHeader("priority", "u=1, i")
            .addHeader("Cookie", "datr=$freshDatr")
            .post(formBuilder.build())
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val cookieHeaders = response.headers("Set-Cookie")
                val cookieMap = mutableMapOf<String, String>()
                for (header in cookieHeaders) {
                    val rawCookie = header.split(";").firstOrNull() ?: continue
                    val parts = rawCookie.split("=", limit = 2)
                    if (parts.size == 2) {
                        cookieMap[parts[0].trim()] = parts[1].trim()
                    }
                }

                val cookieStr = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
                var uid = cookieMap["c_user"] ?: ""

                val resText = response.body?.string() ?: ""

                if (uid.isEmpty()) {
                    val patterns = listOf(
                        Regex("\"c_user\"\\s*:\\s*\"(\\d+)\""),
                        Regex("c_user=(\\d+)"),
                        Regex("\"actor_id\"\\s*:\\s*\"([1-9]\\d+)\""),
                        Regex("\"user_id\"\\s*:\\s*\"([1-9]\\d+)\""),
                        Regex("\"account_id\"\\s*:\\s*\"([1-9]\\d+)\"")
                    )
                    for (pattern in patterns) {
                        val match = pattern.find(resText) ?: pattern.find(cookieStr)
                        if (match != null) {
                            val candidate = match.groupValues.getOrNull(1) ?: match.value
                            if (candidate.isNotEmpty()) {
                                uid = candidate
                                break
                            }
                        }
                    }
                }

                if (uid.isNotEmpty()) {
                    val fullCookiePayload = if (!cookieStr.contains("datr=")) {
                        "datr=$freshDatr; $cookieStr"
                    } else {
                        cookieStr
                    }
                    CreatedAccount(
                        success = true,
                        name = "$fName $lName",
                        phone = cleanTarget,
                        uid = uid,
                        password = userPassword,
                        birthday = birthdayStr,
                        time = "$localTimeStr ($tzName)",
                        cookie = fullCookiePayload,
                        timezone = connInfo.timezone,
                        region = connInfo.region,
                        proxyCode = connInfo.proxyCode
                    )
                } else {
                    CreatedAccount(
                        success = false,
                        phone = cleanTarget,
                        timezone = connInfo.timezone,
                        region = connInfo.region,
                        proxyCode = connInfo.proxyCode,
                        error = "c_user পাওয়া যায়নি! অ্যাকাউন্ট তৈরি হয়নি (Checkpoint / Blocked)।"
                    )
                }
            }
        } catch (e: Exception) {
            val connInfoFallback = getConnectionInfo(phone, masterProxy)
            CreatedAccount(
                success = false,
                phone = cleanTarget,
                timezone = connInfoFallback.timezone,
                region = connInfoFallback.region,
                proxyCode = connInfoFallback.proxyCode,
                error = e.localizedMessage ?: "নেটওয়ার্ক ত্রুটি"
            )
        }
    }
}
