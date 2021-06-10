
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-04-21
// @version   1,0 
//
// Description 
// This API transacation LstVendor is used for send data to ESKAR from M3
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


public class LstVendor extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;  
  
  // Definition 
  public int Company  
  public String Supplier 
  public int AddressType 
  public String AddressID 
  public String InSTAT
  public int InADTE
  public String InADID
  public int InCONO  
    
  // Definition of output fields
  public String OutSUNO  
  public String OutSUNM  
  public String OutSTAT  
  public String OutPHNO
  public String OutTFNO  
  public String OutCSCD 
  public String OutECAR  
  public String OutVRNO 
  public String OutCUCD
  public String OutTEPY
  public String OutADR1   
  public String OutTOWN  
  public String OutPONO 
  public String OutEMAL

  
  // Constructor 
  public LstVendor(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger; 
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
    def CIDVEN = query.getContainer()
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
  private Optional<DBContainer> findCIDADR(Integer CONO, String SUNO, Integer ADTE, String ADID){  
    DBAction query = database.table("CIDADR").index("00").selection("SAADR1", "SATOWN", "SAPONO").build()
    def CIDADR = query.getContainer()
    CIDADR.set("SACONO", CONO)
    CIDADR.set("SASUNO", SUNO)
    CIDADR.set("SAADTE", ADTE)
    CIDADR.set("SAADID", ADID)
    //CIDADR.set("SASTDT", 20080101)
    if(query.read(CIDADR))  { 
      return Optional.of(CIDADR)
    } 
  
    return Optional.empty()
  }

  
  //******************************************************************** 
  // Get Email address CEMAIL
  //******************************************************************** 
  private Optional<DBContainer> findCEMAIL(Integer CONO, String SUNO){   
    DBAction query = database.table("CEMAIL").index("00").selection("CBEMAL").build()
    def CEMAIL = query.getContainer()
    CEMAIL.set("CBCONO", CONO)
    CEMAIL.set("CBEMTP", "02")
    CEMAIL.set("CBEMKY", SUNO)
    if(query.read(CEMAIL))  { 
      return Optional.of(CEMAIL)
    } 
    
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      int InCONO = getCONO()  
      
      InSTAT = mi.in.get("STAT")  
      InADTE = mi.in.get("ADTE")  
      InADID = mi.in.get("ADID") 

      // Start the listing in CIDMAS
      LstVendorRecord()
   
  }
 
  //******************************************************************** 
  // Check if null or empty
  //********************************************************************  
   public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
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
  void SetOutPut() {
     
    //mi.outData.put("CONO", OutCONO) 
    mi.outData.put("SUNO", OutSUNO)
    mi.outData.put("SUNM", OutSUNM)
    mi.outData.put("STAT", OutSTAT)
    mi.outData.put("PHNO", OutPHNO)  
    mi.outData.put("TFNO", OutTFNO)  
    mi.outData.put("CSCD", OutCSCD)  
    mi.outData.put("ECAR", OutECAR)
    mi.outData.put("VRNO", OutVRNO)  
    mi.outData.put("CUCD", OutCUCD)
    mi.outData.put("TEPY", OutTEPY)
    mi.outData.put("ADR1", OutADR1)    
    mi.outData.put("TOWN", OutTOWN)  
    mi.outData.put("PONO", OutPONO) 
    mi.outData.put("EMAL", OutEMAL) 

  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void LstVendorRecord(){   
     
     // List all Purchase Order lines
     ExpressionFactory expression = database.getExpressionFactory("CIDMAS")
   
     // Depending on input value (Status)
     // If status is blank, read all, else read only with the input status
     expression = expression.eq("IDSTAT", String.valueOf(InSTAT))

     // List Purchase order line   
     //DBAction actionline = database.table("CIDMAS").index("00").matching(expression).selectAllFields().build()   //D 20210604
     DBAction actionline = database.table("CIDMAS").index("00").matching(expression).selection("IDCONO", "IDSUNO", "IDSUNM", "IDSTAT", "IDPHNO", "IDTFNO", "IDCSCD", "IDECAR", "IDVRNO").build()   //A 20210604 

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("IDCONO", CONO)  
     actionline.readAll(line, 1, releasedLineProcessor)   
   
   } 
    
  //******************************************************************** 
  // List Purchase order line - main loop - CIDMAS
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Fields from CIDMAS to use in the other read
  Company = line.get("IDCONO")
  Supplier = line.get("IDSUNO") 

  // Output selectAllFields 
  OutSUNO = String.valueOf(line.get("IDSUNO")) 
  OutSUNM = String.valueOf(line.get("IDSUNM"))  
  OutSTAT = String.valueOf(line.get("IDSTAT"))  
  OutPHNO = String.valueOf(line.get("IDPHNO"))
  OutTFNO = String.valueOf(line.get("IDTFNO"))
  OutCSCD = String.valueOf(line.get("IDCSCD"))
  OutECAR = String.valueOf(line.get("IDECAR"))
  OutVRNO = String.valueOf(line.get("IDVRNO"))

    
  // Get Supplier information 
  Optional<DBContainer> CIDVEN = findCIDVEN(Company, Supplier)
  if(CIDVEN.isPresent()){
    // Record found, continue to get information  
    DBContainer containerCIDVEN = CIDVEN.get() 
    OutCUCD = containerCIDVEN.getString("IICUCD")   
    OutTEPY = containerCIDVEN.getString("IITEPY")   
  } else {
    OutCUCD = ""
    OutTEPY = ""
  } 
     
  // Get Supplier Address information 
  Optional<DBContainer> CIDADR = findCIDADR(Company, Supplier, InADTE, InADID)
  if(CIDADR.isPresent()){
    // Record found, continue to get information  
    DBContainer containerCIDADR = CIDADR.get() 
    OutADR1 = containerCIDADR.getString("SAADR1")   
    OutTOWN = containerCIDADR.getString("SATOWN")   
    OutPONO = containerCIDADR.getString("SAPONO")   
  } else {
    OutADR1 = ""
    OutTOWN = ""
    OutPONO = ""
  } 
  
  //Get Email Address
  Optional<DBContainer> CEMAIL = findCEMAIL(Company, Supplier)
  if(CEMAIL.isPresent()){
    // Record found, continue to get information  
    DBContainer containerCEMAIL = CEMAIL.get()    
    OutEMAL = containerCEMAIL.getString("CBEMAL")
  } else {
    OutEMAL = ""
  }

    
  // Send Output
  SetOutPut()
  mi.write() 
} 
}
 