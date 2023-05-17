/** Name: EXT101MI.AddUpdEXTZLN.groovy
 *
 * The API transaction EXT101MI.AddUpdEXTZLN is used to add or update EXTZLN records
 * with initial data comming from OIS101
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-17
 *  @version   1.1
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class AddUpdEXTZLN extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final DecimalFormat df6 = new DecimalFormat("0.000000");

	private int intCono = 0;
	private String iOrno = "";
	private String iPonr = "";
	private int intPonr = 0;
	private String iPosx = "";
	private int intPosx = 0;
	private String iSapr = "";
	private double doubleSapr;
	private String iAcva = "";
	private double doubleAcva;
	private String iQtst = "";
	private int countQMSTTP = 0;
	
	private String oAncl = "     ";
	private String oTx40 = "                                         ";
	private double doubleZero = 0d;
	private double doubleEvmn = 0d;
	private double doubleQtrs = 0d;
	private String oStat = "  ";
	private boolean decimalError = false;
	private boolean modeUpdate = false;

	// Constructor
	public AddUpdEXTZLN(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {
		logger.debug("EXT101MI.CrtZLN Start main()");
		
		intCono = program.LDAZD.get("CONO");
		
		iOrno = mi.in.get("ORNO");
		iPonr = mi.in.get("PONR");
		iPosx = mi.in.get("POSX");
		iSapr = mi.in.get("SAPR");
		iAcva = mi.in.get("ACVA");
		iQtst = mi.in.get("QTST");
		
		if (!validateInput()) {
			logger.debug("EXT101MI.CrtZLN validateInput ended with false!!!!");
			return;
		}

		initRemaingOutputFields();

		if (modeUpdate) {
			updateDbRecord();
		} else {
			addDbRecord();
		}
	}

	/**
	 * validateInput
	 * validate data from the input
	 */
	boolean validateInput() {
		logger.debug("XEXT101MI/AddUpdEXTZLN validateInput started");		

		//check ORNO
		if (iOrno == null) {
			iOrno = "";
		}
		iOrno.trim();
		if (iOrno == ""){
			mi.error("An order no has to be entered");
			return false;
		}

		//check PONR position no
		if (iPonr == null) {
			iPonr = "";
		}
		iPonr.trim();
		if (iPonr == ""){
			mi.error("Position no has to be entered");
			return false;
		}
		intPonr = mi.in.get("PONR");

		//check POSX position no suffix
		if (iPosx == null) {
			iPosx = "";
		}
		iPosx.trim();
		if (iPosx == ""){
			mi.error("Position no suffix has to be entered");
			return false;
		}
		intPosx = mi.in.get("POSX");
		
		//check if customer order line exists
		Optional<DBContainer> OOLINE = readOOLINE();
		if(!OOLINE.isPresent()){
			mi.error("Customer order position doesn't exist");
			return false;
		}
		
		//check if record already exists
		Optional<DBContainer> EXTZLN = readEXTZLN();
		if(EXTZLN.isPresent()){
			modeUpdate = true;
		} else {
			modeUpdate = false;
		}

		//check ORNO
		if (iQtst == null) {
			iQtst = "";
		}
		iQtst.trim();
		
		if (!modeUpdate && iQtst == ""){
			mi.error("A value for TEST has to be entered");
			return false;
		}
		if (!validateQTST()) {
			return false;
		}

		//check or initialize ACVA
		if (iAcva == null) {
			iAcva = "";
		}
		iAcva.trim();
		if (iAcva == ""){
			iAcva = "0.00";
		}
		doubleAcva = setDecimals(iAcva, 2);
		if (decimalError) {
			return;
		}

		if (!modeUpdate) {
			if (!iQtst.trim().isEmpty()
			&&  doubleAcva == 0d) {
				mi.error("For field 'ACVA - actual value' a numeric value has to be entered");
				return false;
			}
		}

		//check or initialize iSapr
		if (iSapr == null) {
			iSapr = "";
		}
		iSapr.trim();
		if (iSapr == ""){
			iSapr = "0.000000";
		}
		doubleSapr = setDecimals(iSapr, 6) ;
		if (decimalError) {
			return;
		}

		//all checks are done and ok
		return true;

	}

	/**
	 * initRemaingOutputFields
	 * 
	 * initialize remaining output fields, used when adding a new record to EXTZLN  
	 */
	public void initRemaingOutputFields() {
		oAncl = "     ";
		oTx40 = "                                         ";
		doubleEvmn = setDecimals("0.0", 6);
		doubleQtrs = setDecimals("0.0", 6);
		oStat = "  ";
	}

	/**
	 * Set Decimals -
	 * Input
	 * iValue  - a string from Input
	 * outDeciamls - count of decimals used for the fix output format
	 * Output
	 * doubleValue - with correct count of decimals
	 */
	double setDecimals(String iValue, int outDecimals) {
		decimalError = false;
		double doubleVal = 0d;
		String strOut = iValue.trim();
		int lengthOut = strOut.length();
		logger.debug("XEXT101MI/AddUpdEXTZLN strOut: " + strOut);
		try {
			doubleVal = Double.parseDouble(strOut);
		} catch(IOException e) {
			mi.error("numeric value of " + strOut + " is not valid!" );
			decimalError = true;
			doubleVal = 0d;
			return doubleVal;
		}
		if (outDecimals == 2) {
			doubleVal = Double.parseDouble(df2.format(doubleVal));
		}
		if (outDecimals == 6) {
			doubleVal = Double.parseDouble(df6.format(doubleVal));
		}
		return doubleVal;
	}

	/**
	 * validate QTST - Validate given LI class
	 * Input
	 * Company - from LDA
	 * QTST - from Input
	 *
	 * Output
	 * returns true or false, depends on DB read operation result for a single record of file QMSTTP
	 */
	private boolean validateQTST() {
		logger.debug("XEXT101MI/AddUpdEXTZLN validateQTST started! QTST: " + iQtst);
		countQMSTTP == 0;
		DBAction action = database.table("QMSTTP")
				.index("00")
				.selection("QTQTST")
				.build();
		DBContainer QMSTTP = action.createContainer();
		QMSTTP.set("QTCONO", intCono);
		QMSTTP.set("QTQTST", iQtst);
		if (!action.readAll(QMSTTP, 02, countRecQMSTTP)) {
			mi.error("LI Class {$iQtst} does not exist");
			return false;
		}
		if (countQMSTTP != 0) {
			return true;
		}
		return false;
	}
	
	Closure<?> countRecQMSTTP = { DBContainer ext ->
		 if (ext.get("QTQTST") == iQtst) {
			 countQMSTTP++;
		 }
	}

	/**
	 * addDbRecord
	 * add a record to file EXTZLN
	 */
	private void addDbRecord() {
		logger.debug("XEXT101MI/AddUpdEXTZLN addDbRecord");
		DBAction action_EXTZLN = database.table("EXTZLN")
				.index("00")
				.build();
		DBContainer EXTZLN = action_EXTZLN.createContainer();
		EXTZLN.set("EXCONO", intCono);
		EXTZLN.set("EXORNO", iOrno);
		EXTZLN.set("EXPONR", intPonr);
		EXTZLN.set("EXPOSX", intPosx);

		EXTZLN.set("EXSAPR", doubleSapr);
		EXTZLN.set("EXANCL", oAncl);
		EXTZLN.set("EXACVA", doubleAcva);
		EXTZLN.set("EXQTST", iQtst);
		EXTZLN.set("EXTX40", oTx40);
		EXTZLN.set("EXEVMN", doubleEvmn);
		EXTZLN.set("EXQTRS", doubleQtrs);
		EXTZLN.set("EXSTAT", oStat);

		EXTZLN.set("EXCHID", program.getUser());
		EXTZLN.set("EXCHNO", 1);
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		String formatTime = now.format(format2);

		//Converting String into int using Integer.parseInt()
		int regdate=Integer.parseInt(formatDate);
		int regtime=Integer.parseInt(formatTime);
		EXTZLN.set("EXRGDT", regdate);
		EXTZLN.set("EXLMDT", regdate);
		EXTZLN.set("EXRGTM", regtime);
		action_EXTZLN.insert(EXTZLN);
		logger.debug("EXT101MI/insert - Orno = " + iOrno + " Ponr: " + intPonr.toString() + " Posx " + intPosx.toString());
	}

	/**
	 * updateDbRecord
	 * Start update process by reading and updating EXTZLN
	 *
	 * input data
	 * - EXTZLN key fields index 00
	 */
	void updateDbRecord(){
		logger.debug("XEXT101MI/AddUpdEXTZLN start- updateDbRecord" );
		DBAction action_EXTZLN = database.table("EXTZLN")
				.index("00")
				.selection("EXQTST", "EXACVA", "EXSAPR")
				.build();
		DBContainer EXTZLN = action_EXTZLN.getContainer();

		EXTZLN.set("EXCONO", intCono);
		EXTZLN.set("EXORNO", iOrno);
		EXTZLN.set("EXPONR", intPonr);
		EXTZLN.set("EXPOSX", intPosx);

		// Read with lock
		action_EXTZLN.readLock(EXTZLN, updateCallBack);
	}


	/**
	 * updateCallBack
	 * update EXTZLN/VKPM
	 */
	Closure<?> updateCallBack = { LockedResult lockedResult ->
		logger.debug("XEXT101MI/AddUpdEXTZLN Closure<?> updateCallBack");
		// Get todays date
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);

		int changeNo = lockedResult.get("EXCHNO");
		int newChangeNo = changeNo + 1;

		// Update the EXTZLN fields
		String checkString = mi.in.get("QTST");
		if (!isNullOrEmpty(checkString)) {
			lockedResult.set("EXQTST", iQtst);
			logger.debug("EXT101MI/update EXTZLN QTST: " + iQtst);
		}
		checkString = mi.in.get("ACVA");
		if (!isNullOrEmpty(checkString)) {
			lockedResult.set("EXACVA", doubleAcva);
			logger.debug("EXT101MI/update EXTZLN ACVA: " + iAcva);
		}
		checkString = mi.in.get("SAPR");
		if (!isNullOrEmpty(checkString)) {
			lockedResult.set("EXSAPR", doubleSapr);
			logger.debug("EXT101MI/update EXTZLN SAPR: " + iSapr);
		}

		// Update changed information
		int changeddate=Integer.parseInt(formatDate);
		lockedResult.set("EXLMDT", changeddate);
		logger.debug("EXT101MI/update EXTZLN " + " EXLMDT: " + changeddate);
		lockedResult.set("EXCHNO", newChangeNo);
		logger.debug("EXT101MI/update EXTZLN " + " EXCHNO: " + newChangeNo);
		lockedResult.set("EXCHID", program.getUser());
		logger.debug("EXT101MI/update EXTZLN " + " EXCHID: " +  program.getUser());
		lockedResult.update();
		logger.debug("EXT101MI/update Orno = " + iOrno + " Ponr: " + intPonr.toString() + " Posx " + intPosx.toString());
	}
	
	/**
	 * isNullOrEmpty - check if given field is null or empty
	 * Input
	 * chkValue - String to be checked
	 */
	 public  boolean isNullOrEmpty(String chkValue) {
		 if(chkValue != null && !chkValue.isEmpty()) {
			 return false;
		 }
		 return true;
	 }
	
	/**
	 * readEXTZLN
	 * get and return a record from file EXTZLN
	 */
	private Optional<DBContainer> readEXTZLN() {
		logger.debug("XEXT101MI/AddUpdEXTZLN getEXTZLN");
		DBAction action_EXTZLN = database.table("EXTZLN")
			.index("00")
			.selectAllFields()
			.build();
		DBContainer EXTZLN = action_EXTZLN.getContainer();

		logger.debug("XEXT101MI/AddUpdEXTZLN readEXTZLN KEYFIELDS EXCONO" + intCono.toString()
		+ " EXORNO: " + iOrno
		+ " EXPONR: " + intPonr.toString()
		+ " EXPOSX: " + intPosx.toString());

		// Key value for read
		EXTZLN.set("EXCONO", intCono);
		EXTZLN.set("EXORNO", iOrno);
		EXTZLN.set("EXPONR", intPonr);
		EXTZLN.set("EXPOSX", intPosx);

		// Read
		if (action_EXTZLN.read(EXTZLN)) {
			logger.debug("XEXT101MI/AddUpdEXTZLN readEXTZLN record is existing");
			return Optional.of(EXTZLN);
		}
		logger.debug("XEXT101MI/AddUpdEXTZLN readEXTZLN record is not existing");
		return Optional.empty();
	}
	
	/**
	 * readOOLINE
	 * get and return a record from file OOLINE
	 */
	private Optional<DBContainer> readOOLINE() {
		logger.debug("XEXT101MI/AddUpdEXTZLN readOOLINE");
		DBAction action_OOLINE = database.table("OOLINE")
			.index("00")
			.selectAllFields()
			.build();
		DBContainer OOLINE = action_OOLINE.getContainer();

		logger.debug("XEXT101MI/AddUpdEXTZLN readOOLINE Key OBCONO: " + intCono.toString()
		+ " OBORNO: " + iOrno
		+ " OBPONR: " + intPonr.toString()
		+ " OBPOSX: " + intPosx.toString());

		// Key value for read
		OOLINE.set("OBCONO", intCono);
		OOLINE.set("OBORNO", iOrno);
		OOLINE.set("OBPONR", intPonr);
		OOLINE.set("OBPOSX", intPosx);

		// Read
		if (action_OOLINE.read(OOLINE)) {
			logger.debug("XEXT101MI/AddUpdEXTZLN readOOLINE record is existing");
			return Optional.of(OOLINE);
		}
		logger.debug("XEXT101MI/AddUpdEXTZLN readOOLINE record is not existing");
		return Optional.empty();
	}

}
