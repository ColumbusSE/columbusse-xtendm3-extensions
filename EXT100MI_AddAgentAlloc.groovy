// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-19
// @version   1.0 
//
// Description 
// This API is used to add an agent allocation record to EXTAGA
// Transaction AddAgentAlloc
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


public class AddAgentAlloc extends ExtendM3Transaction {
	private final MIAPI mi
	private final ProgramAPI program
	private final DatabaseAPI database
	private final LoggerAPI logger
	
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

  public AddAgentAlloc(MIAPI mi, ProgramAPI program, DatabaseAPI database, LoggerAPI logger) {
		this.mi = mi
		this.program = program
		this.database = database
		this.logger = logger
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
     
     //Check Priority
     inPRIO = mi.in.get("PRIO") 
     if (inPRIO == 10 || inPRIO == 20 || inPRIO == 30 || inPRIO == 40 || inPRIO == 50) {
     } else {
       mi.error("Priority " + inPRIO + " is invalid")   
       return                                         
     }
     
     //Payer must be entered if PRIO is 30
     inPYNO = mi.in.get("PYNO") 
     if (inPRIO == 30) {
       if (inPYNO == null || inPYNO == "") {
          mi.error("Payer must be entered")   
          return                                         
       }
     } 
     
     //Validate PYNO 
     if (inPYNO != null && inPYNO != "" && inPYNO != "*") {
        Optional<DBContainer> OCUSMAPayer = findOCUSMA(CONO, inPYNO)  
        if(!OCUSMAPayer.isPresent()){                         
          mi.error("Payer " + inPYNO + " is invalid")   
          return                                         
        }
     }

     //Customer must be entered if PRIO is 10 or 20
     inCUNO = mi.in.get("CUNO") 
     if (inPRIO == 10 || inPRIO == 20) {
         if (inCUNO == null || inCUNO == "") {
           mi.error("Customer must be entered")   
           return                                         
         }
     } 
     
     //Validate CUNO 
     if (inCUNO != null && inCUNO != "" && inCUNO != "*") {
         Optional<DBContainer> OCUSMACustomer = findOCUSMA(CONO, inCUNO)  
         if(!OCUSMACustomer.isPresent()){                         
           mi.error("Customer " + inCUNO + " is invalid")   
           return                                         
         }
     }

     //Country Code must be entered if PRIO is 10 or 20
     inCSCD = mi.in.get("CSCD") 
     if (inPRIO == 10 || inPRIO == 20) {
        if (inCSCD == null || inCSCD == "") {
           mi.error("Country Code must be entered")   
           return                                         
        }
     }
     
     //Validate CSCD 
     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {
         Optional<DBContainer> CSYTAB = findCSYTAB(CONO, inCSCD)  
         if(!CSYTAB.isPresent()){                         
           mi.error("Country Code " + inCSCD + " is invalid")   
           return                                         
         } 
     }
     
     //State must be entered if PRIO is 10 or 20
     inECAR = mi.in.get("ECAR") 

     //Validate ECAR 
     if (inECAR != null && inECAR != "" && inECAR != "*") {
         Optional<DBContainer> CSYSTS = findCSYSTS(CONO, inECAR, inCSCD)  
         if(!CSYSTS.isPresent()){                         
           mi.error("State " + inECAR + " is invalid")   
           return                                         
         }
     }


     inPONO = mi.in.get("PONO")  

     //Focus must always be entered
     inATAV = mi.in.get("ATAV") 
     if (inPRIO == 10 || inPRIO == 20 || inPRIO == 30 || inPRIO == 40 || inPRIO == 50) {
       if (inATAV == null || inATAV == "") {
          mi.error("Focus must be entered")   
          return                                         
       } 
     }
     
     //Validate Focus 
     if (inATAV != null && inATAV != "" && inATAV != "*") {
         Optional<DBContainer> MPDOPT = findMPDOPT(CONO, inATAV.trim())  
         if(!MPDOPT.isPresent()){                         
           mi.error("Focus " + inATAV + " is invalid")   
           return                                         
         } 
     }

     //Hierarchy must be entered if priority is 10 or 40
     inHIE3 = mi.in.get("HIE3") 
     if (inPRIO == 10 || inPRIO == 40) {
       if (inHIE3 == null || inHIE3 == "") {
          mi.error("Hierarchy must be entered")   
          return                                         
       } 
     }
     
     //Validate HIE3 
     if (inHIE3 != null && inHIE3 != "" && inHIE3 != "*") {
         Optional<DBContainer> MITHRY = findMITHRY(CONO, 3, inHIE3.trim())  
         if(!MITHRY.isPresent()){                         
           mi.error("Hierarchy " + inHIE3 + " is invalid")   
           return                                         
         } 
     }

     //Date must be entered and valid format
     inFDAT = mi.in.get("FDAT")  
     if (isDateValid(String.valueOf(inFDAT), "yyyyMMdd")) {
     } else {
        mi.error("Date format " + inFDAT + " is invalid")   
        return                                         
     }
     inTDAT = mi.in.get("TDAT") 
     if (isDateValid(String.valueOf(inTDAT), "yyyyMMdd")) {
     } else {
        mi.error("Date format " + inTDAT + " is invalid")   
        return                                         
     }

     if (inPRIO == 10 || inPRIO == 20 || inPRIO == 30 || inPRIO == 40 || inPRIO == 50) {
       if (inFDAT == null || inFDAT == 0 || inTDAT == null || inTDAT == 0) {
          mi.error("Date must be entered")   
          return                                         
       } 
     } 
     if (inTDAT == null) {
        inTDAT = 0
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
     if (inAGN1 == null || inAGN1 == "") {
        inAGN1 = "*"
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
     if (inAGN2 == null || inAGN2 == "") {
        inAGN2 = "*"
     }


     //Check if Agent Allocation record exists  
     Optional<DBContainer> EXTAGA = findEXTAGA(CONO, inPRIO, inPYNO, inCUNO, inCSCD, inECAR, inPONO, inATAV.trim(), inHIE3.trim(), inFDAT)        
     if (EXTAGA.isPresent()) {                                           
        //If record found, return error message 
        mi.error("Allocation Agent record already exists")   
        return
     } else {
        // Write record 
        addRecord(CONO, inPRIO, inPYNO, inCUNO, inCSCD, inECAR, inPONO, inATAV.trim(), inHIE3.trim(), inFDAT, inTDAT, inAGN1, inAGN2)    
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
  // Check Payer/Customer
  //******************************************************************** 
  private Optional<DBContainer> findOCUSMA(int CONO, String CUNO){  
    DBAction query = database.table("OCUSMA").index("00").selection("OKCONO", "OKDIVI", "OKCUNO", "OKPYNO").build()   
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
    DBAction query = database.table("CSYTAB").index("00").selection("CTCONO", "CTDIVI", "CTSTCO", "CTSTKY", "CTLNCD").build()   
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
    DBAction query = database.table("CSYSTS").index("00").selection("CKCONO", "CKECAR", "CKCSCD").build()   
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
    DBAction query = database.table("MPDOPT").index("00").selection("PFCONO", "PFOPTN").build()   
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
    DBAction query = database.table("MITHRY").index("00").selection("HICONO", "HIHLVL", "HIHIE0").build()   
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
  // Validate if record exists in EXTAGA
  //******************************************************************** 
  private Optional<DBContainer> findEXTAGA(int CONO, int PRIO, String PYNO, String CUNO, String CSCD, String ECAR, String PONO, String ATAV, String HIE3, int FDAT){  
      DBAction query = database.table("EXTAGA").index("00").selectAllFields().build()   
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
    // Get Agent from EXTAGA
    //******************************************************************** 
    private Optional<DBContainer> findEXTAGA(Integer CONO, int PRIO, String PYNO, String CUNO, String CSCD, String ECAR, String PONO, String ATAV, String HIE3, int FDAT){  
      DBAction query = database.table("EXTAGA").index("00").selectAllFields().build()   
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
  // Add EXTAGA record
  //********************************************************************     
  void addRecord(int CONO, int PRIO, String PYNO, String CUNO, String CSCD, String ECAR, String PONO, String ATAV, String HIE3, int FDAT, int TDAT, String AGN1, String AGN2){     
     DBAction action = database.table("EXTAGA").index("00").selectAllFields().build()
     DBContainer EXTAGA = action.createContainer()
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
     EXTAGA.set("EXTDAT", TDAT)
     EXTAGA.set("EXAGN1", AGN1)
     EXTAGA.set("EXAGN2", AGN2)

     EXTAGA.set("EXCHID", program.getUser())
     EXTAGA.set("EXCHNO", 1) 
     
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");  
     String formatTime = now.format(format2);  
     
     //Converting String into int using Integer.parseInt()
     int regdate=Integer.parseInt(formatDate); 
     int regtime=Integer.parseInt(formatTime); 
     EXTAGA.set("EXRGDT", regdate) 
     EXTAGA.set("EXLMDT", regdate) 
     EXTAGA.set("EXRGTM", regtime)
     action.insert(EXTAGA)         
  } 
  

}