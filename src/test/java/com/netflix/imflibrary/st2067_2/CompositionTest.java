package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.util.UUID;

@Test(groups = "unit")
public class CompositionTest
{
    @Test
    public void testCompositionPlaylist() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("test_mapped_file_set/CPL_682feecb-7516-4d93-b533-f40d4ce60539.xml");
        Composition composition = new Composition(inputFile, new IMFErrorLoggerImpl());
        Assert.assertTrue(Composition.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(composition.toString().length() > 0);
        Assert.assertEquals(composition.getEditRate().getNumerator().longValue(), 24);
        Assert.assertEquals(composition.getEditRate().getDenominator().longValue(), 1);
        Assert.assertEquals(composition.getUUID(), UUID.fromString("682feecb-7516-4d93-b533-f40d4ce60539"));

        UUID uuid = UUID.fromString("586286d2-c45f-4b2f-ad76-58eecd0202b4");
        Assert.assertEquals(composition.getVirtualTrackMap().size(), 2);
        Composition.VirtualTrack virtualTrack = composition.getVirtualTrackMap().get(uuid);
        Assert.assertEquals(virtualTrack.getSequenceTypeEnum(), Composition.SequenceTypeEnum.MainImageSequence);

        Assert.assertTrue(composition.getAnnotation().length() > 0);
        Assert.assertTrue(composition.getIssuer().length() > 0);
        Assert.assertTrue(composition.getCreator().length() > 0);
        Assert.assertTrue(composition.getContentOriginator().length() > 0);
        Assert.assertTrue(composition.getContentTitle().length() > 0);
    }
}
