/** Name: EXT201MI.AddPurLineAcc.groovy
 *
 * The API transaction EXT201MI.AddPurLineAcc is used to write/update data to CACCST
 * like it happens in CRS935 when someone uses PF11 in program PPS201
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-13
 *  @version   1.0
 *
 *  1.0
 *  2.0  2023-02-23   Frank Zahlten  get integer value directly from mi.in
 *  2.0  2023-03.02   Frank Zahlten  additional Update functionality
 */

import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class AddPurLineAcc extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df = new DecimalFormat("0.00");

	private String iCono = "";
	private int intCono = 0;
	private String iPuno = "";
	private String iPnli = "";
	private int intPnli = 0;
	private String iPnls = "";
	private int intPnls = 0;
	private String iAupd = "";
	private int intAupd = 0;
	private String iAit1 = "";
	private String iAit2 = "";
	private String iAit3 = "";
	private String iAit4 = "";
	private String iAit5 = "";
	private String iAit6 = "";
	private String iAit7 = "";
	private String blankAitx = "          ";

	private String plineCono = "";
	private String plineFaci = "";
	private String plineUpav = "";
	private String plinePust = "";
	private String plineItno = "";
	private String plineRorc = "";
	private String plineRorn = "";
	private String plineRorl = "";
	private String plineRorx = "";
	private String plineFatp = "";

	private String pheadDwdt = "";
	private String pheadDivi = "";
	private String pheadOrty = "";

	private String pordtP620 = "";
	String formatDate = "";
	String formatTime = "";

	//CRS935 PX data
	int 	crs935Cono = 0;
	String  crs935Divi = "";
	int  	crs935Fdat = 0;
	String 	crs935Ridn = "";
	int		crs935Ridl = 0;
	int 	crs935Ridx = 0;
	String 	crs935Orca = "";
	String 	crs935Rorn = "";
	int 	crs935Rorc = 0;
	String 	crs935Even = "";
	String 	crs935Acty = "";

	public AddPurLineAcc(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {
		iCono = program.LDAZD.get("CONO");
		intCono = Integer.parseInt(iCono);
		iPuno = mi.in.get("PUNO");
		iPnli = mi.in.get("PNLI");
		iPnls = mi.in.get("PNLS");
		iAupd = mi.in.get("AUPD");
		iAit1 = mi.in.get("AIT1");
		iAit2 = mi.in.get("AIT2");
		iAit3 = mi.in.get("AIT3");
		iAit4 = mi.in.get("AIT4");
		iAit5 = mi.in.get("AIT5");
		iAit6 = mi.in.get("AIT6");
		iAit7 = mi.in.get("AIT7");

		if (!validateInput()) {
			logger.debug("EXT201MI/AddPurLineAcc validateInput ended with false!!!!");
			mi.write()
			return;
		}

		logger.debug("EXT201MI/AddPurLineAcc input field CONO : " + iCono
				+ " PUNO " + iPuno
				+ " PNLI " + iPnli
				+ " PNLS " + iPnls
				+ " AUPD " + iAupd
				+ " AIT1 " + iAit1
				+ " AIT2 " + iAit2
				+ " AIT3 " + iAit3
				+ " AIT4 " + iAit4
				+ " AIT5 " + iAit5
				+ " AIT6 " + iAit6
				+ " AIT7 " + iAit7);

		crs935Cono = intCono;
		crs935Divi = pheadDivi;
		crs935Fdat = Integer.parseInt(pheadDwdt);
		crs935Ridn = iPuno;
		crs935Ridl = intPnli;
		crs935Ridx = intPnls;
		crs935Orca = "251";
		crs935Rorn = plineRorn;
		crs935Rorc = Integer.parseInt(plineRorc);
		
		if (!getAccountingEventAndType()) {
		  mi.write();
			return;
		}
		
		if (!simulateCRS935()) {
			mi.write();
			return;
		}
		
	}

  /**
  * validateInput
  *
  * check given input values
  *
  * returns true/false depending on found error
  */
	private boolean validateInput() {
		//check purchase order no is given
		if (iPuno == null) {
			iPuno = "";
		}
		if (isNullOrEmpty(iPuno)) {
			mi.error("The purchase order no {$iPuno} must be entered");
			return false;
		}
		
		//check position no
		if (iPnli == null) {
			iPnli = "";
		}
		iPnli.trim();
		if (isNullOrEmpty(iPnli)) {
			mi.error("The purchase order position {$iPnli} must be entered");
			return false;
		}
		intPnli = mi.in.get("PNLI");
		
		//check sub position
		if (iPnls == null) {
			iPnls = "";
		}
		iPnls.trim();
		if (isNullOrEmpty(iPnls)) {
			mi.error("The purchase order position suffix {$iPnls} must be entered");
			return false;
		}
		intPnls = mi.in.get("PNLS");
		
		//check allow update selection
		if (iAupd == null) {
			iAupd = "";
		}
		iAupd.trim();
		if (isNullOrEmpty(iAupd)) {
			mi.error("The parmeter 'AUPD - allow update' {$iPnls} must be entered");
			return false;
		}
		
		intAupd = mi.in.get("AUPD");
		if (intAupd != 0 && intAupd != 1) {
			mi.error("Only '0' or '1' are allowed for parmeter 'AUPD - allow update'");
			logger.debug("EXT201MI validateInput- Only '0' or '1' are allowed - AUPD = " + iAupd);
			return false;
		}

		//check accounting information is given
		if (isNullOrEmpty(iAit1)
		&&  isNullOrEmpty(iAit2)
		&&  isNullOrEmpty(iAit3)
		&&  isNullOrEmpty(iAit4)
		&&  isNullOrEmpty(iAit5)
		&&  isNullOrEmpty(iAit6)
		&&  isNullOrEmpty(iAit7)) {
			mi.error("accounting dimension isn't entered");
			return false;
		}
		if (isNullOrEmpty(iAit1)) {
			iAit1 = "";
		}
		if (isNullOrEmpty(iAit2)) {
			iAit2 = "";
		}
		if (isNullOrEmpty(iAit3)) {
			iAit3 = "";
		}
		if (isNullOrEmpty(iAit4)) {
			iAit4 = "";
		}
		if (isNullOrEmpty(iAit5)) {
			iAit5 = "";
		}
		if (isNullOrEmpty(iAit6)) {
			iAit6 = "";
		}
		if (isNullOrEmpty(iAit7)) {
			iAit7 = "";
		}

		//MPLINE data must exist
		if (!validateMPLINE())  {
			return false;
		}
		
		//MPHEAD data must exist
		if (!validateMPHEAD())  {
			return false;
		}

		return true;
	}

	/**
	 * validateMPLINE - Validate given purchase order line
	 * Input
	 * PUNO - from Input
	 * PNLI - from Input
	 * PNLS - from Input
	 * Output
	 * boolean value
	 */
	private boolean validateMPLINE() {
		logger.debug("EXT201MI validateMPLINE started! CONO: "+ intCono + " PUNO" + iPuno + " PNLI: " + iPnli + "PNLS: " + iPnls);
		logger.debug("EXT201MI validateMPLINE - Read MPLINE");
		DBAction action_MPLINE = database.table("MPLINE")
				.index("00")
				.selection("IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBFACI", "IBUPAV", "IBPUST", "IBITNO",
						   "IBRORC", "IBRORN", "IBRORL", "IBRORX", "IBFATP")
				.build();
		DBContainer MPLINE = action_MPLINE.createContainer();
		MPLINE.set("IBCONO", intCono);
		MPLINE.set("IBPUNO", iPuno);
		MPLINE.set("IBPNLI", intPnli);
		MPLINE.set("IBPNLS", intPnls);
		if (!action_MPLINE.read(MPLINE)) {
			mi.error("The purchase oder line {$iPuno}.{$iPnli}.{$iPnls} does not exist in file MPLINE");
			return false;
		}
		//save values from MPLINE for later use
		plineCono = MPLINE.get("IBCONO");
		plineFaci = MPLINE.get("IBFACI");
		plineUpav = MPLINE.get("IBUPAV");
		plinePust = MPLINE.get("IBPUST");
		plineItno = MPLINE.get("IBITNO");
		plineRorc = MPLINE.get("IBRORC");
		plineRorn = MPLINE.get("IBRORN");
		plineRorl = MPLINE.get("IBRORL");
		plineRorx = MPLINE.get("IBRORX");
		plineFatp = MPLINE.get("IBFATP");

		if (Integer.parseInt(plinePust) >= 50) {
			mi.error("The purchase oder line status {$plinePust} does not allow to change the accounting");
			return false;
		}
		return true;
	}

	/**
	 * validateMPHEAD - Validate given purchase order header
	 *
	 * Input    PUNO - from Input
	 * Output   false, if MPHEAD is not existing 
	 *          DIVI - from MPHEAD
	 *          DWDT - from MPHEAD
	 *          ORTY - from MPHEAD
	 */
	private boolean validateMPHEAD() {

		logger.debug("EXT201MI validateMPHEAD");
		DBAction action_MPHEAD = database.table("MPHEAD")
				.index("00")
				.selection("IADIVI", "IADWDT", "IAORTY")
				.build();
		DBContainer MPHEAD = action_MPHEAD.createContainer();
		MPHEAD.set("IACONO", intCono);
		MPHEAD.set("IAPUNO", iPuno);
		if (!action_MPHEAD.read(MPHEAD)) {
			mi.error("The purchase oder {$iPuno} does not exist in file MPHEAD");
			return false;
		}
		pheadDivi = MPHEAD.get("IADIVI");
		pheadDwdt = MPHEAD.get("IADWDT");
		pheadOrty = MPHEAD.get("IAORTY");
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
	 * getAccountingEventAndType
	 *
	 * based on given data in MITMAS, MITFAC, MPLINE, ...
	 * the accounting event and type will be determined
	 */
	private boolean getAccountingEventAndType() {
	  logger.debug("EXT201MI getAccountingEventAndType()");
		DBAction action_MITMAS = database.table("MITMAS")
				.index("00")
				.selection("MMINDI", "MMSTCD")
				.build();
		DBContainer MITMAS = action_MITMAS.createContainer();
		MITMAS.set("MMCONO", intCono);
		MITMAS.set("MMITNO", plineItno);
		if (!action_MITMAS.read(MITMAS)) {
			mi.error("item no {$plineItno} doesn't exist");
			return false;
		}

		DBAction action_MITFAC = database.table("MITFAC")
				.index("00")
				.selection("M9VAMT")
				.build();
		DBContainer MITFAC = action_MITFAC.createContainer();
		MITFAC.set("M9CONO", intCono);
		MITFAC.set("M9FACI", plineFaci);
		MITFAC.set("M9ITNO", plineItno);
		if (!action_MITFAC.read(MITFAC)) {
			mi.error("item no {$plineItno} doesn't exist at facility {$plineFaci}");
			return false;
		}

		String cas900Parm = "";
		String cas900Smet = "";
		DBAction action_CSYPAR = database.table("CSYPAR")
				.index("00")
				.selection("CPPARM")
				.build();
		DBContainer CSYPAR = action_CSYPAR.createContainer();
		CSYPAR.set("CPCONO", intCono);
		CSYPAR.set("CPDIVI", pheadDivi);
		CSYPAR.set("CPSTCO", "CAS900");
		if (action_CSYPAR.read(CSYPAR)) {
			cas900Parm = CSYPAR.get("CPPARM");
			cas900Smet = cas900Parm.substring(19, 20);
		}
		
		logger.debug("EXT201MI getAccountingEventAndType() - MMSTCD: " + MITMAS.get("MMSTCD"));
		logger.debug("EXT201MI getAccountingEventAndType() - plineUpav: " + plineUpav);
		logger.debug("EXT201MI getAccountingEventAndType() - M9VAMT: " + MITFAC.get("M9VAMT"));
		
		if (MITMAS.get("MMSTCD") == 0
		&&	plineUpav == "0"
		&&	MITFAC.get("M9VAMT") == 5) {
			crs935Even = "PP20";
		} else {
			crs935Even = "PP10";
		}
		if (program.LDAZD.get("MXMI") == 1
		&& 	!plineFatp.trim().length() == 0
		&&	(MITMAS.get("MMINDI") == 2 || MITMAS.get("MMINDI") == 3)) {
			crs935Acty  = "905 ";
		} else if (MITMAS.get("MMSTCD") == 1
		&& plineUpav == "1") {
			crs935Acty = "910 ";
		} else {
			if (cas900Smet == '2' &&
			MITFAC.get("M9VAMT") == 5) {
				crs935Acty = "952 ";
			} else {
				crs935Acty = "903 ";
			}
		}
		return true;
	}

	/**
	 * simulateCRS935
	 *
	 * simulate CRS935 program work with already checked data
	 * the accounting data will be checked during the booking process in GLS040
	 */
	private boolean simulateCRS935() {
		logger.debug("XEXT201MI simulateCRS935");
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		formatTime = now.format(format2);
		
		Optional<DBContainer> CACCST = readCACCST();
		if(CACCST.isPresent()){
			logger.debug("XEXT201MI simulateCRS935 CACCST.isPresent()");
			if (intAupd == 1) {
					logger.debug("XEXT201MI simulateCRS935 intAupd = " + intAupd.toString() + " >>> updateDbRecord");
					updateDbRecord();
			} else {
				mi.error("Record already exists! Transaction isn't allowed");
				logger.debug("XEXT201MI simulateCRS935 - Record already exists!");
				return false;
			}
		} else {
			logger.debug("XEXT201MI simulateCRS935 - addDbRecord");
			addDbRecord();
		}
		
		createOutput();
			
		return true;
	}
	
	/**
	 * readCACCST
	 *
	 * try to read an already existing record in file CACCST with the given data
	 */
	private Optional<DBContainer> readCACCST() {
	  logger.debug("XEXT201MI readCACCST");
		DBAction action_CACCST = database.table("CACCST")
				.index("00")
				.selection("SCAIT1")
				.build();
		DBContainer CACCST = action_CACCST.getContainer();
		
		logger.debug("EXT201MI KEYFIELDS EXCONO" + intCono.toString()
			+ " SCRIDN: " + crs935Ridn
			+ " SCRIDL: " + crs935Ridl.toString()
			+ " SCRIDX: " + crs935Ridx.toString()
			+ " SCORCA: " + crs935Orca
			+ " SCEVEN: " + crs935Even
			+ " SCACTY: " + crs935Acty);

		// Key value for read
		CACCST.set("SCCONO", intCono);
		CACCST.set("SCRIDN",crs935Ridn);
		CACCST.set("SCRIDL",crs935Ridl);
		CACCST.set("SCRIDX",crs935Ridx);
		CACCST.set("SCORCA",crs935Orca);
		CACCST.set("SCEVEN",crs935Even);
		CACCST.set("SCACTY",crs935Acty);
		
		// Read
		if (action_CACCST.read(CACCST)) {
			logger.debug("EXT201MI readCACCST record is existing");
			return Optional.of(CACCST);
		}
		logger.debug("EXT201MI readCACCST record is not existing");
		return Optional.empty();
	}
	
	
	/**
	 * addDbRecord
	 *
	 * insert a new record with the given data
	 * into DB  file CACCST
	 */
	private void addDbRecord() {
		logger.debug("XEXT201MI addDbRecord");

		int regdate=Integer.parseInt(formatDate);
		int regtime=Integer.parseInt(formatTime);
		
		DBAction action_CACCST = database.table("CACCST")
		  .index("00")
		  .build();
		DBContainer CACCST = action_CACCST.getContainer();
		
		logger.debug("EXT201MI KEYFIELDS EXCONO" + intCono.toString()
			+ " SCRIDN: " + crs935Ridn
			+ " SCRIDL: " + crs935Ridl.toString()
			+ " SCRIDX: " + crs935Ridx.toString()
			+ " SCORCA: " + crs935Orca
			+ " SCEVEN: " + crs935Even
			+ " SCACTY: " + crs935Acty);
		
		// Key value for read
		CACCST.set("SCCONO", intCono);
		CACCST.set("SCRIDN",crs935Ridn);
		CACCST.set("SCRIDL",crs935Ridl);
		CACCST.set("SCRIDX",crs935Ridx);
		CACCST.set("SCORCA",crs935Orca);
		CACCST.set("SCEVEN",crs935Even);
		CACCST.set("SCACTY",crs935Acty);
		CACCST.set("SCAIT1",iAit1);
		CACCST.set("SCAIT2",iAit2);
		CACCST.set("SCAIT3",iAit3);
		CACCST.set("SCAIT4",iAit4);
		CACCST.set("SCAIT5",iAit5);
		CACCST.set("SCAIT6",iAit6);
		CACCST.set("SCAIT7",iAit7);
		CACCST.set("SCRGDT", regdate as int);
		CACCST.set("SCRGTM", regtime as int);
		CACCST.set("SCLMDT", regdate as int);
		CACCST.set("SCCHNO", 1);
		CACCST.set("SCCHID", program.getUser());
		action_CACCST.insert(CACCST);
	}
	
	/**
	 * updateDbRecord
	 * Start update process by reading and updating EXTZLN
	 *
	 * input data
	 * - EXTZLN 		key fields index 00
	 * - oolineSAPR		original price from OOLINE
	 */
	void updateDbRecord(){
		logger.debug("XEXT201MI updateDbRecord" );
			DBAction action_CACCST = database.table("CACCST")
				.index("00")
				.selection("SCCHNO")
				.build();
		DBContainer CACCST = action_CACCST.getContainer();
		
		logger.debug("EXT201MI KEYFIELDS EXCONO" + intCono.toString()
			+ " SCRIDN: " + crs935Ridn
			+ " SCRIDL: " + crs935Ridl.toString()
			+ " SCRIDX: " + crs935Ridx.toString()
			+ " SCORCA: " + crs935Orca
			+ " SCEVEN: " + crs935Even
			+ " SCACTY: " + crs935Acty);

		// Key value for read
		CACCST.set("SCCONO", intCono);
		CACCST.set("SCRIDN",crs935Ridn);
		CACCST.set("SCRIDL",crs935Ridl);
		CACCST.set("SCRIDX",crs935Ridx);
		CACCST.set("SCORCA",crs935Orca);
		CACCST.set("SCEVEN",crs935Even);
		CACCST.set("SCACTY",crs935Acty);

		// Read with lock
		action_CACCST.readLock(CACCST, updateCallBack);
	}

	/**
	 * updateCallBack
	 * update CACCST fields /AIT1 to /AIT7
	 */
	Closure<?> updateCallBack = { LockedResult lockedResult ->
		logger.debug("EXT201MI/AddPurLineAcc Closure<?> updateCallBack");

		int changeNo = lockedResult.get("SCCHNO");
		int newChangeNo = changeNo + 1;

		// Update the CACCST fields
	  // AIT1 can never be blank
		if (!isNullOrEmpty(iAit1)) {
			lockedResult.set("SCAIT1", iAit1);
		}
		// AIT2 may get changed
		if (!isNullOrEmpty(iAit2)) {
			if (iAit2 == "!") {
				lockedResult.set("SCAIT2", blankAitx);
			} else {
				lockedResult.set("SCAIT2", iAit2);
			}
		}
		// AIT3 may get changed
		if (!isNullOrEmpty(iAit3)) {
			if (iAit3 == "!") {
				lockedResult.set("SCAIT3", blankAitx);
			} else {
				lockedResult.set("SCAIT3", iAit3);
			}
		}
		// AIT4 may get changed
		if (!isNullOrEmpty(iAit4)) {
			if (iAit4 == "!") {
				lockedResult.set("SCAIT4", blankAitx);
			} else {
				lockedResult.set("SCAIT4", iAit4);
			}
		}
		// AIT5 may get changed
		if (!isNullOrEmpty(iAit5)) {
			if (iAit5 == "!") {
				lockedResult.set("SCAIT5", blankAitx);
			} else {
				lockedResult.set("SCAIT5", iAit5);
			}
		}
		// AIT6 may get changed
		if (!isNullOrEmpty(iAit6)) {
			if (iAit6 == "!") {
				lockedResult.set("SCAIT6", blankAitx);
			} else {
				lockedResult.set("SCAIT6", iAit6);
			}
		}
		// AIT7 may get changed
		if (!isNullOrEmpty(iAit7)) {
			if (iAit7 == "!") {
				lockedResult.set("SCAIT7", blankAitx);
			} else {
				lockedResult.set("SCAIT7", iAit7);
			}
		}
		
		// Update changed information
		int changeddate=Integer.parseInt(formatDate);
		lockedResult.set("SCLMDT", changeddate);
		logger.debug("XEXT201MI/ADD LMDT " + changeddate);
		lockedResult.set("SCCHNO", newChangeNo);
		logger.debug("XEXT201MI/ADD CHNO: " + newChangeNo);
		lockedResult.set("SCCHID", program.getUser());
		logger.debug("XEXT201MI/ADD CHID: " +  program.getUser());
		
		lockedResult.update();
	}
	
	/**
	 * createOutput
	 * create output information with data from input and
	 * modified sales price information VKPM
	 */
	 private void createOutput() {
		 logger.debug("XEXT201MI/ADD createOutput");
		 mi.outData.put("CONO", iCono);
		 mi.outData.put("RIDN", crs935Ridn);
		 mi.outData.put("RIDL", crs935Ridl.toString());
		 mi.outData.put("RIDX", crs935Ridx.toString());
		 mi.outData.put("ORCA", crs935Orca);
		 mi.outData.put("EVEN", crs935Even);
		 mi.outData.put("ACTY", crs935Acty);
		 mi.outData.put("AIT1", iAit1);
		 mi.outData.put("AIT2", iAit2);
		 mi.outData.put("AIT3", iAit3);
		 mi.outData.put("AIT4", iAit4);
		 mi.outData.put("AIT5", iAit5);
		 mi.outData.put("AIT6", iAit6);
		 mi.outData.put("AIT7", iAit7);
		 mi.write();
	 }
}
