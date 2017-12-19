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
import java.util.Map;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLParser;
import one.utopic.sparse.ebml.EBMLType;

public class EBMLMappedReader<T> implements Reader<EBMLParser, T> {

    private final Map<EBMLType, Reader<EBMLParser, ? extends T>> readerMap;
    private final boolean strict;

    public EBMLMappedReader(Map<EBMLType, Reader<EBMLParser, ? extends T>> readerMap) {
        this(readerMap, true);
    }

    public EBMLMappedReader(Map<EBMLType, Reader<EBMLParser, ? extends T>> readerMap, boolean strict) {
        this.readerMap = readerMap;
        this.strict = strict;
    }

    public T read(EBMLParser parser) throws IOException {
        EBMLHeader header = parser.getHeader();
        if (header != null) {
            Reader<EBMLParser, ? extends T> reader = readerMap.get(header.getType());
            if (reader != null) {
                return reader.read(parser);
            } else if (strict) {
                throw new IOException("Strict mapped reader element type missmatch");
            } else {
                parser.skip();
            }
        }
        return null;
    }

}
