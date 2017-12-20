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

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

import java.io.IOException;
import java.util.Objects;
import java.util.Stack;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Formatter;
import one.utopic.sparse.api.exception.ParserIOException;

public class EBMLFormatter implements Formatter {

    protected final Stack<EBMLOutputHeader> stack;
    protected final Output output;

    public EBMLFormatter(Output output) {
        this.output = output;
        this.stack = new Stack<EBMLOutputHeader>();
    }

    public boolean hasNext() throws IOException {
        return !stack.isEmpty();
    }

    public void next() throws IOException {
        if (stack.isEmpty()) {
            throw new ParserIOException("Failed to finish header. Header stack is empty.");
        }
        if (!stack.peek().output.isFinished()) {
            throw new ParserIOException("Failed to finish header. Header has more data to read.");
        }
        EBMLOutputHeader header = stack.pop();
        if (!stack.isEmpty()) {
            stack.peek().output.advance(header.length);
        }
    }

    public int getPartSize(EBMLType type, int size) throws IOException {
        return type.getEBMLCode().getSize() + getCodeLength(intToBytes(size)) + size;
    }

    public void newHeader(EBMLType type, int size) throws IOException {
        Output out = output;
        if (!stack.isEmpty()) {
            out = stack.peek().output;
        }
        type.getEBMLCode().write(out);
        writeUnsignedCode(out, intToBytes(size));
        stack.push(new EBMLOutputHeader(type, new WrappedOutput(output, size)));
    }

    public void write(Input in) throws IOException {
        if (stack.isEmpty()) {
            return;
        }
        WrappedOutput out = stack.peek().output;
        while (!in.isFinished() && !out.isFinished()) {
            out.writeByte(in.readByte());
        }
    }

    private class WrappedOutput implements Output {

        private final Output output;
        private int remain;

        public WrappedOutput(Output output, int size) {
            Objects.requireNonNull(output);
            this.remain = size;
            if (output instanceof WrappedOutput) {
                this.output = ((WrappedOutput) output).output;
            } else {
                this.output = output;
            }
        }

        public boolean isFinished() {
            return remain < 1 || output.isFinished();
        }

        public void writeByte(byte b) throws IOException {
            output.writeByte(b);
            remain -= 1;
        }

        public void advance(int length) throws IOException {
            if (length > remain) {
                throw new IOException("Can not advance more than remain");
            }
            remain -= length;
        }

    }

    private static class EBMLOutputHeader extends EBMLHeader {

        private final WrappedOutput output;
        private final int length;

        public EBMLOutputHeader(EBMLType type, WrappedOutput output) {
            super(type);
            this.output = output;
            this.length = Objects.requireNonNull(output).remain;
        }
    }
}
