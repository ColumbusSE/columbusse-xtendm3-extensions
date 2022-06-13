/**
 * This extension is used for the update of field ALWT in record OOLINE
 * for a selected DLIX
 *
 * Name: EXT101MI.UpdMHDISL.groovy
 *
 * Date         Changed By                         Description
 * 20220221     Jörg Wanning (Columbus)            Update updateMHDISL/ALWT and POPN, no standard API exist
 * 20220525     Frank Zahlten (Columbus)           get data for MHDISL directly from OOLINE, reduced input parameters
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat

public class UpdateMHDISL extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final UtilityAPI utility;
	private final MICallerAPI miCaller;

	//input fields for the MHDISL Update
	private String ooline_POPN;
	private int ooline_ALWT;
	private String ooline_ALWQ;

	//array to store DLIX data
	private long[] relevantDLIX = new long [50];

	//fields to store checked values from transaction call
	private String iCono;
	private String iRorc;
	private String iRidn;
	private String iRidl;
	private String iRidx;

	private int rorcForCO = 3;
	private int errorInt = -1;

	public UpdateMHDISL(MIAPI mi, ProgramAPI program, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger) {
		this.mi = mi;
		this.program = program;
		this.database = database;
		this.miCaller = miCaller;
		this.logger = logger;
	}

	public void main() {

		if(!validateInput()) {
			logger.debug("XtendM3Debug_UpdateMHDISL validateInput ended with false!!!!");
			mi.write()
			return;
		}

		logger.debug("XtendM3Debug_UpdateMHDISL save input data as String");

		iCono = mi.in.get("CONO");
		logger.debug("XtendM3Debug_UpdateMHDISL input field CONO : " + iCono);
		iRorc = mi.in.get("RORC");
		logger.debug("XtendM3Debug_UpdateMHDISL input field RORC : " + iRorc);
		iRidn = mi.in.get("RIDN");
		logger.debug("XtendM3Debug_UpdateMHDISL input field RIDN : " + iRidn);
		iRidl = mi.in.get("RIDL");
		logger.debug("XtendM3Debug_UpdateMHDISL input field RIDL : " + iRidl);
		iRidx = mi.in.get("RIDX");
		logger.debug("XtendM3Debug_UpdateMHDISL input field RIDX : " + iRidx);

		collectDLIX();

		updRecords();

	}

	//*****************************************************
	// validateInput - Validate entered CONO; POPN and ALWT
	//*****************************************************
	boolean validateInput(){

		logger.debug("XtendM3Debug_UpdateMHDISL Start - validateInput");

		//check company
		String company = mi.in.get("CONO")
		if (company == null) {
			mi.error("Company " + company + " must be entered")
			return false
		}
		if(validateCompany(company)){
			mi.error("Company " + company + " is invalid")
			return false
		}
		int cono = mi.in.get("CONO");

		//Checks for RORC
		String rorc = mi.in.get("RORC");
		if (rorc == "" || rorc == null){
			mi.error("Reference order cathegory has to be entered");
			return false;
		}
		if (rorc != "3" && rorc != "3.0"){
			mi.error("Actually only reference order cathegory 3 is allowed - " + rorc + "is not allowed" );
			return false;
		}

		//Check for RIDN
		String ridn = mi.in.get("RIDN");
		if (ridn == ""|| ridn == null){
			mi.error("A reference order no has to be entered");
			return false;
		}
		//checks for RIDL
		String str_ridl = mi.in.get("RIDL");
		if (str_ridl == ""|| str_ridl == null){
			mi.error("A reference order line no has to be entered");
			return false;
		}

		if (Integer.getInteger(str_ridl) == 0){
			mi.error("A reference order line no " + str_ridl + " is no allowed");
			return false;
		}
		int ridl = mi.in.get("RIDL");

		//check for RIDX
		String ridx_str = mi.in.get("RIDX");
		if (ridx_str == ""|| ridx_str == null){
			mi.error("A reference order line suffix no has to be entered");
			return false;
		}
		int ridx = mi.in.get("RIDX");

		//check order position in OOLLINE
		if (!getOOLINE(cono, ridn, ridl, ridx)) {
			return false;
		}
		return true;
	}

	//*****************************************************
	// validateCompany - Validate given or retrieved CONO
	// Input
	// Company - from Input
	//*****************************************************
	boolean validateCompany(String company){
		// Run MI program
		def parameter = [CONO: company]
		List <String> result = []
		Closure<?> handler = {Map<String, String> response ->
			return response.CONO == 0}
		miCaller.call("MNS095MI", "Get", parameter, handler)
	}


	//*****************************************************
	// get order position
	//
	// Input
	// Company - from Input
	// order no - from Input
	// order position - from Input
	// order postiion suffix- from Input
	//
	// save order position information for later use
	// OBPOPN, OBALWT, OBALWQ
	//*****************************************************

	boolean getOOLINE(int cono, String ridn, int ridl, int ridx){
		DBAction action_OOLINE = database.table("OOLINE")
				.index("00")
				.selection("OBCONO", "OBORNO", "OBPONR", "OBPOSX", "OBPOPN", "OBALWQ", "OBALWT")
				.build();
		DBContainer OLINE = action_OOLINE.createContainer();
		logger.debug("XtendM3Debug_UpdateMHDISL Start - getOOLINE");
		logger.debug("XtendM3Debug_UpdateMHDISL getOOLINE cono " + cono.toString());
		logger.debug("XtendM3Debug_UpdateMHDISL getOOLINE ridn " + ridn);
		logger.debug("XtendM3Debug_UpdateMHDISL getOOLINE ridl " + ridl.toString());
		logger.debug("XtendM3Debug_UpdateMHDISL getOOLINE ridx " + ridx.toString());
		OLINE.set("OBCONO", cono)
		OLINE.set("OBORNO", ridn)
		OLINE.set("OBPONR", ridl)
		OLINE.set("OBPOSX", ridx)
		if (!action_OOLINE.read(OLINE)) {
			logger.debug("XtendM3Debug_UpdateMHDISL Start - not found OOLINE");
			mi.error("The order line does not exist ORNO = " + ridn + " PONR " + ridl + "POSX" + ridx);
			return false;
		}
		ooline_POPN = OLINE.get("OBPOPN");
		ooline_ALWT = OLINE.get("OBALWT");
		ooline_ALWQ = OLINE.get("OBALWQ");
		return true;
	}

	//*****************************************************
	//  collectDLIX
	//  get existing DLIX records from MHDISL, using given
	//  key data, compare data POPN, ALWT and ALWQ of MHDISL
	//  with saved data from OOLINE and store the key data
	//  via maintainTabDLIX for later MHDISL update
	//*****************************************************
	def collectDLIX() {
		logger.debug("XtendM3Debug_UpdateMHDISL Start - collectDLIX");
		long mhdisl_DLIX = 0l;
		maintainTabDLIX("clear", mhdisl_DLIX);

		DBAction query_MHDISL10 = database.table("MHDISL")
				.index("10")
				.selection("URCONO", "URDLIX", "URRORC", "URRIDN", "URRIDL", "URRIDX", "URPOPN", "URALWT", "URALWQ")
				.build();
		DBContainer MHDISL10 = query_MHDISL10.getContainer();

		int cono = mi.in.get("CONO");
		int rorc = mi.in.get("RORC");
		String ridn = mi.in.get("RIDN");
		int ridl = mi.in.get("RIDL");
		int ridx = mi.in.get("RIDX");

		MHDISL10.set("URCONO", cono);
		MHDISL10.set("URRORC", rorc);
		MHDISL10.set("URRIDN", ridn);
		MHDISL10.set("URRIDL", ridl);
		MHDISL10.set("URRIDX", ridx);
		query_MHDISL10.readAll(MHDISL10, 5, orderConnectedDLIX)
	}

	Closure<?> orderConnectedDLIX = { DBContainer MHDISL10 ->
		logger.debug("XtendM3Debug_UpdateMHDISL per MHDISL closure orderConnectedDLIX");
		long mhdisl_DLIX = MHDISL10.get("URDLIX")
		String str_mhdisl_DLIX = MHDISL10.get("URDLIX")
		String mhdisl_POPN = MHDISL10.get("URPOPN")
		int mhdisl_ALWT = MHDISL10.get("URALWT")
		String mhdisl_ALWQ = MHDISL10.get("URALWQ")
		logger.debug("XtendM3Debug_UpdateMHDISL closure orderConnectedDLIX  " + str_mhdisl_DLIX);
		//		 if (mhdisl_POPN != ooline_POPN
		//		 ||  mhdisl_ALWT != ooline_ALWT
		//		 ||  mhdisl_ALWQ != ooline_ALWQ) {
		maintainTabDLIX("add", mhdisl_DLIX);
		//		 }
	}

	//*****************************************************
	// maintainTabDLIX
	// maintain array relevantDLIX, which is used to store
	// DLIX keys which should get updated
	// Input
	// - operation
	// - add
	//*****************************************************

	void maintainTabDLIX(String operation, long dlix) {
		logger.debug("XtendM3Debug_UpdateMHDISL maintainTabDLIX -OPERATION ${operation}  DLIX: ${dlix}");
		if (operation == "clear") {
			for (int i = 0; i < 50; i++) {
				relevantDLIX [i] = 0l;
			}
		}
		if (operation == "add") {
			for (int i = 0; i < 50; i++) {
				if (relevantDLIX [i] == dlix) {
					break;
				}
				if (relevantDLIX [i] == 0l) {
					relevantDLIX [i] = dlix;
					logger.debug("XtendM3Debug_UpdateMHDISL maintainTabDLIX used index ${i} ");
					break;
				}
			}
		}
	}

	//*****************************************************
	// updRecord
	// Start update process by reading DLIX from array
	// "relevantDLIX"
	//*****************************************************
	void updRecords(){
		logger.debug("XtendM3Debug_UpdateMHDISL start updRecords()");
		for (int i = 0; i < 50; i++) {
			if (relevantDLIX [i] != 0l) {
				logger.debug("XtendM3Debug_UpdateMHDISL updRecords() DLIX in relevantDLIX: " + relevantDLIX[i]);
				updMHDISL(relevantDLIX [i]);
				mi.outData.put("DLIX", relevantDLIX[i].toString());
				mi.write();
			}
		}
	}

	//*****************************************************
	// updMHDISL
	// Start update process by reading and updating MHDISL
	// 
	// input data
	// - delievery index  
	//*****************************************************
	void updMHDISL(long par_dlix){
		logger.debug("XtendM3Debug_UpdateMHDISL start updMHDISL ${par_dlix}" );
		DBAction action_MHDISL = database.table("MHDISL").index("00").selection("URCONO", "URDLIX", "URRORC", "URRIDN", "URRIDL", "URRIDX").build();
		DBContainer mhdisl = action_MHDISL.getContainer();

		int cono = mi.in.get("CONO");
		int rorc = mi.in.get("RORC");
		String ridn = mi.in.get("RIDN");
		int ridl = mi.in.get("RIDL");
		int ridx = mi.in.get("RIDX");
		long dlix = par_dlix;

		mhdisl.set("URCONO", cono);
		mhdisl.set("URDLIX", dlix);
		mhdisl.set("URRORC", rorc);
		mhdisl.set("URRIDN", ridn);
		mhdisl.set("URRIDL", ridl);
		mhdisl.set("URRIDX", ridx);

		// Read with lock
		action_MHDISL.readLock(mhdisl, updateCallBack);
	}


	//*****************************************************
	// updateCallBack
	// update MHDISL fields POPN, ALWT and ALWQ
	//*****************************************************
	Closure<?> updateCallBack = { LockedResult lockedResult ->
		logger.debug("XtendM3Debug_UpdateMHDISL Closure<?> updateCallBack");
		// Get todays date
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);

		int changeNo = lockedResult.get("URCHNO");
		int newChangeNo = changeNo + 1;

		logger.debug("XtendM3Debug_UpdateMHDISL Closure<?> updateCallBack - POPN = ${ooline_POPN} ALWT = ${ooline_ALWT} ALWQ = ${ooline_ALWQ}");
		// Update the MHDISL fields with OOLINE fields
		lockedResult.set("URPOPN", ooline_POPN);
		lockedResult.set("URALWT", ooline_ALWT);
		lockedResult.set("URALWQ", ooline_ALWQ);

		// Update changed information
		int changeddate=Integer.parseInt(formatDate);
		lockedResult.set("URLMDT", changeddate);
		lockedResult.set("URCHNO", newChangeNo);
		lockedResult.set("URCHID", program.getUser());
		lockedResult.update();
	}

	//	  public double isDouble(String stringValue) {
	//		  double returnValue = 0d
	//		  try {
	//			  returnValue = Double.parseDouble(stringValue);
	//		  } catch (NumberFormatException e) {
	//			  returnValue = -1d
	//		  }
	//		  return returnValue
	//	  }

	//	public int isInteger(String stringValue) {
	//		int returnValue = 0
	//		try {
	//			returnValue = stringValue.trim() as int;
	//		} catch (NumberFormatException e) {
	//			returnValue = errorInt;
	//		}
	//		return returnValue
	//	}

}
