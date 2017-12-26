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

import static one.utopic.sparse.ebml.util.EBMLHelper.isCodeValid;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import one.utopic.abio.api.output.Output;

public final class EBMLCode {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

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

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int b = 0xff & bytes[j];
            hexChars[j * 2] = hexArray[b >>> 4];
            hexChars[j * 2 + 1] = hexArray[0xf & b];
        }
        return new String(hexChars);
    }

    private final byte[] code;

    public EBMLCode(byte[] code) {
        if (!isCodeValid(code)) {
            throw new IllegalArgumentException("Code is not a valid EBML coded data");
        }
        this.code = code;
    }

    int getSize() {
        return code.length;
    }

    void write(Output out) throws IOException {
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
    public String toString() {
        return "EBMLCode [0x" + bytesToHex(code) + "]";
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

}
