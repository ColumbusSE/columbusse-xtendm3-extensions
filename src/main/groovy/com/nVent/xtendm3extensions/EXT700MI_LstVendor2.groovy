
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-21
// @version   1.0 
//
// Description 
// This API transacation LstVendor2 is used for send vendor data to Esker from M3
//

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


public class LstVendor2 extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  
  // Definition 
  public int company  
  public String supplier 
  public String inSTAT
  public int inADTE
  public String inADID
  public int inCONO  
  public String suppl
  public String address1
  public String postal
  public String town
  public String bankSuppl
  public String bankAddress1
  public String bankPostal
  public String bankTown
  
  // Definition of output fields
  public String outSUNO  
  public String outSUNM  
  public String outSTAT  
  public String outPHNO
  public String outTFNO  
  public String outCSCD 
  public String outECAR  
  public String outVRNO 
  public String outCUCD
  public String outTEPY
  public String outADR1   
  public String outTOWN  
  public String outPONO 
  public String outEMAL

  
  // Constructor 
  public LstVendor2(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger; 
  } 
     
  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      int inCONO = getCONO()  
      
      inSTAT = mi.in.get("STAT")  
      inADTE = mi.in.get("ADTE")  
      inADID = mi.in.get("ADID") 

      // Start the listing in CIDMAS
      lstVendorRecord()  
  }
   
                
  //******************************************************************** 
  // Get Company from LDA
  //******************************************************************** 
  private Integer getCONO() {
    int company = mi.in.get("CONO") as Integer
    if(company == null){
      company = program.LDAZD.CONO as Integer
    } 
    return company
  } 

  
  //******************************************************************** 
  // Get Supplier information CIDVEN
  //******************************************************************** 
  private Optional<DBContainer> findCIDVEN(Integer CONO, String SUNO){  
    DBAction query = database.table("CIDVEN").index("00").selection("IICUCD", "IITEPY").build()
    DBContainer CIDVEN = query.getContainer()
    CIDVEN.set("IICONO", CONO)
    CIDVEN.set("IISUNO", SUNO)
    if(query.read(CIDVEN))  { 
      return Optional.of(CIDVEN)
    } 
  
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get Supplier address information CIDADR
  //******************************************************************** 
  private Optional<DBContainer> findCIDADR(Integer CONO, String SUNO, Integer ADTE){  
    DBAction query = database.table("CIDADR").index("10").selection("SAADR1", "SATOWN", "SAPONO").build()
    DBContainer CIDADR = query.getContainer()
    CIDADR.set("SACONO", CONO)
    CIDADR.set("SASUNO", SUNO)
    CIDADR.set("SAADTE", ADTE)
    if(query.readAll(CIDADR, 3, addressProcessor))  { 
      return Optional.of(CIDADR)
    } 
  
    return Optional.empty()
  }

  
  //******************************************************************** 
  // List Address from CIDADR
  //********************************************************************  
  Closure<?> addressProcessor = { DBContainer CIDADR ->   
    suppl = CIDADR.getString("SASUNO")
    address1 = CIDADR.getString("SAADR1")
    town = CIDADR.getString("SATOWN")
    postal = CIDADR.getString("SAPONO")
  }
  
  
  //******************************************************************** 
  // Get Supplier bank address information CIDADR
  //******************************************************************** 
  private Optional<DBContainer> findbankCIDADR(Integer CONO, String SUNO, int ADTE){  
    DBAction query = database.table("CIDADR").index("10").selection("SAADR1", "SATOWN", "SAPONO").build()
    DBContainer CIDADR2 = query.getContainer()
    CIDADR2.set("SACONO", CONO)
    CIDADR2.set("SASUNO", SUNO)
    CIDADR2.set("SAADTE", ADTE)
    if(query.readAll(CIDADR2, 3, addressProcessor2))  { 
      return Optional.of(CIDADR2)
    } 
  
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // List BankAddress from CIDADR
  //********************************************************************  
  Closure<?> addressProcessor2 = { DBContainer CIDADR2 ->   
    bankSuppl = CIDADR2.getString("SASUNO")
    bankAddress1 = CIDADR2.getString("SAADR1")
    bankTown = CIDADR2.getString("SATOWN")
    bankPostal = CIDADR2.getString("SAPONO")
  }
  
  //******************************************************************** 
  // Get Email address CEMAIL
  //******************************************************************** 
  private Optional<DBContainer> findCEMAIL(Integer CONO, String SUNO){   
    DBAction query = database.table("CEMAIL").index("00").selection("CBEMAL").build()
    DBContainer CEMAIL = query.getContainer()
    CEMAIL.set("CBCONO", CONO)
    CEMAIL.set("CBEMTP", "02")
    CEMAIL.set("CBEMKY", SUNO)
    if(query.read(CEMAIL))  { 
      return Optional.of(CEMAIL)
    } 
    
    return Optional.empty()
  }
  
   
  //******************************************************************** 
  // Get date in yyyyMMdd format
  // @return date
  //******************************************************************** 
  public String currentDateY8AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }

    
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
    mi.outData.put("SUNO", outSUNO)
    mi.outData.put("SUNM", outSUNM)
    mi.outData.put("STAT", outSTAT)
    mi.outData.put("PHNO", outPHNO)  
    mi.outData.put("TFNO", outTFNO)  
    mi.outData.put("CSCD", outCSCD)  
    mi.outData.put("ECAR", outECAR)
    mi.outData.put("VRNO", outVRNO)  
    mi.outData.put("CUCD", outCUCD)
    mi.outData.put("TEPY", outTEPY)
    mi.outData.put("ADR1", outADR1)    
    mi.outData.put("TOWN", outTOWN)  
    mi.outData.put("PONO", outPONO) 
    mi.outData.put("EMAL", outEMAL) 
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstVendorRecord(){   
     
     // List all Vendor lines
     ExpressionFactory expression = database.getExpressionFactory("CIDMAS")
   
     expression = expression.eq("IDSTAT", String.valueOf(inSTAT))

     // List Purchase order line   
	   DBAction actionline = database.table("CIDMAS").index("00").matching(expression).selection("IDCONO", "IDSUNO", "IDSUNM", "IDSTAT", "IDPHNO", "IDTFNO", "IDCSCD", "IDECAR", "IDVRNO").build()   
     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("IDCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        

     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   
   } 
    
  //******************************************************************** 
  // List Purchase order line - main loop - CIDMAS
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
    // Fields from CIDMAS to use in the other read
    company = line.get("IDCONO")
    supplier = line.get("IDSUNO") 
  
    // Output selectAllFields 
    outSUNO = String.valueOf(line.get("IDSUNO")) 
    outSUNM = String.valueOf(line.get("IDSUNM"))  
    outSTAT = String.valueOf(line.get("IDSTAT"))  
    outPHNO = String.valueOf(line.get("IDPHNO"))
    outTFNO = String.valueOf(line.get("IDTFNO"))
    outCSCD = String.valueOf(line.get("IDCSCD"))
    outECAR = String.valueOf(line.get("IDECAR"))
    outVRNO = String.valueOf(line.get("IDVRNO"))
  
      
    // Get Supplier information 
    Optional<DBContainer> CIDVEN = findCIDVEN(company, supplier)
    if (CIDVEN.isPresent()) {
      // Record found, continue to get information  
      DBContainer containerCIDVEN = CIDVEN.get() 
      outCUCD = containerCIDVEN.getString("IICUCD")   
      outTEPY = containerCIDVEN.getString("IITEPY")   
    } else {
      outCUCD = ""
      outTEPY = ""
    } 
       
    // Get Supplier Address information 
    Optional<DBContainer> CIDADR = findCIDADR(company, supplier, inADTE)
    if (CIDADR.isPresent()) {
      // Record found, continue to get information  
      DBContainer containerCIDADR = CIDADR.get() 
      outADR1 = address1
      outTOWN = town  
      outPONO = postal 
    } else {
      outADR1 = ""
      outTOWN = ""
      outPONO = ""
    } 
    
    Optional<DBContainer> CIDADR2 = findbankCIDADR(company, supplier, 10)
    if (CIDADR2.isPresent()) {
      // Record found, continue to get information  
      DBContainer containerCIDADR2 = CIDADR2.get() 
      outADR1 = bankAddress1
      outTOWN = bankTown  
      outPONO = bankPostal 
    } 
    
    //Get Email Address
    Optional<DBContainer> CEMAIL = findCEMAIL(company, supplier)
    if (CEMAIL.isPresent()) {
      // Record found, continue to get information  
      DBContainer containerCEMAIL = CEMAIL.get()    
      outEMAL = containerCEMAIL.getString("CBEMAL")
    } else {
      outEMAL = ""
    }
  
    // Send Output
    setOutput()
    mi.write() 
  } 
  
}
 