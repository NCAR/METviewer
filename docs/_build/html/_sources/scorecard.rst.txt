Scorecards
==========

Description
-----------

A scorecard is a graphical representation of significance and performance of a comparison, normally a comparison between two models. The scorecard can only be produced using xml code. The xml code specifications are listed in the detail below. There is no GUI interface. 

Line Type
---------
Scorecards can be run with either summary or aggregation logic. Aggregation logic is the default and generally, the better choice. The following are line types scorecards support: 

* CTC
  
* SL1L2

* SAL1L2

* VL1L2
  
* VAL1L2

* PCT

* GRAD

* NBRCNT

* ECNT

* NBRCTC

* RPS 

How-To
-------

The scorecard module is used to generate a scorecard PNG and HTML images using data in the METviewer database. The usage statement:

**Usage:**

.. code-block:: none

  mv_scorecard.sh scorecard_example.xml

where scorecard_example.xml is the XML that contains all of your scorecard specifications.

The scorecard_example.xml passed to the scorecard module contains information about the scorecard that will be generated. 

.. literalinclude:: scorecard_example.xml

Here is an explanation for the sections:

**<plot_spec>:** This is the top level tag. It stands for "plot specifications". Everything is defined in this tag.

**<connection>:** Please reference the "Common XML Structures" documentation. ??**NOTE: This is in Tatiana's HTML documentation we'll need to incorporate**??

**<rscript>:** Specifies the Rscript instance to use, for example /usr/local/bin/Rscript.

**<folders>:** A structure that specifies the input and output file system folders.
        
        **<r_tmpl>:** The folder that contains the R templates used by METviewer, typically *R_tmpl/* from the METviewer CVS source tree.
        
        **<r_work>:** The folder that will contain the data and generated R scripts, must contain the sub-folder *include/*.
        
        **<plots>:** The output folder that will contain generated image and HTML files.
        
        **<data>:** The output folder that will contain generated data files.
        
        **<scripts>:** The output folder that will contain scripts files.

**<plot>:** A structure that specifies scorecard characteristics.
        
        **<execution_type>:** **Rscript** or **Python**, indicating whether to use Rscript ot Python for the statistics calculation and plotting (if available). This is an optional element. The default value is Rscript.
        
        **<view_value>:** **TRUE** or **FALSE**, indicating whether or not to display statistical values in the scorecard cells. The default value is FALSE (do not display).
        
        **<view_symbol>:** **TRUE** or **FALSE**, indicating whether or not to display a symbol in the scorecard cells. The default value is TRUE (display).
        
        **<view_legend>:** **TRUE** or **FALSE**, indicating whether or not to display a legend for the scorecard. The default value is TRUE (display).
        
        **<stat_flag>:** **NCAR** or **EMC**, which algorithm to use to calculate statistics. The default value is NCAR.
        
        **<printSQL>:** **FALSE** or **TRUE**, indicating whether or not to print SQL queries. The default value is FALSE (do not print).
        
        **<stat>:** **DIFF** (display a difference between models) or **DIFF_SIG** ( display p_value ) or **SINGLE** (display a value of statistic for the 1st model. In this case the value of the 2nd model is optional), indicating which statistic use to display for both - values and symbols - in the cell. The default value is DIFF_SIG (display p_value).
        
        or
        
        **<stat_value>:** **DIFF** (display a difference between models) or **DIFF_SIG** (display p_value) or **SINGLE** (display a value of statistic for the 1st model. In this case the value of the 2nd model is optional), indicating which statistic use to display for values in the cell.
        
        **<stat_symbol>:** **DIFF** (display a difference between models) or **DIFF_SIG** (display p_value) or **SINGLE** (display a value of statistic for the 1st model. In this case the value of the 2nd model is optional), indicating which statistic use to display for symbols in the cell.
        
        **<threshold_file>:** full path to the XML file describing configurations for thresholds, colors and symbols.
        
        **<left_column_names>:** a list of names for the most left columns as in <val>NAME</val> format.
        
        **<symbol_size>:** a size for the symbols. It can be in '%' or 'px'. For example, '120%' will increase the size of symbols in 1.2 times. '20px' can also be used.
        
        **<template>:** the name of the R plot script template to use. The following templates are currently supported: scorecard.R_tmpl

        **<plot_fix>:** describes database fields - models, date range and init hour - that are constant. Model field contains two values - first and second model names. Date range can be specified using 'fcst_valid_beg' or 'fcst_init_beg' and contains ine or more groups of two values - start and end dates. This example will create a scorecard for two periods (07-01 00:00 - 07-03 12:00) and (07-05 00:00 - 07-10 12:00)

        .. code-block:: XML

                <field name="fcst_valid_beg">
                 <val name="2011-07-01 00:00:00"/>
                 <val name="2011-07-03 12:00:00"/>
                 <val name="2011-07-05 00:00:00"/>
                 <val name="2011-07-10 12:00:00"/> 
                </field>

       Init hour can contain one or more values
       
                .. code-block:: XML

                        <field name="model">
                          <val name="first_model"/>
                          <val name="second_model"/>
                        </field>
                        <field name="fcst_valid_beg">
                          <val name="start_date"/>
                          <val name="end_date"/>
                        <!--optional second date range:-->
                          <val name="start_date"/>
                          <val name="end_date" />
                        <!--or optional single date:-->
                          <val name="date"/>
                        </field>
                        <field name="init_hour">
                          <val name="first_hour"/>
                        </field>


           **<rows>:** Describes an individual row in the scorecard table.
           Contains one or more nested **<field>** elements.
           
                **<field>:** Describes an individual row or row group in the scorecard table. Attributes:
                
                        **name:** Database field name
                        
                        **<val>:** describes a value of the field. Field can have more than one value element. Attributes:
                                
                                **name:** Database field name
                                
                                **label:** The label that shows up on the image
                        
                         **<field>:** next level database field name. Field can have more than one field element
           
           **<columns>:** describes an individual column in the scorecard table.
           Contains one or more nested **<field>** elements
           
                **<field>:** Describes an individual column or column group in the scorecard table. Attributes:
                        
                        **name:** Database field name
                                
                        **<val>:** describes a value of the field. Field can have more than one value element. Attributes:
                        
                                **name:** Database field name
                                
                                **label:** The label that shows up on the image
                        
                        **<field>:** next level database field name. Field can have more than one field element

           **<agg_stat>:** **TRUE** (bootstrapping) or **FALSE** (student-T/normal), indicating whether or not to create a scorecard using aggregated statistics. The default value is TRUE (use bootstrapping).
           
           **<boot_repl>:** number of bootstrapping replications, use 1 for no bootstrapping. Used when **<agg_stat>** is TRUE.
           
           **<boot_random_seed>:** value of Random.seed for bootstrapping to get a reproducible random results. Used when **<agg_stat>** is TRUE.
           
           **<plot_stat>:** name of the aggregation statistic. **median** or **mean**. The default value is median.
           
           **<tmpl>:**
           
                **<data_file>:** name of the R data.frame data file, inside of the **<r_work>** folder specified above
                
                **<plot_file>:** name of the output image file, inside of the **<plots>** folder specified above.
                
                **<title>:** title of the scorecard displayed on the image. Default value: Verification Scorecard

Example
-------

The image below shows the output of scorecard_example.xml. 

.. figure:: scorecard_example.png

	    Figure 13.1 Scorecard output example.
