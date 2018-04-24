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
package one.utopic.sparse.ebml.test.util;

import one.utopic.sparse.ebml.EBMLCode;
import one.utopic.sparse.ebml.EBMLType;

import static one.utopic.sparse.ebml.test.util.TestType.Context.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public enum TestType implements EBMLType {

    TYPE_1_1(0x8A, SUB1, ROOT), //
    TYPE_1_2(0x80, SUB1), //
    TYPE_2_1(0x8B, SUB2, ROOT), //
    TYPE_2_2(0x80, SUB2), //

    ;

    public static enum Context implements EBMLType.Context {
        ROOT, //
        SUB1(ROOT), //
        SUB2(ROOT), //

        ;

        private final Map<EBMLCode, EBMLType> typeMap = new HashMap<>();
        private final Context parentContext;

        private Context() {
            this(null);
        }

        private Context(Context parentContext) {
            this.parentContext = parentContext;
        }

        @Override
        public EBMLType getType(EBMLCode code) {
            return this.typeMap.computeIfAbsent(code, c -> null == this.parentContext ? null : this.parentContext.typeMap.get(c));
        }

        private void register(TestType type) {
            if (typeMap.putIfAbsent(type.getEBMLCode(), type) != null) {
                throw new IllegalArgumentException("EventType is already registered for " + type.getEBMLCode());
            }
        }

        @Override
        public boolean contains(EBMLType type) {
            return typeMap.containsValue(type) || (null != this.parentContext && this.parentContext.typeMap.containsValue(type));
        }

    }

    private final Context context;
    private final EBMLCode code;

    private TestType(long code, Context context, Context... regContexts) {
        this.context = context;
        try {
            this.code = new EBMLCode(code > 0xFF ? longToBytes(code) : new byte[] { (byte) (0xFF & code) });
            context.register(this);
            for (int i = 0; i < regContexts.length; i++) {
                regContexts[i].register(this);
            }
        } catch (Throwable e) {
            throw new TypeInitializationException(this, e);
        }
    }

    @Override
    public EBMLCode getEBMLCode() {
        return code;
    }

    @Override
    public Context getContext() {
        return context;
    }

    private static byte[] longToBytes(long value) {
        if (value == 0) {
            return new byte[0];
        }
        return BigInteger.valueOf(value).toByteArray();
    }
}

class TypeInitializationException extends LinkageError {

    private static final long serialVersionUID = -77030685922999867L;

    public TypeInitializationException(TestType t, Throwable cause) {
        super("Can't inialize " + t.name() + ": " + cause.getMessage(), cause);
    }

}