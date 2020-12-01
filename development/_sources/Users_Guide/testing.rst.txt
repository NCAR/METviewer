Testing Module
==============

Testing example - install and run on dakota
-------------------------------------------

Testing HOME directory: /d3/projects/METViewer/auto_test
Testing data directory: /d3/projects/METViewer/test_data/load_data/load
Branch to verify against: mv_2_5_dev
Branch to verify : mv_2_6

#. Check out and copy to the HOME script : auto_test.sh
   
#. Create a database for the branch to verify against (mv_2_5_dev):

   .. code-block:: none
   
        mysql -uUSER -p -e 'create database mv_test_mv_2_5_dev'

#. Clone and build the branch to verify against (mv_2_5_dev):

   .. code-block:: none
   
        ./auto_test.sh -UGIT_USER -t/d3/projects/METViewer/auto_test/METViewerTest -bmv_2_5_dev -Bmv_2_5_dev -l/d3/projects/METViewer/test_data/load_data/load -dmv_test_2_5_dev -uUSER -m/d3/projects/METViewer/auto_test/METViewer -pUSER -hHOST -P3306

#. Create a database for the branch to verify (mv_2_6):

   .. code-block:: none
   
        mysql -uUSER -p -e 'create database mv_test_mv_2_6'

#. Clone, build and verify the branch:

   .. code-block:: none
   
        ./auto_test.sh -UGIT_USER -t/d3/projects/METViewer/auto_test/METViewerTest -bmv_2_6 -Bmv_2_5_dev -l/d3/projects/METViewer/test_data/load_data/load -dmv_test_2_6 -uUSER -m/d3/projects/METViewer/auto_test/METViewer -pUSER -hHOST -P3306

If the testing is done using crontab the gituser credentials should be stored in the git store - see https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage. Also, auto_test.sh should be modified to send emails with testing results.

Testing - capture
-----------------

The testing module is used to execute the regression testing of a specified version of METviewer. The capture tool is invoked by using the mv_test script. During test capture, system or processing errors will be reported but images and data are not verified. Verification of data or images is performed by the mv_compare script. The mv_test script essentially generates output files and copies them from the output directory to the appropriate directory test_cases directory.

Capture and Verification are performed against a specified METviewer branch and tag. It is expected that the proper branch or tag has been previously fetched or cloned and checked out into the METviewer home directory that is specified with -m (the auto test script does check out specific code and then runs test capture and compare - see below). The METviewer test directory should also be a subdirectory that corresponds to the proper branch and tag, with an output directory that will contain the generated images, scripts and data for a given test run, along with the corresponding test_data (use case xml files) and load data (load specification xml files). The actual load data files are not yet source controlled and so they are linked from the path to the met data that is specified with the -l option. The auto_test script takes branch and tag parameters and creates the proper test subdirectory and reconciles properly with the remote repository. All the mv_test script needs is the proper test subdirectory. Successfully generated output images will be copied to the appropriate test_cases directory.

**NOTE ABOUT GIT TAGS**

A short discussion of METviewer git tags is appropriate here. Git tags are not directly related to any git branches. They are different things. A git tag simply refers to a specific commit to the git repository. The branches that existed at the time of the tagging could all be deleted (except for the master) and the tag would still be a valid tag. For our purposes we name tags after our branches, i.e. MV_2_6_tagname or something like that. This creates an mnemonic relationship to a branch. Furthermore we only create tags after a commit to a specific branch that we then reference in the tag name. That tagged commit is a special point in the history of the repository, a point that we wanted to capture with a name on behalf of a branch. Specifying the tag and branch here allows us to set the repository pointer to the specific commit that we wanted to capture for a given branch. Including a branch name is only a mnemonic that reminds us which branch we were setting the tag on behalf of. The mnemonic allows us to organize our tests for regression checking against special times in the repository commit history.

**NOTE** that since we want to do binary comparisons on captured test data the test data is saved in a directory structure that is mnemonically tied to the product branches or tags. The METviewer code under test is only ever checked out to one specific branch HEAD, or to a tag point, at a time. Its produced data and images may be compared, however, to data that was captured by a different version or tag point of METviewer code. This test directory structure might look like this, for example...

|        METVIEWER_TEST_DIR/
|            MV_2_5/
|                HEAD/
|                    test_data/
|                        test_cases
|                            diff_grouping
|                                xml plot specification files
|                                expected .png files
|                            ens_ss
|                                xml plot specification files
|                                expected .png files
|                            loading
|                                xml plot specification files
|                                expected .png files
|                            phist
|                                xml plot specification files
|                                expected .png files
|                            plot_afwa_thresh
|                                xml plot specification files
|                                expected .png files
|                            rely
|                                xml plot specification files
|                                expected .png files
|                            rhist
|                                xml plot specification files
|                                expected .png files
|                            series_sum_stat
|                                xml plot specification files
|                                expected .png files
|                    met_data
|                        ens_vsdb
|                            data dirs
|                        noahmp
|                            data dirs
|                        precip_vsdb
|                            data dirs
|                        meso_vsdb
|                            data dirs
|                        grid_stat
|                            data dirs
|                        afwaoc
|                            data dirs
|                        point_stat
|                            data dirs
|                        ensemble_stat
|                            data dirs
|                    load_data
|                        load
|                            mv_mysql.sql
|                            load_test.xml
|                    output
|                        data/
|                            generated data files
|                        plots/
|                            generated .png files
|                        scripts/
|                            generated script files
|                        sql/
|                            generated sql files
|                        xml/
|                            generated xml files
|                    R_work/
|                            generated R scripts                   
|                    R_tmpl (link to METviewer R_tmpl)
|                TAG1/
|                    ...
|                TAG2/
|                    ...
|            MV_2_6/
|                ...
|

Testing - verify
----------------

The mv_compare script accepts a branch and a tag that it uses to identify a test subdirectory and a second "expected" branch and tag that it uses to identify a comparison test directory. If tags are ommited the HEAD is used. The compare script looks for corresponding image files in the corresponding plots directories and does a binary comparison of the corresponding files. Differences will be reported as errors.

auto test
---------

The auto_test script defines the branch, optionally a tag, directories, and database credentials for a version under test and a comparison version. It performs the following steps...

#. Remove any METviewer directory and METviewerTestSource directory.

#. Do a git clone into the METviewer home directory and checkout the corresponding branch or tag.
                                                                     
#. Do a git clone of the METviewer testing repository into the METviewerTestSource directory (this is not the test directory)          

#. If needed create a METVIEWER_TEST_DIR subdirectory for the specified branch and load common data from the METviewerTestSource into it.

#. Run the testing capture program to capture test data for the specified branch, and copy the data to the corresponding test subdirectory.

#. Run the testing verify program to verify the images in the two corresponding test subdirectories
 
auto_test example: 

.. code-block:: none

        ./bin/auto_test.sh -Usomegituser -t/myhomedir/METViewerTest -bmv_2_5_dev -Bmv_2_5_dev -l/myhomedir/METViewerTestData -dmydb -umet_admin -m/myhomedir/METViewer -pdppassword -hdphost -P3306

mv_test example: 

.. code-block:: none

        /bin/sh ./bin/mv_test.sh -t/myhomedir/METViewerTest/mv_2_5_dev/HEAD -m/myhomedir/METViewer -dmydb -umet_admin -pdppassword -hdphost -P3306 -l -c

mv_compare example: 

.. code-block:: none

        /bin/sh ./bin/mv_compare.sh -m /myhomedir/METViewer -t /myhomedir/METViewerTest/mv_2_5_dev/HEAD -c /myhomedir/METViewerTest/mv_2_5_dev/HEAD


**---- Auto Test ----**

Usage: auto_test.sh 
-U <git user> -t<path to METviewer test directory> -b<git branch> -B<compare git branch> -l<path to met data> -d<mv_database> -m<path to METViewer home> [-a address list] [-g<git tag>] [-G<compare git tag>] [-u<mv_user>] [-p<mv_passwd>] [-h<mv_host>] [-P<mv_port>] [-j<path to java executible>]

| Where:
| -U <git user with access to github.com/NCAR/METViewer.git and https://github.com/NCAR/METViewer-test.git>
| -t <path to METviewer test directory>
| -b <git branch>
| -B <compare git branch>
| -l <path to met data> causes the LoadDataTest submodule to be executed, gets met data from specified path
| -d <mv_database>
| -m <path to METviewer home>
| [-a <address list>] commas separated email addresses - default sends output to console
| [-g <git tag>] default is HEAD
| [-G <compare git tag>] default is HEAD
| [-u <mv_user>] default is mvuser
| [-p <mv_passwd>] default is mvuser
| [-h <mv_host>] default is dakota.rap.ucar.edu
| [-P <mv_port>] default is 3306
| [-j <path to an alternate java executible>] default is found in PATH environment
| 

**---- Auto Test Done ----**


**---- Capture ----**

Usage: mv_test.sh -t<path to METviewer test directory> -b<git branch> [-g<git tag>] [-m<path to METviewer home>] [-d<mv_database>] [-u<mv_user>] [-p<mv_passwd>] [-h<mv_host>] [-P<mv_port>] [-j<path to java executible>] [-c] [-n] [-l]

| Where:
| -t <path to METviewer test directory>
| [-m <path to METviewer home>] default is /d3/projects/METviewer/src_dev/apps/METviewer
| [-d <mv_database>] default is mv_test
| [-u <mv_user>] default is mvuser
| [-p <mv_passwd>] default is mvuser
| [-h <mv_host>] default is dakota.rap.ucar.edu
| [-P <mv_port>] default is 3306
| [-j <path to an alternate java executible>] default is found in PATH environment
| [-n] causes no clean - i.e. test created data and plots will remain after the test completes, default is to clean prior to and after running
| [-l] causes the LoadDataTest submodule to be executed, default is to not load data, gets met data from METviewer test directory meta_data
| [-c] Captures created output data and copies it into the test directory. Prior to doing this the compare will fail [-X] Do not compare output automatically - only create images
|

**---- capture Done ----**
                         
**---- Verify ----**

Usage: mv_compare.sh [-b<git branch>] [-g<git tag>] [-B<compare git branch>] [-G<expected git tag>] [-m<path to METviewer home>] [-t<path to METviewer test directory>] [-j<path to java executible>]

| Where:
| -t <path to METviewer test directory>
| -b <git branch>
| -B <compare git branch>
| [-g <git tag>] default is HEAD
| [-G <expected git tag>] default is HEAD
| [-m <path to METviewer home>] default is /d3/projects/METViewer/src_dev/apps/METViewer
| [-j <path to an alternate java executible>] default is found in PATH environment
|

**---- Verify Done ----**
                        
The testing and compare modules produce output files (images, points, XML, data) and compares them with the expected output. In order for tests to pass, produced and expected files should be byte identical.
                        
NOTE: R scripts create visually similar results but bitwise different image files on different platforms.

Testing submodules
------------------
               
**LoadDataTest** recreates and refills mv_test database with MET output data and compares number of rows in each table with the expected number.
Test data and XML configuration file are located in <test_dir>/load_data directory

**CreatePlotBatchTest** runs MVBatch with testing plot specification files and creates output files with the expected output. Any errors encountered with creating plots will be reported. Images are not compared
Plot specification files and expected output are located in <test_dir>/plots_batch/<test_type> directory  

**ComparePlotBatchTest** \compares a test ROOT_DIR with a test COMPARE+DIR
These directories are specified by the testdir and compare dir.   plot specification files and expected output are located in <test_dir>/test_data_test_cases/<test_type> directories.

**TestMVServlet** simulates POST intermediate requests (ex. get list of variables), send them to MVServlet and compare servlet's response with the expected output.
Requests files and expected response are located in <test_dir>/servlet/ directory

**CreatePlotServletTest** simulates POST create a plot requests , send them to MVServlet and compare produces output files with the expected output.
Requests files and expected output files are located in <test_dir>/plots_web/<test_type> directory

Location of <test_dir> : /d3/projects/METViewer/test_data/
