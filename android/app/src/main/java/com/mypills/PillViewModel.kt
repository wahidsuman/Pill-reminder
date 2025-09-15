package com.mypills

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PillViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("MyPillsPrefs", Context.MODE_PRIVATE)
    
    private val _pills = mutableStateListOf<Pill>()
    val pills: List<Pill> = _pills
    
    private val _currentTime = mutableStateOf("")
    val currentTime: String = _currentTime.value
    
    private val _showAddPillDialog = mutableStateOf(false)
    val showAddPillDialog: Boolean = _showAddPillDialog.value
    
    private val _showNotificationDialog = mutableStateOf(false)
    val showNotificationDialog: Boolean = _showNotificationDialog.value
    
    private val _activeNotificationPill = mutableStateOf<Pill?>(null)
    val activeNotificationPill: Pill? = _activeNotificationPill.value
    
    val takenCount: Int
        get() = _pills.count { it.taken }
    
    val remainingCount: Int
        get() = _pills.count { !it.taken }
    
    init {
        loadPillsFromStorage()
        startTimeUpdater()
    }
    
    private fun startTimeUpdater() {
        viewModelScope.launch {
            while (true) {
                updateCurrentTime()
                delay(60000) // Update every minute
            }
        }
    }
    
    private fun updateCurrentTime() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        _currentTime.value = timeFormat.format(Date())
    }
    
    fun addPill(pill: Pill) {
        _pills.add(pill)
        savePillsToStorage()
    }
    
    fun updatePill(pill: Pill) {
        val index = _pills.indexOfFirst { it.id == pill.id }
        if (index != -1) {
            _pills[index] = pill
            savePillsToStorage()
        }
    }
    
    fun deletePill(pillId: Long) {
        _pills.removeAll { it.id == pillId }
        savePillsToStorage()
    }
    
    fun markPillAsTaken(pillId: Long) {
        val index = _pills.indexOfFirst { it.id == pillId }
        if (index != -1) {
            _pills[index] = _pills[index].copy(taken = true)
            savePillsToStorage()
        }
    }
    
    fun markPillAsNotTaken(pillId: Long) {
        val index = _pills.indexOfFirst { it.id == pillId }
        if (index != -1) {
            _pills[index] = _pills[index].copy(taken = false)
            savePillsToStorage()
        }
    }
    
    fun showAddPillDialog() {
        _showAddPillDialog.value = true
    }
    
    fun hideAddPillDialog() {
        _showAddPillDialog.value = false
    }
    
    fun showNotificationDialog(pill: Pill) {
        _activeNotificationPill.value = pill
        _showNotificationDialog.value = true
    }
    
    fun hideNotificationDialog() {
        _showNotificationDialog.value = false
        _activeNotificationPill.value = null
    }
    
    fun triggerDemoNotification() {
        val availablePills = _pills.filter { !it.taken }
        if (availablePills.isNotEmpty()) {
            val randomPill = availablePills.random()
            showNotificationDialog(randomPill)
        }
    }
    
    private fun savePillsToStorage() {
        try {
            val pillsArray = JSONArray()
            _pills.forEach { pill ->
                val pillObject = JSONObject().apply {
                    put("id", pill.id)
                    put("name", pill.name)
                    put("dosage", pill.dosage)
                    put("time", pill.time)
                    put("color", pill.color)
                    put("taken", pill.taken)
                }
                pillsArray.put(pillObject)
            }
            
            sharedPreferences.edit()
                .putString("saved_pills", pillsArray.toString())
                .apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    
    private fun loadPillsFromStorage() {
        try {
            val pillsJson = sharedPreferences.getString("saved_pills", null)
            if (pillsJson != null && pillsJson.isNotEmpty()) {
                val pillsArray = JSONArray(pillsJson)
                val loadedPills = mutableListOf<Pill>()
                
                for (i in 0 until pillsArray.length()) {
                    val pillObject = pillsArray.getJSONObject(i)
                    val pill = Pill(
                        id = pillObject.getLong("id"),
                        name = pillObject.getString("name"),
                        dosage = pillObject.getString("dosage"),
                        time = pillObject.getString("time"),
                        color = pillObject.getString("color"),
                        taken = pillObject.getBoolean("taken")
                    )
                    loadedPills.add(pill)
                }
                
                _pills.clear()
                _pills.addAll(loadedPills)
            } else {
                // Add sample pills if no saved data
                addSamplePills()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            addSamplePills()
        }
    }
    
    private fun addSamplePills() {
        _pills.addAll(listOf(
            Pill(
                name = "Vitamin D",
                dosage = "1000 IU",
                time = "8:00 AM",
                color = "blue"
            ),
            Pill(
                name = "Blood Pressure",
                dosage = "5mg",
                time = "2:00 PM",
                color = "red"
            ),
            Pill(
                name = "Multivitamin",
                dosage = "1 tablet",
                time = "6:00 PM",
                color = "green"
            )
        ))
        savePillsToStorage()
    }
}