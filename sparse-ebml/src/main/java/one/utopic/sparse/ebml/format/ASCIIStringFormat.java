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

import java.nio.charset.Charset;

import one.utopic.sparse.ebml.EBMLFormat;

/**
 * Writes and reads UTF8 string data
 */
public class ASCIIStringFormat implements EBMLFormat<String> {

    public static final ASCIIStringFormat INSTANCE = new ASCIIStringFormat();

    private static final Charset CHARSET = Charset.forName("ASCII");

    @Override
    public String readFormat(byte[] data) {
        if (data.length < 1) {
            return "";
        }
        return new String(data, CHARSET);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Writable getWritable(String data) {
        return BytesFormat.INSTANCE.getWritable(data.getBytes(CHARSET));
    }
}
