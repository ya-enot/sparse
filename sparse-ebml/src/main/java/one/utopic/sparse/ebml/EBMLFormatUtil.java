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

import java.io.IOException;
import java.util.Arrays;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;

public final class EBMLFormatUtil {

    public static byte[] readCodeStrip(byte[] in) throws IOException {
        return readCode(in, 0, true);
    }

    public static byte[] readCodeStrip(byte[] in, int offset) throws IOException {
        return readCode(in, offset, true);
    }

    public static byte[] readCode(byte[] in) throws IOException {
        return readCode(in, 0, false);
    }

    public static byte[] readCode(byte[] in, int offset) throws IOException {
        return readCode(in, offset, false);
    }

    public static byte[] readCode(byte[] in, boolean strip) throws IOException {
        return readCode(in, 0, strip);
    }

    public static byte[] readCode(byte[] in, int offset, boolean strip) throws IOException {
        return readCode(new Input() {

            int i = offset;

            @Override
            public byte readByte() throws IOException {
                return in[i++];
            }

            @Override
            public boolean isFinished() {
                return i >= in.length;
            }

        }, strip);
    }

    public static int writeCode(byte[] data, byte[] out) throws IOException {
        return writeCode(data, out, 0);
    }

    public static int writeCode(byte[] data, byte[] out, int offset) throws IOException {
        return writeCode(new Output() {

            int i = offset;

            @Override
            public void writeByte(byte b) throws IOException {
                out[i++] = b;
            }

            @Override
            public boolean isFinished() {
                return i < out.length;
            }

        }, data);
    }

    public static int codeLength(byte[] data) {
        if (data.length == 0) {
            return 1;
        }
        if (data.length == 1 && data[0] == -1) {
            return 1;
        }
        int overlap = data.length % 7;
        if (getNumberOfLeadingZeros(data[0]) < overlap) {
            overlap = 0;
        } else if (overlap != 0) {
            if (data[0] == Byte.MAX_VALUE) {
                overlap = 0;
            } else {
                overlap = 1;
            }
        }
        return data.length + data.length / 7 + (data.length % 7 > 0 ? 1 : 0) - overlap;
    }

    public static int readLength(byte[] data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("Input data should not be empty");
        }
        if (data.length == 1 && data[0] == -1) {
            return 1;
        }
        int i = 0, length = 1;
        for (int l; i < data.length; i++) {
            length += (l = getNumberOfLeadingZeros(data[i]));
            if (l < Byte.SIZE) {
                break;
            }
        }
        if (length > data.length + i) {
            throw new IllegalArgumentException("Input data size smaller than coded data length");
        }
        return length;
    }

    public static boolean isCodeValid(byte[] data) {
        if (data.length < 1) {
            return false;
        }
        int i = 0;
        byte snip = data[i++];
        int size = 1;
        while (snip == 0 && i < data.length) {
            size += 7 + 1; // Data 7 and 1 header byte
            snip = data[i++];
        }
        size += getNumberOfLeadingZeros(snip);
        return size == data.length;
    }

    private EBMLFormatUtil() {
    }

    public static byte[] readCode(Input in, boolean strip) throws IOException {
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

    public static int writeCode(Output out, byte[] data) throws IOException {
        if (data.length < 1) {
            out.writeByte(Byte.MIN_VALUE);
            return 1;
        }
        // -1 is a reserved code
        if (data.length == 1 && data[0] == -1) {
            out.writeByte((byte) -1);
            return 1;
        }
        int overlap = data.length % 7;
        int head = data.length / 7 - (overlap > 0 ? 0 : 1);
        for (int i = head; i > 0; i--) {
            out.writeByte((byte) 0);
        }
        if (overlap == 0) {
            out.writeByte((byte) 1);
        } else if (getNumberOfLeadingZeros(data[0]) < overlap) {
            out.writeByte((byte) (((0xff & Byte.MIN_VALUE) >>> overlap)));
            overlap = 0;
        } else {
            byte write = (byte) (data[0] | ((0xff & Byte.MIN_VALUE) << 1 >>> overlap));
            // -1 is a reserved code
            if (write == -1) {
                out.writeByte((byte) (((0xff & Byte.MIN_VALUE) >>> overlap)));
                overlap = 0;
            } else {
                out.writeByte(write);
                overlap = 1;
            }
        }
        for (int i = overlap; i < data.length; i++) {
            out.writeByte(data[i]);
        }
        return head + data.length + (1 - overlap);
    }

    // TODO implement optimized calculation instead of general one
    public static int getNumberOfLeadingZeros(byte b) {
        return getNumberOfLeadingZeros(b, Byte.SIZE);
    }

    public static int getNumberOfLeadingZeros(long value, int size) {
        value &= -1 >>> Long.SIZE - size;
        if (value == 0) {
            return size;
        }
        if (value < 0) {
            return 0;
        }
        int result = 1;
        for (int c = size / 2; c > 1; c /= 2) {
            if (value >>> size - c == 0) {
                result += c;
                value <<= c;
            }
        }
        return result - ((int) (value >>> size - 1));
    }

}
