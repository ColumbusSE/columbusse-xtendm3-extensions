/*This API transaction get is used to get a single record  from the customer specific file EXTCLC
 *
 * @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 * @date      2022-11-02
 *
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class Get extends ExtendM3Transaction {
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
	
	private String oCono = "";
	private String oArtn = "";    //Art-Nr.
	private String oZiel = "";    //Zielland
	private String oUldp = "";    //Urprungsland pr�ferenziell
	private String oZtnr = "";    //Zolltarifnummer
	private String oUldh = "";    //Ursprungsland handelsrechtlich
	private String oVkpm = "";    //Mindestverkaufspreis

	// Constructor
	public Get(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
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
		iUldh =  mi.in.get("ULDH");
		iVkpm =  mi.in.get("VKPM");
		iZtnr =  mi.in.get("ZTNR");
		

		logger.debug("XEXT800MI/Get input field CONO : " + iCono);
		logger.debug("XEXT800MI/Get input field ARTN : " + iArtn);
		logger.debug("XEXT800MI/Get input field ZIEL : " + iZiel);
		logger.debug("XEXT800MI/Get input field ULDP : " + iUldp);
		logger.debug("XEXT800MI/Get input field ZTNR : " + iZtnr);
		logger.debug("XEXT800MI/Get input field ULDH : " + iUldh);

		if (!validateInput()) {
			logger.debug("XEXT800MI/Get validateInput ended with false!!!!");
			mi.write();
			return;
		}

		Optional<DBContainer> EXTCLC = createOutput();
		if(!EXTCLC.isPresent()){
			logger.debug("XEXT800MI/Get validateInput ended with false!!!!");
			mi.error("The record doesn't exist in EXTCLC");
		}
		mi.write();
		return;
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
			mi.error("Company " + iCono + " is not valid");
			return false;
		}
		if(validateCompany(iCono)){
			mi.error("Company " + iCono + " is invalid");
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
	* validateCompany - Validate given or retrieved CONO
	* Input
	* Company - from Input
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
	* isNullOrEmpty - check given string if it is
	*                 null or without content
	* Input
	* String stringVal
	*/
	public  boolean isNullOrEmpty(String stringVal) {
		if(stringVal != null && !stringVal.isEmpty()) {
			return false;
		}
		return true;
	}

  /**
	* createOutput - read the record and create the
	*                output data
	*
	* returns additional the output record
	*/
	private Optional<DBContainer> createOutput() {
		logger.debug("XEXT800MI/Get getEXTCLC");
		DBAction action = database.table("EXTCLC")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXTCLC = action.getContainer();
		
		logger.debug("XEXT800MI/Get readEXTCLC KEYFIELDS EXCONO" + intCono.toString()
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
			logger.debug("XEXT800MI/Get readEXTCLC record is existing");
			oCono = String.valueOf(EXTCLC.get("EXCONO"));
			oArtn = EXTCLC.get("EXARTN");
			oZiel = EXTCLC.get("EXZIEL");
			oUldp = EXTCLC.get("EXULDP");
			oZtnr = String.valueOf(EXTCLC.get("EXZTNR"));
			oUldh = EXTCLC.get("EXULDH");
			oVkpm = df.format(EXTCLC.get("EXVKPM"));
			
			mi.outData.put("CONO", oCono);
			mi.outData.put("ARTN", oArtn);
			mi.outData.put("ZIEL", oZiel);
			mi.outData.put("ULDP", oUldp);
			mi.outData.put("ULDH", oUldh);
			mi.outData.put("ZTNR", oZtnr);
			mi.outData.put("VKPM", oVkpm);
			
			return Optional.of(EXTCLC);
		}
		
		logger.debug("XEXT800MI/Get readEXTCLC record is not existing");
		return Optional.empty();
	}

}
