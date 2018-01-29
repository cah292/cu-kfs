package edu.cornell.kfs.vnd.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import edu.cornell.kfs.vnd.businessobject.VendorBatchDetail;

public class VendorBatchServiceImplParallelTest {

    private static final String TEST_FILE_01_NAME = "testFile01.txt";

    private TestVendorBatchServiceImpl vendorBatchService;

    @Before
    public void setUp() throws Exception {
        this.vendorBatchService = new TestVendorBatchServiceImpl();
    }

    @Test
    public void testBatchService() throws Exception {
        StringBuilder sequentialResults = new StringBuilder();
        StringBuilder parallelResults = new StringBuilder();
        
        long sequentialStart = System.currentTimeMillis();
        boolean sequentialSuccess = vendorBatchService.maintainVendorsSequential(TEST_FILE_01_NAME, null, sequentialResults);
        long sequentialEnd = System.currentTimeMillis();
        
        long parallelStart = System.currentTimeMillis();
        boolean parallelSuccess = vendorBatchService.maintainVendorsParallel(TEST_FILE_01_NAME, null, parallelResults);
        long parallelEnd = System.currentTimeMillis();
        
        System.out.println("---------------");
        System.out.println("Sequential run time in ms: " + (sequentialEnd - sequentialStart));
        System.out.println("Parallel run time in ms: " + (parallelEnd - parallelStart));
        System.out.println("---------------");
        System.out.println("Sequential full-success state: " + sequentialSuccess);
        System.out.println("Parallel full-success state: " + parallelSuccess);
        System.out.println("---------------");
        System.out.println("Sequential string result:");
        System.out.println(sequentialResults.toString());
        System.out.println("---------------");
        System.out.println("Parallel string result:");
        //System.out.println(parallelResults.toString());
    }

    public static class TestVendorBatchServiceImpl extends VendorBatchServiceImpl {
        @Override
        protected List<VendorBatchDetail> loadVendorDetailsFromFile(String fileName, BatchInputFileType batchInputFileType) {
            List<VendorBatchDetail> testDetails = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                VendorBatchDetail detail = new VendorBatchDetail();
                if (i % 4 != 0) {
                    detail.setVendorNumber("VND" + i);
                }
                testDetails.add(detail);
            }
            return testDetails;
        }
        
        @Override
        protected String addVendor(VendorBatchDetail vendorBatch) {
            pauseFor200ms();
            vendorBatch.setVendorNumber("VND" + vendorBatch.hashCode());
            return vendorBatch.getVendorNumber();
        }
        
        @Override
        protected String updateVendor(VendorBatchDetail vendorBatch) {
            pauseFor200ms();
            return vendorBatch.getVendorNumber();
        }
        
        protected void pauseFor200ms() {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                System.out.println("pauseFor200ms: Thread interrupted!");
            }
        }
    }

}
