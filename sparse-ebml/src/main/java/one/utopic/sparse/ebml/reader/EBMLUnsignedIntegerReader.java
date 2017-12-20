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
package one.utopic.sparse.ebml.reader;

import java.io.IOException;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLParser;

public class EBMLUnsignedIntegerReader implements Reader<EBMLParser, Integer> {

    public Integer read(EBMLParser parser) throws IOException {
        IntegerOutput out = new IntegerOutput();
        parser.read(out);
        return out.finish().result;
    }

    private static class IntegerOutput implements Output {

        private int result;
        private int remain = Integer.BYTES;

        public boolean isFinished() {
            return remain == 0;
        }

        public void writeByte(byte b) throws IOException {
            result <<= Byte.SIZE;
            result |= (Integer) 0xff & b;
            if (--remain < 0) {
                throw new IOException("Size overflow for type Integer");
            }
            if (result < 0) {
                throw new IOException("Unsigned Integer value read as negative");
            }
        }

        public IntegerOutput finish() {
            if (remain > 0) {
                remain = 0;
            }
            return this;
        }
    }
}
