// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-05-01 
// @version   1.0 
//
// Description 
// API transaction DelStockCount will be used to delete a Stock Count record in table EXTSTK
// ERP-9265


/**
 * IN
 * @param: CONO - Company Number
 * @param: STNB - Physical Inventory Number
 * @param: STRN - Line Number
 * @param: RENU - Recount Number
*/



 public class DelStockCount extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO

  // Constructor 
  public DelStockCount(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database
     this.program = program
     this.logger = logger
     this.miCaller = miCaller
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Physical Inventory Number
     int inSTNB
     if (mi.in.get("STNB") != null) {
        inSTNB = mi.in.get("STNB") 
     } else {
        inSTNB = 0        
     }
     
     // Line Number
     int inSTRN
     if (mi.in.get("STRN") != null) {
        inSTRN = mi.in.get("STRN") 
     } else {
        inSTRN = 0        
     }

     // Recount Number
     int inRENU
     if (mi.in.get("RENU") != null) {
        inRENU = mi.in.get("RENU") 
     } else {
        inRENU = 0        
     }


     // Validate Stock Count record
     Optional<DBContainer> EXTSTK = findEXTSTK(inCONO, inSTNB, inSTRN, inRENU)
     if (!EXTSTK.isPresent()) {
        mi.error("EXTSTK record doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTSTKRecord(inCONO, inSTNB, inSTRN, inRENU) 
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
  // Delete record from EXTSTK
  //******************************************************************** 
  void deleteEXTSTKRecord(int CONO, int STNB, int STRN, int RENU){ 
     DBAction action = database.table("EXTSTK").index("00").build()
     DBContainer EXTSTK = action.getContainer()
     EXTSTK.set("EXCONO", CONO)
     EXTSTK.set("EXSTNB", STNB)
     EXTSTK.set("EXSTRN", STRN)
     EXTSTK.set("EXRENU", RENU)
     action.readLock(EXTSTK, deleterCallbackEXTSTK)
  }
    
  Closure<?> deleterCallbackEXTSTK = { LockedResult lockedResult ->  
     lockedResult.delete()
  }

 }