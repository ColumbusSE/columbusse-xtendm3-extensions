// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-04-07
// @version   1.0 
//
// Description 
// This API will add a record to table EXTPAR
// Transaction AddPAR

/**
 * IN
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


public class AddPAR extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    Integer inCONO
    String inFACI
    String inPLGR
    int inTRDT
    String planningArea
    String department
    int inTMSX
    int timeSuffix
    int regDate
    boolean recordFound
    int transactionTime
    
    
    // Constructor 
    public AddPAR(MIAPI mi, DatabaseAPI database, ProgramAPI program,  UtilityAPI utility, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.program = program
       this.utility = utility
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
          
          // Validate facility if entered
          Optional<DBContainer> CFACIL = findCFACIL(inCONO, inFACI)
          if (!CFACIL.isPresent()) {
             mi.error("Facility doesn't exist")   
             return             
          }

       } else {
          inFACI = ""         
       }
        
       // Work Center
       if (mi.in.get("PLGR") != null && mi.in.get("PLGR") != "") {
          inPLGR = mi.inData.get("PLGR").trim() 
          
          // Validate work center if entered
          Optional<DBContainer> MPDWCT = findMPDWCT(inCONO, inFACI, inPLGR)
          if (!MPDWCT.isPresent()) {
             mi.error("Work Center doesn't exist")   
             return             
          }

       } else {
          inPLGR = ""        
       }
        
       // Transaction Date
       if (mi.in.get("TRDT") != null) {
          inTRDT = mi.in.get("TRDT") 
          
          //Validate date format
          boolean validTRDT = utility.call("DateUtil", "isDateValid", String.valueOf(inTRDT), "yyyyMMdd")  
          if (!validTRDT) {
             mi.error("Transaction Date is not valid")   
             return  
          } 

       } else {
          inTRDT = 0        
       }
 
       // Transaction Time
       int inTRTM
       if (mi.in.get("TRTM") != null) {
          inTRTM = mi.in.get("TRTM") 
       } else {
          inTRTM = utility.call("DateUtil", "currentTimeAsInt")    
       }
       transactionTime = inTRTM

       // Shift
       String inSHFC
       if (mi.in.get("SHFC") != null && mi.in.get("SHFC") != "") {
          inSHFC = mi.inData.get("SHFC").trim() 
          
          // Validate shift if entered
          Optional<DBContainer> MPDSHT = findMPDSHT(inCONO, inFACI, inSHFC)
          if (!MPDSHT.isPresent()) {
             mi.error("Shift doesn't exist")   
             return             
          }

       } else {
          inSHFC = ""        
       }

       // Used Machine Time
       double inUPIT
       if (mi.in.get("UPIT") != null) {
          inUPIT = mi.in.get("UPIT") 
       } else {
          inUPIT = 0d       
       }

       // Run Disturbance
       String inRUDI
       if (mi.in.get("RUDI") != null && mi.in.get("RUDI") != "") {
          inRUDI = mi.inData.get("RUDI").trim() 
          
          // Validate disturbance if entered
          Optional<DBContainer> MWDSTC = findMWDSTC(inCONO, inRUDI)
          if (!MWDSTC.isPresent()) {
             mi.error("Run Disturbance doesn't exist")   
             return             
          }

       } else {
          inRUDI = ""        
       }

       // Setup Disturbance
       String inSEDI
       if (mi.in.get("SEDI") != null && mi.in.get("SEDI") != "") {
          inSEDI = mi.inData.get("SEDI").trim() 
          
          // Validate disturbance if entered
          Optional<DBContainer> MWDSTC = findMWDSTC(inCONO, inSEDI)
          if (!MWDSTC.isPresent()) {
             mi.error("Setup Disturbance doesn't exist")   
             return             
          }

       } else {
          inSEDI = ""        
       }

       // Start Date
       int inSTDT
       if (mi.in.get("STDT") != null) {
          inSTDT = mi.in.get("STDT") 
          
          //Validate date format
          boolean validSTDT = utility.call("DateUtil", "isDateValid", String.valueOf(inSTDT), "yyyyMMdd")  
          if (!validSTDT) {
             mi.error("Start Date is not valid")   
             return  
          } 

       } else {
          inSTDT = 0        
       }
 
       // Start Time
       int inOSTM
       if (mi.in.get("OSTM") != null) {
          inOSTM = mi.in.get("OSTM") 
       } else {
          inOSTM = 0        
       }
 
       // Finish Date
       int inFIDT
       if (mi.in.get("FIDT") != null) {
          inFIDT = mi.in.get("FIDT") 
          
          //Validate date format
          boolean validFIDT = utility.call("DateUtil", "isDateValid", String.valueOf(inFIDT), "yyyyMMdd")  
          if (!validFIDT) {
             mi.error("Finish Date is not valid")   
             return  
          } 

       } else {
          inFIDT = 0        
       }
 
       // Finish Time
       int inOFTM
       if (mi.in.get("OFTM") != null) {
          inOFTM = mi.in.get("OFTM") 
       } else {
          inOFTM = 0        
       }
 
       // Object
       String inKIWG
       if (mi.in.get("KIWG") != null && mi.in.get("KIWG") != "") {
          inKIWG = mi.inData.get("KIWG").trim() 
       } else {
          inKIWG = ""        
       }

       // Text
       String inTX60
       if (mi.in.get("TX60") != null && mi.in.get("TX60") != "") {
          inTX60 = mi.inData.get("TX60").trim() 
       } else {
          inTX60 = ""        
       }

       // Responsible
       String inRESP
       if (mi.in.get("RESP") != null && mi.in.get("RESP") != "") {
          inRESP = mi.inData.get("RESP").trim() 
          
          // Validate responsible if entered
          Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inRESP)
          if (!CMNUSR.isPresent()) {
             mi.error("Responsible doesn't exist")   
             return             
          }
          
       } else {
          inRESP = program.getUser()      
       }

       // Validate PAR record
       Optional<DBContainer> EXTPAR = findEXTPAR(inCONO, inFACI, inPLGR, inTRDT, inTRTM, inTMSX)
       if(EXTPAR.isPresent()){
          mi.error("PAR record already exists")   
          return             
       } else {
          getMPDWCTInfo()
          recordFound = false
          getEXTPARInfo()
          if (recordFound) {
             inTMSX = timeSuffix + 1
          } else {
             inTMSX = 1
          }
          addEXTPARRecord(inCONO, inFACI, inPLGR, inTRDT, inTRTM, inTMSX, department, planningArea, inSHFC, inUPIT, inRUDI, inSEDI, inSTDT, inOSTM, inFIDT, inOFTM, inKIWG, inTX60, inRESP) 
       }  
  
    }
    
      
    //******************************************************************** 
    // Validate EXTPAR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTPAR(int CONO, String FACI, String PLGR, int TRDT, int TRTM, int TMSX){  
       DBAction query = database.table("EXTPAR").index("00").build()
       DBContainer EXTPAR = query.getContainer()
       EXTPAR.set("EXCONO", CONO)
       EXTPAR.set("EXFACI", FACI)
       EXTPAR.set("EXPLGR", PLGR)
       EXTPAR.set("EXTRDT", TRDT)
       EXTPAR.set("EXTRTM", TRTM)
       EXTPAR.set("EXTMSX", TMSX)
       if(query.read(EXTPAR))  { 
         return Optional.of(EXTPAR)
       } 
    
       return Optional.empty()
    }
    


    //******************************************************************** 
    // Get MPDWCT record
    //********************************************************************  
    void getMPDWCTInfo() {   
       DBAction queryMPDWCT = database.table("MPDWCT").index("00").selection("PPDEPT", "PPREAR", "PPRGDT").build()    
       DBContainer containerMPDWCT = queryMPDWCT.getContainer()
       containerMPDWCT.set("PPCONO", inCONO)
       containerMPDWCT.set("PPFACI", inFACI)
       containerMPDWCT.set("PPPLGR", inPLGR)

       int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
       queryMPDWCT.readAll(containerMPDWCT, 3, pageSize, releasedLineProcessorMPDWCT)
    } 

    
    //******************************************************************** 
    // Get MPDWCT record - main loop
    //********************************************************************  
    Closure<?> releasedLineProcessorMPDWCT = { DBContainer containerMPDWCT -> 
        department = containerMPDWCT.get("PPDEPT") 
        planningArea = containerMPDWCT.get("PPREAR") 
        regDate = containerMPDWCT.get("PPRGDT") 
    }
    
    
    //******************************************************************** 
    // Get last EXTPAR record to get the time stamp
    //********************************************************************  
    void getEXTPARInfo() {   
       DBAction queryEXTPAR = database.table("EXTPAR").index("00").selection("EXTMSX").build()    
       DBContainer containerEXTPAR = queryEXTPAR.getContainer()
       containerEXTPAR.set("EXCONO", inCONO)
       containerEXTPAR.set("EXFACI", inFACI)
       containerEXTPAR.set("EXPLGR", inPLGR)
       containerEXTPAR.set("EXTRDT", inTRDT)
       containerEXTPAR.set("EXTRTM", transactionTime)

       int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
       queryEXTPAR.readAll(containerEXTPAR, 5, pageSize, releasedLineProcessorEXTPAR)
    } 

    
    //******************************************************************** 
    // Get EXTPAR record - main loop
    //********************************************************************  
    Closure<?> releasedLineProcessorEXTPAR = { DBContainer containerEXTPAR -> 
        timeSuffix = containerEXTPAR.get("EXTMSX")
        
        if (timeSuffix != null && timeSuffix != 0 ) {
           recordFound = true
        }
    }
  

    //******************************************************************** 
    // Check Work Center
    //******************************************************************** 
    private Optional<DBContainer> findMPDWCT(int CONO, String FACI, String PLGR){  
       DBAction query = database.table("MPDWCT").index("00").build()
       DBContainer MPDWCT = query.getContainer()
       MPDWCT.set("PPCONO", CONO)
       MPDWCT.set("PPFACI", FACI)
       MPDWCT.set("PPPLGR", PLGR)
       if(query.read(MPDWCT))  { 
         return Optional.of(MPDWCT)
       } 
    
       return Optional.empty()
    }

    
    //******************************************************************** 
    // Check Facility
    //******************************************************************** 
    private Optional<DBContainer> findCFACIL(int CONO, String FACI){  
      DBAction query = database.table("CFACIL").index("00").build()   
      DBContainer CFACIL = query.getContainer()
      CFACIL.set("CFCONO", CONO)
      CFACIL.set("CFFACI", FACI)
    
      if(query.read(CFACIL))  { 
        return Optional.of(CFACIL)
      } 
  
      return Optional.empty()
    }


    //******************************************************************** 
    // Check Shift
    //******************************************************************** 
    private Optional<DBContainer> findMPDSHT(int CONO, String FACI, String SHFC){  
       DBAction query = database.table("MPDSHT").index("00").build()
       DBContainer MPDSHT = query.getContainer()
       MPDSHT.set("ICCONO", CONO)
       MPDSHT.set("ICFACI", FACI)
       MPDSHT.set("ICSHFC", SHFC)
       if(query.read(MPDSHT))  { 
         return Optional.of(MPDSHT)
       } 
    
       return Optional.empty()
    }


    //******************************************************************** 
    // Check Disturbance Type
    //******************************************************************** 
    private Optional<DBContainer> findMWDSTC(int CONO, String DICE){  
       DBAction query = database.table("MWDSTC").index("00").build()
       DBContainer MWDSTC = query.getContainer()
       MWDSTC.set("M2CONO", CONO)
       MWDSTC.set("M2DICE", DICE)
       if(query.read(MWDSTC))  { 
         return Optional.of(MWDSTC)
       } 
    
       return Optional.empty()
    }



    //******************************************************************** 
    // Check Responsible
    //******************************************************************** 
    private Optional<DBContainer> findCMNUSR(int CONO, String USID){  
      DBAction query = database.table("CMNUSR").index("00").build()   
      DBContainer CMNUSR = query.getContainer()
      CMNUSR.set("JUCONO", 0)
      CMNUSR.set("JUDIVI", "")
      CMNUSR.set("JUUSID", USID)
    
      if(query.read(CMNUSR))  { 
        return Optional.of(CMNUSR)
      } 
  
      return Optional.empty()
    }
   

    //******************************************************************** 
    // Add EXTPAR record 
    //********************************************************************     
    void addEXTPARRecord(int CONO, String FACI, String PLGR, int TRDT, int TRTM, int TMSX, String DEPT, String REAR, String SHFC, double UPIT, String RUDI, String SEDI, int STDT, int OSTM, int FIDT, int OFTM, String KIWG, String TX60, String RESP){     
         DBAction action = database.table("EXTPAR").index("00").build()
         DBContainer EXTPAR = action.createContainer()
         EXTPAR.set("EXCONO", CONO)
         EXTPAR.set("EXFACI", FACI)
         EXTPAR.set("EXPLGR", PLGR)
         EXTPAR.set("EXTRDT", TRDT) 
         EXTPAR.set("EXTRTM", TRTM) 
         EXTPAR.set("EXTMSX", TMSX) 
         EXTPAR.set("EXDEPT", DEPT) 
         EXTPAR.set("EXREAR", REAR) 
	     EXTPAR.set("EXSHFC", SHFC) 
	     EXTPAR.set("EXUPIT", UPIT) 
	     EXTPAR.set("EXRUDI", RUDI) 
	     EXTPAR.set("EXSEDI", SEDI) 
	     EXTPAR.set("EXSTDT", STDT) 
	     EXTPAR.set("EXOSTM", OSTM) 
	     EXTPAR.set("EXFIDT", FIDT) 
	     EXTPAR.set("EXOFTM", OFTM) 
	     EXTPAR.set("EXKIWG", KIWG) 
	     EXTPAR.set("EXTX60", TX60) 
	     EXTPAR.set("EXRESP", RESP) 
         EXTPAR.set("EXCHID", program.getUser())
         EXTPAR.set("EXCHNO", 1)          
         int regdate = utility.call("DateUtil", "currentDateY8AsInt")
         int regtime = utility.call("DateUtil", "currentTimeAsInt")
         EXTPAR.set("EXRGDT", regdate) 
         EXTPAR.set("EXLMDT", regdate) 
         EXTPAR.set("EXRGTM", regtime)
         action.insert(EXTPAR)         
    } 
  
     
} 

