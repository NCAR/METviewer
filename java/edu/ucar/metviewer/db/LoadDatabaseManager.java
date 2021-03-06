/**
 * LoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.File;
import java.util.Map;

import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.DatabaseException;
import edu.ucar.metviewer.MVLoadJob;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/06/17 12:33 $
 */
public interface LoadDatabaseManager {

  void dropIndexes();

  void applyIndexes();

  void updateInfoTable(String strXML, MVLoadJob job) throws DatabaseException;

  DataFileInfo processDataFile(File file, boolean forceDupFile) throws DatabaseException;

  Map<String, Long> loadStatFile(DataFileInfo info) throws DatabaseException;

  Map<String, Long> loadModeFile(DataFileInfo info) throws DatabaseException;

  Map<String, Long> loadStatFileVSDB(DataFileInfo info) throws DatabaseException;

  Map<String, Long> loadMtdFile(DataFileInfo info) throws DatabaseException;

  void updateGroup(final String group) throws DatabaseException;

  void updateDescription(String description) throws DatabaseException;

}
