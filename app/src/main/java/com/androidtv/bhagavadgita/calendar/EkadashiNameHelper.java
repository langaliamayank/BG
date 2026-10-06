package com.androidtv.bhagavadgita.calendar;

/**
 * Nathdwara Tippani aani Pushtimarg kramanusar Ekadashi chi nave[cite: 4].
 * Masa index MasaCalculator nusar:
 * 0 = Vaishakh, 1 = Jeth, 2 = Ashadh, 3 = Shravan, 4 = Bhadarvo, 5 = Aaso,
 * 6 = Kartak, 7 = Magshar, 8 = Posh, 9 = Maha, 10 = Fagan, 11 = Chaitra[cite: 4]
 */
public class EkadashiNameHelper {

    // ==========================================
    // 1. ENGLISH (PUSHTIMARG TIPPANI EXACT MATCH)
    // ==========================================
    private static final String[] SHUKLA_EN = {
            "Mohini Ekadashi",                    // 0: Vaishakh (27 Apr)
            "Nirjala Ekadashi",                   // 1: Jeth (25 Jun)
            "Devshayani Ekadashi",                // 2: Ashadh (25 Jul)
            "Putrada / Pavitra Ekadashi",         // 3: Shravan (24 Aug)
            "Parivartini / Jaljhulani Ekadashi",  // 4: Bhadarvo (22 Sep)
            "Papankusha Ekadashi",                // 5: Aaso (22 Oct)
            "Prabodhini Ekadashi",                // 6: Kartak (21 Nov)
            "Mokshada Ekadashi",                  // 7: Magshar (20 Dec)
            "Putrada Ekadashi",                   // 8: Posh
            "Jaya Ekadashi",                      // 9: Maha (29 Jan)
            "Amalaki / Kunj Ekadashi",            // 10: Fagan (27 Feb)[cite: 11]
            "Kamada Ekadashi"                     // 11: Chaitra (29 Mar)
    };

    // Ashadh Vad la Yogini, Shravan Vad la Kamika, Bhadarvo Vad la Aja
    private static final String[] KRISHNA_EN = {
            "Varuthini Ekadashi",                 // 0: Vaishakh (13 Apr)
            "Apara Ekadashi",                     // 1: Jeth (13 May)
            "Yogini Ekadashi",                    // 2: Ashadh (11 Jul)
            "Kamika Ekadashi",                    // 3: Shravan (09 Aug)
            "Aja Ekadashi",                       // 4: Bhadarvo (07 Sep)
            "Indira Ekadashi",                    // 5: Aaso (06 Oct)
            "Rama Ekadashi",                      // 6: Kartak (05 Nov)
            "Utpanna / Utpatti Ekadashi",         // 7: Magshar (04 Dec)
            "Saphala Ekadashi",                   // 8: Posh
            "Shattila Ekadashi",                  // 9: Maha (14 Jan)[cite: 11]
            "Vijaya Ekadashi",                    // 10: Fagan (13 Feb)[cite: 11]
            "Papamochani Ekadashi"                // 11: Chaitra (15 Mar)
    };

    // ==========================================
    // 2. HINDI (पुष्टिमार्गीय नाथद्वारा नाम)
    // ==========================================
    private static final String[] SHUKLA_HI = {
            "मोहिनी एकादशी",                      // 0: Vaishakh
            "निर्जला एकादशी",                     // 1: Jeth
            "देवशयनी एकादशी",                    // 2: Ashadh
            "पुत्रदा / पवित्रा एकादशी",           // 3: Shravan
            "परिवर्तिनी / जलझूलनी एकादशी",        // 4: Bhadarvo
            "पापांकुशा एकादशी",                   // 5: Aaso
            "प्रबोधिनी एकादशी",                   // 6: Kartak
            "मोक्षदा एकादशी",                     // 7: Magshar
            "पुत्रदा एकादशी",                     // 8: Posh
            "जया एकादशी",                         // 9: Maha
            "आमलकी / कुंज एकादशी",                // 10: Fagan
            "कामदा एकादशी"                        // 11: Chaitra
    };

    private static final String[] KRISHNA_HI = {
            "वरूथिनी एकादशी",                     // 0: Vaishakh
            "अपरा एकादशी",                        // 1: Jeth
            "योगिनी एकादशी",                      // 2: Ashadh
            "कामिका एकादशी",                      // 3: Shravan
            "अजा एकादशी",                         // 4: Bhadarvo
            "इन्दिरा एकादशी",                     // 5: Aaso
            "रमा एकादशी",                         // 6: Kartak
            "उत्पत्ति / उत्पन्ना एकादशी",         // 7: Magshar
            "सफला एकादशी",                        // 8: Posh
            "षट्तिला एकादशी",                     // 9: Maha
            "विजया एकादशी",                       // 10: Fagan
            "पापमोचिनी एकादशी"                    // 11: Chaitra
    };

    // ==========================================
    // 3. GUJARATI (ગુજરાતી / ટિપ્પણી નામ)
    // ==========================================
    private static final String[] SHUKLA_GU = {
            "મોહિની અગિયારસ",                    // 0: Vaishakh
            "નિર્જળા અગિયારસ",                   // 1: Jeth
            "દેવશયની અગિયારસ",                  // 2: Ashadh
            "પુત્રદા / પવિત્રા અગિયારસ",          // 3: Shravan
            "પરિવર્તિની / જલઝૂલણી અગિયારસ",       // 4: Bhadarvo
            "પાપાંકુશા અગિયારસ",                 // 5: Aaso
            "પ્રબોધિની અગિયારસ",                 // 6: Kartak
            "મોક્ષદા અગિયારસ",                   // 7: Magshar
            "પુત્રદા અગિયારસ",                   // 8: Posh
            "જયા અગિયારસ",                       // 9: Maha
            "આમલકી / કુંજ અગિયારસ",              // 10: Fagan
            "કામદા અગિયારસ"                      // 11: Chaitra
    };

    private static final String[] KRISHNA_GU = {
            "વરૂથિની અગિયારસ",                    // 0: Vaishakh
            "અપરા અગિયારસ",                      // 1: Jeth
            "યોગિની અગિયારસ",                    // 2: Ashadh
            "કામિકા અગિયારસ",                    // 3: Shravan
            "અજા અગિયારસ",                       // 4: Bhadarvo
            "ઇન્દિરા અગિયારસ",                   // 5: Aaso
            "રમા અગિયારસ",                       // 6: Kartak
            "ઉત્પત્તિ / ઉત્પન્ના અગિયારસ",        // 7: Magshar
            "સફળા અગિયારસ",                      // 8: Posh
            "ષટતિલા અગિયારસ",                    // 9: Maha
            "વિજયા અગિયારસ",                     // 10: Fagan
            "પાપમોચની અગિયારસ"                   // 11: Chaitra
    };

    public static String getEkadashiName(int masaIndex, boolean isShukla, boolean isAdhik, Language lang) {
        if (lang == null) {
            lang = Language.ENGLISH;
        }

        // Adhik Maas (Purushottam Maas) chya 2 Ekadashis
        if (isAdhik) {
            switch (lang) {
                case HINDI:
                    return isShukla ? "कमला / पद्मिनी एकादशी" : "कमला / परमा एकादशी";
                case GUJARATI:
                    return isShukla ? "કમલા / પદ્મિની અગિયારસ" : "કમલા / પરમા અગિયારસ";
                case ENGLISH:
                default:
                    return isShukla ? "Kamala / Padmini Ekadashi" : "Kamala / Parama Ekadashi";
            }
        }

        if (masaIndex < 0 || masaIndex > 11) {
            switch (lang) {
                case HINDI: return "एकादशी";
                case GUJARATI: return "અગિયારસ";
                case ENGLISH:
                default: return "Ekadashi";
            }
        }

        switch (lang) {
            case HINDI:
                return isShukla ? SHUKLA_HI[masaIndex] : KRISHNA_HI[masaIndex];
            case GUJARATI:
                return isShukla ? SHUKLA_GU[masaIndex] : KRISHNA_GU[masaIndex];
            case ENGLISH:
            default:
                return isShukla ? SHUKLA_EN[masaIndex] : KRISHNA_EN[masaIndex];
        }
    }
}