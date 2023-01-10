/* This API transacation Del is used to delete a specific record from file EXTCLC
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2022-11-03
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class Del extends ExtendM3Transaction {
	
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;
	
	private String iCono = "";
	private int intCono = 0;
	private String iArtn = "";    //Art-Nr.
	private String iZiel = "";    //Zielland
	private String iUldp = "";    //Urprungsland pr�ferenziell
	private String iUldh = "";    //Urprungsland handelsrechtlich
	private String iZtnr = "";    //Zolltarifnummer
	private int intZtnr = 0;
	
	public Del(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
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
		
		if (!validateInput()) {
			logger.debug("XEXT800MI/Del validateInput ended with false!!!!");
			mi.write();
			return;
		}
		
		deleteRecord();
	}
	
	/**
	* validateInput
	*
	* validate data from the input
	*/
	boolean validateInput() {
		logger.debug("XEXT800MI/Get validateInput started");
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
			mi.error("An item no ARTN has to be entered");
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
		
		//check iUldp Ursprungsland pr�ferentiell
		if (iUldp == null) {
			iUldp = "";
		}
		iUldp.trim();
		if (iUldp == ""){
			mi.error("Ursprungsland pr�ferentiell has to be entered");
			return false;
		}
		
		//check iUldh Ursprungsland handelsrechtlich
		if (iUldh == null) {
			iUldh = "";
		}
		iUldh.trim();
		if (iUldh == ""){
			mi.error("Ursprungsland handelsrechtlich has to be entered");
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
		intZtnr = Integer.parseInt(iZtnr.trim());

		//all checks are done and ok
		return true;
	}
	
	/**
	*  validateCompany - Validate given or retrieved CONO
	*  Input
	*  Company - from Input
	*/
	 boolean validateCompany(String company){
		 logger.debug("XEXT800MI/Get validateCompany started! company: " + iCono);
		 // Run MI program
		 def parameter = [CONO: company];
		 List <String> result = [];
		 Closure<?> handler = {Map<String, String> response ->
			 return response.CONO == 0};
		 miCaller.call("MNS095MI", "Get", parameter, handler);
	 }
	
	/**
	 * Deletes the record in file EXTCLC
	*/
	void deleteRecord() {
		DBAction query = database.table("EXTCLC")
						.index("00")
						.build();
		DBContainer container = query.getContainer();
		container.set("EXCONO", intCono);
		container.set("EXARTN", iArtn);
		container.set("EXZIEL", iZiel);
		container.set("EXULDP", iUldp);
		container.set("EXULDH", iUldh);
		container.set("EXZTNR", intZtnr);
		query.readLock(container, doDelete);
	}

	Closure<?> doDelete = { LockedResult lockedResult ->
		lockedResult.delete();
	}
  }
