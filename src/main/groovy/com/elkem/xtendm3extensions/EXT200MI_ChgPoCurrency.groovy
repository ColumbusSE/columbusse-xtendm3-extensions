/** Name: EXT200MI.ChgPoCurrency.groovy
 *
 * The API transaction EXT200MI.ChgPoCurrency is used to update the currency in an exsiting purchase order
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-13
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class ChgPoCurrency extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df = new DecimalFormat("0.00");
	private String date;
	private String time;
	private String iCono = "";
	private int intCono = 0;
	private String iPuno = "";
	private String iCucd = "";
	private String mphead_CUCD = "";

	public ChgPoCurrency(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
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
		iCucd = mi.in.get("CUCD");

		if (!validateInput()) {
			logger.debug("EXT200MI/ChgPoCurrency ended with false!!!!");
			mi.write()
			return;
		}

		Date now = new Date();
		LocalDateTime localNow = LocalDateTime.now();
		date = DateTimeFormatter.ofPattern("yyyyMMdd").format(localNow);
		time = DateTimeFormatter.ofPattern("HHmm").format(localNow);

		//do the update action
		updateMPHEAD();

	}

	/**
	 * validateInput
	 * validate data from the input
	 */
	boolean validateInput() {
		logger.debug("XtendM3Debug_ChgPoCurrency validateInput ");
		//check purchase order
		if (iPuno == null) {
			iPuno = "";
		}
		iPuno.trim();
		if (iPuno == ""){
			mi.error("Purchase order has to be entered");
			return false;
		}
		if (!validatePuno()) {
			return false;
		}

		//check Currency
		if (iCucd == null) {
			iCucd = "";
		}
		iCucd.trim();
		if (iCucd == ""){
			mi.error("Currency has to be entered");
			return false;
		}
		if (!validateCSYTAB("CUCD", iCucd, "Currency")) {
			return false;
		}
		return true;
	}
	
	/** 
	 * validatePuno
	 * 
	 * Input
	 *  Company - from Input
	 *  purchase order no - from Input
	 *  
	 *  saved information for later use
	 *  mphead_CUCD
	 */
	 boolean validatePuno(){
		 DBAction action_MPHEAD = database.table("MPHEAD")
				 .index("00")
				 .selection("IACUCD")
				 .build();
		 DBContainer PHEAD = action_MPHEAD.createContainer();
		 logger.debug("validatePuno CONO " + iCono);
		 logger.debug("validatePuno PUNO " + iPuno);
		 PHEAD.set("IACONO", intCono);
		 PHEAD.set("IAPUNO", iPuno);
		 if (!action_MPHEAD.read(PHEAD)) {
			 logger.debug("XtendM3Debug_ChgPoCurrency validatePuno - not found MPHEAD");
			 mi.error("The purchase order does not exist! PUNO = " + iPuno);
			 return false;
		 }
		 mphead_CUCD = PHEAD.get("IACUCD");
		 return true;
	 }
	 
	 /**
	  * validate CSYTAB data - Validate with given key data
	  * Input
	  * Company - from LDA
	  * stco - constant value for CSYTAB data
	  * stky - key data, to be used in combination with stco
	  * description - for the output of the correct field description
	  */
	  private boolean validateCSYTAB(String iStco, String iStky, String iDescription) {
		  logger.debug("validateCSYTAB started! STCO: " + iStco + " STKY: " + iStky);
		  DBAction action_CSYTAB = database.table("CSYTAB")
				  .index("00")
				  .build();
		  DBContainer CSYTAB = action_CSYTAB.createContainer();
		  CSYTAB.set("CTCONO", intCono);
		  CSYTAB.set("CTDIVI", "   ");
		  CSYTAB.set("CTSTCO", iStco);
		  CSYTAB.set("CTSTKY", iStky);
		  CSYTAB.set("CTLNCD", "  ");
		  if (!action_CSYTAB.read(CSYTAB)) {
			  mi.error(iDescription + " " + iStky +" does not exist!");
			  return false;
		  }
		  return true;
	  }
	 
	/**
	 * do the update on the MPHEAD record after locking of the record
	 */
	void updateMPHEAD() {
		logger.debug("EXT200MI/ChgPoCurrency updateMPHEAD started! company: " + iCono);
		DBAction query = database.table("MPHEAD")
				.index("00")
				.selection("IACHNO")
				.build();
		DBContainer container = query.getContainer();
		container.set("IACONO", intCono);
		container.set("IAPUNO", iPuno);
		query.readLock(container, doUpdate);
	}

	Closure<?> doUpdate = { LockedResult lockedResult ->
		boolean updated = false
		int intChno = lockedResult.get("IACHNO");
		intChno = intChno + 1;
		if (mi.in.get("CUCD")) {
			updated = true;
			lockedResult.set("IACUCD", mi.in.get("CUCD"));
		}
		if (updated) {
			lockedResult.set("IALMDT", date as int);
			lockedResult.set("IACHNO", intChno);
			lockedResult.set("IACHID", program.getUser());
			lockedResult.update();
		}
	}

}
