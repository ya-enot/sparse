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

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLParser;
import one.utopic.sparse.ebml.util.ByteArrayOutput;

public class EBMLByteReader implements Reader<EBMLParser, byte[]> {

    public byte[] read(EBMLParser parser) throws IOException {
        if (parser.hasNext()) {
            byte[] buff = new byte[parser.getRemain()];
            ByteArrayOutput out = new ByteArrayOutput(buff);
            parser.read(out);
            parser.next();
            return buff;
        }
        return null;
    }
}
