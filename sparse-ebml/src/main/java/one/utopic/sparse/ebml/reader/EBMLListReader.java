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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLParser;
import one.utopic.sparse.ebml.EBMLType;

public class EBMLListReader<T> implements Reader<EBMLParser, List<T>> {

    private final Reader<EBMLParser, T> elementReader;
    private final EBMLType elementType;
    private final boolean strict;

    public EBMLListReader(EBMLType elementType, Reader<EBMLParser, T> elementReader) {
        this(elementType, elementReader, true);
    }

    public EBMLListReader(EBMLType elementType, Reader<EBMLParser, T> elementReader, boolean strict) {
        this.elementType = elementType;
        this.strict = strict;
        this.elementReader = Objects.requireNonNull(elementReader);
    }

    public List<T> read(EBMLParser parser) throws IOException {
        List<T> list = newList();
        EBMLHeader elementHeader = null;
        while ((elementHeader = parser.readHeader()) != null) {
            if (elementHeader.getType().equals(elementType)) {
                list.add(elementReader.read(parser));
                parser.next();
            } else if (strict) {
                throw new IOException("Strict list reader element type missmatch");
            } else {
                parser.skip();
            }
        }
        return list;
    }

    protected List<T> newList() {
        return new ArrayList<T>();
    }

}
