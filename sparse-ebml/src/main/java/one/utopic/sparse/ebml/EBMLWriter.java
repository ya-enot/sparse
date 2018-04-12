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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Event;
import one.utopic.sparse.api.Event.CommonEventType;
import one.utopic.sparse.api.WriteFormat;
import one.utopic.sparse.api.Writer;
import one.utopic.sparse.api.exception.SparseWriterException;
import one.utopic.sparse.ebml.format.IntegerFormat;

public class EBMLWriter implements Writer<EBMLType>, Consumer<Event<EBMLType>> {

    private static final class StructureFrame {

        private final EBMLType type;
        private final LinkedList<DataFrame> children = new LinkedList<>();

        public StructureFrame(EBMLType type) {
            this.type = type;
        }

    }

    private static final class DataFrame {

        private final byte[] buffer;

        public DataFrame(byte[] buffer) {
            this.buffer = buffer;
        }

    }

    private final Output out;

    private final LinkedList<StructureFrame> frameStack = new LinkedList<>();

    public EBMLWriter(Output out) {
        this.out = out;
    }

    @Override
    public void accept(Event<EBMLType> event) throws SparseWriterException {
        EBMLType ebmlType = event.get();
        if (CommonEventType.BEGIN.equals(event.getType())) {
            if (frameStack.isEmpty() || frameStack.peek().type.getContext().getType(ebmlType.getEBMLCode()).equals(ebmlType)) {
                this.begin(ebmlType);
            } else {
                throw new SparseWriterException("No type " + ebmlType + " found in context " + frameStack.peek().type.getContext());
            }
        } else if (CommonEventType.END.equals(event.getType())) {
            if (frameStack.isEmpty()) {
                throw new SparseWriterException("Nothing to end");
            } else if (frameStack.peek().type.equals(ebmlType)) {
                this.end();
            } else {
                throw new SparseWriterException("Can't close " + ebmlType + " before " + frameStack.peek().type);
            }
        } else {
            throw new SparseWriterException("Unknown event type " + event.getType());
        }
    }

    protected void begin(EBMLType t) throws SparseWriterException {
        frameStack.push(new StructureFrame(t));
    }

    protected void end() throws SparseWriterException {
        StructureFrame sFrame = frameStack.poll();
        if (null == sFrame) {
            throw new SparseWriterException("No frames left for write");
        }
        DataFrame dFrame = convertFrame(sFrame);
        if (null == dFrame) {
            throw new SparseWriterException("No frame to write");
        }
        if (!frameStack.isEmpty()) {
            frameStack.peek().children.add(dFrame);
        } else {
            writeFrame(out, dFrame);
        }
    }

    private static DataFrame convertFrame(StructureFrame sFrame) {
        int dataLength = 0;
        for (DataFrame dFrame : sFrame.children) {
            dataLength += dFrame.buffer.length;
        }
        byte[] codedDataLength = IntegerFormat.INSTANCE.writeFormat(dataLength);
        int fullLength = dataLength + codedDataLength.length + sFrame.type.getEBMLCode().getSize();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(fullLength)) {
            Output dataOut = new Output() {

                @Override
                public void writeByte(byte b) throws IOException {
                    baos.write(b);
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

            };
            sFrame.type.getEBMLCode().write(dataOut);
            EBMLUtil.writeUnsignedCode(dataOut, codedDataLength);
            for (DataFrame dFrame : sFrame.children) {
                writeFrame(dataOut, dFrame);
            }
            return new DataFrame(baos.toByteArray());
        } catch (IOException e) {
            throw new SparseWriterException(e);
        }
    }

    private static void writeFrame(Output out, DataFrame dFrame) {
        try {
            byte[] data = dFrame.buffer;
            for (int i = 0; i < data.length; i++) {
                if (out.isFinished()) {
                    throw new EOFException();
                }
                out.writeByte(data[i]);
            }
        } catch (IOException e) {
            throw new SparseWriterException(e);
        }
    }

    public boolean isEmpty() {
        return frameStack.isEmpty();
    }

    public static abstract interface EBMLWriteFormat<O> extends WriteFormat<EBMLType, EBMLWriter, O> {

        @Override
        default void write(EBMLWriter w, O o) throws SparseWriterException {
            DataFrame dFrame = new DataFrame(writeFormat(o));
            StructureFrame sFrame = w.frameStack.peek();
            if (null == sFrame) {
                writeFrame(w.out, dFrame);
            } else {
                sFrame.children.push(dFrame);
            }
        }

        public abstract byte[] writeFormat(O data);
    }

}
