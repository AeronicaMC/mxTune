package aeronicamc.mods.mxtune.caps.venues;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.Map;

public class ToolState
{
    public static enum Type implements IStringSerializable
    {
        START("START"), END("END"), DONE("DONE");

        public static final Codec<ToolState.Type> CODEC = IStringSerializable.fromEnum(ToolState.Type::values, ToolState.Type::getFromKey);
        private final String serializationKey;
        private static final Map<String, ToolState.Type> REVERSE_LOOKUP = Util.make(Maps.newHashMap(), (p_222679_0_) -> {
            for(ToolState.Type toolState$type : values()) {
                p_222679_0_.put(toolState$type.serializationKey, toolState$type);
            }
        });

        @Override
        public String getSerializedName()
        {
            return this.serializationKey;
        }

        private Type(String serializationKey)
        {
            this.serializationKey = serializationKey;
        }

        @Nullable
        public static ToolState.Type getFromKey(String key) {
            return REVERSE_LOOKUP.get(key);
        }
    }
}
