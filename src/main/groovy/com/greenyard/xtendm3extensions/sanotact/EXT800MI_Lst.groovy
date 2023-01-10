/* This API transacation Lst is used to display data from customer specific file EXTCLC
 *
 *  @author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  @date      2022-11-02
 *  @version   1.0
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class Lst extends ExtendM3Transaction {
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
	private int keycount = 0;
	private int recordCount = 0;

	private String oCono = "";
	private String oArtn = "";    //Art-Nr.
	private String oZiel = "";    //Zielland
	private String oUldp = "";    //Urprungsland pr�ferenziell
	private String oZtnr = "";    //Zolltarifnummer
	private String oUldh = "";    //Ursprungsland handelsrechtlich
	private String oVkpm = "";    //Mindestverkaufspreis
	
	private int maxCount = 500;	 //maximum count of records and array elements

	private String[] arrARTN = new String [maxCount];	//[];
	private String[] arrZIEL = new String [maxCount];	//[];
	private String[] arrULDP = new String [maxCount];	//[];
	private String[] arrULDH = new String [maxCount];	//[];
	private String[] arrZTNR = new String [maxCount];	//[];
	private String[] arrVKPM = new String [maxCount];	//[];


	public Lst(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
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
			mi.write();
			return;
		}

		if (!readDbFileData()) {
			mi.write();
			return;
		}

		createOutput();

	}

	//*****************************************************
	// validateInput
	// validate data from the input
	//*****************************************************
	boolean validateInput() {
		logger.debug("XEXT800MI/Lst validateInput() Start");
		
		if (validateCompany(iCono)) {
			mi.error("Company " + iCono + " is invalid")
			return false;
		}
		intCono = program.LDAZD.get("CONO");

		if (iArtn == null) {
			iArtn = "";
		}
		iArtn.trim();

		if (iZiel == null) {
			iZiel = "";
		}
		iZiel.trim();

		if (iUldp == null) {
			iUldp = "";
		}
		iUldp.trim();

		if (iUldh == null) {
			iUldh = "";
		}
		iUldh.trim();

		if (iZtnr == null) {
			iZtnr = "";
		}
		iZtnr.trim();
		if (!iZtnr.trim().isEmpty()) {
			intZtnr = Integer.parseInt(iZtnr);
		}
		return true;
	}

  /**
	 * readDbFileData
	 *
	 * check if data with given key data is existing
	 */
	boolean readDbFileData() {
		logger.debug("XEXT800MI/Lst readDbFileData() Start");
		
		recordCount = 0;
		maintainArr("clear", " ", " ", " ", " ", " ", " ")

		DBAction action = database.table("EXTCLC").index("00").selectAllFields().build()
		DBContainer ext = action.getContainer()

		ext.set("EXCONO", intCono);
		ext.set("EXARTN", iArtn);
		ext.set("EXZIEL", iZiel);
		ext.set("EXULDP", iUldp);
		ext.set("EXULDH", iUldh);
		ext.set("EXZTNR", intZtnr);


		// set keycount depending on the input
		keycount = 1;
		if (!iArtn.trim().isEmpty()) {
			keycount = 2;
			if (!iZiel.trim().isEmpty()) {
				keycount = 3;
				if (!iUldp.trim().isEmpty()) {
					keycount = 4;
					if (!iUldh.trim().isEmpty()) {
						keycount = 5;
						if (!iZtnr.trim().isEmpty()) {
							keycount = 6;
						}
					}
				}
			}
		}

		logger.debug("XEXT800MI/Lst readDbFileData() KEyCount: " + String.valueOf(keycount).trim());
		action.readAll(ext, keycount, maxCount, workOnDbData);
		
		return true;
		
	}

	Closure<?> workOnDbData = { DBContainer ext ->
		maintainArr("add",
				String.valueOf(ext.get("EXARTN")),
				String.valueOf(ext.get("EXZIEL")),
				String.valueOf(ext.get("EXULDP")),
				String.valueOf(ext.get("EXULDH")),
				String.valueOf(ext.get("EXZTNR")),
				df.format(ext.get("EXVKPM")));
	}

	/**
	 *  validateCompany - Validate given or retrieved CONO
	 *
	 *  Input
	 *   Company - from Inp
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

	/** maintain arrays used to store from DB gotten data
	 * input  parameter
	 * operation - allowed are "clear", "exist" and "add"
	 *             detailed field information
	 */
	boolean maintainArr(String operation,
			String artn,
			String ziel,
			String uldp,
			String uldh,
			String ztnr,
			String vkpm) {

		boolean answerValue = false;

		logger.debug("XM3Debug EXT800MI_maintainArr -OPERATION ${operation} ");
		if (operation == "clear") {
			for (int i = 0; i < maxCount; i++) {
				arrARTN[i] = "";
				arrULDP[i] = "";
				arrULDH[i] = "";
				arrZIEL[i] = "";
				arrZTNR[i] = "";
				arrVKPM[i] = "";
			}
			return true;
		}

		if (operation == "add") {
			logger.debug("XtendM3DebugEXT800MI_maintainArr ADD f�r ARTN: " + artn
			+ " ZIEL: " + ziel
			+ " ULDP: " + uldp
			+ " ULDH: " + uldh
			+ " ZTNR: " + ztnr);
			if (artn.trim().isEmpty()) {
				return true;
			}
			for (int i = 0; i < maxCount; i++) {
				if (arrARTN [i] == artn
				&&  arrZIEL [i] == ziel
				&&  arrULDP [i] == uldp
				&&  arrULDH [i] == uldh
				&&  arrZTNR [i] == ztnr) {
					logger.debug("XEXT800MI/Lst maintainArr arr... exists");
					return true;
				}

				if (arrARTN[i].trim().isEmpty()) {
					arrARTN[i] = artn;
					arrZIEL[i] = ziel;
					arrULDP[i] = uldp;
					arrULDH[i] = uldh;
					arrZTNR[i] = ztnr;
					arrVKPM[i] = vkpm;
					recordCount++;
					logger.debug("XtendM3Debug maintainarrARTN ADD f�r VKPM: " + vkpm + " ausgef�hrt. RecordCount: " + String.valueOf(recordCount).trim());
					return true;
				}
			}
			return false;
		}
	}

  /**
  * createOutput - reads the records and creates and
	*                writes the output data
	*/
	void createOutput() {
		logger.debug("XtendM3DebugEXT800MI_createOutput - Start");
		if (recordCount == 0) {
			mi.error("no data found for given key field(s)")
			mi.write();
		}
		for (int i = 0; i < maxCount; i++) {
			if (arrARTN[i].isEmpty()
			&&  arrZIEL[i].isEmpty()
			&&  arrULDP[i].isEmpty()
			&&  arrULDH[i].isEmpty()
			&&  arrZTNR[i].isEmpty()) {
				break;
			} else {
				mi.outData.put("CONO", iCono);
				mi.outData.put("ARTN", arrARTN[i]);
				mi.outData.put("ZIEL", arrZIEL[i]);
				mi.outData.put("ULDP", arrULDP[i]);
				mi.outData.put("ULDH", arrULDH[i]);
				mi.outData.put("ZTNR", arrZTNR[i]);
				mi.outData.put("VKPM", arrVKPM[i]);
				mi.write();
			}
		}
	}

}
