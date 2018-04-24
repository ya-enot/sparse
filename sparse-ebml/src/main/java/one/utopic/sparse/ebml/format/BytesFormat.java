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

import java.io.IOException;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.ebml.EBMLFormat;

/**
 * Writes and reads data unmodified
 */
public class BytesFormat implements EBMLFormat<byte[]> {

    public static final BytesFormat INSTANCE = new BytesFormat();

    @Override
    public byte[] readFormat(byte[] data) {
        return data;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Writable getWritable(byte[] data) {
        return new Writable() {

            @Override
            public void writeFormat(Output out) throws IOException {
                for (int i = 0; i < data.length; i++) {
                    out.writeByte(data[i]);
                }
            }

            @Override
            public int getSize() {
                return data.length;
            }

        };
    }
}
