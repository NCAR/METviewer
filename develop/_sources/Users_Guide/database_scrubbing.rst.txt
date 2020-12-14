Database Scrubbing Module
=========================

The database scrubbing utility is used to to delete data from METviewer
databases that meets some user-specified selection criteria. The usage
statement:

.. code-block:: none
		
  ---- Database Scrubbing ----

  Usage: mv_prune.sh prune_db_spec_file

            where prune_db_spec_file specifies the XML pruning specification document

  ---- Database Scrubbing Done ----

The prune_db_spec_file passed to the pruning module contains information
about the criteria for deleting data. It is an XML file with the top-level
tag **<prune_spec>** which contains the elements described below, divided
into functional sections. The data to be deleted may be specified in one of
three ways: as a list of file names, as a list of directories, or as a set
of values for various fields in the data.
        
**<connection>:** Please reference the
`Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_
documentation.

**<info_only>:** **TRUE** or **FALSE**, this option indicates if the data
to be deleted should only be listed (**TRUE**) or the actual deletion
performed (**FALSE**).

| **<fields>:** A list of fields used for pruning.
|       **<value_range>:** An inclusive range for continuous variables, such as dates.
|                **<start>:** Beginning of the range.                
|                **<end>:** End of the range.
|
|       **OR**
|        
|       **<value_list>:** A list of values, such as models.         
|                **<value>:** A single value.
| 
| **OR**
|        
| **<files>:** A set of files to be removed.               
|       **<file>:** File name.         
|
| **OR**
|
| **<folders>:** A set of directories to be removed.
|       **<folder_tmpl>:** A template string describing the file structure of the MET files, which is populated with values specified in the **<load_val>** tag structure.
|       **<date_list>:** Please reference the `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_ documentation.
|
|       **<load_val>:** A tree structure containing values used to populate the **<folder_tmpl>** template.
|               **<field>:** A template value, its name is specified by the attribute name, and its values are specified by its children **<val>** tags.
|                        **<val>:** A single template value which will slot into the template in the value specified by the parent field's name.         
|                        **<date_list>:** Specifies a previously declared **<date_list>** element, using the name attribute, which represents a list of dates in a particular format.
|

Examples
--------

**Example 1: Prune by describing the data (<fields>)**

The following configuration will remove data from database 'mv_database'
that has model names NAM and GFS, forecast variable APCP_03 and forecast
valid dates between 2013-07-05 06:00 and 2013-07-05 18:00:00

.. code-block:: XML

        <prune_spec>
          <connection>
            <host>db_host:3306</host>
            <database>mv_database</database>
            <user>user_name<user>
            <password>user_password<password>
          </connection>
          
          <info_only>false</info_only>
                                  
          <fields>
            <field name="fcst_valid_beg">
              <value_range>
                <start>2013-07-05 06:00:00</start>
                <end>2013-07-05 18:00:00</end>
              </value_range>
            </field>
                                                                        
            <field name="model">
              <value_list>
                <value>NAM</value>
                <value>GFS</value>
              </value_list>
            </field>
          
            <field name="fcst_var">
              <value_list>
                <value>APCP_03</value>
              </value_list>
            </field>
          </fields>
        </prune_spec>
       
**Example 2: Prune by a list of files (<files>) and by a list of
directories (<folders>)**
                              
This configuration will remove data from database 'mv_database' that
was loaded from the following files:
         
/d3/metprd/grid_stat/grid_stat_APCP_03_030000L_20130705_030000V.stat
/d3/metprd/mode/mode_APCP_06_180000L_20130705_180000V_060000A_obj.txt

And from the following directories:

*/d1/data/arw/FULL/2010051914*
*/d1/data/arw/SWC/2010051914*
*/d1/data/nmm/FULL/2010051914*
*/d1/data/nmm/SWC/2010051914*
*/d1/data/arw/FULL/2010051915*
*/d1/data/arw/SWC/2010051915*
*/d1/data/nmm/FULL/2010051915*
*/d1/data/nmm/SWC/2010051915*

.. code-block:: XML

        <prune_spec>
          <connection>
            <host>db_host:3306</host>
            <database>mv_database</database>
            <user>user_name</user>
            <password>user_password</password>
          </connection>
        
          <info_only>false</info_only>
          
          <files>
            <file>/d3/metprd/grid_stat/grid_stat_APCP_03_030000L_20130705_030000V.stat</file>
            <file>/d3/metprd/grid_stat/grid_stat_APCP_03_030000L_20130705_030000V.stat</file>
          </files>
         
          <folders>
            <date_list name="folder_dates">
              <start>2010051914</start>
              <end>2010051915</end>
              <inc>3600</inc>
              <format>yyyyMMddHH</format>
            </date_list>

            <folder_tmpl>/d1/data/{model}/{vx_mask}/{valid_time}</folder_tmpl>

            <load_val>
              <field name="model">
                <val>arw</val>
                <val>nmm</val>
              </field>

              <field name="valid_time">
                <date_list name="folder_dates"/>
              </field>

              <field name="vx_mask">
                <val>FULL</val>
                <val>SWC</val>
              </field>
            </load_val>
          </folders>
        </prune_spec>

