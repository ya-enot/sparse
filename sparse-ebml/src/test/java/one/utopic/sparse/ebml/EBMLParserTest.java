/**
 * Copyright © 2017 Anton Filatov (ya-enot@mail.ru)
 *
 * This file is part of SParse.
 *
 * SParse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SParse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SParse.  If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package one.utopic.sparse.ebml;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLType.Context;
import one.utopic.sparse.ebml.TestHelper.ByteArrayInput;
import one.utopic.sparse.ebml.TestHelper.ByteArrayOutput;
import one.utopic.sparse.ebml.reader.EBMLSignedReader;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParserTest {

    private static final byte[] TEST = new byte[] { -1 };
    private Context ctx = new Context() {
        {
            newType("Test", new EBMLCode(new byte[] { -1 }));
        }
    };
    private static final Reader<EBMLParser, Long> READER_UNSIGNED = new EBMLSignedReader();

    @Test
    public void parserReadTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            writeValue(out, intToBytes(i));
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            EBMLParser parser = new EBMLParser(in, ctx);
            Long value = parser.read(READER_UNSIGNED);
            assertEquals(i, value.intValue());
            value = parser.read(READER_UNSIGNED);
            assertNull(value);
        }
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            Long value = parser.read(READER_UNSIGNED);
            assertNull(value);
            value = parser.read(READER_UNSIGNED);
            assertNull(value);
        }
    }

    @Test
    public void parserReadAllTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            writeValue(out, intToBytes(i));
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            int i = Short.MIN_VALUE;
            while (parser.hasNext()) {
                Long value = parser.read(READER_UNSIGNED);
                assertEquals(i++, value.intValue());
                value = parser.read(READER_UNSIGNED);
                assertNull(value);
                parser = new EBMLParser(in, ctx);
            }
        }
    }

    private void writeValue(ByteArrayOutput out, byte[] ba) throws IOException {
        writeUnsignedCheck(out, TEST);
        writeUnsignedCode(out, intToBytes(ba.length));
        for (int i = 0; i < ba.length; i++) {
            out.writeByte(ba[i]);
        }
    }
}
