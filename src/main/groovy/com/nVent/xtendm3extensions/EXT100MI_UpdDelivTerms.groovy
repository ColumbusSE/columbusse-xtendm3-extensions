// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-28
// @version   1.0 
//
// Description 
// This API is to update the delivery terms field on the order header
// Transaction UpdDelivTerms
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class UpdDelivTerms extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  String inTEDL
  String inLNCD
  String inORNO
  
  public UpdDelivTerms(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
     
     // Validate OOHEAD
     inORNO = mi.in.get("ORNO")  
     Optional<DBContainer> OOHEAD = findOOHEAD(CONO, inORNO)
     if(!OOHEAD.isPresent()){
        mi.error("OOHEAD record doesn't exists")   
        return             
     } else {
        DBContainer OOHEADContainer = OOHEAD.get() 
        inLNCD = OOHEADContainer.getString("OALNCD")
     }
     
    // Validate Delivery Terms in CSYTAB
     inTEDL = mi.in.get("TEDL")  
     Optional<DBContainer> CSYTAB = findCSYTAB(CONO, inTEDL, inLNCD)
     if(!CSYTAB.isPresent()){
        mi.error("Delivery Terms doesn't exists")   
        return             
     } 
      
     updRecord(CONO, inORNO)

  }
 

  //******************************************************************** 
  // Get Company record
  //********************************************************************     
  private Optional<DBContainer> findCMNCMP(Integer CONO){                             
      DBAction query = database.table("CMNCMP").index("00").build()   
      DBContainer CMNCMP = query.getContainer()                                           
      CMNCMP.set("JICONO", CONO)                                                         
      if(query.read(CMNCMP))  {                                                           
        return Optional.of(CMNCMP)                                                        
      }                                                                                  
      return Optional.empty()                                                            
  }     

  //******************************************************************** 
  // Validate CSYTAB record
  //******************************************************************** 
  private Optional<DBContainer> findCSYTAB(Integer CONO, String TEDL, String LNCD){  
     DBAction query = database.table("CSYTAB").index("00").build()
     def CSYTAB = query.getContainer()
     CSYTAB.set("CTCONO", CONO)
     CSYTAB.set("CTDIVI", "")
     CSYTAB.set("CTSTCO", "TEDL")
     CSYTAB.set("CTSTKY", TEDL)
     CSYTAB.set("CTLNCD", LNCD)
     if(query.read(CSYTAB))  { 
       return Optional.of(CSYTAB)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Validate OOHEAD record
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(Integer CONO, String ORNO){  
     DBAction query = database.table("OOHEAD").index("00").selection("OALNCD").build()
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
  void updRecord(Integer CONO, String ORNO){ 
     
     DBAction action = database.table("OOHEAD").index("00").build()
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
     
     lockedResult.set("OATEDL", inTEDL) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OALMDT", changeddate)  
      
     lockedResult.set("OACHNO", newChangeNo) 
     lockedResult.set("OACHID", program.getUser())
     lockedResult.update()
  }
     
    
}