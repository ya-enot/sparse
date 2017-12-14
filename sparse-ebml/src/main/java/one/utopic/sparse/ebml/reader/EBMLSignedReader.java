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
package one.utopic.sparse.ebml.reader;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLParser;

public class EBMLSignedReader implements Reader<EBMLParser, Integer> {

    public Integer read(EBMLParser parser) {
	EBMLHeader header = parser.getHeader();
	rawValue = parser.readData(size)
	long l = 0;
	long tmp = 0;
	l |= ((long) rawValue[0] << (56 - ((8 - rawValue.length) * 8)));
	for (int i = 1; i < rawValue.length; i++) {
		tmp = ((long) rawValue[rawValue.length - i]) << 56;
		tmp >>>= 56 - (8 * (i - 1));
		l |= tmp;
	}
	return l;
    }

}
