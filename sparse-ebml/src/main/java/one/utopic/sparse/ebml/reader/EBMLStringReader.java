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
import java.nio.charset.Charset;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLParser;

public class EBMLStringReader implements Reader<EBMLParser, String> {

    private final Charset cs;
    private final EBMLByteReader byteReader;

    public EBMLStringReader(EBMLByteReader byteReader) {
        this(Charset.defaultCharset(), byteReader);
    }

    public EBMLStringReader(Charset cs, EBMLByteReader byteReader) {
        this.cs = cs;
        this.byteReader = byteReader;
    }

    public String read(EBMLParser parser) throws IOException {
        byte[] bytes = byteReader.read(parser);
        return bytes == null ? null : new String(bytes, cs);
    }
}
