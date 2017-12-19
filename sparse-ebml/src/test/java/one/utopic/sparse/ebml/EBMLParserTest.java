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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLType.Context;
import one.utopic.sparse.ebml.TestHelper.ByteArrayInput;
import one.utopic.sparse.ebml.TestHelper.ByteArrayOutput;
import one.utopic.sparse.ebml.reader.EBMLByteReader;
import one.utopic.sparse.ebml.reader.EBMLListReader;
import one.utopic.sparse.ebml.reader.EBMLMappedReader;
import one.utopic.sparse.ebml.reader.EBMLSignedIntegerReader;
import one.utopic.sparse.ebml.reader.EBMLSignedLongReader;
import one.utopic.sparse.ebml.reader.EBMLStringReader;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParserTest {

    private static final byte[] TEST = new byte[] { -1 };

    private static final byte[] TEST_INT = new byte[] { -100 };

    private static final byte[] TEST_LONG = new byte[] { -101 };

    private final Context ctx = new Context();

    private final EBMLType TEST_TYPE = ctx.newType("Test", new EBMLCode(TEST));

    private final EBMLType TEST_INT_TYPE = ctx.newType("TestInt", new EBMLCode(TEST_INT));
    private final EBMLType TEST_LONG_TYPE = ctx.newType("TestLong", new EBMLCode(TEST_LONG));

    private final Reader<EBMLParser, String> READER_STRING = new EBMLStringReader(new EBMLByteReader());

    private final Reader<EBMLParser, Long> READER_SIGNED_LONG = new EBMLSignedLongReader();
    private final Reader<EBMLParser, Integer> READER_SIGNED_INTEGER = new EBMLSignedIntegerReader();

    private final Reader<EBMLParser, List<Long>> READER_LIST_SIGNED_LONG = new EBMLListReader<Long>(TEST_TYPE,
            READER_SIGNED_LONG);

    private final Reader<EBMLParser, Number> READER_SIGNED_NUMBER;
    {
        Map<EBMLType, Reader<EBMLParser, ? extends Number>> readerMap = new HashMap<EBMLType, Reader<EBMLParser, ? extends Number>>();
        readerMap.put(TEST_INT_TYPE, READER_SIGNED_LONG);
        readerMap.put(TEST_LONG_TYPE, READER_SIGNED_INTEGER);
        READER_SIGNED_NUMBER = new EBMLMappedReader<Number>(readerMap);
    }

    @Test
    public void parserReadTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            writeValue(out, TEST, intToBytes(i));
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            EBMLParser parser = new EBMLParser(in, ctx);
            Long value = READER_SIGNED_LONG.read(parser);
            assertEquals(i, value.intValue());
            value = READER_SIGNED_LONG.read(parser);
            assertNull(value);
        }
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            Long value = READER_SIGNED_LONG.read(parser);
            assertNull(value);
            value = READER_SIGNED_LONG.read(parser);
            assertNull(value);
        }
    }

    @Test
    public void parserReadAllTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            writeValue(out, TEST, intToBytes(i));
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            int i = Short.MIN_VALUE;
            while (parser.hasNext()) {
                Long value = READER_SIGNED_LONG.read(parser);
                assertEquals(i++, value.intValue());
                value = READER_SIGNED_LONG.read(parser);
                assertNull(value);
                parser = new EBMLParser(in, ctx);
            }
        }
    }

    @Test
    public void parserReadArrayTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        {
            ByteArrayOutput outData = new ByteArrayOutput();
            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                writeValue(outData, TEST, intToBytes(i));
            }
            writeValue(out, TEST, outData.getBytes());
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            while (parser.hasNext()) {
                List<Long> value = READER_LIST_SIGNED_LONG.read(parser);
                for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
                    assertEquals(i, value.get(c).longValue());
                }
                value = READER_LIST_SIGNED_LONG.read(parser);
                assertNull(value);
                parser = new EBMLParser(in, ctx);
            }
        }
    }

    @Test
    public void parserReadMappedTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            writeValue(out, (i % 2 == 0 ? TEST_INT : TEST_LONG), intToBytes(i));
        }
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            int i = Short.MIN_VALUE;
            while (parser.hasNext()) {
                Number value = READER_SIGNED_NUMBER.read(parser);
                assertEquals((i++ % 2 == 0 ? Long.class : Integer.class), value.getClass());
                value = READER_SIGNED_NUMBER.read(parser);
                assertNull(value);
                parser = new EBMLParser(in, ctx);
            }
        }
    }

    @Test
    public void parserReadStringTest() throws IOException {
        ByteArrayOutput out = new ByteArrayOutput();
        writeValue(out, TEST, "Hello dolly! How are you?".getBytes());
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            while (parser.hasNext()) {
                String value = READER_STRING.read(parser);
                System.out.println(value);
                value = READER_STRING.read(parser);
                assertNull(value);
                parser = new EBMLParser(in, ctx);
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
