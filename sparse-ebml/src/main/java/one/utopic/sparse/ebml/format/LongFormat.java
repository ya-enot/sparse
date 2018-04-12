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

import java.math.BigInteger;

import one.utopic.sparse.ebml.EBMLFormat;

/**
 * Acts like a BigInteger.longValue(), except that zero length byte array
 * results in 0
 */
public class LongFormat implements EBMLFormat<Long> {

    public static final LongFormat INSTANCE = new LongFormat();

    @Override
    public Long readFormat(byte[] data) {
        if (data.length < 1) {
            return 0L;
        }
        return new BigInteger(data).longValue();
    }

    @Override
    public byte[] writeFormat(Long data) {
        if (null == data || data == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        return BigInteger.valueOf(data).toByteArray();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
