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

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Formatter;

public class EBMLFormatter implements Formatter {

    protected final Output output;

    public EBMLFormatter(Output output) {
        Objects.requireNonNull(output);
        this.output = output;
    }

    public int getPartSize(EBMLType type, int size) throws IOException {
        return type.getEBMLCode().getSize() + getCodeLength(intToBytes(size)) + size;
    }

    public void newHeader(EBMLType type, int size) throws IOException {
        type.getEBMLCode().write(output);
        writeUnsignedCode(output, intToBytes(size));
    }

    public void write(Input in) throws IOException {
        while (!in.isFinished() && !output.isFinished()) {
            output.writeByte(in.readByte());
        }
    }

}
