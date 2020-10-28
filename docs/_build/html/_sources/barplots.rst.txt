Bar Plots
=========

Description
-----------

A bar plot shows comparisons among discrete categories. One axis of the chart shows the specific categories being compared, while the other represents some measured value. The heights or lengths are proportional to the values that they represent. Bar plots are simple and flexible, unlike some other METview plot types. Rather than using prescribed statistics in a specific way, the user can select both axes.

Bar plots are distinct from histograms and the two are not interchangeable. Histograms show the frequency of occurrence of values in discrete categories (that are sometimes created by binning continuous values). To create a histogram, the user may only select the variable for a single axis. In a bar plot, two axis values are selected by the user, one categorical and one numeric.

Bar plots often represent counts or frequencies, however, bar plots can represent means, medians, standard deviations, or any other statistic.

How-To
-------

Selection of options to produce the plot proceeds approximately counter-clockwise around the METviewer window. The steps to create a series plot are:

#. Select the desired database from the "Select databases" pulldown menu at the top margin of the METviewer window.
   
#. There are a number of tabs just under the database pulldown menu. Select the 'Bar' tab.

#. Select the type of MET statistics that will be used to create the bar plot. Click on the “Plot Data” pulldown menu which is located under the tabs. The list contains “Stat”, “MODE”, or “MODE-TD”. For details about these types of output statistics in MET, please see the most recent version of the `MET User's Guide <https://dtcenter.org/community-code/model-evaluation-tools-met/documentation>`_.

#. Select the desired variable to calculate statistics for in the "Y1 Axis Variables" tab. The first pulldown menu in the  "Y1 Dependent (Forecast) Variables" section lists the variables available in the selected dataset.

#. Select the desired statistic to calculate in the second pulldown menu which is to the right of variable menu. This lists the available attribute statistics the selected dataset. Multiple statistics can be selected and they will each be plotted as a separate bar on the plot.

#. Select the Y1 Series Variable from the first pulldown menu in that section. There are many options. "MODEL" is used in the included example. In the second pulldown menu to the right of the first are the series variable options, for example, different models.

#. It usually does not make sense to mix statistics for different groups. The desired group to calculate statistics over can be specified using the "Fixed Values" section. In the example below, a precipitation threshold value (category: "FCST_THR", value: ">=2.54") is chosen. If multiple domains or thresholds were chosen, the statistics would be a summary of all of those cases together, which may not always be desired.

#. Select the x-axis value in the "Independent Variable" dropdown menu. For a bar plot, this is often a date, lead time, or threshold. In the example in the next section, the Y1 dependent variable “Object Count” is plotted for two of the “Y1 Series Variable” ensemble members.
   
#. Select the type of statistics summary by selecting either "Summary" or "Aggregation Statistics" button in the “Statistics” section. Aggregated statistics may be selected for certain varieties of statistics. The selection can be made from the leftmost dropdown menu in the "Statistics" section. By default, the median value of all statistics will be plotted. Using the dropdown menu, the mean or sum may be selected instead. Choosing this option will cause a single statistic to be calculated from the individual database lines. 

#. Now enough information has been entered to produce a graph. To do this, click the "Generate Plot" button at the top of the METviewer window (this is in red text). Typically, if a plot is not produced, it is because the database selected does not contain the correct type of data. Also, it is imperative to check the data used for the plot by selecting the "R data" tab on the righthand side, above the plot area. The data from the database that is being used to calculate the statistics is listed in this tab. This tab should be checked to avoid the accidental accumulation of inappropriate database lines. For example, it does not make sense to accumulate statistics over different domains, thresholds, models, etc.

There are many other options for plots, but these are the basics.

Example
--------

The image below shows an example of the plot and set-up options for a series plot in METviewer. This example uses the database “mv_hrrr_sppmp_test” to plot “MODE” output for seven ensemble members. The total object count over the is plotted for 3-hour precipitation accumulation exceeding >=2.54 cm. Appropriate titles and labels have been entered in the titles and labels tab shown below the plot. Colors and line formatting are shown across the bottom menu of the plot. The values here are the defaults.


| The example below shows a bar plot.

.. figure:: barplots_plot.png

Figure 4.1 Example Bar Plot created by METviewer.

Here is the associated xml for this example. It can be copied into an empty file and saved to the desktop then uploaded into the system by clicking on the "Load XML" button in the upper-right corner of the GUI.  This XML can be downloaded by visiting GitHub here ??[insert link to XML]??. 

.. literalinclude:: bar_plots_xml.xml
