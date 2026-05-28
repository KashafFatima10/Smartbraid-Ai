package com.example.smartbraidai

data class UserSelection(
    val hairTexture: String = "Straight",
    val hairColor: String = "Black",
    val hairLength: String = "Short",
    val hairVolume: String = "Thin",
    val makeupType: String = "Natural",
    val skinFinish: String = "Matte",
    val eyeMakeup: String = "Soft",
    val lipStyle: String = "Glossy",
    val faceShape: String = "Oval",
    val cutLength: String = "Short"
)

data class AiSuggestion(
    val title: String,
    val description: String,
    val tips: List<String>,
    val confidence: Int,
    val maintenanceLevel: String
)

object SuggestionEngine {

    fun generateHairSuggestion(input: UserSelection): AiSuggestion {
        val (style, tips, maint) = when {
            input.hairTexture == "Coily" && input.hairVolume == "Thick" -> Triple(
                "Majestic Layered Afro-Braids",
                listOf("Use leave-in conditioner weekly", "Sleep with a silk bonnet", "Avoid tight tension on edges"),
                "High"
            )
            input.hairTexture == "Curly" && input.hairLength == "Long" -> Triple(
                "Bohemian Goddess Waterfall Braids",
                listOf("Refresh curls with water/oil mix", "Minimize touching to reduce frizz", "Detangle only when wet"),
                "Medium"
            )
            input.hairTexture == "Straight" && input.hairLength == "Medium" -> Triple(
                "Sleek Silk-Press with Curtain Braids",
                listOf("Use heat protectant", "Keep away from humidity", "Wrap hair at night"),
                "Medium"
            )
            else -> Triple(
                "SmartBraid Signature Custom Weave",
                listOf("Maintain scalp hydration", "Visit stylist every 4 weeks", "Avoid heavy oils"),
                "Medium"
            )
        }

        return AiSuggestion(
            title = style,
            description = "AI Analysis: For your ${input.hairTexture.lowercase()} texture, this style provides structural balance while protecting ends.",
            tips = tips,
            confidence = (85..98).random(),
            maintenanceLevel = maint
        )
    }

    fun generateMakeupSuggestion(input: UserSelection): AiSuggestion {
        val (style, tips, maint) = when {
            input.makeupType == "Bridal" && input.skinFinish == "Dewy" -> Triple(
                "Ethereal Royal Glow",
                listOf("Use a hydrating primer", "Set only the T-zone", "Carry a facial mist for refresh"),
                "High"
            )
            input.makeupType == "Party" && input.eyeMakeup == "Bold" -> Triple(
                "Midnight Sapphire Smokey Eye",
                listOf("Use eye primer to avoid creasing", "Blend edges with a transition shade", "Pair with a nude lip"),
                "Medium"
            )
            else -> Triple(
                "Tailored Professional Glamour",
                listOf("Match foundation to neck", "Use setting spray", "Blot excess oil"),
                "Low"
            )
        }

        return AiSuggestion(
            title = style,
            description = "AI Recommendation: Optimized for your ${input.skinFinish.lowercase()} preference with ${input.eyeMakeup.lowercase()} eyes.",
            tips = tips,
            confidence = (90..99).random(),
            maintenanceLevel = maint
        )
    }

    fun generateHaircutSuggestion(input: UserSelection): AiSuggestion {
        val (cut, tips, maint) = when {
            input.faceShape == "Round" && input.cutLength == "Long" -> Triple(
                "Angled Face-Framing Layers",
                listOf("Ask for layers starting below chin", "Style with volume at the crown", "Use a round brush"),
                "Medium"
            )
            input.faceShape == "Oval" && input.cutLength == "Short" -> Triple(
                "Textured Modern French Bob",
                listOf("Apply sea salt spray for texture", "Air dry for a natural look", "Trim every 6 weeks"),
                "Low"
            )
            else -> Triple(
                "Precision Architect Cut",
                listOf("Maintain clean lines", "Use light hold pomade", "Comb through daily"),
                "Medium"
            )
        }

        return AiSuggestion(
            title = cut,
            description = "AI Structural Analysis: Perfectly complements your ${input.faceShape.lowercase()} face shape to highlight cheekbones.",
            tips = tips,
            confidence = (88..97).random(),
            maintenanceLevel = maint
        )
    }
}
