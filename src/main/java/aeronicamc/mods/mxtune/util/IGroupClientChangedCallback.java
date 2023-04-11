package aeronicamc.mods.mxtune.util;

public interface IGroupClientChangedCallback
{
    enum Type { Group, Member }

    void onGroupClientChanged(Type type);
}
