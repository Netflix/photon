package com.netflix.imflibrary.st2067_2;

import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.util.UUID;

@Test(groups = "unit")
public class CompositionPlaylistTest
{
    @Test
    public void testCompositionPlaylist() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("test_mapped_file_set/CPL_682feecb-7516-4d93-b533-f40d4ce60539.xml");
        CompositionPlaylist compositionPlaylist = new CompositionPlaylist(inputFile);
        Assert.assertTrue(compositionPlaylist.toString().length() > 0);
        Assert.assertEquals(compositionPlaylist.getEditRate().getNumerator(), 24);
        Assert.assertEquals(compositionPlaylist.getEditRate().getDenominator(), 1);
        Assert.assertEquals(compositionPlaylist.getUUID(), UUID.fromString("682feecb-7516-4d93-b533-f40d4ce60539"));

        UUID uuid = UUID.fromString("586286d2-c45f-4b2f-ad76-58eecd0202b4");
        Assert.assertEquals(compositionPlaylist.getVirtualTrackMap().size(), 2);
        CompositionPlaylist.VirtualTrack virtualTrack = compositionPlaylist.getVirtualTrackMap().get(uuid);
        Assert.assertEquals(virtualTrack.getSequenceTypeEnum(), CompositionPlaylist.SequenceTypeEnum.MainImageSequence);

        Assert.assertEquals(compositionPlaylist.getVirtualTrackResourceList().get(uuid).size(), 1);
    }
}
