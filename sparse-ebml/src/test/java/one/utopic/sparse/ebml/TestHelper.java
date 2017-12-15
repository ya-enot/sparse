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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;

@SuppressWarnings("restriction")
public final class TestHelper {

    private static final String TEST_VERBOSE_PROPERTY_NAME = "testVerbose";

    static byte[] parseNormalizedHexBinary(String string) {
        return DatatypeConverter.parseHexBinary(string.toUpperCase().replaceAll("[^0-9A-F]*", ""));
    }

    static boolean isVerbose() {
        return Boolean.getBoolean(TEST_VERBOSE_PROPERTY_NAME);
    }

    private TestHelper() {
    }

    static class ByteArrayInput implements Input {

        private final byte[] data;
        private int pos;

        public ByteArrayInput(byte[] data) {
            this.data = data;
        }

        public boolean isFinished() {
            return pos >= data.length;
        }

        public byte readByte() throws IOException {
            return data[pos++];
        }

    }

    static class ByteArrayOutput implements Output {

        private final ByteArrayOutputStream baos;

        public ByteArrayOutput() {
            this(new ByteArrayOutputStream());
        }

        public ByteArrayOutput(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void writeByte(byte b) throws IOException {
            baos.write(b);
        }

        public boolean isFinished() {
            return false;
        }

        public byte[] getBytes() {
            return baos.toByteArray();
        }

    }

    static String byteToBinaryString(byte b) {
        String str = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        return str.isEmpty() ? "--------" : str;
    }

    static String byteToBinaryString(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for (int i = 0; i < ba.length; i++) {
            sb.append(' ');
            sb.append(byteToBinaryString(ba[i]));
        }
        return sb.length() > 0 ? sb.substring(1) : "[      ]";
    }
}
