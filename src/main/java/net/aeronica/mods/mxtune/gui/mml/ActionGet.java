/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.gui.mml;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.aeronica.mods.mxtune.caches.Simularity.getModInstruments;
import static net.aeronica.mods.mxtune.caches.Simularity.getPackedPresetFromName;

public class ActionGet implements ISelectorAction
{
    public static final ActionGet INSTANCE = new ActionGet();

    private Path path;
    private String title;
    private String author;
    private String source;
    private String mml;
    private String instrument;
    private String suggestedInstrument;
    private int packedPatch;

    @Override
    public void select(Path path)
    {
        this.path = path;
        this.suggestedInstrument = getFileNameString();
        Tuple<Integer, String> suggested = getPackedPresetFromName(suggestedInstrument);
        this.instrument = suggested.getSecond();
    }

    @Override
    public void select(String title, String author, String source, String mml, String instrument)
    {
        this.title = title != null ? title : "";
        this.author = author != null ? author : "";
        this.source = source != null ? source : "";
        this.mml = mml != null ? mml : "";
        this.instrument = instrument != null ? I18n.format(instrument) : I18n.format(getModInstruments().get(0).getLangKey());
        Tuple<Integer, String> suggested = getPackedPresetFromName(this.instrument);
        this.packedPatch = suggested.getFirst();
        this.suggestedInstrument = I18n.format(suggested.getSecond());
    }

    public void clear()
    {
        path = null;
        title = "";
        author = "";
        source = "";
        mml = "";
    }

    public Path getFileName() { return path != null ? path.getFileName() : Paths.get(""); }

    public String getFileNameString() { return path != null ? (path.getFileName().toString()) : ""; }

    @Nullable
    public Path getPath() { return path; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getSource() { return source; }

    public String getMml() { return mml; }

    public String getInstrument() { return instrument; }

    public String getSuggestedInstrument() { return suggestedInstrument; }

    public int getPackedPatch() { return packedPatch; }

    public enum SELECTOR
    {
        FILE,
        PASTE,
        CANCEL
    }
}
