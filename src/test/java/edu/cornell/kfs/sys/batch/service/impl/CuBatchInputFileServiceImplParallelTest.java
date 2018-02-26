package edu.cornell.kfs.sys.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.FlatFileParserBase;

public class CuBatchInputFileServiceImplParallelTest {

    private static final String TEST_FILE_BASE_PATH = "test/sys/batchFiles";
    private static final String TEST_FILE_PREFIX = TEST_FILE_BASE_PATH + "/testFile";

    private CuBatchInputFileServiceImpl batchInputFileService;
    private BatchInputFileType batchInputFileType;

    @Before
    public void setUp() throws Exception {
        batchInputFileService = new CuBatchInputFileServiceImpl();
        batchInputFileType = buildBatchInputFileType();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestFileDirectory();
    }

    @Test
    public void testBatchSizeOne() throws Exception {
        assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(1);
    }

    @Test
    public void testBatchSizeTen() throws Exception {
        assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(10);
    }

    @Test
    public void testBatchSizeFifty() throws Exception {
        assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(50);
    }

    @Test
    public void testBatchSizeOneHundred() throws Exception {
        assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(100);
    }

    @Test
    public void testBatchSizeOneThousand() throws Exception {
        assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(1000);
    }

    private void assertSequentialAndParallelOperationsYieldSameResultsForBatchSize(int batchCount) throws Exception {
        createTestFiles(batchCount);
        System.out.println("Testing batch size: " + batchCount);
        
        List<String> sequentialResults = runFileNameListingOperation("sequential", batchInputFileService::listInputFileNamesWithDoneFile);
        List<String> parallelResults = runFileNameListingOperation("parallel", batchInputFileService::listInputFileNamesWithDoneFileParallel);
        System.out.println();
        
        assertEquals("Wrong number of results from parallel search", sequentialResults.size(), parallelResults.size());
        assertEquals("Wrong elements returned by parallel search", sequentialResults, parallelResults);
    }

    private List<String> runFileNameListingOperation(String sequencingTitle, Function<BatchInputFileType, List<String>> fileNameOperation) {
        System.out.println("Running " + sequencingTitle + " file-listing operation");
        long startTime = System.currentTimeMillis();
        
        List<String> result = fileNameOperation.apply(batchInputFileType);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Finished " + sequencingTitle + " file-listing operation in " + executionTime + " ms");
        
        return result;
    }

    private BatchInputFileType buildBatchInputFileType() {
        FlatFileParserBase batchType = new FlatFileParserBase();
        batchType.setDirectoryPath(TEST_FILE_BASE_PATH);
        batchType.setFileExtension(".txt");
        return batchType;
    }

    private void createTestFiles(int batchCount) throws IOException {
        for (int i = 1; i <= batchCount; i++) {
            String basePath = TEST_FILE_PREFIX + i;
            if (shouldCreateDataFileForIndex(i)) {
                File dataFile = new File(basePath + ".txt");
                FileUtils.writeStringToFile(dataFile, "This is a test!", StandardCharsets.UTF_8, false);
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
            FileUtils.forceDelete(testFileDirectory.getAbsoluteFile());
        }
    }

}
