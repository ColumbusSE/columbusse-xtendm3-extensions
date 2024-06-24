// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-05-01 
// @version   1.0 
//
// Description 
// API transaction UpdStockCount will be used to update a Stock Count record in table EXTSTK
// ERP-9265


/**
 * IN
 * @param: CONO - Company Number
 * @param: STNB - Physical Inventory Number
 * @param: STRN - Line Number
 * @param: RENU - Recount Number
 * @param: STQI - Stock Count Balance
*/



public class UpdStockCount extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  int inSTNB
  int inSTRN
  int inRENU
  double inSTQI

  // Constructor 
  public UpdStockCount(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger  
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Physical Inventory Number
     if (mi.in.get("STNB") != null) {
        inSTNB = mi.in.get("STNB") 
     } else {
        inSTNB = 0        
     }
     
     // Line Number
     if (mi.in.get("STRN") != null) {
        inSTRN = mi.in.get("STRN") 
     } else {
        inSTRN = 0        
     }

     // Recount Number
     if (mi.in.get("RENU") != null) {
        inRENU = mi.in.get("RENU") 
     } else {
        inRENU = 0        
     }

     // Stock Count Balance
     if (mi.in.get("STQI") != null) {
        inSTQI = mi.in.get("STQI") 
     } else {
        inSTQI = 0d        
     }


     // Validate Stock Count record
     Optional<DBContainer> EXTSTK = findEXTSTK(inCONO, inSTNB, inSTRN, inRENU)
     if (!EXTSTK.isPresent()) {
        mi.error("EXTSTK record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTSTKRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Validate EXTSTK record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSTK(int CONO, int STNB, int STRN, int RENU){  
     DBAction query = database.table("EXTSTK").index("00").build()
     DBContainer EXTSTK = query.getContainer()
     EXTSTK.set("EXCONO", CONO)
     EXTSTK.set("EXSTNB", STNB)
     EXTSTK.set("EXSTRN", STRN)
     EXTSTK.set("EXRENU", RENU)
     if(query.read(EXTSTK))  { 
       return Optional.of(EXTSTK)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTSTK record
  //********************************************************************    
  void updEXTSTKRecord(){      
     DBAction action = database.table("EXTSTK").index("00").build()
     DBContainer EXTSTK = action.getContainer()     
     EXTSTK.set("EXCONO", inCONO)
     EXTSTK.set("EXSTNB", inSTNB)
     EXTSTK.set("EXSTRN", inSTRN)
     EXTSTK.set("EXRENU", inRENU)

     // Read with lock
     action.readLock(EXTSTK, updateCallBackEXTSTK)
     }
   
     Closure<?> updateCallBackEXTSTK = { LockedResult lockedResult ->      
     if (mi.in.get("STQI") != null) {
        lockedResult.set("EXSTQI", mi.in.get("STQI"))
     }

     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     int changedate = utility.call("DateUtil", "currentDateY8AsInt")
     lockedResult.set("EXLMDT", changedate)       
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }

} 

