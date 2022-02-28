// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-10-29
// @version   1.0 
//
// Description 
// This API is to update fields on the delivery header to reset a download of a delivery in MYS410
// Transaction ResetDownload
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class ResetDownload extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  
  public ResetDownload(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
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
        // Update record 
        updRecord(CONO, DLIX)
     }  

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
     DBAction query = database.table("MHDISH").index("00").selection("OQDLIX").build()
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
  // Update MHDISH record
  //********************************************************************    
  void updRecord(Integer CONO, Long DLIX){ 
     
     DBAction action = database.table("MHDISH").index("00").selection("OQDLIX", "OQIRST", "OQMSGN", "OQLMDT", "OQCHNO", "OQCHID").build()
     DBContainer MHDISH = action.getContainer()
          
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", 1)
     MHDISH.set("OQDLIX", DLIX)

     // Read with lock
     action.readLock(MHDISH, updateCallBackMHDISH)
     }
   
     Closure<?> updateCallBackMHDISH = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("OQCHNO")
     int newChangeNo = changeNo + 1 
        
     lockedResult.set("OQIRST", "10") 
     lockedResult.set("OQMSGN", "               ") 
           
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OQLMDT", changeddate)  
      
     lockedResult.set("OQCHNO", newChangeNo) 
     lockedResult.set("OQCHID", program.getUser())
     lockedResult.update()
  }
     
    
}