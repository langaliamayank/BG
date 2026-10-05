package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vikram Samvat year (Chaitradi reckoning): the new year begins at
 * Chaitra Shukla Pratipada. Masa index follows MasaCalculator
 * (0 = Vaishakh ... 6 = Kartik, 7 = Margashirsh, 8 = Paush,
 *  9 = Magh, 10 = Phalgun, 11 = Chaitra).
 */
public class VikramSamvatCalculator {

    private static final int OFFSET = 57; // VS year = Gregorian year + 57 (after Chaitra Shukla)

    private static final int MARGASHIRSH = 7;
    private static final int PHALGUN = 10;
    private static final int CHAITRA = 11;

    public static int getYear(LocalDateTime sunriseUtc, LocalDate localDate) {
        MasaCalculator.MasaResult masa = MasaCalculator.getMasa(sunriseUtc);
        TithiCalculator.Result tithi = TithiCalculator.getTithi(sunriseUtc);

        int gYear = localDate.getYear();
        int month = localDate.getMonthValue();

        // 1. Chaitra: the new year starts at Shukla Pratipada.
        //    Krishna paksha = still the old year. An Adhik Chaitra is also old year,
        //    the new year starts with the Nija (regular) Chaitra.
        if (masa.masaIndex == CHAITRA) {
            boolean newYearStarted = tithi.tithiNumber <= 15 && !masa.isAdhik;
            return gYear + (newYearStarted ? OFFSET : OFFSET - 1);
        }

        // 2. Margashirsh..Phalgun (7-10) can fall in Dec or Jan-Mar.
        //    Jul-Dec -> this Gregorian year's Chaitra already happened.
        //    Jan-Jun -> the VS year began in the previous Gregorian year.
        if (masa.masaIndex >= MARGASHIRSH && masa.masaIndex <= PHALGUN) {
            int startYear = (month >= 7) ? gYear : gYear - 1;
            return startYear + OFFSET;
        }

        // 3. Vaishakh..Kartik (0-6) always come after that Gregorian year's
        //    Chaitra Shukla Pratipada.
        return gYear + OFFSET;
    }
}