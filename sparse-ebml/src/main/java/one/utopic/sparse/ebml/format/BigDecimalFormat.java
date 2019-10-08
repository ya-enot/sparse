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
package one.utopic.sparse.ebml.format;

import static one.utopic.sparse.ebml.EBMLFormatUtil.codeLength;
import static one.utopic.sparse.ebml.EBMLFormatUtil.readCode;
import static one.utopic.sparse.ebml.EBMLFormatUtil.readCodeStrip;
import static one.utopic.sparse.ebml.EBMLFormatUtil.writeCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.exception.SparseReaderException;
import one.utopic.sparse.ebml.EBMLFormat;

/**
 * Writes and reads Decimal numbers with scale
 */
public class BigDecimalFormat implements EBMLFormat<BigDecimal> {

    public static final BigDecimalFormat INSTANCE = new BigDecimalFormat();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public BigDecimal readFormat2(byte[] data) {
        if (data.length < 1) {
            return BigDecimal.ZERO;
        }
        try {
            byte[] scaleRawLength = readCode(data);
            int scaleLength = new BigInteger(readCodeStrip(scaleRawLength)).intValue();
            int scale = new BigInteger(Arrays.copyOfRange(data, scaleRawLength.length, scaleRawLength.length + scaleLength)).intValue();
            BigInteger value = new BigInteger(Arrays.copyOfRange(data, scaleRawLength.length + scaleLength, data.length));
            return new BigDecimal(value, scale);
        } catch (IOException e) {
            throw new SparseReaderException(e);
        }
    }

    public Writable getWritable2(BigDecimal data) {
        byte[] scaleRaw = BigInteger.valueOf(data.scale()).toByteArray();
        byte[] valueRaw = data.unscaledValue().toByteArray();
        return new Writable() {
            @Override
            public void writeFormat(Output out) throws IOException {
                writeCode(out, BigInteger.valueOf(scaleRaw.length).toByteArray());
                for (int i = 0; !out.isFinished() && i < scaleRaw.length; i++) {
                    out.writeByte(scaleRaw[i]);
                }
                for (int i = 0; !out.isFinished() && i < valueRaw.length; i++) {
                    out.writeByte(valueRaw[i]);
                }
            }

            @Override
            public int getSize() {
                return codeLength(scaleRaw) + valueRaw.length;
            }

        };
    }

    @Override
    public BigDecimal readFormat(byte[] data) {
        if (data.length < 1) {
            return BigDecimal.ZERO;
        }
        try {
            byte[] scaleRaw = readCodeStrip(data);
            long scale = new BigInteger(scaleRaw).longValueExact();
            scale = (1 & scale) == 0 ? scale >> 1 : -scale >> 1; // scale sign unpacking
            BigInteger value = new BigInteger(Arrays.copyOfRange(data, codeLength(scaleRaw), data.length));
            return new BigDecimal(value, (int) scale);
        } catch (IOException e) {
            throw new SparseReaderException(e);
        }
    }

    @Override
    public Writable getWritable(BigDecimal data) {
        long scale = data.scale();
        scale = scale >= 0 ? scale << 1 : -(scale << 1 | 1); // scale sign packing
        byte[] scaleRaw = BigInteger.valueOf(scale).toByteArray();
        byte[] valueRaw = data.unscaledValue().toByteArray();
        return new Writable() {

            @Override
            public void writeFormat(Output out) throws IOException {
                writeCode(out, scaleRaw);
                for (int i = 0; !out.isFinished() && i < valueRaw.length; i++) {
                    out.writeByte(valueRaw[i]);
                }
            }

            @Override
            public int getSize() {
                return codeLength(scaleRaw) + valueRaw.length;
            }

        };
    }

}
