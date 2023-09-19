// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-12
// @version   1.0 
//
// Description 
// This API is used to update the forwarding agent in MHDISH
// Transaction ValidateLine
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230812  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.LocalDate
import java.time.LocalDateTime  
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

public class UpdForwAgent extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger 
  private final MICallerAPI miCaller
  
    // Definition 
  Integer inCONO
  String companyNumber
  int inINOU 
  String direction
  Long inDLIX
  String deliveryNumber
  String inFWNO
  String inETRN
  String deliveryStatus
  String supplierStatus
  int supplierType
  
  public UpdForwAgent(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger 
     this.miCaller = miCaller
  } 
    
  public void main() { 
     inCONO = program.LDAZD.CONO as Integer
     companyNumber = String.valueOf(inCONO)

     inINOU = mi.in.get("INOU")  
     if(inINOU < 1 && inINOU > 4){
        mi.error("Direction is not valid")   
        return             
     } 
     
     direction = String.valueOf(inINOU)
     
     inDLIX = mi.in.get("DLIX")  
     deliveryNumber = String.valueOf(inDLIX)
     
     if (mi.in.get("FWNO") != null && mi.in.get("FWNO") != "") {
        inFWNO = mi.inData.get("FWNO").trim() 
        
        //Validate forwarding agent
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inFWNO)
        if(!CIDMAS.isPresent()){
           mi.error("Forwarding Agent doesn't exists")   
           return             
        } else {
           DBContainer containerCIDMAS = CIDMAS.get() 
           supplierStatus = containerCIDMAS.getString("IDSTAT")
           supplierType = containerCIDMAS.get("IDSUTY")
           if (supplierStatus == "30" || supplierStatus == "90") {
              mi.error("Forwarding Agent is not valid")   
              return             
           }
           if (supplierType != 5) {
              mi.error("Forwarding Agent is not valid")   
              return             
           } 
        }

     } else {
        inFWNO = ""
     }
     
     if (mi.in.get("ETRN") != null && mi.in.get("ETRN") != "") {
        inETRN = mi.inData.get("ETRN").trim() 
     } else {
        inETRN = ""
     }

     //Validate delivery header
     Optional<DBContainer> MHDISH = findMHDISH(inCONO, inINOU, inDLIX)
     if(!MHDISH.isPresent()){
        mi.error("Delivery doesn't exists")   
        return             
     } else {
        if (inETRN != null && inETRN != "") {
           updatePlanningStatus(companyNumber, direction, deliveryNumber, inETRN)
        } 
        updMHDISH(inCONO, inINOU, inDLIX)
     }
     
  }
 

  //******************************************************************** 
  // Get CIDMAS record
  //******************************************************************** 
  private Optional<DBContainer> findCIDMAS(Integer CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").selection("IDSTAT", "IDSUTY").build()
     def CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get MHDISH record
  //******************************************************************** 
  private Optional<DBContainer> findMHDISH(Integer CONO, Integer INOU, Long DLIX){  
     DBAction query = database.table("MHDISH").index("00").selection("OQPGRS").build()
     def MHDISH = query.getContainer()
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", INOU)
     MHDISH.set("OQDLIX", DLIX)
     if(query.read(MHDISH))  { 
       return Optional.of(MHDISH)
     } 
  
     return Optional.empty()
  }


   //***************************************************************************** 
   // Update tracking number by calling MWS410MI.UpdPlanSts
   //***************************************************************************** 
   private updatePlanningStatus(String company, String direction, String deliveryNumber, String trackingNumber){   
        def params = [CONO: company, INOU: direction, DLIX: deliveryNumber, ETRN: trackingNumber]  
        def callback = {
        Map<String, String> response ->
        }

        miCaller.call("MWS410MI","UpdPlanSts", params, callback)
   } 
    
    
  //******************************************************************** 
  // Update MHDISH record
  //********************************************************************    
  void updMHDISH(Integer CONO, Integer INOU, Long DLIX){ 
     
     DBAction action = database.table("MHDISH").index("00").build()
     DBContainer MHDISH = action.getContainer()
          
     MHDISH.set("OQCONO", CONO)
     MHDISH.set("OQINOU", INOU)
     MHDISH.set("OQDLIX", DLIX)

     // Read with lock
     action.readLock(MHDISH, updateCallBackMHDISH)
     }
   
     Closure<?> updateCallBackMHDISH = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now()    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd")  
     String formatDate = now.format(format1)    
     
     int changeNo = lockedResult.get("OQCHNO")
     int newChangeNo = changeNo + 1 
     
     if (inFWNO != null && inFWNO != "") {
        lockedResult.set("OQFWNO", inFWNO) 
     }
     
     // Update changed information
     int changeddate=Integer.parseInt(formatDate)   
     lockedResult.set("OQLMDT", changeddate)  
      
     lockedResult.set("OQCHNO", newChangeNo) 
     lockedResult.set("OQCHID", program.getUser())
     lockedResult.update()
  }
  
    
}