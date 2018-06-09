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
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import one.utopic.abio.api.output.Output;

public final class EBMLCode {

    private static final HashMap<EBMLCode, EBMLCode> internalCache = new HashMap<EBMLCode, EBMLCode>();

    private static synchronized EBMLCode intern(EBMLCode extCode) {
        EBMLCode intCode = internalCache.get(extCode);
        if (intCode != null) {
            return intCode;
        } else {
            internalCache.put(extCode, extCode);
            return extCode;
        }
    }

    private final byte[] code;

    public EBMLCode(byte[] code) {
        if (!EBMLFormatUtil.isCodeValid(code)) {
            throw new IllegalArgumentException("EBMLCode [" + DatatypeConverter.printHexBinary(code) + "] is not a valid EBML coded data");
        }
        this.code = code;
    }

    int getSize() {
        return code.length;
    }

    public void write(Output out) throws IOException {
        for (int i = 0; i < code.length; i++) {
            out.writeByte(code[i]);
        }
    }

    public EBMLCode intern() {
        return intern(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(code);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EBMLCode other = (EBMLCode) obj;
        if (!Arrays.equals(code, other.code))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EBMLCode [" + DatatypeConverter.printHexBinary(this.code) + "]";
    }

    public String toHexString() {
        return DatatypeConverter.printHexBinary(this.code);
    }

}
