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
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Event;
import one.utopic.sparse.api.Event.CommonEventType;
import one.utopic.sparse.api.WriteFormat;
import one.utopic.sparse.api.Writer;
import one.utopic.sparse.api.exception.SparseWriterException;
import one.utopic.sparse.ebml.EBMLWriter.EBMLWriteFormat.Writable;
import one.utopic.sparse.ebml.EBMLWriter.Frame.Format;
import one.utopic.sparse.ebml.EBMLWriter.Frame.Structure;

public class EBMLWriter implements Writer<EBMLType>, Consumer<Event<EBMLType>> {

    protected static interface Frame {

        int getSize();

        void write(Output out) throws IOException;

        static final class Structure implements Frame {

            private final EBMLType type;
            private final LinkedList<Frame> children = new LinkedList<>();

            private int dataSize = -1;
            private int fullSize = -1;

            public Structure(EBMLType type) {
                this.type = Objects.requireNonNull(type);
            }

            @Override
            public int getSize() {
                if (this.fullSize == -1) {
                    return this.fullSize = getDataSize() + EBMLFormatUtil.codeLength(BigInteger.valueOf(getDataSize()).toByteArray())
                            + this.type.getEBMLCode().getSize();
                }
                return this.fullSize;
            }

            public int getDataSize() {
                if (this.dataSize == -1) {
                    this.dataSize = 0;
                    for (Frame frame : this.children) {
                        this.dataSize += frame.getSize();
                    }
                }
                return this.dataSize;
            }

            @Override
            public void write(Output out) throws IOException {
                this.type.getEBMLCode().write(out);
                EBMLFormatUtil.writeCode(out, BigInteger.valueOf(getDataSize()).toByteArray());
                for (Frame frame : this.children) {
                    frame.write(out);
                }
            }

        }

        static class Format implements Frame {

            private final EBMLWriteFormat.Writable writable;

            private int size = -1;

            public Format(Writable writable) {
                this.writable = writable;
            }

            @Override
            public int getSize() {
                if (this.size == -1) {
                    this.size = this.writable.getSize();
                }
                return this.size;
            }

            @Override
            public void write(Output out) throws IOException {
                this.writable.writeFormat(out);
            }

        }
    }

    private final Output out;

    private final LinkedList<Frame.Structure> frameStack = new LinkedList<>();

    public EBMLWriter(Output out) {
        this.out = out;
    }

    @Override
    public void accept(Event<EBMLType> event) throws SparseWriterException {
        EBMLType ebmlType = Objects.requireNonNull(event.get());
        if (CommonEventType.BEGIN.equals(event.getType())) {
            openFrame(ebmlType);
        } else if (CommonEventType.END.equals(event.getType())) {
            Frame.Structure frame = closeFrame(ebmlType);
            if (null != frame) {
                try {
                    frame.write(this.out);
                } catch (IOException e) {
                    throw new SparseWriterException(e);
                }
            }
        } else {
            throw new SparseWriterException("Unknown event type " + event.getType());
        }
    }

    private void openFrame(EBMLType ebmlType) throws SparseWriterException {
        Frame.Structure headFrame = this.frameStack.peek();
        if (headFrame != null && !headFrame.type.getContext().contains(ebmlType)) {
            throw new SparseWriterException("No type " + ebmlType + " found in context " + headFrame.type.getContext());
        }
        this.frameStack.push(newStructureFrame(ebmlType));
    }

    private <O> void openFrame(EBMLWriteFormat.Writable w) throws SparseWriterException {
        Frame.Structure headFrame = this.frameStack.peek();
        if (headFrame == null) {
            try {
                w.writeFormat(this.out);
            } catch (IOException e) {
                throw new SparseWriterException(e);
            }
        } else if (headFrame.children.isEmpty()) {
            headFrame.children.add(newFormatFrame(w));
        } else {
            throw new SparseWriterException("Dirty Structure frame " + headFrame);
        }
    }

    protected Format newFormatFrame(EBMLWriteFormat.Writable w) {
        return new Frame.Format(w);
    }

    protected Structure newStructureFrame(EBMLType ebmlType) {
        return new Frame.Structure(ebmlType);
    }

    private Frame.Structure closeFrame(EBMLType ebmlType) {
        Iterator<Frame.Structure> it = frameStack.iterator();
        while (it.hasNext()) {
            Frame.Structure headFrame = it.next();
            if (headFrame.type.equals(ebmlType)) {
                it.remove();
                if (it.hasNext()) {
                    it.next().children.add(headFrame);
                    return null;
                }
                return headFrame;
            } else {
                throw new SparseWriterException("Can't close " + ebmlType + " before " + headFrame.type);
            }
        }
        throw new SparseWriterException("Nothing to end");
    }

    public boolean isEmpty() {
        return frameStack.isEmpty();
    }

    public static abstract interface EBMLWriteFormat<O> extends WriteFormat<EBMLType, EBMLWriter, O> {

        @Override
        default void write(EBMLWriter w, O data) throws SparseWriterException {
            w.openFrame(this.getWritable(data));
        }

        Writable getWritable(O data);

        interface Writable {

            void writeFormat(Output out) throws IOException;

            int getSize();

        }

    }

}
