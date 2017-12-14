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

import static one.utopic.sparse.ebml.TestHelper.*;
import static one.utopic.sparse.ebml.util.EBMLHelper.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;

public class EBMLHelperTest {

    private static final byte FF = (byte) 0xff;
    private static final byte[] ENCODED_LONG_MAX_VALUE = parseNormalizedHexBinary("00FF FFFF FFFF FFFF FF");

    @Test
    public void getNumberOfLeadingZerosTest() {
        {
            byte value = Byte.MIN_VALUE;
            for (int i = 0; i < Byte.BYTES * Byte.SIZE; i++) {
                assertEquals(Integer.numberOfLeadingZeros(value) - (i < 1 ? 0 : 24), getNumberOfLeadingZeros(value));
                value = (byte) ((0xff & value) >>> 1);
            }
        }
        {
            short value = Short.MIN_VALUE;
            for (int i = 0; i < Short.BYTES * Byte.SIZE; i++) {
                assertEquals(Integer.numberOfLeadingZeros(value) - (i < 1 ? 0 : 16), getNumberOfLeadingZeros(value));
                value = (byte) ((0xffff & value) >>> 1);
            }
        }
        {
            int value = Integer.MIN_VALUE;
            for (int i = 0; i < Integer.BYTES * Byte.SIZE; i++) {
                assertEquals(Integer.numberOfLeadingZeros(value), getNumberOfLeadingZeros(value));
                value >>>= 1;
            }
        }
        {
            long value = Long.MIN_VALUE;
            for (int i = 0; i < Long.BYTES * Byte.SIZE; i++) {
                assertEquals(Long.numberOfLeadingZeros(value), getNumberOfLeadingZeros(value));
                value >>>= 1;
            }
        }
    }

    @Test
    public void bytesToLongTest() throws IOException {
        for (int i = 1; i <= Long.BYTES; i++) {
            byte[] value = new byte[i];
            Arrays.fill(value, FF);
            assertEquals(-1, bytesToLong(value));
        }
        for (int i = 1; i <= Long.BYTES; i++) {
            byte[] value = new byte[i];
            Arrays.fill(value, (byte) 0);
            long result = bytesToLong(value);
            assertEquals(0, result);
            assertEquals(new BigInteger(value).longValue(), result);
        }
        for (int i = 1; i <= Long.BYTES; i++) {
            byte[] value = new byte[i];
            value[0] = Byte.MIN_VALUE;
            long result = bytesToLong(value);
            assertEquals((-1L << (i * 8) - 1), result);
            assertEquals(new BigInteger(value).longValue(), result);
        }
        assertEquals(0, bytesToLong(new byte[] {}));
    }

    @Test
    public void longToBytesTest() throws IOException {
        long maxValue = Long.MAX_VALUE;
        long minValue = Long.MIN_VALUE;
        for (int i = 1; i < Long.BYTES * Byte.SIZE; i++) {
            byte[] maxValueBytes = longToBytes(maxValue);
            byte[] minValueBytes = longToBytes(minValue);
            if (isVerbose()) {
                System.out.println(byteToBinaryString(maxValueBytes)); // TODO LOG
                System.out.println(byteToBinaryString(minValueBytes)); // TODO LOG
            }
            assertArrayEquals(BigInteger.valueOf(maxValue).toByteArray(), maxValueBytes);
            assertEquals(maxValue, bytesToLong(maxValueBytes));
            assertArrayEquals(BigInteger.valueOf(minValue).toByteArray(), minValueBytes);
            assertEquals(minValue, bytesToLong(minValueBytes));
            maxValue >>= 1;
            minValue >>= 1;
        }
        {
            byte[] maxValueBytes = longToBytes(maxValue);
            if (isVerbose()) {
                System.out.println(byteToBinaryString(maxValueBytes)); // TODO LOG
            }
            assertTrue(longToBytes(0).length == 0);
            assertEquals(maxValue, bytesToLong(maxValueBytes));
        }
        {
            byte[] minValueBytes = longToBytes(minValue);
            if (isVerbose()) {
                System.out.println(byteToBinaryString(minValueBytes)); // TODO LOG
            }
            assertArrayEquals(BigInteger.valueOf(minValue).toByteArray(), minValueBytes);
            assertEquals(minValue, bytesToLong(minValueBytes));
        }
    }

    @Test
    public void readEncodedTest() throws IOException {
        {
            ByteArrayInput input = new ByteArrayInput(ENCODED_LONG_MAX_VALUE);
            byte[] result = readUnsignedCode(input, true);
            assertEquals(Long.MAX_VALUE, new BigInteger(result).longValue());
        }
        {
            ByteArrayInput input = new ByteArrayInput(ENCODED_LONG_MAX_VALUE);
            byte[] result = readUnsignedCode(input, false);
            assertEquals(-1, new BigInteger(result).longValue());
        }
    }

    @Test
    public void writeAndReadEncodedTest() throws IOException {
        {
            BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
            BigInteger div = BigInteger.valueOf(2);
            for (int i = 0; i < Long.SIZE; i++) {
                value = value.multiply(div).add(BigInteger.ONE);
            }
            while (value.compareTo(BigInteger.ONE) >= 0 || value.compareTo(BigInteger.ZERO) == 0) {
                if (value.compareTo(BigInteger.ZERO) == 0) {
                    value = BigInteger.valueOf(-1);
                } else if (value.compareTo(BigInteger.ONE) == 0) {
                    value = BigInteger.ZERO;
                } else {
                    value = value.divide(div);
                }
                if (isVerbose()) {
                    System.out.println(value); // TODO LOG
                    System.out.println(byteToBinaryString(value.toByteArray())); // TODO LOG
                }
                ByteArrayOutput out = new ByteArrayOutput();
                int w = writeUnsignedCode(out, value.toByteArray());
                if (isVerbose()) {
                    System.out.println(byteToBinaryString(out.getBytes())); // TODO LOG
                }
                assertEquals(out.getBytes().length, w);
                byte[] bytes = readUnsignedCode(new ByteArrayInput(out.getBytes()), true);
                if (isVerbose()) {
                    System.out.println(byteToBinaryString(bytes)); // TODO LOG
                    System.out.println(new BigInteger(bytes)); // TODO LOG
                }
                assertArrayEquals(value.toByteArray(), bytes);
                assertEquals(value.toString(), new BigInteger(bytes).toString());
            }
        }
    }

    static class ByteArrayInput implements Input {

        private final byte[] data;
        private int pos;

        public ByteArrayInput(byte[] data) {
            this.data = data;
        }

        public boolean isFinished() {
            return pos < data.length;
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

    private static String byteToBinaryString(byte b) {
        String str = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        return str.isEmpty() ? "--------" : str;
    }

    private static String byteToBinaryString(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for (int i = 0; i < ba.length; i++) {
            sb.append(' ');
            sb.append(byteToBinaryString(ba[i]));
        }
        return sb.length() > 0 ? sb.substring(1) : "[      ]";
    }
}
