package uk.co.celant.playtimeschedule;

import uk.co.celant.playtimeschedule.exceptions.ScheduleParseException;

import java.time.LocalTime;

public class Schedule {
    public final LocalTime from;
    public final LocalTime to;

    private static final String TIME_REGEX = "^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";

    public Schedule(String schedule) throws ScheduleParseException {
        var parts = schedule.split("-");
        if (parts.length != 2) throw new ScheduleParseException("Invalid schedule format");
        if (!parts[0].matches(TIME_REGEX)) throw new ScheduleParseException("Invalid 'from' timestamp");
        if (!parts[1].matches(TIME_REGEX)) throw new ScheduleParseException("Invalid 'to' timestamp");

        LocalTime from = LocalTime.parse(parts[0]);
        LocalTime to = LocalTime.parse(parts[1]);
        if (!to.isAfter(from)) throw new ScheduleParseException("'from' cannot be after 'to'");

        this.from = from;
        this.to = to;
    }

    public boolean isBetween(LocalTime target) {
        return (
                (target.isAfter(this.from) || target.equals(this.from)) &&
                (target.isBefore(this.to) || target.equals(this.to))
        );
    }

    public boolean isBetween() {
        return isBetween(LocalTime.now());
    }

    @Override
    public String toString() {
        return "Schedule{from="+from+", to="+to+"}";
    }
}
