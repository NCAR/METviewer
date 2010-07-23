
var _boolIE = false;
var _boolDim = false;

var _url;

var _strDBCon = "";
var _boolDBStatus = false;

var _intDepIdNext = 1;
var _listDepDiv = new Array();

var _divFieldVal;
var _intFieldValIdNext = 0;

var _listVarStat = ["MODEL", "FCST_LEAD", "FCST_VALID_BEG", "FCST_INIT_BEG", "INIT_HOUR", "FCST_LEV", "OBTYPE", "VX_MASK", "INTERP_MTHD", "INTERP_PNTS", "FCST_THRESH"];
var _listVarMode = ["MODEL", "FCST_LEAD", "FCST_VALID", "FCST_INIT", "INIT_HOUR", "FCST_ACCUM", "FCST_RAD", "FCST_THR", "FCST_INIT", "FCST_LEV"];
var _listVar = _listVarStat;

var _listSeries1Div = new Array();
var _listSeries2Div = new Array();
var _listFixDiv = new Array();

var _listIndyVarStat = ["FCST_LEAD", "FCST_LEV", "FCST_THRESH", "INIT_HOUR"];
var _listIndyVarMode = ["FCST_LEAD", "FCST_LEV", "FCST_THR", "INIT_HOUR"];
var _listIndyVar = _listIndyVarStat;
var _intIndyValIdNext = 0;

var _strPlotData = "stat";

var _strFmtPlotWidth = "275px";
var _intNumFmtPlotCol = 4;
var _intFmtPlotTxtIndex = 0;
var _intFmtPlotBoolIndex = 0;


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Administration/Utility Functions
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * When the page loads, perform the initialization duties including setting pointers to DHTML elements,
 * loading the list of databases and populating the various database field lists.
 */

function onLoad(){
	_url			= window.location + "servlet";

	_boolIE = (-1 != navigator.appName.indexOf("Internet Explorer"));

	/*
	console("browser codeName: " + navigator.appCodeName + "\n" +
			"browser name: " + navigator.appName + "\n" +
			"browser version: " + navigator.appVersion + "\n" +
			"cookies enabled: " + navigator.cookieEnabled + "\n" +
			"platform: " + navigator.platform + "\n" +
			"user-agent header: " + navigator.userAgent + "\n" +
			"IE: " + (_boolIE? "true" : "false") + "\n\n");
	*/
	console("load() - IE: " + (_boolIE? "true" : "false") + "\n\n");

	window.onscroll = function(e){
		if( _boolDim ){ dimScreen(true); }
	}

	//  initialize the database list and controls
	listDBReq();

	//  initialize the dep list
	_listDepDiv.push( document.getElementById("divDep0") );

	//  initialize the series lists
	_divFieldVal = document.getElementById("divFieldVal");
	fillSelect(document.getElementById("selField"), _listVar);
	addSeries1Div();
	document.getElementById("lnkRemoveFieldVal0").style.display = "none";

	//  initialize the independent variable controls
	fillSelect(document.getElementById("selIndyVar"), _listIndyVar);

	//  initialize the plot format controls
	for(var i=1; i < _intNumFmtPlotCol; i++){
		var tdFmtPlotBool = document.getElementById("trFmtPlotBool").insertCell(i);
		tdFmtPlotBool.align = "right";
		tdFmtPlotBool.style.width = _strFmtPlotWidth;
		tdFmtPlotBool.appendChild( document.createTextNode("\u00a0") );
	}
	for(var i=1; i < _intNumFmtPlotCol; i++){
		var tdFmtPlotTxt = document.getElementById("trFmtPlotTxt").insertCell(i);
		tdFmtPlotTxt.align="right";
		tdFmtPlotTxt.style.width = _strFmtPlotWidth;
		tdFmtPlotTxt.appendChild( document.createTextNode("\u00a0") );
	}

	//  add the boolean formatting option controls
	addFmtPlot("event_equal",	"false",	"bool");
	addFmtPlot("plot1_diff",	"false",	"bool");
	addFmtPlot("plot2_diff",	"false",	"bool");
	addFmtPlot("num_stats",		"false",	"bool");
	addFmtPlot("indy1_stag",	"false",	"bool");
	addFmtPlot("indy2_stag",	"false",	"bool");
	addFmtPlot("grid_on",		"true",		"bool");
	addFmtPlot("sync_axes",		"false",	"bool");
	addFmtPlot("dump_points1",	"false",	"bool");
	addFmtPlot("dump_points2",	"false",	"bool");
	addFmtPlot("log_y1",		"false",	"bool");
	addFmtPlot("log_y2",		"false",	"bool");

	//  add onchange listeners to the plot_diff controls
	var trFmtPlot = document.getElementById("trFmtPlotBool");
	var selBool1 = trFmtPlot.cells[1].getElementsByTagName("select")[0];
	selBool1.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selBool1.attachEvent("onchange", new Function("buildSeriesDiv()")); }	
	var selBool2 = trFmtPlot.cells[2].getElementsByTagName("select")[0];
	selBool2.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selBool2.attachEvent("onchange", new Function("buildSeriesDiv()")); }	

	//  add the text formatting options
	addFmtPlot("plot_type",		"png256",	"txt");
	addFmtPlot("plot_height",	"8.5",		"txt");
	addFmtPlot("plot_width",	"11",		"txt");
	addFmtPlot("plot_res",		"72",		"txt");
	addFmtPlot("plot_units",	"in",		"txt");
	addFmtPlot("mar",			"c(8, 4, 5, 4)", "txt");
	addFmtPlot("mgp",			"c(1, 1, 0)", "txt");
	addFmtPlot("cex",			"1",		"txt");
	addFmtPlot("title_weight",	"2",		"txt");
	addFmtPlot("title_size",	"1.4",		"txt");
	addFmtPlot("title_offset",	"-.4",		"txt");
	addFmtPlot("title_align",	".5",		"txt");
	addFmtPlot("xtlab_orient",	"1",		"txt");
	addFmtPlot("xtlab_perp",	"-.75",		"txt");
	addFmtPlot("xtlab_horiz",	".5",		"txt");
	addFmtPlot("xlab_weight",	"1",		"txt");
	addFmtPlot("xlab_size",		"1",		"txt");
	addFmtPlot("xlab_offset",	"2",		"txt");
	addFmtPlot("xlab_align",	".5",		"txt");
	addFmtPlot("ytlab_orient",	"1",		"txt");
	addFmtPlot("ytlab_perp",	".5",		"txt");
	addFmtPlot("ytlab_horiz",	".5",		"txt");
	addFmtPlot("ylab_weight",	"1",		"txt");
	addFmtPlot("ylab_size",		"1",		"txt");
	addFmtPlot("ylab_offset",	"-2",		"txt");
	addFmtPlot("ylab_align",	".5",		"txt");
	addFmtPlot("grid_lty",		"3",		"txt");
	addFmtPlot("grid_col",		"#CCCCCC",	"txt");
	addFmtPlot("grid_lwd",		"1",		"txt");
	addFmtPlot("grid_x",		"listX",	"txt");
	addFmtPlot("x2tlab_orient",	"1",		"txt");
	addFmtPlot("x2tlab_perp",	"1",		"txt");
	addFmtPlot("x2tlab_horiz",	".5",		"txt");
	addFmtPlot("x2lab_weight",	"1",		"txt");
	addFmtPlot("x2lab_size",	".8",		"txt");
	addFmtPlot("x2lab_offset",	"-.5",		"txt");
	addFmtPlot("x2lab_align",	".5",		"txt");
	addFmtPlot("y2tlab_orient",	"1",		"txt");
	addFmtPlot("y2tlab_perp",	".5",		"txt");
	addFmtPlot("y2tlab_horiz",	".5",		"txt");
	addFmtPlot("y2lab_weight",	"1",		"txt");
	addFmtPlot("y2lab_size",	"1",		"txt");
	addFmtPlot("y2lab_offset",	"1",		"txt");
	addFmtPlot("y2lab_align",	".5",		"txt");
	addFmtPlot("legend_size",	".8",		"txt");
	addFmtPlot("legend_box",	"o",		"txt");
	addFmtPlot("legend_inset",	"c(0, -.25)", "txt");
	addFmtPlot("legend_ncol",	"3",		"txt");
	addFmtPlot("box_boxwex",	"1",		"txt");
	addFmtPlot("box_notch",		"FALSE",	"txt");
	addFmtPlot("ci_alpha",		".05", 		"txt");

	//  build the series formatting controls
	buildSeriesDiv();

	//  update the bootstrapping controls
	updateBoot();
}

function parse(){
	var strData = document.getElementById("txtData").value;
	var strPattern = document.getElementById("txtPattern").value;

	console("parse()\n  data: " + strData + "\n  pattern: " + strPattern + "\n");

	var pat = new RegExp(strPattern);
	var listParse = strData.match( pat );
	if( null != listParse ){
		console("  match:\n");
		for(i in listParse){ console("    listParse[" + i + "]: " + listParse[i] + "\n"); }
	} else {
		console("  no match\n");
	}

	console("parse() complete\n\n");
}


/**
 * Wrapper for appending information to the console text box
 */
function console(str){ document.getElementById("txtConsole").value += str; }
function consoleClear(){ document.getElementById("txtConsole").value = ""; }

/**
 * Dims the screen while the web app is waiting for the servlet
 */
function dimScreen(boolDim){

	//  adjust the size and location of the dim screen elements
	var divLoading = document.getElementById("divLoading");
	var divDimScreen = document.getElementById("divDimScreen");
	if( !_boolIE ){
	    divLoading.style.left = (window.innerWidth / 2) - 60 + window.pageXOffset;
	    divLoading.style.top = (window.innerHeight / 2) - 40 + window.pageYOffset;
	    divDimScreen.style.left = window.pageXOffset;
	    divDimScreen.style.top = window.pageYOffset;
	    divDimScreen.style.width = window.innerWidth;
	    divDimScreen.style.heigth = window.innerHeight;
	} else {
		divLoading.style.left = (document.body.clientWidth / 2) - 60 + document.body.scrollLeft;
		divLoading.style.top = (document.body.clientHeight / 2) - 40 + document.body.scrollTop;
	    divDimScreen.style.left = document.body.scrollLeft;
	    divDimScreen.style.top = document.body.scrollTop;
	    divDimScreen.style.width = document.body.clientWidth;
	    divDimScreen.style.heigth = document.body.clientHeight;
	}

	//  display the dim screen controls
	_boolDim = boolDim;
	document.title = "METViewer" + (boolDim? " - Loading..." : "");
	document.getElementById("divDimScreen").style.display = (boolDim? "block" : "none");
	document.getElementById("divLoading").style.display = (boolDim? "block" : "none");
}

/**
 * Debugging facility which dumps the DHTML for the input element into the console
 */
function serialize(strId){
	var doc = document.getElementById(strId);
	var strXML = "";
	try     { strXML = XML( (new XMLSerializer()).serializeToString(doc) ).toXMLString(); }
	catch(e){ strXML = doc.outerHTML; }
	console("\n" + strXML + "\n\n");
}

/**
 * Button handlers that call the server to clear the <list_val>  and <list_stat> caches
 */
function listValClearCacheReq(){ sendRequest("POST", "<list_val_clear_cache/>", nullResp); }
function listStatClearCacheReq(){ sendRequest("POST", "<list_stat_clear_cache/>", nullResp); }

/**
 * Clear and populate a select list with the specified id with the items in the specified list
 */
function fillSelect(sel, listOpt){
	while( 0 < sel.length ){ sel.remove(sel.length - 1); }
	for(i in listOpt){
		var opt = document.createElement("option");
		opt.text = listOpt[i];
		try{ sel.add(opt, null); }catch(ex){ sel.add(opt); }
	}
}

/**
 * Build a list of the selected items in the specified select control
 */
function getSelected(sel){
	var listRet = new Array();
	for(var i=0; i < sel.options.length; i++){
		if( sel.options[i].selected ){ listRet.push( sel.options[i].text ); }
	}
	return listRet;
}

/**
 * Search the specified div list for the member with the specified id and return its index.  The div id
 *  is determined from the value of the div input with the specified index.
 */
function findDivId(listDiv, intDivId, intInputIndex){
 	var intIndex = -1;
 	for(i in listDiv){
 		var intIdCur = listDiv[i].getElementsByTagName("input")[intInputIndex].value;
 		if( intDivId == intIdCur ){ intIndex = i; }
 	}
 	return intIndex;
}

/**
 * A simple data structure for storing the components of a parsed <list_val> reponse
 */
function ListValResp(id, vals){
	this.id = id;
	this.vals = vals;
}

/**
 * Parse the specified <list_val> server response XML and return the results in a listVal object
 */
function parseListValResp(strResp, strType){
	var listProc = strResp.match( new RegExp("<list_" + strType + ">(?:<id>(\\d+)<\/id>)?<val>(.*)<\/val><\/list_" + strType + ">") );
	if( null == listProc ){
		console("parseListValResp() - ERROR: could not parse response: " + strResp + "\n\n");
		return null;
	}
	return new ListValResp(listProc[1], listProc[2].split( /<\/val><val>/ ));
}

/**
 * Print an array of string values to the console, for debugging
 */
function printList(listVal){ for(var i=0; i < listVal.length; i++){ console("    listVal[" + i + "]: " + listVal[i] + "\n"); } }


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  AJAX Functions
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a request object in a browser-dependent manner
 */
function getRequest(){
	var req;
	if (window.XMLHttpRequest) { req = new XMLHttpRequest(); }
	else if (window.ActiveXObject) {
		try{ req = new ActiveXObject("Microsoft.XMLHTTP"); }
		catch (e) { try { req = new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {} }
	}
	return req;
}

/**
 * Send an XML request to the server asynchronously, and call processResponse() when the response arrives
 */
function sendRequest(reqType, reqData, fnResp){

	dimScreen(true);

	//  add the database connection to the request, if appropriate
	if( null == reqData.match( /<list_db\/>/ ) ){ reqData = "<db_con>" + _strDBCon + "</db_con>" + reqData; }
	console("sendRequest() - request: " + reqData + "\n");

	//  set the request to wait until the data is ready
	var strResp = "";
	var req = getRequest();
	req.onreadystatechange =
    function() {
		if (req.readyState == 4) {

			//  classify the response type
			if (req.status == 200) { strResp = req.responseText; }
			else                   { strResp = "<error>req.status " + req.status + "</error>"; }

			//  dispatch the response
			console("sendRequest() - response: " + strResp + "\n\n");
			var listParse = strResp.match( /<error>(.*)<\/error>/ );
			if( null != listParse ){ alert("METViewer error: " + listParse[1]); }
			else                   { fnResp(strResp);                           }
			dimScreen(false);
        }
	};

	var reqURL = _url + "?date=" + (new Date()).getMilliseconds();

	//  send the request in the manner specified
    if( reqType == "GET" ){
		req.open(reqType, reqURL + "&data=" + encodeURI(reqData), true);
		req.send(null);
	} else {
		var reqPostData = "<request>" + reqData + "</request>";
		req.open(reqType, reqURL, true);
		req.setRequestHeader("Content-Type", "text/xml");
		req.setRequestHeader("Content-length", reqPostData.length);
		req.setRequestHeader("Connection", "close");
		req.send(reqPostData);
	}
}

/**
 * A response handler that does nothing
 */
function nullResp(strResp){}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Database Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Test functions related to the GET and POST test controls
 */
function testPostReq()    { sendRequest("POST", document.getElementById("txtPost").value, nullResp); }
function testGetReq()     { sendRequest("GET", document.getElementById("txtGet").value, nullResp);   }

/**
 * Request and process the list of databases, populating the database list select control
 */
function listDBReq(){ sendRequest("POST", "<list_db/>", listDBResp); }
function listDBResp(strResp){
	var resp = parseListValResp(strResp, "db");
	fillSelect(document.getElementById("selDB"), resp.vals);
	updateDBCon();
}

/**
 * When the selected database is changed, update the data members and controls
 */
function updateDBCon(){
	var selDB = document.getElementById("selDB");
	_strDBCon = selDB.options[selDB.selectedIndex].text;
	console("updateDBCon() - _strDBCon: " + _strDBCon + "\n\n");

	//  populate the dep list of fcst_var
	listFcstVarReq(0);
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Dependent Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a GUI control cluster for specifying a dependent variable fcst_var and stat(s)
 */
function addDep(){

 	//  clone the dependent variable controls
 	var intDepId = _intDepIdNext++;
 	var divDep = _listDepDiv[0].cloneNode(true);

 	//  update the components of the cloned fixed value controls
 	divDep.id = "divDep" + intDepId;
 	divDep.getElementsByTagName("select")[0].id = "selFcstVar" + intDepId;
 	var btnFcstVar = divDep.getElementsByTagName("input")[0];
 	btnFcstVar.setAttribute("onclick", "javascript:selectFcstVarReq(" + intDepId + ")");
 	if( _boolIE ){ btnFcstVar.attachEvent("onclick", new Function("selectFcstVarReq(" + intDepId + ")")); }
 	var radAxis1 = divDep.getElementsByTagName("input")[1]; 
	radAxis1.name = "radAxis" + intDepId;
 	radAxis1.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ radAxis1.attachEvent("onchange", new Function("buildSeriesDiv()")); }
 	var radAxis2 = divDep.getElementsByTagName("input")[2]; 
 	radAxis2.name = "radAxis" + intDepId;
 	radAxis2.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ radAxis2.attachEvent("onchange", new Function("buildSeriesDiv()")); }
 	var selStat = divDep.getElementsByTagName("select")[1];
 	while( 0 < selStat.length ){ selStat.remove(selStat.length - 1); }
 	selStat.style.display = "none";
 	selStat.id = "selStat" + intDepId;
 	selStat.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selStat.attachEvent("onchange", new Function("buildSeriesDiv()")); }
 	var lblStat = divDep.getElementsByTagName("span")[0];
 	lblStat.style.display = "none";
 	lblStat.id = "lblStat" + intDepId;
 	var lnkDep = divDep.getElementsByTagName("a")[0];
 	lnkDep.setAttribute("onclick", "javascript:removeDepVar(" + intDepId + ")");
 	if( _boolIE ){ lnkDep.attachEvent("onclick", new Function("removeDepVar(" + intDepId + ")")); }
 	divDep.getElementsByTagName("span")[1].style.display = "inline";
 	divDep.getElementsByTagName("input")[3].value = "" + intDepId;

 	//  add the new fixed variable value section to the page
 	_listDepDiv.push(divDep);
 	document.getElementById("divDep").insertBefore(divDep, document.getElementById("imgDep"));

 	//  ensure the first remove link is visible
	_listDepDiv[0].getElementsByTagName("span")[1].style.display = "inline";
}

/**
 * Remove the specified dep div from the list of dependent variable controls
 */
function removeDepVar(intDepId){
	removeFieldValDiv(intDepId, _listDepDiv, 3);
 	if( 1 == _listDepDiv.length ){ _listDepDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
}

/**
 * List the fcst_var database field values, and populate the dependent variable fcst_var select with the results
 */
function listFcstVarReq(intDepId){
	sendRequest("POST", "<list_val><id>" + intDepId + "</id><" + _strPlotData + "_field>FCST_VAR</" + _strPlotData + "_field>" + "</list_val>", listFcstVarResp);
}
function listFcstVarResp(strResp){ selectFieldResp(strResp, _listDepDiv, 3, 0); }

/**
 * List the statistics available for the specified forecast variable and populate the statistics select
 * with the results
 */
function selectFcstVarReq(intId){
	var selFcstVar = document.getElementById("selFcstVar" + intId);
	sendRequest("POST",
				"<list_stat><id>" + intId + "</id><" + _strPlotData + "_fcst_var>" + selFcstVar.options[selFcstVar.selectedIndex].text +
					"</" + _strPlotData + "_fcst_var></list_stat>",
				selectFcstVarResp);
}
function selectFcstVarResp(strResp){

	//  parse the response
	var resp = parseListValResp(strResp, "stat");
	if( null == resp ){ return; }

	//  populate and display the stats select control
	var selFcstVar = document.getElementById("selStat" + resp.id);
	fillSelect(selFcstVar, resp.vals);
	selFcstVar.style.display = "inline";
	document.getElementById("lblStat" + resp.id).style.display = "inline";
}

/**
 * Build an XML criteria string for a <list_val> command which contains the list of currently selected
 * dependent variable fcst_var values
 */
function buildFcstVarCrit(){
	var strFixCrit = "<field name=\"FCST_VAR\">";
	for(i in _listDepDiv){
		var selFcstVar = _listDepDiv[i].getElementsByTagName("select")[0];
		strFixCrit += "<val>" + selFcstVar.options[selFcstVar.selectedIndex].text + "</val>";
	}
	strFixCrit += "</field>";
	return strFixCrit;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Field Value Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a field val div of the specified category and add it to the from and to the input controls
 * list
 */
function addFieldValDiv(category, listDiv){

	//  use the next field val id
	var intId = _intFieldValIdNext++;

	//  clone the field value div and update the controls
	var divFieldVal = _divFieldVal.cloneNode(true);
	divFieldVal.id = "divFieldVal" + intId;
	divFieldVal.getElementsByTagName("select")[0].id = "selFieldVal" + intId;
	var btnFieldVal = divFieldVal.getElementsByTagName("input")[0];
	btnFieldVal.setAttribute("onclick", "javascript:select" + category + "VarReq(" + intId + ")");
	if( _boolIE ){ btnFieldVal.attachEvent("onclick", new Function("select" + category + "VarReq(" + intId + ")")); }
	var selVal = divFieldVal.getElementsByTagName("select")[1];
	while( 0 < selVal.length ){ selVal.remove(selVal.length - 1); }
	selVal.style.display = "none";
	selVal.id = "selVal" + intId;
	divFieldVal.getElementsByTagName("span")[1].id = "lnkRemoveFieldVal" + intId;
	var lnkRemove = divFieldVal.getElementsByTagName("a")[0];
	lnkRemove.setAttribute("onclick", "javascript:remove" + category + "Div(" + intId + ")");
	if( _boolIE ){ lnkRemove.attachEvent("onclick", new Function("remove" + category + "Div(" + intId + ")")); }
	divFieldVal.getElementsByTagName("input")[1].value = "" + intId;
	divFieldVal.style.display = "inline";

	//  add the new div to the input controls list and add it to the form
	listDiv.push( divFieldVal );
	document.getElementById("div" + category).insertBefore(divFieldVal, document.getElementById("img" + category));

}

/**
 * Remove the field val div with the specified id from the from and the controls list.  The index of
 * the hidden id field is specified by intInputId.
 */
function removeFieldValDiv(intId, listDiv, intInputId){

	//  attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, intInputId);
	if( 0 > intIndex ){
		console("removeFieldValDiv() - WARNING: div " + intId + " not found\n\n");
		return;
	}

 	//  remove the specified div from the list and hide it
 	var divFieldVal =  listDiv[intIndex];
 	listDiv.splice(intIndex, 1);
 	divFieldVal.style.display = "none";

}

/**
 * Build a list_val server request for the field val with the specified id in the specified controls
 * list.  The request includes the fixed value criteria up to the specified index.  The response xml
 * is passed to the specified response function.
 */
function selectFieldReq(intId, listDiv, intFixEnd, fnResp){

	//  attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, 1);
	if( 0 > intIndex ){
		console("selectFieldReq() - ERROR: div " + intId + " not found\n\n");
		return;
	}

	//  build a list_val request for the selected field
	var strFcstVarCrit = buildFcstVarCrit();
	var strFixCrit = buildFixCrit(intFixEnd);
	var selField = listDiv[intIndex].getElementsByTagName("select")[0];
	var strField = selField.options[selField.selectedIndex].text;
	sendRequest("POST",
				"<list_val><id>" + intId + "</id><" + _strPlotData + "_field>" + strField + "</" + _strPlotData + "_field>" +
				strFcstVarCrit + strFixCrit + "</list_val>",
				fnResp);
}

/**
 * Handle the specified <list_val> response XML (strResp), populating the div from the input list with the
 * id contained in the response <id>.  The div id is determined by examining the hidden field with the
 * specified index (intIdIndex).  The select control of the specified index (intSelIndex) will be populated.
 */
function selectFieldResp(strResp, listDiv, intIdIndex, intSelIndex){

	//  parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	//  retrieve and validate the div from the input div list
	var intIndex = findDivId(listDiv, resp.id, intIdIndex);
	if( 0 > intIndex ){
		console("selectFieldResp() - ERROR: index for div id " + resp.id + " not found\n\n");
		return;
	}

	//  add the field values to the value select list
	var selVal = listDiv[intIndex].getElementsByTagName("select")[intSelIndex];
	selVal.style.display = "inline";
	fillSelect(selVal, resp.vals);

}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Series Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function addSeries1Div(){
	addFieldValDiv("Series1", _listSeries1Div);
	var selVal = _listSeries1Div[ _listSeries1Div.length - 1 ].getElementsByTagName("select")[1];
	selVal.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selVal.attachEvent("onchange", new Function("buildSeriesDiv()")); }
	for(i in _listSeries1Div){ _listSeries1Div[i].getElementsByTagName("span")[1].style.display = "inline"; }
}
function removeSeries1Div(intId){
	removeFieldValDiv(intId, _listSeries1Div, 1);
 	if( 1 == _listSeries1Div.length ){ _listSeries1Div[0].getElementsByTagName("span")[1].style.display = "none"; }
}
function selectSeries1VarReq(intId){ selectFieldReq(intId, _listSeries1Div, _listFixDiv.length - 1, selectSeries1VarResp); }
function selectSeries1VarResp(strResp){ selectFieldResp(strResp, _listSeries1Div, 1, 1); }

function addSeries2Div(){
	addFieldValDiv("Series2", _listSeries2Div);
	var selVal = _listSeries2Div[ _listSeries2Div.length - 1 ].getElementsByTagName("select")[1];
	selVal.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selVal.attachEvent("onchange", new Function("buildSeriesDiv()")); }
	for(i in _listSeries2Div){ _listSeries2Div[i].getElementsByTagName("span")[1].style.display = "inline"; }
}
function removeSeries2Div(intId){
	removeFieldValDiv(intId, _listSeries2Div, 1);
 	if( 1 == _listSeries2Div.length ){ _listSeries2Div[0].getElementsByTagName("span")[1].style.display = "none"; }
}
function selectSeries2VarReq(intId){ selectFieldReq(intId, _listSeries2Div, _listFixDiv.length - 1, selectSeries2VarResp); }
function selectSeries2VarResp(strResp){ selectFieldResp(strResp, _listSeries2Div, 1, 1); }


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Fixed Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add, remove and populate the fixed variable controls
 */
function addFixVar()         { addFieldValDiv("Fix", _listFixDiv);       }
function removeFixDiv(intId) { removeFieldValDiv(intId, _listFixDiv, 1); }
function selectFixVarReq(intId){
	var intIndexCrit = findDivId(_listFixDiv, intId, 1);
	if( 0 > intIndexCrit ){
		console("selectFixVarReq() - ERROR: index for id " + intId + " not found\n");
		return;
	}
	selectFieldReq(intId, _listFixDiv, intIndexCrit - 1, selectFixVarResp);
}
function selectFixVarResp(strResp){ selectFieldResp(strResp, _listFixDiv, 1, 1); }

/**
 * Construct a string of database field and value criteria that reflects the selected fields and values in
 * the fixed values controls
 */
function buildFixCrit(endIndex){
	var strFixCrit = "";
	for(i=0; i <= endIndex; i++){
		var divFixCrit = _listFixDiv[i];
		var selFixCrit = divFixCrit.getElementsByTagName("select")[0];
		var strFixCritCur = "<field name=\"" + selFixCrit.options[ selFixCrit.selectedIndex ].text + "\">";
		var listFixCritVal = getSelected( divFixCrit.getElementsByTagName("select")[1] );
		for(var j=0; j < listFixCritVal.length; j++){ strFixCritCur += "<val>" + listFixCritVal[j] + "</val>"; }
		strFixCritCur += "</field>";
		if( 0 < listFixCritVal.length ){ strFixCrit += strFixCritCur; }
	}
	return strFixCrit;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Independent Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Build and run a request for values for the currently selected independent variable and populate
 * the independent variable value list with the results
 */
function selectIndyVarReq(){

	//  build a list_val request for the selected independent field
	var strFcstVarCrit = buildFcstVarCrit();
	var strFixCrit = buildFixCrit(_listFixDiv.length - 1);
	var selIndyVar = document.getElementById("selIndyVar");
	var strField = selIndyVar.options[selIndyVar.selectedIndex].text;
	sendRequest("POST",
				"<list_val><id>0</id><" + _strPlotData + "_field>" + strField + "</" + _strPlotData + "_field>" +
					strFcstVarCrit + strFixCrit + "</list_val>",
				selectIndyVarResp);
}
function selectIndyVarResp(strResp){

	//  parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	//  hide all currently display indy val controls
	var tabIndyVal = document.getElementById("tabIndyVal");
	while( 1 < tabIndyVal.rows.length ){ tabIndyVal.deleteRow(tabIndyVal.rows.length - 1); }

	//  add a indy val control group for each indy value
	var divIndy = document.getElementById("divIndy");
	var trIndyVal0 = tabIndyVal.rows[0];
	for( i in resp.vals ){
		var trIndyVal = tabIndyVal.insertRow(tabIndyVal.rows.length);

		var tdIndyChk = trIndyVal.insertCell(0);
		tdIndyChk.appendChild( document.getElementById("spanIndyValChk").cloneNode(true) );
		var tdIndyLab = trIndyVal.insertCell(1);
		tdIndyLab.appendChild( document.getElementById("spanIndyValLab").cloneNode(true) );
		trIndyVal.getElementsByTagName("span")[1].innerHTML = resp.vals[i];
		trIndyVal.style.display = "table-row";
		trIndyVal.getElementsByTagName("input")[1].value = resp.vals[i];
	}
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Plot Formatting Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Construct plot formatting controls with the specified label, value and type.  The currently
 * supported values of type are "txt" and "bool".  The controls are placed at the next available
 * place for their respective type.
 */
function addFmtPlot(label, value, type){

	var boolTypeTxt = (type == "txt");
	var intFmtIndex = (boolTypeTxt? _intFmtPlotTxtIndex : _intFmtPlotBoolIndex);
	var intCol = intFmtIndex % _intNumFmtPlotCol
	var tabFmt = document.getElementById( boolTypeTxt? "tabFmtPlotTxt" : "tabFmtPlotBool" );
	var tdFmt;

	//  if the index is zero, populate the existing cell
	if( 0 == intFmtIndex ){
		tdFmt = tabFmt.rows[0].cells[0];
	}

	//  if the table requires a new row, create one
	else if( 0 == intCol ){
		var trFmt = tabFmt.insertRow( tabFmt.rows.length );
		for(var i=0; i < _intNumFmtPlotCol; i++){
			tdFmt = trFmt.insertCell(i);
			tdFmt.align = "right";
			tdFmt.style.width = _strFmtPlotWidth;
			tdFmt.appendChild( document.createTextNode("\u00a0") );
		}
		tdFmt = trFmt.cells[0];
	}

	//  if a new row is not required, get the next available cell on the current row
	else {
		tdFmt = tabFmt.rows[ tabFmt.rows.length - 1 ].cells[intCol];
	}

	//  populate the controls with the specified input information
	if( 0 != intFmtIndex ){
		tdFmt.appendChild( document.getElementById( boolTypeTxt? "spanFmtPlotTxt" : "spanFmtPlotBool" ).cloneNode(true) );
	}
	tdFmt.getElementsByTagName("span")[1].innerHTML = label + ":";
	if( boolTypeTxt ){
		tdFmt.getElementsByTagName("input")[0].value = value;
		_intFmtPlotTxtIndex++;
	} else {
		tdFmt.getElementsByTagName("select")[0].selectedIndex = ( "true" == value? 0 : 1 );
		_intFmtPlotBoolIndex++;
	}
}

/**
 * Build the list of plot series reflected by the current state of the controls
 */
function buildSeriesDiv(){
console("buildSeriesDiv()\n");
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var spanFmtSeriesNone = document.getElementById("spanFmtSeriesNone");
	var intNumSeries = 0;

	//  clear all existing series, except the first two
	while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }

	//  build permutation of the series values
	var listSeries1Perm = permuteSeries(_listSeries1Div, 0, getPlotDiff(1));
	var listSeries2Perm = permuteSeries(_listSeries2Div, 0, getPlotDiff(2));

	//  for each dep div, consider the fcst_var and selected stats
	for(var i=0; i < _listDepDiv.length; i++){

		//  determine the y axis to which these permutations belong
		var chkY1 = _listDepDiv[i].getElementsByTagName("input")[1];
		var boolY1 = chkY1.checked;
		var listSeriesPerm = (boolY1? listSeries1Perm : listSeries2Perm);

		//  get the dep var information
		var strFcstVar = getSelected( _listDepDiv[i].getElementsByTagName("select")[0] )[0];
		var listStat = getSelected( _listDepDiv[i].getElementsByTagName("select")[1] );

		//  build a series for each combination of fcst_var, stat and series permutation
		for(var j=0; j < listStat.length; j++){
			for(var k=0; k < listSeriesPerm.length; k++){

				//  build the series name
				var strSeriesName =  listSeriesPerm[k] + " " + strFcstVar + " " + listStat[j];

				var trFmtSeries;
				var tdName;

				//  if the series is the first to be built, use the existing controls
				if( 0 == intNumSeries++ ){
					trFmtSeries = tabFmtSeries.rows[0];
					tdName = trFmtSeries.cells[0];
				}

				//  otherwise, build a new set of series formatting controls
				else {
					var trHR = tabFmtSeries.insertRow( tabFmtSeries.rows.length );
					var tdHR = trHR.insertCell(0);
					tdHR.colSpan = "3";
					tdHR.appendChild( document.getElementById("spanFmtSeriesHR").cloneNode(true) );

					trFmtSeries = tabFmtSeries.insertRow( tabFmtSeries.rows.length );

					var tdName = trFmtSeries.insertCell(0);
					tdName.align = "right";
					tdName.style.width = "350px";
					tdName.style.paddingTop = "20px";
					tdName.appendChild( document.getElementById("spanFmtSeriesName").cloneNode(true) );

					var tdFmt1 = trFmtSeries.insertCell(1);
					tdFmt1.align = "right";
					tdFmt1.style.width = "200px";
					tdFmt1.style.paddingTop = "20px";
					tdFmt1.appendChild( document.getElementById("spanFmtSeriesFmt1").cloneNode(true) );

					var tdFmt2 = trFmtSeries.insertCell(2);
					tdFmt2.align = "right";
					tdFmt2.style.width = "275px";
					tdFmt2.style.paddingTop = "20px";
					tdFmt2.appendChild( document.getElementById("spanFmtSeriesFmt2").cloneNode(true) );
				}

				//  populate the controls with the series name
				tdName.getElementsByTagName("span")[2].innerHTML = strSeriesName;
				tdName.getElementsByTagName("span")[3].innerHTML = (boolY1? "Y1" : "Y2") + " Series";
			}
		}
	}
	
	//  show or hide the controls, depending on the number of series
	tabFmtSeries.style.display		= (1 > intNumSeries ? "none" : "inline");
	spanFmtSeriesNone.style.display	= (1 > intNumSeries ? "inline" : "none");
	
console("buildSeriesDiv() complete\n\n");
}

/**
 * Build a list of all series variable combinations for the specified list of series field divs, starting with
 * the div at the specified index (0 for all permutations).  If a difference curve is specified, add it to the
 * series.
 */
function permuteSeries(listSeriesDiv, intIndex, boolDiff){

	if( 1 > listSeriesDiv.length ){ return new Array(); }
	var listVal = getSelected( listSeriesDiv[intIndex].getElementsByTagName("select")[1] );

	//  if the index has reached the end of the list, return the selected values from the last control
	if( listSeriesDiv.length == intIndex + 1 ){
		if( boolDiff ){ listVal.splice(listVal.length - 1, 0, "__DIFF__"); }
		return listVal;
	}

	//  otherwise, get the list for the next fcst_var and build upon it
	var listValNext = permuteSeries(listSeriesDiv, intIndex + 1, boolDiff);
	if( 1 > listVal.length ){ return listValNext; }
	var listRet = new Array();
	for(var i=0; i < listVal.length; i++){
		for(var j=0; j < listValNext.length; j++){
			listRet.push(listVal[i] + " " + listValNext[j]);
		}
	}
	return listRet;
}

/**
 * Return the boolean value of the format setting for plotN_diff, where N is specified as either 1 or 2
 */
function getPlotDiff(y){
	var tab = document.getElementById("tabFmtPlotBool");
	for(var i=0; i < tab.rows.length; i++){
		for(var j=0; j < tab.rows[i].cells.length; j++){
			var strLabel = tab.rows[i].cells[j].getElementsByTagName("span")[1].innerHTML;
			if( "plot" + y + "_diff:" == strLabel ){
				var strDiff = getSelected( tab.rows[i].cells[j].getElementsByTagName("select")[0] )[0];
				return ("true" == strDiff);
			}
		}
	}
	return false;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*
*  Bootstrap Controls
*
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Update the bootstrapping controls according to the enabled checkbox setting
 */
function updateBoot(){
	var divBoot = document.getElementById("divBoot");
	var chkBoot = divBoot.getElementsByTagName("input")[0];
	divBoot.getElementsByTagName("input")[1].disabled = !chkBoot.checked;
	divBoot.getElementsByTagName("input")[2].disabled = !chkBoot.checked;
	divBoot.getElementsByTagName("input")[3].disabled = !chkBoot.checked;
	divBoot.getElementsByTagName("input")[4].disabled = !chkBoot.checked;
}