// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-04-11
// @version   1.0 
//
// Description 
// This API will delete a record in table EXTPAR
// Transaction DelPAR



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


 public class DelPAR extends ExtendM3Transaction {
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
    
    // Constructor 
    public DelPAR(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
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

       // Validate PAR record
       Optional<DBContainer> EXTPAR = findEXTPAR(inCONO, inFACI, inPLGR, inTRDT, inTRTM, inTMSX)
       if(!EXTPAR.isPresent()){
          mi.error("PAR record doesn't exist")   
          return             
       } else {
          deleteEXTPARRecord()
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
    // Delete record from EXTPAR
    //******************************************************************** 
    void deleteEXTPARRecord(){ 
      DBAction action = database.table("EXTPAR").index("00").build()
      DBContainer EXTPAR = action.getContainer()
      EXTPAR.set("EXCONO", inCONO)
      EXTPAR.set("EXFACI", inFACI)
      EXTPAR.set("EXPLGR", inPLGR)
      EXTPAR.set("EXTRDT", inTRDT)
      EXTPAR.set("EXTRTM", inTRTM)
      EXTPAR.set("EXTMSX", inTMSX)
      action.readLock(EXTPAR, deleterCallbackEXTPAR)
    }
    
    Closure<?> deleterCallbackEXTPAR = { LockedResult lockedResult ->  
      lockedResult.delete()
    }
  

 }