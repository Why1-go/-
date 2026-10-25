package com.suixing.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

data class Pet(val id: String, val name: String, val alias: String, val imagePath: String)
data class Line(val id: String, var text: String, var lastSpoken: Long = 0L)

object PetStore {

    private const val PREFS = "suixing_data"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun newId(): String = UUID.randomUUID().toString()

    // ---------- 角色 ----------
    fun loadPets(ctx: Context): MutableList<Pet> {
        val list = mutableListOf<Pet>()
        try {
            val arr = JSONArray(prefs(ctx).getString("pets", "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Pet(o.getString("id"), o.getString("name"),
                    o.optString("alias", "主人"), o.getString("imagePath")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun savePets(ctx: Context, pets: List<Pet>) {
        val arr = JSONArray()
        pets.forEach {
            arr.put(JSONObject().put("id", it.id).put("name", it.name)
                .put("alias", it.alias).put("imagePath", it.imagePath))
        }
        prefs(ctx).edit().putString("pets", arr.toString()).apply()
    }

    fun activePetId(ctx: Context): String? = prefs(ctx).getString("activePetId", null)

    fun setActivePetId(ctx: Context, id: String?) {
        prefs(ctx).edit().putString("activePetId", id).apply()
    }

    fun loadActivePet(ctx: Context): Pet? = loadPets(ctx).find { it.id == activePetId(ctx) }

    // ---------- 台词 ----------
    fun loadLines(ctx: Context): MutableList<Line> {
        val list = mutableListOf<Line>()
        try {
            val arr = JSONArray(prefs(ctx).getString("lines", "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Line(o.getString("id"), o.getString("text"), o.optLong("lastSpoken", 0L)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveLines(ctx: Context, lines: List<Line>) {
        val arr = JSONArray()
        lines.forEach {
            arr.put(JSONObject().put("id", it.id).put("text", it.text)
                .put("lastSpoken", it.lastSpoken))
        }
        prefs(ctx).edit().putString("lines", arr.toString()).apply()
    }

    fun markLineSpoken(ctx: Context, lineId: String) {
        val lines = loadLines(ctx)
        lines.find { it.id == lineId }?.lastSpoken = System.currentTimeMillis()
        saveLines(ctx, lines)
    }

    // ---------- 行为设置 ----------
    fun minSec(ctx: Context) = prefs(ctx).getInt("minSec", 60)
    fun maxSec(ctx: Context) = prefs(ctx).getInt("maxSec", 300)
    fun bubbleSec(ctx: Context) = prefs(ctx).getInt("bubbleSec", 4)
    fun driftEnabled(ctx: Context) = prefs(ctx).getBoolean("drift", true)
    fun shuffleEnabled(ctx: Context) = prefs(ctx).getBoolean("shuffle", true)

    fun saveIntervals(ctx: Context, min: Int, max: Int, bubble: Int) {
        prefs(ctx).edit()
            .putInt("minSec", min.coerceAtLeast(10))
            .putInt("maxSec", max.coerceAtLeast(min + 1))
            .putInt("bubbleSec", bubble.coerceIn(1, 60))
            .apply()
    }

    fun saveDrift(ctx: Context, on: Boolean) = prefs(ctx).edit().putBoolean("drift", on).apply()
    fun saveShuffle(ctx: Context, on: Boolean) = prefs(ctx).edit().putBoolean("shuffle", on).apply()

    fun randomIntervalMs(ctx: Context): Long {
        val lo = minSec(ctx).coerceAtLeast(10) * 1000L
        val hi = (maxSec(ctx) * 1000L).coerceAtLeast(lo + 1000)
        return Random.nextLong(lo, hi + 1)
    }
}
