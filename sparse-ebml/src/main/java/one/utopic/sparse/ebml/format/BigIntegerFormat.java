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
 * Writes and reads Integer numbers
 */
public class BigIntegerFormat implements EBMLFormat<BigInteger> {

    public static final BigIntegerFormat INSTANCE = new BigIntegerFormat();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public BigInteger readFormat(byte[] data) {
        if (data.length < 1) {
            return BigInteger.ZERO;
        }
        return new BigInteger(data);
    }

    @Override
    public Writable getWritable(BigInteger data) {
        return BytesFormat.INSTANCE.getWritable(data.toByteArray());
    }
}
