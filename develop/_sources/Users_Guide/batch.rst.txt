Batch Plotting Module
=====================

The batch plotting system is used to generate a number of plots of data in
the METviewer database. The usage statement:

.. code-block:: none
		
  ---- MVBatch ----
  
  Usage: mv_batch
  
         [-list]
         [-printSql]
         plot_spec_file
         [job_name]
     
         where "-list" indicates that the available plot jobs should be listed and no plots run
         "-printSql" print SQL statements
         "plot_spec_file" specifies the XML plot specification document
         "job_name" specifies the name of the job from the plot specification to run
  
  ---- MVBatch Done ----
                  
The plot_spec_file passed to the batch module contains information about
the plots that will be generated. It is an XML file and its top-level tag
is **<plot_spec>** which contains the following elements, divided into
functional sections.
                                                                                                 
NOTE: All **<val>** elements should be listed in alphabetical order.

**<connection>:** Please reference the
`Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_
documentation.

**<rscript>:** (optional) This tag specifies the Rscript instance to
use, for example */usr/local/bin/Rscript*. It defaults to the instance
on the user path.

**<folders>:** A structure that specifies the input and output file
system folders.        

|       **<r_tmpl>:** The folder that contains the R templates used by METviewer, typically *R_tmpl/* from the METviewer CVS source tree.
|       **<r_work>:** The folder that will contain the data and generated R scripts, must contain the sub-folder *include/*        
|       **<plots>:** The output folder that will contain the generated plots.
|

**<date_list>:** Please reference the
`Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_
documentation. 

**<date_range>:** A structure that specifies a date range, including all
dates and times between. 

|        **<start>:** The start date and time of the date list, specified in the format yyyy-MM-dd [HH:mm:ss], or with a **<date_offset>** structure. See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|        **<end>:** The end date and time of the date list, specified in the format yyyy-MM-dd [HH:mm:ss], or with a **<date_offset>** structure.  See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|

**<date_range_list>:** A structure that specifies a list of date ranges,
conceptually, a list of **<date_range>** structures.

|       **<range_start>:** The start date and time of the first date range of the list, specified in the format yyyy-MM-dd [HH:mm:ss], or with a **<date_offset>** structure.  See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_     
|       **<range_end>:** The start date and time of the last date range of the list, specified in the format yyyy-MM-dd [HH:mm:ss], or with a **<date_offset>** structure.  See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_   
|       **<range_length>:** The range length, in minutes.       
|       **<inc>:** The increment, in minutes, between the end of the previous range member and the start of the next.
|

| **<plot>:** a structure that specifies plot characteristics and may inherit characteristics from other <plot> structures
|
| **Attributes:**
|       **name:** Name for the plot structure, used when plot's characteristics are inherited by later **<plot>** structures.
|
|       **inherits:** Name of one or more **<plot>** structure's characteristics to inherit, separated by commas (no spaces). Plots named later on the list override the characteristics of those earlier on the list.        
|       **run:** (optional) **TRUE** or **FALSE**, specifying whether or not to run this plot job.
|
|       **<template>:** The name of the R plot script template to use. The following templates are currently supported: 
|
|               **series_plot.R_tmpl:** Use for time series, threshold series, date series, and other series type plots which display a fcst_var and statistic vs. A stat_header quantity.
|               **bar_plot.R_tmpl:** Use for bar plots of fcst_var/statistic vs. stat_header quantities.
|               **box_plot.R_tmpl:** Use for box plots of fcst_var/statistic vs. stat_header quantities.
|               **rhist.R_tmpl:** Use for ensemble rank histograms, only fixed data must be specified.
|               **roc.R_tmpl:** Use for ROC plots, only fixed data and PCT/CTC data must be specified.
|               **rely.R_tmpl:** Use for reliability diagrams, only fixed data must be specified.
|               **ens_ss.R_tmpl:** Use ensemble spread/skill diagrams, fixed data and series must be specified.
|
|       **<indep>:** A structure that defines the independent variable and its values for the plot.
|        **Attributes:**                
|                 **name:** Database field name of the independent variable.
|                 **depends:** The field name of one of the plot_fix field structures; will be used to calculate independent variable values.                
|                 
|                 **<val>:** A single value.
|                 **Attributes:**                     
|                        **label:** (optional) x-axis label.                       
|                        **plot_val:** (optional) x-axis position.
|       
|                 **<date_list>:** A structure that specifies a list of date strings in a certain format.
|                        **<start>:** The start date and time of the date list, specified in the format yyyy-MM-dd HH:mm:ss.                        
|                        **<end>:** The end date and time, specified in the format yyyy-MM-dd HH:mm:ss.                        
|                        **<inc>:** The increment, in seconds, between successive members of the date list.                       
|                        **<label_format>:** The date format, specified by the java class SimpleDateFormat.
|
|        **<series1>:** Describes the database field names and values that constitute the various lines, or series, corresponding to the left y axis (y1-axis) of the plot.
|                **<field>:** A database field whose values constitute the members of the series.   
|                 **Attributes:**
|                         **name:** The database field name. Its values are contained in the child tags.
|
|                **<val>:** A single value.
|

         **<series2>:** Describes the database field names and values
	 that constitute the various lines, or series, corresponding to
	 the right y axis (y2-axis) of the plot. The structure is the same
	 as **<series1>** above.

       **<series_nobs>:** Indicates which database fields should be used
       to calculate the number of observations (NOBS) for the plot.

|       **<dep1_scale>** or **<dep2_scale>:** Specifies a scale factor for a fcst_var/stat pair, for example to convert the units of a statistic.
|                **<field>:** Contains the value of the scalar to apply.
|                **Attributes:**
|                        **name:** The fcst_var/stat name pair in the format [fcst_var]_[stat].
|

|       **<plot_fix>:** Describes the database fields and values that are constant on a given plot, or fixed. Each permutation of plot_fix field values will be used to create a database query and all plots will be run with the resulting data held constant. The fields can be used in the tmpl values, described below, to create paths, filenames and labels for the plots.        
|       **<field>:** A database field. Its values constitute the members of the series.        
|       **Attributes:**                
|               **name:** The database field name. Its values are contained in the child tag.
|                
|               **<val>:** A single value.
|               **<date_list>:** Refers to a previously declared **<date_list>**, using the name attribute. See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|               **<date_range>:** Refers to a previously declared **<date_range>**, using the name attribute. See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|               **<date_range_list>:** Refers to a previously declared **<date_range_list>**, using the name attribute. See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|               **<set>:** A set of values.
|               **Attributes:**                       
|                       **name:** (optional) Name of the set, used in the field template.
|                        
|                       A set can contain one or more of the following:
|                       **<val>:** A single value.
|                       **<date_list>:** Refers to a previously declared **<date_list>**, using the name attribute.  See `Common XML Structures <https://dtcenter.github.io/METviewer/latest/Users_Guide/common.html>`_.
|

        **<plot_cond>:** Contains a SQL clause that will be inserted with the clause generated by the **<plot_fix>** information, allowing a user to specify record conditions for plotted statistics. Examples include:

                .. code-block:: XML
                        
                        <plot_cond>ld.baser > 0.02</plot_cond>


        **<agg>:** This tag has been replaced by **<plot_fix>**.
	Do not use **<agg>**.
                
|       **<tmpl>:**                     
|               **<val_map>:** A map of key/value pairs used for when templates are populated.
|               **Attributes:**                        
|                        **name:** The database field name. Its values should be mapped using the enclosed pairs.
|
|                        **<pair>:** A single value pair.
|                          **<key>:** The template field value as taken from the database.
|                          **<val>:** The value to use as a replacement.
|
|               **<data_file>:** Path and name of the R data.frame data file, inside of the **<r_work>** folder specified above.
|               **<plot_file>:** Path and name of the output plot image file, inside of the **<plots>** folder specified above.
|               **<r_file>:** Path and name of the R script that generates the plot, inside of the **<r_work>** folder specified above.
|               **<title>:** Title that will be placed on top of the plot.
|               **<x_label>:** x-axis label that will be placed on the plot.
|               **<y1_label>:** Left y axis (y1-axis) label that will be placed on the plot.
|               **<y2_label>:** Right y axis (y2-axis) label that will be placed on the plot.
|               **<caption>:** Caption that will be placed on the bottom of the plot.
|               **<listDiffSeries1>:** Turns on a difference curve should be plotted for the y1 series; calculated as the pairwise difference between the members of the first series minus the second - example:list(c("series1","series2"), c("series3","series4")).
|               **<listDiffSeries2>:** Turns on a difference curve should be plotted for the y2 series; calculated as the pairwise difference between the members of the first series minus the second - example:list(c("series1","series2"), c("series3","series4")).
|

**Template Formatting**

Template values have the syntax
{var_name[?param1=val1[&amp;param2=val2[...]]]}. For example,
{fcst_lead?format=HH} or {vx_mask?map=masks} or
{fcst_thresh?units=in&amp;symbol=letters}. The var_name must be one of
the database fields that is used in the plot fixed variables or agg
variables. When the template is applied, the current plot values for each
variable replaces the tag in the template. The parameters can be used to
format, convert or map the values. The dependent variables forecast
variables are represented using the syntax {depY_N}, where Y is the
y-axis (either 1 or 2) and N is the index of the fcst_var in the
**<dep>** structure, starting with 1. The dependent variable statistics
are represented using the syntax {depY_N_statS}, where Y and N match the
values of the parent fcst_var, and S is the index of the statistic,
starting at 1 for each fcst_var.

|               **map=[name]:** Indicates that the template **<val_map>** structure with the given name should be applied
|               **format=[format_type]:** Depending on the variable being formatted the following options apply:
|                       **fcst_lead:** Use HH for the two-character wide hour, HHmm for the four-character hour and minute
|                       **init_hour** or **valid_hour:** Use HH for the two-character wide hour init_hour.
|                       **fcst_thresh**, **fcst_thr**, **obs_thresh** or **obs_thr:** Apply the specified decimal format to the threshold value, according to the formatting syntax for the java class DecimalFormat.
|               For any variable, format=R will replace all non-variable characters (like <, = and .) with appropriate letters and underscores.
|               **units=in:** Can only be used if the variable is a threshold, converts the threshold value to inches from mm.
|               **symbol=letters:** Can only be used if the variable is a threshold, converts the threshold criteria to FORTRAN-like letters, e.g. >= becomes ge.
|
       
|       **<dep>:** Information about the dependent variables of the plot. Each curve of the plot is assumed to have a constant fcst_var and constant statistic. The stats plotted on the two dependent axes (y1 and y2) are specified using the identical dep1 and dep2 structures. Fixed values, which correspond to each fcst_var, are specified using the fix structure. The MODE statistics documentation can be viewed below on this page.
|               **<dep1>:** The fcst_var/stat pairs for the first dependent axis (y1).
|                       **<fcst_var>:** The fcst_var for which the enclosed list of stats will be plotted.
|                       **Attributes:**
|                               **name:** The fcst_var whose statistics will be plotted.
|                               
|                               **<stat>:** The name of the stat to plot. Its name must be identical to the name in the `MET User's Guide <https://dtcenter.org/community-code/model-evaluation-tools-met/documentation>`_.
|
|               **<dep2>:** The fcst_var/stat pairs for the second dependent axis (y2). Its structure is identical to that of **<dep1>**
|               **<fix>:** Do not use **<fix>**, use **<plot_fix>** instead.
|

        **<bootstrapping>:** Replaced by **<agg_stat>**, do not use **<bootstrapping>**.

|       **<agg_stat>:** Contains information about how to calculate and display aggregated statistics. If present, agg_stat is turned on. Depending on the selected **<dep>** statistics, only certain settings are appropriate.
|               **<agg_ctc>:** **TRUE** or **FALSE**, indicating whether or not to aggregate CTCs.
|               **<agg_sl1l2>:** **TRUE** or **FALSE**, indicating whether or not to aggregate SL1L2s.
|               **<agg_sal1l2>:** **TRUE** or **FALSE**, indicating whether or not to aggregate SAL1L2s.
|               **<agg_pct>:** **TRUE** or **FALSE**, indicating whether or not to aggregate PCTs.
|               **<agg_nbrcnt>:** **TRUE** or **FALSE**, indicating whether or not to aggregate NBR_CNTs.
|               **<agg_ssvar>:** **TRUE** or **FALSE**, indicating whether or not to aggregate SSVARs.
|               **<agg_vl1l2>:** **TRUE** or **FALSE**, indicating whether or not to aggregate VL1L2s.
|               **<agg_val1l2>:** **TRUE** or **FALSE**, indicating whether or not to aggregate Vector Anomaly Partial Sums.
|               **<agg_grad>:** **TRUE** or **FALSE**, indicating whether or not to aggregate GRADs.
|               **<boot_repl>:** Number of bootstrapping replications, use 1 for no bootstrapping.
|               **<boot_random_seed>:** Bootstrapping seed.
|               **<boot_ci>:** Type of confidence interval to calculate, passed to the boot.ci() R function (e.g. bca).
|               **<cache_agg_stat>:** **true** or **false**, turns on/off the prevention the reuse of existing bootstrapping output data.
|

                When using **<agg_stat>**, the following constraints and
		conditions apply:
        
                        * To turn off bootstrapping, set **<boot_repl>** to 1.
          
                        * agg_stat types must only be used with appropriate
			  statistics for each, and only one can be used on
			  a single plot.

                        * agg_stat is performed once for each plot that is
			  created.

                        * A plot cannot have both agg_stat and non-agg_stat
			  data on the same plot.

                        * If the agg_stat difference curve is turned on,
			  the plot1 difference curve must be turned off.
          
                        * To display the calculated bootstrap CIs, set
			  the plot_cis to "boot".
          
                        * Bootstrapping CIs cannot be calculated for PSTD
			  stats, instead use the "brier" CI type for Brier
			  score.
          
                        * The plot must contain statistics for only one
			  fcst_var.
          
                        * All series on the plot must be derived from the
			  same data
          
                        * If agg_stat is turned on, event equalization
			  must be turned off (it is done automatically).
          
                        * agg_stat cannot be used on the same plot with
			  calc_stat.

|       **<calc_stat>:** Contains flags telling METviewer to calculate the plot statistics from CTCs or partial sums. If present, calc_stat is turned on. Depending on the selected **<dep>** statistics, only certain settings are appropriate.
|               **<calc_ctc>:** **TRUE** or **FALSE**, indicating whether or not to calculate CTC stats.
|               **<calc_sl1l2>:** **TRUE** or **FALSE**, indicating whether or not to calculate SL1L2 stats.
|               **<calc_sal1l2>:** **TRUE** or **FALSE**, indicating whether or not to calculate SAL1L2 stats.
|               **<calc_vl1l2>:** **TRUE** or **FALSE**, indicating whether or not to calculate VL1L2 stats.
|        
|        When using **<calc_stat>**, the following constraints apply:

                * calc_ctc, calc_sl1l2, calc_sal1l2, calc_vl1l2 must only
		  be used with appropriate statistics for each, and only
		  one can be used on a single plot.
          
                * A plot cannot have both calc_stat and non-calc_stat data
		  on the same plot.
          
                * calc_stat cannot be used on the same plot with agg_stat.


|       **<roc_calc>:** Contains flags telling METviewer how to calculate the points on the ROC curve: from PCTs or CTCs. This setting is required if the roc.R_tmpl is used, otherwise, it is ignored.
|               **<roc_pct>:** **TRUE** or **FALSE**, indicating whether or not to calculate ROC points using Probabilistic Contingency Table Counts (PCTs).
|               **<roc_ctc>:** **TRUE** or **FALSE**, indicating whether or not to calculate ROC points using Contingency Table Counts (CTCs).
|        
                When using **<roc_calc>**, the following constraints apply:
        
                        * Only one of roc_pct or roc_ctc can be used on a
			  single plot.
        
                        * If using roc_pct, select a list of probabilistic
			  threshold values as a fcst_thresh **<plot_fix>**
			  value.
          
                        * If using roc_pct, select a single obs_thresh
			  **<plot_fix>** value.
          
                        * If using roc_ctc, select two or more fcst_thresh
			  **<plot_fix>** values and a single obs_thresh
			  **<plot_fix>** value.

|        **<normalized_histogram>:** Contains flags telling METviewer which type of histogram to build: normalized or raw counts plot. This setting is required if the rhist.R_tmpl is used, otherwise, it is ignored. The default value is **TRUE** (normalized histogram).
|               **<normalized_histogram>:** **TRUE** or **FALSE**, indicating whether to build a normalized or raw counts histogram.
|

        **<ensss_pts>:** Number of binned points.
        
                * If the number of binned points is not specified or < 1,
		  use a default.
          
                * If the number of binned points is > 10 , use 1.
          
                * In other cases use the result of division of the number
		  of points on 10.

|      **<execution_type>:** Rscript or Python, indicating whether to use Rscript or Python for the statistics calculation and plotting (if available). This is an optional element. The default value is Rscript.
|      **<event_equal>:** **true** or **false**, turns on the event equalizer.
|      **<event_equal_m>:** **true** or **false**, turns on the event equalizer for multiple events.
|      **<vert_plot>:** **true** or **false**, turns on vertical levels plotting.
|      **<x_reverse>:** **true** or **false**, reverse the direction of the x-axis.
|      **<num_stats>:** **true** or **false**, turns on the number of stats for each independent variable value, shown along the x2 axis.
|      **<indy1_stag>:** **true** or **false**, indicates whether y1 series points should be staggered at each independent variable value, for visibility.
|      **<indy2_stag>:** **true** or **false**, indicates whether y2 series points should be staggered at each independent variable value, for visibility.
|      **<grid_on>:** **true** or **false**, turns on gridding on the plot.
|      **<sync_axes>:** **true** or **false**, indicates that the y1 and y2 axes should be the same.
|      **<dump_points1>:** **true** or **false**, turns on a report for the y1 series points.
|      **<dump_points2>:** **true** or **false**, turns on a report for the y2 series points.
|      **<log_y1>:** **true** or **false**, turns on log-scale on the y1 axis.
|      **<log_y2>:** **true** or **false**, turns on log-scale on the y2 axis.
|      **<varianceInflationFactor>:** **true** or **false**, include/exclude the variance inflation factor when computing standard errors for means and medians - **TRUE**: include, **FALSE**: exclude - default value: **TRUE**.
|      **<order_series>:** **true** or **false**, include/exclude the variance inflation factor when computing standard errors for means and medians - **TRUE**: include, **FALSE**: exclude - default value: **TRUE**.
|      
|      The following group of settings are each optional and act as placeholders for R plotting function arguments. Specified values should follow the format of the default values listed. The values are passed directly to plotting functions which can be viewed in the R template files for further details. The R function and parameter name that the value is used for is listed after the tag name below. The default value is also shown for each setting.
|       
|      **<plot_type>:** bitmap(type) - plot image type - default value: png256
|      **<plot_height>:** bitmap(height) - height of the display region, in specified units - default value: 8.5 
|      **<plot_width>:** bitmap(width) - width of the display region, in specified units - default value: 11
|      **<plot_res>:** bitmap(res) - resolution, in dots per inch - default value: 72
|      **<plot_units>:** bitmap(units) - units for height and width - default value: in
|      **<mar>:** par(mar) - lines of margin in form c(bottom, left, top, right) - default value: c(8, 4, 5, 4)
|      **<mgp>:** par(mgp) - margin line for axis title, axis labels and axis line in form c(title, labels, line) - default value: c(1, 1, 0)
|      **<cex>:** par(cex) - magnification for plot text - default value: 1
|      **<title_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 2
|      **<title_size>:** mtext(cex) - relative magnification - default value: 1.4
|      **<title_offset>:** mtext(padj) - axis label perpendicular location adjustment - default value: -.4
|      **<title_align>:** mtext(adj) - axis label parallel location adjustment - default value: .5
|      **<xtlab_orient>:** axis(las) - axis label orientation - 0: parallel to axis, 1: horiz, 2: perp to axis, 3: vert - default value: 1
|      **<xtlab_perp>:** axis(padj) - axis label perpendicular location adjustment - default value: -.75
|      **<xtlab_horiz>:** axis(hadj) - axis label parallel location adjustment - default value: .5
|      **<xtlab_freq>:** axis(labels) - frequency of axis labels, 0 to disable - default value: 0
|      **<xtlab_size>:** axis(cex) - relative axis label magnification - default value: 1
|      **<xlab_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 1
|      **<xlab_size>:** mtext(cex) - relative axis label magnification - default value: 1
|      **<xlab_offset>:** mtext(padj) - axis label perpendicular location adjustment - default value: 2
|      **<xlab_align>:** mtext(adj) - axis label parallel location adjustment - default value: .5
|      **<ytlab_orient>:** axis(las) - axis label orientation - 0: parallel to axis, 1: horiz, 2: perp to axis, 3: vert - default value: 1
|      **<ytlab_perp>:** axis(padj) - axis label perpendicular location adjustment - default value: .5
|      **<ytlab_horiz>:** axis(hadj) - axis label parallel location adjustment - default value: .5
|      **<ytlab_size>:** axis(cex) - relative axis label magnification - default value: 1
|      **<ylab_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 1
|      **<ylab_size>:** mtext(cex) - relative axis label magnification - default value: 1
|      **<ylab_offset>:** mtext(padj) - axis label perpendicular location adjustment - default value: -2
|      **<ylab_align>:** mtext(adj) - axis label parallel location adjustment - default value: .5
|      **<grid_lty>:** abline(lty) - line type - 0: blank, 1: solid, 2: dashed, 3: dotted, 4: dotdash, 5: longdash, 6: twodash - default value: 3
|      **<grid_col>:** abline(col) - line color - default value: #CCCCCC
|      **<grid_lwd>:** abline(lwd) - line width - default value: 1
|      **<grid_x>:** abline(v) - list of positions for the gridlines - default value: listX
|      **<x2tlab_orient>:** axis(las) - axis label orientation - 0: parallel to axis, 1: horiz, 2: perp to axis, 3: vert - default value: 1
|      **<x2tlab_perp>:** axis(padj) - axis label perpendicular location adjustment - default value: 1
|      **<x2tlab_horiz>:** axis(hadj) - axis label parallel location adjustment - default value: .5
|      **<x2tlab_size>:** axis(cex) - relative axis label magnification - default value: .8
|      **<x2lab_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 1
|      **<x2lab_size>:** mtext(cex) - relative axis label magnification - default value: .8
|      **<x2lab_offset>:** mtext(padj) - axis label perpendicular location adjustment - default value: -.5
|      **<x2lab_align>:** mtext(adj) - axis label parallel location adjustment - default value: .5
|      **<y2tlab_orient>:** axis(las) - axis label orientation - 0: parallel to axis, 1: horiz, 2: perp to axis, 3: vert - default value: 1
|      **<y2tlab_perp>:** axis(padj) - axis label perpendicular location adjustment - default value: .5
|      **<y2tlab_horiz>:** axis(hadj) - axis label parallel location adjustment - default value: .5
|      **<y2tlab_size>:** axis(cex) - relative axis label magnification - default value: 1
|      **<y2lab_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 1
|      **<y2lab_size>:** mtext(cex) - relative axis label magnification - default value: 1
|      **<y2lab_offset>:** mtext(padj) - axis label perpendicular location adjustment - default value: 1
|      **<y2lab_align>:** mtext(adj) - axis label parallel location adjustment - default value: .5
|      **<legend_size>:** legend(size) - relative magnification - default value: .8
|      **<legend_box>:** legend(bty) - legend box type - o: box, n: none - default value: o
|      **<legend_inset>:** legend(inset) - inset distance from margin - default value: c(0, -.25)
|      **<legend_ncol>:** legend(ncol) - number of columns in legend - default value: 3
|      **<caption_weight>:** mtext(font) - 1: plain text, 2: bold, 3: italic, 4: bold italic and 5: symbol - default value: 1
|      **<caption_col>:** mtext(col) - font color - default value: #999999
|      **<caption_size>:** mtext(cex) - relative magnification - default value: .8
|      **<caption_offset>:** mtext(padj) - axis perpendicular location adjustment - default value: 5
|      **<caption_align>:** mtext(adj) - axis parallel location adjustment - 0:left, 1: right - default value: 0
|      **<box_pts>:** points() - draw points instead of boxes - **TRUE**: draw, **FALSE**: no draw - default value: **FALSE**.
|      **<box_outline>:** boxplot(outline) - draw outliers - **TRUE**: draw, **FALSE**: no draw - default value: **TRUE**.
|      **<box_boxwex>:** boxplot(boxwex) - scale factor to apply to all box widths - default value: 1
|      **<box_notch>:** boxplot(notch) - draw box notches - **TRUE**: notches on, **FALSE**: notches off - default value: **FALSE**
|      **<box_avg>:** points() - draw box averages as asterisks - **TRUE**: on, **FALSE**: off - default value: **FALSE**
|      **<rely_event_hist>:** - draw histogram of events by threshold on reliability diagram - **TRUE**: draw, **FALSE**: no draw - default value: **TRUE**
|      **<ci_alpha>:** alpha value used to calculate the standard median error confidence intervals - default value: .05
|

       The following group of settings should be set to lists of values
       where the length of each list is equal to the number of series that
       are specified for the plot. Calculating the number of series and
       furthermore, the order of the series, can be done by considering
       the series1, series2 and dep values. For each dep1 statistic,
       enumerate each series1 permutation. Then, do the same for each dep2
       statistic.


|      **<plot_ci>:** confidence interval type for each series - options: none, norm, boot, brier, std - example: c("norm", "none")
|      **<show_signif>:** if the statistically significant CI's should be highlighted c(FALSE, TRUE)
|      **<plot_disp>:** indicates which series are visible or not using TRUE/FALSE - example: c(TRUE, FALSE)
|      **<colors>:** lines(col) - line color for each series - example: c("#33FF00FF", "#00FF19FF")
|      **<pch>:** lines(pch) - point symbol - see example:(points) - example: c(19, 19)
|      **<type>:** lines(type) - series line type - p: points, l: lines, o: overplotted points and lines, b or c: points joined by lines, s or S: stair steps, h: histogram, n: nothing - example: c("b", "h")
|      **<lty>:** lines(lty) - line type - 0: blank, 1: solid, 2: dashed, 3: dotted, 4: dotdash, 5: longdash, 6: twodash - example: c(1, 2)
|      **<lwd>:** lines(lwd) - line width - example: c(2, 2)
|      **<con_series>:** if a series contains NAs, specifies whether to connect the points or not - 0: do not connect, 1: connect - example: c(0, 1)
|
|      **<legend>:** legend labels for each series - example: c("series1", "series2")
|      **<order_series>:** the order of the series - example: c(1, 2, 3)
|      **<x1_lim>:** lower y axis (x1-axis) limits - example: c(10,15) or c("2017-05-10 00:00:00","2017-05-15 06:00:00")
|      **<y1_lim>:** left y axis (y1-axis) limits - example: c(0,5)
|      **<y1_bufr>:** left y axis (y1-axis) top and bottom buffer - example: .04
|      **<y2_lim>:** right y axis (y2-axis) limits - example: c(0,1)
|      **<y2_bufr>:** right y axis (y2-axis) top and bottom buffer - example: .04
|      **<plot_cmd>:** extra R commands to execute at the end of the plot script - example: par(usr=listY1Usr); abline(h=1, lty=1, col="#999999FF");
|
|      Elements specific to ROC curve:
|      **<add_point_thresholds>:** **TRUE** or **FALSE**, indicating whether or not to display threshold points on the plot. The default value is **TRUE** (display threshold points).
|
|      Elements specific to Rely plot:
|      **<add_skill_line>:** **TRUE** or **FALSE**, indicating whether or not to display skill line on the graph. The default value is **TRUE** (display skill line).
|      **<add_reference_line>:** **TRUE** or **FALSE**, indicating whether or not to display the reference line on the graph. The default value is **TRUE** (display reference line).
|

MODE Statistics
---------------

Single Object Statistics
~~~~~~~~~~~~~~~~~~~~~~~~
MODE statistics are broken up into two categories: single and pair
statistics. Single statistics are suffixed with a three letter code which
indicates the group of objects over which the statistic should be calculated
or aggregated. The syntax for the three letter code is as follows:
STATNAME_CCC where STATNAME is one of the Single Object Statistics in the
table below, and CCC follows the following system:

[A|F|O|D] - indicating All, Forecast, Observed or a Difference between the
forecast statistic and the observed statistic

[A|S|C] - indicating All, Simple or Cluster objects

[A|M|U] - indicating All, Matched or Unmatched objects

.. list-table::
  :widths: auto
  :header-rows: 1
                    
  * - Single Object Stat
    - METviewer Stat Name
  * - SUM(area) / total
    - ACOV
  * - COUNT(object_id)
    - CNT
  * - COUNT(object_id)
    - CNTSUM
  * - centroid_x
    - CENTX
  * - centroid_y
    - CENTY
  * - centroid_lat
    - CENTLAT
  * - centroid_lon
    - CENTLON
  * - axis_avg
    - AXAVG
  * - length
    - LEN
  * - width
    - WID
  * - area
    - AREA
  * - area_threshold
    - AREATHR
  * - curvature
    - CURV
  * - curvature_x
    - CURVX
  * - curvature_y
    - CURVY
  * - complexity
    - CPLX
  * - intensity_10
    - INT10
  * - intensity_25
    - INT25
  * - intensity_50
    - INT50
  * - intensity_75
    - INT75
  * - intensity_90
    - INT90
  * - intensity_nn
    - INTN
  * - intensity_sum
    - INTSUM

In addition to the single object statistics, METviewer supports ratios of
single object counts and areas, and also some contingency table statistics.
These statistics are specified in the table below with their corresponding
interpretation. For the area-weighted version of these statistics, replace
RATIO\_ with AREARAT\_ and OBJ with OBJA.


.. list-table::
  :widths: auto
  :header-rows: 1

  * - Interpretation
    - METviewer Stat Name
  * - What percentage of simple objects are forecast?
    - RATIO_FSA_ASA
  * - What percentage of simple objects are observation?
    - RATIO_OSA_ASA
  * - What percentage of simple objects are matched?
    - RATIO_ASM_ASA
  * - What percentage of simple objects are unmatched?
    - RATIO_ASU_ASA
  * - What percentage of simple forecast objects are matched?
    - RATIO_FSM_FSA
  * - What percentage of simple forecast objects are unmatched?
    - RATIO_FSU_FSA
  * - What percentage of simple observation objects are matched?
    - RATIO_OSM_OSA
  * - What percentage of simple observation objects are unmatched?
    - RATIO_OSU_OSA
  * - What percentage of simple matched objects that are forecast?
    - RATIO_FSM_ASM
  * - What percentage of simple matched objects that are observation?
    - RATIO_OSM_ASM
  * - What percentage of simple unmatched objects that are forecast?
    - RATIO_FSU_ASU
  * - What percentage of simple unmatched objects that are observation?
    - RATIO_OSU_ASU
  * - What percentage of all objects are simple?
    - RATIO_ASA_AAA
  * - What percentage of all objects are cluster?
    - RATIO_ACA_AAA
  * - What percentage of all forecast objects are simple?
    - RATIO_FSA_FAA
  * - What percentage of all forecast objects are cluster?
    - RATIO_FCA_FAA 
  * - What percentage of all observation objects are simple?
    - RATIO_OSA_OAA
  * - What percentage of all observation objects are cluster?
    - RATIO_OCA_OAA
  * - What percentage of cluster objects are forecast?
    - RATIO_FCA_ACA
  * - What percentage of cluster objects are observation?
    - RATIO_OCA_ACA  
  * - What is the ratio of simple forecasts to simple observations (frequency bias)?
    - RATIO_FSA_OSA
  * - What is the ratio of simple observations to simple forecasts (1 / frequency bias)?
    - RATIO_OSA_FSA
  * - What is the ratio of cluster objects to simple objects?
    - RATIO_ACA_ASA
  * - What is the ratio of simple objects to cluster objects?
    - RATIO_ASA_ACA
  * - What is the ratio of cluster forecast objects to simple forecast objects?
    - RATIO_FCA_FSA
  * - What is the ratio of simple forecast objects to cluster forecast objects?
    - RATIO_FSA_FCA
  * - What is the ratio of cluster observation objects to simple observation objects?
    - RATIO_OCA_OSA
  * - What is the ratio of simple observation objects to cluster observation objects?
    - RATIO_OSA_OCA
  * - Hits = (FSM + OSM)/2
    - OBJHITS
  * - Misses = OSU
    - OBJMISSES
  * - False Alarms = FSU
    - OBJFAS
  * - Correct Negatives (areas only) = [(TOT - FSA) + (TOT - OSA)] / 2
    - (not implemented)
  * - CSI = hits / (hits + misses + false alarms) = [(FSM + OSM)/2] / [(FSM + OSM)/2 + OSU + FSU]
    - OBJCSI
  * - PODY = hits / (hits + misses) = [(FSM + OSM)/2] / [(FSM + OSM)/2 + OSU]
    - OBJPODY
  * - FAR = false alarms / (hits + false alarms) = FSU / [(FSM + OSM)/2 + FSU]
    - OBJFAR
  * - FBIAS = RATIO_FSA_OSA = FSA / OSA (a.k.a. RATIO_FSA_OSA above)
    - OBJFBIAS

Pair Object Statistics
~~~~~~~~~~~~~~~~~~~~~~

MODE object pair statistics are specified using the following format:
STATNAME_CC where STATNAME is one of the Object Pair Statistics in the
table below, and CC follows the following system:

[A|S|C] - indicating All, Simple or Cluster pairs

[A|M|U] - indicating All, Matched or Unmatched objects


.. list-table::
  :widths: auto
  :header-rows: 1
          
  * - Object Pair Stat
    - METviewer Stat Name
  * - centroid_dist
    - CENTDIST
  * - boundary_dist
    - BOUNDDIST
  * - convex_hull_dist
    - HULLDIST
  * - angle_diff
    - ANGLEDIFF
  * - area_ratio
    - AREARAT
  * - intersection_area
    - INTAREA
  * - union_area
    - UNIONAREA
  * - symmetric_diff
    - SYMDIFF
  * - intersection_over_areai
    - INTOVERAREA
  * - complexity_ratio
    - CMPLXRATIO
  * - percentile_intensity_ratio
    - PERCINTRATIO
  * - interest
    - INT
  * - MAX(interest)
    - MAXINT
  * - MAX(interest) for fcst objects
    - MAXINTF
  * - MAX(interest) for obs objects
    - MAXINTO

