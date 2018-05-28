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
 * Acts like a BigDecimal, except that zero length byte array results in 0
 */
public class BigDecimalFormat implements EBMLFormat<BigDecimal> {

    public static final BigDecimalFormat INSTANCE = new BigDecimalFormat();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public BigDecimal readFormat(byte[] data) {
        if (data.length < 1) {
            return BigDecimal.ZERO;
        }
        try {
            byte[] scaleRaw = readCodeStrip(data);
            int length = codeLength(scaleRaw);
            BigInteger value = new BigInteger(Arrays.copyOfRange(data, length, data.length));
            return new BigDecimal(value, new BigInteger(scaleRaw).intValue());
        } catch (IOException e) {
            throw new SparseReaderException(e);
        }
    }

    @Override
    public Writable getWritable(BigDecimal data) {
        byte[] scaleRaw = BigInteger.valueOf(data.scale()).toByteArray();
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
