// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-10-29
// @version   1,0 
//
// Description 
// This API is to update the priority field on the order header
// Transaction UpdOrderHeader
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class UpdOrderHeader extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  
  public UpdOrderHeader(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
     
     // Priority
     Integer OPRI = mi.in.get("OPRI")  

     // Validate OOHEAD
     String ORNO = mi.in.get("ORNO")  
     Optional<DBContainer> OOHEAD = findOOHEAD(CONO, ORNO)
     if(!OOHEAD.isPresent()){
        mi.error("OOHEAD record doesn't exists")   
        return             
     } else {
        // Update record 
        updRecord(CONO, ORNO, OPRI)
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
  // Get OOHEAD record
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(Integer CONO, String ORNO){  
     DBAction query = database.table("OOHEAD").index("00").selection("OAORNO", "OAOPRI").build()
     def OOHEAD = query.getContainer()
     OOHEAD.set("OACONO", CONO)
     OOHEAD.set("OAORNO", ORNO)
     if(query.read(OOHEAD))  { 
       return Optional.of(OOHEAD)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update OOHEAD record
  //********************************************************************    
  void updRecord(Integer CONO, String ORNO, Integer OPRI){ 
     
     DBAction action = database.table("OOHEAD").index("00").selection("OAORNO", "OAOPRI", "OALMDT", "OACHNO", "OACHID").build()
     DBContainer OOHEAD = action.getContainer()
          
     OOHEAD.set("OACONO", CONO)
     OOHEAD.set("OAORNO", ORNO)

     // Read with lock
     action.readLock(OOHEAD, updateCallBackOOHEAD)
     }
   
     Closure<?> updateCallBackOOHEAD = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("OACHNO")
     int newChangeNo = changeNo + 1 
     
     if(!isNullOrEmpty(mi.in.get("OPRI").toString())){
        lockedResult.set("OAOPRI", mi.in.get("OPRI")) 
     }
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OALMDT", changeddate)  
      
     lockedResult.set("OACHNO", newChangeNo) 
     lockedResult.set("OACHID", program.getUser())
     lockedResult.update()
  }
     
    
}