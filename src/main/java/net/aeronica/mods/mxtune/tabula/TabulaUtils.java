/**
 * Copyright {2016} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.Gson;

import net.aeronica.mods.mxtune.tabula.components.TabulaCubeContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaModelContainer;

/**
 * Taken in part for the purpose of testing from:</br>
 * https://github.com/gegy1000/JurassiCraft2</br>
 * package org.jurassicraft.server.tabula;
 * 
 * TODO: make it load the texture too.
 * FIXME: Squash NPE! Load a default single cube from a string instead
 */
public class TabulaUtils
{

    public static final String EXT = ".tbl";
    
    public static TabulaModelContainer loadTabulaModel(String path) throws IOException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (!path.endsWith(TabulaUtils.EXT)) {
            path += TabulaUtils.EXT;
        }

        InputStream stream = TabulaUtils.class.getResourceAsStream(path);
        return TabulaUtils.loadTabulaModel(getModelJsonStream(path, stream));
    }

    public static TabulaModelContainer loadTabulaModel(InputStream stream) {
        return new Gson().fromJson(new InputStreamReader(stream), TabulaModelContainer.class);
    }

    private static InputStream getModelJsonStream(String name, InputStream file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file);
        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals("model.json")) {
                return zip;
            }
        }

        throw new RuntimeException("No model.json present in " + name);
    }
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("Test Begin\n");
        TabulaModelContainer model = loadTabulaModel("/assets/llibtest/models/block/test_animation.tbl");
        System.out.println("TabulaCanon: " + model);
        
        System.out.println("\n\nTest End");
    }
    
}
