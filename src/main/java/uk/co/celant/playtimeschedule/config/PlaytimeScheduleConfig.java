package uk.co.celant.playtimeschedule.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;
import uk.co.celant.playtimeschedule.Schedule;
import uk.co.celant.playtimeschedule.exceptions.ScheduleParseException;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlaytimeScheduleConfig extends ConfigBase {
    protected ForgeConfigSpec configSpec;

    public final ForgeConfigSpec.IntValue dailyPlaytimeLimit;
    public final ForgeConfigSpec.BooleanValue freePlayOpBypass;
    public final ForgeConfigSpec.BooleanValue freePlayCreativeBypass;
    public final Map<Integer, List<Schedule>> freePlaySchedules = new HashMap<>();

    private final Map<Integer, ForgeConfigSpec.ConfigValue<List<? extends String>>> freePlayScheduleStrings = new HashMap<>();

    private static final Splitter DOT_SPLITTER = Splitter.on(".");
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlaytimeScheduleConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        dailyPlaytimeLimit = builder
                .comment("The time (in seconds) per day that a player can play (-1 = unlimited).")
                .defineInRange("playtime.limit.daily", 2 * 60 * 60, -1, Integer.MAX_VALUE);

        freePlayOpBypass = builder
                .comment("Whether ops should always be in free play mode.")
                .define("playtime.freeplay.opBypass", false);
        freePlayCreativeBypass = builder
                .comment("Whether players in Creative Mode should always be in free play mode.")
                .define("playtime.freeplay.creativeBypass", true);

        builder.comment(
                "Allows you to define 'free play' schedules, during which playtime limits are paused.",
                "Schedules must be formatted as pairs of ISO8601 times separated by '-'.",
                "Multiple schedules can be defined per day, but 'start' cannot be after 'end'.",
                "",
                "For example:",
                "[\"00:00:00-01:00:00\",\"17:00:00-23:59:59\"]"
        ).push("playtime.schedule");

        freePlayScheduleStrings.put(Calendar.MONDAY, builder
                .defineListAllowEmpty(
                        split("monday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.TUESDAY, builder
                .defineListAllowEmpty(
                        split("tuesday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.WEDNESDAY, builder
                .defineListAllowEmpty(
                        split("wednesday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.THURSDAY, builder
                .defineListAllowEmpty(
                        split("thursday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.FRIDAY, builder
                .defineListAllowEmpty(
                        split("friday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.SATURDAY, builder
                .defineListAllowEmpty(
                        split("saturday"),
                        ArrayList::new,
                        validSchedule()
                ));
        freePlayScheduleStrings.put(Calendar.SUNDAY, builder
                .defineListAllowEmpty(
                        split("sunday"),
                        ArrayList::new,
                        validSchedule()
                ));
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);

        Map<Integer, List<Schedule>> newMap = new HashMap<>();
        freePlayScheduleStrings.forEach((day, list) -> {
            List<Schedule> newList = new ArrayList<>();
            list.get().forEach((string) -> {
                try {
                    newList.add(new Schedule(string));
                } catch (ScheduleParseException e) {
                    throw new RuntimeException(e);
                }
            });
            newMap.put(day, newList);
        });
        freePlaySchedules.putAll(newMap);
    }

    private Predicate<Object> validSchedule() {
        return s -> {
            try {
                new Schedule(s.toString());
                return true;
            } catch (ScheduleParseException e) {
                throw new RuntimeException(e);
            }
        };
    }
    private static List<String> split(String path)
    {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }
}

