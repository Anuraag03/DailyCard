package com.example.dailycardapp

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class Card(val suit: String, val rank: String)

class MainActivity : AppCompatActivity() {
    private lateinit var cardDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardDisplay = findViewById(R.id.cardDisplay)
        if (shouldShowCard()) {
            showCardOfTheDay()
        } else {
            showStoredCard()
        }
    }

    private fun getShuffledDeck(): MutableList<Card> {
        val suits = listOf("Spades", "Hearts", "Clubs", "Diamonds")
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        return suits.flatMap { suit -> ranks.map { rank -> Card(suit, rank) } }.shuffled().toMutableList()
    }

    private fun shouldShowCard(): Boolean {
        val prefs = getSharedPreferences("CardPrefs", Context.MODE_PRIVATE)
        val lastShownDate = prefs.getString("lastShownDate", null)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return (lastShownDate != currentDate && (hour > 21 || (hour == 21 && minute >= 30)))
    }

    private fun showCardOfTheDay() {
        val prefs = getSharedPreferences("CardPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val deckJson = prefs.getString("deck", null)

        val type = object : TypeToken<MutableList<Card>>() {}.type
        val deck: MutableList<Card> = if (deckJson != null) gson.fromJson(deckJson, type) else getShuffledDeck()

        if (deck.isEmpty()) {
            editor.putString("deck", gson.toJson(getShuffledDeck()))
            editor.apply()
            showCardOfTheDay()
            return
        }

        val card = deck.removeAt(0)
        editor.putString("lastShownDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        editor.putString("lastCard", gson.toJson(card))
        editor.putString("deck", gson.toJson(deck))
        editor.apply()

        cardDisplay.text = "${card.rank} of ${card.suit}"
    }

    private fun showStoredCard() {
        val prefs = getSharedPreferences("CardPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val lastCardJson = prefs.getString("lastCard", null)
        if (lastCardJson != null) {
            val card = gson.fromJson(lastCardJson, Card::class.java)
            cardDisplay.text = "${card.rank} of ${card.suit}"
        } else {
            cardDisplay.text = "No card for today yet."
        }
    }
}