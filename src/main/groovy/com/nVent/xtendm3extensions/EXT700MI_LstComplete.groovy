
// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2021-02-03
// @version   1,0 
//
// Description 
// This API transacation LstComplete is used to send data to ESKAR from M3
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math


public class LstComplete extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;  
  
  // Definition 
  public int Company  
  public String Division
  public String PurchaseOrder 
  public int PurchaseLine 
  public int PurchaseSuffix 
  public String Supplier
  public String Buyer 
  public int RecNumber 
  public String ItemNumber
  public double OrderQtyBaseUnit 
  public double InvQtyBaseUnit 
  public double RecQtyBaseUnit 
  public double ORQA 
  public double IVQA 
  public double RVQA   
  public double IVNA 
  public String PUUN 
  public double Discount1 
  public double Discount2  
  public double Discount3 
  public double ConfirmedDiscount1 
  public double ConfirmedDiscount2 
  public double ConfirmedDiscount3 
  public double ConfirmedPrice  
  public double LineAmount  
  public double LineQty  
  public double ResultDiscount 
  public double ResultConfirmedDiscount 
  public double RCAC  
  public double SERA  
  public double IVQT  
  public double AccRecCostAmount 
  public double AccRecExcRate  
  public double AccRecQty  
  public double AccResult 
  public double COFA  
  public int DMCF 
  public double AccInvQty  
  public double AccInvAmount 
  public int RegDate
  public String PO
  public String InRegDate
  public boolean AlreadySentOut
  public int countFGRECL  
  public String CC_CountryCode
  public String ID_CountryCode
  public double RecCostAmount 
  public double RecExcRate 
  public double Result 
  public double CalcCOFA1   
  public double CalcDMCF1   
  public double CalcCOFA2   
  public double CalcDMCF2   
  public double ResultFACT1   
  public double ResultFACT2   
  public double ResultFACTTotal   
  public String PPUN       
    
  // Definition of output fields
  public String OutLPUD
  public String OutPNLI  
  public String OutPNLS  
  public String OutITNO  
  public String OutLNAM
  public String OutPITD  
  public String OutORQT 
  public String OutRVQT  
  public String OutCONO 
  public String OutDIVI
  public String OutPUNO
  public String OutSUNO   
  public String OutNTAM  
  public String OutPUDT 
  public String OutEMAL
  public String OutORIG
  public String OutUNPR
  public String OutDEAH
  public String OutGRAM
  public String OutDEAL 
  public String OutSUDO 
  public String OutRPQT
  public String OutTRDT  
  public String OutREPN 
  public String OutIVQT
  public String OutCOMP
  public String OutGRIQ
  public String OutGRIA
  public String OutTIVA
  public String OutIVNA
  public String OutFACT    
  
  
  // Constructor 
  public LstComplete(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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
  // Get Division information CMNDIV
  //******************************************************************** 
  private Optional<DBContainer> findCMNDIV(Integer CONO, String DIVI){  
    DBAction query = database.table("CMNDIV").index("00").selection("CCCONO", "CCCSCD", "CCDIVI").build()
    def CMNDIV = query.getContainer()
    CMNDIV.set("CCCONO", CONO)
    CMNDIV.set("CCDIVI", DIVI)
    if(query.read(CMNDIV))  { 
      return Optional.of(CMNDIV)
    } 
  
    return Optional.empty()
  }
   
  //******************************************************************** 
  // Get Division information MPHEAD
  //******************************************************************** 
  private Optional<DBContainer> findMPHEAD(Integer CONO, String PUNO){  
    DBAction query = database.table("MPHEAD").index("00").selectAllFields().build()
    def MPHEAD = query.getContainer()
    MPHEAD.set("IACONO", CONO)
    MPHEAD.set("IAPUNO", PUNO)
    if(query.read(MPHEAD))  { 
      return Optional.of(MPHEAD)
    } 
  
    return Optional.empty()
  } 
  
  //******************************************************************** 
  // Accumulate value from FGINLI
  //******************************************************************** 
  private List<DBContainer> ListFGINLI(Integer CONO, String PUNO, Integer PNLI, Integer PNLS, Integer REPN){ 
    List<DBContainer>InvLine = new ArrayList() 
    DBAction query = database.table("FGINLI").index("20").selection("F5IVNA", "F5IVQT").build() 
    DBContainer FGINLI = query.getContainer() 
    FGINLI.set("F5CONO", CONO)   
    FGINLI.set("F5PUNO", PUNO) 
    FGINLI.set("F5PNLI", PNLI) 
    FGINLI.set("F5PNLS", PNLS) 
    FGINLI.set("F5REPN", REPN) 
    if(REPN == 0 && PNLI == 0 && PNLS == 0){
    int countFGINLI = query.readAll(FGINLI, 2,{ DBContainer record ->  
     InvLine.add(record) 
    })
    } else if(REPN == 0){
    int countFGINLI = query.readAll(FGINLI, 4,{ DBContainer record ->  
     InvLine.add(record) 
    })
    } else{
      int countFGINLI = query.readAll(FGINLI, 5,{ DBContainer record ->  
     InvLine.add(record) 
    })
    }
  
    return InvLine
  } 
   
  //******************************************************************** 
  // Accumulate value from FGRECL 
  //********************************************************************  
  private List<DBContainer> ListFGRECL(int CONO, String DIVI, String PUNO, int PNLI, int PNLS){
    List<DBContainer>RecLine = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("FGRECL")
    expression = expression.eq("F2RELP", "1") 
    def query = database.table("FGRECL").index("00").matching(expression).selection("F2IMST", "F2SUDO", "F2RPQT", "F2TRDT", "F2REPN", "F2IVQT", "F2RCAC", "F2SERA", "F2RPQT").build()
    def FGRECL = query.createContainer()
    FGRECL.set("F2CONO", CONO)
    FGRECL.set("F2DIVI", DIVI)
    FGRECL.set("F2PUNO", PUNO)
    FGRECL.set("F2PNLI", PNLI)
    FGRECL.set("F2PNLS", PNLS) 
    if(PNLI == 0 && PNLS == 0){
      query.readAll(FGRECL, 3,{ DBContainer record ->  
       RecLine.add(record.createCopy()) 
    })
    } else {
       int countFGRECL = query.readAll(FGRECL, 5,{ DBContainer record -> 
       
       RecLine.add(record.createCopy()) 
    })
    }
    
    return RecLine
  }
  //******************************************************************** 
  // Get Supplier information CIDMAS
  //******************************************************************** 
  private Optional<DBContainer> findCIDMAS(Integer CONO, String SUNO){  
    DBAction query = database.table("CIDMAS").index("00").selection("IDCSCD").build()
    def CIDMAS = query.getContainer()
    CIDMAS.set("IDCONO", CONO)
    CIDMAS.set("IDSUNO", SUNO)
    if(query.read(CIDMAS))  { 
      return Optional.of(CIDMAS)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Get Email address CEMAIL
  //******************************************************************** 
  private Optional<DBContainer> findCEMAIL(Integer CONO, String BUYE){   
    DBAction query = database.table("CEMAIL").index("00").selection("CBEMAL").build()
    def CEMAIL = query.getContainer()
    CEMAIL.set("CBCONO", CONO)
    CEMAIL.set("CBEMTP", "04")
    CEMAIL.set("CBEMKY", BUYE)
    if(query.read(CEMAIL))  { 
      return Optional.of(CEMAIL)
    } 
    
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get Alternativ unit MITAUN
  //******************************************************************** 
   private Optional<DBContainer> findMITAUN(Integer CONO, String ITNO, Integer AUTP, String ALUN){  
    DBAction query = database.table("MITAUN").index("00").selection("MUCOFA", "MUDMCF", "MUAUTP", "MUALUN").build()
    def MITAUN = query.getContainer()
    MITAUN.set("MUCONO", CONO)
    MITAUN.set("MUITNO", ITNO)
    MITAUN.set("MUAUTP", AUTP)
    MITAUN.set("MUALUN", ALUN)
    if(query.read(MITAUN))  { 
      return Optional.of(MITAUN)
    } 
    
    return Optional.empty()
  }   
  
  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      int CONO = getCONO()  
      
      // If Registration date and/or Purchae order are filled it will be used 
      InRegDate = mi.in.get("RGDT")  
      if(isNullOrEmpty(InRegDate)){ 
        RegDate = 0
      }else{
        RegDate = mi.in.get("RGDT")
      } 
      
      PO = mi.in.get("PUNO")  
      
      // Start the listing in MPLINE
      LstRecord()
   
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
  // Set Output data
  //******************************************************************** 
  void SetOutPut() {
     
    mi.outData.put("CONO", OutCONO) 
    mi.outData.put("DIVI", OutDIVI)
    mi.outData.put("PUNO", OutPUNO)
    mi.outData.put("SUNO", OutSUNO)
    mi.outData.put("LPUD", OutLPUD)
    mi.outData.put("PNLI", OutPNLI)  
    mi.outData.put("PNLS", OutPNLS)  
    mi.outData.put("ITNO", OutITNO)  
    mi.outData.put("LNAM", OutLNAM)
    mi.outData.put("PITD", OutPITD)  
    mi.outData.put("ORQT", OutORQT)
    mi.outData.put("IVQT", OutIVQT)
    mi.outData.put("RVQT", OutRVQT)    
    mi.outData.put("NTAM", OutNTAM)  
    mi.outData.put("PUDT", OutPUDT) 
    mi.outData.put("EMAL", OutEMAL) 
    mi.outData.put("ORIG", OutORIG)
    mi.outData.put("UNPR", OutUNPR)
    mi.outData.put("DEAH", OutDEAH)
    mi.outData.put("GRAM", OutGRAM)
    mi.outData.put("DEAL", OutDEAL) 
    mi.outData.put("SUDO", OutSUDO) 
    mi.outData.put("RPQT", OutRPQT) 
    mi.outData.put("TRDT", OutTRDT)  
    mi.outData.put("REPN", OutREPN) 
    mi.outData.put("GRIQ", OutGRIQ)
    mi.outData.put("COMP", OutCOMP)
    mi.outData.put("GRIQ", OutGRIQ)
    mi.outData.put("GRIA", OutGRIA)
    mi.outData.put("TIVA", OutTIVA)
    mi.outData.put("IVNA", OutIVNA) 
    mi.outData.put("FACT", OutFACT)    
    
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void LstRecord(){   
     
     // List all Purchase Order lines
     ExpressionFactory expression = database.getExpressionFactory("MPLINE")
   
     // Depending on input value (Registrationdate and Purchase order)
     if(RegDate != 0 && !isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBRGDT", String.valueOf(RegDate))).and(expression.eq("IBPUNO",  String.valueOf(PO)))
     }else if(RegDate != 0 && isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBRGDT", String.valueOf(RegDate))) 
     }else if(RegDate == 0 && !isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBPUNO",  String.valueOf(PO)))
     }else{
       expression = expression.le("IBPUST", "80")        
     }
     
     // List Purchase order line   
     DBAction actionline = database.table("MPLINE").index("00").matching(expression).selectAllFields().build()   
     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("IBCONO", CONO)  
     actionline.readAll(line, 1, releasedLineProcessor)   
   
   } 
    
  //******************************************************************** 
  // List Purchase order line - main loop - MPLINE
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Fields from MPLINE to use in the other read
  Company = line.get("IBCONO")
  ItemNumber = line.get("IBITNO") 
  PurchaseOrder = line.get("IBPUNO") 
  PurchaseLine = line.get("IBPNLI") 
  PurchaseSuffix = line.get("IBPNLS")  
    
  // Output selectAllFields 
  OutLPUD = String.valueOf(line.get("IBLPUD"))
  OutPNLI = String.valueOf(line.get("IBPNLI")) 
  OutPNLS = String.valueOf(line.get("IBPNLS"))  
  OutITNO = String.valueOf(line.get("IBITNO"))  
  OutLNAM = String.valueOf(line.get("IBLNAM"))
  OutPITD = String.valueOf(line.get("IBPITD"))

  // Fields for calculation
  ORQA = line.get("IBORQA")
  IVQA = line.get("IBIVQA")
  RVQA = line.get("IBRVQA") 
  PUUN = line.get("IBPUUN") 
  PPUN = line.get("IBPPUN")    
    
  // Calculate with alternativ unit 
  Optional<DBContainer> MITAUN = findMITAUN(Company, ItemNumber, 1, PUUN)
  if(MITAUN.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN = MITAUN.get() 
    COFA = containerMITAUN.get("MUCOFA")
    DMCF = containerMITAUN.get("MUDMCF") 
    if(DMCF == 1){
      OrderQtyBaseUnit = ORQA * COFA
      InvQtyBaseUnit = IVQA * COFA
      RecQtyBaseUnit = RVQA * COFA
    }else {
      if(COFA != 0){ 
       OrderQtyBaseUnit = ORQA / COFA
       InvQtyBaseUnit = IVQA / COFA
       RecQtyBaseUnit = RVQA / COFA
      } 
    } 
    OutORQT = String.valueOf(OrderQtyBaseUnit)
    OutIVQT = String.valueOf(InvQtyBaseUnit)
    OutRVQT = String.valueOf(RecQtyBaseUnit) 
  } else { 
    OutORQT = String.valueOf(ORQA)
    OutIVQT = String.valueOf(IVQA)
    OutRVQT = String.valueOf(RVQA)
  }
  
  // Calculate with Unit of measure factor   
  // Get COFA and DMCF from PPUN             
  Optional<DBContainer> MITAUN1 = findMITAUN(Company, ItemNumber, 2, PPUN)
  if(MITAUN1.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN1 = MITAUN1.get() 
    CalcCOFA1 = containerMITAUN1.get("MUCOFA")
    CalcDMCF1 = containerMITAUN1.get("MUDMCF") 
  } else { 
    CalcCOFA1 = 1
    CalcDMCF1 = 1
  }
  
  // Get COFA and DMCF from PUUN            
  Optional<DBContainer> MITAUN2 = findMITAUN(Company, ItemNumber, 2, PUUN)
  if(MITAUN2.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN2 = MITAUN2.get() 
    CalcCOFA2 = containerMITAUN2.get("MUCOFA")
    CalcDMCF2 = containerMITAUN2.get("MUDMCF") 
  } else { 
    CalcCOFA2 = 1
    CalcDMCF2 = 1
  }

  //Calculate the UoM factor               
  ResultFACT1 = Math.pow(CalcCOFA2,((CalcDMCF2 * -2) + 3))
  ResultFACT2 = Math.pow(CalcCOFA1,((CalcDMCF1 * 2) - 3))
  ResultFACTTotal = ResultFACT1 * ResultFACT2
  OutFACT = String.valueOf(ResultFACTTotal)

    
    // Get Purchase order head
  Optional<DBContainer> MPHEAD = findMPHEAD(Company, PurchaseOrder)
  if(MPHEAD.isPresent()){
     // Record found, continue to get information  
    DBContainer containerMPHEAD = MPHEAD.get()  
    
    // Output fields   
    OutCONO = String.valueOf(containerMPHEAD.get("IACONO"))
    OutDIVI = containerMPHEAD.getString("IADIVI")
    OutPUNO = containerMPHEAD.getString("IAPUNO")
    OutSUNO = containerMPHEAD.getString("IASUNO")   
    OutNTAM = String.valueOf(containerMPHEAD.get("IANTAM"))  
    OutPUDT = String.valueOf(containerMPHEAD.get("IAPUDT"))
    
    // Fields from MPHEAD to use in the other read
    Division = containerMPHEAD.getString("IADIVI")  
    Supplier = containerMPHEAD.getString("IASUNO")  
    Buyer = containerMPHEAD.getString("IABUYE")   
   
    // Get Email address for Buyer
    Optional<DBContainer> CEMAIL = findCEMAIL(Company, Buyer)
    if(CEMAIL.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCEMAIL = CEMAIL.get()    
      OutEMAL = containerCEMAIL.getString("CBEMAL")
    } 
  
    // Get Supplier information 
    Optional<DBContainer> CIDMAS = findCIDMAS(Company, Supplier)
    if(CIDMAS.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCIDMAS = CIDMAS.get() 
      ID_CountryCode = containerCIDMAS.getString("IDCSCD")   
    }  
     
    // Get Division information
    Optional<DBContainer> CMNDIV = findCMNDIV(Company, Division)
    if(CMNDIV.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCMNDIV = CMNDIV.get() 
      CC_CountryCode = containerCMNDIV.getString("CCCSCD")   
    } 
     
    // Compare Division's country code and the Supplier's 
    if(CC_CountryCode != ID_CountryCode){ 
      OutORIG = String.valueOf("FOR")
    }else{ 
      OutORIG = String.valueOf("DOM")
    } 
    
    // Calculate unitprice  
    Discount1 = line.get("IBODI1")
    Discount2 = line.get("IBODI2")
    Discount3 = line.get("IBODI3") 
    ConfirmedDiscount1 = line.get("IBCFD1")
    ConfirmedDiscount2 = line.get("IBCFD2")
    ConfirmedDiscount3 = line.get("IBCFD3")
    ConfirmedPrice = line.get("IBCPPR")
    LineAmount = line.get("IBLNAM")
    LineQty = line.get("IBORQA") 
    
    if(ConfirmedPrice == 0d){
      // Calculate confirmed price from receiving lines
      ResultDiscount = (1 - (0.01 * Discount1)) * (1 - (0.01 * Discount2)) * (1 - (0.01 * Discount3))
      // Get information from receiving lines
      List<DBContainer> ResultFGRECL = ListFGRECL(Company, Division, PurchaseOrder, PurchaseLine, PurchaseSuffix) 
      for (DBContainer RecLine : ResultFGRECL){   
        // Accumulate quantity   
        RCAC = RecLine.get("F2RCAC") 
        SERA = RecLine.get("F2SERA") 
        IVQT = RecLine.get("F2IVQT")  
   
        AccRecCostAmount =+ RCAC 
        AccRecExcRate =+ SERA
        AccRecQty =+ IVQT * LineAmount 
   
    
        if(AccRecExcRate != 0){
          AccResult = (AccRecCostAmount / AccRecExcRate)  * ResultDiscount 
          BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(AccResult) 
          RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
          if(RecConfirmedPrice == 0d){
            if(AccRecQty == 0d){ 
              OutUNPR = String.valueOf(LineQty)
            }else{ 
              OutUNPR = String.valueOf(AccRecQty) 
            }
          }else{ 
            OutUNPR = String.valueOf(RecConfirmedPrice) 
          } 
        }else{
          AccResult = AccRecCostAmount * ResultDiscount   
          BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(AccResult) 
          RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
          if(RecConfirmedPrice == 0d){ 
             OutUNPR = String.valueOf(AccRecQty * LineAmount)
          }else{ 
             OutUNPR = String.valueOf(RecConfirmedPrice) 
          } 
        }  
      }  
    }else{
       // Use confirmed price from orderline
       ResultConfirmedDiscount = (1 - (0.01 * ConfirmedDiscount1)) * (1 - (0.01 * ConfirmedDiscount2)) * (1 - (0.01 * ConfirmedDiscount3))
       AccResult = ConfirmedPrice * ResultConfirmedDiscount
       BigDecimal POConfirmedPrice  = BigDecimal.valueOf(AccResult) 
       POConfirmedPrice = POConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
       OutUNPR = String.valueOf(POConfirmedPrice)
    } 
    
    // Loop Rec invoice header information, to accumulate value  
    AccInvQty = 0
    AccInvAmount = 0
    
    PurchaseLine = 0
    PurchaseSuffix = 0
    RecNumber = 0 
    List<DBContainer> ResultFGRECLHead = ListFGRECL(Company, Division, PurchaseOrder, PurchaseLine, PurchaseSuffix) 
    for (DBContainer RecLine : ResultFGRECLHead){ 
      // Accumulate quantity   
      RCAC = RecLine.get("F2RCAC") 
      SERA = RecLine.get("F2SERA")  
   
      AccRecCostAmount =+ RCAC 
      AccRecExcRate =+ SERA 
    }
     
    // Summarize to output value 
    if(AccRecExcRate != 0){
      AccResult = (AccRecCostAmount / AccRecExcRate)  
      BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(AccResult) 
      RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
      if(RecConfirmedPrice == 0d){  
        OutDEAH = String.valueOf(0)
      }else{ 
        OutDEAH = String.valueOf(RecConfirmedPrice)
      } 
    }else{  
        OutDEAH = String.valueOf(0)
    }  
   
     
    // Loop and send to output Rec invoice line information   
    AlreadySentOut = false
    PurchaseLine = line.get("IBPNLI") 
    PurchaseSuffix = line.get("IBPNLS")  
    List<DBContainer> ResultFGRECL = ListFGRECL(Company, Division, PurchaseOrder, PurchaseLine, PurchaseSuffix) 
    for (DBContainer RecLine : ResultFGRECL){   
    
      // Output   
      OutSUDO = String.valueOf(RecLine.get("F2SUDO")) 
      OutRPQT = String.valueOf(RecLine.get("F2RPQT")) 
      OutTRDT = String.valueOf(RecLine.get("F2TRDT"))  
      OutREPN = String.valueOf(RecLine.get("F2REPN")) 
      OutGRIQ = String.valueOf(RecLine.get("F2IVQT"))
      if(RecLine.get("F2IMST") == 9){ 
        OutCOMP = "Yes" 
      }else{ 
        OutCOMP = "No" 
      } 
        
      // Accumulate quantity   
      RecCostAmount = RecLine.get("F2RCAC") 
      RecExcRate = RecLine.get("F2SERA")   
   
      if(RecExcRate != 0){
        Result = (RecCostAmount / RecExcRate)  
        BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(Result) 
        RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
        if(RecConfirmedPrice == 0d){  
          OutDEAL = String.valueOf(0)  
        }else{ 
          OutDEAL = String.valueOf(RecConfirmedPrice)
        } 
      }else{  
        OutDEAL = String.valueOf(0)  
      } 
    
      // Get Rec invoice line information  (FGINLI)  
      // - rec number level   
      List<DBContainer> ResultFGINLIRec = ListFGINLI(Company, PurchaseOrder, PurchaseLine, PurchaseSuffix, RecNumber) 
      for (DBContainer InvLine : ResultFGINLIRec){ 
         // Accumulate quantity   
         IVQT = InvLine.get("F5IVQT") 
         IVNA = InvLine.get("F5IVNA") 
   
         AccInvQty =+ IVQT 
         AccInvAmount =+ IVNA 
      }
     
      OutGRIQ = String.valueOf(AccInvQty)
      OutGRIA = String.valueOf(AccInvAmount)  
      
      // - line level 
      RecNumber = 0
      List<DBContainer> ResultFGINLILine = ListFGINLI(Company, PurchaseOrder, PurchaseLine, PurchaseSuffix, RecNumber) 
      for (DBContainer InvLine : ResultFGINLILine){  
        // Accumulate amount   
        IVNA = InvLine.get("F5IVNA") 
        AccInvAmount =+ IVNA 
      } 
    
      OutTIVA = String.valueOf(AccInvAmount) 
    
      // - header level 
      PurchaseLine = 0
      PurchaseSuffix = 0
      RecNumber = 0
      List<DBContainer> ResultFGINLIHead = ListFGINLI(Company, PurchaseOrder, PurchaseLine, PurchaseSuffix, RecNumber) 
        for (DBContainer InvLine : ResultFGINLIHead){  
          // Accumulate quantity  
          IVNA = InvLine.get("F5IVNA") 
          AccInvAmount =+ IVNA 
        }
      
      OutIVNA = String.valueOf(AccInvAmount)    
     
      // Send Output parameter, for all received lines 
      SetOutPut()
      mi.write()   
      AlreadySentOut = true 
    } 
   
    // Send Output parameter when no receiving lines exist, send the lines information
    if(!AlreadySentOut){  
      SetOutPut()
      mi.write() 
    } 
  }  
} 
}  