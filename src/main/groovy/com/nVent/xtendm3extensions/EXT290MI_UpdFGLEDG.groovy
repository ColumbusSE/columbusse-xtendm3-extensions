// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-01-30 
// @version   1.0 
//
// Description 
// API transaction UpdFGLEDG will be used to update the reconciliation code in table FGLEDG
// ERP-9012


/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: YEA4 - Year
 * @param: JRNO - Voucher
 * @param: JSNO - Voucher Number Series
 * @param: RECO - Reconciliation Code
*/



public class UpdFGLEDG extends ExtendM3Transaction {
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
  int inRECO
  boolean foundFGLEDGrecord
  
  
  // Constructor 
  public UpdFGLEDG(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Reconsiliation Code
     if (mi.in.get("RECO") != null) {
        if (mi.in.get("RECO") != 0 && mi.in.get("RECO") != 8 && mi.in.get("RECO") != 9) {
           mi.error("Reconciliation code is wrong")   
           return             
        } else {
           inRECO = mi.in.get("RECO") as Integer
        }
     }
     

     // Validate Voucher Revesal Record
     validateFGLEDGrecord() 
     if (foundFGLEDGrecord) {
        // Update record
        updFGLEDGRecord()            
     } else {
        mi.error("Matching voucher record does not exist")   
        return             
     }
     
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
  // Update FGLEDG record
  //********************************************************************    
  void updFGLEDGRecord(){      
     DBAction action = database.table("FGLEDG").index("07").build()
     DBContainer FGLEDG = action.getContainer()    
     FGLEDG.set("EGCONO", inCONO)     
     FGLEDG.set("EGDIVI", inDIVI)  
     FGLEDG.set("EGYEA4", inYEA4)
     FGLEDG.set("EGVONO", inVONO)
     FGLEDG.set("EGVSER", inVSER)

     // Read with lock
     action.readAllLock(FGLEDG, 5, updateCallBackFGLEDG)
     }
   
     Closure<?> updateCallBackFGLEDG = { LockedResult lockedResult ->   
       if (mi.in.get("RECO") != null) {
          lockedResult.set("EGRECO", inRECO)
       }
       
       int changeNo = lockedResult.get("EGCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EGLMDT", changeddate)       
       lockedResult.set("EGCHNO", newChangeNo) 
       lockedResult.set("EGCHID", program.getUser())
       lockedResult.update()
    }


} 

