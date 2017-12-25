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
import java.util.List;

import org.junit.Test;

import one.utopic.sparse.api.Anchor;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLType.Context;
import one.utopic.sparse.ebml.TestHelper.ByteArrayInput;
import one.utopic.sparse.ebml.TestHelper.ByteArrayOutput;
import one.utopic.sparse.ebml.reader.EBMLSignedLongReader;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLAnchorParserTest {

    private static final byte[] TEST = new byte[] { -1 };

    private static final byte[] UNMAPPED = new byte[] { -128 };

    private static final byte[] TEST_LONG = new byte[] { -101 };

    private final Context ctx = new Context();
    {
        ctx.newType("Unmapped", new EBMLCode(UNMAPPED));
    }

    private final EBMLType TEST_TYPE = ctx.newType("Test", new EBMLCode(TEST));

    private final EBMLType TEST_LONG_TYPE = ctx.newType("TestLong", new EBMLCode(TEST_LONG));

    private final Reader<EBMLParser, Long> READER_SIGNED_LONG = new EBMLSignedLongReader();

    @Test
    public void anchorParserReadAnchorTest() throws IOException {
        int deep = 3;
        ByteArrayOutput out = new ByteArrayOutput();
        {
            ByteArrayOutput outData = new ByteArrayOutput();
            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                writeValue(outData, TEST_LONG, intToBytes(i));
            }
            for (int c = 0; c < deep; c++) {
                ByteArrayOutput outWrapper = new ByteArrayOutput();
                writeValue(outWrapper, TEST, outData.getBytes());
                writeValue(outWrapper, UNMAPPED, "Hello".getBytes());
                outData = outWrapper;
            }
            writeValue(out, TEST, outData.getBytes());
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_LONG_TYPE);
            EBMLAnchorParser parser = new EBMLAnchorParser(new EBMLParser(in, ctx));
            Anchor<Long> anchor = parser.newAnchor(READER_SIGNED_LONG, path);
            List<Anchor<?>> result = parser.read(anchor);
            assertTrue(result.contains(anchor));
            List<Long> value = anchor.get();
            for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
                assertEquals(i, value.get(c).longValue());
            }
        }
        in = new ByteArrayInput(out.getBytes());
        {
            EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_LONG_TYPE);
            EBMLAnchorParser parser = new EBMLAnchorParser(new EBMLParser(in, ctx));
            Anchor<Long> anchor = parser.newAnchor(READER_SIGNED_LONG, path);
            List<Anchor<?>> result = parser.readAll();
            assertTrue(result.contains(anchor));
            List<Long> value = anchor.get();
            for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
                assertEquals(i, value.get(c).longValue());
            }
        }
    }

    private void writeValue(ByteArrayOutput out, byte[] code, byte[] ba) throws IOException {
        assertTrue(isCodeValid(code));
        for (int i = 0; i < code.length; i++) {
            out.writeByte(code[i]);
        }
        writeUnsignedCode(out, intToBytes(ba.length));
        for (int i = 0; i < ba.length; i++) {
            out.writeByte(ba[i]);
        }
    }
}
