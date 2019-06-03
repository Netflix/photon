package com.netflix.imflibrary.app;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.mock;

@Test(groups = "unit")
public class IMPFixerTest {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Usage.*")
    public void testMain() throws Exception {
        IMPFixer.main(new String[]{});
    }

    @Test
    public void testUsage() {
        String usageStr = IMPFixer.usage();
        assert usageStr.contains("Usage");
        assert usageStr.contains("input_package_directory");
        assert usageStr.contains("options");
        assert usageStr.contains("-cs");
        assert usageStr.contains("-nc");
        assert usageStr.contains("-nh");
    }

    @Test
    public void testValidateEssencePartitionNull() throws Exception {
        ResourceByteRangeProvider resourceByteRangeProvider = mock(ResourceByteRangeProvider.class);
        List<ErrorLogger.ErrorObject> errors = IMPFixer.validateEssencePartition(resourceByteRangeProvider);
        assert errors.stream().anyMatch(e -> e.getErrorDescription().contains("Failed to get header partition"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Usage.*")
    public void testMainCplSchemaBadArgs() throws Exception {
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("test_mapped_file_set").getAbsolutePath(),
                Files.createTempDirectory(null).toFile().getAbsolutePath(),
                "--cpl-schema", "2011"
        });
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Usage.*")
    public void testMainCplSchemaBadArgsNoCopy() throws Exception {
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("test_mapped_file_set").getAbsolutePath(),
                Files.createTempDirectory(null).toFile().getAbsolutePath(),
                "--cpl-schema", "2013",
                "--no-copy",
                "--no-hash",
                "blargh"
        });
        Assert.fail();
    }

    @Test(expectedExceptions = FileNotFoundException.class, expectedExceptionsMessageRegExp = ".* does not exist")
    public void testMainNoFile() throws Exception {
        IMPFixer.main(new String[] {
                "/bad/path.mxf",
                "/bad/dir"
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Invalid input package path:.*")
    public void testMainNotDirectory() throws Exception {
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("IMFTrackFiles/TearsOfSteel_4k_Test_Master_Audio_002.mxf").getAbsolutePath(),
                Files.createTempDirectory(null).toFile().getAbsolutePath()
        });
    }

    @Test(expectedExceptions = IMFException.class, expectedExceptionsMessageRegExp = "Unable to get header partition payload for file:.*")
    public void testMainBadIMP() throws Exception {
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("test_mapped_file_set").getAbsolutePath(),
                Files.createTempDirectory(null).toFile().getAbsolutePath()
        });
    }

    @Test(expectedExceptions = IMFException.class, expectedExceptionsMessageRegExp = ".*invalid ContentKind.*")
    public void testMainInvalidContentKind() throws Exception {
        File tempDir = Files.createTempDirectory(null).toFile();
        // todo: IMPFixer doesn't fail, but the resulting IMP fails IMPAnalyzer validation with invalid content kind
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006").getAbsolutePath(),
                tempDir.getAbsolutePath()
        });
        assert tempDir.exists();
        IMPAnalyzer.main(new String[] {
                tempDir.getAbsolutePath()
        });
    }

}
