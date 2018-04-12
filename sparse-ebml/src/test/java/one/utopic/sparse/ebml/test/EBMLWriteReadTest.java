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
package one.utopic.sparse.ebml.test;

import static one.utopic.sparse.ebml.test.util.TestType.TYPE_1_1;
import static one.utopic.sparse.ebml.test.util.TestType.TYPE_1_2;
import static one.utopic.sparse.ebml.test.util.TestType.TYPE_2_1;
import static one.utopic.sparse.ebml.test.util.TestType.TYPE_2_2;
import static one.utopic.sparse.ebml.test.util.TestUtil.decode;
import static one.utopic.sparse.ebml.test.util.TestUtil.encode;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import one.utopic.sparse.api.Event.CommonEventType;
import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;
import one.utopic.sparse.ebml.test.util.TestType.Context;

@DisplayName("EBMLWriteReadTest")
public class EBMLWriteReadTest {

    @DisplayName("DataSizeTest")
    @ParameterizedTest(name = "Size {arguments}K")
    @ValueSource(ints = { 1000, 100, 10, 1 })
    void dataSizeTest(int size) throws IOException {
        byte[] byteData = new byte[size * 1024];
        byte[] encData = encode(w -> {
            w.accept(new EBMLEvent(TYPE_1_1, EBMLEvent.CommonEventType.BEGIN));
            BytesFormat.INSTANCE.write(w, byteData);
            w.accept(new EBMLEvent(TYPE_1_1, EBMLEvent.CommonEventType.END));
        });
        decode(encData, Context.ROOT, r -> {
            while (r.hasNext()) {
                EBMLEvent event = r.next();
                if (CommonEventType.BEGIN.equals(event.getType())) {
                    if (TYPE_1_1.equals(event.get())) {
                        byte[] result = BytesFormat.INSTANCE.read(r);
                        assertArrayEquals(byteData, result);
                    }
                }
            }
        });
    }

    @DisplayName("HierarchyDeepTest")
    @ParameterizedTest(name = "Deep {arguments}")
    @ValueSource(ints = { 1000, 100, 10, 1 })
    void hierarchyDeepTest(int deep) {
        String data = UUID.randomUUID().toString();
        String otherData = UUID.randomUUID().toString();
        assertNotEquals(data, otherData);
        byte[] encData = encode(w -> {
            for (int i = 0; i < deep; i++) {
                w.accept(new EBMLEvent(TYPE_1_1, EBMLEvent.CommonEventType.BEGIN));
                {
                    w.accept(new EBMLEvent(TYPE_1_2, EBMLEvent.CommonEventType.BEGIN));
                    UTF8StringFormat.INSTANCE.write(w, otherData);
                    w.accept(new EBMLEvent(TYPE_1_2, EBMLEvent.CommonEventType.END));

                    w.accept(new EBMLEvent(TYPE_2_1, EBMLEvent.CommonEventType.BEGIN));
                    w.accept(new EBMLEvent(TYPE_2_2, EBMLEvent.CommonEventType.BEGIN));
                    UTF8StringFormat.INSTANCE.write(w, data);
                    w.accept(new EBMLEvent(TYPE_2_2, EBMLEvent.CommonEventType.END));
                    w.accept(new EBMLEvent(TYPE_2_1, EBMLEvent.CommonEventType.END));
                }
            }
            for (int i = 0; i < deep; i++) {
                w.accept(new EBMLEvent(TYPE_1_1, EBMLEvent.CommonEventType.END));
            }
            assertTrue(w.isEmpty());
        });
        decode(encData, Context.ROOT, r -> {
            int count = 0;
            while (r.hasNext()) {
                EBMLEvent event = r.next();
                if (CommonEventType.BEGIN.equals(event.getType())) {
                    if (TYPE_2_2.equals(event.get())) {
                        String result = UTF8StringFormat.INSTANCE.read(r);
                        assertEquals(data, result);
                        count++;
                    } else if (TYPE_1_2.equals(event.get())) {
                        String result = UTF8StringFormat.INSTANCE.read(r);
                        assertEquals(otherData, result);
                    }
                }
            }
            assertEquals(deep, count);
        });
    }

}
