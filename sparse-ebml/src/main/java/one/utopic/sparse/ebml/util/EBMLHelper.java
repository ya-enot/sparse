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

    public static final long UNIX_EPOCH_DELAY = 978307200; // 2001/01/01 00:00:00 UTC
    public static final byte[] EMPTY = new byte[0];

    public static byte[] readTag(Input in) throws IOException {
        return readUnsignedCode(in, false);
    }

    public static int readLength(Input in) throws IOException {
        return bytesToInt(readUnsignedCode(in, true));
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
        if (result.length < 1) {
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
        if (ba.length < 1) {
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

    public static int getCodeLength(byte[] ba) {
        if (ba.length == 0) {
            return 1;
        }
        if (ba.length == 1 && ba[0] == -1) {
            return 1;
        }
        int overlap = ba.length % 7;
        if (getNumberOfLeadingZeros(ba[0]) < overlap) {
            overlap = 0;
        } else if (overlap != 0) {
            if (ba[0] == Byte.MAX_VALUE) {
                overlap = 0;
            } else {
                overlap = 1;
            }
        }
        return ba.length + ba.length / 7 + (ba.length % 7 > 0 ? 1 : 0) - overlap;
    }

    public static boolean isCodeValid(byte[] ba) {
        if (ba.length < 1) {
            return false;
        }
        int i = 0;
        byte snip = ba[i++];
        int size = 1;
        while (snip == 0 && i < ba.length) {
            size += 7 + 1; // Data 7 and 1 header byte
            snip = ba[i++];
        }
        size += getNumberOfLeadingZeros(snip);
        return size == ba.length;
    }

    private EBMLHelper() {
    }

    // Acts like a BigInteger, except that zero length byte array results in 0
    public static long bytesToLong(byte[] ba) throws IOException {
        if (ba.length < 1) {
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

    // Acts like a BigInteger, except that zero length byte array results in 0
    public static int bytesToInt(byte[] ba) throws IOException {
        if (ba.length < 1) {
            return 0;
        }
        return new BigInteger(ba).intValue();
    }

    // Acts like a BigInteger, except that 0 results in zero length byte array
    public static byte[] intToBytes(int value) throws IOException {
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
