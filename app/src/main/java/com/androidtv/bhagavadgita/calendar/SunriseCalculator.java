package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Computes local sunrise time (returned as UTC LocalDateTime) for a lat/lon on a given date. */
class SunriseCalculator {

    /**
     * @param date        local calendar date
     * @param latDeg      latitude in degrees (+N)
     * @param lonDeg      longitude in degrees (+E)
     * @param utcOffsetHr local timezone offset from UTC (e.g. 5.5 for IST)
     */
    static LocalDateTime getSunriseUtc(LocalDate date, double latDeg, double lonDeg, double utcOffsetHr) {
        // start from local midnight in UTC as first guess (used only to seed the JD iteration)
        LocalDateTime guessUtc = date.atStartOfDay().minusMinutes((long) (utcOffsetHr * 60));
        double jd = AstroMath.toJulianDay(guessUtc);

        // two refinement passes is enough for sub-minute convergence
        double fracOfDay = 0.25; // rough initial guess: sunrise near 06:00 local
        for (int i = 0; i < 3; i++) {
            double jdTry = jd + fracOfDay;
            fracOfDay = computeSunriseFraction(jdTry, latDeg, lonDeg);
        }

        // fracOfDay is the UTC time-of-day sunrise occurs on `date`'s own UTC calendar day.
        // Anchor it to date's actual UTC midnight — NOT "local midnight shifted into UTC" —
        // otherwise the result lands ~utcOffsetHr hours too early (the original bug).
        return date.atStartOfDay().plusSeconds((long) (fracOfDay * 86400));
    }

    /** Returns sunrise time as a fraction of the UTC day (0.0-1.0), given a JD estimate near sunrise. */
    private static double computeSunriseFraction(double jd, double latDeg, double lonDeg) {
        double T = AstroMath.julianCentury(jd);
        double trueLon = AstroMath.sunLongitude(T);
        double L0 = AstroMath.sunMeanLongitude(T);

        double eps = 23.439291 - 0.0130042 * T; // mean obliquity of ecliptic
        double lamR = Math.toRadians(trueLon);
        double epsR = Math.toRadians(eps);

        double decl = Math.asin(Math.sin(epsR) * Math.sin(lamR));
        double alpha = AstroMath.normalize(Math.toDegrees(Math.atan2(Math.cos(epsR) * Math.sin(lamR), Math.cos(lamR))));

        double E = 4 * (L0 - 0.0057183 - alpha); // equation of time, minutes
        while (E > 20) E -= 4 * 360;
        while (E < -20) E += 4 * 360;

        double latR = Math.toRadians(latDeg);
        double cosHA = (Math.cos(Math.toRadians(90.833)) - Math.sin(latR) * Math.sin(decl))
                / (Math.cos(latR) * Math.cos(decl));
        cosHA = Math.max(-1, Math.min(1, cosHA));
        double HA = Math.toDegrees(Math.acos(cosHA)); // degrees

        double solarNoonFrac = (720 - 4 * lonDeg - E) / 1440.0;
        return solarNoonFrac - HA * 4 / 1440.0;
    }
}