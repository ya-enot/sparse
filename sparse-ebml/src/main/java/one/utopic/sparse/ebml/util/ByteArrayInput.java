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
import java.util.Objects;

import one.utopic.abio.api.input.Input;

public class ByteArrayInput implements Input {

    private final byte[] buff;
    private final int length;
    private int pos;

    public ByteArrayInput(byte[] buff) {
        this(buff, 0, buff.length);
    }

    public ByteArrayInput(byte[] buff, int offset, int length) {
        Objects.requireNonNull(buff);
        this.buff = buff;
        this.length = offset + length;
        this.pos = offset;
    }

    public boolean isFinished() {
        return pos >= length;
    }

    public byte readByte() throws IOException {
        return buff[pos++];
    }

}
