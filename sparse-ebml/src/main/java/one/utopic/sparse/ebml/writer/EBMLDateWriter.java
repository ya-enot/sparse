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
import java.util.Date;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.EBMLWriter;

import static one.utopic.sparse.ebml.util.EBMLHelper.*;

public class EBMLDateWriter implements EBMLWriter<EBMLFormatter, Date> {

    private final EBMLWriter<EBMLFormatter, Long> longWriter;

    public EBMLDateWriter(EBMLWriter<EBMLFormatter, Long> longWriter) {
        this.longWriter = longWriter;
    }

    public Part<EBMLFormatter> prepare(final Date o) throws IOException {
        return o == null ? null : longWriter.prepare((o.getTime() - UNIX_EPOCH_DELAY) * 1000000000);
    }

}
