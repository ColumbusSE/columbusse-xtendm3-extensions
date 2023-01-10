/* This API transacation Add is used to write/update data into customer specific file EXTCLC
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2022-10-26
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class Add extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df = new DecimalFormat("0.00");

	private String iCono = "";
	private int intCono = 0;
	private String iArtn = "";    //Art-Nr.
	private String iZiel = "";    //Zielland
	private String iUldp = "";    //Urprungsland pr�ferenziell
	private String iZtnr = "";    //Zolltarifnummer
	private int intZtnr = 0;
	private String iUldh = "";    //Ursprungsland handelsrechtlich
	private String iVkpm = "";    //Mindestverkaufspreis
	private double doubleVkpm = 0d;



	// Constructor
	public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {

		iCono =  program.LDAZD.get("CONO");
		iArtn =  mi.in.get("ARTN");
		iZiel =  mi.in.get("ZIEL");
		iUldp =  mi.in.get("ULDP");
		iZtnr =  mi.in.get("ZTNR");
		iUldh =  mi.in.get("ULDH");
		iVkpm =  mi.in.get("VKPM");

		logger.debug("XEXT800MI/ADD input field CONO : " + iCono);
		logger.debug("XEXT800MI/ADD input field ARTN : " + iArtn);
		logger.debug("XEXT800MI/ADD input field ZIEL : " + iZiel);
		logger.debug("XEXT800MI/ADD input field ULDP : " + iUldp);
		logger.debug("XEXT800MI/ADD input field ZTNR : " + iZtnr);
		logger.debug("XEXT800MI/ADD input field ULDH : " + iUldh);
		logger.debug("XEXT800MI/ADD input field VKPM : " + iVkpm);

		if (!validateInput()) {
			logger.debug("XEXT800MI/ADD validateInput ended with false!!!!");
			mi.write();
			return;
		}

		Optional<DBContainer> EXTCLC = readEXTCLC()
		if(EXTCLC.isPresent()){
			updateDbRecord();
		} else {
			addDbRecord();
		}
		createOutput();

	}

	/**
	* validateInput
	* validate data from the input
	*/
	boolean validateInput() {
		logger.debug("XEXT800MI/ADD validateInput started");
		//check CONO
		if (iCono == null) {
			mi.error("Company " + iCono + " is not valid")
			return false;
		}
		if(validateCompany(iCono)){
			mi.error("Company " + iCono + " is invalid")
			return false;
		}
		intCono = program.LDAZD.get("CONO");

		//check ARTN Artikelnummer
		if (iArtn == null) {
			iArtn = "";
		}
		iArtn.trim();
		if (iArtn == ""){
			mi.error("An item no has to be entered");
			return false;
		}
		if (!validateMITMAS()) {
			return false;
		}

		//check iZiel Zielland
		if (iZiel == null) {
			iZiel = "";
		}
		iZiel.trim();
		if (iZiel == ""){
			mi.error("A Zielland ZIEL has to be entered");
			return false;
		}
		if (!validateCSCD(iZiel, "Zielland ZIEL" )) {
			return false;
		}

		//check iUldp Ursprungsland pr�ferentiell
		if (iUldp == null) {
			iUldp = "";
		}
		iUldp.trim();
		if (iUldp == ""){
			mi.error("Ursprungsland pr�ferentiell has to be entered");
			return false;
		}
		if (!validateCSCD(iUldp,  "Ursprungsland ULDP")) {
			return false;
		}

		//check iZtnr Zolltarifnummer
		if (iZtnr == null) {
			iZtnr = "";
		}
		iZtnr.trim();
		if (iZtnr == ""){
			mi.error("Zolltariufnummer has to be entered");
			return false;
		}
		if (!validateCSNO(iZtnr, "Zolltarifnummer ZTNR")) {
			return false;
		}
		intZtnr = Integer.parseInt(iZtnr.trim());

		//check iUldh Ursprungsland handelsrechtlich
		if (iUldh == null) {
			iUldh = "";
		}
		iUldh.trim();
		if (iUldh == ""){
			mi.error("Ursprungsland handelsrechtlich has to be entered");
			return false;
		}
		if (!validateCSCD(iUldh, "Ursprungsland handelsrechtlich ULDH")) {
			return false;
		}

		//check iVkpm Mindestverkaufspreis
		if (iVkpm == null) {
			iVkpm = "";
		}
		iVkpm.trim();
		if (iVkpm == ""){
			mi.error("Mindestverkaufspreis VKPM has to be entered");
			return false;
		}

		doubleVkpm = 0d;
		double doubleVal = 0d;

		//numeric data will be given
		// - with fixed length, here ten digits (example: 0000012345 = 123.45)
		// - without a decimal sign
		// - the last 2 digits represent the decimals
		// - zero value is allowed
		String strVKPM = iVkpm.trim();
		int lengthVPKM = strVKPM.length();
		String newStrVKPM = strVKPM.substring(0, lengthVPKM - 2) + '.' + strVKPM.substring(lengthVPKM - 2);
		logger.debug("XEXT800MI/ADD newStrVKPM: " + newStrVKPM );
		doubleVal = Double.parseDouble(newStrVKPM);
		doubleVkpm = Double.parseDouble(df.format(doubleVal));

		//all checks are done and ok
		return true;

	}


	/**
	* validateCompany - Validate given or retrieved CONO
	* Input
	* Company - from Input
	*/
	boolean validateCompany(String company){
		logger.debug("XEXT800MI/ADD validateCompany started! company: " + iCono);
		// Run MI program
		def parameter = [CONO: company];
		List <String> result = [];
		Closure<?> handler = {Map<String, String> response ->
			return response.CONO == 0};
		miCaller.call("MNS095MI", "Get", parameter, handler);
	}

	/**
	* validateMITMAS - Validate given item no
	* Input
	* Company - from Input
	* Item no - from Input
	*/
	private boolean validateMITMAS() {
		logger.debug("XEXT800MI/ADD validateMITMAS started! company: " + iCono);
		DBAction action_MITMAS = database.table("MITMAS")
				.index("00")
				.build();
		DBContainer MITMAS = action_MITMAS.createContainer();
		logger.debug("XEXT800MI/ADD validateMITMAS CONO: " + iCono + " ARTN: " + iArtn );
		MITMAS.set("MMCONO", intCono);
		MITMAS.set("MMITNO", iArtn);
		if (!action_MITMAS.read(MITMAS)) {
			mi.error("The item {$iArtn} does not exist in file MITMAS");
			return false;
		}
		return true;
	}

	/**
	* isNullOrEmpty - check given string value if it is null
	*                 or if the content is emtpy
	* Input
	* key   - String value to be analyzed
	*/
	public  boolean isNullOrEmpty(String key) {
		if(key != null && !key.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	* validate CSCD - Validate given country code
	* Input
	* Company - from LDA
	* country code - from Input ZIEL, ULDP or ULDH
	* dexcription - for the output of the correct field description
	*/
	private boolean validateCSCD(String iStky, String iDescription) {
		logger.debug("XEXT800MI/ADD validateCSCD started! country code: " + iStky);
		DBAction action_CSYTAB = database.table("CSYTAB")
				.index("00")
				.build();
		DBContainer CSYTAB = action_CSYTAB.createContainer();
		logger.debug("XEXT800MI/ADD validateCSCD STKY: " + iStky);
		CSYTAB.set("CTCONO", intCono);
		CSYTAB.set("CTDIVI", "   ");
		CSYTAB.set("CTSTCO", "CSCD");
		CSYTAB.set("CTSTKY", iStky);
		CSYTAB.set("CTLNCD", "  ");
		if (!action_CSYTAB.read(CSYTAB)) {
			mi.error(iDescription + " " + iStky +" does not exist in CRS045");
			return false;
		}
		return true;
	}

	/**
	* validate CSNO - Validate given custom statistics no
	* Input
	* Company - from LDA
	* CSNO - from Input ZTNR (Zolltarifnummer)
	*/
	private boolean validateCSNO(String iCsno, String iDescription) {
		logger.debug("XEXT800MI/ADD validateCSNO started! country code: " + iCsno);
		DBAction action_CSYCSN = database.table("CSYCSN")
				.index("00")
				.build();
		DBContainer CSYCSN = action_CSYCSN.createContainer();
		logger.debug("XEXT800MI/ADD validateCSNO STKY: " + iCsno);
		CSYCSN.set("CKCONO", intCono);
		CSYCSN.set("CKCSNO", iCsno);
		if (!action_CSYCSN.read(CSYCSN)) {
			mi.error(iDescription + " {$iCsno} does not exist in CRS128");
			return false;
		}
		return true;
	}

	/**
	* createOutput
	* create output information with data from input and
	* modified sales price information VKPM
	*/
	private void createOutput() {
		logger.debug("XEXT800MI/ADD createOutput");
		mi.outData.put("CONO", iCono);
		mi.outData.put("ARTN", iArtn);
		mi.outData.put("ZIEL", iZiel);
		mi.outData.put("ULDP", iUldp);
		mi.outData.put("ULDH", iUldh);
		mi.outData.put("ZTNR", iZtnr);
		mi.outData.put("VKPM", String.valueOf(doubleVkpm));
		mi.write();
	}

	/**
	* addDbRecord
	* add the data to file EXTCLC
	*/
	private void addDbRecord() {
		logger.debug("XEXT800MI/ADD addDbRecord");
		DBAction action = database.table("EXTCLC")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXTCLC = action.createContainer();
		EXTCLC.set("EXCONO", intCono);
		EXTCLC.set("EXARTN", iArtn);
		EXTCLC.set("EXZIEL", iZiel);
		EXTCLC.set("EXULDP", iUldp);
		EXTCLC.set("EXULDH", iUldh);
		EXTCLC.set("EXZTNR", intZtnr);
		EXTCLC.set("EXVKPM", doubleVkpm);

		EXTCLC.set("EXCHID", program.getUser())
		EXTCLC.set("EXCHNO", 1)
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		String formatTime = now.format(format2);

		//Converting String into int using Integer.parseInt()
		int regdate=Integer.parseInt(formatDate);
		int regtime=Integer.parseInt(formatTime);
		EXTCLC.set("EXRGDT", regdate);
		EXTCLC.set("EXLMDT", regdate);
		EXTCLC.set("EXRGTM", regtime);
		action.insert(EXTCLC);
		logger.debug("XEXT800MI/insert - ARTN = " + iArtn + " VKPM: " + doubleVkpm);
	}

  /**
	* readEXTCLC
	* get and return a record from file EXTCLC
	*/
	private Optional<DBContainer> readEXTCLC() {
		logger.debug("XEXT800MI/ADD getEXTCLC");
		DBAction action = database.table("EXTCLC")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXTCLC = action.getContainer();
		
		logger.debug("XEXT800MI/ADD readEXTCLC KEYFIELDS EXCONO" + intCono.toString()
			+ " EXARTN: " + iArtn
			+ " EXZIEL: " + iZiel
			+ " EXULDP: " + iUldp
			+ " EXULDH: " + iUldh
			+ " EXZTNR: " + intZtnr.toString());

		// Key value for read
		EXTCLC.set("EXCONO", intCono);
		EXTCLC.set("EXARTN", iArtn);
		EXTCLC.set("EXZIEL", iZiel);
		EXTCLC.set("EXULDP", iUldp);
		EXTCLC.set("EXULDH", iUldh);
		EXTCLC.set("EXZTNR", intZtnr);

		// Read
		if (action.read(EXTCLC)) {
			logger.debug("XEXT800MI/ADD readEXTCLC record is existing");
			return Optional.of(EXTCLC);
		}
		logger.debug("XEXT800MI/ADD readEXTCLC record is not existing");
		return Optional.empty();
	}
	
	/**
	 * updateDbRecord
	 * Start update process by reading and updating EXTCLC
	 *
	 * input data
	 * - EXTCLC key fields index 00
	 */
	 void updateDbRecord(){
		 logger.debug("XEXT800MI/ADD start- updateDbRecord" );
		 DBAction action_EXTCLC = database.table("EXTCLC")
		 .index("00")
		 .selection("EXCONO", "EXARTN", "EXZIEL", "EXULDP", "EXULDH", "EXZTNR")
		 .build();
		 DBContainer EXTCLC = action_EXTCLC.getContainer();
 
		 EXTCLC.set("EXCONO", intCono);
		 EXTCLC.set("EXARTN", iArtn);
		 EXTCLC.set("EXZIEL", iZiel);
		 EXTCLC.set("EXULDP", iUldp);
		 EXTCLC.set("EXULDH", iUldh);
		 EXTCLC.set("EXZTNR", intZtnr);
 
		 // Read with lock
		 action_EXTCLC.readLock(EXTCLC, updateCallBack);
	 }
 
 
	 /**
	 * updateCallBack
	 * update EXTCLC/VKPM
	 */
	 Closure<?> updateCallBack = { LockedResult lockedResult ->
		 logger.debug("XEXT800MI/ADD Closure<?> updateCallBack");
		 // Get todays date
		 LocalDateTime now = LocalDateTime.now();
		 DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		 String formatDate = now.format(format1);
 
		 int changeNo = lockedResult.get("EXCHNO");
		 int newChangeNo = changeNo + 1;
 
		 // Update the EXTCLC fields
		 lockedResult.set("EXVKPM", doubleVkpm);
 
		 // Update changed information
		 int changeddate=Integer.parseInt(formatDate);
		 lockedResult.set("EXLMDT", changeddate);
		 logger.debug("XEXT800MI/update - ARTN = " + " EXLMDT: " + changeddate);
		 lockedResult.set("EXCHNO", newChangeNo);
		 logger.debug("XEXT800MI/update - ARTN = " + " EXCHNO: " + newChangeNo);
		 lockedResult.set("EXCHID", program.getUser());
		 logger.debug("XEXT800MI/update - ARTN = " + " EXCHID: " +  program.getUser());
		 lockedResult.update();
		 logger.debug("XEXT800MI/update - ARTN = " + lockedResult.get("EXARTN") + " VKPM: " + doubleVkpm);
	 }

}
