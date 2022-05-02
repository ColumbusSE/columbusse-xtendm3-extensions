/**
* This extension is used for the update of field ALWT in record OOLINE 
* for a selected DLIX
*
* Name: EXT101MI.UpdALWT.groovy
* 
* Date         Changed By                         Description 
* 20220221     Jörg Wanning (Columbus)            Update updateMHDISL/ALWT and POPN, no standard API exist
* 
*/

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class updateMHDISL extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final UtilityAPI utility;
	private final MICallerAPI miCaller;

  public updateMHDISL(MIAPI mi, ProgramAPI program, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger) {
    this.mi = mi;
    this.program = program;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
  }

  public void main() {
    if(validateInput()) {
       mi.write()
       return
    } 
    updRecord() 
  }
  
  //***************************************************** 
  // validateInput - Validate entered MODF and CONO
  //*****************************************************
  boolean validateInput(){
    String company = mi.in.get("CONO") 
    if(validateCompany(company)){  
       mi.error("Company " + company + " is invalid") 
       return true
    }
    String popn = mi.in.get("POPN")
    if(popn == ""){
       mi.error("Alias number " + popn + " is invalid") 
       return true
    }
    return false
  }
  
  //***************************************************** 
  // validateCompany - Validate given or retrieved CONO
  // Input 
  // Company - from Input
  //*****************************************************
  boolean validateCompany(String company){  
    // Run MI program 
    def parameter = [CONO: company] 
    List <String> result = []
    Closure<?> handler = {Map<String, String> response -> 
         return response.CONO == 0} 
     miCaller.call("MNS095MI", "Get", parameter, handler)
  }
  
  //***************************************************** 
  // updRecord - Start update process by reading MHDISH 
  //             as Inbound, if not exist as outbound
  //*****************************************************
  void updRecord(){
     int company =    mi.in.get("CONO")
     long delivery =  mi.in.get("DLIX")
     int referenceOrderCat = mi.in.get("RORC")
     String orderNumber = mi.in.get("RIDN")
     int orderLine = mi.in.get("RIDL")
     int lineSuffix = mi.in.get("RIDX")

     DBAction action = database.table("MHDISL").index("00").selection("URCONO", "URDLIX", "URRORC", "URRIDN", "URRIDL", "URRIDX").build()
     DBContainer hdisl = action.getContainer()
     
     hdisl.set("URCONO", company)
     hdisl.set("URDLIX", delivery)
     hdisl.set("URRORC", referenceOrderCat)
     hdisl.set("URRIDN", orderNumber)
     hdisl.set("URRIDL", orderLine)
     hdisl.set("URRIDX", lineSuffix)
     
     /*
     if (!action.read(hdish)) {
         hdish.set("OQINOU", outbound)
         action.read(hdish)
     }
     */
     
     // Read with lock
     action.readLock(hdisl, updateCallBack)
  }
  
  //***************************************************** 
  // updateCallBack - update MHDISL field POPN & ALWT  
  //*****************************************************
  Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);
     
     //logger.info("Order:"  ${orderNumber});
     
     int changeNo = lockedResult.get("URCHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the fields if filled
     lockedResult.set("URPOPN", mi.in.get("POPN"))  
     lockedResult.set("URALWT", mi.in.get("ALWT"))  
     
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("URLMDT", changeddate)  
     lockedResult.set("URCHNO", newChangeNo) 
     lockedResult.set("URCHID", program.getUser())
     lockedResult.update()
  }
  
}
