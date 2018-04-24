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
package one.utopic.sparse.ebml.format;

import java.util.Date;

import one.utopic.sparse.ebml.EBMLFormat;

/**
 * Writes and reads integer data describing the distance in milliseconds from
 * 2001-01-01 00:00:00 UTC.
 */
public class DateFormat implements EBMLFormat<Date> {

    public static final DateFormat INSTANCE = new DateFormat(LongFormat.INSTANCE);

    public static final long UNIX_EPOCH_DELAY = 978307200000L; // milliseconds from 2001/01/01 00:00:00.000 UTC

    private final LongFormat longFormat;

    public DateFormat(LongFormat longFormat) {
        this.longFormat = longFormat;
    }

    @Override
    public Date readFormat(byte[] data) {
        return dateFromLong(longFormat.readFormat(data));
    }

    @Override
    public Writable getWritable(Date data) {
        return longFormat.getWritable(dateToLong(data));
    }

    private Date dateFromLong(Long date) {
        return new Date(date + UNIX_EPOCH_DELAY);
    }

    private long dateToLong(Date data) {
        return data.getTime() - UNIX_EPOCH_DELAY;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
