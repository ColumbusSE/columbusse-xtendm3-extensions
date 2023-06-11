// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-19
// @version   1.0 
//
// Description 
// This API is used to find an agent allocation in EXTAGA by priority order
// Transaction FindAgentAlloc
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230419  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class FindAgentAlloc extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  private final MICallerAPI miCaller; 

	public Integer CONO
	public int inPRIO
	public String inPYNO
  public String inCUNO
  public String inCSCD
  public String inECAR
  public String inPONO
  public String inATAV
  public String inHIE3
  public int inDATE
  public String outAGN1
  public String outAGN2
  public int outPRIO
  public String readIndex
  public boolean agentFound
  public int numberOfKeys
  
  // Constructor 
  public FindAgentAlloc(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi;
     this.database = database;  
     this.program = program;
     this.logger = logger
     this.miCaller = miCaller;
  } 
    
  public void main() { 
     CONO = program.LDAZD.CONO as Integer   
     
     if (mi.in.get("PYNO") != null && mi.in.get("PYNO") != "" && mi.in.get("PYNO") != "*") {
       inPYNO = mi.in.get("PYNO") 
       Optional<DBContainer> OCUSMAPayer = findOCUSMA(CONO, inPYNO)  
       if(!OCUSMAPayer.isPresent()){                         
         mi.error("Payer " + inPYNO + " is invalid")   
         return                                         
       }   
     } else {
       inPYNO = "*"
     }

     if (mi.in.get("CUNO") != null && mi.in.get("CUNO") != "" && mi.in.get("CUNO") != "*") {     
        inCUNO = mi.in.get("CUNO")        
        Optional<DBContainer> OCUSMACustomer = findOCUSMA(CONO, inCUNO)  
        if(!OCUSMACustomer.isPresent()){                         
          mi.error("Customer " + inCUNO + " is invalid")   
          return                                         
        }   
     } else {
        inCUNO = "*"
     }

     if (mi.in.get("CSCD") != null && mi.in.get("CSCD") != "" && mi.in.get("CSCD") != "*") {  
        inCSCD = mi.in.get("CSCD")   
        Optional<DBContainer> CSYTAB = findCSYTAB(CONO, inCSCD)  
        if(!CSYTAB.isPresent()){                         
          mi.error("Country Code " + inCSCD + " is invalid")   
          return                                         
        }   
     } else {
        inCSCD = "*"
     }

     if (mi.in.get("ECAR") != null && mi.in.get("ECAR") != "" && mi.in.get("ECAR") != "*") {       
       inECAR = mi.in.get("ECAR")  
       Optional<DBContainer> CSYSTS = findCSYSTS(CONO, inECAR, inCSCD)  
       if(!CSYSTS.isPresent()){                         
         mi.error("State " + inECAR + " is invalid")   
         return                                         
       } 
     } else {
       inECAR = "*"
     }

     if (mi.in.get("PONO") != null && mi.in.get("PONO") != "" && mi.in.get("PONO") != "*") {       
        inPONO = mi.in.get("PONO")  
     } else {
        inPONO = "*"
     }
     
     if (mi.in.get("ATAV") != null && mi.in.get("ATAV") != "" && mi.in.get("ATAV") != "*") {
        inATAV = mi.in.get("ATAV")
        Optional<DBContainer> MPDOPT = findMPDOPT(CONO, inATAV)  
        if(!MPDOPT.isPresent()){                         
          mi.error("Focus " + inATAV + " is invalid")   
          return                                         
        }   
     } else {
       inATAV = "*"
     }

     if (mi.in.get("HIE3") != null && mi.in.get("HIE3") != "" && mi.in.get("HIE3") != "*") {   
       inHIE3 = mi.in.get("HIE3")
       Optional<DBContainer> MITHRY = findMITHRY(CONO, 3, inHIE3)  
       if(!MITHRY.isPresent()){                         
         mi.error("Hierarchy" + inHIE3 + " is invalid")   
         return                                         
       }   
     } else {
       inHIE3 = "*"
     }

     if (mi.in.get("ORDT") != null) {
        inDATE = mi.in.get("ORDT")  
     } else {
        inDATE = 0
     }

     // Find agent in EXTAGA
     agentFound = false   
     
     //Start to search priority 10
     inPRIO = 10
     findAgent10()
     
     //If not found, try the other priorities until found
     if (!agentFound) {
        inPRIO = 20
        findAgent20()
     }
       
     if (!agentFound) {
        inPRIO = 30
        findAgent30()
     }

     if (!agentFound) {
        inPRIO = 40
        findAgent40()
     }

     if (!agentFound) {
        inPRIO = 50
        findAgent50()
     }
    
     if (agentFound) {
       // Send Output
       setOutPut()
       mi.write() 
     } else {
       mi.error("No Agent Allocation found")   
       return                                         
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
  // Set Output data
  //******************************************************************** 
  void setOutPut() {  
    mi.outData.put("PRIO", String.valueOf(outPRIO)) 
    mi.outData.put("AGN1", outAGN1) 
    mi.outData.put("AGN2", outAGN2)
  } 

   
   //******************************************************************** 
   // Find agent from EXTAGA - PRIO 10
   //********************************************************************  
   void findAgent10(){   
     
     ExpressionFactory expression = database.getExpressionFactory("EXTAGA")

     // CONO will always have a value in the table
     if (CONO != null && CONO != 0) {   
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }

     // PRIO will always have a value in the table
     if (inPRIO != null && inPRIO != 0) {   
         expression = expression.and(expression.eq("EXPRIO", String.valueOf(inPRIO)))
     }

     if (inCUNO != null && inCUNO != "" && inCUNO != "*") {   
         expression = expression.and(expression.eq("EXCUNO", inCUNO))
     }

     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {   
         expression = expression.and(expression.eq("EXCSCD", inCSCD))
     }

     if (inATAV != null && inATAV != "" && inATAV != "*") {   
         expression = expression.and(expression.eq("EXATAV", inATAV))
     }

     if (inHIE3 != null && inHIE3 != "" && inHIE3 != "*") {   
         expression = expression.and(expression.eq("EXHIE3", inHIE3))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inDATE != null && inDATE != 0) {   
        expression = expression.and(expression.le("EXFDAT", String.valueOf(inDATE)))
        expression = expression.and(expression.ge("EXTDAT", String.valueOf(inDATE)))
     }

     DBAction actionline10 = database.table("EXTAGA").index("00").matching(expression).selection("EXPRIO", "EXAGN1", "EXAGN2").build()
	   DBContainer line10 = actionline10.getContainer()   
     
     actionline10.readAll(line10, 0, releasedLineProcessor10)               

   } 

    
  //******************************************************************** 
  // List Agents - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor10 = { DBContainer line10 -> 

      // Output
      outPRIO = line10.get("EXPRIO") 
      outAGN1 = line10.get("EXAGN1") 
      outAGN2 = line10.get("EXAGN2") 
      
      if ((outAGN1 != null && outAGN1 != "" && outAGN1 != "*" && outAGN1 != "᠀") || (outAGN2 != null && outAGN2 != "" && outAGN2 != "*" && outAGN2 != "᠀")) {
        agentFound = true
      }
  
  }

  
   //******************************************************************** 
   // Find agent from EXTAGA - PRIO 20
   //********************************************************************  
   void findAgent20(){   
     
     ExpressionFactory expression = database.getExpressionFactory("EXTAGA")

     // CONO will always have a value in the table
     if (CONO != null && CONO != 0) {   
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }

     // PRIO will always have a value in the table
     if (inPRIO != null && inPRIO != 0) {   
         expression = expression.and(expression.eq("EXPRIO", String.valueOf(inPRIO)))
     }

     if (inCUNO != null && inCUNO != "" && inCUNO != "*") {   
         expression = expression.and(expression.eq("EXCUNO", inCUNO))
     }

     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {   
         expression = expression.and(expression.eq("EXCSCD", inCSCD))
     }

     if (inATAV != null && inATAV != "" && inATAV != "*") {   
         expression = expression.and(expression.eq("EXATAV", inATAV))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inDATE != null && inDATE != 0) {   
        expression = expression.and(expression.le("EXFDAT", String.valueOf(inDATE)))
        expression = expression.and(expression.ge("EXTDAT", String.valueOf(inDATE)))
     }

     DBAction actionline20 = database.table("EXTAGA").index("00").matching(expression).selection("EXPRIO", "EXAGN1", "EXAGN2").build()
	   DBContainer line20 = actionline20.getContainer()   
     
     actionline20.readAll(line20, 0, releasedLineProcessor20)               

   } 

    
  //******************************************************************** 
  // List Agents - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor20 = { DBContainer line20 -> 

      // Output
      outPRIO = line20.get("EXPRIO") 
      outAGN1 = line20.get("EXAGN1") 
      outAGN2 = line20.get("EXAGN2") 
      
      if ((outAGN1 != null && outAGN1 != "" && outAGN1 != "*" && outAGN1 != "᠀") || (outAGN2 != null && outAGN2 != "" && outAGN2 != "*" && outAGN2 != "᠀")) {
        agentFound = true
      }
  
  }

  
   //******************************************************************** 
   // Find agent from EXTAGA - PRIO 30
   //********************************************************************  
   void findAgent30(){   
     
     ExpressionFactory expression = database.getExpressionFactory("EXTAGA")

     // CONO will always have a value in the table
     if (CONO != null && CONO != 0) {   
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }

     // PRIO will always have a value in the table
     if (inPRIO != null && inPRIO != 0) {   
         expression = expression.and(expression.eq("EXPRIO", String.valueOf(inPRIO)))
     }

     if (inPYNO != null && inPYNO != "" && inPYNO != "*") {   
         expression = expression.and(expression.eq("EXPYNO", inPYNO))
     }

     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {   
         expression = expression.and(expression.eq("EXCSCD", inCSCD))
     }

     if (inATAV != null && inATAV != "" && inATAV != "*") {   
         expression = expression.and(expression.eq("EXATAV", inATAV))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inDATE != null && inDATE != 0) {   
        expression = expression.and(expression.le("EXFDAT", String.valueOf(inDATE)))
        expression = expression.and(expression.ge("EXTDAT", String.valueOf(inDATE)))
     }

     DBAction actionline30 = database.table("EXTAGA").index("00").matching(expression).selection("EXPRIO", "EXAGN1", "EXAGN2").build()
	   DBContainer line30 = actionline30.getContainer()   
     
     actionline30.readAll(line30, 0, releasedLineProcessor30)               

   } 

    
  //******************************************************************** 
  // List Agents - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor30 = { DBContainer line30 -> 

      // Output
      outPRIO = line30.get("EXPRIO") 
      outAGN1 = line30.get("EXAGN1") 
      outAGN2 = line30.get("EXAGN2") 
      
      if ((outAGN1 != null && outAGN1 != "" && outAGN1 != "*" && outAGN1 != "᠀") || (outAGN2 != null && outAGN2 != "" && outAGN2 != "*" && outAGN2 != "᠀")) {
        agentFound = true
      }
  
  }


   //******************************************************************** 
   // Find agent from EXTAGA - PRIO 40
   //********************************************************************  
   void findAgent40(){   
     
     ExpressionFactory expression = database.getExpressionFactory("EXTAGA")

     // CONO will always have a value in the table
     if (CONO != null && CONO != 0) {   
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }

     // PRIO will always have a value in the table
     if (inPRIO != null && inPRIO != 0) {   
         expression = expression.and(expression.eq("EXPRIO", String.valueOf(inPRIO)))
     }

     if (inPONO != null && inPONO != "" && inPONO != "*") {   
         expression = expression.and(expression.eq("EXPONO", inPONO))
     }

     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {   
         expression = expression.and(expression.eq("EXCSCD", inCSCD))
     }

     if (inATAV != null && inATAV != "" && inATAV != "*") {   
         expression = expression.and(expression.eq("EXATAV", inATAV))
     }

     if (inHIE3 != null && inHIE3 != "" && inHIE3 != "*") {   
         expression = expression.and(expression.eq("EXHIE3", inHIE3))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inDATE != null && inDATE != 0) {   
        expression = expression.and(expression.le("EXFDAT", String.valueOf(inDATE)))
        expression = expression.and(expression.ge("EXTDAT", String.valueOf(inDATE)))
     }

     DBAction actionline40 = database.table("EXTAGA").index("00").matching(expression).selection("EXPRIO", "EXAGN1", "EXAGN2").build()
	   DBContainer line40 = actionline40.getContainer()   
     
     actionline40.readAll(line40, 0, releasedLineProcessor40)               

   } 

    
  //******************************************************************** 
  // List Agents - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor40 = { DBContainer line40 -> 

      // Output
      outPRIO = line40.get("EXPRIO") 
      outAGN1 = line40.get("EXAGN1") 
      outAGN2 = line40.get("EXAGN2") 
      
      if ((outAGN1 != null && outAGN1 != "" && outAGN1 != "*" && outAGN1 != "᠀") || (outAGN2 != null && outAGN2 != "" && outAGN2 != "*" && outAGN2 != "᠀")) {
        agentFound = true
      }
  
  }


   //******************************************************************** 
   // Find agent from EXTAGA - PRIO 50
   //********************************************************************  
   void findAgent50(){  
     
     ExpressionFactory expression = database.getExpressionFactory("EXTAGA")

     // CONO will always have a value in the table
     if (CONO != null && CONO != 0) {   
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }

     // PRIO will always have a value in the table
     if (inPRIO != null && inPRIO != 0) {   
         expression = expression.and(expression.eq("EXPRIO", String.valueOf(inPRIO)))
     }

     if (inPONO != null && inPONO != "" && inPONO != "*") {   
         expression = expression.and(expression.eq("EXPONO", inPONO))
     }

     if (inCSCD != null && inCSCD != "" && inCSCD != "*") {   
         expression = expression.and(expression.eq("EXCSCD", inCSCD))
     }

     if (inATAV != null && inATAV != "" && inATAV != "*") {   
         expression = expression.and(expression.eq("EXATAV", inATAV))
     }

     if (inECAR != null && inECAR != "" && inECAR != "*") {   
         expression = expression.and(expression.eq("EXECAR", inECAR))
     }

     if (inDATE != null && inDATE != 0) {   
        expression = expression.and(expression.le("EXFDAT", String.valueOf(inDATE)))
        expression = expression.and(expression.ge("EXTDAT", String.valueOf(inDATE)))
     }

     DBAction actionline50 = database.table("EXTAGA").index("00").matching(expression).selection("EXPRIO", "EXAGN1", "EXAGN2").build()
	   DBContainer line50 = actionline50.getContainer()   
     
     actionline50.readAll(line50, 0, releasedLineProcessor50)               

   } 

    
  //******************************************************************** 
  // List Agents - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor50 = { DBContainer line50 -> 

      // Output
      outPRIO = line50.get("EXPRIO") 
      outAGN1 = line50.get("EXAGN1") 
      outAGN2 = line50.get("EXAGN2") 
      
      if ((outAGN1 != null && outAGN1 != "" && outAGN1 != "*" && outAGN1 != "᠀") || (outAGN2 != null && outAGN2 != "" && outAGN2 != "*" && outAGN2 != "᠀")) {
        agentFound = true
      }
  
  }

}