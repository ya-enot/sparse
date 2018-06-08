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

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Supplier;

import one.utopic.abio.api.input.Input;
import one.utopic.sparse.api.Event;
import one.utopic.sparse.api.ReadFormat;
import one.utopic.sparse.api.Reader;
import one.utopic.sparse.api.exception.SparseReaderException;
import one.utopic.sparse.ebml.EBMLType.Context;
import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class EBMLReader implements Reader<EBMLType>, Supplier<Event<EBMLType>> {

    protected static class WrappedInput implements Input {

        private final Input in;
        private int read = 0;

        public WrappedInput(Input in) {
            this.in = in;
        }

        @Override
        public boolean isFinished() {
            return in.isFinished();
        }

        @Override
        public byte readByte() throws IOException {
            byte b = in.readByte();
            read++;
            return b;
        }

    }

    private final WrappedInput in;
    private final LinkedList<EBMLType> typeStack = new LinkedList<>();
    private final LinkedList<Integer> lengthStack = new LinkedList<>();
    private final LinkedList<EBMLEvent> pendingEvents = new LinkedList<>();
    private final Context rootTypeContext;

    public EBMLReader(Input in, EBMLType.Context context) {
        this.in = new WrappedInput(requireNonNull(in));
        this.rootTypeContext = requireNonNull(context);
    }

    @Override
    public boolean hasNext() {
        return !in.isFinished() || !pendingEvents.isEmpty();
    }

    @Override
    public EBMLEvent next() throws SparseReaderException {
        if (!pendingEvents.isEmpty()) {
            return pendingEvents.poll();
        }
        try {
            EBMLCode code;
            try {
                code = new EBMLCode(EBMLFormatUtil.readCode(in, false));
            } catch (Exception e) {
                throw new IllegalStateException("EBML code reading error: " + e.getMessage(), e);
            }

            EBMLType type;
            if (typeStack.isEmpty()) {
                type = rootTypeContext.getType(code);
            } else {
                type = typeStack.peek().getContext().getType(code);
            }
            type = resolveTypeCode(type, code);

            int length;
            try {
                length = BigIntegerFormat.INSTANCE.readFormat(EBMLFormatUtil.readCode(in, true)).intValueExact();
            } catch (Exception e) {
                throw new IllegalStateException("EBML length reading error: " + e.getMessage(), e);
            }

            advance();
            lengthStack.push(length);
            typeStack.push(type);
            return new EBMLEvent(type, EBMLEvent.CommonEventType.BEGIN);
        } catch (Exception e) {
            throw new SparseReaderException(e);
        }
    }

    protected EBMLType resolveTypeCode(EBMLType type, EBMLCode code) throws SparseReaderException {
        if (null == type) {
            throw new SparseReaderException("Unknown EBML code: " + code);
        }
        return type;
    }

    private void advance() throws SparseReaderException {
        ListIterator<Integer> li = lengthStack.listIterator();
        while (li.hasNext()) {
            int remain = li.next() - in.read;
            if (remain > 0) {
                li.set(remain);
                continue;
            } else if (0 == remain) {
                pendingEvents.add(new EBMLEvent(typeStack.poll(), EBMLEvent.CommonEventType.END));
                li.remove();
                continue;
            } else {
                throw new SparseReaderException("Frame boundary violation while reading " + remain);
            }
        }
        in.read = 0;
    }

    public static abstract interface EBMLReadFormat<O> extends ReadFormat<EBMLType, EBMLReader, O> {

        @Override
        default O read(EBMLReader reader) throws SparseReaderException {
            return reader.read(this);
        }

        public abstract O readFormat(byte[] data);
    }

    @Override
    public Event<EBMLType> get() throws SparseReaderException {
        return hasNext() ? next() : null;
    }

    protected <O> O read(EBMLReadFormat<O> ebmlReadFormat) throws SparseReaderException {
        if (lengthStack.isEmpty()) {
            return readVarLen(ebmlReadFormat);
        }
        try {
            int length = lengthStack.peek();
            byte[] data = new byte[length];
            for (int i = 0; i < length && !in.isFinished(); i++) {
                data[i] = in.readByte();
            }
            int read = in.read;
            advance();
            return read == length ? ebmlReadFormat.readFormat(data) : ebmlReadFormat.readFormat(Arrays.copyOf(data, read));
        } catch (Exception e) {
            throw new SparseReaderException(e);
        }
    }

    protected <O> O readVarLen(EBMLReadFormat<O> ebmlReadFormat) throws SparseReaderException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (!in.isFinished()) {
                baos.write(in.readByte());
            }
            advance();
            return ebmlReadFormat.readFormat(baos.toByteArray());
        } catch (Exception e) {
            throw new SparseReaderException(e);
        }
    }

}
