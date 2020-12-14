METviewer Web Service
=====================

The METviewer web service is a web-server driven API that parses XML requests and returns results in an XML structure. All calls are session-less meaning that each individual call does not affect and is not affected by the results of any other call. Also, a client does not have to establish a connection with the server and all calls can be made automatically.

The web service can be accessed using the relative URL metviewer/servlet, for example http://www.dtcenter.org/met/metviewer/servlet. When the web service receives an HTTP GET request, it echoes the GET parameters and acts as a "ping" mechanism to ensure that the system is online and working. All API XML requests should be issued using an HTML POST request with the XML request as the entire body. The XML response will indicate the type of request, and echo some information.

There are several different request types, each with a particular format of response. The METviewer client calls the different API methods in a loose ordering:

#. List available databases
   
#. List forecast variables for user selected database

#. List stats for user selected database and forecast variable

#. List values for user selected fixed fields

#. Repeat steps 2-4 as needed
   
#. Plot request for user selected database and plot information

API Request/Response Formats
----------------------------

The following sections each describe a web service API function in terms of the request/response XML structure.

List Databases
~~~~~~~~~~~~~~

List the METviewer databases that are available on the system for access through the web app. The returned list reflects the contents of the mvservlet.properties file in the web app installation folder.

Request:

.. code-block:: XML

        <request><list_db/></request>

Response:

.. code-block:: XML

        <list_db>
          <val>mv_gfs_nam</val>
          <val>mv_hmt_2010</val>
          <val>mv_hmt_2011</val>
          <val>mv_hwt_2010</val>
          <val>mv_met_ncep</val>
        </list_db>

List Values
~~~~~~~~~~~
          
The list_val request is used to conditionally list the distinct values in the stat_header table of the selected database. The stat_field element controls which field's values should be listed. There are two conditions that can be optionally included in the request: a fcst_var/stat pair and a stat_header field name with a set of values. At this point, it is worth noting that although the fields fcst_lead, fcst_valid_beg and fcst_lead_beg are not in the stat_header table of the database schema, they are considered stat_header fields from the standpoint of the METviewer servlet and client. The virtual fields inithour and validhour are considered stat_header fields.

The id element is common to the list_val and list_stat API methods, and the value specified in the request is simply echoed back to the client in the response. This feature helps the client determine which controls should be updated with the contents of the response.

**Request #1:** In this case, the request is for all distinct values of the stat_header field fcst_var, without any conditions.

.. code-block:: XML
        
        <request>
          <db_con>mv_gfs_nam</db_con>
          <list_val>
            <id>0</id>
            <stat_field>FCST_VAR</stat_field>
          </list_val>
        </request>

**Response #1**

.. code-block:: XML

        <list_val>
          <id>0</id>
          <val>APCP_03</val>
          <val>APCP_24</val>
        </list_val>
                   

**Request #2:** This conditional request is for all distinct values of vx_mask for which the fcst_var is APCP_03 and the statistic type is categorical (the statistic CSI can be found in the database table line_data_cts). An additional stat_header field condition stipulates that only vx_mask values whose fcst_lead is 120000 should be returned.

.. code-block:: XML

        <request>
          <db_con>mv_gfs_nam</db_con>
          <list_val>
            <id>3</id>
            <stat_field>VX_MASK</stat_field>
            <stat>
              <fcst_var name="APCP_03">
                <val>CSI</val>
              </fcst_var>
            </stat>
            <field name="OBTYPE">
              <val>MC_PCP</val>
            </field>
          </list_val>
        </request>

**Response #2:** The response contains only values of vx_mask for which the criteria are satisfied. This feature is useful to users for narrowing the results of subsequent search requests. Note that, for performance reasons, time criteria such as fcst_valid_beg, fcst_init_beg and fcst_lead does not affect the results of non-time based fields and vice versa.

.. code-block:: XML

        <list_val>
          <id>3</id>
          <val>FULL</val>
          <val>CONUS</val>
          <val>EAST</val>
        </list_val>

                  
List Statistics
~~~~~~~~~~~~~~~

The list_stat request builds a list of all statistics available in the METviewer database for the specified fcst_var. It is primarily used to construct the dep1 and dep2 portions of the plot request. Conditional criteria is not supported in list_stat requests.

**Request**

.. code-block:: XML
        
        <request>
          <db_con>mv_gfs_nam</db_con>
          <list_stat>
            <id>0</id>
            <stat_fcst_var>APCP_03</stat_fcst_var>
          </list_stat>
        </request>

**Response (abridged)**

.. code-block:: XML

        <list_stat>
          <id>0</id>
          <val>ACC</val>
          <val>BASER</val>
          <val>CSI</val>
          <val>FAR</val>
          <val>GSS</val>
        </list_stat>


Generate Plot
~~~~~~~~~~~~~

Once a user has provided all the information for a complete plot specification, the plot request handles the parsing of the plot specification and generation of the plot. If a plot is successfully created from the plot specification, the location of the plot image is included in the response. If any error or warning information was captured from R during the process, it will be included in the r_error element.

**Request**

.. code-block:: XML
        
        <request>
          <db_con>mv_gfs_nam</db_con>
          <plot>??(see body of plot element) link to Chapter**
          </plot>
        </request>

**Response**

.. code-block:: XML
        
        <plot>plot_00155_20111019_122758</plot>
        <r_error>NAs produced by integer overflow</r_error>


Open app with predefine database
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Use this url to open METviewer main page with predefine database:

http://www.dtcenter.org/met/metviewer/metviewer1.jsp?db=name_of_database

