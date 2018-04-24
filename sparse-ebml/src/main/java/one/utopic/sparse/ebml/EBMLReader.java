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
import one.utopic.sparse.ebml.format.IntegerFormat;

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

    public static final Context EMPTY_CONTEXT = new Context() {
        @Override
        public EBMLType getType(EBMLCode code) {
            return null;
        }

        @Override
        public boolean contains(EBMLType type) {
            return false;
        }
    };

    private final WrappedInput in;
    private final LinkedList<EBMLType> typeStack = new LinkedList<>();
    private final LinkedList<Integer> lengthStack = new LinkedList<>();
    private final LinkedList<EBMLEvent> pendingEvents = new LinkedList<>();
    private final Context rootTypeContext;

    public EBMLReader(Input in, EBMLType.Context context) {
        this.in = new WrappedInput(requireNonNull(in));
        this.rootTypeContext = requireNonNull(context);
    }

    public EBMLReader(Input in) {
        this(in, EMPTY_CONTEXT);
    }

    @Override
    public boolean hasNext() throws SparseReaderException {
        return !in.isFinished();
    }

    @Override
    public EBMLEvent next() {
        if (!pendingEvents.isEmpty()) {
            return pendingEvents.poll();
        }
        try {
            EBMLCode code = new EBMLCode(EBMLUtil.readTag(in));
            EBMLType type;
            if (typeStack.isEmpty()) {
                type = rootTypeContext.getType(code);
            } else {
                type = typeStack.peek().getContext().getType(code);
            }
            if (null == type) {
                throw new SparseReaderException("Unknown " + code);
            }
            Integer length = IntegerFormat.INSTANCE.readFormat(EBMLUtil.readUnsignedCode(in, true));
            advance();
            lengthStack.push(length);
            typeStack.push(type);
            return new EBMLEvent(type, EBMLEvent.CommonEventType.BEGIN);
        } catch (IOException e) {
            throw new SparseReaderException(e);
        }
    }

    private void advance() {
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
        default O read(EBMLReader r) {
            if (r.lengthStack.isEmpty()) {
                return readVarLen(r);
            }
            try {
                int length = r.lengthStack.peek();
                byte[] data = new byte[length];
                for (int i = 0; i < length && !r.in.isFinished(); i++) {
                    data[i] = r.in.readByte();
                }
                int read = r.in.read;
                r.advance();
                return read == length ? readFormat(data) : readFormat(Arrays.copyOf(data, read));
            } catch (IOException e) {
                throw new SparseReaderException(e);
            }
        }

        default O readVarLen(EBMLReader r) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                while (!r.in.isFinished()) {
                    baos.write(r.in.readByte());
                }
                r.advance();
                return readFormat(baos.toByteArray());
            } catch (IOException e) {
                throw new SparseReaderException(e);
            }
        }

        public abstract O readFormat(byte[] data);
    }

    @Override
    public Event<EBMLType> get() {
        return hasNext() ? next() : null;
    }

}
