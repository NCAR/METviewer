/**
 * DatabaseException.java Copyright UCAR (c) 2019. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2019.
 */

package edu.ucar.metviewer;

/**
 * @author : tatiana $
 * @version : 1.0 : 2019-03-06 09:48 $
 */
public class ParsingException extends Exception {

  public ParsingException(String errorMessage) {
    super(errorMessage);
  }
}
