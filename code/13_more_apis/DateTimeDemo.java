import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/*
 * TOPIC: java.time - LocalDate/Time/DateTime, Zoned, Instant, Period vs Duration, ChronoUnit,
 *        TemporalAdjusters, formatting/parsing, immutability, legacy interop
 * RUN:   java code/13_more_apis/DateTimeDemo.java
 */
public class DateTimeDemo {

    public static void main(String[] args) {
        section("1. Creating dates and times");
        LocalDate today = LocalDate.now();
        LocalDate dob = LocalDate.of(1998, Month.AUGUST, 15);        // months are 1-12
        LocalDate xmas = LocalDate.parse("2026-12-25");
        LocalTime opens = LocalTime.of(9, 30);
        LocalDateTime meeting = LocalDateTime.of(2026, 11, 3, 15, 0);
        System.out.println("today=" + today + ", dob=" + dob + ", xmas=" + xmas + ", opens=" + opens + ", meeting=" + meeting);

        section("2. Immutability - every 'change' returns a NEW object");
        LocalDate d = LocalDate.of(2026, 1, 31);
        d.plusDays(1);                                               // result ignored!
        System.out.println("after d.plusDays(1) without assignment: " + d);
        System.out.println("plusMonths(1) from Jan 31 -> " + d.plusMonths(1) + " (clamped to the end of Feb)");
        System.out.println("chain: " + d.plusYears(1).minusDays(10).withDayOfMonth(1));

        section("3. Querying");
        LocalDate fixed = LocalDate.of(2024, 2, 29);
        System.out.println(fixed + " is a " + fixed.getDayOfWeek() + ", leap year=" + fixed.isLeapYear()
                + ", days in month=" + fixed.lengthOfMonth() + ", day of year=" + fixed.getDayOfYear());
        System.out.println("dob before xmas? " + dob.isBefore(xmas));

        section("4. Period (date-based) vs Duration (time-based)");
        LocalDate ref = LocalDate.of(2026, 10, 27);
        Period age = Period.between(dob, ref);
        System.out.println("age on " + ref + ": " + age.getYears() + " years, " + age.getMonths() + " months, " + age.getDays() + " days (" + age + ")");
        System.out.println("days from " + ref + " to xmas: " + ChronoUnit.DAYS.between(ref, xmas));
        System.out.println("weeks between: " + ChronoUnit.WEEKS.between(ref, xmas));
        Duration work = Duration.between(LocalTime.of(9, 15), LocalTime.of(18, 45));
        System.out.println("workday length: " + work + " = " + work.toHours() + "h " + work.toMinutesPart() + "m");
        System.out.println("Duration.ofMinutes(135) = " + Duration.ofMinutes(135));

        section("5. TemporalAdjusters");
        System.out.println("first day of month : " + ref.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("last day of month  : " + ref.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.println("next Friday        : " + ref.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)));
        System.out.println("last Sunday of year: " + ref.with(TemporalAdjusters.lastDayOfYear()).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        YearMonth cardExpiry = YearMonth.of(2027, 3);
        System.out.println("card expires " + cardExpiry + ", last valid day " + cardExpiry.atEndOfMonth());

        section("6. Formatting and parsing (DateTimeFormatter is thread-safe)");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        String text = meeting.format(fmt);
        System.out.println("formatted : " + text);
        System.out.println("parsed    : " + LocalDateTime.parse(text, fmt));
        System.out.println("ISO date  : " + meeting.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("custom    : " + ref.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        try {
            LocalDate.parse("31/02/2026", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("invalid date -> DateTimeParseException: " + e.getMessage());
        }

        section("7. Time zones and Instant");
        ZonedDateTime india = ZonedDateTime.of(meeting, ZoneId.of("Asia/Kolkata"));
        ZonedDateTime newYork = india.withZoneSameInstant(ZoneId.of("America/New_York"));
        ZonedDateTime london = india.withZoneSameInstant(ZoneId.of("Europe/London"));
        System.out.println("India   : " + india);
        System.out.println("NewYork : " + newYork);
        System.out.println("London  : " + london);
        System.out.println("same instant? " + india.toInstant().equals(newYork.toInstant()));
        Instant start = Instant.now();
        long sum = 0;
        for (int i = 0; i < 5_000_000; i++) sum += i;
        System.out.println("Instant-based timing: loop took " + Duration.between(start, Instant.now()).toMillis() + " ms (sum " + sum + ")");
        System.out.println("epoch millis now: " + Instant.now().toEpochMilli());

        section("8. Interop with legacy java.util.Date");
        Date legacy = Date.from(india.toInstant());
        System.out.println("legacy Date: " + legacy);
        System.out.println("back to LocalDateTime in IST: " + LocalDateTime.ofInstant(legacy.toInstant(), ZoneId.of("Asia/Kolkata")));
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
