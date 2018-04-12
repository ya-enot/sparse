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

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import one.utopic.abio.api.output.Output;;

public class ByteArrayStreamOutput implements Output {

	private final OutputStream bos;

	public ByteArrayStreamOutput(OutputStream os) {
		this.bos = os;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void writeByte(byte b) throws IOException {
		if (isFinished()) {
			throw new EOFException();
		}
		bos.write(b);
	}

}
