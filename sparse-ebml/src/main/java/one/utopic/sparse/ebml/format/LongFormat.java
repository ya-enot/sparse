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
 * Acts like a BigIntegerFormat but value and scale are limited to Long type
 */
public class LongFormat implements EBMLFormat<Long> {

    public static final LongFormat INSTANCE = new LongFormat();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Long readFormat(byte[] data) {
        return BigIntegerFormat.INSTANCE.readFormat(data).longValueExact();
    }

    @Override
    public Writable getWritable(Long data) {
        return BigIntegerFormat.INSTANCE.getWritable(BigInteger.valueOf(data));
    }
}
