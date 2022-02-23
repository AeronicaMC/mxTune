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
        START("START", "enum.mxtune.tool_state.type.start"),
        END("END", "enum.mxtune.tool_state.type.end"),
        DONE("DONE", "enum.mxtune.tool_state.type.done");

        public static final Codec<ToolState.Type> CODEC = IStringSerializable.fromEnum(ToolState.Type::values, ToolState.Type::getFromKey);
        private final String serializationKey;
        private final String translationKey;
        private static final Map<String, ToolState.Type> REVERSE_LOOKUP = Util.make(Maps.newHashMap(), (reverseHash) -> {
            for(ToolState.Type toolState$type : values()) {
                reverseHash.put(toolState$type.serializationKey, toolState$type);
            }
        });

        @Override
        public String getSerializedName()
        {
            return this.serializationKey;
        }

        Type(String serializationKey, String translationKey)
        {
            this.serializationKey = serializationKey;
            this.translationKey = translationKey;
        }

        public String getTranslationKey()
        {
            return translationKey;
        }

        @Nullable
        public static ToolState.Type getFromKey(String key) {
            return REVERSE_LOOKUP.get(key);
        }
    }
}
