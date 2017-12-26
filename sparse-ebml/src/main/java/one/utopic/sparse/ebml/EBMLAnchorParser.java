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
package one.utopic.sparse.ebml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import one.utopic.sparse.api.Anchor;
import one.utopic.sparse.api.AnchorParser;
import one.utopic.sparse.api.Reader;

public class EBMLAnchorParser implements AnchorParser<EBMLTypePath, EBMLParser> {

    private final Map<EBMLTypePath, EBMLAnchor<?>> anchorMap;
    private EBMLTypePath typePath = null;

    public EBMLAnchorParser() throws IOException {
        this.anchorMap = new HashMap<EBMLTypePath, EBMLAnchor<?>>();
    }

    public <O> Anchor<O> newAnchor(Reader<EBMLParser, O> reader, EBMLTypePath anchorPath) {
        EBMLAnchor<O> anchor = new EBMLAnchor<O>(reader);
        registerAnchorPath(anchorPath, anchor);
        return anchor;
    }

    protected void registerAnchorPath(EBMLTypePath typePath, EBMLAnchor<?> anchor) {
        Objects.requireNonNull(typePath);
        if (null != anchorMap.put(typePath, anchor)) {
            throw new IllegalArgumentException("Anchors duplicate at " + typePath);
        }
        while ((typePath = typePath.getParent()) != null) {
            if (null != anchorMap.put(typePath, null)) {
                throw new IllegalArgumentException("Anchors overlap at " + typePath);
            }
        }
    }

    private static class EBMLAnchor<O> implements Anchor<O> {

        private final Reader<EBMLParser, O> reader;
        private final List<O> result = new ArrayList<O>();

        public EBMLAnchor(Reader<EBMLParser, O> reader) {
            this.reader = reader;
        }

        public void read(EBMLParser parser) throws IOException {
            result.add(reader.read(parser));
        }

        public List<O> get() {
            return result;
        }

    }

    public Set<Anchor<?>> readAll(EBMLParser parser) throws IOException {
        Set<Anchor<?>> result = new HashSet<Anchor<?>>();
        Anchor<?> read;
        while ((read = read(parser)) != null) {
            result.add(read);
        }
        return result;
    }

    public Anchor<?> read(EBMLParser parser) throws IOException {
        while (parser.hasNext()) {
            EBMLHeader header = parser.getHeader();
            EBMLTypePath typePath = new EBMLTypePath(header.getType(), this.typePath);
            if (anchorMap.containsKey(typePath)) {
                EBMLAnchor<?> anchor = anchorMap.get(typePath);
                if (anchor != null) {
                    anchor.read(parser);
                    parser.next();
                    readHeader(parser);
                    return anchor;
                } else {
                    this.typePath = typePath;
                    readHeader(parser);
                }
            } else {
                parser.skip();
                readHeader(parser);
            }
        }
        return null;
    }

    private void readHeader(EBMLParser parser) throws IOException {
        while (parser.readHeader() == null && this.typePath != null) {
            this.typePath = this.typePath.getParent();
            parser.next();
        }
    }

}
