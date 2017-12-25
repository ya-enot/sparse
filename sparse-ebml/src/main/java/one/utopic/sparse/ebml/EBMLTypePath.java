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

import java.util.Objects;

class EBMLTypePath {

    final EBMLTypePath parent;
    private final EBMLType type;

    public EBMLTypePath(EBMLType type) {
        this(type, null);
    }

    public EBMLTypePath(EBMLType type, EBMLTypePath parent) {
        Objects.requireNonNull(type);
        this.parent = parent;
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        EBMLTypePath other = (EBMLTypePath) obj;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return (parent == null ? "!" : parent + ".") + type.getName();
    }

    public static EBMLTypePath typePath(EBMLType... path) {
        if (path.length == 0) {
            return null;
        }
        EBMLTypePath typePath = new EBMLTypePath(path[0]);
        for (int i = 1; i < path.length; i++) {
            typePath = new EBMLTypePath(path[i], typePath);
        }
        return typePath;
    }
}