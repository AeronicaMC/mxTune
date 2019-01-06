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

package net.aeronica.libs.mml.readers.ms2mml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class TestMs2Mml
{


    private static Logger LOGGER = LogManager.getLogger();

    static void test() throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance("net.aeronica.libs.mml.readers.ms2mml");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Ms2 ms2 = (Ms2) unmarshaller.unmarshal(getFile());

        LOGGER.info("Melody {}", ms2.getMelody().replace('\n', ' ').trim());
        for (Ms2.Chord chord : ms2.chord)
        {
            LOGGER.info("Chord {}: {}", chord.index, chord.value.replace('\n', ' ').trim());
        }
    }


    static FileInputStream getFile()
    {
        FileInputStream reader = null;
        try
        {
            reader = new FileInputStream("E:\\Users\\Paul\\Downloads\\darling-in-the-franxx-ed5-escape-r2518.ms2mml");
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return reader;
    }

    public static void main(String[] args) throws Exception
    {
        test();
    }
}
