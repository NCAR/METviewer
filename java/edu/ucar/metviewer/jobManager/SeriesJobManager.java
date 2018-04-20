/**
 * SeriesJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotDep;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVPlotJobParser;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.rscriptManager.RscriptAggStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptNoneStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptSumStatManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 13:13 $
 */
public class SeriesJobManager extends JobManager {


  public SeriesJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected void run(MVPlotJob job) throws Exception {


    //  determine if the plots require data aggregation
    boolean isAggStat = job.getAggCtc()
                            || job.getAggSl1l2()
                            || job.getAggSal1l2()
                            || job.getAggNbrCnt()
                            || job.getAggSsvar()
                            || job.getAggVl1l2()
                            || job.getAggVal1l2()
                            || job.getAggGrad()
                            || job.getAggPct();
    boolean isCalcStat = job.getCalcCtc()
                             || job.getCalcSl1l2()
                             || job.getCalcSal1l2()
                             || job.getCalcVl1l2()
                             || job.getCalcGrad();

    /*
     *  Build a plot for each permutation of <plot_fix> values
    */

    List<String> listQuery;
    AppDatabaseManager appDatabaseManager = mvBatch.getDatabaseManager();

    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap plotFixPerm : listPlotFixPerm) {

      mvBatch
          .print("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");

      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(),
                                                    plotFixPerm,
                                                    appDatabaseManager.getDateFormat());
      job.setTmplVal(fixTmplVal);
      //  if the independent variable uses a dependency, populate the values
      MVPlotDep depIndy = job.getIndyDep();
      if (null != depIndy) {
        String strDep = "";
        if (job.getTmplVal().containsKey(depIndy.getDepVar())) {
          strDep = appDatabaseManager.getDateFormat().format(
              MVUtil.PLOT_FORMAT.parse(job.getTmplVal().getStr(depIndy.getDepVar())));
        }
        String[][] listIndy = MVPlotJobParser.parseIndyNode(depIndy.getSpec(), strDep);
        job.setIndyVal(listIndy[0]);
        job.setIndyLabel(listIndy[1]);
      }

      //if it is a model job with attribute stat  - validate
      if ((job.isModeJob() && !job.isModeRatioJob())
              || (job.isMtdJob() && !job.isMtdRatioJob())) {
        validateModeSeriesDefinition(job);
      }

      //  build the SQL statements for the current plot
      listQuery = appDatabaseManager.buildPlotSql(job, plotFixPerm, mvBatch.getPrintStreamSql());
      for (String sql : listQuery) {
        mvBatch.printSql(sql + "\n");
      }


      Map.Entry[] listSeries2Val;
      if (null != job.getSeries2Val()) {
        listSeries2Val = job.getSeries2Val().getOrderedEntriesForSqlSeries();
      } else {
        listSeries2Val = new Map.Entry[]{};
      }

      MVOrderedMap mapDep2Plot = (MVOrderedMap) job.getDepGroups()[0].get("dep2");
      Map.Entry[] listDep2Plot;
      if (null != mapDep2Plot) {
        listDep2Plot = mapDep2Plot.getOrderedEntries();
      } else {
        listDep2Plot = new Map.Entry[]{};
      }

      // calculate the number of plot curves

      int intNumDep2 = 0;
      for (Map.Entry aListDep2Plot : listDep2Plot) {
        intNumDep2 += ((String[]) aListDep2Plot.getValue()).length;
      }
      int intNumSeries1Perm = 1;
      Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
      for (Map.Entry aListSeries1Val : listSeries1Val) {
        String[] listVal = (String[]) aListSeries1Val.getValue();
        intNumSeries1Perm *= listVal.length;
      }
      int intNumSeries2Perm = 1;
      for (Map.Entry aListSeries2Val : listSeries2Val) {
        intNumSeries2Perm *= ((String[]) aListSeries2Val.getValue()).length;
      }
      Map.Entry[] listDep1Plot = ((MVOrderedMap) job.getDepGroups()[0].get("dep1"))
                                     .getOrderedEntries();
      int intNumDep1 = getNumberPlotCurves(listDep1Plot);
      int intNumDep1Series = intNumDep1 * intNumSeries1Perm;
      int intNumDep2Series = intNumDep2 * intNumSeries2Perm;
      int intNumDepSeries = getNumDepSeries(intNumDep1Series, intNumDep2Series, job);
      intNumDepSeries = intNumDepSeries + job.getDiffSeries1Count();
      intNumDepSeries = intNumDepSeries + job.getDiffSeries2Count();


      //  validate the number of formatting elements
      if (intNumDepSeries > MVUtil.parseRCol(job.getPlotDisp()).length) {
        throw new Exception("length of plot_disp differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0
              && intNumDepSeries > MVUtil.parseRCol(job.getOrderSeries()).length) {
        throw new Exception("length of order_series differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getColors()).length) {
        throw new Exception("length of colors differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getPch()).length) {
        throw new Exception("length of pch differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getType()).length) {
        throw new Exception("length of type differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getLty()).length) {
        throw new Exception("length of lty differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getLwd()).length) {
        throw new Exception("length of lwd differs from number of series ("
                                + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty()
              && intNumDepSeries > MVUtil.parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series ("
                                + intNumDepSeries + ")");
      }

      if (intNumDepSeries > MVUtil.parseRCol(job.getShowSignif()).length) {
        throw new Exception("length of show_signif differs from number of series ("
                                + intNumDepSeries + ")");
      }
      validateNumDepSeries(job, intNumDepSeries);

      Map<String, String> info = createInfoMap(job, intNumDepSeries);


      RscriptStatManager rscriptStatManager = null;
      if (job.isModeJob() || job.isMtdJob() || isAggStat) {
        rscriptStatManager = new RscriptAggStatManager(mvBatch);
      } else if (isCalcStat) {
        rscriptStatManager = new RscriptSumStatManager(mvBatch);
      }

      //run summary or agg stats Rscripts - if needed
      if (rscriptStatManager != null) {
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
        rscriptStatManager.runRscript(job, info);
        //  turn off the event equalizer
        job.setEventEqual(Boolean.FALSE);
        info.put("event_equal", "FALSE");
        listQuery.clear();
      }

      //run main Rscript
      rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      String dataFileName = mvBatch.getDataFolder()
                                + MVUtil.buildTemplateString(job.getDataFileTmpl(),
                                                             MVUtil.addTmplValDep(job),
                                                             job.getTmplMaps(),
                                                             mvBatch.getPrintStream());
      File dataFile = new File(dataFileName);
      if (!listQuery.isEmpty() && !dataFile.exists()) {
        long intStartTime = new Date().getTime();
        dataFile.getParentFile().mkdirs();
        for (int i = 0; i < job.getCurrentDBName().size(); i++) {
          appDatabaseManager.executeQueriesAndSaveToFile(listQuery,
                                                         dataFileName,
                                                         job.getCalcCtc()
                                                             || job.getCalcSl1l2()
                                                             || job.getCalcSal1l2(),
                                                         job.getCurrentDBName().get(i),
                                                         i == 0);
        }
        mvBatch.print("Query returned  plot_data rows in "
                          + MVUtil.formatTimeSpan(new Date().getTime() - intStartTime));
      }

      rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
      info.put("data_file", dataFileName);


      boolean success = rscriptStatManager.runRscript(job, info);
      if (success) {
        mvBatch.print("Created plot " + rscriptStatManager.getPlotFile());
      } else {
        mvBatch.print("Failed to create plot " + rscriptStatManager.getPlotFile());
      }

    }

  }

  private void validateModeSeriesDefinition(MVPlotJob job) throws Exception {
    MVOrderedMap[] listDep = job.getDepGroups();
    for (int dep = 1; dep <= 2; dep++) {
      String[][] listFcstVarStat
          = MVUtil.buildFcstVarStatList((MVOrderedMap) listDep[0].get("dep" + dep));

      for (String[] aListFcstVarStat : listFcstVarStat) {
        String stat = aListFcstVarStat[1].split("_")[0];
        String type = aListFcstVarStat[1].split("_")[1];
        //validate for all attr stats except for those
        if (!stat.equals("CNT")
                && !stat.equals("CNTSUM")
                && !stat.equals("MAXINT")
                && !stat.equals("MAXINTF")
                && !stat.equals("MAXINTO")
                && type.startsWith("D")) {

          if (!type.equals("DCM")) {
            throw new Exception("Incorrect series definition. Stat "
                                    + stat + " can only have Cluster and Matched for Diff type");
          }
        }
      }
    }
  }

}
