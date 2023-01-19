/* This API transaction EXT422MI.DelyConGGN is used to add GGN information as text to MFTRNS data
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023.01.18
 *  @version   1.0
 *
 */

 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.time.temporal.ChronoUnit;
 import java.util.Map;
 
 public class DelyConGGN extends ExtendM3Transaction {
	 private final MIAPI mi;
	 private final DatabaseAPI database;
	 private final MICallerAPI miCaller;
	 private final LoggerAPI logger;
	 private final ProgramAPI program;
	 private final IonAPI ion;
 
	 private String errorMessage = "";
	 private final String dfmt = "yyyyMMdd";
	 private int conoInt = 0;
	 private String csvData = "";
	 private String cono = "";
	 private long dlixLong = 0l;
	 private String dlix = "";
	 private String panr = "";
	 private String whlo = "";
	 private String rorc = "";
	 private int rorcInt = 0;
	 private String ridn = "";
	 private int ridlInt = 0;
	 private String ridl = "";
	 private int ridxInt = 0;
	 private String ridx = "";
	 private String bano = "";
	 private String camu = "";
	 private String itno = ""
	 private long orgTxidLong = 0l;
	 private long txidLong = 0l;
	 private String txid = "";
	 private String[] arrGGN = new long [50];
 
	 public DelyConGGN(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		 this.mi = mi;
		 this.database = database;
		 this.miCaller = miCaller;
		 this.logger = logger;
		 this.program = program;
		 this.ion = ion;
	 }
 
	 public void main() {
		 //check the given data
		 if (!validateInput()) {
			 logger.debug("XtendM3Debug_DelyConGGN validateInput ended with false!!!!");
			 mi.write();
			 return;
		 }
		 //check data for update is existing in M3
		 if (!validateMFTRNS()) {
			 logger.debug("XtendM3Debug_DelyConGGN valdateMFTRNS - MFTRNS was not found");
			 mi.write();
			 return;
		 }
		 //do the update and create the GGN text information
		 if (!updateFromGYGI()) {
			 logger.debug("XtendM3Debug_DelyConGGN UpdateFromGyGI- Update didn't work");
			 mi.write();
			 return;
		 }
		 //change the MFTRNS TXId if necessary
		 if (orgTxidLong != txidLong) {
			 updMFTRNS();
		 }
	 }
	 
	 
	 /*  validateInput
	  *
	  *  - Validate entered data like CONO, WHLO, DLIX,....
	  */
	 boolean validateInput(){
 
		 logger.debug("XtendM3Debug_DelyConGGN Start - validateInput");
 
		 //check content (result from before called API Flow GYGI)
		 csvData = mi.in.get("DATA");
		 if (csvData == null) {
			 csvData = "";
		 }
		 csvData.trim();
		 logger.debug("XtendM3Debug_DelyConGGN - input csvData: " + csvData);
		 if (csvData == "") {
			 mi.error("Jason Data (DATA) must be entered");
			 return false;
		 }
		 //check company
		 cono = mi.in.get("CONO")
		 if (cono == null) {
			 cono = "";
		 }
		 cono.trim();
		 if (cono == "") {
			 mi.error("Company must be entered")
			 return false;
		 }
		 if(validateCompany(cono)){
			 mi.error("Company " + cono + " is invalid")
			 return false;
		 }
		 conoInt = mi.in.get("CONO");
		 cono = conoInt.toString();
 
		 //check warehouse
		 whlo = mi.in.get("WHLO");
		 if (whlo == null) {
			 whlo = "";
		 }
		 whlo.trim();
		 if (whlo == ""){
			 mi.error("Warehouse has to be entered");
			 return false;
		 }
		 if (!validateMITWHL()) {
			 return false;
		 }
 
		 //check for DLIX
		 dlix = mi.in.get("DLIX");
		 if (dlix == null) {
			 dlix = "";
		 }
		 dlix.trim();
		 if (dlix == ""){
			 mi.error("Delivery no has to be entered");
			 return false;
		 }
		 dlixLong = mi.in.get("DLIX");
		 if (dlixLong == 0l) {
			 mi.error("Delivery no has to be entered");
			 return false;
		 }
 
		 //Checks for RORC
		 rorc = mi.in.get("RORC");
		 if (rorc == null) {
			 rorc = "";
		 }
		 rorc.trim();
		 if (rorc == ""){
			 mi.error("Reference order cathegory has to be entered");
			 return false;
		 }
		 if (rorc != "3" && rorc != "3.0" && rorc != "5" && rorc != "5.0" ){
			 mi.error("Transaction is only active for order categories 3 and 5 - " + rorc + "is not supported" );
			 return false;
		 }
		 //rorcInt = 3;
		 rorcInt = mi.in.get("RORC");
 
		 //Check for PANR
		 panr = mi.in.get("PANR");
		 if (panr == null) {
			 panr = "";
		 }
		 panr.trim();
		 if (panr == ""){
			 mi.error("A package no has to be entered");
			 return false;
		 }
 
		 //Check for RIDN
		 ridn = mi.in.get("RIDN");
		 if (ridn == null) {
			 ridn = "";
		 }
		 ridn.trim();
		 if (ridn == ""){
			 mi.error("A reference order no has to be entered");
			 return false;
		 }
 
		 //checks for RIDL
		 ridl = mi.in.get("RIDL");
		 if (ridl == null) {
			 ridl = "";
		 }
		 ridl.trim();
		 if (ridl == ""){
			 mi.error("A reference order line no has to be entered");
			 return false;
		 }
		 ridlInt = mi.in.get("RIDL")
		 if ( ridlInt == 0) {
			 mi.error("A reference order line no ${ridl} is not allowed");
			 return false;
		 }
 
		 //check for RIDX
		 ridx = mi.in.get("RIDX");
		 if (ridx == null) {
			 ridx = "";
		 }
		 ridx.trim();
		 if (ridx == "" ){
			 mi.error("A reference order line suffix no has to be entered");
			 return false;
		 }
		 ridxInt = mi.in.get("RIDX");
 
		 //initialize bano if not already done
		 bano = mi.in.get("BANO");
		 if (bano == null) {
			 bano = "";
		 }
		 bano.trim();
		 
 
		 //initialize CAMU if not already done
		 camu = mi.in.get("CAMU");
		 if (camu == null) {
			 camu = "";
		 }
		 camu.trim();
		 
		 //no errors detected - validation was successful
		 return true;
	 }
 
	 /* validateCompany
	  *
	  * Validate given or retrieved CONO
	  * Input
	  * Company - from Input
	  */
	 boolean validateCompany(String company){
		 // Run MI program
		 def parameter = [CONO: company];
		 List <String> result = [];
		 Closure<?> handler = {Map<String, String> response ->
			 return response.CONO == 0};
		 miCaller.call("MNS095MI", "Get", parameter, handler);
	 }
 
 
	 /* validateMFTRNS
	  *
	  * check MFTRNS and save data for later use
	  */
	 boolean validateMFTRNS() {
		 logger.debug("XtendM3Debug_DelyConGGN Start -  validateMFTRNS");
		 DBAction action_MFTRNS = database.table("MFTRNS")
				 .index("00")
				 .selection("OSITNO", "OSTXID")
				 .build();
		 DBContainer MFTRNS = action_MFTRNS.createContainer();
		 logger.debug("XtendM3Debug_DelyConGGN Start - getMFTRNS");
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS cono " + cono.toString());
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS whlo " + whlo);
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS dlix " + dlix.toString());
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS panr " + panr);
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS ridn " + ridn);
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS ridl " + ridl.toString());
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS ridx " + ridx.toString());
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS bano " + bano);
		 logger.debug("XtendM3Debug_DelyConGGN getMFTRNS camu " + camu);
		 
		 MFTRNS.set("OSCONO", conoInt);
		 MFTRNS.set("OSWHLO", whlo);
		 MFTRNS.set("OSDLIX", dlixLong);
		 MFTRNS.set("OSPANR", panr);
		 MFTRNS.set("OSRORC", rorcInt);
		 MFTRNS.set("OSRIDN", ridn);
		 MFTRNS.set("OSRIDL", ridlInt);
		 MFTRNS.set("OSRIDX", ridxInt);
		 MFTRNS.set("OSBANO", bano);
		 MFTRNS.set("OSCAMU", camu);
		 if (!action_MFTRNS.read(MFTRNS)) {
			 mi.error("The package does not exist whlo " + whlo + " dlix " + dlix.toString()
					 + " panr " + panr + " ridn " + ridn + " ridl " + ridl.toString()
					 + " ridx " + ridx.toString() + " bano " + bano + " camu " + camu);
			 return false;
		 }
 
		 itno = MFTRNS.get("OSITNO");
		 txid = MFTRNS.get("OSTXID");
		 txidLong = MFTRNS.get("OSTXID");
		 orgTxidLong = MFTRNS.get("OSTXID");
		 return true;
	 }
 
	 /*
	  * updMFTRNS
	  *
	  * Start update process by reading and updating MFTRNS
	  *
	  */
	 void updMFTRNS(){
		 logger.debug("XtendM3Debug_DelyConGGN Start - updMFTRNS");
		 DBAction action_MFTRNS = database.table("MFTRNS")
				 .index("00")
				 .selection("OSCHNO", "OSTXID")
				 .build();
		 DBContainer MFTRNS = action_MFTRNS.getContainer();
		 
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS cono " + conoInt.toString());
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS ridn " + whlo);
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS dlix " + dlixLong.toString());
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS panr " + panr);
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS ridn " + ridn);
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS ridl " + ridlInt.toString());
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS ridx " + ridxInt.toString());
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS bano " + bano);
		 logger.debug("XtendM3Debug_DelyConGGN updMFTRNS camu " + camu);
		 
		 MFTRNS.set("OSCONO", conoInt);
		 MFTRNS.set("OSWHLO", whlo);
		 MFTRNS.set("OSDLIX", dlixLong);
		 MFTRNS.set("OSPANR", panr);
		 MFTRNS.set("OSRORC", rorcInt);
		 MFTRNS.set("OSRIDN", ridn);
		 MFTRNS.set("OSRIDL", ridlInt);
		 MFTRNS.set("OSRIDX", ridxInt);
		 MFTRNS.set("OSBANO", bano);
		 MFTRNS.set("OSCAMU", camu);
 
		 // Read with lock
		 action_MFTRNS.readLock(MFTRNS, updateCallBack_MFTRNS);
	 }
 
 
	 /* updateCallBack_MFTRNS
	 *
	 *  - update MFTRNS fields TXID
	 */
	 Closure<?> updateCallBack_MFTRNS = { LockedResult lockedResult ->
		 
		 logger.debug("XtendM3Debug_DelyConGGN Closure<?> updateCallBack_MFTRNS");
		 // Get todays date
		 LocalDateTime now = LocalDateTime.now();
		 DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		 String formatDate = now.format(format1);
 
		 int changeNo = lockedResult.get("OSCHNO");
		 int newChangeNo = changeNo + 1;
 
		 logger.debug("XtendM3Debug_DelyConGGN Closure<?> updateCallBack - TXID = ${txid} ");
		 // Update the MFTRNS field with new TXID
		 lockedResult.set("OSTXID", txidLong);
 
		 // Update changed information
		 int changeddate=Integer.parseInt(formatDate);
		 lockedResult.set("OSLMDT", changeddate);
		 lockedResult.set("OSCHNO", newChangeNo);
		 lockedResult.set("OSCHID", program.getUser());
		 lockedResult.update();
	 }
 
	 /* validateMITWHL
	  *
	  * check that the given WHLO is existing
	  */
	 private boolean validateMITWHL() {
		 DBAction action_MITWHL = database.table("MITWHL")
				 .index("00")
				 .build();
		 DBContainer MITWHL = action_MITWHL.createContainer();
		 logger.debug("XtendM3Debug_DelyConGGN Start - validateMITWHL");
		 logger.debug("XtendM3Debug_DelyConGGN validateMITWHL cono " + cono.toString() + " WHLO: " + whlo);
		 MITWHL.set("MWCONO", conoInt);
		 MITWHL.set("MWWHLO", whlo);
		 if (!action_MITWHL.read(MITWHL)) {
			 mi.error("The warehouse ${whlo} does not exist");
			 return false;
		 }
		 return true;
	 }
 
	 
	 /* updateFromGYGI
	  *
	  * - get the GGN codes via call of lstEXT_GGN in an array
	  * - use the array arrGGN for the creation of text information in M3
	  *
	  */
	 private boolean updateFromGYGI() {
		 logger.debug("XtendM3Debug_updateFromGYGI() - start");
 
		 //set integer data 123.0 to string data without decimal sign and zero 123
		 dlix = dlixLong.toString().trim();
		 ridl = ridlInt.toString().trim();
 
		 //use given json data fromGYGI DB
		 lstEXT_GGN(whlo, dlix, ridl, bano);
		 
		 //check if GGN code have been detected
		 if (arrGGN[0] == null) {
			 arrGGN[0] == "";
		 }
		 arrGGN[0].trim();
		 if (arrGGN[0] == null || arrGGN[0] == "") {
			 logger.debug("XtendM3Debug_updateFromGYGI() - No records found");
			 mi.error("GYGI DB - No records found for whlo " + whlo + " DLIX " + dlix + " RIDL " + ridl + " BANO " + bano);
			 return false;
		 }
 
		 //delete already existing GGN connected TXID
		 if (arrGGN[0] != "") {
			 if (txidLong != 0l) {
				 delTxtBlocklins(txidLong);
			 } else {
				 //create a new txid if the now existing value is 0l
				 txid = rtvNewTXID();
				 txidLong = Long.parseLong(txid);
				 setTextId(txid, cono, whlo, dlix, panr, rorc, ridn, ridl, ridx, bano, camu);
			 }
		 }
		 txid = txidLong.toString().trim();
		 
		 //create a new text header
		 if (!addTxtBlockHead(txid)) {
			 logger.debug("XtendM3Debug_updateFromGYGI() finishes after gotten back FALSE from addTxtBlockHead");
			 return false;
		 }
 
		 //add the GGN information to the text header as lines
		 for(int arrIndex = 0; arrIndex < 50; arrIndex++) {
			 if (arrGGN[arrIndex] != "") {
				 logger.debug("XtendM3Debug_updateFromGYGI() arrGGN value ${arrGGN[arrIndex]} for index ${arrIndex}"  );
				 addTxtBlockLine(txid, arrGGN[arrIndex]);
 
				 logger.debug("WHLO: " + whlo);
				 logger.debug("DLIX: " + dlix);
				 logger.debug("RIDL: " + ridl);
				 logger.debug("BANO: " + bano);
				 logger.debug("TGGN: " + arrGGN[arrIndex]);
 
			 }
		 }
		 return true;
	 }
 
 
	 /* delTxtBlocklins
	  *
	  * Call Api "CRS980MI"/"DltTxtBlockLins"
	  *
	  * input values
	  *   CONO - cono
	  *   TXVR - "GGN"
	  *   TFIL - constant value "MSYTXH"
	  *   FILE - constant value "MFTRNS00"
	  * return value
	  *  OK/NOK NOK will be ignored (specification)
	  */
	 boolean delTxtBlocklins (long txidLong) {
		 boolean returnValue = false;
		 logger.debug("XtendM3Debug_delTxtBlocklins() Start - passed txid value ${txid}");
		 String txid = txidLong.toString().trim();
		 def callback_dtbl = { Map <String, String> out ->
			 if (out.error != null) {
				 logger.debug("XtendM3Debug_delTxtBlocklins()- not ok >> result not used with regards to the spcification");
				 returnValue = false;
			 } else {
				 logger.debug("XtendM3Debug_delTxtBlocklins()- ok");
				 returnValue = true;
			 }
		 }
		 
		 def params = [ "CONO": cono,
			 "TXID": txid,
			 "TXVR": "GGN",
			 "TFIL": "MSYTXH",
			 "FILE": "MFTRNS00"
		 ];
		 
		 miCaller.call("CRS980MI",
				 "DltTxtBlockLins",
				 params,
				 callback_dtbl);
		 return returnValue;
	 }
 
	 /* rtvNewTXID
	  *
	  * Call Api "CRS980MI/RtvNewTextID"
	  *
	  * input
	  *   FILE - constant value "MSYTXH"
	  * output
	  *   TXID
	  */
	 String rtvNewTXID () {
		 String returnTXID = "";
		 logger.debug("XtendM3Debug_rtvNewTXID() Start");
 
		 def callback_rnt = { Map <String, String> out ->
			 if (out.error != null) {
				 logger.debug("XtendM3Debug_rtvNewTXID()- not ok - return TXID = ${returnTXID}");
				 mi.error(out.errorMessage);
				 return returnTXID;
			 }
			 returnTXID = out.get("TXID").trim();
			 logger.debug("XtendM3Debug_rtvNewTXID()- ok - return TXID = ${returnTXID}");
		 }
 
		 def params = [ "FILE": "MSYTXH"		];
		 
		 miCaller.call("CRS980MI", 
		     "RtvNewTextID", 
		     params, 
		     callback_rnt);
		 return returnTXID;
	 }
 
	 /* setTextId
	  *
	  * Call Api "CRS980MI/SetTextID"
	  *
	  * input
	  *   FILE - constant "MFTRNS00"
	  *   TXID - TXID
	  *   KV01 - cono
	  *   KV02 - whlo
	  *   KV03 - dlix
	  *   KV04 - panr
	  *   KV05 - rorc
	  *   KV06 - ridn
	  *   KV07 - ridl
	  *   KV08 - ridx
	  *   KV09 - bano
	  *   KV10 - camu
	  * output
	  *   OK/NOK - with regards to the specification the result will not be used
	  */
	 void setTextId (String txid, String cono, String whlo, String dlix, String panr, String rorc, String ridn, String ridl, String ridx, String bano, String camu) {
		 logger.debug("XtendM3Debug_setTextId() Start");
		 Closure <?> callback_sti = { Map <String, String> out ->
			 logger.debug("XtendM3Debug_setTextId() - callBack Start");
			 if (out.error != null) {
				 logger.debug("XtendM3Debug_setTextId- not ok >> result will be ignored (specitication)");
			 } else {
				 logger.debug("XtendM3Debug_setTextId- ok");
			 }
		 }
 
		 def params = [ "FILE": "MFTRNS00",
			 "TXID":  txid,
			 "KV01":  cono,
			 "KV02":  whlo,
			 "KV03":  dlix,
			 "KV04":  panr,
			 "KV05":  rorc,
			 "KV06":  ridn,
			 "KV07":  ridl,
			 "KV08":  ridx,
			 "KV09":  bano,
			 "KV10":  camu,
		 ];
		 
		 miCaller.call("CRS980MI",
				 "SetTextID",
				 params,
				 callback_sti);
		 return;
	 }
 
	 /* addTxtBlockHead
	  *
	  * Call Api "CRS980MI/AddTxtBlockHead"
	  *
	  * Input
	  *   CONO - cono
	  *   TXID - txid
	  *   TXVR - constant value GGN
	  *   FILE - constant value MFTRNS00
	  *   KFLD - txid
	  *   USID - actual user
	  *   TFIL - constant value MSYTXH
	  * Output
	  *   OK/NOK - Error message on NOT ok, for Ok no additional data
	  */
	 boolean addTxtBlockHead (String txid) {
		 boolean returnValue = true;
		 logger.debug("XtendM3Debug_addTxtBlockHead() Start TXID = " + txid);
		 Closure <?> callback_atbh = { Map <String, String> out ->
			 logger.debug("XtendM3Debug_addTxtBlockHead() callBack Start");
			 if (out.error != null) {
				 logger.debug("XtendM3Debug_addTxtBlockHead- not ok - " + out.errorMessage);
				 mi.error(out.errorMessage);
				 returnValue = false;
			 }
		 }
 
		 def params = [ "CONO": cono,
			 "TXID":  txid,
			 "TXVR":  "GGN",
			 "FILE":  "MFTRNS00",
			 "KFLD":  txid,
			 "USID":  program.getUser().trim(),
			 "TFIL":  "MSYTXH"
		 ];
		 miCaller.call("CRS980MI", 
		    "AddTxtBlockHead", 
		    params, 
		    callback_atbh);
		 return returnValue;
	 }
 
	 /* addTxtBlockLine
	  *
	  * Call Api "CRS980MI/addTxtBlockLine"
	  *
	  * Input
	  *   CONO - cono
	  *   TXID - txid
	  *   TXVR - constant value GGN
	  *   TX60 - GGN from GYGI DB
	  *   TFIL - constant value MSYTXH
	  *   FILE - constant value MFTRNS00
	  * Output
	  *   OK/NOK - Error message on NOT ok, for OK also the created line no
	  */
	 boolean addTxtBlockLine (String txid, String ggn) {
		 boolean returnValue = false;
		 logger.debug("XtendM3Debug_addTxtBlockLine() Start");
		 Closure <?> callback_atbl = { Map <String, String> out ->
			 logger.debug("XtendM3Debug_addTxtBlockLine- callBack Start");
			 if (out.error != null) {
				 logger.debug("XtendM3Debug_addTxtBlockLine- not ok");
				 mi.error(out.errorMessage);
				 returnValue = false;
			 } else {
				 logger.debug("XtendM3Debug_addTxtBlockLine- ok");
				 returnValue = true;
			 }
		 }
 
		 def params = [ "CONO": cono,
			 "TXID":  txid,
			 "TXVR":  "GGN",
			 "TX60":  ggn,
			 "TFIL":  "MSYTXH",
			 "FILE":  "MFTRNS00"
		 ];
		 miCaller.call("CRS980MI",
				 "AddTxtBlockLine",
				 params,
				 callback_atbl);
		 return returnValue;
	 }
 
	 /* lstEXT_GGN
	  *
	  * instead of calling an Api workflow "AXON_GYGI" the output data is given as csv data directly to this transaction
	  *
	  * input
	  *   WHLO from event
	  *   DLIX from event
	  *   RIDL from event
	  *   BANO from event
	  * output
	  * 	 no output value
	  */
	 void lstEXT_GGN(final String whlo, final String dlix, final String ridl, final String bano) {
		 final Map<String, String> input = new HashMap<>();
		 input.put("warehouseCode", whlo);
		 input.put("pickListCode", dlix);
		 input.put("pickListLineNumber", ridl);
		 input.put("lotNumber", bano);
 
		 logger.debug("XtendM3Debug_lstEXT_GGN - input: " + input);
 
		 workOnCsvData(csvData, input);
	 }
 
	 /*
	  * workOnCsvData
	  *
	  * Executes a transaction via ION. This is used because the M3 program is not
	  * compatible with XtendM3.
	  *
	  * @param program MI Program
	  * @param transaction MI Transaction
	  * @param input Input fields for MI Transaction
	  * @return a Map of the JSON response
	  *
	  * at the end separate the GGN from the given content
	  * gotten data will look like * 1111111111111%3B2222222222222%3B
	  * or 1111111111111;2222222222222;
	  */
	 void workOnCsvData(final String csvDat, final Map<String, String> input) {
		 logger.debug("XtendM3Debug - start workOnContent");
		 logger.debug("XtendM3Debug_workOnContent content: " + csvDat + " Input[" + input + "]");
		 errorMessage = "";
		 maintainArrGGN("clear", "    ");
		 logger.debug("XtendM3Debug_workOnContent API - content" + csvDat);
		 
		 //unify given input data
		 String csvDatGGNS = csvDat.replaceAll("%3B",";");
		 
		 //separate the data
		 String searchValue = ";";
		 String[] ggnCodes = csvDatGGNS.split(searchValue);
		 //store every single GGN in array arrGGN
		 for (String value : ggnCodes) {
			 if (value != "") {
				 maintainArrGGN("add", value);
			 }
		 }
 
		 if (arrGGN [0] == null || arrGGN [0].trim() == "") {
			 errorMessage = "Expected jsonDat from the request but got nothing";
			 logger.debug("XtendM3_workOncsvData no GGN code - " + errorMessage);
			 return;
		 }
 
	 }
 
 
	 /* maintain array arrGGN, used to store detected GGN codes
	  * input  parameter
	  * 	@param	operation - allowed are "clear" and add
	  * 	@param	ggn       - a GGN code to be used with operation add
	  */
	 void maintainArrGGN(String operation, String ggn) {
		 logger.debug("XtendM3Debug_maintainArrGGN -OPERATION ${operation}  GGN: ${ggn}");
		 if (operation == "clear") {
			 for (int i = 0; i < 50; i++) {
				 arrGGN [i] = "";
			 }
		 }
		 if (operation == "add") {
			 if (ggn != null && ggn.trim() != "") {
				 for (int i = 0; i < 50; i++) {
					 if (arrGGN [i] == ggn) {
						 break;
					 }
					 if (arrGGN [i] == "") {
						 arrGGN [i] = ggn;
						 break;
					 }
				 }
			 }
		 }
	 }
 
 }
