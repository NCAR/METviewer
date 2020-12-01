.. _install:

METviewer Installation Guide
============================

Introduction
------------

This chapter describes how to install METviewer. METviewer has been developed and tested on Linux operating systems. Support for additional platforms and compilers may be added in future releases. 

Installing METviewer
--------------------

METviewer relies on the following tools. These must be installed and tested prior to installing METviewer:

**Java JDK 1.8+**

**ant** - `download ant <http://ant.apache.org/bindownload.cgi>`_ and install the latest version.

**MySQL** - `download MySQL <https://dev.mysql.com/downloads/mysql/>`_ and install the latest version. Use "SET GLOBAL max_allowed_packet=110000000;" by typing the command in MySQL and/or make the corresponding edit to /etc/my.cnf, so that the change persists after the next reboot.

**Apache Tomcat** - `download Apache Tomcat <http://tomcat.apache.org/>`_ and install the latest version; test the sample JSP web apps.

**R** - `download R <https://dev.mysql.com/downloads/mysql/>`_ and install the latest version.

#. Configure the batch tool, database loader tool, and web application:

   * edit [install]/metviewer/bin/mv_batch.sh:
     
        * set the variable JAVA to point at the desired jvm instance to run
        * set the variable MV_HOME to point at [install]/metviewer

   * edit [install]/metviewer/bin/mv_load.sh:
        
        * set the variable JAVA to point at the desired jvm instance to run
        * set the variable MV_HOME to point at [install]/metviewer
        
   * edit [install]/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties:
        
        * set log4j.appender.logfile.File setting to the absolute path of [install]/metviewer/log file
        
   * edit [install]/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties:
        
        * set db.host to the MySQL database server host and port, e.g. db.ncep.gov:3306
        * set db.user and db.password to the database username and password
        * set cache.val to true/false depending on whether value caching should be enabled (recommend true)
        * set cache.stat to true/false depending on whether statistics caching should be enabled (recommend true)
        * set folders.plot_xml to the absolute path of [output_dir]/xml
        * set folders.r_tmpl to the absolute path of [tomcat]/metviewer/R_tmpl
        * set folders.r_work to the absolute path of [tomcat]/metviewer/R_work
        * set folders.plots to the absolute path of [output_dir]/plots
        * set folders.data to the absolute path of [output_dir]/data
        * set folders.scripts to the absolute path of [output_dir]/scripts
        * set redirect to the application name in url (ex. if the application URL is "http://www.dtcenter.org/met/metviewer/" redirect is "metviewer")
        * set url.output to the url to the output folder

#. Configure the batch tool, database loader tool, and web application:

   * Create output folders: [output_dir]/plots, [output_dir]/data, [output_dir]/scripts, [output_dir]/xml
   
   * Include [output_dir] to Apache context

#. Build and deploy the application:
   
   * Build METviewer and the web application:
     
   .. code-block:: none
        
        $ cd [install]/metviewer
        $ ant

  * Install load_batch module:
   
   .. code-block:: none

        $ cd [tomcat_base]/webapps
        $ cp [install]/dist/metviewer.war ./

   * Deploy the web app to tomcat:

   .. code-block:: none
        
        $ cd [install]/dist
        $ cp [install]/dist/batch_load.tar [load_batch_dir]
        $ cd [load_batch_dir]
        $ tar -zxvf batch_load.tar

#. Create a METviewer database:
   
   * create a database to store MET data, which has the prefix 'mv\_', e.g. mv_met_data:
   
   .. code-block:: none
        
        $ cd [install]/metviewer
        $ mysql -u[db_username] -p[db_password] -e'create database [db_name];'
        $ mysql -u[db_username] -p[db_password] [db_name] < sql/mv_mysql.sql

   * create an XML load specification document which contains information about the MET data
   
   * run the METviewer load tool using the XML load specification as input (called [load_xml] here) and monitor progress:

   .. code-block:: none
        
        $ cd [install]/metviewer
        $ bin/mv_load.sh [load_xml] [optional redirection of output, e.g. &> log/load_[date].log &]'

#. Install test directory (for development, optional):
   
   * check out test_data (.../apps/verif/metviewer_test_data/test_data/) from CVS and move test_data directory to /d3/projects/METViewer/:
   
   * create links to R script and sql files

   .. code-block:: none
        
        $ cd /d3/projects/METViewer/test_data
        $ ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/R_tmpl R_tmpl
        $ mkdir R_work
        $ cd R_work
        $ mkdir data
        $ mkdir plots
        $ mkdir scripts
        $ ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/R_work/include/ include
        $ cd /d3/projects/METViewer/test_data/load_data/load
        $ ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/sql/mv_mysql.sql mv_mysql.sql

Making a Database Accessible in the METviewer Web Application
-------------------------------------------------------------
To make a new database accessible in the METviewer Web Application click on "Reload list of databases" button in the upper right corner of the main JSP page. The list of available databases should be updated and a new database should be in it

