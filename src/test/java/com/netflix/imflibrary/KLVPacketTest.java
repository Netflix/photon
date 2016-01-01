/*
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.imflibrary;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import com.netflix.imflibrary.utils.ByteProvider;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.*;

@Test(groups = "unit")
public class KLVPacketTest
{
    private static final byte[] KberL1 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, 0x7f};
    private static final byte[] KberL2 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] KberL3 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -1};   //0xff
    private static final byte[] KberL4 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -128}; //0x80
    private static final byte[] KberL5 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -127, 0x01}; //0x81 (length = 1)
    private static final byte[] KberL6 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -125, 0x00, 0x01, 0x01}; //0x83 (length = 257)
    private static final byte[] KberL7 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -121, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01}; //0x87 (length = 65793)
    private static final byte[] KberL8 = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00, -120}; //0x88
    private static final byte[] KberL9 = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x00, 0x01, 0x00, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00};

    private InputStream inputStream;

    @AfterMethod
    public void afterMethod() throws Exception
    {
        if (this.inputStream != null)
        {
            inputStream.close();
        }
    }

    @Test
    public void testBER1() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL1);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 1);
        assertEquals(header.getKLSize(), 17);
        assertEquals(header.getVSize(), 127);
        assertTrue(header.categoryDesignatorIsDictionaries());
        assertFalse(header.categoryDesignatorIsGroups());
        assertFalse(header.categoryDesignatorIsWrappersAndContainers());
        assertFalse(header.categoryDesignatorIsLabels());
        assertTrue(KLVPacket.isKLVFillItem(header.getKey()));
        assertTrue(header.toString().length() > 0);
    }

    @Test
    public void testBER2() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL2);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 1);
        assertEquals(header.getKLSize(), 17);
        assertEquals(header.getVSize(), 0);
    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "First byte of length field in KLV item equals 0xFF")
    public void testInvalidBER3() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL3);

        new KLVPacket.Header(byteProvider, 0L);
    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "First byte of length field in KLV item equals 0x80")
    public void testInvalidBER4() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL4);

        new KLVPacket.Header(byteProvider, 0L);
    }

    @Test
    public void testBER5() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL5);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 2);
        assertEquals(header.getKLSize(), 18);
        assertEquals(header.getVSize(), 1);
    }

    @Test
    public void testBER6() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL6);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 4);
        assertEquals(header.getKLSize(), 20);
        assertEquals(header.getVSize(), 257);
    }

    @Test
    public void testBER7() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL7);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 8);
        assertEquals(header.getKLSize(), 24);
        assertEquals(header.getVSize(), 65793);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Cannot read .*")
    public void testInvalidBER8() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL8);

        new KLVPacket.Header(byteProvider, 0L);
    }

    @Test
    public void testBER9() throws Exception
    {
        ByteProvider byteProvider = new ByteArrayDataProvider(KLVPacketTest.KberL9);

        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);

        assertEquals(header.getLSize(), 1);
        assertEquals(header.getKLSize(), 17);
        assertEquals(header.getVSize(), 0);
        assertFalse(KLVPacket.isKLVFillItem(header.getKey()));
    }

}
