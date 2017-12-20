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
package one.utopic.sparse.ebml.writer;

import java.io.IOException;
import java.nio.charset.Charset;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.EBMLWriter;

public class EBMLStringWriter implements EBMLWriter<EBMLFormatter, String> {

    private final Charset cs;
    private final EBMLByteWriter byteWriter;

    public EBMLStringWriter(EBMLByteWriter byteWriter) {
        this(Charset.defaultCharset(), byteWriter);
    }

    public EBMLStringWriter(Charset cs, EBMLByteWriter byteWriter) {
        this.byteWriter = byteWriter;
        this.cs = cs;
    }

    public Part<EBMLFormatter> prepare(final String o) throws IOException {
        return o == null ? null : byteWriter.prepare(o.getBytes(cs));
    }

}