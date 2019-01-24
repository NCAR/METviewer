/**
 * MysqlLoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.mysql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.MVLoadJob;
import edu.ucar.metviewer.MVLoadStatInsertData;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:19 $
 */
public class MysqlLoadDatabaseManager extends MysqlDatabaseManager implements edu.ucar.metviewer.db.LoadDatabaseManager {

  protected static final DateTimeFormatter DB_DATE_STAT_FORMAT
      = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
  private static final Logger logger = LogManager.getLogger("MysqlLoadDatabaseManager");
  private static final int INDEX_LINE_DATA = 1;
  private static final int INDEX_VAR_LENGTH = 3;
  private static final double[] X_POINTS_FOR_ECON = new double[]{
      0.952380952, 0.909090909, 0.800000000, 0.666666667, 0.500000000, 0.333333333,
      0.200000000, 0.125000000, 0.100000000, 0.055555556, 0.037037037, 0.025000000,
      0.016666667, 0.011111111, 0.007142857, 0.004761905, 0.002857143, 0.002000000
  };
  private final Pattern patIndexName = Pattern.compile("#([\\w\\d]+)#([\\w\\d]+)");
  private final Map<String, Integer> tableVarLengthLineDataId = new HashMap<>();
  private final Map<String, Integer> statHeaders = new HashMap<>();
  private final Map<String, Integer> modeHeaders = new HashMap<>();
  private final Map<String, Integer> mtdHeaders = new HashMap<>();
  private final String[] modeObjSingleColumns = new String[]{
      "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT", "CENTROID_LON", "AXIS_ANG",
      "LENGTH", "WIDTH", "AREA", "AREA_THRESH", "CURVATURE", "CURVATURE_X", "CURVATURE_Y",
      "COMPLEXITY", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75", "INTENSITY_90",
      "INTENSITY_50", "INTENSITY_SUM"};

  private final String[] mtdObj2dColumns = new String[]{
      "OBJECT_CAT", "TIME_INDEX", "AREA", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT",
      "CENTROID_LON", "AXIS_ANG"
  };
  private final String[] mtdObj3dSingleColumns = new String[]{
      "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_T", "CENTROID_LAT",
      "CENTROID_LON", "X_DOT", "Y_DOT ", "AXIS_ANG", "VOLUME", "START_TIME", "END_TIME",
      "CDIST_TRAVELLED", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75",
      "INTENSITY_90"
  };
  private final String[] listLineDataTables = {
      "line_data_fho", "line_data_ctc", "line_data_cts", "line_data_cnt", "line_data_pct",
      "line_data_pstd", "line_data_pjc", "line_data_prc", "line_data_sl1l2", "line_data_sal1l2",
      "line_data_vl1l2", "line_data_val1l2", "line_data_mpr", "line_data_nbrctc", "line_data_nbrcts",
      "line_data_nbrcnt", "line_data_isc", "line_data_mctc", "line_data_rhist", "line_data_orank",
      "line_data_relp", "line_data_eclv",
      "line_data_ssvar", "line_data_enscnt", "line_data_grad"
  };
  /*
   * variable length table names for each variable length output line type
   */
  private final Map<String, String> tableVarLengthTable;
  /*
   * data_file_lu_id values for each MET output type
   */
  private final Map<String, Integer> tableDataFileLU;
  private MVOrderedMap mapIndexes;

  public MysqlLoadDatabaseManager(edu.ucar.metviewer.db.DatabaseInfo databaseInfo) throws Exception {
    super(databaseInfo);
    mapIndexes = new MVOrderedMap();
    mapIndexes.put("#stat_header#_model_idx", "model");
    mapIndexes.put("#stat_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#stat_header#_fcst_lev_idx", "fcst_lev");
    mapIndexes.put("#stat_header#_obtype_idx", "obtype");
    mapIndexes.put("#stat_header#_vx_mask_idx", "vx_mask");
    mapIndexes.put("#stat_header#_interp_mthd_idx", "interp_mthd");
    mapIndexes.put("#stat_header#_interp_pnts_idx", "interp_pnts");
    mapIndexes.put("#stat_header#_fcst_thresh_idx", "fcst_thresh");

    mapIndexes.put("#mode_header#_model_idx", "model");
    mapIndexes.put("#mode_header#_fcst_lead_idx", "fcst_lead");
    mapIndexes.put("#mode_header#_fcst_valid_idx", "fcst_valid");
    mapIndexes.put("#mode_header#_fcst_init_idx", "fcst_init");
    mapIndexes.put("#mode_header#_fcst_rad_idx", "fcst_rad");
    mapIndexes.put("#mode_header#_fcst_thr_idx", "fcst_thr");
    mapIndexes.put("#mode_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#mode_header#_fcst_lev_idx", "fcst_lev");

    mapIndexes.put("#mtd_header#_model_idx", "model");
    mapIndexes.put("#mtd_header#_fcst_lead_idx", "fcst_lead");
    mapIndexes.put("#mtd_header#_fcst_valid_idx", "fcst_valid");
    mapIndexes.put("#mtd_header#_fcst_init_idx", "fcst_init");
    mapIndexes.put("#mtd_header#_fcst_rad_idx", "fcst_rad");
    mapIndexes.put("#mtd_header#_fcst_thr_idx", "fcst_thr");
    mapIndexes.put("#mtd_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#mtd_header#_fcst_lev_idx", "fcst_lev");


    for (String listLineDataTable : listLineDataTables) {
      mapIndexes.put("#" + listLineDataTable + "#_fcst_lead_idx", "fcst_lead");
      mapIndexes.put("#" + listLineDataTable + "#_fcst_valid_beg_idx", "fcst_valid_beg");
      mapIndexes.put("#" + listLineDataTable + "#_fcst_init_beg_idx", "fcst_init_beg");
    }
    tableVarLengthTable = new HashMap<>();
    tableVarLengthTable.put("PCT", "line_data_pct_thresh");
    tableVarLengthTable.put("PSTD", "line_data_pstd_thresh");
    tableVarLengthTable.put("PJC", "line_data_pjc_thresh");
    tableVarLengthTable.put("PRC", "line_data_prc_thresh");
    tableVarLengthTable.put("MCTC", "line_data_mctc_cnt");
    tableVarLengthTable.put("RHIST", "line_data_rhist_rank");
    tableVarLengthTable.put("RELP", "line_data_relp_ens");
    tableVarLengthTable.put("PHIST", "line_data_phist_bin");
    tableVarLengthTable.put("ORANK", "line_data_orank_ens");
    tableVarLengthTable.put("ECLV", "line_data_eclv_pnt");

    tableDataFileLU = new HashMap<>();
    tableDataFileLU.put("point_stat", 0);
    tableDataFileLU.put("grid_stat", 1);
    tableDataFileLU.put("mode_cts", 2);
    tableDataFileLU.put("mode_obj", 3);
    tableDataFileLU.put("wavelet_stat", 4);
    tableDataFileLU.put("ensemble_stat", 5);
    tableDataFileLU.put("vsdb_point_stat", 6);
    tableDataFileLU.put("stat", 7);
    tableDataFileLU.put("mtd_2d", 8);
    tableDataFileLU.put("mtd_3d_pc", 9);
    tableDataFileLU.put("mtd_3d_ps", 10);
    tableDataFileLU.put("mtd_3d_sc", 11);
    tableDataFileLU.put("mtd_3d_ss", 12);


    initVarLengthLineDataIds();
  }

  @Override
  public void applyIndexes() throws Exception {
    applyIndexes(false);
  }

  @Override
  public void dropIndexes() throws Exception {
    applyIndexes(true);
  }

  private void applyIndexes(boolean drop) throws Exception {

    logger.info("    ==== indexes ====\n" + (drop ? "  dropping..." : ""));
    Map.Entry[] listIndexes = mapIndexes.getOrderedEntries();
    for (Map.Entry listIndex : listIndexes) {
      String strIndexKey = listIndex.getKey().toString();
      String strField = listIndex.getValue().toString();
      long intIndexStart = System.currentTimeMillis();

      //  build a create index statment and run it
      Matcher matIndex = patIndexName.matcher(strIndexKey);
      if (!matIndex.matches()) {
        throw new Exception("  **  ERROR: failed to parse index key " + strIndexKey);
      }
      String strTable = matIndex.group(1);
      String strIndexName = strTable + matIndex.group(2);
      String strIndex;
      if (drop) {
        strIndex = "DROP INDEX " + strIndexName + " ON " + strTable + " ;";
      } else {
        strIndex = "CREATE INDEX " + strIndexName + " ON " + strTable + " (" + strField + ");";
      }
      try {
        executeUpdate(strIndex);
      } catch (Exception e) {
        logger.error(
            "  **  ERROR: caught " + e.getClass() + " applying index " + strIndexName + ": " + e.getMessage());
      }

      //  print out a performance message
      long intIndexTime = System.currentTimeMillis() - intIndexStart;
      logger.info(MVUtil.padBegin(strIndexName + ": ", 36) + MVUtil.formatTimeSpan(intIndexTime));
    }
    logger.info("");
  }

  /**
   * Executes the input update statement against the database underlying the input Connection and
   * cleans up any resources upon completion.
   *
   * @param update SQL UPDATE statement to execute
   * @return Number of records affected (output of Statement.executeUpdate() call)
   * @throws SQLException
   */
  private int executeUpdate(String update) throws Exception {

    int intRes;
    try (Connection con = getConnection();
         Statement stmt = con.createStatement()) {
      intRes = stmt.executeUpdate(update);
    } catch (SQLException se) {
      logger.error(update);
      throw new Exception("caught SQLException calling executeUpdate: " + se.getMessage());
    }

    return intRes;
  }

  /**
   * Initialize the table containing the max line_data_id for all line_data tables corresponding to
   * variable length rows. //* @param con database connection used to search against
   *
   * @throws Exception
   */
  private void initVarLengthLineDataIds() throws Exception {
    tableVarLengthLineDataId.clear();
    Set<String> lineTypes = tableVarLengthTable.keySet();
    for (String lineType : lineTypes) {
      String strVarLengthTable = "line_data_" + lineType.toLowerCase();
      int lineDataId = getNextId(strVarLengthTable, "line_data_id");
      tableVarLengthLineDataId.put(lineType, lineDataId);
    }
  }

  /**
   * Build and execute a query that retrieves the next table record id, whose name is specified by
   * the input field, from the specified input table. The statement is run against the input
   * Connection and the next available id is returned. // * @param con
   *
   * @param table Database table whose next available id is returned
   * @param field Field name of the table id record
   * @return Next available id
   * @throws Exception
   */
  private int getNextId(String table, String field) throws Exception {
    int intId = -1;
    PreparedStatement pstmt = null;
    ResultSet res = null;
    try (Connection con = getConnection()) {
      pstmt = con.prepareStatement("SELECT MAX(" + field + ") FROM " + table);
      res = pstmt.executeQuery();
      if (!res.next()) {
        throw new Exception("METviewer load error: getNextId(" + table + ", " + field + ") unable"
                                + " to find max id");
      }
      String strId = res.getString(1);
      if (null == strId) {
        intId = 0;
      } else {
        intId = Integer.parseInt(strId) + 1;
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
      if (res != null) {
        res.close();
      }
    }

    return intId;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolStatHeaderTableCheck) or
   * by searching the stat_header table for a duplicate (specified by statHeaderDBCheck). Records in
   * line_data tables, stat_group tables and line_data_thresh tables are created from the data in
   * the input file. If necessary, records in the stat_header table are created as well.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws Exception
   */
  @Override
  public Map<String, Long> loadStatFile(final DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();
    //  initialize the insert data structure
    MVLoadStatInsertData insertData = new MVLoadStatInsertData();
    //  performance counters
    long statHeaderLoadStart = System.currentTimeMillis();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);

    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String filename = info.path + "/" + info.filename;
    int intLine = 0;
    List<String> headerNames = new ArrayList<>();
    try (
        FileReader fileReader = new FileReader(filename);
        BufferedReader reader = new BufferedReader(fileReader)) {
      //  read in each line of the input file
      while (reader.ready()) {
        String[] listToken = reader.readLine().split("\\s+");
        intLine++;

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          continue;
        }


        //  if the line type load selector is activated, check that the current line type is on the list
        insertData.setLineType(MVUtil.findValue(listToken, headerNames, "LINE_TYPE"));
        if (!MVUtil.isValidLineType(insertData.getLineType())) {
          logger.warn(
              "  **  WARNING: unexpected line type: " + insertData.getLineType()
                  + "  the line will be ignored     ");
          continue;
        }

        if (info.lineTypeLoad && !info.tableLineTypeLoad.containsKey(insertData.getLineType())) {
          continue;
        }

        insertData.setFileLine(filename + ":" + intLine);


        //  parse the valid time

        LocalDateTime fcstValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "FCST_VALID_BEG"),
            DB_DATE_STAT_FORMAT);


        LocalDateTime fcstValidEnd = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "FCST_VALID_END"),
            DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "OBS_VALID_BEG"),
            DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidEnd = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "OBS_VALID_END"),
            DB_DATE_STAT_FORMAT);

        //  format the valid times for the database insert
        String fcstValidBegStr = DATE_FORMAT_1.format(fcstValidBeg);
        String fcstValidEndStr = DATE_FORMAT_1.format(fcstValidEnd);
        String obsValidBegStr = DATE_FORMAT_1.format(obsValidBeg);
        String obsValidEndStr = DATE_FORMAT_1.format(obsValidEnd);

        //  calculate the number of seconds corresponding to fcst_lead
        String fcstLeadStr = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int fcstLeadLen = fcstLeadStr.length();
        int fcstLeadSec = Integer.parseInt(fcstLeadStr.substring(fcstLeadLen - 2, fcstLeadLen));
        fcstLeadSec += Integer
                           .parseInt(fcstLeadStr.substring(fcstLeadLen - 4, fcstLeadLen - 2)) * 60;
        fcstLeadSec += Integer.parseInt(fcstLeadStr.substring(0, fcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead

        LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
        fcstInitBeg = fcstInitBeg.minusSeconds(fcstLeadSec);

        String fcstInitBegStr = DATE_FORMAT_1.format(fcstInitBeg);

        //  ensure that the interp_pnts field value is a reasonable integer
        String strInterpPnts = MVUtil.findValue(listToken, headerNames, "INTERP_PNTS");
        if (strInterpPnts.equals("NA")) {
          strInterpPnts = "0";
        }

        String lineType = insertData.getLineType();

        //  do not load matched pair lines or orank lines
        if ((!info.loadMpr && lineType.equals("MPR"))
                || (!info.loadOrank && lineType.equals("ORANK"))) {
          continue;
        }


        /*
         * * * *  stat_header insert  * * * *
         */
        headerRecords++;

        //  build the stat_header value list for this line

        List<Object> statHeaderValueList = new ArrayList<>();
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "VERSION"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "MODEL"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "DESC"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_LEV"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBTYPE"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "VX_MASK"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "INTERP_MTHD"));
        statHeaderValueList.add(strInterpPnts);
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_THRESH"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_THRESH"));


        //  build a where clause for searching for duplicate stat_header records
        String statHeaderWhere = BINARY + "  model = '"
                                     + MVUtil.findValue(listToken, headerNames, "MODEL")
                                     + "' AND " + BINARY + "descr = '"
                                     + MVUtil.findValue(listToken, headerNames, "DESC")
                                     + "'  AND " + BINARY + "fcst_var = '"
                                     + MVUtil.findValue(listToken, headerNames, "FCST_VAR")
                                     + "'  AND " + BINARY + "fcst_lev = '"
                                     + MVUtil.findValue(listToken, headerNames, "FCST_LEV")
                                     + "'  AND " + BINARY + "obtype = '"
                                     + MVUtil.findValue(listToken, headerNames, "OBTYPE")
                                     + "'  AND " + BINARY + "vx_mask = '"
                                     + MVUtil.findValue(listToken, headerNames, "VX_MASK")
                                     + "'  AND " + BINARY + "interp_mthd = '"
                                     + MVUtil.findValue(listToken, headerNames, "INTERP_MTHD")
                                     + "'  AND interp_pnts = " + strInterpPnts
                                     + "  AND " + BINARY + "fcst_thresh = '"
                                     + MVUtil.findValue(listToken, headerNames, "FCST_THRESH")
                                     + "'  AND " + BINARY + "obs_thresh = '"
                                     + MVUtil.findValue(listToken, headerNames, "OBS_THRESH")
                                     + "'";

        //  build the value list for the stat_header insert
        StringBuilder csvBuilder = new StringBuilder();
        for (int i = 0; i < statHeaderValueList.size(); i++) {
          Object value = statHeaderValueList.get(i);
          if (MVUtil.isNumeric(value)) {
            csvBuilder.append(value);
          } else {
            csvBuilder.append("'").append(value).append("'");
          }
          if (i != statHeaderValueList.size() - 1) {
            csvBuilder.append(MVUtil.SEPARATOR);
          }
        }
        String statHeaderValue = csvBuilder.toString();

        String fileLine = filename + ":" + intLine;

        //  look for the header key in the table
        Integer statHeaderId = -1;
        if (statHeaders.containsKey(statHeaderValue)) {
          statHeaderId = statHeaders.get(statHeaderValue);
        }

        //  if the stat_header does not yet exist, create one
        else {

          //  look for an existing stat_header record with the same information
          boolean foundStatHeader = false;
          long statHeaderSearchBegin = System.currentTimeMillis();
          if (info.statHeaderDBCheck) {
            String statHeaderSelect = "SELECT stat_header_id FROM  stat_header WHERE" +
                                          statHeaderWhere;
            Connection con = null;
            Statement stmt = null;
            ResultSet res = null;
            try {
              con = getConnection();
              stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                         java.sql.ResultSet.CONCUR_READ_ONLY);
              res = stmt.executeQuery(statHeaderSelect);
              if (res.next()) {
                String strStatHeaderIdDup = res.getString(1);
                statHeaderId = Integer.parseInt(strStatHeaderIdDup);
                foundStatHeader = true;
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
            } finally {
              try {
                res.close();
              } catch (Exception e) { /* ignored */ }
              try {
                stmt.close();
              } catch (Exception e) { /* ignored */ }
              try {
                con.close();
              } catch (Exception e) { /* ignored */ }
            }
          }

          timeStats.put("headerSearchTime",
                        timeStats.get("headerSearchTime") + System.currentTimeMillis()
                            - statHeaderSearchBegin);


          //  if the stat_header was not found, add it to the table
          if (!foundStatHeader) {

            statHeaderId = intStatHeaderIdNext++;
            statHeaders.put(statHeaderValue, statHeaderId);

            //  build an insert statement for the mode header
            statHeaderValue = Integer.toString(statHeaderId) + ", " + statHeaderValue;

            //  insert the record into the stat_header database table
            String statHeaderInsertSql = "INSERT INTO stat_header VALUES ("
                                             + statHeaderValue + ");";
            int intStatHeaderInsert;
            try {
              intStatHeaderInsert = executeUpdate(statHeaderInsertSql);
              if (1 != intStatHeaderInsert) {
                logger.warn(
                    "  **  WARNING: unexpected result from stat_header INSERT: "
                        + intStatHeaderInsert + "\n        " + fileLine);
                statHeaderId = null;
              } else {
                headerInserts++;
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
              statHeaderId = null;
            }

          } else {
            statHeaders.put(statHeaderValue, statHeaderId);
          }
        }
        boolean isMet8 = true;
        if (insertData.getLineType().equals("RHIST")) {
          int indexOfNrank = headerNames.indexOf("LINE_TYPE") + 2;
          boolean isInt = MVUtil.isInteger(listToken[indexOfNrank], 10);
          isMet8 = isInt
                       && (Integer.valueOf(listToken[indexOfNrank])
                               + indexOfNrank == listToken.length - 1);

        }
        if (statHeaderId != null) {

          int lineDataMax = listToken.length;
          String lineDataId = "";
          dataRecords++;

          //  if the line type is of variable length, get the line_data_id
          boolean hasVarLengthGroups = MVUtil.lengthGroupIndices
                                           .containsKey(insertData.getLineType());

          //  determine the maximum token index for the data
          if (hasVarLengthGroups) {
            int intLineDataId = tableVarLengthLineDataId.get(lineType);
            lineDataId = Integer.toString(intLineDataId);
            tableVarLengthLineDataId.put(lineType, intLineDataId + 1);
            int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices
                                                   .get(insertData.getLineType());
            int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1,
                                                            listVarLengthGroupIndices1.length);
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
              listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;

            }

            switch (insertData.getLineType()) {
              case "RHIST":

                if (!isMet8) {
                  //old met data
                  listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] + 2;
                  listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] + 2;

                }
                lineDataMax = lineDataMax - Integer.valueOf(
                    listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
                break;
              case "PSTD":
                lineDataMax = lineDataMax - Integer.valueOf(
                    listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
                break;
              default:
                lineDataMax = listVarLengthGroupIndices[1];
                break;
            }
          }

          //  build the value list for the insert statement
          List<Object> lineDataValues = new ArrayList<>();

          if (!lineDataId.isEmpty()) {
            lineDataValues.add(lineDataId);
          }
          lineDataValues.add(statHeaderId);
          lineDataValues.add(info.fileId);
          lineDataValues.add(intLine);
          lineDataValues.add(fcstLeadStr);
          lineDataValues.add(fcstValidBegStr);
          lineDataValues.add(fcstValidEndStr);
          lineDataValues.add(fcstInitBegStr);
          lineDataValues.add(MVUtil.findValue(listToken, headerNames, "OBS_LEAD"));
          lineDataValues.add(obsValidBegStr);
          lineDataValues.add(obsValidEndStr);

          //  if the line data requires a cov_thresh value, add it
          String covThresh = MVUtil.findValue(listToken, headerNames, "COV_THRESH");
          if (MVUtil.covThreshLineTypes.containsKey(insertData.getLineType())) {
            lineDataValues.add(replaceInvalidValues(covThresh));
          }

          //  if the line data requires an alpha value, add it
          String alpha = MVUtil.findValue(listToken, headerNames, "ALPHA");
          if (MVUtil.alphaLineTypes.containsKey(insertData.getLineType())) {
            if (alpha.equals("NA")) {
              logger.warn("  **  WARNING: alpha value NA with line type '"
                              + insertData.getLineType() + "'\n        "
                              + insertData.getFileLine());
            }
            lineDataValues.add(replaceInvalidValues(alpha));
          } else if (!alpha.equals("NA")) {
            logger.warn(
                "  **  WARNING: unexpected alpha value '" + alpha + "' in line type '"
                    + insertData.getLineType() + "'\n        "
                    + insertData.getFileLine());
          }

          //  add total and all of the stats on the rest of the line to the value list
          if (lineType.equals("RHIST")) {
            int lineTypeIndex = headerNames.indexOf("LINE_TYPE");
            for (int i = lineTypeIndex + 1; i < lineDataMax; i++) {
              if (!isMet8) {
                //skip crps ,ign,crpss, spread
                if (i == lineTypeIndex + 2 || i == lineTypeIndex + 3
                        || i == lineTypeIndex + 5 || i == lineTypeIndex + 6) {
                  continue;
                }
              }
              lineDataValues.add(replaceInvalidValues(listToken[i]));
            }
            if (!isMet8) {
              //insert crps ,ign,crpss, spread to ECNT table
              List<Object> ecntLineDataValues = new ArrayList<>(lineDataValues);
              int indexOfNrankOld = headerNames.indexOf("LINE_TYPE") + 4;
              boolean isMetOld = (Double.valueOf(listToken[indexOfNrankOld])
                                      .intValue() + indexOfNrankOld) ==
                                     listToken.length - 1;

              ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 2]));
              if (isMetOld) {
                ecntLineDataValues.add(-9999);
              } else {
                ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 5]));
              }
              ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 3]));
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              if (isMetOld) {
                ecntLineDataValues.add(-9999);
              } else {
                ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 6]));
              }
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              csvBuilder = new StringBuilder();

              for (int i = 1; i < ecntLineDataValues.size(); i++) {
                Object value = ecntLineDataValues.get(i);
                if (MVUtil.isNumeric(value)) {
                  csvBuilder.append(value);
                } else {
                  csvBuilder.append("'").append(value).append("'");
                }
                if (i != ecntLineDataValues.size() - 1) {
                  csvBuilder.append(MVUtil.SEPARATOR);
                }
              }
              String csv = csvBuilder.toString();
              if (!insertData.getTableLineDataValues().containsKey("ECNT")) {
                insertData.getTableLineDataValues().put("ECNT", new ArrayList<>());
              }
              insertData.getTableLineDataValues().get("ECNT").add("(" + csv + ")");
              dataInserts++;
            }
          } else {
            for (int i = headerNames.indexOf("LINE_TYPE") + 1; i < lineDataMax; i++) {
              //  add the stats in order
              lineDataValues.add(replaceInvalidValues(listToken[i]));

            }
          }


          if (lineType.equals("ORANK")) {
            //skip ensemble fields and get data for the rest
            int[] varLengthGroupIndices1 = MVUtil.lengthGroupIndices.get(insertData.getLineType());
            int[] varLengthGroupIndices = Arrays.copyOf(varLengthGroupIndices1,
                                                        varLengthGroupIndices1.length);
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              varLengthGroupIndices[0] = varLengthGroupIndices[0] - 1;
              varLengthGroupIndices[1] = varLengthGroupIndices[1] - 1;
            }
            int extraFieldsInd = lineDataMax + Integer.valueOf(
                listToken[varLengthGroupIndices[0]]) * varLengthGroupIndices[2];
            for (int i = extraFieldsInd; i < listToken.length; i++) {
              lineDataValues.add(replaceInvalidValues(listToken[i]));
            }
          }


          int size = lineDataValues.size();
          int maxSize = size;
          switch (lineType) {
            case "CNT":
              maxSize = 105;
              break;
            case "MPR":
              maxSize = 24;
              break;
            case "ORANK":
              maxSize = 30;
              break;
            case "CTS":
              maxSize = 104;
              break;
            case "NBRCTS":
              maxSize = 105;
              break;
            case "NBRCNT":
              maxSize = 30;
              break;
            case "SAL1L2":
              maxSize = 17;
              break;
            case "SL1L2":
              maxSize = 17;
              break;
            case "GRAD":
              maxSize = 18;
              break;
            case "PSTD":
              maxSize = 30;
              break;
            case "SSVAR":
              maxSize = 46;
              break;

            case "VL1L2":
              maxSize = 20;
              break;
            case "ECNT":
              maxSize = 22;
              break;

            default:
          }
          while (size < maxSize) {
            lineDataValues.add(-9999);
            size++;
          }


          csvBuilder = new StringBuilder();
          for (int i = 0; i < lineDataValues.size(); i++) {
            Object value = lineDataValues.get(i);
            if (MVUtil.isNumeric(value)) {
              csvBuilder.append(value);
            } else {
              csvBuilder.append("'").append(value).append("'");
            }
            if (i != lineDataValues.size() - 1) {
              csvBuilder.append(MVUtil.SEPARATOR);
            }
          }
          String csv = csvBuilder.toString();

          //  add the values list to the line type values map
          if (!insertData.getTableLineDataValues().containsKey(insertData.getLineType())) {
            insertData.getTableLineDataValues().put(insertData.getLineType(), new ArrayList<>());
          }
          insertData.getTableLineDataValues().get(insertData.getLineType()).add("(" + csv + ")");

          dataInserts++;


          /*
           * * * *  var_length insert  * * * *
           */

          if (hasVarLengthGroups) {

            //  get the index information about the current line type
            int[] varLengthGroupIndices1 = MVUtil.lengthGroupIndices.get(insertData.getLineType());
            int[] varLengthGroupIndices = Arrays.copyOf(varLengthGroupIndices1,
                                                        varLengthGroupIndices1.length);
            if (lineType.equals("RHIST")) {
              if (!isMet8) {
                //old met data
                varLengthGroupIndices[0] = varLengthGroupIndices[0] + 2;
                varLengthGroupIndices[1] = varLengthGroupIndices[1] + 2;

              }
            }
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              varLengthGroupIndices[0] = varLengthGroupIndices[0] - 1;
              varLengthGroupIndices[1] = varLengthGroupIndices[1] - 1;
            }
            int groupCntIndex = varLengthGroupIndices[0];
            int groupIndex = varLengthGroupIndices[1];
            int groupSize = varLengthGroupIndices[2];
            int numGroups = Integer.parseInt(listToken[groupCntIndex]);

            if (insertData.getLineType().equals("PCT")
                    || insertData.getLineType().equals("PJC")
                    || insertData.getLineType().equals("PRC")) {
              numGroups -= 1;
            }
            List<String> threshValues = insertData.getTableVarLengthValues()
                                            .get(insertData.getLineType());
            if (null == threshValues) {
              threshValues = new ArrayList<>();
            }

            //  build a insert value statement for each threshold group
            if (insertData.getLineType().equals("MCTC")) {
              for (int i = 0; i < numGroups; i++) {
                for (int j = 0; j < numGroups; j++) {
                  threshValues.add("(" + lineDataId + ", " + (i + 1) + ", " + (j + 1) + ", " +
                                       replaceInvalidValues(listToken[groupIndex++]) + ")");
                  lengthRecords++;
                }
              }
            } else {
              if (insertData.getLineType().equals("RHIST")
                      || insertData.getLineType().equals("PSTD")) {
                groupIndex = lineDataMax;
              }
              for (int i = 0; i < numGroups; i++) {
                String threshValuesStr = "(" + lineDataId + "," + (i + 1);
                for (int j = 0; j < groupSize; j++) {
                  threshValuesStr += ", " + replaceInvalidValues(listToken[groupIndex++]);
                }
                threshValuesStr += ")";
                threshValues.add(threshValuesStr);
                lengthRecords++;
              }
            }
            insertData.getTableVarLengthValues().put(insertData.getLineType(), threshValues);
          }

          //  if the insert threshold has been reached, commit the stored data to the database
          if (info.insertSize <= insertData.getListInsertValues().size()) {
            int[] listInserts = commitStatData(insertData);
            dataInserts += listInserts[INDEX_LINE_DATA];
            lengthInserts += listInserts[INDEX_VAR_LENGTH];
          }
        }
      }  // end: while( reader.ready() )

    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(insertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];


    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);


    //  print a performance report
    long statHeaderLoadTime = System.currentTimeMillis() - statHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (statHeaderLoadTime);

    if (info.verbose) {
      logger.info(MVUtil.padBegin("file lines: ", 36) + (intLine - 1) + "\n" +
                      MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
                      MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
                      MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
                      MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
                      MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
                      MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
                      MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          statHeaderLoadTime) + "\n" +
                      MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
          headerSearchTime) + "\n" +
                      MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(
          dblLinesPerMSec) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method
   * was designed to be called from loadStatFile(), which is responsible for building insert value
   * lists for the various types of grid_stat and point_stat database tables.
   *
   * @param mvLoadStatInsertData Data structure loaded with insert value lists
   * @return An array of four integers, indexed by the INDEX_* members, representing the number of
   * database inserts of each type
   * @throws Exception
   */
  private int[] commitStatData(MVLoadStatInsertData mvLoadStatInsertData)
      throws Exception {

    int[] listInserts = new int[]{0, 0, 0, 0};

    /*
     * * * *  stat_header was committed commit  * * * *
     */

    mvLoadStatInsertData.getListInsertValues().clear();


    /*
     * * * *  line_data commit  * * * *
     */

    //  for each line type, build an insert statement with the appropriate list of values
    for (Map.Entry<String, List<String>> entry : mvLoadStatInsertData.getTableLineDataValues()
                                                     .entrySet()) {
      mvLoadStatInsertData.setLineType(entry.getKey());
      ArrayList listValues = (ArrayList) entry.getValue();
      String strLineDataTable = "line_data_" + mvLoadStatInsertData.getLineType()
                                                   .toLowerCase(Locale.US);

      int resLineDataInsertCount = executeBatch(listValues, strLineDataTable);
      if (listValues.size() != resLineDataInsertCount) {
        logger.warn("  **  WARNING: unexpected result from line_data INSERT: " +
                        resLineDataInsertCount + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      listInserts[INDEX_LINE_DATA]++;
    }
    mvLoadStatInsertData.getTableLineDataValues().clear();


    /*
     * * * *  stat_group commit  * * * *
     */

    //  build a stat_group insert with all stored values
    if (!mvLoadStatInsertData.getListStatGroupInsertValues().isEmpty()) {
      String statGroupInsertValues = "";
      for (int i = 0; i < mvLoadStatInsertData.getListStatGroupInsertValues().size(); i++) {
        statGroupInsertValues += (i == 0 ? "" : ", ") + mvLoadStatInsertData
                                                            .getListStatGroupInsertValues()
                                                            .get(i);
      }
      String statGroupInsert = "INSERT INTO stat_group VALUES " + statGroupInsertValues + ";";
      int statGroupInsertCount = executeUpdate(statGroupInsert);
      if (mvLoadStatInsertData.getListStatGroupInsertValues().size() != statGroupInsertCount) {
        logger.warn(
            "  **  WARNING: unexpected result from stat_group INSERT: " + statGroupInsertCount + " vs. " +
                mvLoadStatInsertData.getListStatGroupInsertValues()
                    .size() + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      int indexStatGroup = 2;
      listInserts[indexStatGroup]++;
    }
    mvLoadStatInsertData.getListStatGroupInsertValues().clear();

    /*
     * * * *  variable length data commit  * * * *
     */

    //  insert probabilistic data into the thresh tables
    Set<String> strings = mvLoadStatInsertData.getTableVarLengthValues().keySet();
    String[] varLengthTypes = strings.toArray(new String[strings.size()]);


    for (String listVarLengthType : varLengthTypes) {
      String[] listVarLengthValues = MVUtil.toArray(
          mvLoadStatInsertData.getTableVarLengthValues().get(listVarLengthType));
      if (1 > listVarLengthValues.length) {
        continue;
      }
      String strVarLengthTable = tableVarLengthTable.get(listVarLengthType);
      String strThreshInsert = "INSERT INTO " + strVarLengthTable + " VALUES ";
      for (int j = 0; j < listVarLengthValues.length; j++) {
        strThreshInsert += (0 < j ? ", " : "") + listVarLengthValues[j];
        listInserts[INDEX_VAR_LENGTH]++; //  lengthInserts++;
      }
      int intThreshInsert = executeUpdate(strThreshInsert);
      if (listVarLengthValues.length != intThreshInsert) {
        logger.warn(
            "  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " +
                listVarLengthValues.length + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      mvLoadStatInsertData.getTableVarLengthValues().put(listVarLengthType, new ArrayList<>());
    }

    return listInserts;
  }

  @Override
  public Map<String, Long> loadStatFileVSDB(DataFileInfo info) throws Exception {

    Map<String, Long> timeStats = new HashMap<>();

    //  initialize the insert data structure
    MVLoadStatInsertData mvLoadStatInsertData = new MVLoadStatInsertData();

    //  performance counters
    long intStatHeaderLoadStart = System.currentTimeMillis();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);


    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String strFilename = info.path + "/" + info.filename;
    String ensValue = "";
    String[] ensValueArr = info.path.split("\\/");
    if (ensValueArr[ensValueArr.length - 1].contains("_")) {
      String[] ensValue1 = ensValueArr[ensValueArr.length - 1].split("_");
      ensValue = "_" + ensValue1[ensValue1.length - 1];
    }

    int intLine = 0;
    try (FileReader fileReader = new FileReader(
        strFilename); BufferedReader reader = new BufferedReader(fileReader)) {
      List<String> allMatches;

      DateTimeFormatter formatStatVsdb = DateTimeFormatter.ofPattern("yyyyMMddHH");


      //  read in each line of the input file, remove "="
      while (reader.ready()) {

        String line = reader.readLine();
        try {
          line = line.replaceAll("\\s=\\s", " "); // remove " = "
          Matcher m = Pattern.compile("\\d-0\\.").matcher(
              line); // some records do not have a space between columns if the value in column starts with "-"

          allMatches = new ArrayList<>();
          while (m.find()) {
            allMatches.add(m.group());
          }
          for (String match : allMatches) {
            String newStr = match.replace("-", " -");
            line = line.replace(match, newStr);
          }

          String[] listToken = line.split("\\s+");
          intLine++;
          String thresh = "NA";
          String modelName = listToken[1];

          if (listToken[6].equals("BSS") || listToken[6].equals("ECON")
                  || listToken[6].equals("HIST") || listToken[6].equals("RELI")
                  || listToken[6].equals("RELP") || listToken[6].equals("RMSE")
                  || listToken[6].equals("RPS")) {
            modelName = modelName.split("\\/")[0] + ensValue;
          }

          //  if the line type load selector is activated, check that the current line type is on the list

          if (listToken[6].equals("RMSE")) {
            mvLoadStatInsertData.setLineType("CNT");
          } else if (listToken[6].equals("BSS")) {
            mvLoadStatInsertData.setLineType("PSTD");
          } else if (listToken[6].equals("HIST")) {
            mvLoadStatInsertData.setLineType("RHIST");
          } else if (listToken[6].equals("RELP")) {
            mvLoadStatInsertData.setLineType("RELP");
          } else if (listToken[6].equals("SL1L2")) {
            mvLoadStatInsertData.setLineType("SL1L2");
          } else if (listToken[6].equals("GRAD")) {
            mvLoadStatInsertData.setLineType("GRAD");
          } else if (listToken[6].equals("SAL1L2")) {
            mvLoadStatInsertData.setLineType("SAL1L2");
          } else if (listToken[6].equals("VL1L2")) {
            mvLoadStatInsertData.setLineType("VL1L2");
          } else if (listToken[6].equals("VAL1L2")) {
            mvLoadStatInsertData.setLineType("VAL1L2");
          } else if (listToken[6].equals("RPS")) {
            mvLoadStatInsertData.setLineType("ENSCNT");
          } else if (listToken[6].equals("ECON")) {
            mvLoadStatInsertData.setLineType("ECLV");
          } else if (listToken[6].equals("RELI")) {
            mvLoadStatInsertData.setLineType("PCT");
            int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
            thresh = "==1/" + String.valueOf(intGroupSize);
          } else if (listToken[6].startsWith("FHO")) {
            mvLoadStatInsertData.setLineType("CTC");
            String[] threshArr = listToken[6].split("FHO");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else if (listToken[6].startsWith("FSS")) {
            mvLoadStatInsertData.setLineType("NBRCNT");
            String[] threshArr = listToken[6].split("FSS");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else {
            continue;
          }
          if (info.lineTypeLoad
                  && !info.tableLineTypeLoad.containsKey(mvLoadStatInsertData.getLineType())) {
            continue;
          }

          mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);

          //  parse the valid times


          LocalDateTime fcstValidBeg = LocalDateTime.parse(listToken[3], formatStatVsdb);

          //  format the valid times for the database insert
          String fcstValidBegStr = DATE_FORMAT_1.format(fcstValidBeg);

          //  calculate the number of seconds corresponding to fcst_lead
          String strFcstLead = listToken[2];
          int intFcstLeadSec = Integer.parseInt(strFcstLead) * 3600;

          //  determine the init time by combining fcst_valid_beg and fcst_lead

          LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
          fcstInitBeg = fcstInitBeg.minusSeconds(intFcstLeadSec);
          String fcstInitBegStr = DATE_FORMAT_1.format(fcstInitBeg);
          String obsValidBegStr = DATE_FORMAT_1.format(fcstValidBeg);
          String fcstValidEndStr = DATE_FORMAT_1.format(fcstValidBeg);
          String obsValidEndStr = DATE_FORMAT_1.format(fcstValidBeg);


          //  ensure that the interp_pnts field value is a reasonable integer
          String interpPnts = "0";

          String strLineType = mvLoadStatInsertData.getLineType();



          /*
           * * * *  stat_header insert  * * * *
           */
          headerRecords++;

          //  build the stat_header value list for this line
          String[] statHeaderValue = {
              listToken[0],    //  version
              modelName,      //  model
              "NA",           //  descr
              listToken[7],    //  fcst_var
              listToken[8],    //  fcst_lev
              listToken[7],    //  obs_var
              listToken[8],    //  obs_lev
              listToken[4],    //  obtype
              listToken[5],    //  vx_mask
              "NA",    //  interp_mthd
              interpPnts,    //  interp_pnts
              thresh,    //  fcst_thresh
              thresh    //  obs_thresh
          };

          //  build a where clause for searching for duplicate stat_header records
          String statHeaderWhere = BINARY +
                                       "  model = '" + modelName + "'\n" +
                                       "  AND " + BINARY + "descr = '" + "NA" + "'\n" +
                                       "  AND " + BINARY + "fcst_var = '" + listToken[7] + "'\n" +
                                       "  AND " + BINARY + "fcst_lev = '" + listToken[8] + "'\n" +
                                       "  AND " + BINARY + "obtype = '" + listToken[4] + "'\n" +
                                       "  AND " + BINARY + "vx_mask = '" + listToken[5] + "'\n" +
                                       "  AND " + BINARY + "interp_mthd = '" + "NA" + "'\n" +
                                       "  AND interp_pnts = " + interpPnts + "\n" +
                                       "  AND " + BINARY + "fcst_thresh = '" + thresh + "'\n" +
                                       "  AND " + BINARY + "obs_thresh = '" + thresh + "'";

          //  build the value list for the stat_header insert
          String statHeaderValueList = "";
          for (int i = 0; i < statHeaderValue.length; i++) {
            statHeaderValueList += (0 < i ? ", " : "") + "'" + statHeaderValue[i] + "'";
          }


          String strFileLine = strFilename + ":" + intLine;

          //  look for the header key in the table
          Integer statHeaderId = -1;
          if (statHeaders.containsKey(statHeaderValueList)) {
            statHeaderId = statHeaders.get(statHeaderValueList);
          }

          //  if the stat_header does not yet exist, create one
          else {

            //  look for an existing stat_header record with the same information
            boolean foundStatHeader = false;
            long intStatHeaderSearchBegin = System.currentTimeMillis();
            if (info.statHeaderDBCheck) {
              String statHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n"
                                            + statHeaderWhere;
              Connection con = null;
              Statement stmt = null;
              ResultSet res = null;
              try {
                con = getConnection();
                stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                           java.sql.ResultSet.CONCUR_READ_ONLY);
                res = stmt.executeQuery(statHeaderSelect);
                if (res.next()) {
                  String statHeaderIdDup = res.getString(1);
                  statHeaderId = Integer.parseInt(statHeaderIdDup);
                  foundStatHeader = true;
                }
              } catch (Exception e) {
                logger.error(e.getMessage());
              } finally {
                try {
                  res.close();
                } catch (Exception e) { /* ignored */ }
                try {
                  stmt.close();
                } catch (Exception e) { /* ignored */ }
                try {
                  con.close();
                } catch (Exception e) { /* ignored */ }
              }
            }
            timeStats.put("headerSearchTime",
                          timeStats.get("headerSearchTime")
                              + System.currentTimeMillis() - intStatHeaderSearchBegin);


            //  if the stat_header was not found, add it to the table
            if (!foundStatHeader) {

              statHeaderId = intStatHeaderIdNext++;
              statHeaders.put(statHeaderValueList, statHeaderId);

              //  build an insert statement for the mode header
              statHeaderValueList = Integer.toString(
                  statHeaderId) + ", " +        //  stat_header_id
                                        statHeaderValueList;

              //  insert the record into the stat_header database table
              String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + statHeaderValueList + ");";

              int intStatHeaderInsert;
              try {
                intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
                if (1 != intStatHeaderInsert) {
                  logger.warn(
                      "  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
                  statHeaderId = null;
                } else {
                  headerInserts++;
                }
              } catch (Exception e) {
                logger.error(e.getMessage());
                statHeaderId = null;
              }

            } else {
              statHeaders.put(statHeaderValueList, statHeaderId);
            }
          }
          if (statHeaderId != null) {

            String lineDataIdStr = "";
            dataRecords++;

            //  if the line type is of variable length, get the line_data_id
            boolean hasVarLengthGroups = MVUtil.lengthGroupIndices
                                             .containsKey(mvLoadStatInsertData.getLineType());

            //  determine the maximum token index for the data
            if (hasVarLengthGroups) {
              int lineDataId = tableVarLengthLineDataId.get(strLineType);
              lineDataIdStr = Integer.toString(lineDataId) + ", ";
              tableVarLengthLineDataId.put(strLineType, lineDataId + 1);
            }

            //  build the value list for the insert statment
            String lineDataValueList =
                lineDataIdStr +            //  line_data_id (if present)
                    statHeaderId + ", " +      //  stat_header_id
                    info.fileId + ", " +      //  data_file_id
                    intLine + ", " +          //  line_num
                    strFcstLead + ", " +        //  fcst_lead
                    "'" + fcstValidBegStr + "', " +    //  fcst_valid_beg
                    "'" + fcstValidEndStr + "', " +    //  fcst_valid_end
                    "'" + fcstInitBegStr + "', " +    //  fcst_init_beg
                    "000000" + ", " +        //  obs_lead
                    "'" + obsValidBegStr + "', " +    //  obs_valid_beg
                    "'" + obsValidEndStr + "'";      //  obs_valid_end

            //  if the line data requires a cov_thresh value, add it
            String strCovThresh = "NA";
            if (MVUtil.covThreshLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
              lineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
            }

            //  if the line data requires an alpha value, add it
            String alpha = "-9999";
            if (MVUtil.alphaLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
              if (alpha.equals("NA")) {
                logger.warn("  **  WARNING: alpha value NA with line type '" + mvLoadStatInsertData
                                                                                   .getLineType() + "'\n        " + mvLoadStatInsertData
                                                                                                                        .getFileLine());
              }
              lineDataValueList += ", " + replaceInvalidValues(alpha);
            }

            if (listToken[6].equals("RMSE")) {//CNT line type
              for (int i = 0; i < 94; i++) {
                if (i == 53) {
                  lineDataValueList += ", '" + listToken[10] + "'";
                } else if (i == 31) {
                  lineDataValueList += ", '" + listToken[11] + "'";
                } else if (i == 36) {
                  lineDataValueList += ", '" + listToken[9] + "'";
                } else if (i == 44) {
                  lineDataValueList += ", '" + listToken[12] + "'";
                } else if (i == 0 || i == 28 || i == 29 || i == 30) {//total,ranks, frank_ties, orank_ties
                  lineDataValueList += ", '0'";
                } else if (i == 77) {
                  lineDataValueList += ", '" + listToken[13] + "'";
                } else {
                  lineDataValueList += ", '-9999'";
                }
              }
            }


            if (listToken[6].equals("BSS")) {//PSTD line type
              for (int i = 0; i < 17; i++) {
                switch (i) {
                  case 0:
                  case 1:
                    lineDataValueList += ", '0'";
                    break;
                  case 2:
                  case 3:
                  case 8:
                  case 10:
                  case 11:
                  case 4:
                  case 13:
                  case 14:
                  case 16:
                    lineDataValueList += ", '-9999'";
                    break;
                  case 5:
                    lineDataValueList += ", '" + listToken[12] + "'";
                    break;
                  case 6:
                    lineDataValueList += ", '" + listToken[13] + "'";
                    break;
                  case 7:
                    lineDataValueList += ", '" + listToken[14] + "'";
                    break;
                  case 9:
                    lineDataValueList += ", '" + listToken[9] + "'";
                    break;
                  case 12:
                    lineDataValueList += ", '" + listToken[10] + "'";
                    break;
                  case 15:
                    lineDataValueList += ", '" + listToken[11] + "'";
                    break;
                  default:
                }

              }
            }

            if (listToken[6].equals("RPS")) {//ENSCNT line type
              for (int i = 0; i < 30; i++) {
                switch (i) {
                  case 0:
                    lineDataValueList += ", '" + listToken[9] + "'";
                    break;
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                    lineDataValueList += ", '-9999'";
                    break;
                  case 5:
                    lineDataValueList += ", '" + listToken[10] + "'";
                    break;
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                    lineDataValueList += ", '-9999'";
                    break;
                  case 10:
                    lineDataValueList += ", '" + listToken[11] + "'";
                    break;
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                    lineDataValueList += ", -9999";
                    break;
                  case 15:
                    lineDataValueList += ", '" + listToken[12] + "'";
                    break;
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                    lineDataValueList += ", -9999";
                    break;
                  case 20:
                    lineDataValueList += ", '" + listToken[13] + "'";
                    break;
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                    lineDataValueList += ", -9999";
                    break;
                  case 25:
                    lineDataValueList += ", '" + listToken[14] + "'";
                    break;
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                    lineDataValueList += ", -9999";
                    break;
                  default:

                }
              }

            }

            if (listToken[6].equals("HIST")) { //RHIST line type
              int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              lineDataValueList += ", 0," + intGroupSize;

            }

            if (listToken[6].equals("RELP")) {  // RELP line type
              lineDataValueList += ", 0";
              int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]);
              lineDataValueList += ", '" + intGroupSize + "'";
            }
            if (listToken[6].equals("ECON")) {  // ECLV line type
              lineDataValueList += ", 0, -9999, -9999";
              int intGroupSize = 18;
              lineDataValueList += ", '" + intGroupSize + "'";
            }


            if (listToken[6].equals("RELI")) { //PCT line type
              int total = 0;
              int intGroupSize;
              int intGroupIndex = 9;
              try {
                intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              } catch (Exception e) {
                intGroupSize = 0;
              }
              for (int i = 0; i < intGroupSize; i++) {
                Integer on;
                try {
                  on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue();
                  total = total + on;
                } catch (Exception e) {
                  logger.error(e.getMessage());
                }
                intGroupIndex++;
              }


              lineDataValueList += ", " + total + ", " + intGroupSize;
            }

            if (listToken[6].equals("SL1L2")
                    || listToken[6].equals("SAL1L2")) {//SL1L2,SAL1L2 line types
              for (int i = 0; i < 7; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList += ", '"
                                             + (Double.valueOf(listToken[i + 9]))
                                                   .intValue() + "'";
                  } else {
                    lineDataValueList += ", '" + Double.valueOf(listToken[i + 9]) + "'";
                  }

                } else {
                  lineDataValueList += ", '-9999'";
                }
              }
            }
            if (listToken[6].equals("VAL1L2")
                    || listToken[6].equals("GRAD")) {//VAL1L2,GRAD line type
              for (int i = 0; i < 8; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList += ", '"
                                             + (Double.valueOf(listToken[i + 9])).intValue()
                                             + "'";
                  } else {
                    lineDataValueList += ", '" + Double.valueOf(listToken[i + 9]) + "'";
                  }
                } else {
                  lineDataValueList += ", '-9999'";
                }

              }
            }
            if (listToken[6].equals("VL1L2")) {//VL1L2
              for (int i = 0; i < 10; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList += ", '"
                                             + (Double.valueOf(listToken[i + 9])).intValue()
                                             + "'";
                  } else {
                    lineDataValueList += ", '" + Double.valueOf(listToken[i + 9]) + "'";
                  }
                } else {
                  lineDataValueList += ", '-9999'";
                }

              }
            }
            if (listToken[6].startsWith("FHO")) {//CTC line type

              double total = Double.parseDouble(listToken[9]);
              double f_rate = Double.parseDouble(listToken[10]);
              double h_rate = Double.parseDouble(listToken[11]);
              double o_rate;
              if (listToken.length > 12) {
                o_rate = Double.valueOf(listToken[12]);
              } else {
                o_rate = 0;
                logger.error("o_rate os 0");
              }

              double fy = total * f_rate;
              double fy_oy = total * h_rate;
              double oy = total * o_rate;
              double fy_on = fy - fy_oy;
              double fn_oy = oy - fy_oy;
              double fn_on = total - fy - oy + fy_oy;


              for (int i = 0; i < 5; i++) {
                if (i == 4) {
                  lineDataValueList += ", '" + Math.max(0, fn_on) + "'";
                } else if (i == 3) {
                  lineDataValueList += ", '" + Math.max(0, fn_oy) + "'";
                } else if (i == 2) {
                  lineDataValueList += ", '" + Math.max(0, fy_on) + "'";
                } else if (i == 1) {
                  lineDataValueList += ", '" + Math.max(0, fy_oy) + "'";
                } else if (i == 0) {//total,
                  lineDataValueList += ", '" + listToken[9] + "'";
                }

              }
            }
            if (listToken[6].startsWith("FSS")) {//NBRCNT line type
              double fss = -9999;
              if (listToken.length > 11) {
                fss = 1 - Double.valueOf(listToken[10])
                              / (Double.valueOf(listToken[11]) + Double.valueOf(listToken[12]));
              }
              for (int i = 0; i < 19; i++) {
                if (i == 0) {//total,
                  lineDataValueList += ", " + listToken[9];
                } else if (i == 1) {//fbs
                  lineDataValueList += ", " + listToken[10];
                } else if (i == 4) {//fss
                  lineDataValueList += ", " + fss;
                } else {
                  lineDataValueList += ", '-9999'";
                }
              }
            }


            //  add the values list to the line type values map
            List<String> listLineTypeValues = new ArrayList<>();
            if (mvLoadStatInsertData.getTableLineDataValues()
                    .containsKey(mvLoadStatInsertData.getLineType())) {
              listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues()
                                       .get(mvLoadStatInsertData.getLineType());
            }
            listLineTypeValues.add("(" + lineDataValueList + ")");
            mvLoadStatInsertData.getTableLineDataValues()
                .put(mvLoadStatInsertData.getLineType(), listLineTypeValues);
            dataInserts++;


            /*
             * * * *  var_length insert  * * * *
             */

            if (hasVarLengthGroups) {
              //  get the index information about the current line type
              int intGroupIndex = 0;
              int intGroupSize = 0;
              int intNumGroups = 0;

              if (listToken[6].equals("HIST")) {//RHIST line type
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                } catch (Exception e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("RELP")) {//RELP line type)
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]);
                } catch (Exception e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("ECON")) {//ECLV line type)
                intGroupIndex = 9;
                intNumGroups = 18;
                intGroupSize = 1;
              } else if (listToken[6].equals("RELI")) {//PCT line type)
                intGroupIndex = 9;
                try {
                  intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                } catch (Exception e) {
                  intGroupSize = 0;
                }
                intNumGroups = 2;
              }

              List<String> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues()
                                                  .get(mvLoadStatInsertData.getLineType());
              if (null == listThreshValues) {
                listThreshValues = new ArrayList<>();
              }

              //  build a insert value statement for each threshold group
              if (listToken[6].equals("HIST")) {
                for (int i = 0; i < intNumGroups; i++) {
                  StringBuilder strThreshValues = new StringBuilder("(");
                  strThreshValues.append(lineDataIdStr).append(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      strThreshValues.append(", ").append(res * 100);
                    }

                  }
                  strThreshValues.append(')');
                  listThreshValues.add(strThreshValues.toString());
                  lengthRecords++;
                }
              } else if (listToken[6].equals("RELI")) {
                int total = 0;
                for (int i = 0; i < intGroupSize; i++) {
                  double thresh_i;
                  if (intGroupSize > 1) {
                    thresh_i = (double) i / (double) (intGroupSize - 1);
                  } else {
                    thresh_i = 0;
                  }
                  String strThreshValues = "(" + lineDataIdStr + (i + 1) + "," + thresh_i;
                  int oy;
                  int on;
                  try {
                    oy = Double.valueOf(listToken[intGroupIndex]).intValue();
                    on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue() - oy;
                    strThreshValues += ", " + oy + ", " + on;
                    total = total + oy + on;
                  } catch (Exception e) {
                    strThreshValues += ", -9999,  -9999";
                  }

                  intGroupIndex++;
                  strThreshValues += ")";
                  listThreshValues.add(strThreshValues);
                  lengthRecords++;
                }
              } else if (listToken[6].equals("RELP")) {
                for (int i = 0; i < intNumGroups; i++) {
                  StringBuilder strThreshValues = new StringBuilder("(");
                  strThreshValues.append(lineDataIdStr).append(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      strThreshValues.append(", ").append(res);
                    }

                  }
                  strThreshValues.append(')');
                  listThreshValues.add(strThreshValues.toString());
                  lengthRecords++;
                }
              } else if (listToken[6].equals("ECON")) {

                for (int i = 0; i < intNumGroups; i++) {
                  StringBuilder strThreshValues = new StringBuilder("(");
                  strThreshValues.append(lineDataIdStr).append(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      strThreshValues.append(", ").append(X_POINTS_FOR_ECON[i]).append(",")
                          .append(res);
                    }

                  }
                  strThreshValues.append(')');
                  listThreshValues.add(strThreshValues.toString());
                  lengthRecords++;
                }
              }

              mvLoadStatInsertData.getTableVarLengthValues()
                  .put(mvLoadStatInsertData.getLineType(), listThreshValues);
            }

            //  if the insert threshhold has been reached, commit the stored data to the database
            if (info.insertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
              int[] listInserts = commitStatData(mvLoadStatInsertData);
              dataInserts += listInserts[INDEX_LINE_DATA];
              lengthInserts += listInserts[INDEX_VAR_LENGTH];
            }
          }
        } catch (Exception e) {
          logger.error("ERROR: line:" + line + " has errors and would be ignored.");
          logger.error(e.getMessage());
        }

      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(mvLoadStatInsertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];

    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);

    //  print a performance report
    long intStatHeaderLoadTime = System.currentTimeMillis() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (info.verbose) {
      logger.info(MVUtil.padBegin("file lines: ", 6) + (intLine - 1) + "\n" +
                      MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
                      MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
                      MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
                      MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
                      MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
                      MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
                      MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intStatHeaderLoadTime) + "\n" +
                      MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
          headerSearchTime) + "\n" +
                      MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(
          dblLinesPerMSec) + "\n\n");
    }
    logger.info("intLine " + intLine);
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws Exception
   */
  @Override
  public Map<String, Long> loadModeFile(DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();

    //  data structure for storing mode object ids
    Map<String, Integer> tableModeObjectId = new HashMap<>();

    //  performance counters
    long intModeHeaderLoadStart = System.currentTimeMillis();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long ctsInserts = 0;
    long objSingleInserts = 0;
    long objPairInserts = 0;

    //  get the next mode record ids from the database
    int intModeHeaderIdNext = getNextId("mode_header", "mode_header_id");
    int intModeObjIdNext = getNextId("mode_obj_single", "mode_obj_id");

    //  set up the input file for reading
    String strFilename = info.path + "/" + info.filename;
    int intLine = 1;
    List<String> headerNames = new ArrayList<>();
    try (
        FileReader fileReader = new FileReader(strFilename);
        BufferedReader reader = new BufferedReader(fileReader)) {
      //  read each line of the input file
      while (reader.ready()) {
        String[] listToken = reader.readLine().split("\\s+");

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          intLine++;
          continue;
        }

        String strFileLine = strFilename + ":" + intLine;

        //  determine the line type
        int lineTypeLuId;
        int dataFileLuId = info.luId;
        String objectId = MVUtil.findValue(listToken, headerNames, "OBJECT_ID");
        Matcher matModeSingle = MVUtil.patModeSingleObjectId.matcher(objectId);
        Matcher matModePair = MVUtil.patModePairObjectId.matcher(objectId);
        int modeCts = 19;
        int modeSingle = 17;
        int modePair = 18;
        if (2 == dataFileLuId) {
          lineTypeLuId = modeCts;
        } else if (matModeSingle.matches()) {
          lineTypeLuId = modeSingle;
        } else if (matModePair.matches()) {
          lineTypeLuId = modePair;
        } else {
          throw new Exception("METviewer load error: loadModeFile() unable to determine line type "
                                  + MVUtil.findValue(listToken, headerNames, "OBJECT_ID")
                                  + "\n        " + strFileLine);
        }


        /*
         * * * *  mode_header insert  * * * *
         */

        //  parse the valid times

        LocalDateTime fcstValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "FCST_VALID"),
            DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "OBS_VALID"),
            DB_DATE_STAT_FORMAT);

        //  format the valid times for the database insert
        String fcstValidBegStr = DATE_FORMAT_1.format(fcstValidBeg);
        String obsValidBegStr = DATE_FORMAT_1.format(obsValidBeg);


        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead

        LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
        fcstInitBeg = fcstInitBeg.minusSeconds(intFcstLeadSec);

        String fcstInitStr = DATE_FORMAT_1.format(fcstInitBeg);


        //  build a value list from the header information
        //replace "NA" for fcst_accum (listToken[4]) and obs_accum (listToken[7]) to NULL

        String modeHeaderValueList =
            "'" + MVUtil.findValue(listToken, headerNames, "VERSION")
                + "', " + "'" + MVUtil.findValue(listToken, headerNames, "MODEL") + "', "
                + "";

        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  N_VALID
        } else {
          modeHeaderValueList = modeHeaderValueList
                                    + MVUtil.findValue(listToken, headerNames, "N_VALID")
                                    + ", ";      //  N_VALID
        }
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  GRID_RES
        } else {
          modeHeaderValueList = modeHeaderValueList
                                    + MVUtil.findValue(listToken, headerNames, "GRID_RES")
                                    + ", ";      //  GRID_RES
        }

        modeHeaderValueList = modeHeaderValueList
                                  + "'" + MVUtil.findValue(listToken, headerNames, "DESC")
                                  + "', " +      //  GRID_RES
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "FCST_LEAD")
                                  + "', " +      //  fcst_lead
                                  "'" + fcstValidBegStr + "', ";      //  fcst_valid
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "FCST_ACCUM"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  fcst_accum
        } else {
          modeHeaderValueList = modeHeaderValueList
                                    + "'"
                                    + MVUtil
                                          .findValue(listToken, headerNames, "FCST_ACCUM")
                                    + "', ";      //  fcst_accum
        }
        modeHeaderValueList = modeHeaderValueList + "'" + fcstInitStr + "', "
                                  +  // fcst_init
                                  "'"
                                  + MVUtil.findValue(listToken, headerNames, "OBS_LEAD")
                                  + "', " +      //  obs_lead
                                  "'" + obsValidBegStr + "', ";      //  obs_valid
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "OBS_ACCUM"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  obs_accum
        } else {
          modeHeaderValueList = modeHeaderValueList
                                    + "'"
                                    + MVUtil
                                          .findValue(listToken, headerNames, "OBS_ACCUM")
                                    + "', ";      //  obs_accum
        }
        modeHeaderValueList = modeHeaderValueList
                                  + "'"
                                  + MVUtil.findValue(listToken, headerNames, "FCST_RAD")
                                  + "', " +      //  fcst_rad
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "FCST_THR") + "', " +      //  fcst_thr
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "OBS_RAD") + "', " +      //  obs_rad
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "OBS_THR") + "', " +      //  obs_thr
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "FCST_VAR") + "', " +      //  fcst_var
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "FCST_LEV") + "', " +      //  fcst_lev
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "OBS_VAR") + "', " +      //  obs_var
                                  "'" + MVUtil.findValue(listToken, headerNames,
                                                         "OBS_LEV") + "'";        //  obs_lev

        String headerWhere = BINARY +
                                 "  version = '" + MVUtil.findValue(listToken, headerNames,
                                                                    "VERSION") + "'\n" +
                                 "  AND model = '" + MVUtil
                                                         .findValue(listToken, headerNames,
                                                                    "MODEL") + "'\n";
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
          headerWhere = headerWhere + "  AND n_valid is NULL ";
        } else {
          headerWhere = headerWhere
                            + "  AND n_valid = "
                            + MVUtil.findValue(listToken, headerNames, "N_VALID")
                            + "\n";
        }
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
          headerWhere = headerWhere + "  AND grid_res is NULL ";
          //  GRID_RES
        } else {
          headerWhere = headerWhere
                            + "  AND grid_res = "
                            + MVUtil.findValue(listToken, headerNames, "GRID_RES")
                            + "\n";
        }
        headerWhere = headerWhere
                          + "  AND " + BINARY + "descr = '"
                          + MVUtil.findValue(listToken, headerNames, "DESC")
                          + "'\n"
                          + "  AND fcst_lead = "
                          + Integer.valueOf(MVUtil.findValue(listToken, headerNames, "FCST_LEAD"))
                          + "\n" +
                          "  AND fcst_valid = '" + fcstValidBegStr + "'\n" +
                          "  AND fcst_accum = ";

        Integer accum = null;
        try {
          accum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "FCST_ACCUM"));
        } catch (Exception e) {
        }
        if (accum == null) {
          headerWhere = headerWhere + "NULL";
        } else {
          headerWhere = headerWhere + accum;
        }

        headerWhere = headerWhere + "\n"
                          + "  AND fcst_init = '" + fcstInitStr + "'\n"
                          + "  AND obs_lead = "
                          + Integer.valueOf(MVUtil.findValue(listToken, headerNames, "OBS_LEAD"))
                          + "\n"
                          + "  AND obs_valid = '" + obsValidBegStr + "'\n"
                          + "  AND obs_accum = ";

        try {
          accum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "OBS_ACCUM"));
        } catch (Exception e) {
        }
        if (accum == null) {
          headerWhere = headerWhere + "NULL";
        } else {
          headerWhere = headerWhere + accum;
        }
        headerWhere = headerWhere
                          + "\n"
                          + "  AND fcst_rad = "
                          + MVUtil.findValue(listToken, headerNames, "FCST_RAD") + "\n"
                          + "  AND " + BINARY + "fcst_thr = '"
                          + MVUtil.findValue(listToken, headerNames,
                                             "FCST_THR") + "'\n"
                          + "  AND obs_rad = "
                          + MVUtil.findValue(listToken, headerNames, "OBS_RAD") + "\n"
                          + "  AND " + BINARY + "obs_thr = '"
                          + MVUtil.findValue(listToken, headerNames, "OBS_THR")
                          + "'\n"
                          + "  AND " + BINARY + "fcst_var = '"
                          + MVUtil.findValue(listToken, headerNames, "FCST_VAR")
                          + "'\n"
                          + "  AND " + BINARY + "fcst_lev = '"
                          + MVUtil.findValue(listToken, headerNames, "FCST_LEV")
                          + "'\n"
                          + "  AND " + BINARY + "obs_var = '"
                          + MVUtil.findValue(listToken, headerNames, "OBS_VAR")
                          + "'\n"
                          + "  AND " + BINARY + "obs_lev = '"
                          + MVUtil.findValue(listToken, headerNames,
                                             "OBS_LEV") + "'";

        //  look for the header key in the table
        int modeHeaderId = -1;
        if (modeHeaders.containsKey(modeHeaderValueList)) {
          modeHeaderId = modeHeaders.get(modeHeaderValueList);
        }

        //  if the mode_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean foundModeHeader = false;
          long modeHeaderSearchBegin = System.currentTimeMillis();
          if (info.modeHeaderDBCheck) {
            String modeHeaderSelect = "SELECT\n  mode_header_id\nFROM\n  mode_header\nWHERE\n"
                                          + headerWhere;
            try (Connection con = getConnection();
                 Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                      java.sql.ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(modeHeaderSelect)) {
              if (res.next()) {
                String modeHeaderIdDup = res.getString(1);
                modeHeaderId = Integer.parseInt(modeHeaderIdDup);
                foundModeHeader = true;
                logger.warn(
                    "  **  WARNING: found duplicate mode_header record with id "

                        + modeHeaderIdDup + "\n        " + strFileLine);
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
            }

          }
          timeStats.put("headerSearchTime",
                        timeStats.get("headerSearchTime")
                            + System.currentTimeMillis() - modeHeaderSearchBegin);


          //  if the mode_header was not found, add it to the table
          if (!foundModeHeader) {

            modeHeaderId = intModeHeaderIdNext++;
            modeHeaders.put(modeHeaderValueList, modeHeaderId);

            //  build an insert statement for the mode header
            modeHeaderValueList =
                modeHeaderId + ", " +        //  mode_header_id
                    lineTypeLuId + ", " +        //  line_type_lu_id
                    info.fileId + ", " +        //  data_file_id
                    intLine + ", " +            //  linenumber
                    modeHeaderValueList;

            //  insert the record into the mode_header database table
            String modeHeaderInsert = "INSERT INTO mode_header VALUES ("
                                          + modeHeaderValueList + ");";
            int modeHeaderInsertCount = executeUpdate(modeHeaderInsert);
            if (1 != modeHeaderInsertCount) {
              logger.warn(
                  "  **  WARNING: unexpected result from mode_header INSERT: "
                      + modeHeaderInsertCount + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


        /*
         * * * *  mode_cts insert  * * * *
         */

        if (modeCts == lineTypeLuId) {

          //  build the value list for the mode_cts insert
          String ctsValueList = modeHeaderId + ", '"
                                    + MVUtil.findValue(listToken, headerNames, "FIELD")
                                    + "'";
          int totalIndex = headerNames.indexOf("TOTAL");
          for (int i = 0; i < 18; i++) {
            ctsValueList += ", " + replaceInvalidValues(listToken[totalIndex + i]);
          }

          //  insert the record into the mode_cts database table
          String modeCtsInsert = "INSERT INTO mode_cts VALUES (" + ctsValueList + ");";
          int modeCtsInsertCount = executeUpdate(modeCtsInsert);
          if (1 != modeCtsInsertCount) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_cts INSERT: "
                    + modeCtsInsertCount + "\n        " + strFileLine);
          }
          ctsInserts++;

        }

        /*
         * * * *  mode_obj_single insert  * * * *
         */

        else if (modeSingle == lineTypeLuId) {

          //  build the value list for the mode_cts insert
          int modeObjId = intModeObjIdNext++;
          String singleValueList = modeObjId + ", " + modeHeaderId + ", '"
                                       + objectId + "'";
          for (String header : modeObjSingleColumns) {
            singleValueList += ", '" + replaceInvalidValues(
                MVUtil.findValue(listToken, headerNames, header)) + "'";
          }

          //set flags
          int simpleFlag = 1;
          int fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          int matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          if (objCatArr.length == 1 && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }
          singleValueList = singleValueList + "," + fcstFlag + "," + simpleFlag + ","
                                + matchedFlag;

          //  insert the record into the mode_obj_single database table
          String strModeObjSingleInsert = "INSERT INTO mode_obj_single VALUES ("
                                              + singleValueList + ");";
          int intModeObjSingleInsert = executeUpdate(strModeObjSingleInsert);
          if (1 != intModeObjSingleInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_obj_single INSERT: "
                    + intModeObjSingleInsert + "\n        " + strFileLine);
          }
          objSingleInserts++;

          //  add the mode_obj_id to the table, using the object_id as the key
          tableModeObjectId.put(objectId, modeObjId);

        }

        /*
         * * * *  mode_obj_pair insert  * * * *
         */

        else if (modePair == lineTypeLuId) {

          //  determine the mode_obj_id values for the pair
          int modeObjectIdFcst = tableModeObjectId.get(matModePair.group(1));
          int modeObjectIdObs = tableModeObjectId.get(matModePair.group(2));

          //  build the value list for the mode_cts insert
          String pairValueList = modeObjectIdObs + ", " + modeObjectIdFcst
                                     + ", " + modeHeaderId + ", " +
                                     "'" + objectId + "', '"
                                     + MVUtil.findValue(listToken, headerNames,
                                                        "OBJECT_CAT")
                                     + "'";

          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "CENTROID_DIST"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "BOUNDARY_DIST"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "CONVEX_HULL_DIST"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "ANGLE_DIFF"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "ASPECT_DIFF"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "AREA_RATIO"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "INTERSECTION_AREA"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "UNION_AREA"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "SYMMETRIC_DIFF"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "INTERSECTION_OVER_AREA"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "CURVATURE_RATIO"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "COMPLEXITY_RATIO"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "PERCENTILE_INTENSITY_RATIO"));
          pairValueList += ", " + replaceInvalidValues(
              MVUtil.findValue(listToken, headerNames, "INTEREST"));


          //set flags
          int simpleFlag = 1;
          String[] objIdArr = objectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          int matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          if (objCatArr.length == 2 && objCatArr[0].substring(2).equals(objCatArr[1].substring(2))
                  && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }
          pairValueList = pairValueList + "," + simpleFlag + "," + matchedFlag;

          //  insert the record into the mode_obj_pair database table
          String strModeObjPairInsert = "INSERT INTO mode_obj_pair VALUES ("
                                            + pairValueList + ");";
          int intModeObjPairInsert = executeUpdate(strModeObjPairInsert);
          if (1 != intModeObjPairInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_obj_pair INSERT: "
                    + intModeObjPairInsert + "\n        " + strFileLine);
          }
          objPairInserts++;

        }

        intLine++;
      }
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  increment the global mode counters
    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("ctsInserts", ctsInserts);
    timeStats.put("objSingleInserts", objSingleInserts);
    timeStats.put("objPairInserts", objPairInserts);


    //  print a performance report
    if (info.verbose) {
      long intModeHeaderLoadTime = System.currentTimeMillis() - intModeHeaderLoadStart;
      logger.info(MVUtil.padBegin("mode_header inserts: ", 36) + headerInserts + "\n" +
                      MVUtil.padBegin("mode_cts inserts: ", 36) + ctsInserts + "\n" +
                      MVUtil.padBegin("mode_obj_single inserts: ", 36) + objSingleInserts + "\n" +
                      MVUtil.padBegin("mode_obj_pair inserts: ", 36) + objPairInserts + "\n" +
                      (info.modeHeaderDBCheck ? MVUtil.padBegin("mode_header search time: ",
                                                                36) + MVUtil.formatTimeSpan(
                          timeStats.get("headerSearchTime")) + "\n" : "") +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intModeHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws Exception
   */
  @Override
  public Map<String, Long> loadMtdFile(DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();

    //  performance counters
    long intMtdHeaderLoadStart = System.currentTimeMillis();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long obj3dSingleInserts = 0;
    long obj3dPairInserts = 0;
    long obj2dInserts = 0;

    //  get the next mode record ids from the database
    int intMtdHeaderIdNext = getNextId("mtd_header", "mtd_header_id");

    //  set up the input file for reading
    String filename = info.path + "/" + info.filename;
    int line = 1;
    List<String> headerNames = new ArrayList<>();
    try (
        FileReader fileReader = new FileReader(filename);
        BufferedReader reader = new BufferedReader(fileReader)) {
      //  read each line of the input file
      while (reader.ready()) {
        String lineStr = reader.readLine().trim();
        String[] listToken = lineStr.split("\\s+");

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          line++;
          continue;
        }

        String strFileLine = filename + ":" + line;

        //  determine the line type
        int lineTypeLuId;
        int dataFileLuId = info.luId;
        String objectId = MVUtil.findValue(listToken, headerNames, "OBJECT_ID");
        int mtd3dSingle = 17;
        int mtd3dPair = 18;
        int mtd2d = 19;
        if (11 == dataFileLuId || 12 == dataFileLuId) {
          lineTypeLuId = mtd3dSingle;
        } else if (9 == dataFileLuId || 10 == dataFileLuId) {
          lineTypeLuId = mtd3dPair;
        } else if (8 == dataFileLuId) {
          lineTypeLuId = mtd2d;
        } else {
          throw new Exception("METviewer load error: loadModeFile() unable to determine line type"
                                  + " " + strFileLine);
        }
        //  parse the valid times


        LocalDateTime fcstValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "FCST_VALID"),
            DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidBeg = LocalDateTime.parse(
            MVUtil.findValue(listToken, headerNames, "OBS_VALID"),
            DB_DATE_STAT_FORMAT);

        //  format the valid times for the database insert
        String fcstValidBegStr = DATE_FORMAT_1.format(fcstValidBeg);


        String obsValidBegStr = DATE_FORMAT_1.format(obsValidBeg);


        //  calculate the number of seconds corresponding to fcst_lead
        String fcstLead = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int fcstLeadLen = fcstLead.length();
        int fcstLeadSec = 0;
        try {
          fcstLeadSec = Integer.parseInt(
              fcstLead.substring(fcstLeadLen - 2, fcstLeadLen));
          fcstLeadSec += Integer.parseInt(
              fcstLead.substring(fcstLeadLen - 4, fcstLeadLen - 2)) * 60;
          fcstLeadSec += Integer.parseInt(fcstLead.substring(fcstLeadLen - 6,
                                                             fcstLeadLen - 4)) * 3600;
        } catch (Exception e) {
        }
        String fcstLeadInsert = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        if (fcstLeadInsert.equals("NA")) {
          fcstLeadInsert = "0";
        } else {
          if (fcstLeadInsert.contains("_")) {
            fcstLeadInsert = fcstLeadInsert.split("_")[1];
          }
        }

        String obsLeadInsert = MVUtil.findValue(listToken, headerNames, "OBS_LEAD");
        if (obsLeadInsert.equals("NA")) {
          obsLeadInsert = "0";
        } else {
          if (obsLeadInsert.contains("_")) {
            obsLeadInsert = obsLeadInsert.split("_")[1];
          }
        }

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
        fcstInitBeg = fcstInitBeg.minusSeconds(fcstLeadSec);

        String fcstInitStr = DATE_FORMAT_1.format(fcstInitBeg);


        String mtdHeaderValueList = "'" + MVUtil.findValue(listToken, headerNames, "VERSION")
                                        + "', " + "'"
                                        + MVUtil.findValue(listToken, headerNames, "MODEL")
                                        + "', " + "'"
                                        + MVUtil.findValue(listToken, headerNames, "DESC")
                                        + "', ";


        mtdHeaderValueList = mtdHeaderValueList
                                 + "'"
                                 + fcstLeadInsert
                                 + "', " + "'" + fcstValidBegStr + "', "
                                 + "'" + fcstInitStr + "', "
                                 + "'" + obsLeadInsert
                                 + "', " + "'" + obsValidBegStr + "', ";

        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "T_DELTA"))) {
          mtdHeaderValueList = mtdHeaderValueList + "NULL" + ", ";
        } else {
          mtdHeaderValueList = mtdHeaderValueList + "'"
                                   + MVUtil.findValue(listToken, headerNames, "T_DELTA")
                                   + "', ";
        }
        mtdHeaderValueList = mtdHeaderValueList
                                 + "'" + MVUtil.findValue(listToken, headerNames, "FCST_RAD")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "FCST_THR")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "OBS_RAD")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "OBS_THR")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "FCST_VAR")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "FCST_LEV")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "OBS_VAR")
                                 + "', " +
                                 "'" + MVUtil.findValue(listToken, headerNames, "OBS_LEV")
                                 + "'";

        String mtdHeaderWhereClause = BINARY +
                                          "  version = '" + MVUtil.findValue(listToken, headerNames,
                                                                             "VERSION")
                                          + "'\n"
                                          + "  AND " + BINARY + "model = '"
                                          + MVUtil
                                                .findValue(listToken, headerNames, "MODEL") + "'\n"
                                          + "  AND " + BINARY + "descr = '"
                                          + MVUtil.findValue(listToken, headerNames, "DESC") + "'\n"
                                          + "  AND fcst_lead = " + fcstLeadInsert + "\n"
                                          + "  AND fcst_valid = '" + fcstValidBegStr + "'\n"
                                          + "  AND t_delta = "
                                          + MVUtil
                                                .findValue(listToken, headerNames, "T_DELTA") + "\n"
                                          + "  AND fcst_init = '" + fcstInitStr + "'\n"
                                          + "  AND obs_lead = " + obsLeadInsert + "\n"
                                          + "  AND obs_valid = '" + obsValidBegStr + "'\n"
                                          + "  AND fcst_rad = "
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "FCST_RAD") + "\n"
                                          + "  AND " + BINARY + "fcst_thr = '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "FCST_THR") + "'\n"
                                          + "  AND obs_rad = "
                                          + MVUtil
                                                .findValue(listToken, headerNames, "OBS_RAD") + "\n"
                                          + "  AND " + BINARY + "obs_thr = '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "OBS_THR") + "'\n"
                                          + "  AND " + BINARY + "fcst_var = '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "FCST_VAR") + "'\n"
                                          + "  AND " + BINARY + "fcst_lev = '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "FCST_LEV") + "'\n"
                                          + "  AND " + BINARY + "obs_var = '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "OBS_VAR") + "'\n"
                                          + "  AND " + BINARY + "obs_lev = '"
                                          + MVUtil
                                                .findValue(listToken, headerNames, "OBS_LEV") + "'";

        //  look for the header key in the table
        int mtdHeaderId = -1;
        if (mtdHeaders.containsKey(mtdHeaderValueList)) {
          mtdHeaderId = mtdHeaders.get(mtdHeaderValueList);
        }

        //  if the mtd_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean foundMtdHeader = false;
          long mtdHeaderSearchBegin = System.currentTimeMillis();
          if (info.mtdHeaderDBCheck) {
            String strMtdHeaderSelect = "SELECT\n  mtd_header_id\nFROM\n  mtd_header\nWHERE\n" +
                                            mtdHeaderWhereClause;
            try (Connection con = getConnection();
                 Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                      java.sql.ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(strMtdHeaderSelect)) {
              if (res.next()) {
                String strMtdHeaderIdDup = res.getString(1);
                mtdHeaderId = Integer.parseInt(strMtdHeaderIdDup);
                foundMtdHeader = true;
                logger.warn(
                    "  **  WARNING: found duplicate mtd_header record with id " +
                        strMtdHeaderIdDup + "\n        " + strFileLine);
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
            }

          }
          timeStats.put("headerSearchTime",
                        timeStats.get("headerSearchTime")
                            + System.currentTimeMillis() - mtdHeaderSearchBegin);


          //  if the mtd_header was not found, add it to the table
          if (!foundMtdHeader) {

            mtdHeaderId = intMtdHeaderIdNext++;
            mtdHeaders.put(mtdHeaderValueList, mtdHeaderId);

            //  build an insert statement for the mtd header
            mtdHeaderValueList =
                mtdHeaderId + ", " +
                    lineTypeLuId + ", " +
                    info.fileId + ", " +
                    line + ", " +
                    mtdHeaderValueList;

            //  insert the record into the mtd_header database table
            String strMtdHeaderInsert = "INSERT INTO mtd_header VALUES (" + mtdHeaderValueList +
                                            ");";
            int intMtdHeaderInsert = executeUpdate(strMtdHeaderInsert);
            if (1 != intMtdHeaderInsert) {
              logger.warn(
                  "  **  WARNING: unexpected result from mtd_header INSERT: " + intMtdHeaderInsert
                      + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


        if (mtd3dSingle == lineTypeLuId) {
          String str3dSingleValueList = mtdHeaderId + ", '" + objectId + "'";
          for (String header : mtdObj3dSingleColumns) {
            str3dSingleValueList += ", '" + replaceInvalidValues(
                MVUtil.findValue(listToken, headerNames, header)) + "'";
          }

          //set flags
          Integer simpleFlag = 1;
          Integer fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          Integer matchedFlag = 0;
          String objCat = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT");
          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (Exception e) {
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }
          str3dSingleValueList = str3dSingleValueList + "," + fcstFlag + "," + simpleFlag + ","
                                     + matchedFlag;

          //  insert the record into the mtd_obj_single database table
          int mtd3dObjSingleInsert = executeUpdate("INSERT INTO mtd_3d_obj_single VALUES ("
                                                       + str3dSingleValueList + ");");
          if (1 != mtd3dObjSingleInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_3d_obj_single INSERT: "
                    + mtd3dObjSingleInsert + "\n        " + strFileLine);
          }
          obj3dSingleInserts++;
        } else if (mtd2d == lineTypeLuId) {
          String str2dValueList = mtdHeaderId + ", '" + objectId + "'";
          for (String header : mtdObj2dColumns) {
            str2dValueList += ", '" + replaceInvalidValues(
                MVUtil.findValue(listToken, headerNames, header)) + "'";
          }

          //set flags
          Integer simpleFlag = 1;
          Integer fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          Integer matchedFlag = 0;
          String objCat = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT");

          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (Exception e) {
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }
          str2dValueList = str2dValueList + "," + fcstFlag + "," + simpleFlag + "," + matchedFlag;

          //  insert the record into the mtd_obj_single database table
          int mtd2dObjInsert = executeUpdate("INSERT INTO mtd_2d_obj VALUES ("
                                                 + str2dValueList + ");");
          if (1 != mtd2dObjInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_2d_obj INSERT: "
                    + mtd2dObjInsert + "\n        " + strFileLine);
          }
          obj2dInserts++;
        } else if (mtd3dPair == lineTypeLuId) {

          //  build the value list for the mode_cts insert
          String str3dPairValueList = mtdHeaderId + ", "
                                          + "'"
                                          + objectId
                                          + "', '"
                                          + MVUtil.findValue(listToken, headerNames,
                                                             "OBJECT_CAT")
                                          + "'";
          int spaceCentroidDistIndex = headerNames.indexOf("SPACE_CENTROID_DIST");
          for (int i = 0; i < 11; i++) {
            str3dPairValueList += ", " + replaceInvalidValues(
                listToken[spaceCentroidDistIndex + i]);
          }

          //set flags
          Integer simpleFlag = 1;
          String[] objIdArr = objectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          Integer matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          Integer num1 = null;
          Integer num2 = null;
          try {
            num1 = Integer.valueOf(objCatArr[0].substring(objCatArr[0].length() - 3));
            num2 = Integer.valueOf(objCatArr[1].substring(objCatArr[1].length() - 3));
          } catch (Exception e) {
          }
          if (num1.equals(num2) && num1 != 0) {
            matchedFlag = 1;
          }
          str3dPairValueList = str3dPairValueList + "," + simpleFlag + "," + matchedFlag;

          int mtd3dObjPairInsert = executeUpdate("INSERT INTO mtd_3d_obj_pair VALUES ("
                                                     + str3dPairValueList + ");");
          if (1 != mtd3dObjPairInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_3d_obj_pair INSERT: " +
                    mtd3dObjPairInsert + "\n        " + strFileLine);
          }
          obj3dPairInserts++;

        }

        line++;
      }
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  increment the global mode counters
    timeStats.put("linesTotal", (long) (line - 1));
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("obj3dSingleInserts", obj3dSingleInserts);
    timeStats.put("obj3dPairInserts", obj3dPairInserts);
    timeStats.put("obj2dInserts", obj2dInserts);


    //  print a performance report
    if (info.verbose) {
      long intMtdHeaderLoadTime = System.currentTimeMillis() - intMtdHeaderLoadStart;
      logger.info(
          MVUtil.padBegin("mtd_header inserts: ", 36)
              + headerInserts + "\n"
              + MVUtil.padBegin("mtd_3d_obj_single inserts: ", 36)
              + obj3dSingleInserts + "\n"
              + MVUtil.padBegin("mtd_3d_obj_pair inserts: ", 36)
              + obj3dPairInserts + "\n"
              + MVUtil.padBegin("mtd_2d_obj inserts: ", 36) + obj2dInserts + "\n"
              + (info.mtdHeaderDBCheck ? MVUtil.padBegin("mtd_header search time: ", 36)
                                             + MVUtil.formatTimeSpan(
              timeStats.get("headerSearchTime"))
                                             + "\n" : "")
              + MVUtil.padBegin("total load time: ", 36)
              + MVUtil.formatTimeSpan(intMtdHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  @Override
  public void updateGroup(String group) throws Exception {
    String sql = "SELECT  category FROM metadata";
    String currentCategory = "";
    int nrows = 0;

    try (Connection con = getConnection();
         Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                              java.sql.ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery(sql)) {

      while (res.next()) {
        currentCategory = res.getString(1);
        nrows = nrows + 1;
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    if (!currentCategory.equals(group)) {
      if (nrows == 0) {
        sql = "INSERT INTO metadata VALUES ('" + group + "','')";
      } else {
        sql = "UPDATE metadata SET category = '" + group
                  + "' WHERE " + BINARY + "category = '" + currentCategory + "'";
      }
      executeUpdate(sql);
    }
  }

  @Override
  public void updateDescription(String description) throws Exception {
    String sql = "SELECT  description FROM metadata";
    String currentDescription = "";
    int nrows = 0;

    try (Connection con = getConnection();
         Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                              java.sql.ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery(sql)) {

      while (res.next()) {
        currentDescription = res.getString(1);
        nrows = nrows + 1;
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    if (!currentDescription.equals(description)) {
      if (nrows == 0) {
        sql = "INSERT INTO metadata VALUES (''," + description + "')";
      } else {
        sql = "UPDATE metadata SET description = '" + description
                  + "' WHERE " + BINARY + "description = '" + currentDescription + "'";
      }
      executeUpdate(sql);
    }
  }


  private int executeBatch(final List<String> listValues, final String table) throws Exception {

    String insertSql = "INSERT INTO " + table + " VALUES " + "(";
    int numberOfValues = listValues.get(0).split(",").length;
    for (int i = 0; i < numberOfValues; i++) {
      insertSql = insertSql + "?,";
    }
    insertSql = insertSql.substring(0, insertSql.length() - 1);
    insertSql = insertSql + ")";
    int totalInsert = 0;
    Connection con = null;
    Statement stmt = null;
    PreparedStatement ps = null;
    IntStream intStream = null;
    try {
      con = getConnection();
      stmt = con.createStatement();
      ps = con.prepareStatement(insertSql);
      for (int i = 0; i < listValues.size(); i++) {

        String[] valuesArr = listValues.get(i).split(",");
        valuesArr[0] = valuesArr[0].replace("(", "");
        valuesArr[valuesArr.length - 1] = valuesArr[valuesArr.length - 1].replace(")", "");
        for (int j = 0; j < valuesArr.length; j++) {
          ps.setObject(j + 1, valuesArr[j].trim().replaceAll("'", ""));
        }
        ps.addBatch();

        //execute and commit batch of 20000 queries
        if (i != 0 && i % 20000 == 0) {
          int[] updateCounts = ps.executeBatch();
          intStream = IntStream.of(updateCounts);
          totalInsert = totalInsert + intStream.sum();
          intStream.close();
          ps.clearBatch();
        }
      }

      int[] updateCounts = ps.executeBatch();
      intStream = IntStream.of(updateCounts);
      totalInsert = totalInsert + IntStream.of(updateCounts).sum();
      intStream.close();

    } catch (SQLException se) {
      throw new Exception("caught SQLException calling executeBatch: " + se.getMessage());
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (con != null) {
        con.close();
      }
      if (intStream != null) {
        intStream.close();
      }
    }
    return totalInsert;
  }

  /**
   * Analyze the input file object to determine what type of MET output file it is.  Create an entry
   * in the data_file table for the file and build a DataFileInfo data structure with information
   * about the file and return it.
   *
   * @param file points to a MET output file to process // * @param con database connection to use
   * @return data structure containing information about the input file
   */
  @Override
  public DataFileInfo processDataFile(File file, boolean forceDupFile) throws Exception {
    String strPath = file.getParent().replace("\\", "/");
    String strFile = file.getName();
    int dataFileLuId = -1;
    String dataFileLuTypeName;
    int dataFileId;

    //check file size and return if it  is 0
    if (file.length() == 0) {
      return null;
    }
    // set default values for the loaded time (now) and the modified time (that of input file)
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String loadDate = DATE_FORMAT.format(cal.getTime());
    cal.setTimeInMillis(file.lastModified());

    String modDate = DATE_FORMAT.format(cal.getTime());


    // determine the type of the input data file by parsing the filename
    if (strFile.matches("\\S+\\.stat$")) {
      dataFileLuTypeName = "stat";
    } else if (strFile.matches("\\S+_obj\\.txt$")) {
      dataFileLuTypeName = "mode_obj";
    } else if (strFile.matches("\\S+_cts\\.txt$")) {
      dataFileLuTypeName = "mode_cts";
    } else if (strFile.matches("\\S+\\.vsdb$")) {
      dataFileLuTypeName = "vsdb_point_stat";
    } else if (strFile.matches("\\S+2d.txt$")) {
      dataFileLuTypeName = "mtd_2d";
    } else if (strFile.matches("\\S+3d_pair_cluster.txt$")) {
      dataFileLuTypeName = "mtd_3d_pc";
    } else if (strFile.matches("\\S+3d_pair_simple.txt$")) {
      dataFileLuTypeName = "mtd_3d_ps";
    } else if (strFile.matches("\\S+3d_single_cluster.txt$")) {
      dataFileLuTypeName = "mtd_3d_sc";
    } else if (strFile.matches("\\S+3d_single_simple.txt$")) {
      dataFileLuTypeName = "mtd_3d_ss";
    } else {
      return null;
    }

    dataFileLuId = tableDataFileLU.get(dataFileLuTypeName);


    // build a query to look for the file and path in the data_file table
    String dataFileQuery =
        "SELECT " +
            "  dfl.type_name, " +
            "  df.data_file_id, " +
            "  df.load_date, " +
            "  df.mod_date " +
            "FROM " +
            "  data_file_lu dfl, " +
            "  data_file df " +
            "WHERE " +
            "  dfl.data_file_lu_id = df.data_file_lu_id " +
            "  AND " + BINARY + "df.filename = \'" + strFile + "\' " +
            "  AND " + BINARY + "df.path = \'" + strPath + "\'";


    try (
        Connection con = getConnection();
        Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                             java.sql.ResultSet.CONCUR_READ_ONLY);
        ResultSet res = stmt.executeQuery(dataFileQuery)) {

      // if the data file is already present in the database, print a warning and return the id
      if (res.next()) {
        dataFileLuTypeName = res.getString(1);
        dataFileId = res.getInt(2);
        loadDate = res.getString(3);
        modDate = res.getString(4);

        if (forceDupFile) {
          DataFileInfo info = new DataFileInfo(dataFileId,
                                               strFile
              , strPath, loadDate,
                                               modDate, dataFileLuId, dataFileLuTypeName);
          logger.warn("  **  WARNING: file already present in table data_file");
          return info;
        } else {
          throw new Exception(
              "file already present in table data_file, use force_dup_file setting to override");
        }
      }
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
    // if the file is not present in the data_file table, query for the largest data_file_id
    try (
        Connection con = getConnection();
        Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                             java.sql.ResultSet.CONCUR_READ_ONLY);
        ResultSet res = stmt.executeQuery("SELECT MAX(data_file_id) FROM data_file;")) {

      if (!res.next()) {
        throw new Exception("METviewer load error: processDataFile() unable to find max "
                                + "data_file_id");
      }
      dataFileId = res.getInt(1);
      if (res.wasNull()) {
        dataFileId = 0;
      }
      dataFileId = dataFileId + 1;

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }


    // add the input file to the data_file table
    String strDataFileInsert =
        "INSERT INTO data_file VALUES (" +
            dataFileId + ", " +      // data_file_id
            dataFileLuId + ", " +    // data_file_lu_id
            "'" + strFile + "', " +      // filename
            "'" + strPath + "', " +      // path
            "'" + loadDate + "', " +    // load_date
            "'" + modDate + "');";    // mod_date
    int intRes = executeUpdate(strDataFileInsert);
    if (1 != intRes) {
      logger.warn("  **  WARNING: unexpected result from data_file INSERT: " + intRes);
    }

    return new DataFileInfo(dataFileId, strFile, strPath,
                            loadDate,
                            modDate, dataFileLuId,
                            dataFileLuTypeName);
  }

  @Override
  public void updateInfoTable(String strXML, MVLoadJob job) throws Exception {
    //  get the instance_info information to insert
    int instInfoIdNext = getNextId("instance_info", "instance_info_id");
    String updater = "";
    try {
      updater = MVUtil.sysCmd();
    } catch (Exception e) {
      try {
        updater = MVUtil.sysCmd();
      } catch (Exception e2) {
      }
    }
    updater = updater.trim();
    String updateDate = DATE_FORMAT_1.format(LocalDateTime.now());
    String updateDetail = job.getLoadNote();

    //  read the load xml into a string, if requested
    String loadXmlStr = "";
    if (job.getLoadXML()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(strXML))) {
        while (reader.ready()) {
          loadXmlStr += reader.readLine().trim();
        }
      }
    }

    //  construct an update statement for instance_info
    String instInfoSQL =
        "INSERT INTO instance_info VALUES (" +
            "'" + instInfoIdNext + "', " +
            "'" + updater + "', " +
            "'" + updateDate + "', " +
            "'" + updateDetail + "', " +
            "'" + loadXmlStr + "'" +
            ");";

    //  execute the insert SQL
    logger.info("Inserting instance_info record...  ");
    int insert = executeUpdate(instInfoSQL);
    if (1 != insert) {
      throw new Exception("unexpected number of instance_info rows inserted: " + insert);
    }
    logger.info("Done\n");
  }


  private String replaceInvalidValues(String strData) {
    return strData.replace("NA", "-9999")
               .replace("-nan", "-9999")
               .replace("nan", "-9999");
  }

}