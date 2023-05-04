/** Name: EXT101MI.GetEXTZLN.groovy
 *
 * The API transaction EXT101MI.AddUpdEXTZLN is used to add or update EXTZLN records
 * with initial data comming from OIS101
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-02-17
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class GetEXTZLN extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private static final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final DecimalFormat df6 = new DecimalFormat("0.000000");

	private String iCono = "";
	private int intCono = 0;
	private String iOrno = "";
	private String iPonr = "";
	private int intPonr = 0;
	private String iPosx = "";
	private int intPosx  = 0;
	
	private String oCono = "";
	private String oOrno = "";
	private String oPonr = "";
	private String oPosx = "";
	private String oSapr = "";
	private String oAncl = "";
	private String oAcva = "";
	private String oQtst = "";
	private String oTx40 = "";
	private String oEvmn = "";
	private String oQtrs = "";
	private String oStat = "";
	
	// Constructor
	public GetEXTZLN(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {

		iCono =  program.LDAZD.get("CONO");
		iOrno =  mi.in.get("ORNO");
		iPonr =  mi.in.get("PONR");
		iPosx =  mi.in.get("POSX");
		
		logger.debug("EXT101MI/GetEXTZLN input field CONO : " + iCono);
		logger.debug("EXT101MI/GetEXTZLN input field ORNO : " + iOrno);
		logger.debug("EXT101MI/GetEXTZLN input field PONR : " + iPonr);
		logger.debug("EXT101MI/GetEXTZLN input field POSX : " + iPosx);

		if (!validateInput()) {
			logger.debug("EXT101MI/GetEXTZLN validateInput ended with false!!!!");
			mi.write();
			return;
		}

		Optional<DBContainer> EXTZLN = createOutput();
		if(!EXTZLN.isPresent()){
			logger.debug("EXT101MI/GetEXTZLN The record doesn't exist in EXTZLN!!!!");
			mi.error("The record doesn't exist in EXTZLN");
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
		logger.debug("EXT101MI/GetEXTZLN validateInput started");
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
		if (iOrno == null) {
			iOrno = "";
		}
		iOrno.trim();
		if (iOrno == ""){
			mi.error("An order no ORNO has to be entered");
			return false;
		}
		
		//check iPonr Position no
		if (iPonr == null) {
			iPonr = "";
		}
		iPonr.trim();
		if (iPonr == ""){
			mi.error("Position no has to be entered");
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
			return false;
		}
		intPosx = mi.in.get("POSX");
		
		//all checks are done and ok
		return true;

	}


	/**
	* validateCompany - Validate given or retrieved CONO
	* Input
	* Company - from Input
  */
	boolean validateCompany(String company){
		logger.debug("EXT101MI/GetEXTZLN validateCompany started! company: " + iCono);
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
		logger.debug("EXT101MI/GetEXTZLN getEXTZLN");
		DBAction action = database.table("EXTZLN")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXTZLN = action.getContainer();
		
		logger.debug("EXT101MI/GetEXTZLN readEXTZLN KEYFIELDS EXCONO" + intCono.toString()
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
			logger.debug("EXT101MI/GetEXTZLN readEXTZLN record is existing");
			oCono = String.valueOf(EXTZLN.get("EXCONO"));
			oOrno = EXTZLN.get("EXORNO");
			oPonr = String.valueOf(EXTZLN.get("EXPONR"));
			oPosx = String.valueOf(EXTZLN.get("EXPOSX"));
			oSapr = df6.format(EXTZLN.get("EXSAPR"));
			oAncl = EXTZLN.get("EXANCL");
			oAcva = df2.format(EXTZLN.get("EXACVA"));
			oQtst = EXTZLN.get("EXQTST");
			oTx40 = EXTZLN.get("EXTX40");
			oEvmn = df6.format(EXTZLN.get("EXEVMN"));
			oQtrs = df6.format(EXTZLN.get("EXQTRS"));
			oStat = EXTZLN.get("EXSTAT");
			
			mi.outData.put("CONO", oCono);
			mi.outData.put("ORNO", oOrno);
			mi.outData.put("PONR", oPonr);
			mi.outData.put("POSX", oPosx);
			mi.outData.put("SAPR", oSapr);
			mi.outData.put("ANCL", oAncl);
			mi.outData.put("ACVA", oAcva);
			mi.outData.put("QTST", oQtst);
			mi.outData.put("TX40", oTx40);
			mi.outData.put("STAT", oStat);
			
			return Optional.of(EXTZLN);
		}
		
		logger.debug("EXT101MI/GetEXTZLN readEXTZLN record is not existing");
		return Optional.empty();
	}

}
