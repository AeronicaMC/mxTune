package aeronicamc.mods.mxtune.util;

public interface IGroupClientChangedCallback
{
    enum Type { Group, Member, Pin, Close}

    void onGroupClientChanged(Type type);
}
