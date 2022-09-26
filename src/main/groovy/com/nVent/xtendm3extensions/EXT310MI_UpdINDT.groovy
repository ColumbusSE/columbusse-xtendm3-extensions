// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-06-14
// @version   1,0 
//
// Description 
// This API is to update the Inventory Date field INDT
// Transaction UpdINDT
// 

import java.time.LocalDate
import java.time.LocalDateTime  
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

public class UpdINDT extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  
    // Definition 
  public int inINDT  
  public String inINDTString
  
  public UpdINDT(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
      
    // Validate warehouse      
     String inWHLO = mi.in.get("WHLO")  
     Optional<DBContainer> MITWHL = findMITWHL(CONO, inWHLO)
     if(!MITWHL.isPresent()){
        mi.error("Warehouse doesn't exists")   
        return             
     } 
     
     // Validate item number
     String inITNO = mi.in.get("ITNO")  
     Optional<DBContainer> MITMAS = findMITMAS(CONO, inITNO)
     if(!MITMAS.isPresent()){
        mi.error("Item doesn't exists")   
        return             
     } 
     
     inINDT = 0
     inINDTString = ""
     
     inINDTString = mi.in.get("INDT")
     
     if (inINDTString == "0") {  
     } else {
       if (!isDateValid(inINDTString, "yyyyMMdd")) {
          mi.error("Date is invalid")   
          return             
       }    
     }
     
     inINDT = Integer.valueOf(inINDTString)

     String inWHSL = mi.in.get("WHSL") 
     
     String inBANO = mi.in.get("BANO")  
     if (inBANO == null) {
       inBANO = ""
     }
     
     String inCAMU = mi.in.get("CAMU")  
     if (inCAMU == null) {
       inCAMU = ""
     }

     Integer inREPN = mi.in.get("REPN") 
     
     // Update INDT in MITLOC
     Optional<DBContainer> MITLOC = findMITLOC(CONO, inWHLO, inITNO, inWHSL, inBANO, inCAMU, inREPN)
     if(!MITLOC.isPresent()){
        mi.error("Location doesn't exists")   
        return             
     } else {
       updMITLOC(CONO, inWHLO, inITNO, inWHSL, inBANO, inCAMU, inREPN, inINDT)
     }

     // Update INDT in MITPCE
     Optional<DBContainer> MITPCE = findMITPCE(CONO, inWHLO, inWHSL)
     if(!MITPCE.isPresent()){
        mi.error("Location doesn't exists")   
        return             
     } else {
       updMITPCE(CONO, inWHLO, inWHSL, inINDT)
     }
     
     // Update INDT in MITBAL
     Optional<DBContainer> MITBAL = findMITBAL(CONO, inWHLO, inITNO)
     if(!MITBAL.isPresent()){
        mi.error("MITBAL record doesn't exists")   
        return             
     } else {
        // Update record 
        updMITBAL(CONO, inWHLO, inITNO, inINDT)
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
  // Check if date is valid
  // @param date Date to check
  // @param format Format of the date
  // @return {@code true} if date is valid
  //******************************************************************** 
  public boolean isDateValid(String date, String format) {
    try {
      LocalDate.parse(date, DateTimeFormatter.ofPattern(format))
      return true
    } catch (DateTimeParseException e) {
      return false
    }
  }


  //******************************************************************** 
  // Get MITWHL record
  //******************************************************************** 
  private Optional<DBContainer> findMITWHL(Integer CONO, String WHLO){  
     DBAction query = database.table("MITWHL").index("00").selection("MWCONO", "MWWHLO").build()
     def MITWHL = query.getContainer()
     MITWHL.set("MWCONO", CONO)
     MITWHL.set("MWWHLO", WHLO)
     if(query.read(MITWHL))  { 
       return Optional.of(MITWHL)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Get MITLOC record
  //******************************************************************** 
  private Optional<DBContainer> findMITLOC(Integer CONO, String WHLO, String ITNO, String WHSL, String BANO, String CAMU, Integer REPN){  
     DBAction query = database.table("MITLOC").index("00").selection("MLCONO", "MLWHLO", "MLITNO", "MLWHSL", "MLBANO", "MLCAMU", "MLREPN").build()
     def MITLOC = query.getContainer()
     MITLOC.set("MLCONO", CONO)
     MITLOC.set("MLWHLO", WHLO)
     MITLOC.set("MLITNO", ITNO)
     MITLOC.set("MLWHSL", WHSL)
     MITLOC.set("MLBANO", BANO)
     MITLOC.set("MLCAMU", CAMU)
     MITLOC.set("MLREPN", REPN)
     if(query.read(MITLOC))  { 
       return Optional.of(MITLOC)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get MITMAS record
  //******************************************************************** 
  private Optional<DBContainer> findMITMAS(Integer CONO, String ITNO){  
     DBAction query = database.table("MITMAS").index("00").selection("MMCONO", "MMITNO").build()
     def MITMAS = query.getContainer()
     MITMAS.set("MMCONO", CONO)
     MITMAS.set("MMITNO", ITNO)
     if(query.read(MITMAS))  { 
       return Optional.of(MITMAS)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Get MITPCE record
  //******************************************************************** 
  private Optional<DBContainer> findMITPCE(Integer CONO, String WHLO, String WHSL){  
     DBAction query = database.table("MITPCE").index("00").selection("MSCONO", "MSWHLO", "MSWHSL").build()
     def MITPCE = query.getContainer()
     MITPCE.set("MSCONO", CONO)
     MITPCE.set("MSWHLO", WHLO)
     MITPCE.set("MSWHSL", WHSL)
     if(query.read(MITPCE))  { 
       return Optional.of(MITPCE)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get MITBAL record
  //******************************************************************** 
  private Optional<DBContainer> findMITBAL(Integer CONO, String WHLO, String ITNO){  
     DBAction query = database.table("MITBAL").index("00").selection("MBCONO", "MBWHLO", "MBITNO").build()
     def MITBAL = query.getContainer()
     MITBAL.set("MBCONO", CONO)
     MITBAL.set("MBWHLO", WHLO)
     MITBAL.set("MBITNO", ITNO)
     if(query.read(MITBAL))  { 
       return Optional.of(MITBAL)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Update MITLOC record
  //********************************************************************    
  void updMITLOC(Integer CONO, String WHLO, String ITNO, String WHSL, String BANO, String CAMU, Integer REPN, Integer INDT){ 
     
     DBAction action = database.table("MITLOC").index("00").selection("MLCONO", "MLWHLO", "MLITNO", "MLWHSL", "MLBANO", "MLCAMU", "MLREPN", "MLINDT", "MLLMDT", "MLCHID", "MLCHNO").build()
     DBContainer MITLOC = action.getContainer()
          
     MITLOC.set("MLCONO", CONO)
     MITLOC.set("MLWHLO", WHLO)
     MITLOC.set("MLITNO", ITNO)
     MITLOC.set("MLWHSL", WHSL)
     MITLOC.set("MLBANO", BANO)
     MITLOC.set("MLCAMU", CAMU)
     MITLOC.set("MLREPN", REPN)

     // Read with lock
     action.readLock(MITLOC, updateCallBackMITLOC)
     }
   
     Closure<?> updateCallBackMITLOC = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("MLCHNO")
     int newChangeNo = changeNo + 1 
     
     lockedResult.set("MLINDT", inINDT) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("MLLMDT", changeddate)  
      
     lockedResult.set("MLCHNO", newChangeNo) 
     lockedResult.set("MLCHID", program.getUser())
     lockedResult.update()
  }


  //******************************************************************** 
  // Update MITBAL record
  //********************************************************************    
  void updMITBAL(Integer CONO, String WHLO, String ITNO, Integer INDT){ 
     
     DBAction action = database.table("MITBAL").index("00").selection("MBCONO", "MBWHLO", "MBITNO", "MBINDT", "MBLMDT", "MBCHID", "MBCHNO").build()
     DBContainer MITBAL = action.getContainer()
          
     MITBAL.set("MBCONO", CONO)
     MITBAL.set("MBWHLO", WHLO)
     MITBAL.set("MBITNO", ITNO)

     // Read with lock
     action.readLock(MITBAL, updateCallBackMITBAL)
     }
   
     Closure<?> updateCallBackMITBAL = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("MBCHNO")
     int newChangeNo = changeNo + 1 
     
     lockedResult.set("MBINDT", inINDT) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("MBLMDT", changeddate)  
      
     lockedResult.set("MBCHNO", newChangeNo) 
     lockedResult.set("MBCHID", program.getUser())
     lockedResult.update()
  }
    
    
  //******************************************************************** 
  // Update MITPCE record
  //********************************************************************    
  void updMITPCE(Integer CONO, String WHLO, String WHSL, Integer INDT){ 
     
     DBAction action = database.table("MITPCE").index("00").selection("MSCONO", "MSWHLO", "MSWHSL", "MSINDT", "MSLMDT", "MSCHID", "MSCHNO").build()
     DBContainer MITPCE = action.getContainer()
          
     MITPCE.set("MSCONO", CONO)
     MITPCE.set("MSWHLO", WHLO)
     MITPCE.set("MSWHSL", WHSL)

     // Read with lock
     action.readLock(MITPCE, updateCallBackMITPCE)
     }
   
     Closure<?> updateCallBackMITPCE = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("MSCHNO")
     int newChangeNo = changeNo + 1 
     
     lockedResult.set("MSINDT", inINDT) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("MSLMDT", changeddate)  
      
     lockedResult.set("MSCHNO", newChangeNo) 
     lockedResult.set("MSCHID", program.getUser())
     lockedResult.update()
  }
  
    
}