package com.braintrainer.app.game

import kotlin.random.Random

// Data object for Country
data class Country(val code: String, val name: String, val continent: String, val flagEmoji: String, val difficulty: Int = 2) {
    fun getDisplayName(): String {
        return try {
            val loc = java.util.Locale("", code)
            val display = loc.displayCountry
            if (display.isNotEmpty() && display != code) display else name
        } catch (e: Exception) {
            name
        }
    }
}

object CountryData {
    val allCountries = listOf(
        // === AMERICAS ===
        Country("BR", "Brasil", "AMERICAS", "🇧🇷", 1),
        Country("AR", "Argentina", "AMERICAS", "🇦🇷", 1),
        Country("UY", "Uruguai", "AMERICAS", "🇺🇾", 2),
        Country("PY", "Paraguai", "AMERICAS", "🇵🇾", 2),
        Country("CL", "Chile", "AMERICAS", "🇨🇱", 1),
        Country("BO", "Bolívia", "AMERICAS", "🇧🇴", 2),
        Country("PE", "Peru", "AMERICAS", "🇵🇪", 2),
        Country("EC", "Equador", "AMERICAS", "🇪🇨", 2),
        Country("CO", "Colômbia", "AMERICAS", "🇨🇴", 1),
        Country("VE", "Venezuela", "AMERICAS", "🇻🇪", 2),
        Country("GY", "Guiana", "AMERICAS", "🇬🇾", 3),
        Country("SR", "Suriname", "AMERICAS", "🇸🇷", 3),
        Country("US", "Estados Unidos", "AMERICAS", "🇺🇸", 1),
        Country("CA", "Canadá", "AMERICAS", "🇨🇦", 1),
        Country("MX", "México", "AMERICAS", "🇲🇽", 1),
        Country("GT", "Guatemala", "AMERICAS", "🇬🇹", 2),
        Country("BZ", "Belize", "AMERICAS", "🇧🇿", 3),
        Country("SV", "El Salvador", "AMERICAS", "🇸🇻", 2),
        Country("HN", "Honduras", "AMERICAS", "🇭🇳", 2),
        Country("NI", "Nicarágua", "AMERICAS", "🇳🇮", 2),
        Country("CR", "Costa Rica", "AMERICAS", "🇨🇷", 2),
        Country("PA", "Panamá", "AMERICAS", "🇵🇦", 2),
        Country("CU", "Cuba", "AMERICAS", "🇨🇺", 1),
        Country("JM", "Jamaica", "AMERICAS", "🇯🇲", 1),
        Country("HT", "Haiti", "AMERICAS", "🇭🇹", 3),
        Country("DO", "República Dominicana", "AMERICAS", "🇩🇴", 2),
        Country("BS", "Bahamas", "AMERICAS", "🇧🇸", 3),
        Country("BB", "Barbados", "AMERICAS", "🇧🇧", 3),
        Country("TT", "Trinidad e Tobago", "AMERICAS", "🇹🇹", 3),
        Country("LC", "Santa Lúcia", "AMERICAS", "🇱🇨", 3),
        Country("GD", "Granada", "AMERICAS", "🇬🇩", 3),
        Country("AG", "Antígua e Barbuda", "AMERICAS", "🇦🇬", 3),
        Country("DM", "Dominica", "AMERICAS", "🇩🇲", 3),
        Country("KN", "São Cristóvão e Neves", "AMERICAS", "🇰🇳", 3),
        Country("VC", "São Vicente e Granadinas", "AMERICAS", "🇻🇨", 3),

        // === EUROPE ===
        Country("PT", "Portugal", "EUROPE", "🇵🇹", 1),
        Country("ES", "Espanha", "EUROPE", "🇪🇸", 1),
        Country("FR", "França", "EUROPE", "🇫🇷", 1),
        Country("DE", "Alemanha", "EUROPE", "🇩🇪", 1),
        Country("IT", "Itália", "EUROPE", "🇮🇹", 1),
        Country("GB", "Reino Unido", "EUROPE", "🇬🇧", 1),
        Country("IE", "Irlanda", "EUROPE", "🇮🇪", 1),
        Country("NL", "Holanda", "EUROPE", "🇳🇱", 1),
        Country("BE", "Bélgica", "EUROPE", "🇧🇪", 1),
        Country("LU", "Luxemburgo", "EUROPE", "🇱🇺", 2),
        Country("CH", "Suíça", "EUROPE", "🇨🇭", 1),
        Country("AT", "Áustria", "EUROPE", "🇦🇹", 2),
        Country("SE", "Suécia", "EUROPE", "🇸🇪", 1),
        Country("NO", "Noruega", "EUROPE", "🇳🇴", 1),
        Country("DK", "Dinamarca", "EUROPE", "🇩🇰", 2),
        Country("FI", "Finlândia", "EUROPE", "🇫🇮", 2),
        Country("IS", "Islândia", "EUROPE", "🇮🇸", 2),
        Country("PL", "Polônia", "EUROPE", "🇵🇱", 2),
        Country("CZ", "República Tcheca", "EUROPE", "🇨🇿", 2),
        Country("SK", "Eslováquia", "EUROPE", "🇸🇰", 3),
        Country("HU", "Hungria", "EUROPE", "🇭🇺", 2),
        Country("RO", "Romênia", "EUROPE", "🇷🇴", 2),
        Country("BG", "Bulgária", "EUROPE", "🇧🇬", 2),
        Country("GR", "Grécia", "EUROPE", "🇬🇷", 1),
        Country("HR", "Croácia", "EUROPE", "🇭🇷", 1),
        Country("RS", "Sérvia", "EUROPE", "🇷🇸", 2),
        Country("BA", "Bósnia e Herzegovina", "EUROPE", "🇧🇦", 3),
        Country("SI", "Eslovênia", "EUROPE", "🇸🇮", 3),
        Country("MK", "Macedônia do Norte", "EUROPE", "🇲🇰", 3),
        Country("AL", "Albânia", "EUROPE", "🇦🇱", 3),
        Country("ME", "Montenegro", "EUROPE", "🇲🇪", 3),
        Country("RU", "Rússia", "EUROPE", "🇷🇺", 1),
        Country("UA", "Ucrânia", "EUROPE", "🇺🇦", 1),
        Country("BY", "Bielorrússia", "EUROPE", "🇧🇾", 3),
        Country("MD", "Moldávia", "EUROPE", "🇲🇩", 3),
        Country("EE", "Estônia", "EUROPE", "🇪🇪", 3),
        Country("LV", "Letônia", "EUROPE", "🇱🇻", 3),
        Country("LT", "Lituânia", "EUROPE", "🇱🇹", 3),
        Country("MT", "Malta", "EUROPE", "🇲🇹", 3),
        Country("CY", "Chipre", "EUROPE", "🇨🇾", 3),
        Country("AD", "Andorra", "EUROPE", "🇦🇩", 3),
        Country("MC", "Mônaco", "EUROPE", "🇲🇨", 3),
        Country("LI", "Liechtenstein", "EUROPE", "🇱🇮", 3),
        Country("SM", "San Marino", "EUROPE", "🇸🇲", 3),
        Country("VA", "Vaticano", "EUROPE", "🇻🇦", 2),

        // === ASIA ===
        Country("CN", "China", "ASIA", "🇨🇳", 1),
        Country("JP", "Japão", "ASIA", "🇯🇵", 1),
        Country("IN", "Índia", "ASIA", "🇮🇳", 1),
        Country("KR", "Coreia do Sul", "ASIA", "🇰🇷", 1),
        Country("KP", "Coreia do Norte", "ASIA", "🇰🇵", 2),
        Country("ID", "Indonésia", "ASIA", "🇮🇩", 2),
        Country("PK", "Paquistão", "ASIA", "🇵🇰", 2),
        Country("BD", "Bangladesh", "ASIA", "🇧🇩", 2),
        Country("VN", "Vietnã", "ASIA", "🇻🇳", 2),
        Country("PH", "Filipinas", "ASIA", "🇵🇭", 2),
        Country("TR", "Turquia", "ASIA", "🇹🇷", 1),
        Country("IR", "Irã", "ASIA", "🇮🇷", 2),
        Country("TH", "Tailândia", "ASIA", "🇹🇭", 1),
        Country("MM", "Mianmar", "ASIA", "🇲🇲", 3),
        Country("IQ", "Iraque", "ASIA", "🇮🇶", 2),
        Country("AF", "Afeganistão", "ASIA", "🇦🇫", 2),
        Country("SA", "Arábia Saudita", "ASIA", "🇸🇦", 1),
        Country("UZ", "Uzbequistão", "ASIA", "🇺🇿", 3),
        Country("MY", "Malásia", "ASIA", "🇲🇾", 2),
        Country("YE", "Iêmen", "ASIA", "🇾🇪", 3),
        Country("NP", "Nepal", "ASIA", "🇳🇵", 2),
        Country("LK", "Sri Lanka", "ASIA", "🇱🇰", 2),
        Country("KZ", "Cazaquistão", "ASIA", "🇰🇿", 2),
        Country("SY", "Síria", "ASIA", "🇸🇾", 2),
        Country("KH", "Camboja", "ASIA", "🇰🇭", 3),
        Country("JO", "Jordânia", "ASIA", "🇯🇴", 3),
        Country("AZ", "Azerbaijão", "ASIA", "🇦🇿", 3),
        Country("AE", "Emirados Árabes Unidos", "ASIA", "🇦🇪", 2),
        Country("TJ", "Tajiquistão", "ASIA", "🇹🇯", 3),
        Country("IL", "Israel", "ASIA", "🇮🇱", 1),
        Country("LA", "Laos", "ASIA", "🇱🇦", 3),
        Country("KG", "Quirguistão", "ASIA", "🇰🇬", 3),
        Country("TM", "Turcomenistão", "ASIA", "🇹🇲", 3),
        Country("SG", "Cingapura", "ASIA", "🇸🇬", 2),
        Country("OM", "Omã", "ASIA", "🇴🇲", 3),
        Country("PS", "Palestina", "ASIA", "🇵🇸", 2),
        Country("KW", "Kuwait", "ASIA", "🇰🇼", 3),
        Country("GE", "Geórgia", "ASIA", "🇬🇪", 3),
        Country("MN", "Mongólia", "ASIA", "🇲🇳", 3),
        Country("AM", "Armênia", "ASIA", "🇦🇲", 3),
        Country("QA", "Catar", "ASIA", "🇶🇦", 2),
        Country("BH", "Bahrein", "ASIA", "🇧🇭", 3),
        Country("TL", "Timor-Leste", "ASIA", "🇹🇱", 3),
        Country("LB", "Líbano", "ASIA", "🇱🇧", 2),
        Country("BT", "Butão", "ASIA", "🇧🇹", 3),
        Country("MV", "Maldivas", "ASIA", "🇲🇻", 3),
        Country("BN", "Brunei", "ASIA", "🇧🇳", 3),

        // === AFRICA ===
        Country("NG", "Nigéria", "AFRICA", "🇳🇬", 2),
        Country("ET", "Etiópia", "AFRICA", "🇪🇹", 2),
        Country("EG", "Egito", "AFRICA", "🇪🇬", 1),
        Country("CD", "R.D. Congo", "AFRICA", "🇨🇩", 3),
        Country("ZA", "África do Sul", "AFRICA", "🇿🇦", 1),
        Country("TZ", "Tanzânia", "AFRICA", "🇹🇿", 2),
        Country("KE", "Quênia", "AFRICA", "🇰🇪", 2),
        Country("UG", "Uganda", "AFRICA", "🇺🇬", 3),
        Country("DZ", "Argélia", "AFRICA", "🇩🇿", 2),
        Country("SD", "Sudão", "AFRICA", "🇸🇩", 3),
        Country("MA", "Marrocos", "AFRICA", "🇲🇦", 2),
        Country("AO", "Angola", "AFRICA", "🇦🇴", 2),
        Country("MZ", "Moçambique", "AFRICA", "🇲🇿", 2),
        Country("GH", "Gana", "AFRICA", "🇬🇭", 2),
        Country("MG", "Madagascar", "AFRICA", "🇲🇬", 2),
        Country("CM", "Camarões", "AFRICA", "🇨🇲", 2),
        Country("CI", "Costa do Marfim", "AFRICA", "🇨🇮", 2),
        Country("NE", "Níger", "AFRICA", "🇳🇪", 3),
        Country("BF", "Burkina Faso", "AFRICA", "🇧🇫", 3),
        Country("ML", "Mali", "AFRICA", "🇲🇱", 3),
        Country("MW", "Malawi", "AFRICA", "🇲🇼", 3),
        Country("ZM", "Zâmbia", "AFRICA", "🇿🇲", 3),
        Country("SN", "Senegal", "AFRICA", "🇸🇳", 2),
        Country("TD", "Chade", "AFRICA", "🇹🇩", 3),
        Country("SO", "Somália", "AFRICA", "🇸🇴", 3),
        Country("ZW", "Zimbábue", "AFRICA", "🇿🇼", 3),
        Country("GN", "Guiné", "AFRICA", "🇬🇳", 3),
        Country("RW", "Ruanda", "AFRICA", "🇷🇼", 3),
        Country("BJ", "Benin", "AFRICA", "🇧🇯", 3),
        Country("BI", "Burundi", "AFRICA", "🇧🇮", 3),
        Country("TN", "Tunísia", "AFRICA", "🇹🇳", 2),
        Country("SS", "Sudão do Sul", "AFRICA", "🇸🇸", 3),
        Country("TG", "Togo", "AFRICA", "🇹🇬", 3),
        Country("SL", "Serra Leoa", "AFRICA", "🇸🇱", 3),
        Country("LY", "Líbia", "AFRICA", "🇱🇾", 3),
        Country("CG", "Congo", "AFRICA", "🇨🇬", 3),
        Country("LR", "Libéria", "AFRICA", "🇱🇷", 3),
        Country("CF", "Rep. Centro-Africana", "AFRICA", "🇨🇫", 3),
        Country("MR", "Mauritânia", "AFRICA", "🇲🇷", 3),
        Country("ER", "Eritreia", "AFRICA", "🇪🇷", 3),
        Country("NA", "Namíbia", "AFRICA", "🇳🇦", 3),
        Country("GM", "Gâmbia", "AFRICA", "🇬🇲", 3),
        Country("BW", "Botsuana", "AFRICA", "🇧🇼", 3),
        Country("GA", "Gabão", "AFRICA", "🇬🇦", 3),
        Country("LS", "Lesoto", "AFRICA", "🇱🇸", 3),
        Country("GW", "Guiné-Bissau", "AFRICA", "🇬🇼", 3),
        Country("GQ", "Guiné Equatorial", "AFRICA", "🇬🇶", 3),
        Country("MU", "Maurício", "AFRICA", "🇲🇺", 3),
        Country("SZ", "Essuatíni", "AFRICA", "🇸🇿", 3),
        Country("DJ", "Djibouti", "AFRICA", "🇩🇯", 3),
        Country("KM", "Comores", "AFRICA", "🇰🇲", 3),
        Country("CV", "Cabo Verde", "AFRICA", "🇨🇻", 2),
        Country("ST", "São Tomé e Príncipe", "AFRICA", "🇸🇹", 3),
        Country("SC", "Seychelles", "AFRICA", "🇸🇨", 3),

        // === OCEANIA ===
        Country("AU", "Austrália", "OCEANIA", "🇦🇺", 1),
        Country("PG", "Papua Nova Guiné", "OCEANIA", "🇵🇬", 3),
        Country("NZ", "Nova Zelândia", "OCEANIA", "🇳🇿", 1),
        Country("FJ", "Fiji", "OCEANIA", "🇫🇯", 3),
        Country("SB", "Ilhas Salomão", "OCEANIA", "🇸🇧", 3),
        Country("VU", "Vanuatu", "OCEANIA", "🇻🇺", 3),
        Country("NC", "Nova Caledônia", "OCEANIA", "🇳🇨", 3),
        Country("PF", "Polinésia Francesa", "OCEANIA", "🇵🇫", 3),
        Country("WS", "Samoa", "OCEANIA", "🇼🇸", 3),
        Country("GU", "Guam", "OCEANIA", "🇬🇺", 3),
        Country("KI", "Kiribati", "OCEANIA", "🇰🇮", 3),
        Country("TO", "Tonga", "OCEANIA", "🇹🇴", 3),
        Country("FM", "Micronésia", "OCEANIA", "🇫🇲", 3),
        Country("MH", "Ilhas Marshall", "OCEANIA", "🇲🇭", 3),
        Country("PW", "Palau", "OCEANIA", "🇵🇼", 3),
        Country("NR", "Nauru", "OCEANIA", "🇳🇷", 3),
        Country("TV", "Tuvalu", "OCEANIA", "🇹🇻", 3)
    )
}

class FlagGameStrategy : GameStrategy {
    override fun getGameType() = "FLAG_QUIZ"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 15

    // Track used questions to prevent repeats
    private val usedCodes = mutableSetOf<String>()

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Input format expected: "REGION|DIFFICULTY" e.g. "AMERICAS|HARD"
        // If no "|" found, treat entire string as Region.
        
        val parts = difficulty.split("|")
        val region = parts[0]
        val difficultyLevel = if (parts.size > 1) parts[1] else "MEDIUM"
        
        // 1. Filter by Region
        val regionPool = if (region == "WORLD") {
            CountryData.allCountries
        } else {
            CountryData.allCountries.filter { it.continent == region }
        }
        
        // 2. Filter by Difficulty
        // EASY -> Tier 1
        // MEDIUM -> Tier 1, 2
        // HARD -> Tier 1, 2, 3
        val maxTier = when(difficultyLevel) {
            "EASY" -> 1
            "HARD" -> 3
            else -> 2 // Medium
        }
        
        val difficultyPool = regionPool.filter { it.difficulty <= maxTier }
        
        // Priority 1: Unused flags within selected difficulty
        var validPool = difficultyPool.filter { !usedCodes.contains(it.code) }
        
        // Priority 2: Unused flags from ANY difficulty in the region (User Request: Show harder if needed to avoid repeats)
        if (validPool.isEmpty()) {
            validPool = regionPool.filter { !usedCodes.contains(it.code) }
        }
        
        // Priority 3: Repeats allowed (filtered by difficulty) - Only if we exhausted ALL unique flags in region
        if (validPool.isEmpty()) {
            validPool = difficultyPool
        }
        
        // Priority 4: Repeats allowed (entire region)
        if (validPool.isEmpty()) {
             validPool = regionPool
        }
        
        // Ultimate Fallback: All World repeats
        val finalPool = if (validPool.isNotEmpty()) validPool else CountryData.allCountries
        
        val correct = finalPool.random()
        usedCodes.add(correct.code)
        
        // Generate Distractors
        val options = mutableSetOf<String>()
        options.add(correct.getDisplayName())
        
        while (options.size < 4) {
            val fake = CountryData.allCountries.random() 
            if (fake.name != correct.name) {
                options.add(fake.getDisplayName())
            }
        }
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = correct.flagEmoji, // Show Flag
            options = options.toList().shuffled(),
            answer = correct.getDisplayName()
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
