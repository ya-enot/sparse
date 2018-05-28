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

import static one.utopic.sparse.ebml.test.util.TestUtil.decode;
import static one.utopic.sparse.ebml.test.util.TestUtil.encode;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.EBMLReader;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.EBMLWriter;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.LongFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

@DisplayName("EBMLFormatTest [" + EBMLFormatTest.FORMAT_CLASS_PACKAGE_NAME + "]")
public final class EBMLFormatTest {

    private static class TestData<T> {

        private final EBMLFormat<T> format;
        private final T rawData;
        private final byte[] formattedData;

        public TestData(EBMLFormat<T> format, T rawData, byte[] formattedData) {
            this.format = Objects.requireNonNull(format);
            this.formattedData = Objects.requireNonNull(formattedData);
            this.rawData = rawData;
        }

        public static <T> TestData<T> of(EBMLFormat<T> format, T rawData, byte[] formattedData) {
            return new TestData<T>(format, rawData, formattedData);
        }

        @Override
        public String toString() {
            return this.format.toString() + " " + prettyRawData(this.rawData);
        }

        private static final String DELIMITER = ", ";

        private static <T> String prettyRawData(T data) {
            if (data.getClass().isArray()) {
                StringBuilder sb = new StringBuilder();
                int length = Array.getLength(data);
                for (int i = 0; i < length; i++) {
                    if (i > 2 && i < length - 3) {
                        i = length - 3;
                        sb.append(DELIMITER);
                        sb.append("... ");
                    } else {
                        sb.append(DELIMITER);
                    }
                    sb.append(Array.get(data, i).toString());
                }
                return "[" + length + "] { " + sb.substring(DELIMITER.length()) + " }";
            } else {
                return data.toString();
            }
        }

    }

    protected static final String FORMAT_CLASS_PACKAGE_NAME = "one.utopic.sparse.ebml.format";

    private static final Set<Class<?>> FORMAT_CLASS_NAMES = getFormatClasses();

    private static final TestData<?>[] TEST_DATA;

    static {
        String testString = UUID.randomUUID().toString();
        byte[] testStringBytes = testString.getBytes();
        Date testDate = new Date();
        byte[] testDateBytes = formatLong(//
                (testDate.getTime() - DateFormat.UNIX_EPOCH_DELAY));
        TEST_DATA = new TestData[] { //
                TestData.of(BytesFormat.INSTANCE, testStringBytes, testStringBytes), //
                TestData.of(UTF8StringFormat.INSTANCE, testString, testStringBytes), //
                TestData.of(IntegerFormat.INSTANCE, 0, new byte[] {}), //
                TestData.of(IntegerFormat.INSTANCE, 1, new byte[] { 1 }), //
                TestData.of(IntegerFormat.INSTANCE, -1, new byte[] { -1 }), //
                TestData.of(IntegerFormat.INSTANCE, Integer.MAX_VALUE, new byte[] { 127, -1, -1, -1 }), //
                TestData.of(IntegerFormat.INSTANCE, Integer.MIN_VALUE, new byte[] { -128, 0, 0, 0 }), //
                TestData.of(LongFormat.INSTANCE, 0L, new byte[] {}), //
                TestData.of(LongFormat.INSTANCE, 1L, new byte[] { 1 }), //
                TestData.of(LongFormat.INSTANCE, -1L, new byte[] { -1 }), //
                TestData.of(LongFormat.INSTANCE, Long.MAX_VALUE, new byte[] { 127, -1, -1, -1, -1, -1, -1, -1 }), //
                TestData.of(LongFormat.INSTANCE, Long.MIN_VALUE, new byte[] { -128, 0, 0, 0, 0, 0, 0, 0 }), //
                TestData.of(DateFormat.INSTANCE, testDate, testDateBytes), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY), new byte[] {}), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY + 1), formatLong(1L)), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY - 1), formatLong(-1L)), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY + 1000), formatLong(1000L)), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY + Long.MAX_VALUE), formatLong(Long.MAX_VALUE)), //
                TestData.of(DateFormat.INSTANCE, new Date(DateFormat.UNIX_EPOCH_DELAY + Long.MIN_VALUE), formatLong(Long.MIN_VALUE)), //
        };
    }

    @DisplayName("FormatUnformatTest")
    @TestDataParam
    public <T> void formatUnformatTest(TestData<T> d) {
        assertGenericEquals(d.rawData, d.format.readFormat(d.formattedData));
        assertArrayEquals(d.formattedData, writeFormat(d.format, d.rawData));
    }

    private <T> byte[] writeFormat(EBMLFormat<T> format, T data) {
        return encode(w -> format.write(w, data));
    }

    private static byte[] formatLong(long value) {
        return BigInteger.valueOf(value).toByteArray();
    }

    @DisplayName("WriteReadTest")
    @TestDataParam
    public <T> void writeReadTest(TestData<T> d) throws IOException {
        assertArrayEquals(d.formattedData, encode(w -> {
            d.format.write(w, d.rawData);
        }));
        decode(d.formattedData, EBMLType.Context.EMPTY, r -> {
            T read = d.format.read(r);
            if (null != read && read.getClass().isArray()) {
                assertGenericEquals(d.rawData, read);
            } else {
                assertEquals(d.rawData, read);
            }
        });
    }

    static <T> void assertGenericEquals(T expect, T actual) {
        if (expect != actual) {
            assertNotNull(actual);
            assertNotNull(expect);
            if (expect.getClass().isArray() && actual.getClass().isArray()) {
                int length = Array.getLength(actual);
                assertEquals(Array.getLength(expect), length, () -> {
                    return expect + " not equals to " + actual + " by length";
                });
                for (int i = 0; i < length; i++) {
                    final int index = i;
                    assertEquals(Array.get(expect, i), Array.get(actual, i), () -> {
                        return expect + " not equals to " + actual + " at index " + index;
                    });
                }
            } else {
                assertEquals(expect, actual);
            }
        }
    }

    protected static TestData<?>[] getTestDataSets() {
        Set<Class<?>> missingFormatClasses = new HashSet<>(FORMAT_CLASS_NAMES);
        for (TestData<?> testData : TEST_DATA) {
            missingFormatClasses.remove(testData.format.getClass());
        }
        if (!missingFormatClasses.isEmpty()) {
            StringBuilder sb = new StringBuilder("TestData was not found for classes:");
            for (Class<?> missingFormatClass : missingFormatClasses) {
                sb.append("\n\t");
                sb.append(missingFormatClass.getName());
            }
            fail(sb.toString());
        }
        return TEST_DATA;
    }

    private static Set<Class<?>> getFormatClasses() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL formatPackageUrl = cl.getResource(FORMAT_CLASS_PACKAGE_NAME.replace('.', '/'));
        if (null == formatPackageUrl) {
            fail("Package " + FORMAT_CLASS_PACKAGE_NAME + " was not fount in " + EBMLFormatTest.class.getClassLoader());
        }
        Set<Class<?>> formats = new HashSet<>();
        try {
            File packageFolder = new File(formatPackageUrl.toURI());
            if (!packageFolder.isDirectory()) {
                fail(packageFolder + " is not a directory");
            }
            String[] classFileNames = packageFolder.list((dir, name) -> name.endsWith(".class"));
            Arrays.stream(classFileNames)//
                    .map(classFileName -> {
                        return classFileName.substring(0, classFileName.lastIndexOf('.'));
                    })//
                    .map(className -> {
                        try {
                            return cl.loadClass(FORMAT_CLASS_PACKAGE_NAME + "." + className);
                        } catch (ClassNotFoundException e) {
                            fail(e);
                        }
                        return null;
                    })//
                    .filter(c -> null != c && //
                            EBMLReader.EBMLReadFormat.class.isAssignableFrom(c) && //
                            EBMLWriter.EBMLWriteFormat.class.isAssignableFrom(c))//
                    .forEach(formats::add);
        } catch (URISyntaxException e) {
            fail(e);
        }
        return Collections.unmodifiableSet(formats);
    }

}

@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest(name = "{index}. {arguments}")
@MethodSource("getTestDataSets")
@interface TestDataParam {
}