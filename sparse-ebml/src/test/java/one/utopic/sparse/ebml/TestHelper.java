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

import javax.xml.bind.DatatypeConverter;

@SuppressWarnings("restriction")
public final class TestHelper {

    private static final String TEST_VERBOSE_PROPERTY_NAME = "testVerbose";

    public static byte[] parseNormalizedHexBinary(String string) {
        return DatatypeConverter.parseHexBinary(string.toUpperCase().replaceAll("[^0-9A-F]*", ""));
    }

    public static boolean isVerbose() {
        return Boolean.getBoolean(TEST_VERBOSE_PROPERTY_NAME);
    }

    private TestHelper() {
    }
}
