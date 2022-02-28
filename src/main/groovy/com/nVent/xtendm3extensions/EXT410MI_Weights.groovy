// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-01-10
// @version   1,0 
//
// Description 
// This API is used to update fields on the delivery header to reset a download of a delivery in MYS410
// Transaction ResetDownload
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class UpdWeights extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  
  //Definition
  public String partner;
  public String message;
  public int direction; 
  public double netWeight;
  public double grossWeight;
  public double volume;
  public double estFreeUnit;

  
  public UpdWeights(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
     // Validate company
     Integer CONO = mi.in.get("CONO")      
     if (CONO == null) {
        CONO = program.LDAZD.CONO as Integer
     } 
     Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
     if(!CMNCMP.isPresent()){                         
       mi.error("Company " + CONO + " is invalid")   
       return                                         
     }   
     
     // Validate delivery in MHDISH
     Long DLIX = mi.in.get("DLIX")  
     Optional<DBContainer> MHDISH = findMHDISH(CONO, DLIX)
     if(!MHDISH.isPresent()){
        mi.error("MHDISH record doesn't exists")   
        return             
     } else {
        DBContainer containerMHDISH = MHDISH.get() 
        message = containerMHDISH.getString("OQMSGN")
        direction = containerMHDISH.get("OQINOU")  
        netWeight = containerMHDISH.get("OQNEWE")  
        grossWeight = containerMHDISH.get("OQGRWE")  
        volume = containerMHDISH.get("OQVOL3")  
        estFreeUnit = containerMHDISH.get("OQFCU1")  
     } 
     
     partner = mi.in.get("E0PA")  
     
     // Validate record in MYOPID
     Optional<DBContainer> MYOPID = findMYOPID(CONO, DLIX, partner, message)
     if(!MYOPID.isPresent()){
        mi.error("MYOPID record doesn't exists")   
        return             
     } 
     
     // Update record 
     updRecord(CONO, partner, message, DLIX)

  }
 
  public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
  }

  //******************************************************************** 
  // Get Company record
  //********************************************************************     
  private Optional<DBContainer> findCMNCMP(Integer CONO){                             
      DBAction query = database.table("CMNCMP").index("00").selection("JICONO").build()   
      DBContainer CMNCMP = query.getContainer()                                           
      CMNCMP.set("JICONO", CONO)                                                         
      if(query.read(CMNCMP))  {                                                           
        return Optional.of(CMNCMP)                                                        
      }                                                                                  
      return Optional.empty()                                                            
  }     

  //******************************************************************** 
  // Get MHDISH record
  //******************************************************************** 
  private Optional<DBContainer> findMHDISH(Integer CONO, Long DLIX){  
     DBAction query = database.table("MHDISH").index("00").selection("OQDLIX", "OQINOU", "OQE0PA", "OQMSGN", "OQNEWE", "OQGRWE", "OQVOL3", "OQFCU1").build()
     def MHDISH = query.getContainer()
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", 1)
     MHDISH.set("OQDLIX", DLIX)
     if(query.read(MHDISH))  { 
       return Optional.of(MHDISH)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Get MYOPID record
  //******************************************************************** 
  private Optional<DBContainer> findMYOPID(Integer CONO, Long DLIX, String E0PA, String MSGN){  
     DBAction query = database.table("MYOPID").index("00").selection("D1DLIX", "D1INOU", "D1MSGN", "D1NEWE", "D1GRWE", "D1VOL3", "D1FCU1").build()
     def MYOPID = query.getContainer()
     MYOPID.set("D1CONO", CONO)
     MYOPID.set("D1E0PA", E0PA)
     MYOPID.set("D1MSGN", MSGN)
     MYOPID.set("D1INOU", 1)
     MYOPID.set("D1DLIX", DLIX)     
     if(query.read(MYOPID))  { 
       return Optional.of(MYOPID)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update MYOPID record
  //********************************************************************    
  void updRecord(Integer CONO, String E0PA, String MSGN, Long DLIX){ 
     
     DBAction action = database.table("MYOPID").index("00").selection("D1DLIX", "D1INOU", "D1MSGN", "D1NEWE", "D1GRWE", "D1VOL3", "D1FCU1").build()
     DBContainer MYOPID = action.getContainer()
          
     MYOPID.set("D1CONO", CONO)
     MYOPID.set("D1E0PA", E0PA)
     MYOPID.set("D1MSGN", MSGN)
     MYOPID.set("D1INOU", 1)
     MYOPID.set("D1DLIX", DLIX)     

     // Read with lock
     action.readLock(MYOPID, updateCallBackMYOPID)
     }
   
     Closure<?> updateCallBackMYOPID = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("D1CHNO")
     int newChangeNo = changeNo + 1 
        
     lockedResult.set("D1NEWE", netWeight) 
     lockedResult.set("D1GRWE", grossWeight) 
     lockedResult.set("D1VOL3", volume) 
     lockedResult.set("D1FCU1", estFreeUnit) 
           
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("D1LMDT", changeddate)  
      
     lockedResult.set("D1CHNO", newChangeNo) 
     lockedResult.set("D1CHID", program.getUser())
     lockedResult.update()
  }
     
    
}