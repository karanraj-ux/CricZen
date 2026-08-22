package com.example.util

import java.util.Calendar

object ZennyMemoryBank {
    private val onThisDayEvents = mapOf(
        "04-02" to "On this day in 2011, MS Dhoni hit the legendary World Cup-winning six!",
        "09-19" to "On this day in 2007, Yuvraj Singh smashed 6 sixes in a single over off Stuart Broad!",
        "06-25" to "On this day in 1983, Kapil Dev's India won their first ever World Cup at Lord's!",
        "11-13" to "On this day in 2014, Rohit Sharma scored a massive 264 against Sri Lanka!",
        "08-14" to "On this day in 1990, a 17-year-old Sachin Tendulkar scored his first Test century in Manchester!",
        "02-24" to "On this day in 2010, Sachin Tendulkar became the first man to score an ODI double century!",
        "03-24" to "On this day in 2022, MS Dhoni passed the CSK captaincy to Ravindra Jadeja.",
        "03-23" to "On this day in 2003, Australia defeated India in the World Cup Final in Johannesburg.",
        "01-15" to "On this day in 2017, Virat Kohli took over as India's full-time captain across all formats."
    )

    private val playerTrivia = mapOf(
        "virat" to listOf(
            "Did you know? Virat Kohli is the fastest to 10,000 ODI runs, achieving it in just 205 innings.",
            "Kohli's nickname 'Chiku' was given to him by his childhood coach Ajit Chaudhary.",
            "He is the only IPL player to never be auctioned, staying with RCB since 2008!"
        ),
        "dhoni" to listOf(
            "MS Dhoni is the only captain to win all three ICC white-ball trophies!",
            "Did you know? Dhoni was a football goalkeeper before his coach urged him to try wicketkeeping.",
            "He holds the record for the fastest stumping in international cricket—a lightning 0.08 seconds!"
        ),
        "rohit" to listOf(
            "Rohit Sharma holds the record for the most double centuries (3) in ODIs.",
            "He started his career as an off-spinner before becoming a full-time opening batter.",
            "Rohit has won the most IPL titles as a captain along with MS Dhoni (5 titles)."
        ),
        "bumrah" to listOf(
            "Jasprit Bumrah's unique bowling action was self-taught by throwing a ball at the floor skirting in his house.",
            "He holds the record for the most runs scored in a single over in Test cricket (35 runs off Stuart Broad)!"
        )
    )

    private val rainDelayStories = listOf(
        "Match delayed by rain... Did you know? The first ever ODI was played because a Test match was washed out for 3 days in 1971!",
        "Rain, rain, go away... In 1989, a match was abandoned because fans threw a pig onto the outfield!",
        "Waiting for the pitch to dry. Fun fact: The longest cricket match in history lasted 14 days between England and South Africa in 1939!",
        "Covers are on... Did you know cricket bats used to be shaped like hockey sticks until the 18th century?"
    )

    private val randomTrivia = listOf(
        "Did you know? The longest recorded cricket ball throw is 140 yards by Robert Percival in 1882.",
        "Sachin Tendulkar played for Pakistan before India! He fielded as a substitute for Pakistan in a 1987 exhibition match.",
        "Chris Gayle is the only player to hit a six off the very first ball of a Test match.",
        "Alec Stewart, born on 8-4-63, scored exactly 8463 Test runs in his career!"
    )

    fun getMemory(matchStatus: String = "", idolName: String = ""): String {
        // 1. Rain Delay
        val lowerStatus = matchStatus.lowercase()
        if (lowerStatus.contains("rain") || lowerStatus.contains("delay") || lowerStatus.contains("wet")) {
            return rainDelayStories.random()
        }

        // 2. On This Day
        val calendar = Calendar.getInstance()
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val todayKey = "$month-$day"
        
        if (onThisDayEvents.containsKey(todayKey)) {
            // 30% chance to show OTD if there's no rain to mix it up, or 100% if no match
            return "Zenny remembers: ${onThisDayEvents[todayKey]}"
        }

        // 3. Idol Trivia
        if (idolName.isNotBlank()) {
            val lowerIdol = idolName.lowercase()
            for ((key, triviaList) in playerTrivia) {
                if (lowerIdol.contains(key)) {
                    return "Hero Fact: ${triviaList.random()}"
                }
            }
        }

        // 4. Random Trivia
        return "Zenny Fact: ${randomTrivia.random()}"
    }
}
