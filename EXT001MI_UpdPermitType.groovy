// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update permit type in EXTPTT
// Transaction UpdPermitType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: PTPC - Permit Type
 * @param: PTNA - Name
 * @param: PTSW - Slash Withheld
 * @param: PTDE - Description
 * @param: PTDT - Expiration Date
 * 
*/


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class UpdPermitType extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
  
    int inCONO  
    String inPTPC
    String inPTNA
    int inPTSW
    String inPTDE
    int inPTDT
    
    // Constructor 
    public UpdPermitType(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Permit Type
       if (mi.inData.get("PTPC") != null) {
          inPTPC = mi.inData.get("PTPC").trim() 
       } else {
          inPTPC = ""         
       }
        
       // Name
       if (mi.inData.get("PTNA") != null) {
          inPTNA = mi.inData.get("PTNA").trim() 
       } else {
          inPTNA = ""        
       }
       
       // Slash Withheld
       if (mi.in.get("PTSW") != null) {
          inPTSW = mi.in.get("PTSW") 
       } 
       
       // Description
       if (mi.inData.get("PTDE") != null) {
          inPTDE = mi.inData.get("PTDE").trim() 
       } else {
          inPTDE = ""        
       }
  
       // Expiration Date
       if (mi.in.get("PTDT") != null) {
          inPTDT = mi.in.get("PTDT") 
       } 
  
  
       //Validate date format
       boolean validPTDT = isDateValid(String.valueOf(inPTDT), "yyyyMMdd") 
       if(!validPTDT){
          mi.error("Expiration Date is not valid")   
          return  
       } 
  
       // Validate permit type
       Optional<DBContainer> EXTPTT = findEXTPTT(inCONO, inPTPC)
       if(!EXTPTT.isPresent()){
          mi.error("Permit Type doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTPTTRecord()
       
    }
    
    
    
    //******************************************************************** 
    // Check if date format is correct
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
    // Get EXTCTH record
    //******************************************************************** 
    private Optional<DBContainer> findEXTPTT(int cono, String ptpc){  
       DBAction query = database.table("EXTPTT").index("00").build()
       def EXTPTT = query.getContainer()
       EXTPTT.set("EXCONO", cono)
       EXTPTT.set("EXPTPC", ptpc)
       if(query.read(EXTPTT))  { 
         return Optional.of(EXTPTT)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Update EXTPTT record
    //********************************************************************    
    void updEXTPTTRecord(){      
       DBAction action = database.table("EXTPTT").index("00").build()
       DBContainer EXTPTT = action.getContainer()
       EXTPTT.set("EXCONO", inCONO)     
       EXTPTT.set("EXPTPC", inPTPC)
  
       // Read with lock
       action.readLock(EXTPTT, updateCallBackEXTPTT)
    }
     
    Closure<?> updateCallBackEXTPTT = { LockedResult lockedResult -> 
  
       if (inPTNA == "?") {
          lockedResult.set("EXPTNA", "")
       } else {
          if (inPTNA != "") {
             lockedResult.set("EXPTNA", inPTNA)
          }
       }
       
       if (mi.in.get("PTSW") != null) {
          lockedResult.set("EXPTSW", inPTSW)
       } 
       
       if (inPTDE == "?") {
          lockedResult.set("EXPTDE", "")
       } else {
          if (inPTDE != "") {
             lockedResult.set("EXPTDE", inPTDE)
          }
       }
  
       if (mi.in.get("PTDT") != null) {
          lockedResult.set("EXPTDT", inPTDT)
       } 
     
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }
  

} 

