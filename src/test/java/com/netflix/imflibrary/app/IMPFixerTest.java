package com.netflix.imflibrary.app;

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

@Test(groups = "unit")
public class IMPFixerTest {

    private static final Logger log = LoggerFactory.getLogger(IMPFixerTest.class);

    @Test
    public void testMain() {
        try {
            IMPFixer.main(new String[]{});
        } catch (Exception ex) {
            assert ex.getMessage().contains("Usage");
            assert ex.getClass() == IllegalArgumentException.class;
        }
    }

    public void testMainNoFile() {
        try {
            IMPFixer.main(new String[]{
                    "/bad/path.mxf",
                    "/bad/dir"
            });
        } catch (Exception ex) {
            assert ex.getClass() == FileNotFoundException.class;
            log.error("Exception: <{}>", ex.getMessage());
            assert ex.getMessage().endsWith("does not exist");
        }

    }

    public void testMainBadIMP() throws Exception {
        try {
            IMPFixer.main(new String[]{
                    TestHelper.findResourceByPath("test_mapped_file_set").getAbsolutePath(),
                    Files.createTempDirectory(null).toFile().getAbsolutePath()
            });
        } catch (IMFException ex) {
            assert ex.getMessage().contains("Unable to get header partition payload for file");
        }
    }

    public void testMainInvalidContentKind() throws Exception {
        File tempDir = Files.createTempDirectory(null).toFile();
        // todo: IMPFixer doesn't fail, but the resulting IMP fails IMPAnalyzer validation with invalid content kind
        IMPFixer.main(new String[]{
                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006").getAbsolutePath(),
                tempDir.getAbsolutePath()
        });
        assert tempDir.exists();
        try {
            IMPAnalyzer.main(new String[]{
                    tempDir.getAbsolutePath()
            });
        } catch (IMFException ex) {
            assert ex.getMessage().contains("invalid ContentKind");
        }
    }

}
