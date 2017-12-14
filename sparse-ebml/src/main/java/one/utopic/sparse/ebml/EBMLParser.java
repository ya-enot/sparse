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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import one.utopic.abio.api.input.Input;
import one.utopic.sparse.api.Anchor;
import one.utopic.sparse.api.AnchorParser;
import one.utopic.sparse.api.Parser;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.api.Skippable;
import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParser implements Parser<EBMLParser>, Skippable {

    private final Input input;

    private final Stack<EBMLStatefulHeader> stack;

    private EBMLHeader header;

    public EBMLParser(Input input) {
        this.input = input;
        this.stack = new Stack<EBMLStatefulHeader>();
    }

    public void skip() {
        // TODO Auto-generated method stub

    }

    public boolean hasNext() throws IOException {
        return getHeader() != null;
    }

    public EBMLHeader getHeader() throws IOException {
        return header != null ? header : (header = readHeader());
    }

    private EBMLHeader readHeader() throws IOException {
        long typeCode = bytesToLong(readTag(input));
        long length = bytesToLong(readUnsignedCode(input));
        return null;
    }

    public <O> O read(Reader<EBMLParser, O> reader) {
        return reader.read(this);
    }

    public byte[] readData(byte[] buff, int start, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            buff[start + i] = input.readByte();
        }
        return buff;
    }

    private static class EBMLStatefulHeader extends EBMLHeader {

        private long remain;

        public EBMLStatefulHeader(EBMLType type, long length) {
            super(type, length);
            remain = length;
        }
    }

}
