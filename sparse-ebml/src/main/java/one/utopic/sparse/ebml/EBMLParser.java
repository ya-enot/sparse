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

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;
import java.util.Stack;

import one.utopic.abio.api.Skippable;
import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Parser;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.api.SkippableParser;
import one.utopic.sparse.api.exception.ParserIOException;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParser implements Parser<EBMLParser>, SkippableParser {

    protected final Stack<EBMLInputHeader> stack;
    protected final EBMLType.Context typeContext;

    public EBMLParser(Input input, EBMLType.Context typeContext) throws IOException {
        this.stack = new Stack<EBMLInputHeader>();
        this.typeContext = typeContext;
        EBMLInputHeader header = readHeader(input);
        if (header != null) {
            this.stack.push(header);
        }
    }

    public boolean hasNext() throws IOException {
        return !stack.isEmpty();
    }

    public EBMLHeader getHeader() throws IOException {
        return stack.isEmpty() ? null : stack.peek();
    }

    public EBMLHeader readHeader() throws IOException {
        if (stack.isEmpty()) {
            return null;
        }
        EBMLInputHeader header = readHeader(stack.peek().getInput());
        return header == null ? null : stack.push(header);
    }

    public void next() throws ParserIOException {
        if (stack.isEmpty()) {
            throw new ParserIOException("Failed to finish header. Header stack is empty.");
        }
        if (!stack.peek().isFinished()) {
            throw new ParserIOException("Failed to finish header. Header has more data to read.");
        }
        stack.pop();
    }

    protected EBMLInputHeader readHeader(Input input) throws IOException {
        while (!input.isFinished()) {
            EBMLCode typeCode = new EBMLCode(readTag(input));
            int length = bytesToInt(readUnsignedCode(input));
            EBMLType type = typeContext.getType(typeCode);
            if (type != null) {
                return new EBMLInputHeader(type, new WrappedInput(input, length));
            } else {
                // TODO LOG
                intSkip(input, length);
            }
        }
        return null;
    }

    public <O> O read(Reader<EBMLParser, O> reader) throws IOException {
        return reader.read(this);
    }

    public void read(Output out) throws IOException {
        WrappedInput in = stack.peek().getInput();
        while (!in.isFinished() && !out.isFinished()) {
            out.writeByte(in.readByte());
        }
    }

    public void skip() throws IOException {
        EBMLInputHeader header = stack.peek();
        if (header != null) {
            WrappedInput input = header.getInput();
            intSkip(input, input.getRemain());
            next();
        }
    }

    protected final long longSkip(Input input, long length) throws IOException {
        long skipped = 0;
        while (skipped < length && !input.isFinished()) {
            int skip = 0;
            if (length > Integer.MAX_VALUE) {
                skip = Integer.MAX_VALUE;
            } else {
                skip = (int) length;
            }
            skipped += intSkip(input, skip);
        }
        return skipped;
    }

    protected final int intSkip(Input input, int length) throws IOException {
        int skipped;
        if (input instanceof Skippable) {
            skipped = ((Skippable) input).skip(length);
        } else {
            for (skipped = 0; skipped < length && !input.isFinished(); skipped += 1) {
                input.readByte();
            }
        }
        return skipped;
    }

}

class WrappedInput implements Input, Skippable {

    private final Input input;
    private int remain;

    public WrappedInput(Input input, int remain) {
        Objects.requireNonNull(input);
        this.input = input;
        this.remain = remain;
    }

    public int getRemain() {
        return remain;
    }

    public boolean isFinished() {
        return remain < 1 || input.isFinished();
    }

    public byte readByte() throws IOException {
        if (isFinished()) {
            throw new EOFException();
        }
        byte b = input.readByte();
        remain -= 1;
        return b;
    }

    public int skip(int byteCount) throws IOException {
        if (isFinished()) {
            return 0;
        }
        int skippedCount = 0;
        if (input instanceof Skippable) {
            skippedCount = ((Skippable) input).skip(byteCount);
        } else {
            for (; skippedCount < byteCount && !input.isFinished(); skippedCount++) {
                input.readByte();
            }
        }
        remain -= skippedCount;
        return skippedCount;
    }
}

class EBMLInputHeader extends EBMLHeader {

    private final WrappedInput input;
    boolean finished = false;

    public EBMLInputHeader(EBMLType type, WrappedInput input) {
        super(type, Objects.requireNonNull(input).getRemain());
        this.input = input;
    }

    public boolean isFinished() {
        return input.isFinished();
    }

    public WrappedInput getInput() {
        return input;
    }
}