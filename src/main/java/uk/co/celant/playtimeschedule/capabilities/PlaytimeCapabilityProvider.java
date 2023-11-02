package uk.co.celant.playtimeschedule.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.celant.playtimeschedule.PlaytimeSchedule;

public class PlaytimeCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(PlaytimeSchedule.MODID, "playtime_capability");

    private final IPlaytimeCapability backend = new PlaytimeCapability();
    private final LazyOptional<IPlaytimeCapability> playtimeCapabilityLazyOptional = LazyOptional.of(() -> backend);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PlaytimeSchedule.PLAYTIME.orEmpty(cap, this.playtimeCapabilityLazyOptional);
    }

    void invalidate() {
        this.playtimeCapabilityLazyOptional.invalidate();
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.deserializeNBT(nbt);
    }
}
