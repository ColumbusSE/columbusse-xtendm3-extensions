// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-01-30 
// @version   1.0 
//
// Description 
// API transaction UpdVoucherRev will be used to update a voucher reversal record in table EXTGLV
// ERP-9012


/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: YEA4 - Year
 * @param: VONO - Voucher
 * @param: VSER - Voucher Number Series
 * @param: RECD - Requested User
 * @param: APCD - Authorized Use
 * @param: REDT - Reversal Date
 * @param: APDT - Date Approved
 * @param: A256 - Comments 1
 * @param: A257 - Comments 2
 * @param: A258 - Comments 3
 * 
*/



public class UpdVoucherRev extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  int inCONO
  String inDIVI
  int inYEA4
  int inVONO 
  String inVSER
  String inRECD
  String inAPCD
  int inREDT
  int inAPDT
  String inA256
  String inA257
  String inA258

  
  // Constructor 
  public UpdVoucherRev(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger 
     this.utility = utility
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
         
     // Year
     if (mi.in.get("YEA4") != null) {
        inYEA4 = mi.in.get("YEA4") 
     } else {
        inYEA4 = 0        
     }
     
     // Voucher Number
     if (mi.in.get("VONO") != null) {
        inVONO = mi.in.get("VONO") 
     } else {
        inVONO = 0        
     }

     // Voucher Number Series
     if (mi.in.get("VSER") != null && mi.in.get("VSER") != "") {
        inVSER = mi.inData.get("VSER").trim()
     } else {
        inVSER = ""        
     }

     // Requested User
     if (mi.in.get("RECD") != null && mi.in.get("RECD") != "") {
        inRECD = mi.inData.get("RECD").trim()
        
        // Validate User
        Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inDIVI, inRECD)
        if(CMNUSR.isPresent()){
           mi.error("Requested User doesn't exist")   
           return             
        } 

     } else {
        inRECD = ""        
     }

     // Authorized User
     if (mi.in.get("APCD") != null && mi.in.get("APCD") != "") {
        inAPCD = mi.inData.get("APCD").trim()
        
        // Validate User
        Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inDIVI, inAPCD)
        if(CMNUSR.isPresent()){
           mi.error("Authorized User doesn't exist")   
           return             
        } 

     } else {
        inAPCD = ""        
     }

     // Requested Date
     if (mi.in.get("REDT") != null) {
        inREDT = mi.in.get("REDT") 
        
        //Validate date format
        boolean validREDT = utility.call("DateUtil", "isDateValid", String.valueOf(inREDT), "yyyyMMdd")  
        if (!validREDT) {
           mi.error("Requested Date is not valid")   
           return  
        } 

     }

     // Date Approved
     if (mi.in.get("APDT") != null) {
        inAPDT = mi.in.get("APDT") 
        
        //Validate date format
        boolean validAPDT = utility.call("DateUtil", "isDateValid", String.valueOf(inAPDT), "yyyyMMdd")  
        if (!validAPDT) {
           mi.error("Requested Date is not valid")   
           return  
        } 

     }

     // Comments
     if (mi.in.get("A256") != null && mi.in.get("A256") != "") {
        inA256 = mi.inData.get("A256").trim()
     } else {
        inA256 = ""        
     }
	   if (mi.in.get("A257") != null && mi.in.get("A257") != "") {
        inA257 = mi.inData.get("A257").trim()
     } else {
        inA257 = ""        
     }
	   if (mi.in.get("A258") != null && mi.in.get("A258") != "") {
        inA258 = mi.inData.get("A258").trim()
     } else {
        inA258 = ""        
     }
     // Validate Voucher Revesal Record
     Optional<DBContainer> EXTGLV = findEXTGLV(inCONO, inDIVI, inYEA4, inVONO, inVSER)
     if (!EXTGLV.isPresent()) {
        mi.error("Matching voucher record does not exist")   
        return             
     } else {
        // Update record
        updEXTGLVRecord()            
     }
     
  }
  

  //******************************************************************** 
  // Validate EXTGLV record
  //******************************************************************** 
  private Optional<DBContainer> findEXTGLV(int CONO, String DIVI, int YEA4, int VONO, String VSER){  
     DBAction query = database.table("EXTGLV").index("00").build()
     DBContainer EXTGLV = query.getContainer()
     EXTGLV.set("EXCONO", CONO)
     EXTGLV.set("EXDIVI", DIVI)
     EXTGLV.set("EXYEA4", YEA4)
     EXTGLV.set("EXVONO", VONO)
     EXTGLV.set("EXVSER", VSER)
     if(query.read(EXTGLV))  { 
       return Optional.of(EXTGLV)
     } 
  
     return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Validate CMNUSR record
  //******************************************************************** 
  private Optional<DBContainer> findCMNUSR(int CONO, String DIVI, String USID){  
     DBAction query = database.table("CMNUSR").index("00").build()
     DBContainer CMNUSR = query.getContainer()
     CMNUSR.set("JUCONO", CONO)
     CMNUSR.set("JUDIVI", DIVI)
     CMNUSR.set("JUUSID", USID)
     if(query.read(CMNUSR))  { 
       return Optional.of(CMNUSR)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTGLV record
  //********************************************************************    
  void updEXTGLVRecord(){      
     DBAction action = database.table("EXTGLV").index("00").build()
     DBContainer EXTGLV = action.getContainer()    
     EXTGLV.set("EXCONO", inCONO)     
     EXTGLV.set("EXDIVI", inDIVI)  
     EXTGLV.set("EXYEA4", inYEA4)
     EXTGLV.set("EXVONO", inVONO)
     EXTGLV.set("EXVSER", inVSER)

     // Read with lock
     action.readAllLock(EXTGLV, 5, updateCallBackEXTGLV)
     }
   
     Closure<?> updateCallBackEXTGLV = { LockedResult lockedResult ->   
       if (mi.in.get("RECD") != null) {
          if (inRECD == "?") {
             lockedResult.set("EXRECD", "")
          } else {     
            if (inRECD != "") {
               lockedResult.set("EXRECD", inRECD)
            }
          }
       }
  
       if (mi.in.get("APCD") != null) {
          if (inAPCD == "?") {
             lockedResult.set("EXAPCD", "")
          } else {     
            if (inAPCD != "") {
               lockedResult.set("EXAPCD", inAPCD)
            }
          }
       }
       
       if (mi.in.get("REDT") != null && mi.in.get("REDT") != "") {  
          lockedResult.set("EXREDT", inREDT)
       }
  
       if (mi.in.get("APDT") != null && mi.in.get("APDT") != "") {
          lockedResult.set("EXAPDT", inAPDT)
       }
  
       if (mi.in.get("A256") != null && inA256 != "") {
          if (inA256 == "?") {
             lockedResult.set("EXA256", "")
          } else {     
            if (inA256 != "") {
               lockedResult.set("EXA256", inA256)
            }
          }
       }
       
	     if (mi.in.get("A257") != null && inA257 != "") {
          if (inA257 == "?") {
             lockedResult.set("EXA257", "")
          } else {     
            if (inA257 != "") {
               lockedResult.set("EXA257", inA257)
            }
          }
       }
       
	     if (mi.in.get("A258") != null && inA258 != "") {
          if (inA258 == "?") {
             lockedResult.set("EXA258", "")
          } else {     
            if (inA258 != "") {
               lockedResult.set("EXA258", inA258)
            }
          }
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

