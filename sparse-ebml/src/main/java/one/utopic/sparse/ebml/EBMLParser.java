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
import java.util.Objects;
import java.util.Stack;

import one.utopic.abio.api.Skippable;
import one.utopic.abio.api.input.Input;
import one.utopic.sparse.api.Parser;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.api.SkippableParser;
import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParser implements Parser<EBMLParser>, SkippableParser {

    private final WrappedInput input;
    private final Stack<EBMLStatefulHeader> stack;
    private final EBMLType.Context typeContext;

    public EBMLParser(Input input, EBMLType.Context typeContext) {
        this.input = new WrappedInput(input);
        this.stack = new Stack<EBMLStatefulHeader>();
        this.typeContext = typeContext;
    }

    public boolean hasNext() throws IOException {
        return getHeader() != null;
    }

    public EBMLHeader getHeader() throws IOException {
        return !stack.isEmpty() ? stack.peek() : !input.isFinished() ? stack.push(readHeader()) : null;
    }

    public EBMLHeader nextHeader() throws IOException {
        return stack.push(readHeader());
    }

    protected EBMLStatefulHeader readHeader() throws IOException {
        while (!input.isFinished()) {
            input.resetCount();
            EBMLCode typeCode = new EBMLCode(readTag(input));
            long length = bytesToLong(readUnsignedCode(input));
            advance(input.getCount());
            EBMLType type = typeContext.getType(typeCode);
            if (type != null) {
                return new EBMLStatefulHeader(type, length);
            } else {
                // TODO LOG
                advance(input.skip(length));
            }
        }
        return null;
    }

    public <O> O read(Reader<EBMLParser, O> reader) {
        return reader.read(this);
    }

    public void skip() throws IOException {

    }

    protected void advance(long size) throws IOException {
        if (!stack.isEmpty()) {
            stack.peek().advance(size);
        }
    }

}

class WrappedInput implements Input, Skippable {

    private final Input input;
    private long count;

    public WrappedInput(Input input) {
        Objects.requireNonNull(input);
        this.input = input;
    }

    public boolean isFinished() {
        return input.isFinished();
    }

    public byte readByte() throws IOException {
        byte b = input.readByte();
        advance(1);
        return b;
    }

    private void advance(int i) {
        // TODO Not safe, count could overflow. Add overflow check or change data type
        count += i;
    }

    public long getCount() {
        return count;
    }

    public void resetCount() {
        this.count = 0;
    }

    public int skip(int byteCount) throws IOException {
        int skippedCount = 0;
        if (input instanceof Skippable) {
            skippedCount = ((Skippable) input).skip(byteCount);
        } else {
            for (; skippedCount < byteCount && !input.isFinished(); skippedCount++) {
                input.readByte();
            }
        }
        advance(skippedCount);
        return skippedCount;
    }

    public long skip(long length) throws IOException {
        long skipped = 0;
        while (length > 0 && !input.isFinished()) {
            int skip = 0;
            if (length > Integer.MAX_VALUE) {
                skip = Integer.MAX_VALUE;
            } else {
                skip = (int) length;
            }
            skipped += skip(skip);
        }
        return skipped;
    }

}

class EBMLStatefulHeader extends EBMLHeader {

    private long remain;

    public EBMLStatefulHeader(EBMLType type, long length) {
        super(type, length);
        remain = length;
    }

    public long getRemain() {
        return remain;
    }

    public void advance(long size) throws IOException {
        if (size > remain) {
            throw new IOException("Can't advance more than remain");
        }
        remain -= size;
    }
}