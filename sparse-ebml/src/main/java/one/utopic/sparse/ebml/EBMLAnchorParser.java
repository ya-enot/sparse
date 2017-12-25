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
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import one.utopic.sparse.api.Anchor;
import one.utopic.sparse.api.AnchorParser;
import one.utopic.sparse.api.Reader;

public class EBMLAnchorParser implements AnchorParser<EBMLTypePath, EBMLParser> {

    private final EBMLParser parser;
    private final Map<EBMLTypePath, EBMLAnchor<?>> anchorMap;
    private EBMLTypePath typePath = null;

    public EBMLAnchorParser(EBMLParser parser) throws IOException {
        this.parser = parser;
        this.anchorMap = new HashMap<EBMLTypePath, EBMLAnchor<?>>();
        if (parser.hasNext()) {
            this.typePath = new EBMLTypePath(parser.getHeader().getType());
        }
    }

    public <O> Anchor<O> newAnchor(Reader<EBMLParser, O> reader, EBMLTypePath anchorPath) {
        EBMLAnchor<O> anchor = new EBMLAnchor<O>(reader);
        registerAnchorPath(anchorPath, anchor);
        return anchor;
    }

    protected void registerAnchorPath(EBMLTypePath typePath, EBMLAnchor<?> anchor) {
        if (typePath == null) {
            typePath = this.typePath;
        }
        anchorMap.put(typePath, anchor);
        while ((typePath = typePath.parent) != null) {
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

    public List<Anchor<?>> read(Anchor<?>... anchors) throws IOException {
        return read(anchors.length, anchors);
    }

    public List<Anchor<?>> readAll() throws IOException {
        return read(-1);
    }

    protected List<Anchor<?>> read(int count, Anchor<?>... anchors) throws IOException {
        List<Anchor<?>> read = new ArrayList<Anchor<?>>();
        Set<Anchor<?>> check = new HashSet<Anchor<?>>();
        for (int i = 0; i < anchors.length; i++) {
            check.add(anchors[i]);
        }
        {
            EBMLTypePath rootTypePath = new EBMLTypePath(parser.getHeader().getType());
            if (!anchorMap.containsKey(rootTypePath)) {
                return read;
            }
            EBMLAnchor<?> rootAnchor = anchorMap.get(rootTypePath);
            if (rootAnchor != null) {
                rootAnchor.read(parser);
                if (count == -1 || check.contains(rootAnchor)) {
                    read.add(rootAnchor);
                }
                return read;
            }
        }
        while (parser.hasNext() && (count == -1 || count > 0)) {
            EBMLHeader header = parser.readHeader();
            if (header == null) {
                parser.next();
                if (this.typePath == null) {
                    throw new EmptyStackException();
                }
                this.typePath = this.typePath.parent;
                continue;
            }
            EBMLTypePath typePath = new EBMLTypePath(header.getType(), this.typePath);
            if (anchorMap.containsKey(typePath)) {
                EBMLAnchor<?> anchor = anchorMap.get(typePath);
                if (anchor != null) {
                    anchor.read(parser);
                    parser.next();
                    if (count == -1 || check.contains(anchor)) {
                        read.add(anchor);
                    }
                } else {
                    this.typePath = typePath;
                }
            } else {
                parser.skip();
            }
        }
        return read;
    }

}
