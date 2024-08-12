// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-08-09
// @version   1.0 
//
// Description 
// This API is to update the IMDT field in CININD
// Transaction UpdCININD
// 


public class UpdCININD extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger

  int inCONO
  String inDIVI
  int inANBR
  int inSENO
  int inIMDT

  public UpdCININD(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
    this.mi = mi
    this.database = database 
    this.program = program
    this.utility = utility
    this.logger = logger
  }
  
  public void main() {
    
     // Set Company Number
     if (mi.in.get("CONO") != null && mi.in.get("CONO") != 0) {
        inCONO = mi.in.get("CONO")  
     } else {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division     
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim()
     } else {
        inDIVI = program.LDAZD.DIVI
     }
     
     // Accounting Number
     if (mi.in.get("ANBR") != null) {
        inANBR = mi.in.get("ANBR")  
     }

     // Sequence Number
     if (mi.in.get("SENO") != null) {
        inSENO = mi.in.get("SENO")  
     } 
     
     // Invoice Matching Date
     if (mi.in.get("IMDT") != null) {
        inIMDT = mi.in.get("IMDT") 
        
        //Validate date format
        boolean validIMDT = utility.call("DateUtil", "isDateValid", String.valueOf(inIMDT), "yyyyMMdd")  
        if (!validIMDT) {
           mi.error("Invoice Matching Date is not valid")   
           return  
        } 
     }

     // Validate record in CININD
     Optional<DBContainer> CININD = findCININD(inCONO, inDIVI, inANBR, inSENO)
     if(!CININD.isPresent()){
        mi.error("CININD record doesn't exists")   
        return             
     } 

     // Update record 
     updRecord()
  }
  
  public  boolean isNullOrEmpty(String key) {
      if(key != null && !key.isEmpty())
          return false;
      return true;
  }
  
  
  //******************************************************************** 
  // Get CININD record
  //******************************************************************** 
  private Optional<DBContainer> findCININD(int CONO, String DIVI, int ANBR, int SENO){  
     DBAction query = database.table("CININD").index("00").build()
     DBContainer CININD = query.getContainer()
     CININD.set("EXCONO", CONO)
     CININD.set("EXDIVI", DIVI)
     CININD.set("EXANBR", ANBR)
     CININD.set("EXSENO", SENO)
     if(query.read(CININD))  { 
       return Optional.of(CININD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update CININD record
  //******************************************************************** 
  void updRecord(){ 
     DBAction action = database.table("CININD").index("00").build()
     DBContainer ext = action.getContainer()
     ext.set("EXCONO", mi.in.get("CONO"))
     ext.set("EXDIVI", mi.in.get("DIVI"))
     ext.set("EXANBR", mi.in.get("ANBR"))
     ext.set("EXSENO", mi.in.get("SENO"))
     
     // Read with lock
     action.readLock(ext, updateCallBack)
     }
     
     Closure<?> updateCallBack = { LockedResult lockedResult -> 
     
       // Update the fields if filled
       if (!isNullOrEmpty(mi.in.get("IMDT").toString())) { 
          lockedResult.set("EXIMDT", mi.in.get("IMDT")) 
       }
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
       
  }


}