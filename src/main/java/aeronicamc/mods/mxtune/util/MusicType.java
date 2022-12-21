package aeronicamc.mods.mxtune.util;

public enum MusicType
{
    UNDEFINED,
    PART,   // MML: Single part sheet music (Dynamic: MML@i=<IInstrument.getPatch>)
    SCORE   // MML: Music score containing multiple parts with preset instruments (Static: MML@i=<nnn>)
}
