
var _boolIE = false;
var _boolDim = false;

var _url;

var _strDBCon = "";
var _boolDBStatus = false;

var _listLnkSer = ["Dep1", "Series1", "Dep2", "Series2", "Fix", "Indy", "FmtPlot", "FmtSeries", "Boot"];

var _intDepIdNext = 1;
var _listDep1Div = new Array();
var _listDep2Div = new Array();

var _divFieldVal;
var _intFieldValIdNext = 0;

var _listStatMode = ["MMI", "MMIO", "MMIF", "MIA", "MAR", "MCD", "MAD", "P50", "P90"];

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
	_url = window.location + "servlet";

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
	var divDep0 = document.getElementById("divDep1").getElementsByTagName("div")[0];
	_listDep1Div.push( divDep0 );

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
	addFmtPlot("plot_type",		"png16m",	"txt");
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
	addFmtPlot("caption_weight","1",		"txt");
	addFmtPlot("caption_col",	"#333333FF","txt");
	addFmtPlot("caption_size",	".8",		"txt");
	addFmtPlot("caption_offset","3",		"txt");
	addFmtPlot("caption_align",	"0",		"txt");
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
	clearSelect(sel);
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
 * Remove all elements from a select control
 */
function clearSelect(sel){ while( 0 < sel.length ){ sel.remove(sel.length - 1); } }

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
 * It is assumed that the input list contains strings.  The elements of list are searched for an
 * exact match to the input string val and the first index containing a match is returned.  If no
 * match is found, -1 is returned. 
 */
function listSearch(val, list){
	for(i in list){ if( list[i] == val ){ return i; } }
	return -1;
}

/**
 * When the user changes selected plot data type, update the lists of variables appropriately and
 * reset the controls.
 */
function updatePlotData(){

	//  update the data members and lists accordingly
	var strPlotData = getSelected( document.getElementById("selPlotData") )[0];
	if( strPlotData == "Stat" ){
		_strPlotData = "stat";
		_listVar = _listVarStat;
		_listIndyVar = _listIndyVarStat;
	} else if( strPlotData == "Mode" ){
		_strPlotData = "mode";
		_listVar = _listVarMode;
		_listIndyVar = _listIndyVarMode;
	}
	
	clearControls();
}

/**
 * Clear all variable/value controls and reset the select lists to the currently selected lists of 
 * fcst_var and variables.
 */
function clearControls(){

	//  reset the dep stat controls
	clearDepStat(_listDep1Div[0].getElementsByTagName("input")[1].value);	
	while( 1 < _listDep1Div.length ){ removeDep1Var(_listDep1Div[1].getElementsByTagName("input")[1].value); }
	while( 0 < _listDep2Div.length ){ removeDep2Var(_listDep2Div[0].getElementsByTagName("input")[1].value); }
	listFcstVar1Req(0);

	//  reset the series controls
	while( 0 < _listSeries1Div.length ){ removeSeries1Div( _listSeries1Div[0].getElementsByTagName("input")[1].value); }
	while( 0 < _listSeries2Div.length ){ removeSeries2Div( _listSeries2Div[0].getElementsByTagName("input")[1].value); }

	//  reset the select field variable list
	var selField = document.getElementById("selField");
	clearSelect(selField);
	fillSelect(document.getElementById("selField"), _listVar);
	addSeries1Div();
	document.getElementById("lnkRemoveFieldVal0").style.display = "none";
	
	//  reset the fixed values
	while( 0 < _listFixDiv.length ){ removeFixDiv( _listFixDiv[0].getElementsByTagName("input")[1].value); }

	//  reset the indep controls
	var selIndyVar = document.getElementById("selIndyVar");
	clearSelect(selIndyVar);
	fillSelect(selIndyVar, _listIndyVar);
	clearIndyVal();
}

/**
 * Construct a list of RGBA color hex represenations with the specified length and format #RRGGBBAA, 
 * where the colors are spaced equally along the rainbow spectrum.
 */
function rainbow(num){
	if( 1 > num )	{ return new Array();	}
	if( 1 == num )	{ return ["#FF0000FF"]; }

	var listRet = new Array();
	var dblInc = 1.0 / (num - 1);
	var dblVal = 0;
	for(var i=0; i < num; i++, dblVal += dblInc){ listRet.push("#" + interpolateColor(dblVal) +  "FF"); }
	return listRet;
}

/**
 * Create a hex representation of the specified "rainbow" color along the spectrum from 0 (red, 
 * FF0000) to 1 (violet, FF00FF).   
 */
function interpolateColor(rel){
	if     ( rel < 0.0 ) { return "FF0000"; }
	else if( rel > 1.0 ) { return "FF00FF"; }

	var min = 0;
	var max = 1;
	
	switch( Math.floor(rel/.2) ){
		case 0:					return hex(max) + hex(max*(min + (1-min)*(rel/.2))) + hex(min);
		case 1:	rel -= .2;		return hex(min + max*(1-min)*(1 - rel/.2)) + hex(max) + hex(min);
		case 2:	rel -= .4;		return hex(min) + hex(max) + hex(max*(min + (1-min)*(rel/.2)));
		case 3:	rel -= .6;		return hex(min) + hex(max*(1-min)*(1 - rel/.2)) + hex(max);
		case 4:	rel -= .8;		return hex(max*(min + (1-min)*(rel/.2))) + hex(min) + hex(max);
		default:				return hex(max) + hex(min) + hex(max);
	}
}

/**
 * Create the two character hexadecimal representation of specified value, multiplied by 255.  The
 * intended use is to create an RGB representation with 8-bit color depth. 
 */
function hex(val){
	var strRet = Math.round(val * 255).toString(16).toUpperCase();
	while( 2 > strRet.length ){ strRet = "0" + strRet; }
	return strRet;
}

function toggleDebugDisp(show){
	
	//  update the visibility of the Serialize links
	for(var i=0; i < _listLnkSer.length; i++){
		document.getElementById("lnkSer" + _listLnkSer[i]).style.display = (show? "inline" : "none");		
	}
}


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
	listFcstVar1Req(0);
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Dependent Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function addDep1(){ addDep(1); }
function removeDep1Var(intDepId){ removeDepVar(1, intDepId); }
function listFcstVar1Req(intDepId){ listFcstVarReq(intDepId, listFcstVar1Resp); }
function listFcstVar1Resp(strResp){ selectFieldResp(strResp, _listDep1Div, 1, 0); }

function addDep2(){ addDep(2); }
function removeDep2Var(intDepId){ removeDepVar(2, intDepId); }
function listFcstVar2Req(intDepId){ listFcstVarReq(intDepId, listFcstVar2Resp); }
function listFcstVar2Resp(strResp){ selectFieldResp(strResp, _listDep2Div, 1, 0); }

/**
 * Create a GUI control cluster for specifying a dependent variable fcst_var and stat(s)
 */
function addDep(intY){

	var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
	
 	//  clone the dependent variable controls
 	var intDepId = _intDepIdNext++;
 	var divDep = _listDep1Div[0].cloneNode(true);

 	//  update the components of the cloned fixed value controls
 	divDep.id = "divDep" + intDepId;
 	var selFcstVar = divDep.getElementsByTagName("select")[0];
 	selFcstVar.id = "selFcstVar" + intDepId;
 	selFcstVar.setAttribute("onchange", "javascript:clearDepStat(" + intDepId + ")");
 	if( _boolIE ){ selFcstVar.attachEvent("onchange", new Function("clearDepStat(" + intDepId + ")")); }
 	var btnFcstVar = divDep.getElementsByTagName("input")[0];
 	btnFcstVar.setAttribute("onclick", "javascript:selectFcstVarReq(" + intDepId + ")");
 	if( _boolIE ){ btnFcstVar.attachEvent("onclick", new Function("selectFcstVarReq(" + intDepId + ")")); }
 	var selStat = divDep.getElementsByTagName("select")[1];
 	clearSelect(selStat);
 	selStat.style.display = "none";
 	selStat.id = "selStat" + intDepId;
 	selStat.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selStat.attachEvent("onchange", new Function("buildSeriesDiv()")); }
 	var lnkDep = divDep.getElementsByTagName("a")[0];
 	lnkDep.setAttribute("onclick", "javascript:removeDep" + intY + "Var(" + intDepId + ")");
 	if( _boolIE ){ lnkDep.attachEvent("onclick", new Function("removeDep" + intY + "Var(" + intDepId + ")")); }
 	divDep.getElementsByTagName("span")[0].style.display = "inline";
 	divDep.getElementsByTagName("input")[1].value = "" + intDepId;

 	//  add the new fixed variable value section to the page
 	listDepDiv.push(divDep);
	var divDepParent = document.getElementById("divDep" + intY);
	var divImgParent = document.getElementById("imgDep" + intY);
	console("addDep(" + intY + ")\n  divDepParent: " + divDepParent + "\n  divImgParent: " + divImgParent + "\n\n");
 	divDepParent.insertBefore(divDep, divImgParent);

 	//  ensure the first remove link is visible
 	listDepDiv[0].getElementsByTagName("span")[1].style.display = "inline";
}

/**
 * Remove the specified dep div from the list of dependent variable controls
 */
function removeDepVar(intY, intDepId){
	var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
	removeFieldValDiv(intDepId, listDepDiv, 1);
 	if( 1 == intY && 1 == listDepDiv.length ){ listDepDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
 	buildSeriesDiv();
}

/**
 * List the fcst_var database field values, and populate the dependent variable fcst_var select with the results
 */
function listFcstVarReq(intDepId, fnListFcstVarResp){
	sendRequest("POST", "<list_val><id>" + intDepId + "</id><" + _strPlotData + "_field>FCST_VAR</" + _strPlotData + "_field>" + "</list_val>", fnListFcstVarResp);
}

/**
 * List the statistics available for the specified forecast variable and populate the statistics select
 * with the results
 */
function selectFcstVarReq(intId){
	
	//  query the database for stat_header stats, if appropriate
	if( _strPlotData == "stat" ){
		var selFcstVar = document.getElementById("selFcstVar" + intId);
		sendRequest("POST",
					"<list_stat><id>" + intId + "</id><" + _strPlotData + "_fcst_var>" + selFcstVar.options[selFcstVar.selectedIndex].text +
						"</" + _strPlotData + "_fcst_var></list_stat>",
					selectFcstVarResp);
	} 
	
	//  otherwise, use the static list of mode stats
	else {
		var selFcstVar = document.getElementById("selStat" + intId);
		fillSelect(selFcstVar, _listStatMode);
		selFcstVar.style.display = "inline";
	}
}
function selectFcstVarResp(strResp){

	//  parse the response
	var resp = parseListValResp(strResp, "stat");
	if( null == resp ){ return; }

	//  populate and display the stats select control
	var selFcstVar = document.getElementById("selStat" + resp.id);
	fillSelect(selFcstVar, resp.vals);
	selFcstVar.style.display = "inline";
}

/**
 * Build an XML criteria string for a <list_val> command which contains the list of currently selected
 * dependent variable fcst_var values
 */
function buildFcstVarCrit(intY){
	
	//  determine the list of dep divs to consider
	var listDepDiv; 
	if     ( 1 == intY ){ listDepDiv = _listDep1Div; }
	else if( 2 == intY ){ listDepDiv = _listDep2Div; }
	else                { listDepDiv = _listDep1Div.concat(_listDep2Div); }
	
	//  add the fcst_var from each dep div to the list
	var strFixCrit = "<field name=\"FCST_VAR\">";
	for(i in listDepDiv){
		var selFcstVar = listDepDiv[i].getElementsByTagName("select")[0];
		strFixCrit += "<val>" + selFcstVar.options[selFcstVar.selectedIndex].text + "</val>";
	}
	strFixCrit += "</field>";
	return strFixCrit;
}

/**
 * Clears the dep stat select control of the specified index when a change is made to the fcst_var 
 * select 
 */
function clearDepStat(intIndex){
	//var selStat = _listDep1Div[intIndex].getElementsByTagName("select")[1];
	var selStat = document.getElementById("selStat" + intIndex);
	clearSelect(selStat);
	selStat.style.display = "none";
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
	var selVar = divFieldVal.getElementsByTagName("select")[0];
	selVar.id = "selFieldVal" + intId;
	selVar.setAttribute("onchange", "javascript:clearFieldVal(selVal" + intId + ")");
 	if( _boolIE ){ selVar.attachEvent("onchange", new Function("clearFieldVal(selVal" + intId + ")")); }
	var btnFieldVal = divFieldVal.getElementsByTagName("input")[0];
	btnFieldVal.setAttribute("onclick", "javascript:select" + category + "VarReq(" + intId + ")");
	if( _boolIE ){ btnFieldVal.attachEvent("onclick", new Function("select" + category + "VarReq(" + intId + ")")); }
	var selVal = divFieldVal.getElementsByTagName("select")[1];
	clearSelect(selVal);
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
 * is passed to the specified response function.  If the specified y-axis is 1 or 2, the fcst_var
 * criteria for the specified axis is added.
 */
function selectFieldReq(intId, listDiv, intFixEnd, fnResp, intY){

	//  attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, 1);
	if( 0 > intIndex ){
		console("selectFieldReq() - ERROR: div " + intId + " not found\n\n");
		return;
	}

	//  gather the criteria
	var strFcstVarCrit = "";
	if( 1 == intY || 2 == intY ){ strFcstVarCrit = buildFcstVarCrit(intY); }
	else                        { strFcstVarCrit += buildFcstVarCrit();    }
	var strFixCrit = buildFixCrit(intFixEnd);

	//  build a list_val request for the selected field
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

/**
 * Clears the specified field value select control of the specified index when a change is made
 * to the field var 
 */
function clearFieldVal(selVal){
	clearSelect(selVal);
	selVal.style.display = "none";
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Series Variable Controls
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add and remove a series1 div and process select requests and responses
 */
function addSeries1Div()               { addSeriesDiv(1); }
function removeSeries1Div(intId)       { removeSeriesDiv(1, intId); }
function selectSeries1VarReq(intId)    { selectFieldReq(intId, _listSeries1Div, _listFixDiv.length - 1, selectSeries1VarResp, 1); }
function selectSeries1VarResp(strResp) { selectFieldResp(strResp, _listSeries1Div, 1, 1); }

/**
 * Handlers to add and remove a series2 div and process select requests and responses
 */
function addSeries2Div()               { addSeriesDiv(2); }
function removeSeries2Div(intId)       { removeSeriesDiv(2, intId); }
function selectSeries2VarReq(intId)    { selectFieldReq(intId, _listSeries2Div, _listFixDiv.length - 1, selectSeries2VarResp, 2); }
function selectSeries2VarResp(strResp) { selectFieldResp(strResp, _listSeries2Div, 1, 1); }

/**
 * Build and add a series div for the specified series with configured controls 
 */
function addSeriesDiv(intSeries){
	//  determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	//  add a field val div and modify its components for duty as a series div
	addFieldValDiv("Series" + intSeries, listSeriesDiv);
	var intSeriesIndex = listSeriesDiv.length - 1;
	var selVal = listSeriesDiv[ intSeriesIndex ].getElementsByTagName("select")[1];
	selVal.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selVal.attachEvent("onchange", new Function("buildSeriesDiv()")); }
	for(i in listSeriesDiv){ listSeriesDiv[i].getElementsByTagName("span")[1].style.display = "inline"; }
}

/**
 * Dispose of and hide the series div of the specified series with the specified id
 */
function removeSeriesDiv(intSeries, intId){
	//  determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	//  dispose of and hide the series div
	removeFieldValDiv(intId, listSeriesDiv, 1);
 	if( 1 == listSeriesDiv.length ){ listSeriesDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
 	buildSeriesDiv();
}


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
	var strField = getSelected( document.getElementById("selIndyVar") )[0];
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
	clearIndyVal();
	
	//  add a indy val control group for each indy value
	var divIndy = document.getElementById("divIndy");
	var strField = getSelected( document.getElementById("selIndyVar") )[0];
	for( i in resp.vals ){
		var trIndyVal = tabIndyVal.insertRow(tabIndyVal.rows.length);

		//  build a control set for the independent variable value
		var tdIndyChk = trIndyVal.insertCell(0);
		tdIndyChk.appendChild( document.getElementById("spanIndyValChk").cloneNode(true) );
		var tdIndyLab = trIndyVal.insertCell(1);
		tdIndyLab.appendChild( document.getElementById("spanIndyValLab").cloneNode(true) );
		trIndyVal.getElementsByTagName("span")[1].innerHTML = resp.vals[i];
		trIndyVal.style.display = "table-row";
		
		//  set the default label
		var strLabel = resp.vals[i];
		if( "FCST_LEAD" == strField ){
			var listParse = strLabel.match( /(\d+)0000$/ );
			if( null != listParse ){ strLabel = listParse[1]; }
		}
		trIndyVal.getElementsByTagName("input")[1].value = strLabel;
	}
	document.getElementById("spanIndyCheck").style.display = "inline";
}

/**
 * Remove all rows of the indy table that contain values
 */
function clearIndyVal(){
	//  hide all currently display indy val controls
	var tabIndyVal = document.getElementById("tabIndyVal");
	while( 1 < tabIndyVal.rows.length ){ tabIndyVal.deleteRow(tabIndyVal.rows.length - 1); }
	document.getElementById("spanIndyCheck").style.display = "none";
}

/**
 * Checks or unchecks all indy values, as specified
 * @param boolCheck true to check, false to uncheck
 */
function indyCheck(boolCheck){
	var tabIndyVal = document.getElementById("tabIndyVal");
	for(var i=1; i < tabIndyVal.rows.length; i++){
		var chkIndy = tabIndyVal.rows[i].getElementsByTagName("input")[0];
		chkIndy.checked = boolCheck;
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

function handleFmtPlotTxtDisp(){
	var spanDisp = document.getElementById("spanFmtPlotTxtDisp");
	var tab = document.getElementById("tabFmtPlotTxt");
	var imgArrow = spanDisp.getElementsByTagName("img")[0];
	var spanMsg = spanDisp.getElementsByTagName("span")[0];
	if( null != spanMsg.innerHTML.match("Show.*") ){
		console("  showing controls\n");
		spanMsg.innerHTML = "Hide Plot Formatting";
		imgArrow.src = imgArrow.src.substring(0, imgArrow.src.lastIndexOf("/") + 1) + "arrow_down.gif";
		tab.style.display = "table";
	} else {
		console("  hiding controls\n");	
		spanMsg.innerHTML = "Show Plot Formatting";
		imgArrow.src = imgArrow.src.substring(0, imgArrow.src.lastIndexOf("/") + 1) + "arrow_right.gif";		
		tab.style.display = "none";
	}
}

/**
 * Build the list of plot series reflected by the current state of the controls
 */
function buildSeriesDiv(){
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var spanFmtSeriesNone = document.getElementById("spanFmtSeriesNone");
	var intNumSeries = 0;

console("buildSeriesDiv()\n");

	//  build a table containing all current series settings
	var table = new Hashtable();
console("  building table\n");
	for(var i=0; i < tabFmtSeries.rows.length; i++){		
		var listSpan = tabFmtSeries.rows[i].getElementsByTagName("span");
		var listInput = tabFmtSeries.rows[i].getElementsByTagName("input");
//console("    rows[" + i + "] - listSpan.length = " + listSpan.length + "\n");
		if( 2 > listSpan.length || tabFmtSeries.style.display == "none" ){ continue; }
		
		//  get the series name and values and put them in the table
		var strSeriesName = listSpan[2].innerHTML + " - " + listSpan[3].innerHTML;		
		var strFmt = "";
		for(var j=0; j < 8; j++){ strFmt += (0 < j? "|" : "") + listInput[j].value; }
		
console("    table.put():\n      " + strSeriesName + "\n      " + strFmt + "\n");
		table.put(strSeriesName, strFmt);
	}
console("  building table complete\n\n");

	//  clear all existing series, except the first two
	while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }

	//  build permutation of the series values
	var listSeries1Perm = permuteSeries(_listSeries1Div, 0, getPlotDiff(1));
	var listSeries2Perm = permuteSeries(_listSeries2Div, 0, getPlotDiff(2));
	
	//  build all y1 and y2 series
	for(var intY=1; intY <= 2; intY++){

		var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
		var listSeriesPerm = (1 == intY? listSeries1Perm : listSeries2Perm);	
			
		//  for each dep div, consider the fcst_var and selected stats
		for(var intDep=0; intDep < listDepDiv.length; intDep++){
	
			//  get the dep var information
			var strFcstVar = getSelected( listDepDiv[intDep].getElementsByTagName("select")[0] )[0];
			var listStat = getSelected( listDepDiv[intDep].getElementsByTagName("select")[1] );
	
			//  build a series for each combination of fcst_var, stat and series permutation
			for(var intStat=0; intStat < listStat.length; intStat++){
				for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++){
	
					//  build the series name
					var strSeriesName =  listSeriesPerm[intSeries] + " " + strFcstVar + " " + listStat[intStat];
	
					var trFmtSeries;
					var tdName;
	
					//  if the series is the first to be built, use the existing controls
					if( 0 == intNumSeries++ ){
						trFmtSeries = tabFmtSeries.rows[0];
						tdName = trFmtSeries.cells[0];
					}
	
					//  otherwise, build a new set of series formatting controls
					else {
						
						//  insert the <hr/> between series format controls
						if( 2 == intNumSeries ){ tabFmtSeries.rows[1].style.display = "table-row"; }
						else {
							var trHR = tabFmtSeries.insertRow( tabFmtSeries.rows.length );
							var tdHR = trHR.insertCell(0);
							tdHR.colSpan = "3";
							tdHR.appendChild( document.getElementById("spanFmtSeriesHR").cloneNode(true) );
						}
	
						//  insert a copy of the series format controls
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
						tdFmt1.getElementsByTagName("input")[1].value = "";
	
						var tdFmt2 = trFmtSeries.insertCell(2);
						tdFmt2.align = "right";
						tdFmt2.style.width = "275px";
						tdFmt2.style.paddingTop = "20px";
						tdFmt2.appendChild( document.getElementById("spanFmtSeriesFmt2").cloneNode(true) );
					}
	
					//  populate the controls with the series name
					var strYSeries = (1 == intY? "Y1" : "Y2") + " Series";
					tdName.getElementsByTagName("span")[2].innerHTML = strSeriesName;
					tdName.getElementsByTagName("span")[3].innerHTML = strYSeries;
					
					//  if there are format control settings for this series in the table, apply them					
					var strVal = table.get(strSeriesName + " - " + strYSeries)
					var listFmtTxt = trFmtSeries.getElementsByTagName("input");
console("  table[" + strSeriesName + " - " + strYSeries + "]: " + strVal + "\n");
					if( undefined != strVal ){
						var listVal = strVal.split("|");
						for(var i=0; i < listVal.length; i++){ listFmtTxt[i].value = listVal[i]; }
					} 
					
					//  otherwise, apply default settings
					else {
						listFmtTxt[0].value = "none";
						listFmtTxt[1].value = "";
						listFmtTxt[2].value = "20";
						listFmtTxt[3].value = "b";
						listFmtTxt[4].value = "1";
						listFmtTxt[5].value = "1";
						listFmtTxt[6].value = "1";
						listFmtTxt[7].value = "";
					}
					
				}  // end: for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++)
				
			}  // end: for(var intStat=0; intStat < listStat.length; intStat++)
			
		}  // end: for(var intDep=0; intDep < listDepDiv.length; intDep++)
		
	}  // end: for(var intY=1; intY <= 2; intY++)
	
	//  set the default color for each series to the appropriate shade of rainbow
	var listColors = rainbow(intNumSeries);
	for(var i=0; i < intNumSeries; i++){
		var txtColor = tabFmtSeries.rows[2*i].getElementsByTagName("input")[1];
		if( "" == txtColor.value ){ txtColor.value = listColors[i]; }		
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


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 *  Plot Spec Functions
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Contruct the plot spec xml from information selected in the plot controls
 */
function buildPlotXML(){
	
	var listInput;
	
	var strDepXML = "";
	
	//  <template>
	strDepXML += "<template>" + getSelected( document.getElementById("selTemplate") )[0] + ".R_tmpl</template>";	
	
	//  <dep>
	strDepXML += "<dep>";
	strDepXML += "<dep1>" + buildFieldValXML("fcst_var", "stat", _listDep1Div, true) + "</dep1>";
	strDepXML += "<dep2>" + buildFieldValXML("fcst_var", "stat", _listDep2Div, true) + "</dep2>";
	strDepXML += "<fix></fix></dep>";
	
	//  <series1> and <series2>
	var strSeriesXML = "";
	strDepXML += "<series1>" + buildFieldValXML("field", "val", _listSeries1Div, false) + "</series1>";
	strDepXML += "<series2>" + buildFieldValXML("field", "val", _listSeries2Div, false) + "</series2>";
	
	//  <plot_fix>
	strDepXML += "<plot_fix>" + buildFieldValXML("field", "val", _listFixDiv, false) + "</plot_fix>";
	strDepXML += "<agg></agg>";
	
	//  <indep>
	var divIndy = document.getElementById("divIndy");
	var tabIndyVal = document.getElementById("tabIndyVal");
	var strIndepField = getSelected( divIndy.getElementsByTagName("select")[0] )[0].toLowerCase();
	strDepXML += "<indep name=\"" + strIndepField + "\">";
	for(var i=0; i < tabIndyVal.rows.length; i++){
		listInput = tabIndyVal.rows[i].getElementsByTagName("input");
		var boolIndyValChk = listInput[0].checked;
		var strVal = tabIndyVal.rows[i].getElementsByTagName("span")[1].innerHTML;
		var strLab = listInput[1].value;
		var strPlotVal = listInput[2].value;
		if( boolIndyValChk ){
			strDepXML += "<val label=\"" + strLab + "\" plot_val=\"" + strPlotVal + "\">" + strVal + "</val>";
		}
	}
	strDepXML += "</indep>";
	
	//  <tmpl>
	var divTitleLab = document.getElementById("divTitleLab");
	listInput = divTitleLab.getElementsByTagName("input");
	strDepXML += "<tmpl>";
	strDepXML +=    "<title>" + listInput[0].value + "</title>";
	strDepXML +=  "<x_label>" + listInput[1].value + "</x_label>";
	strDepXML += "<y1_label>" + listInput[2].value + "</y1_label>";
	strDepXML += "<y2_label>" + listInput[3].value + "</y2_label>";
	strDepXML +=  "<caption>" + listInput[4].value + "</caption>";
	strDepXML += "</tmpl>";
	
	//  bool formatting
	var tabFmtPlotBool = document.getElementById("tabFmtPlotBool");
	for(var i=0; i < tabFmtPlotBool.rows.length; i++){
		for(var j=0; j < tabFmtPlotBool.rows[i].cells.length; j++){
			var listSpan = tabFmtPlotBool.rows[i].cells[j].getElementsByTagName("span");
			var listSel = tabFmtPlotBool.rows[i].cells[j].getElementsByTagName("select");			
			if( null == listSel || 1 > listSel.length ){ continue; }
			var strName = listSpan[1].innerHTML.replace(/:$/, "");
			strDepXML += "<" + strName + ">" + getSelected(listSel[0])[0] + "</" + strName + ">";
		}
	}
	
	//  txt formatting
	var tabFmtPlotTxt = document.getElementById("tabFmtPlotTxt");
	for(var i=0; i < tabFmtPlotTxt.rows.length; i++){
		for(var j=0; j < tabFmtPlotTxt.rows[i].cells.length; j++){
			var listSpan = tabFmtPlotTxt.rows[i].cells[j].getElementsByTagName("span");
			var listInput = tabFmtPlotTxt.rows[i].cells[j].getElementsByTagName("input");			
			if( null == listInput || 1 > listInput.length ){ continue; }
			var strName = listSpan[1].innerHTML.replace(/:$/, "");
			strDepXML += "<" + strName + ">" + listInput[0].value + "</" + strName + ">";
		}
	}
	
	//  series formatting
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var listFmtSeries = new Array("", "", "", "", "", "", "", "");
	var boolLegend = false;
	for(var i=0; i < tabFmtSeries.rows.length; i++){
		listInput = tabFmtSeries.rows[i].getElementsByTagName("input");
		if( null == listInput || 8 > listInput.length ){ continue; }
		for(var j=0; j < 8; j++){
			var strVal = listInput[j].value;
			if( 7 == j && strVal != "" ){ boolLegend = true; }
			if( 0 == j || 1 == j || 3 == j || 7 == j){ strVal = "\"" + strVal + "\""; }
			listFmtSeries[j] += (0 < i? ", " : "") + strVal;
		}
	}
	strDepXML +=    "<plot_ci>c(" + listFmtSeries[0] + ")</plot_ci>";
	strDepXML +=     "<colors>c(" + listFmtSeries[1] + ")</colors>";
	strDepXML +=        "<pch>c(" + listFmtSeries[2] + ")</pch>";
	strDepXML +=       "<type>c(" + listFmtSeries[3] + ")</type>";
	strDepXML +=        "<lty>c(" + listFmtSeries[4] + ")</lty>";
	strDepXML +=        "<lwd>c(" + listFmtSeries[5] + ")</lwd>";
	strDepXML += "<con_series>c(" + listFmtSeries[6] + ")</con_series>";
	if( boolLegend ){
		strDepXML += "<legend>c(" + listFmtSeries[7] + ")</legend>";
	}
	
	//  axis formatting
	var divFmtAxis = document.getElementById("divFmtAxis");
	listInput = divFmtAxis.getElementsByTagName("input");
	strDepXML +=  "<y1_lim>" + listInput[0].value + "</y1_lim>";
	strDepXML += "<y1_bufr>" + listInput[1].value + "</y1_bufr>";
	strDepXML +=  "<y2_lim>" + listInput[2].value + "</y2_lim>";
	strDepXML += "<y2_bufr>" + listInput[3].value + "</y2_bufr>";
	
	return strDepXML;
}

/**
 * Build an XML structure with specified field tag and value tag from the information selected in the
 * specified list of div controls
 */
function buildFieldValXML(strFieldTag, strValTag, listDiv, boolCapField){
	var strXML = "";
	for(i in listDiv){
		var listSel = listDiv[i].getElementsByTagName("select");
		var strVar = getSelected( listSel[0] )[0];
		if( !boolCapField ){ strVar = strVar.toLowerCase(); }
		var listVal = getSelected( listSel[1] );
		strXML += "<" + strFieldTag + " name=\"" + strVar + "\">";
		for(j in listVal){ strXML += "<" + strValTag + ">" + listVal[j] + "</" + strValTag + ">"; }
		strXML += "</" + strFieldTag + ">";
	}
	return strXML;
}

function runPlotReq(){ sendRequest("POST", "<plot>" + buildPlotXML() + "</plot>", runPlotResp); }
function runPlotResp(strResp){
	if( null != (listProc = strResp.match( /<plot>(.*)<\/plot>/ )) ){
		var strPlot = listProc[1];
		var win = window.open("plot.html", "METViewer - " + strPlot);
	}
}

function testPlotResp(){ runPlotResp("<plot>plot_00021_20100810_084037</plot>"); }