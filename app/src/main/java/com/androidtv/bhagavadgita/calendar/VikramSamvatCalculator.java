package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Computes the Vikram Samvat year (Chaitradi reckoning — new year begins at
 * Chaitra Shukla Pratipada, typically late March).
 */
public class VikramSamvatCalculator {

    public static int getYear(LocalDateTime sunriseUtc, LocalDate localDate) {
        MasaCalculator.MasaResult masa = MasaCalculator.getMasa(sunriseUtc);
        TithiCalculator.Result tithi = TithiCalculator.getTithi(sunriseUtc);

        int gregorianYear = localDate.getYear();
        int month = localDate.getMonthValue();

        if (masa.masaIndex == 11) {
            // Chaitra masa itself straddles the year boundary:
            // Krishna paksha (tithi 16-30) = still old VS year,
            // Shukla paksha (tithi 1-15)  = new VS year has begun.
            return (tithi.tithiNumber <= 15) ? gregorianYear + 57 : gregorianYear + 56;
        }

        // Every other masa falls entirely after this Gregorian year's Chaitra
        // Shukla Pratipada if the Gregorian month is April-December (same year's
        // transition already happened), or before it if Jan-March (last year's
        // transition is the one that applies — e.g. Posh/Maha/Fagan in Jan-Mar
        // still belong to the VS year that began the previous March).
        int transitionYear = (month >= 4) ? gregorianYear : gregorianYear - 1;
        return transitionYear + 57;
    }
}