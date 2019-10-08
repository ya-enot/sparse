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
package one.utopic.sparse.ebml.test.util;

import java.io.IOException;
import java.io.InputStream;

import one.utopic.abio.api.input.Input;

public class ByteArrayStreamInput implements Input {

    private final InputStream is;

    public ByteArrayStreamInput(InputStream is) {
        this.is = is;
    }

    @Override
    public boolean isFinished() {
        try {
            is.mark(1);
            return is.read() == -1;
        } catch (IOException e) {
            return true;
        } finally {
            try {
                is.reset();
            } catch (IOException e) {
                return true;
            }
        }
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) is.read();
    }

}
