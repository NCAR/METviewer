Spread vs. Skill Plots
=======================

Description
-----------

Spread vs. Skill plots are widely used to evaluate the quality of ensemble forecasting systems. One goal of ensemble forecasting is to have the ensemble members represent the range of possible outcomes, which suggests that the ensemble spread should approximately equal the skill of the ensemble mean (as measured by RMSE). Spread and skill will be plotted on the y-axis, and the user may select the x axis variable. Underdispersed ensembles lack spread and overdispersed ensembles have too much spread. 

Ensemble skill is a measure of ensemble accuracy. A perfect spread/skill ratio is 1. 


How-To
-------

Selection of options to produce the spread vs. skill plot  proceeds approximately counter-clockwise around the METviewer window.

#. Select the desired database from the “Select databases” pulldown menu at the top margin of the METviewer window.

#. There are a number of tabs just under the database pulldown menu. Select the ‘Series’ tab, which is the leftmost tab.

#. Select the type of MET statistics that will be used to create the series plot. Click on the “Plot Data” pulldown menu which is located under the tabs. The list contains “Stat”, “MODE”, or “MODE-TD”. For details about these types of output statistics in MET, please see the most recent version of the `MET User's Guide <https://dtcenter.org/community-code/model-evaluation-tools-met/documentation>`_.

#. Select the desired variable to calculate statistics for in the “Y1 Axis Variables” tab. The first pulldown menu in the “Y1 Dependent (Forecast) Variables” section lists the variables available in the selected database.

#. Select the desired statistic to calculate in the second pulldown menu which is to the right of variable menu. This lists the available attribute statistics in the selected dataset. Multiple statistics can be selected and they will each be plotted as a separate line on the plot. For a Spread/Skill plot, two statistics must be selected. In the example below the statistics are "SSVAR_RMSE" and "SSVAR_Spread". 

#. Select the Y1 Series Variable from the first pulldown menu in that section. There are many options. “MODEL” is used in the included example. In the second pulldown menu to the right of the first are the series variable options, for example, different models.

#. It usually does not make sense to mix statistics for different groups. The desired group to calculate statistics over can be specified using the “Fixed Values” section. In the example below, a single domain (category: “VX_MASK”, value: “EAST”) and level (category: "FCST_LEV", value: "Z2")  are chosen. If multiple domains or thresholds were chosen, the statistics would be a summary of all of those cases together, which may not always be desired.

#. Select the x-axis value in the “Independent Variable” dropdown menu. For a spread/skill plot, this is often a date, lead time, or threshold. For a spread/skill plot, this variable must be continuous and there should be reason to believe that the statistic on the Y1 axis is connected across the values of the independent variable. If not, a scatter plot would be a more appropriate choice than the series plot. In the example in the next section, the Y1 dependent variable “Frequency Bias” is plotted for each of the “Y1 Series Variable” ensemble members.

#. Select the type of statistics summary by selecting the “Aggregation Statistics” button in the “Statistics” section. Aggregated statistics must be selected for certain varieties of statistics. The selection can be made from the leftmost dropdown menu in the “Statistics” section. Select the "Spread/Skill Variance (SSVAR)" option in the dropdown menu.

#. Now enough information has been entered to produce a graph. To do this, click the “Generate Plot” button at the top of the METviewer window (this is in red text). Typically, if a plot is not produced, it is because the database selected does not contain the correct type of data. Also, it is imperative to check the data used for the plot by selecting the “R data” tab on the righthand side, above the plot area. The data from the database that is being used to calculate the statistics is listed in this tab. This tab should be checked to avoid the accidental accumulation of inappropriate database lines. For example, it does not make sense to accumulate statistics over different domains, thresholds, models, etc.

There are many other options for plots, but these are the basics.

Example
--------

This example shows the 2-m temperature RMSE and spread for a series of forecast lead times for an ensemble. Plotted are the spread and the skill.   

.. figure:: figure/spreadskill_plot.png
	    
	    Figure 11.1 Example METviewer Spread vs. Skill plot showing temperature RMSE and spread for a series of forecast lead times.
	   
Here is the associated xml for this example. It can be copied into an empty file and saved to the desktop then uploaded into the system by clicking on the “Load XML” button in the upper-right corner of the GUI. This XML can be downloaded by visiting GitHub here ??[insert link to XML]??.

.. literalinclude:: xml/spreadskill_xml.xml
