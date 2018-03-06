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
import java.util.List;
import java.util.Set;

import org.junit.Test;

import one.utopic.sparse.api.Anchor;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLType.RootContext;
import one.utopic.sparse.ebml.TestHelper.ByteArrayInput;
import one.utopic.sparse.ebml.TestHelper.ByteArrayOutput;
import one.utopic.sparse.ebml.reader.EBMLByteReader;
import one.utopic.sparse.ebml.reader.EBMLListReader;
import one.utopic.sparse.ebml.reader.EBMLSignedLongReader;
import one.utopic.sparse.ebml.reader.EBMLStringReader;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLAnchorReaderTest {

	private static final byte[] TEST = new byte[] { -1 };

	private static final byte[] TEST2 = new byte[] { -2 };

	private static final byte[] UNKNOWN = new byte[] { -128 };

	private static final byte[] UNMAPPED = new byte[] { -127 };

	private static final byte[] TEST_LONG = new byte[] { -101 };

	private final RootContext ctx = new RootContext();
	{
		ctx.newType("Unmapped", new EBMLCode(UNMAPPED));
	}

	private final EBMLType TEST_TYPE = ctx.newType("Test", new EBMLCode(TEST));
	private final EBMLType TEST2_TYPE = ctx.newType("Test2", new EBMLCode(TEST2));

	private final EBMLType TEST_LONG_TYPE = ctx.newType("TestLong", new EBMLCode(TEST_LONG));

	private final Reader<EBMLParser, Long> READER_SIGNED_LONG = new EBMLSignedLongReader();
	private final Reader<EBMLParser, String> READER_STRING = new EBMLStringReader(new EBMLByteReader());

	private final Reader<EBMLParser, List<Long>> READER_LIST_SIGNED_LONG = new EBMLListReader<Long>(TEST_LONG_TYPE, READER_SIGNED_LONG);

	@Test
	public void anchorParserReadAllTest() throws IOException {
		int deep = 3;
		List<List<String>> test2Check = new ArrayList<List<String>>();
		ByteArrayOutput out = new ByteArrayOutput();
		{
			ByteArrayOutput outData = new ByteArrayOutput();
			for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
				writeValue(outData, TEST_LONG, intToBytes(i));
			}
			for (int c = 0; c < deep; c++) {
				ArrayList<String> test2CheckList = new ArrayList<String>();
				test2Check.add(test2CheckList);
				String test2A = "A" + c;
				String test2B = "B" + (deep - c);
				test2CheckList.add(test2A);
				test2CheckList.add(test2B);
				ByteArrayOutput outWrapper = new ByteArrayOutput();
				writeValue(outWrapper, TEST2, (test2A).getBytes());
				writeValue(outWrapper, UNKNOWN, "Hello".getBytes());
				writeValue(outWrapper, TEST, outData.getBytes());
				writeValue(outWrapper, UNMAPPED, "Dolly".getBytes());
				writeValue(outWrapper, TEST2, (test2B).getBytes());
				outData = outWrapper;
			}
			writeValue(out, TEST, outData.getBytes());
		}
		ByteArrayInput in = new ByteArrayInput(out.getBytes());
		{
			EBMLParser parser = new EBMLParser(in, ctx);
			EBMLAnchorReader anchorParser = new EBMLAnchorReader();
			EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_LONG_TYPE);
			Anchor<Long> anchor = anchorParser.newAnchor(READER_SIGNED_LONG, path);
			anchorParser.newAnchor(READER_STRING, EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST2_TYPE));
			anchorParser.newAnchor(READER_STRING, EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST2_TYPE));
			anchorParser.newAnchor(READER_STRING, EBMLTypePath.typePath(TEST_TYPE, TEST2_TYPE));
			int c = 0;
			for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
				Anchor<?> result;
				while ((result = anchorParser.read(parser)) != null) {
					if (result.equals(anchor)) {
						break;
					}
				}
				List<Long> value = anchor.get();
				assertEquals(c + 1, value.size());
				assertEquals(i, value.get(c).longValue());
			}
			assertEquals(Short.MAX_VALUE * 2 + 2, c);
			{
				Anchor<?> result;
				List<List<?>> results = new ArrayList<List<?>>();
				while ((result = anchorParser.read(parser)) != null) {
					results.add(result.get());
				}
				assertEquals(test2Check, results);
			}
		}
		in = new ByteArrayInput(out.getBytes());
		{
			EBMLParser parser = new EBMLParser(in, ctx);
			EBMLAnchorReader anchorParser = new EBMLAnchorReader();
			EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST_LONG_TYPE);
			Anchor<Long> anchor = anchorParser.newAnchor(READER_SIGNED_LONG, path);
			EBMLTypePath path0 = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST_TYPE, TEST2_TYPE);
			EBMLTypePath path1 = EBMLTypePath.typePath(TEST_TYPE, TEST_TYPE, TEST2_TYPE);
			EBMLTypePath path2 = EBMLTypePath.typePath(TEST_TYPE, TEST2_TYPE);
			Anchor<String> anchor0 = anchorParser.newAnchor(READER_STRING, path0);
			Anchor<String> anchor1 = anchorParser.newAnchor(READER_STRING, path1);
			Anchor<String> anchor2 = anchorParser.newAnchor(READER_STRING, path2);
			Set<Anchor<?>> result = anchorParser.readAll(parser);
			{
				assertTrue(result.contains(anchor));
				assertEquals(4, result.size());
				List<Long> value = anchor.get();
				for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
					assertEquals(i, value.get(c).longValue());
				}
				assertEquals(Short.MAX_VALUE * 2 + 2, value.size());
			}
			{
				List<List<?>> results = new ArrayList<List<?>>();
				results.add(anchor0.get());
				results.add(anchor1.get());
				results.add(anchor2.get());
				assertEquals(test2Check, results);
			}
		}
	}

	@Test
	public void anchorParserReadRootTest() throws IOException {
		ByteArrayOutput out = new ByteArrayOutput();
		{
			ByteArrayOutput outData = new ByteArrayOutput();
			for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
				writeValue(outData, TEST_LONG, intToBytes(i));
			}
			writeValue(out, TEST, outData.getBytes());
		}
		ByteArrayInput in = new ByteArrayInput(out.getBytes());
		{
			EBMLParser parser = new EBMLParser(in, ctx);
			EBMLAnchorReader anchorParser = new EBMLAnchorReader();
			EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE);
			Anchor<List<Long>> anchor = anchorParser.newAnchor(READER_LIST_SIGNED_LONG, path);
			{
				Anchor<?> result = anchorParser.read(parser);
				assertNotNull(result);
				assertEquals(anchor, result);
			}
			{
				List<List<Long>> results = anchor.get();
				assertEquals(1, results.size());
				List<Long> value = results.get(0);
				assertEquals(Short.MAX_VALUE * 2 + 2, value.size());
				for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
					assertEquals(i, value.get(c).longValue());
				}
			}
		}
	}

	@Test
	public void anchorParserReadRootOverrideTest() throws IOException {
		ByteArrayOutput out = new ByteArrayOutput();
		{
			ByteArrayOutput outData = new ByteArrayOutput();
			for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
				writeValue(outData, TEST_LONG, intToBytes(i));
			}
			writeValue(out, TEST, outData.getBytes());
		}
		ByteArrayInput in = new ByteArrayInput(out.getBytes());
		{
			EBMLParser parser = new EBMLParser(in, ctx);
			EBMLAnchorReader anchorParser = new EBMLAnchorReader();
			EBMLTypePath path = EBMLTypePath.typePath(TEST_TYPE);
			EBMLTypePath path1 = EBMLTypePath.typePath(TEST_TYPE, TEST_LONG_TYPE);
			Anchor<List<Long>> anchor = anchorParser.newAnchor(READER_LIST_SIGNED_LONG, path);
			Anchor<Long> anchor1 = anchorParser.newAnchor(READER_SIGNED_LONG, path1);
			{
				Anchor<?> result = anchorParser.read(parser);
				assertNotNull(result);
				assertEquals(anchor, result);
			}
			{
				List<List<Long>> results = anchor.get();
				assertEquals(1, results.size());
				List<Long> value = results.get(0);
				assertEquals(0, value.size());
			}
			{
				List<Long> value = anchor1.get();
				assertEquals(Short.MAX_VALUE * 2 + 2, value.size());
				for (int c = 0, i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++, c++) {
					assertEquals(i, value.get(c).longValue());
				}
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
