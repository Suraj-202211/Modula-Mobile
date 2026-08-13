package com.modulamobile.ui.i18n

import com.modulamobile.ui.state.GlobalState

val translations = mapOf(
    "English (US)" to mapOf(
        "home" to "Home",
        "versions" to "Versions",
        "mods" to "Mods",
        "modpacks" to "Modpacks",
        "settings" to "Settings",
        "profile" to "Profile",
        "readyToLaunch" to "Ready to launch",
        "launchGame" to "Launch Game",
        "allocated" to "Allocated",
        "security" to "Security",
        "active" to "Active",
        "engineMetrics" to "Engine Metrics",
        "ramAllocation" to "RAM Allocation",
        "visualFidelity" to "Visual Fidelity",
        "interfaceLanguage" to "Interface Language",
        "applicationPreferences" to "Application Preferences",
        "migrationTools" to "Migration & Import",
        "fluxVoice" to "Flux Voice Engine",
        "voiceDescription" to "Native alternative to Simple Voice Chat"
    ),
    "Malayalam" to mapOf(
        "home" to "ഹോം",
        "versions" to "പതിപ്പുകൾ",
        "mods" to "മോഡ്സ്",
        "modpacks" to "മോഡ്പാക്കുകൾ",
        "settings" to "ക്രമീകരണങ്ങൾ",
        "profile" to "പ്രൊഫൈൽ",
        "readyToLaunch" to "തുടങ്ങാൻ തയ്യാറാണ്",
        "launchGame" to "ഗെയിം തുടങ്ങുക",
        "allocated" to "അനുവദിച്ചു",
        "security" to "സുരക്ഷ",
        "active" to "സജീവം",
        "engineMetrics" to "എഞ്ചിൻ മെട്രിക്സ്",
        "ramAllocation" to "റാം അലോക്കേഷൻ",
        "visualFidelity" to "വിഷ്വൽ ഫിഡിലിറ്റി",
        "interfaceLanguage" to "INTERFACE ഭാഷ",
        "applicationPreferences" to "അപ്ലിക്കേഷൻ മുൻഗണനകൾ",
        "migrationTools" to "മൈഗ്രേഷൻ & ഇമ്പോർട്ട്",
        "fluxVoice" to "ഫ്ലക്സ് വോയ്സ് എഞ്ചിൻ",
        "voiceDescription" to "സിംപിൾ വോയ്സ് ചാറ്റിന് പകരമുള്ള സംവിധാനം"
    ),
    "Hindi" to mapOf(
        "home" to "होम",
        "versions" to "संस्करण",
        "mods" to "मॉर्ड्स",
        "modpacks" to "मॉर्डपैक्स",
        "settings" to "सेटिंग्स",
        "profile" to "प्रोफाइल",
        "readyToLaunch" to "शुरू करने के लिए तैयार",
        "launchGame" to "खेल शुरू करें",
        "allocated" to "आवंटित",
        "security" to "सुरक्षा",
        "active" to "सक्रिय",
        "engineMetrics" to "इंजन मेट्रिक्स",
        "ramAllocation" to "रैम आवंटन",
        "visualFidelity" to "दृश्य निष्ठा",
        "interfaceLanguage" to "इंटरफेस भाषा",
        "applicationPreferences" to "एप्लिकेशन प्राथमिकताएं",
        "migrationTools" to "माइग्रेशन और इम्पोर्ट",
        "fluxVoice" to "फ्लक्स वॉयस इंजन",
        "voiceDescription" to "सिंपल वॉयस चैट का मूल विकल्प"
    ),
    "Spanish" to mapOf(
        "home" to "Inicio",
        "versions" to "Versiones",
        "mods" to "Mods",
        "modpacks" to "Modpacks",
        "settings" to "Ajustes",
        "profile" to "Perfil",
        "readyToLaunch" to "Listo para lanzar",
        "launchGame" to "Iniciar Juego",
        "allocated" to "Asignado",
        "security" to "Seguridad",
        "active" to "Activo",
        "engineMetrics" to "Métricas del Motor",
        "ramAllocation" to "Asignación de RAM",
        "visualFidelity" to "Fidelidad Visual",
        "interfaceLanguage" to "Idioma de Interfaz",
        "applicationPreferences" to "Preferencias de Aplicación",
        "migrationTools" to "Migración e Importación",
        "fluxVoice" to "Motor de Voz Flux",
        "voiceDescription" to "Alternativa nativa a Simple Voice Chat"
    ),
    "French" to mapOf(
        "home" to "Accueil",
        "versions" to "Versions",
        "mods" to "Mods",
        "modpacks" to "Modpacks",
        "settings" to "Paramètres",
        "profile" to "Profil",
        "readyToLaunch" to "Prêt à lancer",
        "launchGame" to "Lancer le Jeu",
        "allocated" to "Alloué",
        "security" to "Sécurité",
        "active" to "Actif",
        "engineMetrics" to "Mesures du Moteur",
        "ramAllocation" to "Allocation RAM",
        "visualFidelity" to "Fidélité Visuelle",
        "interfaceLanguage" to "Langue de l'Interface",
        "applicationPreferences" to "Préférences de l'Application",
        "migrationTools" to "Migration & Import",
        "fluxVoice" to "Moteur Vocal Flux",
        "voiceDescription" to "Alternative native à Simple Voice Chat"
    )
)

fun t(key: String): String {
    val lang = GlobalState.language
    val dict = translations[lang] ?: translations["English (US)"]!!
    return dict[key] ?: key
}
