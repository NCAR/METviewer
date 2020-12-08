============
User's Guide
============

**Foreword: A note to METviewer users**

This user's guide is provided as an aid to users of the METviewer. METviewer
is a database and display system designed to work with the ASCII output
statistics produced by the MET software. MET is a set of verification tools
developed by the Developmental Testbed Center (DTC) for use by the numerical
weather prediction community - and especially users and developers of the
Weather Research and Forecasting (WRF) model - to help them assess and
evaluate the performance of numerical weather predictions.

It is important to note here that MET and METviewer are evolving software
packages. Previous releases of METviewer have occurred each year since 2010.
This documentation describes the |release| release dated |release_date|.
Intermediate releases may include bug fixes. 

This User's Guide was prepared by current and former NCAR employees working
on METviewer, including Tatiana Burek, Tara Jensen, Lindsay Blank,
Tressa Fowler, John Halley Gotway, Lisa Goodrich, and Julie Prestopnik.

**Model Evaluation Tools Viewer (METviewer)  TERMS OF USE - IMPORTANT!**

Copyright |copyright|
Licensed under the Apache License, Version 2.0 (the "License");
You may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See
the License for the specific language governing permissions and limitations
under the License.


**The citation for this User's Guide should be:**

|author_list|, |release_year|: The METviewer Version |version| User's Guide.
Developmental Testbed Center. 
Available at : https://github.com/dtcenter/METviewer/releases. 

**Acknowledgments**

We thank the the National Science Foundation (NSF) along with three
organizations within the National Oceanic and Atmospheric Administration
(NOAA):
1) Office of Atmospheric Research (OAR);
2) Next Generation Global Prediction System project (NGGPS); and
3) United States WeatherResearch Program (USWRP)
for their support of this work. Thanks also go to the staff at
the Developmental Testbed Center for their help, advice, and many
types of support. We released METplus Alpha in February 2017 and
would not have made a decade of cutting-edge verification support
without those who participated in DTC planning workshops and the NGGPS
United Forecast System Strategic Implementation Plan Working Groups
(NGGPS UFS SIP WGs).

The DTC is sponsored by the National Oceanic and Atmospheric
Administration (NOAA), the United States Air Force, and the National
Science Foundation (NSF). NCAR is sponsored by the National Science
Foundation (NSF).


.. toctree::
   :hidden: 
   :caption: Table of Contents
   :numbered:
		

   overview
   installation
   common
   database_loading
   database_scrubbing
   testing
   web_service
   batch
   seriesplots
   barplots
   boxplot
   rocplot
   reliabilityplots
   perfdiag
   taylordiag
   contourplot
   spreadskillplot
   eclvplots
   scorecard
   
Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
