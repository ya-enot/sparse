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
import one.utopic.sparse.api.SkippableParser;
import one.utopic.sparse.api.exception.ParserIOException;
import one.utopic.sparse.ebml.EBMLType.Context;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLParser implements Parser, SkippableParser {

    private final Stack<EBMLInputHeader> stack;

    public EBMLParser(Input input, EBMLType.Context typeContext) throws IOException {
        this.stack = new Stack<EBMLInputHeader>();
        EBMLInputHeader header = readHeader(input, typeContext);
        if (header != null) {
            this.stack.push(header);
        }
    }

    public boolean hasNext() throws IOException {
        return !this.stack.isEmpty();
    }

    public EBMLHeader getHeader() throws IOException {
        return this.stack.isEmpty() ? null : this.stack.peek();
    }

    public EBMLHeader readHeader() throws IOException {
        if (this.stack.isEmpty()) {
            return null;
        }
        EBMLInputHeader parent = this.stack.peek();
        EBMLInputHeader header = readHeader(parent.input, parent.getType().getContext());
        return header == null ? null : this.stack.push(header);
    }

    public void read(Output out) throws IOException {
        if (this.stack.isEmpty()) {
            return;
        }
        WrappedInput in = this.stack.peek().input;
        while (!in.isFinished() && !out.isFinished()) {
            out.writeByte(in.readByte());
        }
    }

    public void next() throws IOException {
        if (this.stack.isEmpty()) {
            throw new ParserIOException("Failed to finish header. Header stack is empty.");
        }
        if (!this.stack.peek().input.isFinished()) {
            throw new ParserIOException("Failed to finish header. Header has more data to read.");
        }
        this.stack.pop();
    }

    public void skip() throws IOException {
        EBMLInputHeader header = this.stack.peek();
        if (header != null) {
            header.skip();
            next();
        }
    }

    protected EBMLInputHeader readHeader(Input input, Context typeContext) throws IOException {
        while (!input.isFinished()) {
            EBMLCode typeCode = new EBMLCode(readTag(input));
            int length = readLength(input);
            EBMLType type = typeContext.getType(typeCode);
            WrappedInput wInput = new WrappedInput(input, length);
            if (type != null) {
                return new EBMLInputHeader(type, wInput);
            } else {
                // TODO LOG
                wInput.skip();
            }
        }
        return null;
    }

    protected class WrappedInput implements Input, Skippable {

        private final Input input;
        private int remain;

        public WrappedInput(Input input, int remain) {
            Objects.requireNonNull(input);
            this.remain = remain;
            this.input = input;
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

        public int skip() throws IOException {
            return skip(remain);
        }
    }

    protected static class EBMLInputHeader extends EBMLHeader {

        private final WrappedInput input;

        public EBMLInputHeader(EBMLType type, WrappedInput input) {
            super(type);
            this.input = input;
        }

        public int skip() throws IOException {
            return input.skip();
        }
    }

    public int getRemain() {
        return this.stack.isEmpty() ? 0 : this.stack.peek().input.remain;
    }
}