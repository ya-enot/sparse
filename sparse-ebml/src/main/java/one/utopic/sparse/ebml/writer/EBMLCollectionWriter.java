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
package one.utopic.sparse.ebml.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.EBMLWriter;

public class EBMLCollectionWriter<T> implements EBMLWriter<EBMLFormatter, Collection<T>> {

    private final EBMLType type;
    private final EBMLWriter<EBMLFormatter, T> elementWriter;

    public EBMLCollectionWriter(EBMLType type, EBMLWriter<EBMLFormatter, T> elementWriter) {
        this.type = type;
        this.elementWriter = elementWriter;
    }

    public Part<EBMLFormatter> prepare(final Collection<T> o) throws IOException {
        return o == null ? null : new Part<EBMLFormatter>() {

            private final Collection<Part<EBMLFormatter>> parts = prepareCollection(o);
            private int dataSize = -1;

            public int getSize(EBMLFormatter formatter) throws IOException {
                int size = getDataSize(formatter);
                return formatter.getHeaderSize(type, size) + size;
            }

            private int getDataSize(EBMLFormatter formatter) throws IOException {
                if (dataSize != -1) {
                    return dataSize;
                }
                int size = 0;
                Iterator<Part<EBMLFormatter>> it = parts.iterator();
                while (it.hasNext()) {
                    size += it.next().getSize(formatter);

                }
                return dataSize = size;
            }

            public void write(EBMLFormatter formatter) throws IOException {
                int size = getDataSize(formatter);
                formatter.newHeader(type, size);
                Iterator<Part<EBMLFormatter>> it = parts.iterator();
                while (it.hasNext()) {
                    it.next().write(formatter);
                }
                formatter.next();
            }
        };
    }

    protected Collection<Part<EBMLFormatter>> prepareCollection(Collection<T> o) throws IOException {
        ArrayList<Part<EBMLFormatter>> parts = new ArrayList<Part<EBMLFormatter>>(o.size());
        Iterator<T> it = o.iterator();
        while (it.hasNext()) {
            parts.add(elementWriter.prepare(it.next()));
        }
        return parts;
    }

}
