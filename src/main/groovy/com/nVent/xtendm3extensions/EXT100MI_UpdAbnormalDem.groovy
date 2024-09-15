// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-01-11
// @version   1.0 
//
// Description 
// This API is to update the Abnormal Demand field on the order line
// Transaction UpdAbnormalDem
// 

public class UpdAbnormalDem extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger  
  
  int inCONO
  String inORNO
  int inPONR
  int inPOSX
  int inABNO

  public UpdAbnormalDem(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Validate Order Number, ORNO, in OOHEAD
     inORNO = mi.in.get("ORNO")  
     Optional<DBContainer> OOHEAD = findOOHEAD(inCONO, inORNO)
     if (!OOHEAD.isPresent()) {
        mi.error("OOHEAD record doesn't exists")   
        return             
     } 
    
     // Line Number, PONR
     inPONR = mi.in.get("PONR")  

     // Line Suffix, POSX
     inPOSX = mi.in.get("POSX")  

     // Validate Abnormal Demand, ABNO
     inABNO = mi.in.get("ABNO")  
     if (inABNO != 0  && inABNO != 1) {
        mi.error("Abnormal Demand can only be 0 or 1")   
        return             
     } 

     // Validate OOLINE
     Optional<DBContainer> OOLINE = findOOLINE(inCONO, inORNO, inPONR, inPOSX)
     if (!OOLINE.isPresent()) {
        mi.error("OOLINE record doesn't exists")   
        return             
     } else {
        // Update record 
        updRecord()
     }  


  }


  //******************************************************************** 
  // Validate OOHEAD record
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(Integer CONO, String ORNO){  
     DBAction query = database.table("OOHEAD").index("00").build()
     def OOHEAD = query.getContainer()
     OOHEAD.set("OACONO", CONO)
     OOHEAD.set("OAORNO", ORNO)
     if(query.read(OOHEAD))  { 
       return Optional.of(OOHEAD)
     } 
  
     return Optional.empty()
  }
 
 
  //******************************************************************** 
  // Validate OOLINE record
  //******************************************************************** 
  private Optional<DBContainer> findOOLINE(Integer CONO, String ORNO, int PONR, int POSX){  
     DBAction query = database.table("OOLINE").index("00").build()
     def OOLINE = query.getContainer()
     OOLINE.set("OBCONO", CONO)
     OOLINE.set("OBORNO", ORNO)
     OOLINE.set("OBPONR", PONR)
     OOLINE.set("OBPOSX", POSX)
     if(query.read(OOLINE))  { 
       return Optional.of(OOLINE)
     } 
  
     return Optional.empty()
  }

 
  void updRecord(){ 
     DBAction action = database.table("OOLINE").index("00").build()
     DBContainer OOLINE = action.getContainer()
     OOLINE.set("OBCONO", inCONO)
     OOLINE.set("OBORNO", inORNO)
     OOLINE.set("OBPONR", inPONR)
     OOLINE.set("OBPOSX", inPOSX)

     // Read with lock
     action.readLock(OOLINE, updateCallBack)
  }

    
  Closure<?> updateCallBack = { LockedResult lockedResult -> 
     // Update the fields if filled
     if(mi.in.get("ABNO") != null){  
        lockedResult.set("OBABNO", mi.in.get("ABNO")) 
     }
     
     // Update changed information
     int changeNo = lockedResult.get("OBCHNO")
     int newChangeNo = changeNo + 1 
     int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
     lockedResult.set("OBLMDT", changeddate)       
     lockedResult.set("OBCHNO", newChangeNo) 
     lockedResult.set("OBCHID", program.getUser())
     lockedResult.update()
  }
    
}