package uk.co.celant.playtimeschedule.capabilities;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import uk.co.celant.playtimeschedule.PlaytimeSchedule;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Calendar;

public class PlaytimeCapability implements IPlaytimeCapability {
    private int curPlaytime = 0;
    private long dayMillis = System.currentTimeMillis();
    private long lastCheckMillis = System.currentTimeMillis();
    private boolean inFreePlay = false;

    @Override
    public int getPlaytime() {
        return this.curPlaytime;
    }

    @Override
    public int getPlaytimeLeft() {
        if (PlaytimeSchedule.CONFIG.dailyPlaytimeLimit.get() == -1) return -1;
        return Math.max(PlaytimeSchedule.CONFIG.dailyPlaytimeLimit.get() - getPlaytime(), 0);
    }

    @Override
    public String getPlaytimeLeftString() {
        int left = this.getPlaytimeLeft();

        String seconds = new DecimalFormat("00").format(Duration.ofSeconds(left).toSecondsPart());
        String minutes = new DecimalFormat("00").format(Duration.ofSeconds(left).toMinutesPart());
        String hours = new DecimalFormat("00").format(Duration.ofSeconds(left).toHoursPart());
        return ChatFormatting.GREEN + "You have "
                + ChatFormatting.RED + hours + ChatFormatting.GREEN + " hours, "
                + ChatFormatting.RED +  minutes + ChatFormatting.GREEN + " minutes and "
                + ChatFormatting.RED +  seconds + ChatFormatting.GREEN + " seconds left";
    }

    @Override
    public void incrementPlaytime(boolean save) {
        if (save) {
            this.curPlaytime = this.curPlaytime + deltaTime();
        }
        this.lastCheckMillis = System.currentTimeMillis();
    }

    @Override
    public void resetPlaytime() {
        this.dayMillis = System.currentTimeMillis();
        this.curPlaytime = 0;
    }

    @Override
    public boolean dayHasChanged() {
        long newDayMillis = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.dayMillis);
        int oldDay = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTimeInMillis(newDayMillis);
        int newDay = cal.get(Calendar.DAY_OF_MONTH);

        return oldDay != newDay;
    }

    @Override
    public boolean getInFreePlay() {
        return this.inFreePlay;
    }

    @Override
    public void setInFreePlay(boolean value) {
        this.inFreePlay = value;
    }

    @Override
    public void copy(IPlaytimeCapability playtimeCapability) {
        this.curPlaytime = playtimeCapability.getPlaytime();
        this.inFreePlay = playtimeCapability.getInFreePlay();
    }

    private int deltaTime() {
        long newCheckMillis = System.currentTimeMillis();
        long diff = newCheckMillis - this.lastCheckMillis;
        return Math.round(diff / 1000f);
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("curPlaytime", this.curPlaytime);
        tag.putLong("dayMillis", this.dayMillis);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

        this.curPlaytime = nbt.getInt("curPlaytime");
        this.dayMillis = nbt.getLong("dayMillis");
    }
}
