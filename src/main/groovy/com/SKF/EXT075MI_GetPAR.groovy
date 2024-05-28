// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-04-12
// @version   1.0 
//
// Description 
// This API will get a record from table EXTPAR
// Transaction GetPAR



/**
 * IN
 * @param: CONO - Company Number
 * @param: FACI - Facility
 * @param: PLGR - Work Center
 * @param: TRDT - Transaction Date
 * @param: TRTM - Transaction Time
 * @param: TMSX - Time Suffix
 * 
*/

/**
 * OUT
 * @param: CONO	- Company Number
 * @param: FACI- Name
 * @param: PLGR- Work Center
 * @param: TRDT- Transaction Date
 * @param: TRTM- Transaction Time
 * @param: SHFC- Shift
 * @param: UPIT- Used Mch Run Time
 * @param: RUDI- Run Disturbance
 * @param: SEDI- Setup Disturbance
 * @param: STDT- Start Date
 * @param: OSTM- Start Time
 * @param: FIDT- Finish Date
 * @param: OFTM- Finish Time
 * @param: KIWG- Object
 * @param: TX60- Text
 * @param: RESP- Responsible
 * 
*/


public class GetPAR extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inFACI
  String inPLGR
  int inTRDT
  int inTRTM
  int inTMSX
  
  // Constructor 
  public GetPAR(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database  
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
       // Set Company Number
       inCONO = mi.in.get("CONO")      
       if (inCONO == null || inCONO == 0) {
          inCONO = program.LDAZD.CONO as Integer
       } 
  
       //Facility
       if (mi.in.get("FACI") != null && mi.in.get("FACI") != "") {
          inFACI = mi.inData.get("FACI").trim() 
       } else {
          inFACI = ""         
       }
        
       // Work Center
       if (mi.in.get("PLGR") != null && mi.in.get("PLGR") != "") {
          inPLGR = mi.inData.get("PLGR").trim() 
       } else {
          inPLGR = ""        
       }
        
       // Transaction Date
       if (mi.in.get("TRDT") != null) {
          inTRDT = mi.in.get("TRDT") 
       } else {
          inTRDT = 0        
       }
 
       // Transaction Time
       if (mi.in.get("TRTM") != null) {
          inTRTM = mi.in.get("TRTM") 
       } else {
          inTRTM = 0        
       }

       // Time Suffix
       if (mi.in.get("TMSX") != null) {
          inTMSX = mi.in.get("TMSX") 
       } else {
          inTMSX = 0        
       }

       // Get record
       getRecord()
  }
 

 //******************************************************************** 
 //Get EXTPAR record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTPAR").index("00").selectAllFields().build()
     DBContainer EXTPAR = action.getContainer()
     EXTPAR.set("EXCONO", inCONO)
     EXTPAR.set("EXFACI", inFACI)
     EXTPAR.set("EXPLGR", inPLGR)
     EXTPAR.set("EXTRDT", inTRDT)
     EXTPAR.set("EXTRTM", inTRTM)
     EXTPAR.set("EXTMSX", inTMSX)
     
    // Read  
    if (action.read(EXTPAR)) {             
      mi.outData.put("CONO", EXTPAR.get("EXCONO").toString())
      mi.outData.put("FACI", EXTPAR.getString("EXFACI"))
      mi.outData.put("PLGR", EXTPAR.getString("EXPLGR"))
      mi.outData.put("TRDT", EXTPAR.get("EXTRDT").toString())
      mi.outData.put("TRTM", EXTPAR.get("EXTRTM").toString())
      mi.outData.put("TMSX", EXTPAR.get("EXTMSX").toString())
      mi.outData.put("DEPT", EXTPAR.getString("EXDEPT"))
      mi.outData.put("REAR", EXTPAR.getString("EXREAR"))
      mi.outData.put("SHFC", EXTPAR.getString("EXSHFC"))
      mi.outData.put("UPIT", EXTPAR.getDouble("EXUPIT").toString())
      mi.outData.put("RUDI", EXTPAR.getString("EXRUDI"))
      mi.outData.put("SEDI", EXTPAR.getString("EXSEDI"))
      mi.outData.put("STDT", EXTPAR.get("EXSTDT").toString())
      mi.outData.put("OSTM", EXTPAR.get("EXOSTM").toString())
      mi.outData.put("FIDT", EXTPAR.get("EXFIDT").toString())
      mi.outData.put("OFTM", EXTPAR.get("EXOFTM").toString())
      mi.outData.put("KIWG", EXTPAR.getString("EXKIWG"))
      mi.outData.put("TX60", EXTPAR.getString("EXTX60"))
      mi.outData.put("RESP", EXTPAR.getString("EXRESP"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}