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

package aeronicamc.libs.mml.readers.ms2mml;

import aeronicamc.libs.mml.readers.AbstractMmlFileReader;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.nio.file.Path;

public class Ms2MmlReader extends AbstractMmlFileReader
{
    public Ms2MmlReader() { /* NOP*/ }

    @Override
    public String getFileExtension()
    {
        return "ms2mml";
    }

    @Override
    public boolean parseFile(@Nonnull Path path)
    {
        readMs2Mml(getFile(path));
        return !hasErrors();
    }

    @Override
    public boolean parseStream(InputStream is)
    {
        readMs2Mml(is);
        return !hasErrors();
    }

    @Override
    public String getMML()
    {
        return mml;
    }

    private void readMs2Mml(InputStream is)
    {
        Ms2 ms2;
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance("aeronicamc.libs.mml.readers.ms2mml");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            ms2 = (Ms2) unmarshaller.unmarshal(is);

            if (ms2 != null)
            {
                StringBuilder builder = new StringBuilder("MML@");
                String melodyValue = ms2.melody;
                if (!melodyValue.equals(""))
                    builder.append(melodyValue);

                if (ms2.chord != null)
                {
                    for (Ms2.Chord chord : ms2.chord)
                    {
                        String chordValue = chord.value;
                        if ((chordValue.equals("")))
                            continue;
                        builder.append(",");
                        builder.append(chordValue);
                    }
                }
                builder.append(";");
                mml = builder.toString();
            }
        } catch (JAXBException e)
        {
            addError(e.getLocalizedMessage());
            mml = "";
        }
    }
}
