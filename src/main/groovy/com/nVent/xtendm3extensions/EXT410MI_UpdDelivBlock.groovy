// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-09-01
// @version   1,0 
//
// Description 
// This API is to update the Block field BLOP
// Transaction UpdDelivBlock
// 

import java.time.LocalDate
import java.time.LocalDateTime  
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

public class UpdDelivBlock extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  
    // Definition 
  public int inBLOP 
  public String status
  
  public UpdDelivBlock(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
          
     Integer inINOU = mi.in.get("INOU")  
     if(inINOU < 1 && inINOU > 4){
        mi.error("Direction is not valid")   
        return             
     } 
     
     Long inDLIX = mi.in.get("DLIX")  
     
     inBLOP = mi.in.get("BLOP")  
     if(inBLOP != 0 && inBLOP != 1){
        mi.error("Block code is not valid")   
        return             
     } 

     Optional<DBContainer> MHDISH = findMHDISH(CONO, inINOU, inDLIX)
     if(!MHDISH.isPresent()){
        mi.error("Delivery doesn't exists")   
        return             
     } else {
        DBContainer containerMHDISH = MHDISH.get() 
        status = containerMHDISH.getString("OQPGRS")
        if (status > "05" ) {
           mi.error("Status is higher than 05, no update allowed")   
           return             
        } else {
           updMHDISH(CONO, inINOU, inDLIX, inBLOP)
        }
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
  // Get MHDISH record
  //******************************************************************** 
  private Optional<DBContainer> findMHDISH(Integer CONO, Integer INOU, Long DLIX){  
     DBAction query = database.table("MHDISH").index("00").selection("OQCONO", "OQINOU", "OQDLIX", "OQPGRS").build()
     def MHDISH = query.getContainer()
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", INOU)
     MHDISH.set("OQDLIX", DLIX)
     if(query.read(MHDISH))  { 
       return Optional.of(MHDISH)
     } 
  
     return Optional.empty()
  }


    
    
  //******************************************************************** 
  // Update MHDISH record
  //********************************************************************    
  void updMHDISH(Integer CONO, Integer INOU, Long DLIX, Integer BLOP){ 
     
     DBAction action = database.table("MHDISH").index("00").selection("OQCONO", "OQINOU", "OQDLIX", "OQBLOP", "OQLMDT", "OQCHID", "OQCHNO").build()
     DBContainer MHDISH = action.getContainer()
          
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", INOU)
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
     
     lockedResult.set("OQBLOP", inBLOP) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OQLMDT", changeddate)  
      
     lockedResult.set("OQCHNO", newChangeNo) 
     lockedResult.set("OQCHID", program.getUser())
     lockedResult.update()
  }
  
    
}