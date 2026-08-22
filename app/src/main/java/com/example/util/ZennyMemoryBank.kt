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
        "01-15" to "On this day in 2017, Virat Kohli took over as India's full-time captain across all formats.",
        "02-07" to "On this day in 1999, Anil Kumble took all 10 wickets in a Test innings against Pakistan at Feroz Shah Kotla!"
    )

        private val playerTrivia = mapOf(
        "virat" to listOf(
            "Shocking Fact: Virat Kohli took a wicket off his 'zeroth' delivery in T20Is! He bowled a wide that resulted in a stumping.",
            "Kohli's nickname 'Chiku' was given to him by his childhood coach Ajit Chaudhary.",
            "He is the only IPL player to never be auctioned, staying with RCB since 2008!",
            "Did you know? Virat Kohli has never been bowled out for a golden duck in T20 Internationals.",
            "Kohli holds the record for the most runs in a single IPL season: a staggering 973 runs in 2016.",
            "He is the fastest batter to reach 8,000, 9,000, 10,000, 11,000, and 12,000 runs in ODI cricket."
        ),
        "dhoni" to listOf(
            "MS Dhoni is the only captain to win all three ICC white-ball trophies!",
            "Shocking Fact: Dhoni actually has 1 international wicket! He bowled Travis Dowlin of West Indies in 2009.",
            "He holds the record for the fastest stumping in international cricket—a lightning 0.08 seconds!",
            "MS Dhoni has the most number of stumpings (123) in ODI cricket history.",
            "He batted at number 7 or lower for most of his career, yet amassed over 10,000 ODI runs."
        ),
        "rohit" to listOf(
            "Mass Number: Rohit Sharma holds the world record for the most Sixes in international cricket across all formats (600+ sixes)!",
            "Wikipedia Record: Rohit holds the highest individual score in ODI history—a mammoth 264 off 173 balls against Sri Lanka (33 fours, 9 sixes)!",
            "Mass Number: He holds the record for the most centuries in T20 Internationals (5 centuries).",
            "Shocking Fact: Rohit Sharma has an IPL hat-trick! He took it against Mumbai Indians while playing for Deccan Chargers in 2009.",
            "Record: Rohit is the only batter to hit 5 centuries in a single edition of the ICC World Cup (2019).",
            "He holds the record for the most double centuries in ODIs (3), while no other player has more than one!",
            "Mass Number: Rohit Sharma has played the most T20 Internationals in history (over 150 matches)."
        ),
        "bumrah" to listOf(
            "Wikipedia Record: Jasprit Bumrah is the fastest Indian pace bowler to reach 100 Test wickets (in just 24 matches).",
            "Mass Number: Bumrah took a staggering 15 wickets in the 2024 T20 World Cup at an impossible economy rate of 4.17!",
            "Shocking Fact: Bumrah holds the world record for the most runs scored in a single over in Test cricket (35 runs off Stuart Broad)!",
            "Record: He is only the third Indian to take a Test hat-trick, doing so against the West Indies in 2019.",
            "Bumrah's best Test figures are a deadly 6/19 against the West Indies.",
            "His very first Test wicket was the legendary AB de Villiers."
        ),
        "sachin" to listOf(
            "Mass Number: Sachin Tendulkar scored 34,357 runs across all international formats, the highest by any player.",
            "He used a bat so heavy (1.5 kg) that it caused him severe tennis elbow injuries.",
            "Sachin is the only player to have scored 100 international centuries."
        )
    )

        private val teamTrivia = mapOf(
        "india" to listOf(
            "Wikipedia Record: The India National Cricket Team played its first ever Test match on 25 June 1932 at Lord's.",
            "Mass Number: India's highest ever Test total is a massive 759/7 declared against England in Chennai (2016).",
            "Shocking Number: India's lowest ever Test total is 36 all out against Australia in Adelaide (2020).",
            "Record: India is the only team to win World Cups in 60-overs (1983), 50-overs (2011), and 20-overs (2007, 2024)!",
            "Mass Number: Sachin Tendulkar holds the team record for most Test matches played (200) and most runs (15,921).",
            "Mass Number: Anil Kumble holds the Indian record for the most Test wickets (619) and ODI wickets (337).",
            "Shocking Fact: Bapu Nadkarni bowled 21 consecutive maiden overs (131 dot balls) against England in 1964!",
            "Wikipedia Record: India's biggest Test victory margin by runs is 434 runs against England in Rajkot (2024).",
            "Underrated Fact: Rahul Dravid has faced the most deliveries in Test cricket history—a staggering 31,258 balls!",
            "Mass Number: Stuart Binny holds the best ODI bowling figures for India: a mind-blowing 6 wickets for just 4 runs against Bangladesh in 2014!"
        ),
        "australia" to listOf(
            "Did you know? Australia has won the ICC Cricket World Cup a record 6 times!",
            "Shocking Fact: Sir Donald Bradman retired with a Test batting average of 99.94.",
            "Allan Border played a mind-blowing 153 consecutive Test matches."
        )
    )

    private val rainDelayStories = listOf(
        "Match delayed by rain... Did you know? The first ever ODI was played only because a Test match was washed out for 3 days in 1971!",
        "Rain, rain, go away... In 1989, a match was abandoned because fans threw a pig onto the outfield!",
        "Waiting for the pitch to dry. Shocking fact: The longest cricket match in history lasted 14 days between England and South Africa in 1939 and ended in a draw!",
        "Covers are on... Did you know cricket bats used to be shaped like hockey sticks until the 18th century?",
        "Rain delay! Did you know? The ICC introduced the Duckworth-Lewis method in 1999 because the previous rule made South Africa need 22 runs off 1 ball in the 1992 World Cup semi-final!"
    )

    private val randomTrivia = listOf(
        "Shocking Fact: Wasim Akram's highest Test score (257) is actually higher than Sachin Tendulkar's highest Test score (248*)!",
        "Underrated Fact: Sanath Jayasuriya has taken more ODI wickets (323) than legendary spinner Shane Warne (293)!",
        "Did you know? The longest recorded cricket ball throw is 140 yards by Robert Percival in 1882.",
        "Shocking Fact: Courtney Walsh holds the record for the most ducks in Test cricket history with 43 zeroes.",
        "Shoaib Akhtar bowled the fastest recorded delivery in cricket history at 161.3 km/h (100.2 mph).",
        "Peter Siddle is the only bowler to ever take a hat-trick on his birthday.",
        "Shocking Fact: Adam Gilchrist took a wicket on his very first ball in the IPL and then never bowled again!",
        "Alec Stewart, born on 8-4-63, scored exactly 8463 Test runs in his career!",
        "Chris Gayle is the only player to hit a six off the very first ball of a Test match.",
        "Dirk Nannes has represented both the Netherlands and Australia in international cricket."
    )

    fun getMemory(matchStatus: String = "", idolName: String = "", preferredTeams: Set<String> = emptySet()): String {
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

        // 4. Team-Specific Trivia
        if (preferredTeams.isNotEmpty()) {
            val teamsList = preferredTeams.toList().shuffled()
            for (team in teamsList) {
                val lowerTeam = team.lowercase()
                for ((key, triviaList) in teamTrivia) {
                    if (lowerTeam.contains(key)) {
                        return "Zenny Fact: ${triviaList.random()}"
                    }
                }
            }
        }

        // 5. Random Trivia
        return "Zenny Fact: ${randomTrivia.random()}"
    }
}
