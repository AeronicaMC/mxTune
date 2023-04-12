package aeronicamc.mods.mxtune.util;

public interface IGroupClientChangedCallback
{
    enum Type { Group, Member, Pin }

    void onGroupClientChanged(Type type);
}
