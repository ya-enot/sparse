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
package one.utopic.sparse.ebml.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;

public final class EBMLHelper {

    private static final byte[] EMPTY = new byte[] {};

    public static byte[] readTag(Input in) throws IOException {
        return readUnsignedCode(in, false);
    }

    public static long readLength(Input in) throws IOException {
        return bytesToLong(readUnsignedCode(in, true));
    }

    public static byte[] readUnsignedCode(Input in) throws IOException {
        return readUnsignedCode(in, true);
    }

    public static byte[] readUnsignedCode(Input in, boolean strip) throws IOException {
        byte snip = in.readByte();
        // -1 is a reserved code
        if (snip == -1) {
            return new byte[] { snip };
        }
        int size = 1;
        while (snip == 0) {
            size += 7;
            snip = in.readByte();
        }
        int lz = getNumberOfLeadingZeros(snip);
        size += lz;
        if (strip) {
            snip &= Byte.MAX_VALUE >>> lz;
        }
        byte[] result = new byte[size];
        if (result.length == 0) {
            return result;
        }
        result[0] = snip;
        for (int i = 1; i < result.length; i++) {
            result[i] = in.readByte();
        }
        if (strip) {
            int start = 0;
            while (result[start] == 0 && start < result.length - 1 && result[start + 1] >= 0) {
                start += 1;
            }
            return Arrays.copyOfRange(result, start, result.length);
        }
        return result;
    }

    public static int writeUnsignedCode(Output out, byte[] ba) throws IOException {
        if (ba.length == 0) {
            out.writeByte(Byte.MIN_VALUE);
            return 1;
        }
        // -1 is a reserved code
        if (ba.length == 1 && ba[0] == -1) {
            out.writeByte((byte) -1);
            return 1;
        }
        int overlap = ba.length % 7;
        int head = ba.length / 7 - (overlap > 0 ? 0 : 1);
        for (int i = head; i > 0; i--) {
            out.writeByte((byte) 0);
        }
        if (overlap == 0) {
            out.writeByte((byte) 1);
        } else if (getNumberOfLeadingZeros(ba[0]) < overlap) {
            out.writeByte((byte) (((0xff & Byte.MIN_VALUE) >>> overlap)));
            overlap = 0;
        } else {
            byte write = (byte) (ba[0] | ((0xff & Byte.MIN_VALUE) << 1 >>> overlap));
            // -1 is a reserved code
            if (write == -1) {
                out.writeByte((byte) (((0xff & Byte.MIN_VALUE) >>> overlap)));
                overlap = 0;
            } else {
                out.writeByte(write);
                overlap = 1;
            }
        }
        for (int i = overlap; i < ba.length; i++) {
            out.writeByte(ba[i]);
        }
        return head + ba.length + (1 - overlap);
    }

    public static int writeUnsignedCheck(Output out, byte[] ba) throws IOException {
        if (!isCodeValid(ba)) {
            return 0;
        }
        int i = 0;
        for (; i < ba.length; i++) {
            out.writeByte(ba[i]);
        }
        return i;
    }

    public static boolean isCodeValid(byte[] ba) {
        return false;
    }

    private EBMLHelper() {
    }

    // Acts like a BigInteger, except that zero length byte array results in 0
    public static long bytesToLong(byte[] ba) throws IOException {
        if (ba.length == 0) {
            return 0;
        }
        return new BigInteger(ba).longValue();
    }

    // Acts like a BigInteger, except that 0 results in zero length byte array
    public static byte[] longToBytes(long value) throws IOException {
        if (value == 0) {
            return EMPTY;
        }
        return BigInteger.valueOf(value).toByteArray();
    }

    public static int getNumberOfLeadingZeros(byte b) {
        return getNumberOfLeadingZeros(b, Byte.SIZE);
    }

    public static int getNumberOfLeadingZeros(short s) {
        return getNumberOfLeadingZeros(s, Short.SIZE);
    }

    public static int getNumberOfLeadingZeros(int i) {
        return getNumberOfLeadingZeros(i, Integer.SIZE);
    }

    public static int getNumberOfLeadingZeros(long l) {
        return getNumberOfLeadingZeros(l, Long.SIZE);
    }

    private static int getNumberOfLeadingZeros(long l, int size) {
        l &= -1 >>> Long.SIZE - size;
        if (l == 0) {
            return size;
        }
        if (l < 0) {
            return 0;
        }
        int result = 1;
        for (int c = size / 2; c > 1; c /= 2) {
            if (l >>> size - c == 0) {
                result += c;
                l <<= c;
            }
        }
        return result - ((int) (l >>> size - 1));
    }

}
