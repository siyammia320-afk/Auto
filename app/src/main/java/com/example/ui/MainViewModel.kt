package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.model.ActiveNumber
import com.example.data.model.ConnectionInfo
import com.example.data.model.CreatedAccount
import com.example.data.model.LiveService
import com.example.data.model.OtpItem
import com.example.util.TimezoneHelper
import com.example.util.TotpHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppNavScreen {
    HOME,
    GET_NUMBER,
    AUTO_CREATE,
    SET_PASSWORD,
    TWO_FACTOR,
    OTP_MONITOR,
    PROXY_SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("numgo_prefs", Context.MODE_PRIVATE)

    private val _currentScreen = MutableStateFlow(AppNavScreen.HOME)
    val currentScreen: StateFlow<AppNavScreen> = _currentScreen.asStateFlow()

    private val _userPassword = MutableStateFlow(prefs.getString("user_password", "") ?: "")
    val userPassword: StateFlow<String> = _userPassword.asStateFlow()

    private val _masterProxy = MutableStateFlow(prefs.getString("master_proxy", "") ?: "")
    val masterProxy: StateFlow<String> = _masterProxy.asStateFlow()

    private val _liveServices = MutableStateFlow<List<LiveService>>(emptyList())
    val liveServices: StateFlow<List<LiveService>> = _liveServices.asStateFlow()

    private val _selectedService = MutableStateFlow<LiveService?>(null)
    val selectedService: StateFlow<LiveService?> = _selectedService.asStateFlow()

    private val _activeNumbers = MutableStateFlow<Map<String, ActiveNumber>>(emptyMap())
    val activeNumbers: StateFlow<Map<String, ActiveNumber>> = _activeNumbers.asStateFlow()

    private val _otpList = MutableStateFlow<List<OtpItem>>(emptyList())
    val otpList: StateFlow<List<OtpItem>> = _otpList.asStateFlow()

    private val _createdAccounts = MutableStateFlow<List<CreatedAccount>>(emptyList())
    val createdAccounts: StateFlow<List<CreatedAccount>> = _createdAccounts.asStateFlow()

    private val _isAutoCreating = MutableStateFlow(false)
    val isAutoCreating: StateFlow<Boolean> = _isAutoCreating.asStateFlow()

    private val _connectingInfo = MutableStateFlow<ConnectionInfo?>(null)
    val connectingInfo: StateFlow<ConnectionInfo?> = _connectingInfo.asStateFlow()

    private val _autoCreateLogs = MutableStateFlow<List<String>>(emptyList())
    val autoCreateLogs: StateFlow<List<String>> = _autoCreateLogs.asStateFlow()

    private val _isFetchingNumber = MutableStateFlow(false)
    val isFetchingNumber: StateFlow<Boolean> = _isFetchingNumber.asStateFlow()

    private val _latestFetchedNumber = MutableStateFlow<ActiveNumber?>(null)
    val latestFetchedNumber: StateFlow<ActiveNumber?> = _latestFetchedNumber.asStateFlow()

    private val _twoFactorSecret = MutableStateFlow("")
    val twoFactorSecret: StateFlow<String> = _twoFactorSecret.asStateFlow()

    private val _twoFactorCode = MutableStateFlow<String?>(null)
    val twoFactorCode: StateFlow<String?> = _twoFactorCode.asStateFlow()

    private val _twoFactorSecondsLeft = MutableStateFlow(30)
    val twoFactorSecondsLeft: StateFlow<Int> = _twoFactorSecondsLeft.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var otpMonitorJob: Job? = null
    private var totpJob: Job? = null
    private val sentOtpKeys = mutableSetOf<String>()
    private val sentOtpCodes = mutableSetOf<String>()

    init {
        loadSentOtps()
        loadStoredActiveNumbers()
        loadLiveServices()
        startOtpMonitor()
        startTotpTicker()
    }

    private fun loadSentOtps() {
        try {
            val savedCodes = prefs.getStringSet("saved_sent_otp_codes", emptySet()) ?: emptySet()
            sentOtpCodes.addAll(savedCodes)
            val savedKeys = prefs.getStringSet("saved_sent_otp_keys", emptySet()) ?: emptySet()
            sentOtpKeys.addAll(savedKeys)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigateTo(screen: AppNavScreen) {
        _currentScreen.value = screen
    }

    fun setPassword(password: String) {
        _userPassword.value = password
        prefs.edit().putString("user_password", password).apply()
        _snackbarMessage.value = "পাসওয়ার্ড সফলভাবে সংরক্ষিত হয়েছে!"
    }

    fun setMasterProxy(proxy: String) {
        val trimmed = proxy.trim()
        _masterProxy.value = trimmed
        prefs.edit().putString("master_proxy", trimmed).apply()
        _snackbarMessage.value = if (trimmed.isEmpty()) "প্রক্সি মুছে ফেলা হয়েছে" else "প্রক্সি সংরক্ষিত হয়েছে!"
    }

    fun setTwoFactorSecret(secret: String) {
        _twoFactorSecret.value = secret
        updateTotp()
    }

    fun selectService(service: LiveService?) {
        _selectedService.value = service
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun loadLiveServices() {
        viewModelScope.launch {
            val services = ApiClient.fetchLiveServices()
            _liveServices.value = services
            if (_selectedService.value == null && services.isNotEmpty()) {
                _selectedService.value = services.firstOrNull { it.sid.equals("Facebook", ignoreCase = true) } ?: services.first()
            }
        }
    }

    fun requestNumber(rangeCode: String, serviceName: String) {
        viewModelScope.launch {
            _isFetchingNumber.value = true
            val number = ApiClient.fetchNumber(rangeCode)
            _isFetchingNumber.value = false
            if (number != null) {
                val nowTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val active = ActiveNumber(
                    phone = number,
                    service = serviceName,
                    range = rangeCode,
                    timestamp = nowTime
                )
                _latestFetchedNumber.value = active
                val updated = _activeNumbers.value.toMutableMap()
                updated[number] = active
                _activeNumbers.value = updated
                saveActiveNumbers(updated)
                _snackbarMessage.value = "নতুন নাম্বার প্রাপ্ত হয়েছে: $number"
            } else {
                _snackbarMessage.value = "এই রেঞ্জে কোনো নাম্বার পাওয়া যায়নি! অন্য রেঞ্জ চেষ্টা করুন।"
            }
        }
    }

    fun removeActiveNumber(phone: String) {
        val clean = TimezoneHelper.cleanDigits(phone)
        val updated = _activeNumbers.value.toMutableMap()
        val toRemove = updated.keys.filter {
            it == phone || (clean.isNotEmpty() && TimezoneHelper.cleanDigits(it) == clean)
        }
        for (k in toRemove) {
            updated.remove(k)
        }
        _activeNumbers.value = updated
        saveActiveNumbers(updated)
    }

    fun clearAllActiveNumbers() {
        _activeNumbers.value = emptyMap()
        saveActiveNumbers(emptyMap())
    }

    fun startAutoCreate(rangeCode: String, count: Int) {
        val pass = _userPassword.value
        if (pass.length < 6) {
            _snackbarMessage.value = "পাসওয়ার্ড সেট করা নেই! আগে পাসওয়ার্ড সেট করুন।"
            _currentScreen.value = AppNavScreen.SET_PASSWORD
            return
        }

        viewModelScope.launch {
            _isAutoCreating.value = true
            _autoCreateLogs.value = listOf("অটো অ্যাকাউন্ট তৈরি শুরু হচ্ছে ($count টি)... রেঞ্জ: $rangeCode")

            val newCreated = mutableListOf<CreatedAccount>()
            var successCount = 0
            var failedCount = 0

            for (i in 1..count) {
                addLog("[$i/$count] প্যানেল থেকে লাইভ নাম্বার নেওয়া হচ্ছে...")
                val phone = ApiClient.fetchNumber(rangeCode)

                if (phone.isNullOrEmpty()) {
                    addLog("[$i/$count] নাম্বার পেতে ব্যর্থ হয়েছে!")
                    failedCount++
                    continue
                }

                // Add to active numbers
                val nowTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val active = ActiveNumber(phone, "Facebook", rangeCode, nowTime)
                val currentMap = _activeNumbers.value.toMutableMap()
                currentMap[phone] = active
                _activeNumbers.value = currentMap
                saveActiveNumbers(currentMap)

                val connInfo = ApiClient.getConnectionInfo(phone, _masterProxy.value)
                _connectingInfo.value = connInfo

                addLog("[$i/$count] নাম্বার: $phone")
                addLog("🌐 Time Zone: ${connInfo.timezone}")
                addLog("🌍 Region: ${connInfo.region}")
                addLog("🛡️ Proxy Code: ${connInfo.proxyCode}")
                addLog("⏳ অ্যাকাউন্ট তৈরির অনুরোধ পাঠানো হচ্ছে...")

                val result = ApiClient.createFacebookAccount(phone, pass, _masterProxy.value)
                newCreated.add(result)
                _createdAccounts.value = listOf(result) + _createdAccounts.value

                if (result.success && result.uid.isNotEmpty()) {
                    successCount++
                    addLog("✅ [$i/$count] সফল! UID: ${result.uid} (${result.name})")
                } else {
                    failedCount++
                    addLog("❌ [$i/$count] ব্যর্থ: ${result.error ?: "অ্যাকাউন্ট তৈরি হয়নি"}")
                }

                if (i < count) {
                    delay(1500)
                }
            }

            _connectingInfo.value = null
            addLog("🎉 ব্যাচ সমাপ্ত! মোট: $count | সফল: $successCount | ব্যর্থ: $failedCount")
            _isAutoCreating.value = false
        }
    }

    private fun addLog(message: String) {
        _autoCreateLogs.value = _autoCreateLogs.value + message
    }

    private fun startOtpMonitor() {
        otpMonitorJob?.cancel()
        otpMonitorJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val active = _activeNumbers.value
                    if (active.isNotEmpty()) {
                        val results = ApiClient.checkOtp(active)
                        for (item in results) {
                            val cleanPhone = TimezoneHelper.cleanDigits(item.phone)
                            val otpCode = item.otp.trim()
                            val key = "${cleanPhone}_$otpCode"

                            val isAlreadyInList = _otpList.value.any {
                                it.otp.trim() == otpCode ||
                                (TimezoneHelper.cleanDigits(it.phone) == cleanPhone && it.otp.trim() == otpCode)
                            }
                            val isDuplicate = sentOtpCodes.contains(otpCode) ||
                                              sentOtpKeys.contains(key) ||
                                              isAlreadyInList

                            if (!isDuplicate) {
                                sentOtpCodes.add(otpCode)
                                sentOtpKeys.add(key)
                                try {
                                    prefs.edit()
                                        .putStringSet("saved_sent_otp_codes", HashSet(sentOtpCodes))
                                        .putStringSet("saved_sent_otp_keys", HashSet(sentOtpKeys))
                                        .apply()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                val nowTime = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                                val itemWithTime = item.copy(timestamp = nowTime)
                                _otpList.value = listOf(itemWithTime) + _otpList.value
                                _snackbarMessage.value = "🎯 নতুন OTP এসেছে: ${item.otp} (${item.phone})"
                                removeActiveNumber(item.phone)
                            } else {
                                removeActiveNumber(item.phone)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(4000)
            }
        }
    }

    private fun startTotpTicker() {
        totpJob?.cancel()
        totpJob = viewModelScope.launch {
            while (isActive) {
                val sec = _twoFactorSecret.value
                val rem = TotpHelper.getRemainingSeconds()
                _twoFactorSecondsLeft.value = rem
                if (sec.isNotBlank()) {
                    val code = TotpHelper.generateTotp(sec)
                    _twoFactorCode.value = code
                } else {
                    _twoFactorCode.value = null
                }
                delay(1000)
            }
        }
    }

    private fun updateTotp() {
        val sec = _twoFactorSecret.value
        if (sec.isNotBlank()) {
            _twoFactorCode.value = TotpHelper.generateTotp(sec)
        } else {
            _twoFactorCode.value = null
        }
    }

    private fun loadStoredActiveNumbers() {
        try {
            val jsonStr = prefs.getString("active_numbers_json", "{}") ?: "{}"
            val jsonObj = JSONObject(jsonStr)
            val map = mutableMapOf<String, ActiveNumber>()
            val keys = jsonObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObj.getJSONObject(key)
                map[key] = ActiveNumber(
                    phone = obj.optString("phone", key),
                    service = obj.optString("service", "SMS"),
                    range = obj.optString("range", ""),
                    timestamp = obj.optString("time", "")
                )
            }
            _activeNumbers.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveActiveNumbers(map: Map<String, ActiveNumber>) {
        try {
            val jsonObj = JSONObject()
            for ((key, value) in map) {
                val itemObj = JSONObject().apply {
                    put("phone", value.phone)
                    put("service", value.service)
                    put("range", value.range)
                    put("time", value.timestamp)
                }
                jsonObj.put(key, itemObj)
            }
            prefs.edit().putString("active_numbers_json", jsonObj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        otpMonitorJob?.cancel()
        totpJob?.cancel()
    }
}
