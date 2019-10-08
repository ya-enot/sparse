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

public interface EBMLType {

    EBMLCode getEBMLCode();

    Context getContext();

    public static interface Context {

        public static final Context EMPTY = new Context() {

            @Override
            public EBMLType getType(EBMLCode code) {
                return null;
            }

            @Override
            public boolean contains(EBMLType type) {
                return false;
            }

            @Override
            public boolean is(Context context) {
                return equals(context);
            }

        };

        EBMLType getType(EBMLCode code);

        boolean contains(EBMLType type);

        boolean is(Context context);

    }

}