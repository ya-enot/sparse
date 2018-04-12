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

import one.utopic.sparse.api.Event;

public class EBMLEvent implements Event<EBMLType> {

    private final EBMLType ebmlType;
    private final EventType eventType;

    public EBMLEvent(EBMLType ebmlType, EventType eventType) {
        this.ebmlType = ebmlType;
        this.eventType = eventType;
    }

    public EBMLType get() {
        return ebmlType;
    }

    public EventType getType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "EBMLEvent [eventType=" + eventType + ", ebmlType=" + ebmlType + "]";
    }

}
