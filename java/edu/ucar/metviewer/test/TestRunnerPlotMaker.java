/**
 * AllTestRunner.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.test.util.TestUtil;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

import static edu.ucar.metviewer.test.util.TestUtil.cleanWorkingDirs;

/**
 * @author : tatiana $
 * @version : 1.0 : 11/Jul/14 13:54 $
 */
public class TestRunnerPlotMaker {

  public static void main(String[] args) {

    Result result;
    List<Failure> failureListLoadDataTest = null;
//    if (args.length > 0 ) {
//      TestUtil.ROOT_DIR = args[0];
//    }
    cleanWorkingDirs();
    if (args.length > 1 && args[1].equals("all")) {
      result = JUnitCore.runClasses(LoadDataTest.class);
      failureListLoadDataTest = result.getFailures();

    }

    result = JUnitCore.runClasses(CreatePlotBatchTest.class);
    List<Failure> failureListCreatePlotBatchTest = result.getFailures();


    result = JUnitCore.runClasses(TestMVServlet.class);
    List<Failure> failureListTestMVServlet = result.getFailures();

    System.out.println("*************************************************");
    if (failureListLoadDataTest != null) {

      for (Failure failure : failureListLoadDataTest) {
        System.out.println(failure.toString());
      }
      if (failureListLoadDataTest.isEmpty()) {
        System.out.println("***** Database loading tests finished successfully... *****");
      }

    } else {
      System.out.println("***** Database loading tests was ignored... *****");
    }
    System.out.println("*************************************************");
    System.out.println();
    System.out.println("*************************************************");
    for (Failure failure : failureListCreatePlotBatchTest) {
      System.out.println(failure.toString());
    }
    if (failureListCreatePlotBatchTest.isEmpty()) {
      System.out.println("***** Plot making tests finished successfully... *****");
    }
    System.out.println("*************************************************");
    System.out.println();
    System.out.println("*************************************************");
    for (Failure failure : failureListTestMVServlet) {
      System.out.println(failure.toString());
    }
    if (failureListTestMVServlet.isEmpty()) {
      System.out.println("***** Servlet tests finished successfully... *****");
    }
    int failureListLoadDataTestCount = failureListLoadDataTest == null ? 0 : failureListLoadDataTest.size();
    int failureListCreatePlotBatchTestCount = failureListCreatePlotBatchTest == null ? 0 : failureListCreatePlotBatchTest.size();
    int failureListTestMVServletCount = failureListTestMVServlet == null ? 0 : failureListTestMVServlet.size();
    int exitCode = failureListLoadDataTestCount + failureListCreatePlotBatchTestCount + failureListTestMVServletCount;
    System.out.println("*************************************************");
    System.out.println("There were " + exitCode + " failures");
    System.exit(exitCode);
  }
}
