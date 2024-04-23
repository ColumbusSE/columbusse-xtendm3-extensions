// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-01-30 
// @version   1.0 
//
// Description 
// API transaction AddVoucherRev will be used to add a voucher reversal record to table EXTGLV
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


public class AddVoucherRev extends ExtendM3Transaction {
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
  boolean foundFGLEDGrecord
  
  // Constructor 
  public AddVoucherRev(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     String inRECD
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
     String inAPCD
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
     int inREDT 
     if (mi.in.get("REDT") != null) {
        inREDT = mi.in.get("REDT") 
        
        //Validate date format
        boolean validREDT = utility.call("DateUtil", "isDateValid", String.valueOf(inREDT), "yyyyMMdd")  
        if (!validREDT) {
           mi.error("Requested Date is not valid")   
           return  
        } 

     } else {
        inREDT = 0        
     }

     // Date Approved
     int inAPDT 
     if (mi.in.get("APDT") != null) {
        inAPDT = mi.in.get("APDT") 
        
        //Validate date format
        boolean validAPDT = utility.call("DateUtil", "isDateValid", String.valueOf(inAPDT), "yyyyMMdd")  
        if (!validAPDT) {
           mi.error("Date Approved is not valid")   
           return  
        } 

     } else {
        inAPDT = 0        
     }

     // Comments
     String inA256
     if (mi.in.get("A256") != null && mi.in.get("A256") != "") {
        inA256 = mi.inData.get("A256")
     } else {
        inA256 = ""        
     }
     String inA257
     if (mi.in.get("A257") != null && mi.in.get("A257") != "") {
        inA257 = mi.inData.get("A257")
     } else {
        inA257 = ""        
     }
     String inA258
     if (mi.in.get("A258") != null && mi.in.get("A258") != "") {
        inA258 = mi.inData.get("A258")
     } else {
        inA258 = ""        
     }

     // Validate Voucher Revesal Record
     Optional<DBContainer> EXTGLV = findEXTGLV(inCONO, inDIVI, inYEA4, inVONO, inVSER)
     if (EXTGLV.isPresent()) {
        mi.error("Matching voucher already exists in EXTGLV")   
        return             
     } else {
        //Validate there is a matching record in FGLEDG
        validateFGLEDGrecord()
        if (foundFGLEDGrecord) {
          // Write record 
          addEXTGLVRecord(inCONO, inDIVI, inYEA4, inVONO, inVSER, inRECD, inAPCD, inREDT, inAPDT, inA256, inA257, inA258)  
        } else {
          mi.error("No matching record exists in FGLEDG")   
          return             
        }
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
  // Validate FGLEDG record
  //******************************************************************** 
  void validateFGLEDGrecord() {
    foundFGLEDGrecord = false
    DBAction query = database.table("FGLEDG").index("07").build()
    DBContainer container = query.getContainer()
    container.set("EGCONO", inCONO)
    container.set("EGDIVI", inDIVI)
    container.set("EGYEA4", inYEA4)
    container.set("EGVONO", inVONO)
    container.set("EGVSER", inVSER)
    query.readAll(container, 5, callback)
  }
  Closure<?> callback = { DBContainer container ->
    int year = container.get("EGYEA4")
    int voucher = container.get("EGVONO")
    if (year != null && year > 0 && voucher != null && voucher > 0) {
      foundFGLEDGrecord = true
    } else {
      foundFGLEDGrecord = false
    }
  }  


  //******************************************************************** 
  // Add EXTGLV record 
  //********************************************************************     
  void addEXTGLVRecord(int CONO, String DIVI, int YEA4, int VONO, String VSER, String RECD, String APCD, int REDT, int APDT, String A256, String A257, String A258) {   
       DBAction action = database.table("EXTGLV").index("00").build()
       DBContainer EXTGLV = action.createContainer()
       EXTGLV.set("EXCONO", CONO)
       EXTGLV.set("EXDIVI", DIVI)
       EXTGLV.set("EXYEA4", YEA4)
       EXTGLV.set("EXVONO", VONO)
       EXTGLV.set("EXVSER", VSER)
       EXTGLV.set("EXRECD", RECD)
       EXTGLV.set("EXAPCD", APCD)
       EXTGLV.set("EXREDT", REDT)
       EXTGLV.set("EXAPDT", APDT)
       EXTGLV.set("EXA256", A256)
       EXTGLV.set("EXA257", A257)
       EXTGLV.set("EXA258", A258)
       EXTGLV.set("EXCHID", program.getUser())
       EXTGLV.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTGLV.set("EXRGDT", regdate) 
       EXTGLV.set("EXLMDT", regdate) 
       EXTGLV.set("EXRGTM", regtime)
       action.insert(EXTGLV)         
 } 

     
} 

