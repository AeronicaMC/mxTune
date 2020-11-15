/*
 * MIT License
 *
 * Copyright (c) 2020 Paul Boese a.k.a. Aeronica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.aeronica.libs.mml.parser;

import net.aeronica.libs.mml.util.DataByteBuffer;
import net.aeronica.libs.mml.util.IndexBuffer;

public class MMLLexer
{
    private int position = 0;
    private int elementIndex = 0;

    public MMLLexer() { /* NOP */ }

    public void parse(DataByteBuffer buffer, IndexBuffer elementBuffer)
    {
        this.position = 0;
        this.elementIndex = 0;

        for(; position < buffer.getLength(); position++)
        {
            switch (buffer.getByte(position))
            {
                case 'i':
                case 'I': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_INSTRUMENT, this.position); break;
                case 'p':
                case 'P': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_PERFORM, this.position); break;
                case 's':
                case 'S': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_SUSTAIN, this.position); break;
                case 't':
                case 'T': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_TEMPO, this.position); break;
                case 'o':
                case 'O': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_OCTAVE, this.position); break;
                case 'v':
                case 'V': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_VOLUME, this.position); break;
                case 'l':
                case 'L': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_LENGTH, this.position); break;

                case '>': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_OCTAVE_UP, this.position); break;
                case '<': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_OCTAVE_DOWN, this.position); break;

                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_NOTE, this.position); break;

                case '+':
                case '#': parseSharp(buffer, elementBuffer); break;
                case '-': parseFlat(buffer, elementBuffer); break;

                case 'n':
                case 'N': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_MIDI, this.position); break;

                case '.': parseDot(buffer, elementBuffer); break;

                case '&': parseTie(buffer, elementBuffer); break;

                case 'r':
                case 'R': setElementDataLength(elementBuffer, elementIndex++, ElementTypes.MML_REST, this.position); break;

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': parseNumberToken(buffer, elementBuffer); break;

                case 'M': parseMMLBegin(buffer, elementBuffer); break;
                case ',': parseChord(buffer, elementBuffer); break;
                case ';': parseEnd(buffer, elementBuffer); break;
                default: /* NOP -Ignore ALL other characters- */
            }
        }
        elementBuffer.setCount(this.elementIndex);
    }

    private void parseMMLBegin(DataByteBuffer buffer, IndexBuffer elementBuffer)
    {
        int tempPos = this.position;
        tempPos++;
        if (tempPos < buffer.getLength() && buffer.getByte(tempPos) == 'M')
        {
            tempPos++;
            if (tempPos < buffer.getLength() && buffer.getByte(tempPos) == 'L')
            {
                tempPos++;
                if (tempPos < buffer.getLength() && buffer.getByte(tempPos) == '@')
                {
                    tempPos++;
                    if (tempPos < buffer.getLength())
                    {
                        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_BEGIN, this.position, tempPos - this.position);
                        this.position = tempPos - 1; // -1 because the outer for-loop adds 1 to the position too
                    }
                }
            }
        }
        elementIndex++;
    }

    private void parseNumberToken(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfNumberFound = false;
        while(!isEndOfNumberFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            switch(buffer.getByte(tempPos)){
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': break;

                default: isEndOfNumberFound = true;
            }
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_NUMBER, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseSharp(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            switch(buffer.getByte(tempPos)){
                case '+':
                case '#': break;

                default: isEndOfRunFound = true;
            }
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_SHARP, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseFlat(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            if (buffer.getByte(tempPos) != '-')
                isEndOfRunFound = true;
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_FLAT, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseDot(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            if (buffer.getByte(tempPos) != '.')
                isEndOfRunFound = true;
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_DOT, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseTie(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            if (buffer.getByte(tempPos) != '&')
                isEndOfRunFound = true;
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_TIE, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseChord(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            if (buffer.getByte(tempPos) != ',')
                isEndOfRunFound = true;
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_CHORD, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void parseEnd(DataByteBuffer buffer, IndexBuffer elementBuffer) {
        int tempPos = this.position;
        boolean isEndOfRunFound = false;
        while(!isEndOfRunFound) {
            tempPos++;
            if (tempPos >= buffer.getLength()) break;
            if (buffer.getByte(tempPos) != ';')
                isEndOfRunFound = true;
        }
        setElementData(elementBuffer, this.elementIndex, ElementTypes.MML_END, this.position, tempPos - this.position);
        this.position = tempPos -1; // -1 because the outer for-loop adds 1 to the position too
        elementIndex++;
    }

    private void setElementDataLength(IndexBuffer elementBuffer, int index, byte type, int position) {
        elementBuffer.setType(index, type);
        elementBuffer.setPosition(index, position);
        elementBuffer.setLength(index, 1);
    }

    private void setElementData(IndexBuffer elementBuffer, int index, byte type, int position, int length) {
        elementBuffer.setType(index, type);
        elementBuffer.setPosition(index, position);
        elementBuffer.setLength(index, length);
    }
}
