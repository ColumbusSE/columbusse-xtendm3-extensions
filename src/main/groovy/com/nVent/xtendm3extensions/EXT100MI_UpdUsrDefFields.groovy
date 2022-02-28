// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-02-14
// @version   1,0 
//
// Description 
// This API is to update the User Defined fields on the order header
// Transaction UpdUsrDefFields
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class UpdUsrDefFields extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;  
  
  
  public UpdUsrDefFields(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
     
     // Validate OOHEAD
     String ORNO = mi.in.get("ORNO")  
     Optional<DBContainer> OOHEAD = findOOHEAD(CONO, ORNO)
     if(!OOHEAD.isPresent()){
        mi.error("OOHEAD record doesn't exists")   
        return             
     } else {
        // Update record 
        updRecord()
     }  

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
     DBAction query = database.table("OOHEAD").index("00").selection("OAORNO", "OAUCA0", "OAUCA1", "OAUCA2", "OAUCA3", "OAUCA4", "OAUCA5", "OAUCA6", "OAUCA7", "OAUCA8", "OAUCA9").build()
     def OOHEAD = query.getContainer()
     OOHEAD.set("OACONO", CONO)
     OOHEAD.set("OAORNO", ORNO)
     if(query.read(OOHEAD))  { 
       return Optional.of(OOHEAD)
     } 
  
     return Optional.empty()
  }
 
 
  public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
    }
    
    
  void updRecord(){ 
     int company = mi.in.get("CONO") 
     
     DBAction action = database.table("OOHEAD").index("00").selection("OAORNO", "OAUCA0", "OAUCA1", "OAUCA2", "OAUCA3", "OAUCA4", "OAUCA5", "OAUCA6", "OAUCA7", "OAUCA8", "OAUCA9", "OALMDT", "OACHID", "OACHNO").build()
     DBContainer OOHEAD = action.getContainer()
      
     
     OOHEAD.set("OACONO", company)
     OOHEAD.set("OAORNO", mi.in.get("ORNO"))

     // Read with lock
     action.readLock(OOHEAD, updateCallBack)
  }

    
  Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("OACHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the fields if filled
     if(mi.in.get("UCA0") != null){  
        if(mi.in.get("UCA0") == "?"){  
          lockedResult.set("OAUCA0", "                    ") 
        } else {
          lockedResult.set("OAUCA0", mi.in.get("UCA0")) 
        }
     }
     if(mi.in.get("UCA1") != null){  
        if(mi.in.get("UCA1") == "?"){  
          lockedResult.set("OAUCA1", "                    ") 
        } else {
          lockedResult.set("OAUCA1", mi.in.get("UCA1")) 
        }
     }
     if(mi.in.get("UCA2") != null){  
        if(mi.in.get("UCA2") == "?"){  
          lockedResult.set("OAUCA2", "                    ") 
        } else {
          lockedResult.set("OAUCA2", mi.in.get("UCA2")) 
        }
     }
     if(mi.in.get("UCA3") != null){  
        if(mi.in.get("UCA3") == "?"){  
          lockedResult.set("OAUCA3", "                    ") 
        } else {
          lockedResult.set("OAUCA3", mi.in.get("UCA3")) 
        }
     }
     if(mi.in.get("UCA4") != null){  
        if(mi.in.get("UCA4") == "?"){  
          lockedResult.set("OAUCA4", "                    ") 
        } else {
          lockedResult.set("OAUCA4", mi.in.get("UCA4")) 
        }
     }
     if(mi.in.get("UCA5") != null){  
        if(mi.in.get("UCA5") == "?"){  
          lockedResult.set("OAUCA5", "                    ") 
        } else {
          lockedResult.set("OAUCA5", mi.in.get("UCA5")) 
        }
     }
     if(mi.in.get("UCA6") != null){  
        if(mi.in.get("UCA6") == "?"){  
          lockedResult.set("OAUCA6", "                    ") 
        } else {
          lockedResult.set("OAUCA6", mi.in.get("UCA6")) 
        }
     }
     if(mi.in.get("UCA7") != null){  
        if(mi.in.get("UCA7") == "?"){  
          lockedResult.set("OAUCA7", "                    ") 
        } else {
          lockedResult.set("OAUCA7", mi.in.get("UCA7")) 
        }
     }
     if(mi.in.get("UCA8") != null){  
        if(mi.in.get("UCA8") == "?"){  
          lockedResult.set("OAUCA8", "                    ") 
        } else {
          lockedResult.set("OAUCA8", mi.in.get("UCA8")) 
        }
     }
     if(mi.in.get("UCA9") != null){  
        if(mi.in.get("UCA9") == "?"){  
          lockedResult.set("OAUCA9", "                    ") 
        } else {
          lockedResult.set("OAUCA9", mi.in.get("UCA9")) 
        }
     }

     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OALMDT", changeddate)  
      
     lockedResult.set("OACHNO", newChangeNo) 
     lockedResult.set("OACHID", program.getUser())
     lockedResult.update()
  }
    
}