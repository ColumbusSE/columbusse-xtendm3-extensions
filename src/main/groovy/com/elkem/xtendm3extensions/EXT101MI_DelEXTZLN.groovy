/** Name: EXT101MI.DelEXTZLN.groovy
 *
 * The API transaction EXT101MI.DelEXTZLN is used to delete a single EXTZLN record
 * based on given full key data
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2023-03-16
 *  @version   1.0
 */
import M3.DBContainer;

public class DelEXTZLN extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private String iCono = "";
	private int intCono = 0;
	private String iOrno = "";
	private String iPonr = "";
	private int intPonr = 0;
	private String iPosx = "";
	private int intPosx  = 0;

	public DelEXTZLN(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
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


		logger.debug("EXT101MI/DelEXTZLN input field CONO : " + iCono);
		logger.debug("EXT101MI/DelEXTZLN input field ORNO : " + iOrno);
		logger.debug("EXT101MI/DelEXTZLN input field PONR : " + iPonr);
		logger.debug("EXT101MI/DelEXTZLN input field POSX : " + iPosx);

		if (!validateInput()) {
			logger.debug("EXT101MI/DelEXTZLN validateInput ended with false!!!!");
			mi.write();
			return;
		}

		deleteRecord(intCono, iOrno, intPonr, intPosx);
	}

	/**
	 * validateInput
	 *
	 * validate data from the input
	 */
	boolean validateInput() {
		logger.debug("EXT101MI/DelEXTZLN validateInput started");
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

		//check the record is existing EXTZLN
		if (!checkEXTZLN(intCono, iOrno, intPonr, intPosx)) {
			logger.debug("EXT101MI/DelEXTZLN - EXTZLN was not found");
			return false;
		}

		//all checks are done and ok
		return true;

	}

	/**
	 * validateCompany - Validate given or retrieved CONO
	 * Input
	 * Company - from Input
	 */
	boolean validateCompany(String company){
		logger.debug("EXT101MI/DelEXTZLN validateCompany started! company: " + iCono);
		// Run MI program
		def parameter = [CONO: company];
		List <String> result = [];
		Closure<?> handler = {Map<String, String> response ->
			return response.CONO == 0};
		miCaller.call("MNS095MI", "Get", parameter, handler);
	}

	/**
	 * get order position
	 * 
	 * Input
	 * Company - from LDA
	 * order no - from Input
	 * order position - from Input
	 * order position suffix- from Input
	 * 
	 * Output
	 * boolean value for existence of the record 
	 */

	boolean checkEXTZLN(int cono, String orno, int ponr, int posx){
		DBAction action_EXTZLN = database.table("EXTZLN")
				.index("00")
				.build();
		DBContainer EXTZLN = action_EXTZLN.createContainer();
		logger.debug("EXT101MI/DelEXTZLN Start - checkEXTZLN");
		logger.debug("EXT101MI/DelEXTZLN  cono " + cono.toString());
		logger.debug("EXT101MI/DelEXTZLN  orno " + orno + "/" + ponr.toString() + "/" + posx.toString());
		EXTZLN.set("EXCONO", cono);
		EXTZLN.set("EXORNO", orno);
		EXTZLN.set("EXPONR", ponr);
		EXTZLN.set("EXPOSX", posx);
		if (!action_EXTZLN.read(EXTZLN)) {
			mi.error("The EXTZLN record does not exist-  ORNO " + orno + " PONR " + ponr.toString() + " POSX " + posx.toString());
			return false;
		}
		return true;
	}

	/**
	 * Deletes the record in file EXTCLC
	 * 
	 * Input
	 * Company - from LDA
	 * order no - from Input
	 * order position - from Input
	 * order position suffix- from Input
	 */
	void deleteRecord(int cono, String orno, int ponr, int posx) {
		DBAction action_EXTZLN = database.table("EXTZLN")
				.index("00")
				.build();
		DBContainer EXTZLN = action_EXTZLN.createContainer();
		EXTZLN.set("EXCONO", cono);
		EXTZLN.set("EXORNO", orno);
		EXTZLN.set("EXPONR", ponr);
		EXTZLN.set("EXPOSX", posx);
		action_EXTZLN.readLock(EXTZLN, doDelete);
	}

	Closure<?> doDelete = { LockedResult lockedResult ->
		lockedResult.delete();
	}
}
