// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-08-12
// @version   1.0 
//
// Description 
// This API will be used to call COS410MI and in addition update CFI1-CFI5
// Transaction AddAgrLines
// 

//**************************************************************************** 
// Date    Version     Developer 
// 240812  1.0         Jessica Bjorklund, Columbus   New API transaction
//****************************************************************************  

import java.time.LocalDate
import java.time.LocalDateTime  
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

public class AddAgrLines extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger 
  private final UtilityAPI utility
  private final MICallerAPI miCaller


  // Definition 
  Integer inCONO
  String inAAGN 
  int inSRVP 
  String inPRNO 
  String inSTRT
  String inSUFI
  int inSTDT
  int inTODT
  String inPRTX
  int inLEAS
  String inMES0
  double inWAL1
  String inMES1
  double inWAL2
  String inMES2
  double inWAL3
  String inMES3
  double inWAL4
  String inMES4
  int inAINX
  double inAMVI
  double inAMAM
  double inAMPI
  double inAMAL
  double inAMM1
  double inAMLL
  int inAEXC
  double inAMLJ
  double inAML0
  double inAMIL
  double inAMIO
  double inAMSL
  double inAMSO
  String inELN1
  String inELN2
  String inELN3
  String inELN4
  double inLIZP
  String inCFI1
  double inCFI2
  String inCFI3
  String inCFI4
  String inCFI5
 
  
  public AddAgrLines(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, UtilityAPI utility, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger 
     this.utility = utility
     this.miCaller = miCaller
  }
  
  public void main() {
    
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 
     
     // Agreement
     if (mi.in.get("AAGN") != null && mi.in.get("AAGN") != "") {
        inAAGN = mi.inData.get("AAGN").trim() 
     } else {
        inAAGN = ""
     }
     
     // Service Price Method
     if (mi.in.get("SRVP") != null) {
        inSRVP = mi.in.get("SRVP") 
     } 
     
     // Product
     if (mi.in.get("PRNO") != null && mi.in.get("PRNO") != "") {
        inPRNO = mi.inData.get("PRNO").trim() 
     } else {
        inPRNO = ""
     }

     // Product Structure Type
     if (mi.in.get("STRT") != null && mi.in.get("STRT") != "") {
        inSTRT = mi.inData.get("STRT").trim() 
     } else {
        inSTRT = ""
     }

     // Service
     if (mi.in.get("SUFI") != null && mi.in.get("SUFI") != "") {
        inSUFI = mi.inData.get("SUFI").trim() 
     } else {
        inSUFI = ""
     }
     
     // Start Date
     if (mi.in.get("STDT") != null) {
        inSTDT = mi.in.get("STDT") 
        
        //Validate date format
        boolean validSTDT = utility.call("DateUtil", "isDateValid", String.valueOf(inSTDT), "yyyyMMdd")  
        if (!validSTDT) {
           mi.error("Start Date is not valid")   
           return  
        } 
     } 

     // To Date
     if (mi.in.get("TODT") != null) {
        inTODT = mi.in.get("TODT") 
        
        //Validate date format
        boolean validTODT = utility.call("DateUtil", "isDateValid", String.valueOf(inTODT), "yyyyMMdd")  
        if (!validTODT) {
           mi.error("To Date is not valid")   
           return  
        } 
     } 

     // Text
     if (mi.in.get("PRTX") != null && mi.in.get("PRTX") != "") {
        inPRTX = mi.inData.get("PRTX").trim() 
     } else {
        inPRTX = ""
     }

     // Lead Time Service
     if (mi.in.get("LEAS") != null) {
        inLEAS = mi.in.get("LEAS") 
     } 

     // Meter
     if (mi.in.get("MES0") != null && mi.in.get("MES0") != "") {
        inMES0 = mi.inData.get("MES0").trim() 
     } else {
        inMES0 = ""
     }

     // Meter 1
     if (mi.in.get("MES1") != null && mi.in.get("MES1") != "") {
        inMES1 = mi.inData.get("MES1").trim() 
     } else {
        inMES1 = ""
     }

     // Warranty Limit 1
     if (mi.in.get("WAL1") != null) {
        inWAL1 = mi.in.get("WAL1") 
     } 

     // Meter 2
     if (mi.in.get("MES2") != null && mi.in.get("MES2") != "") {
        inMES2 = mi.inData.get("MES2").trim() 
     } else {
        inMES2 = ""
     }

     // Warranty Limit 2
     if (mi.in.get("WAL2") != null) {
        inWAL2 = mi.in.get("WAL2") 
     } 

     // Meter 3
     if (mi.in.get("MES3") != null && mi.in.get("MES3") != "") {
        inMES3 = mi.inData.get("MES3").trim() 
     } else {
        inMES3 = ""
     }

     // Warranty Limit 3
     if (mi.in.get("WAL3") != null) {
        inWAL3 = mi.in.get("WAL3") 
     } 

     // Meter 4
     if (mi.in.get("MES4") != null && mi.in.get("MES4") != "") {
        inMES4 = mi.inData.get("MES4").trim() 
     } else {
        inMES4 = ""
     }

     // Warranty Limit 4
     if (mi.in.get("WAL4") != null) {
        inWAL4 = mi.in.get("WAL4") 
     } 

     // Index
     if (mi.in.get("AINX") != null) {
        inAINX = mi.in.get("AINX") 
     } 

     // Max Material Volume Per Item
     if (mi.in.get("AMVI") != null) {
        inAMVI = mi.in.get("AMVI") 
     } 

     // Max Material Cost
     if (mi.in.get("AMAM") != null) {
        inAMAM = mi.in.get("AMAM") 
     } 

     // Max Material Price Per Transaction
     if (mi.in.get("AMPI") != null) {
        inAMPI = mi.in.get("AMPI") 
     } 
     
     // User Defined Field 1
     if (mi.in.get("CFI1") != null && mi.in.get("CFI1") != "") {
        inCFI1 = mi.inData.get("CFI1").trim() 
     } else {
        inCFI1 = ""
     }

     // User Defined Field 2
     if (mi.in.get("CFI2") != null) {
        inCFI2 = mi.in.get("CFI2") 
     } 

     // User Defined Field 3
     if (mi.in.get("CFI3") != null && mi.in.get("CFI3") != "") {
        inCFI3 = mi.inData.get("CFI3").trim() 
     } else {
        inCFI3 = ""
     }

     // User Defined Field 4
     if (mi.in.get("CFI4") != null && mi.in.get("CFI4") != "") {
        inCFI4 = mi.inData.get("CFI4").trim() 
     } else {
        inCFI4 = ""
     }

     // User Defined Field 5
     if (mi.in.get("CFI5") != null && mi.in.get("CFI5") != "") {
        inCFI5 = mi.inData.get("CFI5").trim() 
     } else {
        inCFI5 = ""
     }


     //Call COS410MI.AddAgrLines
      Map<String, String> params = [
        AAGN: inAAGN, 
        SRVP: String.valueOf(inSRVP),  
        PRNO: inPRNO,  
        STRT: inSTRT,
        SUFI: inSUFI,
        STDT: String.valueOf(inSTDT),
        TODT: String.valueOf(inTODT),
        PRTX: inPRTX,
        LEAS: String.valueOf(inLEAS),
        MES0: inMES0,
        WAL1: String.valueOf(inWAL1),
        MES1: inMES1,
        WAL2: String.valueOf(inWAL2),
        MES2: inMES2,
        WAL3: String.valueOf(inWAL3),
        MES3: inMES3,
        WAL4: String.valueOf(inWAL4),
        MES4: inMES4,
        AINX: String.valueOf(inAINX),
        AMVI: String.valueOf(inAMVI),
        AMAM: String.valueOf(inAMAM),
        AMPI: String.valueOf(inAMPI),
        AMAL: String.valueOf(inAMAL),
        AMM1: String.valueOf(inAMM1),
        AMLL: String.valueOf(inAMLL),
        AEXC: String.valueOf(inAEXC),
        AMLJ: String.valueOf(inAMLJ),
        AMLO: String.valueOf(inAML0),
        AMIL: String.valueOf(inAMIL),
        AMIO: String.valueOf(inAMIO),
        AMSL: String.valueOf(inAMSL),
        AMSO: String.valueOf(inAMSO),
        ELN1: inELN1,
        ELN2: inELN2,
        ELN3: inELN3,
        ELN4: inELN4,
        LIZP: String.valueOf(inLIZP)
      ] 
      Closure<?> callback = {
        
        Map<String, String> response ->
        
        if (response.error != null) {
          mi.error(response.errorMessage)
          return
        } else {
          //If no error, update the record with CFI1 - CFI5
          updACUAGL(inCONO, inAAGN, inSRVP, inPRNO, inSTRT, inSUFI, inSTDT, inCFI1, inCFI2, inCFI3, inCFI4, inCFI5)
        }
        
 
      }
        
      miCaller.call("COS410MI","AddAgrLines", params, callback)

  }
 
  
  //******************************************************************** 
  // Update ACUAGL record
  //********************************************************************    
  void updACUAGL(int CONO, String AAGN, int SRVP, String PRNO, String STRT, String SUFI, int STDT, String CFI1, double CFI2, String CFI3, String CFI4, String CFI5){ 
     
     DBAction action = database.table("ACUAGL").index("00").build()
     DBContainer ACUAGL = action.getContainer()
     ACUAGL.set("ALCONO", CONO)
     ACUAGL.set("ALAAGN", AAGN)
     ACUAGL.set("ALSRVP", SRVP)
     ACUAGL.set("ALPRNO", PRNO)
     ACUAGL.set("ALSTRT", STRT)
     ACUAGL.set("ALSUFI", SUFI)
     ACUAGL.set("ALSTDT", STDT)

     // Read with lock
     action.readLock(ACUAGL, updateCallBackACUAGL)
     }
   
     Closure<?> updateCallBackACUAGL = { LockedResult lockedResult -> 
        // Get todays date
       LocalDateTime now = LocalDateTime.now()    
       DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd")  
       String formatDate = now.format(format1)    
       
       int changeNo = lockedResult.get("ALCHNO")
       int newChangeNo = changeNo + 1 
       
       if (inCFI1 != null && inCFI1 != "") {
          lockedResult.set("ALCFI1", inCFI1) 
       }
     
       if (inCFI2 != null) {
          lockedResult.set("ALCFI2", inCFI2) 
       }

       if (inCFI3 != null && inCFI3 != "") {
          lockedResult.set("ALCFI3", inCFI3) 
       }

       if (inCFI4 != null && inCFI4 != "") {
          lockedResult.set("ALCFI4", inCFI4) 
       }

       if (inCFI5 != null && inCFI5 != "") {
          lockedResult.set("ALCFI5", inCFI5) 
       }
     
       // Update changed information
       int changeddate=Integer.parseInt(formatDate)   
       lockedResult.set("ALLMDT", changeddate)  
        
       lockedResult.set("ALCHNO", newChangeNo) 
       lockedResult.set("ALCHID", program.getUser())
       lockedResult.update()
  }
  
}