package edu.cornell.kfs.sys.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuBatchInputFileServiceImplParallelTest {

    private static final String TEST_FILE_BASE_PATH = "test/sys/batchFiles";
    private static final String TEST_FILE_PREFIX = TEST_FILE_BASE_PATH + "testFile";

    private CuBatchInputFileServiceImpl batchInputFileService;

    @Before
    public void setUp() throws Exception {
        batchInputFileService = new CuBatchInputFileServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestFileDirectory();
    }

    @Test
    public void testTenFiles() throws Exception {
        
    }

    private void doStuff(int fileCount) throws Exception {
        createTestFiles(fileCount);
        
        long sequentialStart = System.currentTimeMillis();
        batchInputFileService.listInputFileNamesWithDoneFile(null);
    }

    private void createTestFiles(int fileCount) throws IOException {
        for (int i = 1; i <= fileCount; i++) {
            String basePath = TEST_FILE_PREFIX + i;
            if (shouldCreateDataFileForIndex(i)) {
                File dataFile = new File(basePath + ".txt");
                FileUtils.writeStringToFile(dataFile, "This is a test!", StandardCharsets.UTF_8);
            }
            if (shouldCreateDoneFileForIndex(i)) {
                File doneFile = new File(basePath + ".done");
                FileUtils.touch(doneFile);
            }
        }
    }

    private boolean shouldCreateDataFileForIndex(int i) {
        return (i % 9 != 0) && (i % 13 != 0);
    }

    private boolean shouldCreateDoneFileForIndex(int i) {
        return (i % 7 != 0) && (i % 11 != 0);
    }

    private void deleteTestFileDirectory() throws IOException {
        File testFileDirectory = new File(TEST_FILE_BASE_PATH);
        if (testFileDirectory.exists() && testFileDirectory.isDirectory()) {
            FileUtils.forceDelete(testFileDirectory);
        }
    }

}
