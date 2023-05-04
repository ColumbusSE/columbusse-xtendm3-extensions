/** Name: EXT422MI.ChgDlqaOdline.groovy
 *
 * The API transaction EXT422MI.ChgDlqaOdline is used to conditional update ODLINE quantity fields
 * with original order quantity information to avoid rounding problems during the invoicing
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-14
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class ChgDlqaOdline extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private List<DBContainer> records = [];

	private final Integer companyNumber;
	private String iCono;
	private int intCono;
	private String iOrno;
	private String iPonr;
	private int intPonr;
	private String iPosx;
	private int intPosx;
	private String iDlix;
	private long longDlix;
	private String iWhlo;
	private String iTepy;
	private String iAckn;
	private int intAckn;
	private String date;
	private String time;
	private double doubleODlqs;
	private double doubleODlqa;
	private double dlqaODLINE;
	private double dlqtODLINE;
	private String orstOOLINE;
	private double cofaOOLINE;
	private double orqtOOLINE;
	private double orqaOOLINE;
	private int countDelivery;

	public ChgDlqaOdline(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger ) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	void main() {
		iCono = program.LDAZD.get("CONO");
		intCono = Integer.parseInt(iCono);
		iOrno = mi.in.get("ORNO");
		iPonr = mi.in.get("PONR");
		iPosx = mi.in.get("POSX");
		iDlix = mi.in.get("Dlix");
		iWhlo = mi.in.get("WHLO");
		iTepy = mi.in.get("TEPY");
		iAckn = mi.in.get("ACKN");

		//check given data
		if (!validateInput()) {
			logger.debug("XEXT422MI/ChgdlqaODLINE validateInput ended with false!!!!");
			mi.write();
			return;
		}
		
		
		Date now = new Date();
		LocalDateTime localNow = LocalDateTime.now();
		date = DateTimeFormatter.ofPattern("yyyyMMdd").format(localNow);
		time = DateTimeFormatter.ofPattern("HHmm").format(localNow);

	  //do the update action if the ODLINE Update should happen
	  if (cofaOOLINE != 1d
		&&  orqtOOLINE == dlqtODLINE
		&&  dlqaODLINE != 0d) {
			logger.debug("check for Update results with true - cofaOOLINE = ${cofaOOLINE} , orqtOOLINE ${orqtOOLINE} == dlqtODLINE ${dlqtODLINE}, dlqaODLINE != 0d ${dlqtODLINE} ");
			updateOdline();
		}
		
		if (intAckn != 0) {
		  //get the record for output reasons independent from real done updates
		  getOdline();
		  if (records.size() != 1) {
			  logger.debug("Did not find ODLINE record");
			  mi.error("Did not find ODLINE record");
		  } else {
			  output(records[0]);
		  }
		  mi.write();
		}
	}

	/**
	 * validateInput
	 * validate data from the input
	 */
	boolean validateInput() {
		logger.debug("EXT422MI_validateInput started");
		if (iCono == null) {
			mi.error("Company " + iCono + " is not valid");
			return false;
		}
		
		if(validateCompany(iCono)){
			mi.error("Company " + iCono + " is invalid");
			return false;
		}

		if (iOrno == null) {
			iOrno = "";
		}

		if (iPonr == null) {
			iPonr = "";
		}
		intPonr = mi.in.get("PONR");

		if (iPosx == null) {
			iPosx = "";
		}
		intPosx = mi.in.get("POSX");
		
		if (iDlix == null) {
			iDlix = "";
		}
		longDlix = mi.in.get("DLIX");
		
		if (iWhlo == null) {
			iWhlo = "";
		}
		
		if (iTepy == null) {
			iTepy = "";
		}
		
		if (iAckn == null) {
			iAckn = "";
		}
		if (iAckn.isBlank()) {
			intAckn = 0;
		} else {
			intAckn = mi.in.get("ACKN");
		}
		
		//get the ODLINE record for check reasons
		doubleODlqs = 0d;
		doubleODlqa = 0d;
		getOdline();
		if (records.size() != 1) {
			mi.error("Did not find ODLINE data for ORNO ${iOrno} DLIX ${iDlix}");
			return false;
		}
			
		if (!getOOLINE()) {
			mi.error("Did not find OOLINE data for ORNO ${iOrno} PONR ${iPonr} POSX ${iPosx}");
			return false;
		}
		
		if (countDeliveries() != 1) {
			return false;
		}
		
		logger.debug("validateInput returns true");
		return true;
	
	}

	/**
	 * validateCompany - Validate given or retrieved CONO
	 * Input
	 * Company - from Input
	 */
	boolean validateCompany(String company){
		logger.debug("XEXT422MI/ChgdlqaODLINE validateCompany started! company: " + iCono);
		// Run MI program
		def parameter = [CONO: company];
		List <String> result = [];
		Closure<?> handler = {Map<String, String> response ->
			return response.CONO == 0};
		miCaller.call("MNS095MI", "Get", parameter, handler);
	}

	/**
	 * do the update on the MWPREL record after locking of the record
	 */
	void updateOdline() {
		logger.debug("XEXT422MI/ChgdlqaODLINE updateOdline started! company: " + iCono);
		DBAction query = database.table("ODLINE")
				.index("00")
				.selection("UBDLQA")
				.build();
		DBContainer container = query.getContainer();
		container.set("UBCONO", intCono);
		container.set("UBORNO", iOrno);
		container.set("UBPONR", intPonr);
		container.set("UBPOSX", intPosx);
		container.set("UBDLIX", longDlix);
		container.set("UBWHLO", iWhlo);
		container.set("UBTEPY", iTepy);
		query.readLock(container, doUpdate);
	}

	Closure<?> doUpdate = {LockedResult lockedResult ->
		logger.debug("XEXT422MI/ChgdlqaODLINE updateOdline >> doUpdate");
		int intChno = lockedResult.get("UBCHNO");
		intChno = intChno + 1;
		double actualDlqa = lockedResult.get("UBDLQA");
		dlqaODLINE = dlqaODLINE + actualDlqa;
		logger.debug("update Odline DLQA and DLQS with ${dlqaODLINE}");
		lockedResult.set("UBDLQA", dlqaODLINE);
		lockedResult.set("UBDLQS", dlqaODLINE);
		lockedResult.set("UBLMDT", date as int);
		lockedResult.set("UBCHNO", intChno);
		lockedResult.set("UBCHID", program.getUser());
		lockedResult.update();  
	}

	/**
	 * read the record to be updated from MWPREL for check reasons
	 */
	void getOdline() {
		records.clear();
		DBAction query = database.table("ODLINE")
				.index("00")
				.selection("UBDLQA", "UBDLQS", "UBIVQT", "UBIVQA")
				.build();
		DBContainer container = query.getContainer();
		container.set("UBCONO", intCono);
		container.set("UBORNO", iOrno);
		container.set("UBPONR", intPonr);
		container.set("UBPOSX", intPosx);
		container.set("UBDLIX", longDlix);
		container.set("UBWHLO", iWhlo);
		container.set("UBTEPY", iTepy);
		if (query.read(container)) {
			records << container;
				logger.debug("User ${program.getUser()} -- records.size = ${records.size()}");
		} else {
			  logger.debug("User ${program.getUser()} Did not find ${mi.inData.get("DLIX")}");
		}
		if (doubleODlqs == 0d && doubleODlqa == 0d) {
			doubleODlqs = container.get("UBDLQS");
			doubleODlqa = container.get("UBDLQA");
		}
	}
	
	/** countDeliveries
	 *
	 * used to check the count of deliveries and to save quantity information from ODLINE
	 */
	int countDeliveries() {
		logger.debug("EXT422MI_ChgdlqaODLINE start CountDeliveries");
		countDelivery = 0;
		dlqtODLINE = 0d;
		dlqaODLINE = orqaOOLINE;
		DBAction query = database.table("ODLINE")
				.index("00")
				.selection("UBIVQT", "UBDLQT", "UBIVQA", "UBDLQA")
				.build();
		DBContainer container = query.getContainer();
		container.set("UBCONO", intCono);
		container.set("UBORNO", iOrno);
		container.set("UBPONR", intPonr);
		container.set("UBPOSX", intPosx);
		query.readAll(container, 4, recordODLINE);

		return countDelivery;
	}

	Closure<?> recordODLINE = { DBContainer container ->
		logger.debug("start CountDeliveries_Closure<?> recordODLINE");
		countDelivery ++;
		double actualIVQT = container.get("UBIVQT");
		double actualDLQT = container.get("UBDLQT");
		double actualDLQA = container.get("UBDLQA");
		double actualIVQA = container.get("UBIVQA");
		
		dlqtODLINE = dlqtODLINE + actualIVQT + actualDLQT;
		dlqaODLINE = dlqaODLINE - actualDLQA - actualIVQA;
		logger.debug("recordODLINE Details countDelivery ${countDelivery} dlqtODLINE ${dlqtODLINE} dlqaODLINE ${dlqaODLINE}  ");
	}
	
	/**
	 * getOOLINE
	 *
	 * check existing OOLINE reocrd and save the quantity information
	 */
	boolean getOOLINE() {
		logger.debug("EXT422MI_ChrdlqaODLINE Start -  getOOLINE");
		DBAction action = database.table("OOLINE")
				.index("00")
				.selection("OBORST", "OBORQA", "OBORQT", "OBCOFA")
				.build();
		DBContainer container = action.createContainer();
		
		container.set("OBCONO", intCono);
		container.set("OBORNO", iOrno);
		container.set("OBPONR", intPonr);
		container.set("OBPOSX", intPosx);
		if (!action.read(container)) {
			orqtOOLINE = 0d;
			orqaOOLINE = 0d;
			cofaOOLINE = 0d;
			return false;
		}

		orqtOOLINE = container.get("OBORQT");
		orqaOOLINE = container.get("OBORQA");
		orstOOLINE = container.get("OBORST");
		cofaOOLINE = container.get("OBCOFA");
		logger.debug("getOOLINE orqtOOLINE = ${orqtOOLINE}, orqaOOLINE = ${orqaOOLINE}, orstOOLINE = ${orstOOLINE}, cofaOOLINE = ${cofaOOLINE} ");
		return true;
	}

	/**
	 * create the output data with updated content
	 */
	void output(DBContainer container) {
		mi.outData.put("ORNO", container.get("UBORNO") as String);
		mi.outData.put("PONR", container.get("UBPONR") as String);
		mi.outData.put("POSX", container.get("UBPOSX") as String);
		mi.outData.put("DLIX", container.get("UBDLIX") as String);
		mi.outData.put("WHLO", container.get("UBWHLO") as String);
		mi.outData.put("TEPY", container.get("UBTEPY")as String);
		mi.outData.put("ODQS", doubleODlqs as String);
		mi.outData.put("DLQS", container.get("UBDLQS") as String);
		mi.outData.put("ODQA", doubleODlqa as String);
		mi.outData.put("DLQA", container.get("UBDLQA") as String);
	}

}
