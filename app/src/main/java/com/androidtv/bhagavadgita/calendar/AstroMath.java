package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDateTime;

/** Shared astronomical building blocks: Julian Day, Sun longitude, Moon longitude. */
class AstroMath {

    static double toJulianDay(LocalDateTime utc) {
        int y = utc.getYear(), m = utc.getMonthValue(), d = utc.getDayOfMonth();
        int a = (14 - m) / 12;
        int yy = y + 4800 - a;
        int mm = m + 12 * a - 3;
        long jdn = d + (153L * mm + 2) / 5 + 365L * yy + yy / 4 - yy / 100 + yy / 400 - 32045;
        double frac = (utc.getHour() + utc.getMinute() / 60.0 + utc.getSecond() / 3600.0) / 24.0;
        return jdn + frac - 0.5;
    }

    static double julianCentury(double jd) {
        return (jd - 2451545.0) / 36525.0;
    }

    static double normalize(double deg) {
        deg = deg % 360;
        return deg < 0 ? deg + 360 : deg;
    }

    /** Sun's true geometric (tropical) ecliptic longitude, in degrees. */
    static double sunLongitude(double T) {
        double L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T * T;
        double M = Math.toRadians(357.52911 + 35999.05029 * T - 0.0001537 * T * T);
        double C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * Math.sin(M)
                + (0.019993 - 0.000101 * T) * Math.sin(2 * M)
                + 0.000289 * Math.sin(3 * M);
        return normalize(L0 + C);
    }

    /** Sun's mean longitude (needed separately for equation-of-time in sunrise calc). */
    static double sunMeanLongitude(double T) {
        return normalize(280.46646 + 36000.76983 * T + 0.0003032 * T * T);
    }

    // {D, M, M', F, coefficient x1e-6 deg} — Meeus Table 47.A main terms
    private static final int[][] MOON_TERMS = {
            {0,0,1,0,6288774},{2,0,-1,0,1274027},{2,0,0,0,658314},{0,0,2,0,213618},
            {0,1,0,0,-185116},{0,0,0,2,-114332},{2,0,-2,0,58793},{2,-1,-1,0,57066},
            {2,0,1,0,53322},{2,-1,0,0,45758},{0,1,-1,0,-40923},{1,0,0,0,-34720},
            {0,1,1,0,-30383},{2,0,2,0,15327},{0,0,1,-2,-12528},{0,0,1,2,10980},
            {4,0,-1,0,10675},{0,0,3,0,10034},{4,0,-2,0,8548},{2,1,-1,0,-7888},
            {2,1,0,0,-6766},{1,0,-1,0,-5163},{1,1,0,0,4987},{2,-1,1,0,4036},
            {2,0,3,0,3994},{4,0,0,0,3861},{2,0,-3,0,3665}
    };

    /** Moon's true (tropical) ecliptic longitude, in degrees. */
    static double moonLongitude(double T) {
        double Lp = 218.3164477 + 481267.88123421*T - 0.0015786*T*T + Math.pow(T,3)/538841 - Math.pow(T,4)/65194000;
        double D  = 297.8501921 + 445267.1114034*T - 0.0018819*T*T + Math.pow(T,3)/545868 - Math.pow(T,4)/113065000;
        double M  = 357.5291092 + 35999.0502909*T - 0.0001536*T*T + Math.pow(T,3)/24490000;
        double Mp = 134.9633964 + 477198.8675055*T + 0.0087414*T*T + Math.pow(T,3)/69699 - Math.pow(T,4)/14712000;
        double F  = 93.2720950 + 483202.0175233*T - 0.0036539*T*T - Math.pow(T,3)/3526000 + Math.pow(T,4)/863310000;
        double E = 1 - 0.002516*T - 0.0000074*T*T;

        double total = 0.0;
        for (int[] term : MOON_TERMS) {
            double arg = Math.toRadians(term[0]*D + term[1]*M + term[2]*Mp + term[3]*F);
            double eFactor = 1.0;
            if (Math.abs(term[1]) == 1) eFactor = E;
            else if (Math.abs(term[1]) == 2) eFactor = E * E;
            total += term[4] * eFactor * Math.sin(arg);
        }
        return normalize(Lp + total / 1_000_000.0);
    }

    /** Moon longitude minus Sun longitude, normalized 0-360. This IS the tithi angle. */
    static double moonSunDiff(LocalDateTime utc) {
        double T = julianCentury(toJulianDay(utc));
        return normalize(moonLongitude(T) - sunLongitude(T));
    }
}