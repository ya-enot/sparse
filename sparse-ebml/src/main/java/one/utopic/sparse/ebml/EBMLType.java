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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EBMLType {

    private final EBMLCode EBMLCode;
    private final String name;

    private EBMLType(String name, EBMLCode EBMLCode) {
        this.name = name;
        this.EBMLCode = EBMLCode;
    }

    public String getName() {
        return name;
    }

    public EBMLCode getEBMLCode() {
        return EBMLCode;
    }

    public static class Context {

        private final Map<EBMLCode, EBMLType> typeMap = new ConcurrentHashMap<EBMLCode, EBMLType>();

        public synchronized EBMLType newType(String name, EBMLCode code) {
            if (typeMap.containsKey(code)) {
                throw new IllegalArgumentException("Type is already registered for " + code);
            }
            code = code.intern();
            EBMLType type = new EBMLType(name, code);
            typeMap.put(code, type);
            return type;
        }

        public EBMLType getType(EBMLCode code) {
            return typeMap.get(code);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getEBMLCode() == null) ? 0 : getEBMLCode().hashCode());
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
        EBMLType other = (EBMLType) obj;
        if (getEBMLCode() == null) {
            if (other.getEBMLCode() != null)
                return false;
        } else if (!getEBMLCode().equals(other.getEBMLCode()))
            return false;
        return true;
    }
}
