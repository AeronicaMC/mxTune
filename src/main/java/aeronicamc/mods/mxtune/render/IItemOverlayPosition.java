package aeronicamc.mods.mxtune.render;

public interface IItemOverlayPosition {
    IOverlayItem.Position getPosition(IOverlayItem overlayItem);

    float getPercent(IOverlayItem overlayItem);
}
