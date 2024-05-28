// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-04-07
// @version   1.0 
//
// Description 
// This API will update a record in table EXTPAR
// Transaction UpdPAR


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


public class UpdPAR extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    Integer inCONO
    String inFACI
    String inPLGR
    int inTRDT
    int inTRTM
    int inTMSX
    String inSHFC
    double inUPIT
    String inRUDI
    String inSEDI
    int inSTDT
    int inOSTM
    int inFIDT
    int inOFTM
    String inKIWG
    String inTX60
    String inRESP
    String department
    String planningArea
    int regDate
    boolean MPDWCTrecordFound
    int timeSuffix

	
    
    // Constructor 
    public UpdPAR(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
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

       // Shift
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
       if (mi.in.get("UPIT") != null) {
          inUPIT = mi.in.get("UPIT") 
       } else {
          inUPIT = 0d       
       }

       // Run Disturbance
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
       if (mi.in.get("OSTM") != null) {
          inOSTM = mi.in.get("OSTM") 
       } else {
          inOSTM = 0        
       }
 
       // Finish Date
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
       if (mi.in.get("OFTM") != null) {
          inOFTM = mi.in.get("OFTM") 
       } else {
          inOFTM = 0        
       }
 
       // Object
       if (mi.in.get("KIWG") != null && mi.in.get("KIWG") != "") {
          inKIWG = mi.inData.get("KIWG").trim() 
       } else {
          inKIWG = ""        
       }

       // Text
       if (mi.in.get("TX60") != null && mi.in.get("TX60") != "") {
          inTX60 = mi.inData.get("TX60").trim() 
       } else {
          inTX60 = ""        
       }

       // Responsible
       if (mi.in.get("RESP") != null && mi.in.get("RESP") != "") {
          inRESP = mi.inData.get("RESP").trim() 
          
          // Validate responsible if entered
          Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inRESP)
          if (!CMNUSR.isPresent()) {
             mi.error("Responsible doesn't exist")   
             return             
          }

       } else {
          inRESP = ""        
       }


       // Validate PAR record
       Optional<DBContainer> EXTPAR = findEXTPAR(inCONO, inFACI, inPLGR, inTRDT, inTRTM, inTMSX)
       if(!EXTPAR.isPresent()){
          mi.error("PAR record doesn't exist")   
          return             
       } else {
          MPDWCTrecordFound = false
          getMPDWCTInfo() 
          // Update record
          updEXTPARRecord()
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
       DBAction queryMPDWCT = database.table("MPDWCT").index("00").selection("PPDEPT", "PPREAR", "PPRGDT").reverse().build()    
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
        regDate = containerMPDWCT.get("PPRGDT") 
        if (!MPDWCTrecordFound && regDate != null) {
           department = containerMPDWCT.get("PPDEPT") 
           planningArea = containerMPDWCT.get("PPREAR") 
           MPDWCTrecordFound = true
        }
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
    // Update EXTPAR record
    //********************************************************************    
    void updEXTPARRecord(){      
       DBAction action = database.table("EXTPAR").index("00").build()
       DBContainer EXTPAR = action.getContainer()
       EXTPAR.set("EXCONO", inCONO)     
       EXTPAR.set("EXFACI", inFACI)
       EXTPAR.set("EXPLGR", inPLGR)
       EXTPAR.set("EXTRDT", inTRDT)
       EXTPAR.set("EXTRTM", inTRTM)
       EXTPAR.set("EXTMSX", inTMSX)
  
       // Read with lock
       action.readLock(EXTPAR, updateCallBackEXTPAR)
       }
     
       Closure<?> updateCallBackEXTPAR = { LockedResult lockedResult -> 

         if (inSHFC == "?") {
            lockedResult.set("EXSHFC", "")
         } else {
            if (inSHFC != "") {
               lockedResult.set("EXSHFC", inSHFC)
            }
         }
         
         if (inUPIT != 0d) {
            lockedResult.set("EXUPIT", inUPIT)
         }
  
         
         if (inRUDI == "?") {
            lockedResult.set("EXRUDI", "")
         } else {
            if (inRUDI != "") {
               lockedResult.set("EXRUDI", inRUDI)
            }
         }
           
         if (inSEDI == "?") {
            lockedResult.set("EXSEDI", "")
         } else {
            if (inSEDI != "") {
               lockedResult.set("EXSEDI", inSEDI)
            }
         }
  
         if (inSTDT != 0) {
            lockedResult.set("EXSTDT", inSTDT)
         }
  
         if (inOSTM != 0) {
            lockedResult.set("EXOSTM", inOSTM)
         }
  
         if (inFIDT != 0) {
            lockedResult.set("EXFIDT", inFIDT)
         }
  
         if (inOFTM != 0) {
            lockedResult.set("EXOFTM", inOFTM)
         }
  
         if (inKIWG == "?") {
            lockedResult.set("EXKIWG", "")
         } else {
            if (inKIWG != "") {
               lockedResult.set("EXKIWG", inKIWG)
            }
         }

         if (inTX60 == "?") {
            lockedResult.set("EXTX60", "")
         } else {
            if (inTX60 != "") {
               lockedResult.set("EXTX60", inTX60)
            }
         }
  
         if (inRESP == "?") {
            lockedResult.set("EXRESP", "")
         } else {
            if (inRESP != "") {
               lockedResult.set("EXRESP", inRESP)
            }
         }
   
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }


} 

