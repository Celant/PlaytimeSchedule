package uk.co.celant.playtimeschedule.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPlaytimeCapability extends INBTSerializable<CompoundTag> {
    int getPlaytime();
    int getPlaytimeLeft();
    String getPlaytimeLeftString();
    void incrementPlaytime(boolean save);
    void resetPlaytime();
    boolean dayHasChanged();
    boolean getInFreePlay();
    void setInFreePlay(boolean value);
    void copy(IPlaytimeCapability playtimeCapability);
}
