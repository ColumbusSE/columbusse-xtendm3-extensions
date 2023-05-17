/** Name: EXT101MI.CalcProRata.groovy
 *
 * The API transaction EXT101MI.CalcProRata is used to recalculate the price information of a sales position
 * based on allocated quantities and their QMS results
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-22
 *  @version   1.1
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.beans.Expression
import java.text.DecimalFormat;
import M3.DBContainer;

public class CalcProRata extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df1 = new DecimalFormat("0.0");
	private static final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final DecimalFormat df3 = new DecimalFormat("0.000");
	private static final DecimalFormat df4 = new DecimalFormat("0.0000");
	private static final DecimalFormat df5 = new DecimalFormat("0.00000");
	private static final DecimalFormat df6 = new DecimalFormat("0.000000");

	private int intCono = 0;
	private String iOrno = "";
	private String iPonr = "";
	private int intPonr = 0;
	private String iPosx = "";
	private int intPosx  = 0;
	private String iTtyp = "";
	private int intTtyp = 0;

	private boolean foundError = false;

	private double extzlnSapr = 0d;
	private String extzlnAncl = "";
	private double extzlnAcva = 0d;
	private String extzlnQtst = "";
	private String extzlnTx40 = "";
	private double extzlnEvmn = 0d;
	private double extzlnQtrs = 0d;
	private String extzlnStat = "";

	private String mitaloItno = "";
	private String mitaloBano = "";
	private Double mitaloAlqt = 0d;

	private double qmsrqtQtrs = 0d;
	private double chkQtrs = 0d;
	private int countRecQMSRQT = 0;
	private String qmsrqtQtst = "";

	private String milomaFaci = "";

	private int ooheadDccd = 0;
	private int constTTYP = 31;

	private double oolineSapr = 0d;
	private double oolineCofa = 0d;
	private int oolineSacd = 0;

	private int countMitalo = 0;
	private int intRido = 0;
	private int maxCount = 500;
	private int todaysDate = 0;
	private String stringTodaysDate = "";

	private double sumSapr = 0d;
	private double saveSapr = 0d;
	private double sumAlqt = 0d;
	private double doublePxnum = 0d;



	// Constructor
	public CalcProRata(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {

		intCono = program.LDAZD.get("CONO");
		
		iOrno =  mi.in.get("ORNO");
		iPonr =  mi.in.get("PONR");
		iPosx =  mi.in.get("POSX");
		iTtyp =  mi.in.get("TTYP");

		logger.debug("EXT101MI/CalcProRata input field CONO : " + intCono.toString());
		logger.debug("EXT101MI/CalcProRata input field ORNO : " + iOrno);
		logger.debug("EXT101MI/CalcProRata input field PONR : " + iPonr);
		logger.debug("EXT101MI/CalcProRata input field POSX : " + iPosx);
		logger.debug("EXT101MI/CalcProRata input field TRTP : " + iTtyp);

		// Get todays date
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		stringTodaysDate = now.format(format1);
		todaysDate = Integer.parseInt(stringTodaysDate);

		foundError = false;
		if (!validateInput()) {
			if (foundError) {
				logger.debug("EXT101MI/CalcProRata validateInput ended with false and error !!!!");
				mi.write();
			}
			return;
		}

		if (!sumUpAllocationResults()) {
			if (foundError) {
				logger.debug("EXT101MI/CalcProRata sumUpAllocationResults ended with false and error !!!!");
				mi.write();
			}
			return;
		}

		updatePriceInformation();

		return;
	}

	/**
	 * validateInput
	 *
	 * validate data from the input
	 */
	boolean validateInput() {
		logger.debug("EXT101MI/CalcProRata validateInput started");

		// check if a TRTP is given and when iTtyp != 31
		// stop further activities in this transaction
		if (iTtyp == null) {
			iTtyp = "";
		}
		iTtyp.trim();
		if (iTtyp != "") {
			intTtyp = mi.in.get("TTYP");
			if (intTtyp != constTTYP){
				mi.error("Transaction type " + iTtyp + " is not valid");
				foundError = true;
				return false;
			}
		}
		

		//check ARTN Artikelnummer
		if (iOrno == null) {
			iOrno = "";
		}
		iOrno.trim();
		if (iOrno == ""){
			mi.error("An order no ORNO has to be entered");
			foundError = true;
			return false;
		}

		//check iPonr Position no
		if (iPonr == null) {
			iPonr = "";
		}
		iPonr.trim();
		if (iPonr == ""){
			mi.error("Position no has to be entered");
			foundError = true;
			return false;
		}
		intPonr = mi.in.get("PONR");

		//check iPosx Position no suffix
		if (iPosx == null) {
			iPosx = "";
		}
		iPosx.trim();
		if (iPosx == ""){
			mi.error("Position no suffix has to be entered");
			foundError = true;
			return false;
		}
		intPosx = mi.in.get("POSX");

		Optional<DBContainer> EXTZLN = readEXTZLN();
		if(!EXTZLN.isPresent()){
			logger.debug("EXT101MI/readEXTZLN The record doesn't exist in EXTZLN - no error!!");
			return false;
		}

		Optional<DBContainer> OOLINE = readOOLINE();
		if(!OOLINE.isPresent()){
			logger.debug("EXT101MI/readOOLINE The record doesn't exist in OOLINE - no error!!");
			return false;
		}
		
		Optional<DBContainer> OOHEAD = readOOHEAD();
		if(!OOHEAD.isPresent()){
			logger.debug("EXT101MI/readOOHEAD The record doesn't exist in OOHEAD - no error!!");
			return false;
		}

		//all checks are done and ok
		return true;
	}

	/**
	 *   readEXTZLN - read a record for given key values
	 */
	private Optional<DBContainer> readEXTZLN() {
		logger.debug("EXT101MI/readEXTZLN start");
		DBAction action = database.table("EXTZLN")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXTZLN = action.getContainer();

		logger.debug("EXT101MI/readEXTZLN readEXTZLN KEYFIELDS EXCONO" + intCono.toString()
				+ " EXORNO: " + iOrno
				+ " EXPONR: " + intPonr.toString()
				+ " EXPOSX: " + intPosx.toString());

		// Key value for read
		EXTZLN.set("EXCONO", intCono);
		EXTZLN.set("EXORNO", iOrno);
		EXTZLN.set("EXPONR", intPonr);
		EXTZLN.set("EXPOSX", intPosx);

		// Read
		if (action.read(EXTZLN)) {
			logger.debug("EXT101MI/readEXTZLN EXTZLN record is existing");
			extzlnSapr = EXTZLN.get("EXSAPR");
			extzlnAncl = EXTZLN.get("EXANCL");
			extzlnAcva = EXTZLN.get("EXACVA");
			extzlnQtst = EXTZLN.get("EXQTST");
			extzlnTx40 = EXTZLN.get("EXTX40");
			extzlnEvmn = EXTZLN.get("EXEVMN");
			extzlnQtrs = EXTZLN.get("EXQTRS");
			extzlnStat = EXTZLN.get("EXSTAT");
			extzlnSapr = EXTZLN.get("EXSAPR");
			return Optional.of(EXTZLN);
		}

		logger.debug("EXT101MI/readEXTZLN EXTZLN record is not existing");
		return Optional.empty();
	}

	/**
	 *   readOOLINE - read record for given key values
	 */
	private Optional<DBContainer> readOOLINE() {
		logger.debug("EXT101MI/CalcProRata readOOLINE Start");
		DBAction action = database.table("OOLINE")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer OOLINE = action.getContainer();

		logger.debug("EXT101MI/CalcProRata readOOLINE KEYFIELDS EXCONO" + intCono.toString()
				+ " OBORNO: " + iOrno
				+ " OBPONR: " + intPonr.toString()
				+ " OBPOSX: " + intPosx.toString());

		// Key value for read
		OOLINE.set("OBCONO", intCono);
		OOLINE.set("OBORNO", iOrno);
		OOLINE.set("OBPONR", intPonr);
		OOLINE.set("OBPOSX", intPosx);

		// Read
		if (action.read(OOLINE)) {
			logger.debug("EXT101MI/CalcProRata readOOLINE record is existing");
			oolineSapr = OOLINE.get("OBSAPR");
			oolineCofa = OOLINE.get("OBCOFA");
			oolineSacd = OOLINE.get("OBSACD");
			return Optional.of(OOLINE);
		}

		logger.debug("EXT101MI/CalcProRata readOOLINE record is not existing");
		return Optional.empty();
	}
	
	/**
	 *   readOOHEAD - read record for given key values
	 */
	private Optional<DBContainer> readOOHEAD() {
		logger.debug("EXT101MI/CalcProRata readOOLINE Start");
		DBAction action = database.table("OOHEAD")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer OOHEAD = action.getContainer();

		logger.debug("EXT101MI/CalcProRata readOOHEAD KEYFIELDS EXCONO" + intCono.toString()
				+ " OAORNO: " + iOrno);

		// Key value for read
		OOHEAD.set("OACONO", intCono);
		OOHEAD.set("OAORNO", iOrno);

		// Read
		if (action.read(OOHEAD)) {
			logger.debug("EXT101MI/CalcProRata readOOLINE record is existing");
			ooheadDccd = OOHEAD.get("OADCCD");
			return Optional.of(OOHEAD);
		}

		logger.debug("EXT101MI/CalcProRata readOHEAD record is not existing");
		return Optional.empty();
	}

	/**
	 * sumUpAllocationResults
	 * 
	 * read all MITALO data, count the records and sum up allocated qauntity   
	 * use QMSRQT data of the called closure for a validation of existing QMS data per
	 * MITALO record.
	 *  
	 * Result will be the new price, which is in relation to the result of the concrete 
	 * QMS activity per MITALO quantity and not based on an theortical average result, as
	 * used when entering a new customer order postion.
	 */
	boolean sumUpAllocationResults() {
		logger.debug("EXT101MI/CalcProRata sumUpAllocationResults started");

		sumAlqt = 0d;
		sumSapr = 0d;
		countMitalo = 0;
		mitaloAlqt = 0d;

		DBAction action_MITALO = database.table("MITALO")
				.index("20")
				.selection("MQRIDL", "MQRIDX", "MQITNO", "MQBANO", "MQALQT")
				.build();
		DBContainer MITALO = action_MITALO.getContainer();

		MITALO.set("MQCONO", intCono);
		MITALO.set("MQTTYP", constTTYP);
		MITALO.set("MQRIDN", iOrno);
		MITALO.set("MQRIDO", intRido);
		// read all MITALO data for given key fields
		action_MITALO.readAll(MITALO, 4, maxCount, workOnMITALO);

		if (foundError) {
			logger.debug("EXT101MI/CalcProRata sumUpAllocationResults stops because of foundError");
			return false;
		}
		if (countMitalo == 0) {
			logger.debug("EXT101MI/CalcProRata sumUpAllocationResults stops because of countMitalo = 0");
			return false;
		}

		return true;

	}

	Closure<?> workOnMITALO = { DBContainer MITALO ->
		
		int mitaloPonr = MITALO.get("MQRIDL");
		int mitaloPosx = MITALO.get("MQRIDX");
		if (mitaloPonr != intPonr
		||  mitaloPosx != intPosx) {
			return;
		}
		
		logger.debug("EXT101MI/CalcProRata workOnMITALO started");
		
		mitaloItno = MITALO.get("MQITNO");
		mitaloBano = MITALO.get("MQBANO");
		mitaloAlqt = MITALO.get("MQALQT");
		readMILOMA(mitaloItno, mitaloBano);

		if (!readQMSRQT(mitaloItno, mitaloBano, milomaFaci)) {
			logger.debug("EXT101MI/CalcProRata workOnMITALO stops after readQMSRQT");
			return false;
		}
		
		countMitalo++;
		sumAlqt += mitaloAlqt;
		
		logger.debug("EXT101MI/CalcProRata workOnMITALO countMitalo = ${countMitalo}");

		if (extzlnStat >= "33") {
			doublePxnum = extzlnSapr;
			logger.debug("EXT101MI/CalcProRata workOnMITALO extzlnStat = ${extzlnStat} extzlnSapr = ${extzlnSapr} ");
		} else {
			doublePxnum = oolineSapr;
			logger.debug("EXT101MI/CalcProRata workOnMITALO extzlnStat = ${extzlnStat} oolineSapr = ${oolineSapr} ");
		}

		
		logger.debug("EXT101MI/CalcProRata workOnMITALO BEFORE calc doublePxnum = doublePxnum * qmsrqtQtrs * mitaloAlqt / extzlnAcva ");
		logger.debug("EXT101MI/CalcProRata workOnMITALO doublePxnum =  ${doublePxnum}, qmsrqtQtrs = ${qmsrqtQtrs}, mitaloAlqt = ${mitaloAlqt}, extzlnAcva = ${extzlnAcva} ");
		doublePxnum = doublePxnum * qmsrqtQtrs * mitaloAlqt / extzlnAcva;
		logger.debug("EXT101MI/CalcProRata workOnMITALO BEFORE sumSapr += doublePxnum; sumSapr = ${sumSapr}");
		sumSapr += doublePxnum;
		logger.debug("EXT101MI/CalcProRata workOnMITALO AFTER sumSapr = ${sumSapr}");
	}

	/**
	 *   readMILOMA - read MILLOMA record for given key values
	 */
	void readMILOMA(String itno, String bano) {
		milomaFaci = "";
		DBAction action_MILOMA = database.table("MILOMA")
				.index("00")
				.selection("LMFACI")
				.build();
		DBContainer MILOMA = action_MILOMA.getContainer();

		logger.debug("EXT101MI/CalcProRata readMILOMA KEYFIELDS LMCONO" + intCono.toString()
				+ " LMITNO: " + itno
				+ " LMBano: " + bano);

		// Key value for read
		MILOMA.set("LMCONO", intCono);
		MILOMA.set("LMITNO", itno);
		MILOMA.set("LMBANO", bano);

		// Read
		if (action_MILOMA.read(MILOMA)) {
			logger.debug("EXT101MI/CalcProRata readMILOMA record is existing");
			milomaFaci = MILOMA.get("LMFACI");
			return;
		}

		logger.debug("EXT101MI/CalcProRata readMILOMA record is not existing");
		return;
	}

	/**
	 *   readQMSRQT - read QMSRQT data and save later used field values
	 */
	boolean readQMSRQT(String itno, String bano, String faci) {
		chkQtrs = 0d;
		countRecQMSRQT = 0;
		qmsrqtQtrs = 0d;
		foundError = false;
		
		Closure<?> workOnQMSRQT = { DBContainer QMSRQT ->
			logger.debug("EXT101MI/CalcProRata workOnQMSRQT started");
			chkQtrs = QMSRQT.get("RTQTRS");
			qmsrqtQtst = QMSRQT.get("RTQTST");
			if (chkQtrs == 0d) {
				logger.debug("EXT101MI/CalcProRata qmsrqtQtrs: ${qmsrqtQtrs} qmsrqtQtst: ${qmsrqtQtst} CountRecQMSRQT: ${countRecQMSRQT}");
			} else {
				countRecQMSRQT++;
				qmsrqtQtrs = QMSRQT.get("RTQTRS");
			}
		}
		
		ExpressionFactory expression = database.getExpressionFactory("QMSRQT");
		expression = expression.le("RTQSE1", stringTodaysDate)
					.and(expression.ge("RTQTI1", stringTodaysDate)
					.and(expression.eq("RTQTST", extzlnQtst)));
		DBAction action_QMSRQT = database.table("QMSRQT").index("20")
				.matching(expression)
				.selection("RTQTST", "RTQTRS", "RTQSE1", "RTQTI1")
				.build();
		DBContainer QMSRQT = action_QMSRQT.createContainer();
		QMSRQT.set("RTCONO", intCono);
		QMSRQT.set("RTFACI", faci);
		QMSRQT.set("RTITNO", itno);
		QMSRQT.set("RTBANO", bano);
		QMSRQT.set("RTSPEC", itno);
		action_QMSRQT.readAll(QMSRQT, 5, workOnQMSRQT);
		if (countRecQMSRQT == 0) {
			mi.error("There is no satisfying quality check result available ${extzlnQtst}");
			foundError = true;
			return false;
		}
		return true;
	}

	

	/**
	 *   updatePriceInformation - prepare and initiate the update of the price information
	 */
	void updatePriceInformation() {
		logger.debug("EXT101MI/CalcProRata sumSapr: ${sumSapr} - sumAlqt: ${sumAlqt}" );
		int dccd = ooheadDccd;
		if (oolineCofa != 1d) {
			if (oolineCofa > 10000d
			||  oolineCofa < 0.001d) {
				dccd += 4;
			} else if (oolineCofa > 1000d
			||  oolineCofa < 0.01d) {
				dccd += 3;
			} else if (oolineCofa > 100d
			||  oolineCofa < 0.1d) {
				dccd += 2;
			} else if (oolineCofa > 1d
			||  oolineCofa < 1d) {
				dccd += 1;
			}
		}
		
		if (oolineSacd > 10000) {
			dccd += 4;
		} else if (oolineSacd > 1000) {
			dccd += 3;
		} else if (oolineSacd > 100) {
			dccd += 2;
		} else if (oolineSacd > 10) {
			dccd += 1;
		}
		
		if (dccd > 6) {
			dccd = 6;
		}
		
		doublePxnum = sumSapr / sumAlqt;
		
		logger.debug("EXT101MI/CalcProRata doublePxnum = sumSapr / sumAlqt");
		logger.debug("EXT101MI/CalcProRata doublePxnum ${doublePxnum} = ${sumSapr} / ${sumAlqt}");
		
		if (dccd == 0) {
			//doublePxnum += 0.5d;
			doublePxnum = (int) doublePxnum;
		}
		if (dccd == 1) {
			//doublePxnum += 0.05d;
			doublePxnum = Double.parseDouble(df1.format(doublePxnum));
		}
		if (dccd == 2) {
			//doublePxnum += 0.005d;
			doublePxnum = Double.parseDouble(df2.format(doublePxnum));
		}
		if (dccd == 3) {
			//doublePxnum += 0.0005d;
			doublePxnum = Double.parseDouble(df3.format(doublePxnum));
		}
		if (dccd == 4) {
			//doublePxnum += 0.00005d;
			doublePxnum = Double.parseDouble(df4.format(doublePxnum));
		}
		if (dccd == 5) {
			//doublePxnum += 0.000005d;
			doublePxnum = Double.parseDouble(df5.format(doublePxnum));
		}
		if (dccd == 6) {
			//doublePxnum += 0.0000005d;
			doublePxnum = Double.parseDouble(df6.format(doublePxnum));
		}
		
		logger.debug("EXT101MI/CalcProRata vor update Aufrufen doublePxnum: ${doublePxnum}");
		
		updatePriceOOLINE();
		
		updatePriceEXTZLN();
		
	}
	
	/**
	 * updatePriceOOLINE - update the OOLINE price information via API Call "OIS100MI/
	 *
	 * Input
	 * Company - from Input
	 */
	boolean updatePriceOOLINE(){
		logger.debug("EXT101MI/CalcProRata updatPriceOOLINE started! company: " + intCono.toString());
		// Run MI program
		def parameter = [
				  "CONO": intCono.toString(),
				  "ORNO": iOrno,
				  "PONR": iPonr,
				  "POSX": iPosx,
				  "SAPR": doublePxnum.toString()
				  ];
		List <String> result = [];
		Closure<?> handler = {
			Map<String, String> response ->
			logger.debug("Response = "+ response);
			return response.isEmpty()
		};
		logger.debug("EXT101MI/CalcProRata vor OIS100MI");
		miCaller.call("OIS100MI", "UpdPriceInfo", parameter, handler);
	}
		
	/**
	 * updatePriceEXTZLN
	 * Start update process by reading and updating EXTZLN
	 *
	 * input data
	 * - EXTZLN 		key fields index 00
	 * - oolineSAPR		original price from OOLINE
	 */
	void updatePriceEXTZLN(){
		logger.debug("EXT101MI/CalcProRata start- updatePriceEXTZLN" );
		DBAction action_EXTZLN = database.table("EXTZLN")
				.index("00")
				.selection("EXSTAT", "EXCHNO")
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
	 * update EXTZLN fields /STAT and /SAPR
	 */
	Closure<?> updateCallBack = { LockedResult lockedResult ->
		logger.debug("EXT101MI/CalcProRata Closure<?> updateCallBack");
		// Get todays date
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);

		int changeNo = lockedResult.get("EXCHNO");
		int newChangeNo = changeNo + 1;
		String extzlnStat = lockedResult.get("EXSTAT");

		// Update the EXTZLN fields
		if (extzlnStat != "33") {
			lockedResult.set("EXSAPR", oolineSapr);
			logger.debug("EXT101MI/CalcProRata SAPR " + oolineSapr.toString());
		}
		lockedResult.set("EXSTAT", "33");
		
		// Update changed information
		int changeddate=Integer.parseInt(formatDate);
		lockedResult.set("EXLMDT", changeddate);
		logger.debug("EXT101MI/CalcProRata LMDT " + changeddate);
		lockedResult.set("EXCHNO", newChangeNo);
		logger.debug("EXT101MI/CalcProRata CHNO: " + newChangeNo);
		lockedResult.set("EXCHID", program.getUser());
		logger.debug("EXT101MI/CalcProRata CHID: " +  program.getUser());
		
		lockedResult.update();
	}

}
