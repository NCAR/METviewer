Common XML Structures
=====================

The database loading module and batch plotting module both use XML input files. Some XML structures can be used in either input file. These common structures are documented below.

| **<connection>:** Information about the database connection.
|
|       **<host>:** Database hostname and port number in [host]:[port] format.
|       **<database>:** Database name(s) as CSV.
|       **<user>:** Database user.
|       **<password>:** Database user's password.
|       **<management_system>:** Database type. Can be mysql or mariadb.
|

| **<date_list>:** A structure that specifies a list of date strings in a certain format.
|
|       **<start>:** The start date and time of the date list, specified in the format given by the tag **<format>**
|       **<end>:** The end date and time, specified in the format given by the tag **<format>**
|       **<inc>:** The increment, in seconds, between successive members of the date list.       
|       **<format>:** The date format, specified by the java class SimpleDateFormat.
|

| **<date_offset>:** A structure that specifies a date using an offset from another date.
|
|      **<day_offset>:** The number of days to offset (negative means back in time).
|      **<hour>:** The hour of day in UTC.
|

