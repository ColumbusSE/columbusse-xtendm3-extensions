// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-19
// @version   1.0 
//
// Description 
// This API is used to update an agent allocation record from table EXTAGA
// Transaction UpdAgentAlloc
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230419  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.Instant; 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class UpdAgentAlloc extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;  
  
 	public Integer CONO
	public int inPRIO
	public String inPYNO
  public String inCUNO
  public String inCSCD
  public String inECAR
  public String inPONO
  public String inATAV
  public String inHIE3
  public int inFDAT
  public int inTDAT
  public String inAGN1
  public String inAGN2
  
  
  public UpdAgentAlloc(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
     // Validate company
     CONO = mi.in.get("CONO")      
     if (CONO == null) {
        CONO = program.LDAZD.CONO as Integer
     } 
     Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
     if(!CMNCMP.isPresent()){                         
       mi.error("Company " + CONO + " is invalid")   
       return                                         
     } 
     
     inPRIO = mi.in.get("PRIO") 
     if (inPRIO == 10 || inPRIO == 20 || inPRIO == 30 || inPRIO == 40 || inPRIO == 50) {
     } else {
       mi.error("Priority " + inPRIO + " is invalid")   
       return                                         
     }
     
     inPYNO = mi.in.get("PYNO") 
     if (inPYNO != null && inPYNO != "" && inPYNO != "*") {
       Optional<DBContainer> OCUSMAPayer = findOCUSMA(CONO, inPYNO)  
       if(!OCUSMAPayer.isPresent()){                         
         mi.error("Payer " + inPYNO + " is invalid")   
         return                                         
       }   
     }
     
     inCUNO = mi.in.get("CUNO")   
     if (inCUNO != null && inCUNO != "" && inCUNO != "*") {
       Optional<DBContainer> OCUSMACustomer = findOCUSMA(CONO, inCUNO)  
       if(!OCUSMACustomer.isPresent()){                         
         mi.error("Customer " + inCUNO + " is invalid")   
         return                                         
       }   
     }

     inCSCD = mi.in.get("CSCD")   
     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {
       Optional<DBContainer> CSYTAB = findCSYTAB(CONO, inCSCD)  
       if(!CSYTAB.isPresent()){                         
         mi.error("Country Code " + inCSCD + " is invalid")   
         return                                         
       }   
     }
     
     inECAR = mi.in.get("ECAR")  
     if (inECAR != null && inECAR != "" && inECAR != "*") {
       Optional<DBContainer> CSYSTS = findCSYSTS(CONO, inECAR, inCSCD)  
       if(!CSYSTS.isPresent()){                         
         mi.error("State " + inECAR + " is invalid")   
         return                                         
       } 
     }

     inPONO = mi.in.get("PONO")   
     
     inATAV = mi.in.get("ATAV")
     if (inATAV != null && inATAV != "" && inATAV != "*") {
       Optional<DBContainer> MPDOPT = findMPDOPT(CONO, inATAV)  
       if(!MPDOPT.isPresent()){                         
         mi.error("Focus " + inATAV + " is invalid")   
         return                                         
       }   
     }
     
     inHIE3 = mi.in.get("HIE3")
     if (inHIE3 != null && inHIE3 != "" && inHIE3 != "*") {
       Optional<DBContainer> MITHRY = findMITHRY(CONO, 3, inHIE3)  
       if(!MITHRY.isPresent()){                         
         mi.error("Hierarchy" + inHIE3 + " is invalid")   
         return                                         
       }   
     }

     if (mi.in.get("FDAT") == null && mi.in.get("FDAT") == 0 && mi.in.get("FDAT") == "*") {
         mi.error("From Date " + inFDAT + " is invalid")   
         return                                         
     } else {
         inFDAT = mi.in.get("FDAT") 
     }
     
     //Date must be valid format
     inFDAT = mi.in.get("FDAT")  
     if (isDateValid(String.valueOf(inFDAT), "yyyyMMdd")) {
     } else {
        mi.error("Date format " + inFDAT + " is invalid")   
        return                                         
     }
     
     if (mi.in.get("TDAT") != null && mi.in.get("TDAT") != "" && mi.in.get("TDAT") != "*") {
        inTDAT = mi.in.get("TDAT")
     } 
     
     //Date must be valid format
     inTDAT = mi.in.get("TDAT") 
     if (isDateValid(String.valueOf(inTDAT), "yyyyMMdd")) {
     } else {
        mi.error("Date format " + inTDAT + " is invalid")   
        return                                         
     }

     //Validate AGN1 
     inAGN1 = mi.in.get("AGN1")   
     if (inAGN1 != null && inAGN1 != "" && inAGN1 != "*") {
       Optional<DBContainer> OCUSMAAgent1 = findOCUSMA(CONO, inAGN1)  
       if(!OCUSMAAgent1.isPresent()){                         
         mi.error("Agent 1 " + inAGN1 + " is invalid")   
         return                                         
       }
     }
     
     //Validate AGN2 
     inAGN2 = mi.in.get("AGN2")   
     if (inAGN2 != null && inAGN2 != "" && inAGN2 != "*") {
       Optional<DBContainer> OCUSMAAgent2 = findOCUSMA(CONO, inAGN2)  
       if(!OCUSMAAgent2.isPresent()){                         
         mi.error("Agent 2 " + inAGN2 + " is invalid")   
         return                                         
       }   
     }

     // Validate EXTAGA
     Optional<DBContainer> EXTAGA = findEXTAGA(CONO, inPRIO, inPYNO, inCUNO, inCSCD, inECAR, inPONO, inATAV, inHIE3, inFDAT)       
     if (!EXTAGA.isPresent()) {
        mi.error("Allocation Agent record doesn't exists")   
        return             
     } else {
        // Update record 
        updRecord()
     }  


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
  // Check Payer/Customer
  //******************************************************************** 
  private Optional<DBContainer> findOCUSMA(int CONO, String CUNO){  
    DBAction query = database.table("OCUSMA").index("00").build()   
    def OCUSMA = query.getContainer()
    OCUSMA.set("OKCONO", CONO)
    OCUSMA.set("OKDIVI", "")
    OCUSMA.set("OKCUNO", CUNO)
    
    if(query.read(OCUSMA))  { 
      return Optional.of(OCUSMA)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Check Country Code
  //******************************************************************** 
  private Optional<DBContainer> findCSYTAB(int CONO, String CSCD){  
    DBAction query = database.table("CSYTAB").index("00").build()   
    def CSYTAB = query.getContainer()
    CSYTAB.set("CTCONO", CONO)
    CSYTAB.set("CTDIVI", "")
    CSYTAB.set("CTSTCO", "CSCD")
    CSYTAB.set("CTSTKY", CSCD)
    CSYTAB.set("CTLNCD", "")
    
    if(query.read(CSYTAB))  { 
      return Optional.of(CSYTAB)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Check State
  //******************************************************************** 
  private Optional<DBContainer> findCSYSTS(int CONO, String ECAR, String CSCD){  
    DBAction query = database.table("CSYSTS").index("00").build()   
    def CSYSTS = query.getContainer()
    CSYSTS.set("CKCONO", CONO)
    CSYSTS.set("CKECAR", ECAR)
    CSYSTS.set("CKCSCD", CSCD)

    if(query.read(CSYSTS))  { 
      return Optional.of(CSYSTS)
    } 
  
    return Optional.empty()
  }  
  
  //******************************************************************** 
  // Check Focus/Option
  //******************************************************************** 
  private Optional<DBContainer> findMPDOPT(int CONO, String OPTN){  
    DBAction query = database.table("MPDOPT").index("00").build()   
    def MPDOPT = query.getContainer()
    MPDOPT.set("PFCONO", CONO)
    MPDOPT.set("PFOPTN", OPTN)

    if(query.read(MPDOPT))  { 
      return Optional.of(MPDOPT)
    } 
  
    return Optional.empty()
  } 

  //******************************************************************** 
  // Check Hierarchy 3
  //******************************************************************** 
  private Optional<DBContainer> findMITHRY(int CONO, int HLVL, String HIE0){  
    DBAction query = database.table("MITHRY").index("00").build()   
    def MITHRY = query.getContainer()
    MITHRY.set("HICONO", CONO)
    MITHRY.set("HIHLVL", HLVL)
    MITHRY.set("HIHIE0", HIE0)

    if(query.read(MITHRY))  { 
      return Optional.of(MITHRY)
    } 
  
    return Optional.empty()
  } 

  //******************************************************************** 
  // Validate if record exists in EXTAGA
  //******************************************************************** 
  private Optional<DBContainer> findEXTAGA(int CONO, int PRIO, String PYNO, String CUNO, String CSCD, String ECAR, String PONO, String ATAV, String HIE3, int FDAT){  
      DBAction query = database.table("EXTAGA").index("00").build()   
      def EXTAGA = query.getContainer()
      EXTAGA.set("EXCONO", CONO)
      EXTAGA.set("EXPRIO", PRIO)
      EXTAGA.set("EXPYNO", PYNO)
      EXTAGA.set("EXCUNO", CUNO)
      EXTAGA.set("EXCSCD", CSCD)
      EXTAGA.set("EXECAR", ECAR)
      EXTAGA.set("EXPONO", PONO)
      EXTAGA.set("EXATAV", ATAV)
      EXTAGA.set("EXHIE3", HIE3)
      EXTAGA.set("EXFDAT", FDAT)

      if(query.read(EXTAGA))  { 
        return Optional.of(EXTAGA)
      } 
  
      return Optional.empty()
  }
   
  //******************************************************************** 
  // Check Date format
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
  // Update Agent Allocation record
  //******************************************************************** 
  void updRecord(){ 
       DBAction action = database.table("EXTAGA").index("00").build()
       DBContainer EXTAGA = action.createContainer()
       EXTAGA.set("EXCONO", CONO)
       EXTAGA.set("EXPRIO", inPRIO)
       EXTAGA.set("EXPYNO", inPYNO)
       EXTAGA.set("EXCUNO", inCUNO)
       EXTAGA.set("EXCSCD", inCSCD)
       EXTAGA.set("EXECAR", inECAR)
       EXTAGA.set("EXPONO", inPONO)
       EXTAGA.set("EXATAV", inATAV)
       EXTAGA.set("EXHIE3", inHIE3)
       EXTAGA.set("EXFDAT", inFDAT)

     // Read with lock
     action.readLock(EXTAGA, updateCallBack)
  }

    
  Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the fields if filled
     if(mi.in.get("TDAT") != null){  
        lockedResult.set("EXTDAT", mi.in.get("TDAT")) 
     }
     
     // Update the fields if filled
     if(mi.in.get("AGN1") != null){  
        lockedResult.set("EXAGN1", mi.in.get("AGN1")) 
     }
     
     // Update the fields if filled
     if(mi.in.get("AGN2") != null){  
        lockedResult.set("EXAGN2", mi.in.get("AGN2")) 
     }

     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("EXLMDT", changeddate)  
      
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }
    
}