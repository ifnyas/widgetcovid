package app.ifnyas.widgetcovid.utils

import android.content.Context

class SessionManager(context: Context) {

    private val pref = context.getSharedPreferences("Preferences", 0)
    private val editor = pref.edit()

    // Delete Session
    fun clearSession() {
        editor.clear().apply()
    }

    // User
    fun postPlace(str: String) {
        editor.putString("Place", str).apply()
    }

    fun getPlace() = pref.getString("Place", "Buka App dulu,Baru cek di sini!")
}


