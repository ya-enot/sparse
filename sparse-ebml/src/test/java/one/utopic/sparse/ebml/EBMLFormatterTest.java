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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLType.RootContext;
import one.utopic.sparse.ebml.EBMLWriter.Part;
import one.utopic.sparse.ebml.TestHelper.ByteArrayInput;
import one.utopic.sparse.ebml.reader.EBMLListReader;
import one.utopic.sparse.ebml.reader.EBMLSignedLongReader;
import one.utopic.sparse.ebml.writer.EBMLCollectionWriter;
import one.utopic.sparse.ebml.writer.EBMLSignedLongWriter;

import static one.utopic.sparse.ebml.TestHelper.*;

public class EBMLFormatterTest {

    private static final byte[] TEST = new byte[] { -1 };

    private final RootContext ctx = new RootContext();

    private final EBMLType TEST_TYPE = ctx.newType("Test", new EBMLCode(TEST));

    private final EBMLWriter<EBMLFormatter, Long> LONG_WRITER = new EBMLSignedLongWriter();

    private final EBMLWriter<EBMLFormatter, Collection<Long>> COLLECTION_LONG_WRITER = new EBMLCollectionWriter<Long>(
            TEST_TYPE, LONG_WRITER);

    private final Reader<EBMLParser, Long> READER_SIGNED_LONG = new EBMLSignedLongReader();

    private final Reader<EBMLParser, List<Long>> READER_LIST_SIGNED_LONG = new EBMLListReader<Long>(TEST_TYPE,
            READER_SIGNED_LONG);

    @Test
    public void writerWriteArrayTest() throws IOException {
        ArrayList<Long> values = new ArrayList<Long>(Short.MAX_VALUE * 2 + 1);
        {
            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                values.add((long) i);
            }
        }
        ByteArrayOutput out = new ByteArrayOutput();
        EBMLFormatter formatter = new EBMLFormatter(out);
        Part<EBMLFormatter> part = COLLECTION_LONG_WRITER.prepare(values);
        formatter.newHeader(TEST_TYPE, part.getSize(formatter));
        part.write(formatter);
        ByteArrayInput in = new ByteArrayInput(out.getBytes());
        {
            EBMLParser parser = new EBMLParser(in, ctx);
            assertTrue(parser.hasNext());
            List<Long> value = READER_LIST_SIGNED_LONG.read(parser);
            parser.next();
            for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
                assertEquals(i, value.get(c).longValue());
            }
            assertFalse(parser.hasNext());
        }
    }

}
