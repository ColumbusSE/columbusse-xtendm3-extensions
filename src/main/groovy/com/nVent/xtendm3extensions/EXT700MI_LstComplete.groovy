
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
  public int company  
  public String division
  public String purchaseOrder 
  public int purchaseLine 
  public int purchaseSuffix 
  public String supplier
  public String buyer 
  public int recNumber 
  public String itemNumber
  public double orderQtyBaseUnit 
  public double invQtyBaseUnit 
  public double recQtyBaseUnit 
  public double ORQA 
  public double IVQA 
  public double RVQA   
  public double IVNA 
  public String PUUN 
  public double discount1 
  public double discount2  
  public double discount3 
  public double confirmedDiscount1 
  public double confirmedDiscount2 
  public double confirmedDiscount3 
  public double confirmedPrice  
  public double lineAmount  
  public double lineQty  
  public double resultDiscount 
  public double resultConfirmedDiscount 
  public double RCAC  
  public double SERA  
  public double IVQT  
  public double accRecCostAmount 
  public double accRecExcRate  
  public double accRecQty  
  public double accResult 
  public double COFA  
  public int DMCF 
  public double accInvQty  
  public double accInvAmount 
  public int regDate
  public String PO
  public String inRegDate
  public boolean alreadySentOut
  public int countFGRECL  
  public String CC_CountryCode
  public String ID_CountryCode
  public double recCostAmount 
  public double recExcRate 
  public double result 
  public double calcCOFA1   
  public double calcDMCF1   
  public double calcCOFA2   
  public double calcDMCF2   
  public double resultFACT1   
  public double resultFACT2   
  public double resultFACTTotal   
  public String PPUN       
    
  // Definition of output fields
  public String outLPUD
  public String outPNLI  
  public String outPNLS  
  public String outITNO  
  public String outLNAM
  public String outPITD  
  public String outORQT 
  public String outRVQT  
  public String outCONO 
  public String outDIVI
  public String outPUNO
  public String outSUNO   
  public String outNTAM  
  public String outPUDT 
  public String outEMAL
  public String outORIG
  public String outUNPR
  public String outDEAH
  public String outGRAM
  public String outDEAL 
  public String outSUDO 
  public String outRPQT
  public String outTRDT  
  public String outREPN 
  public String outIVQT
  public String outCOMP
  public String outGRIQ
  public String outGRIA
  public String outTIVA
  public String outIVNA
  public String outFACT  
  public String outVTCD
  
  
  // Constructor 
  public LstComplete(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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
      int CONO = getCONO()  
      
      // If Registration date and/or Purchae order are filled it will be used 
      inRegDate = mi.in.get("RGDT")  
      if(isNullOrEmpty(inRegDate)){ 
        regDate = 0
      }else{
        regDate = mi.in.get("RGDT")
      } 
      
      PO = mi.in.get("PUNO")  
      
      // Start the listing in MPLINE
      lstRecord()
   
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
    //DBAction query = database.table("MPHEAD").index("00").selectAllFields().build()              //D 20210607
    DBAction query = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI", "IASUNO", "IANTAM", "IAPUDT", "IABUYE").build()    //A 20210607
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
  private List<DBContainer> listFGINLI(Integer CONO, String PUNO, Integer PNLI, Integer PNLS, Integer REPN){ 
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
  private List<DBContainer> listFGRECL(int CONO, String DIVI, String PUNO, int PNLI, int PNLS){
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
  void setOutput() {
     
    mi.outData.put("CONO", outCONO) 
    mi.outData.put("DIVI", outDIVI)
    mi.outData.put("PUNO", outPUNO)
    mi.outData.put("SUNO", outSUNO)
    mi.outData.put("LPUD", outLPUD)
    mi.outData.put("PNLI", outPNLI)  
    mi.outData.put("PNLS", outPNLS)  
    mi.outData.put("ITNO", outITNO)  
    mi.outData.put("LNAM", outLNAM)
    mi.outData.put("PITD", outPITD)  
    mi.outData.put("ORQT", outORQT)
    mi.outData.put("IVQT", outIVQT)
    mi.outData.put("RVQT", outRVQT)    
    mi.outData.put("NTAM", outNTAM)  
    mi.outData.put("PUDT", outPUDT) 
    mi.outData.put("EMAL", outEMAL) 
    mi.outData.put("ORIG", outORIG)
    mi.outData.put("UNPR", outUNPR)
    mi.outData.put("DEAH", outDEAH)
    mi.outData.put("GRAM", outGRAM)
    mi.outData.put("DEAL", outDEAL) 
    mi.outData.put("SUDO", outSUDO) 
    mi.outData.put("RPQT", outRPQT) 
    mi.outData.put("TRDT", outTRDT)  
    mi.outData.put("REPN", outREPN) 
    mi.outData.put("GRIQ", outGRIQ)
    mi.outData.put("COMP", outCOMP)
    mi.outData.put("GRIQ", outGRIQ)
    mi.outData.put("GRIA", outGRIA)
    mi.outData.put("TIVA", outTIVA)
    mi.outData.put("IVNA", outIVNA) 
    mi.outData.put("FACT", outFACT) 
    mi.outData.put("VTCD", outVTCD)    
    
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstRecord(){   
     
     // List all Purchase Order lines
     ExpressionFactory expression = database.getExpressionFactory("MPLINE")
   
     // Depending on input value (Registrationdate and Purchase order)
     /*if(regDate != 0 && !isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBRGDT", String.valueOf(regDate))).and(expression.eq("IBPUNO",  String.valueOf(PO)))     
     }else if(regDate != 0 && isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBRGDT", String.valueOf(regDate))) 
     }else if(regDate == 0 && !isNullOrEmpty(PO)){
       expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBPUNO",  String.valueOf(PO)))
     }else{
       expression = expression.le("IBPUST", "80")   
     }*/
	 
	   // Depending on input value (Registrationdate and Purchase order)
     if (regDate != 0) {
        expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81")).and(expression.eq("IBRGDT", String.valueOf(regDate))) 
     } else if (regDate == 0) {
        expression = expression.gt("IBPUST", "69").and(expression.lt("IBPUST", "81"))
     } else {
        expression = expression.le("IBPUST", "80")   
     }
     
     // List Purchase order line  	 
     //DBAction actionline = database.table("MPLINE").index("00").matching(expression).selection("IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBITNO", "IBLPUD", "IBLNAM", "IBPITD", "IBORQA", "IBIVQA", "IBRVQA", "IBPUUN", "IBPPUN", "IBPUST", "IBRGDT", "IBODI1", "IBODI2", "IBODI3", "IBCPPR", "IBCFD1", "IBCFD2", "IBCFD3", "IBVTCD").build()   //A 20210604 
     DBAction actionline = database.table("MPLINE").index("06").matching(expression).selection("IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBITNO", "IBLPUD", "IBLNAM", "IBPITD", "IBORQA", "IBIVQA", "IBRVQA", "IBPUUN", "IBPPUN", "IBPUST", "IBRGDT", "IBODI1", "IBODI2", "IBODI3", "IBCPPR", "IBCFD1", "IBCFD2", "IBCFD3", "IBVTCD").build()   //A 20210604 
	   DBContainer line = actionline.getContainer()   
     
     // Read with one key  
     line.set("IBCONO", CONO) 
	   line.set("IBPUNO", PO)                                                       //A 20220108
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           //A 20220112
     
     if(!isNullOrEmpty(PO)){                                                       //A 20220108
       actionline.readAll(line, 2, pageSize, releasedLineProcessor)                //A 20220108
     } else {                                                                      //A 20220108
	     actionline.readAll(line, 1, pageSize, releasedLineProcessor)                //A 20220108
     }                                                                             //A 20220108
     //actionline.readAll(line, 1, releasedLineProcessor)                          //D 20220108
   
   } 
    
  //******************************************************************** 
  // List Purchase order line - main loop - MPLINE
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Fields from MPLINE to use in the other read
  company = line.get("IBCONO")
  itemNumber = line.get("IBITNO") 
  purchaseOrder = line.get("IBPUNO") 
  purchaseLine = line.get("IBPNLI") 
  purchaseSuffix = line.get("IBPNLS")  
    
  // Output selectAllFields 
  outLPUD = String.valueOf(line.get("IBLPUD"))
  outPNLI = String.valueOf(line.get("IBPNLI")) 
  outPNLS = String.valueOf(line.get("IBPNLS"))  
  outITNO = String.valueOf(line.get("IBITNO"))  
  outLNAM = String.valueOf(line.get("IBLNAM"))
  outPITD = String.valueOf(line.get("IBPITD"))
  outVTCD = String.valueOf(line.get("IBVTCD"))

  // Fields for calculation
  ORQA = line.get("IBORQA")
  IVQA = line.get("IBIVQA")
  RVQA = line.get("IBRVQA") 
  PUUN = line.get("IBPUUN") 
  PPUN = line.get("IBPPUN")    
    
  // Calculate with alternativ unit 
  Optional<DBContainer> MITAUN = findMITAUN(company, itemNumber, 1, PUUN)
  if(MITAUN.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN = MITAUN.get() 
    COFA = containerMITAUN.get("MUCOFA")
    DMCF = containerMITAUN.get("MUDMCF") 
    if(DMCF == 1){
      orderQtyBaseUnit = ORQA * COFA
      invQtyBaseUnit = IVQA * COFA
      recQtyBaseUnit = RVQA * COFA
    }else {
      if(COFA != 0){ 
       orderQtyBaseUnit = ORQA / COFA
       invQtyBaseUnit = IVQA / COFA
       recQtyBaseUnit = RVQA / COFA
      } 
    } 
    outORQT = String.valueOf(orderQtyBaseUnit)
    outIVQT = String.valueOf(invQtyBaseUnit)
    outRVQT = String.valueOf(recQtyBaseUnit) 
  } else { 
    outORQT = String.valueOf(ORQA)
    outIVQT = String.valueOf(IVQA)
    outRVQT = String.valueOf(RVQA)
  }
  
  // Calculate with Unit of measure factor   
  // Get COFA and DMCF from PPUN             
  Optional<DBContainer> MITAUN1 = findMITAUN(company, itemNumber, 2, PPUN)
  if(MITAUN1.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN1 = MITAUN1.get() 
    calcCOFA1 = containerMITAUN1.get("MUCOFA")
    calcDMCF1 = containerMITAUN1.get("MUDMCF") 
  } else { 
    calcCOFA1 = 1
    calcDMCF1 = 1
  }
  
  // Get COFA and DMCF from PUUN            
  Optional<DBContainer> MITAUN2 = findMITAUN(company, itemNumber, 2, PUUN)
  if(MITAUN2.isPresent()){
    // Record found, continue to get information  
    DBContainer containerMITAUN2 = MITAUN2.get() 
    calcCOFA2 = containerMITAUN2.get("MUCOFA")
    calcDMCF2 = containerMITAUN2.get("MUDMCF") 
  } else { 
    calcCOFA2 = 1
    calcDMCF2 = 1
  }

  //Calculate the UoM factor               
  resultFACT1 = Math.pow(calcCOFA2,((calcDMCF2 * -2) + 3))
  resultFACT2 = Math.pow(calcCOFA1,((calcDMCF1 * 2) - 3))
  resultFACTTotal = resultFACT1 * resultFACT2
  outFACT = String.valueOf(resultFACTTotal)

    
    // Get Purchase order head
  Optional<DBContainer> MPHEAD = findMPHEAD(company, purchaseOrder)
  if(MPHEAD.isPresent()){
     // Record found, continue to get information  
    DBContainer containerMPHEAD = MPHEAD.get()  
    
    // output fields   
    outCONO = String.valueOf(containerMPHEAD.get("IACONO"))
    outDIVI = containerMPHEAD.getString("IADIVI")
    outPUNO = containerMPHEAD.getString("IAPUNO")
    outSUNO = containerMPHEAD.getString("IASUNO")   
    outNTAM = String.valueOf(containerMPHEAD.get("IANTAM"))  
    outPUDT = String.valueOf(containerMPHEAD.get("IAPUDT"))
    
    // Fields from MPHEAD to use in the other read
    division = containerMPHEAD.getString("IADIVI")  
    supplier = containerMPHEAD.getString("IASUNO")  
    buyer = containerMPHEAD.getString("IABUYE")   
   
    // Get Email address for Buyer
    Optional<DBContainer> CEMAIL = findCEMAIL(company, buyer)
    if(CEMAIL.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCEMAIL = CEMAIL.get()    
      outEMAL = containerCEMAIL.getString("CBEMAL")
    } 
  
    // Get Supplier information 
    Optional<DBContainer> CIDMAS = findCIDMAS(company, supplier)
    if(CIDMAS.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCIDMAS = CIDMAS.get() 
      ID_CountryCode = containerCIDMAS.getString("IDCSCD")   
    }  
     
    // Get division information
    Optional<DBContainer> CMNDIV = findCMNDIV(company, division)
    if(CMNDIV.isPresent()){
      // Record found, continue to get information  
      DBContainer containerCMNDIV = CMNDIV.get() 
      CC_CountryCode = containerCMNDIV.getString("CCCSCD")   
    } 
     
    // Compare division's country code and the Supplier's 
    if(CC_CountryCode != ID_CountryCode){ 
      outORIG = String.valueOf("FOR")
    }else{ 
      outORIG = String.valueOf("DOM")
    } 
    
    // Calculate unitprice  
    discount1 = line.get("IBODI1")
    discount2 = line.get("IBODI2")
    discount3 = line.get("IBODI3") 
    confirmedDiscount1 = line.get("IBCFD1")
    confirmedDiscount2 = line.get("IBCFD2")
    confirmedDiscount3 = line.get("IBCFD3")
    confirmedPrice = line.get("IBCPPR")
    lineAmount = line.get("IBLNAM")
    lineQty = line.get("IBORQA") 
    accRecCostAmount = 0    //A 20211115
    accRecExcRate = 0       //A 20211115
    accRecQty = 0          //A 20211115
    resultDiscount = 0     //A 20211115
    accResult = 0          //A 20211115

    resultConfirmedDiscount = 0  //A 20211115
   
    

    if(confirmedPrice == 0d){
      // Calculate confirmed price from receiving lines
      resultDiscount = (1 - (0.01 * discount1)) * (1 - (0.01 * discount2)) * (1 - (0.01 * discount3))
      // Get information from receiving lines
      List<DBContainer> ResultFGRECL = listFGRECL(company, division, purchaseOrder, purchaseLine, purchaseSuffix) 
      for (DBContainer RecLine : ResultFGRECL){   
        // Accumulate quantity   
        RCAC = RecLine.get("F2RCAC") 
        SERA = RecLine.get("F2SERA") 
        IVQT = RecLine.get("F2IVQT")  
   
        accRecCostAmount =+ RCAC 
        accRecExcRate =+ SERA
        accRecQty =+ IVQT * lineAmount 
        
        if(accRecExcRate != 0){
          accResult = (accRecCostAmount / accRecExcRate)  * resultDiscount 
          BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(accResult) 
          RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
          if(RecConfirmedPrice == 0d){
            if(accRecQty == 0d){ 
              outUNPR = String.valueOf(lineQty)
            }else{ 
              outUNPR = String.valueOf(accRecQty) 
            }
          }else{ 
            outUNPR = String.valueOf(RecConfirmedPrice) 
          } 
        }else{
          accResult = accRecCostAmount * resultDiscount   
          BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(accResult) 
          RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
          if(RecConfirmedPrice == 0d){ 
             outUNPR = String.valueOf(accRecQty * lineAmount)
          }else{ 
             outUNPR = String.valueOf(RecConfirmedPrice) 
          } 
        }  
      }  
    }else{
       // Use confirmed price from orderline
       resultConfirmedDiscount = (1 - (0.01 * confirmedDiscount1)) * (1 - (0.01 * confirmedDiscount2)) * (1 - (0.01 * confirmedDiscount3))
       accResult = confirmedPrice * resultConfirmedDiscount
       BigDecimal POConfirmedPrice  = BigDecimal.valueOf(accResult) 
       POConfirmedPrice = POConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
       outUNPR = String.valueOf(POConfirmedPrice)
    } 
    
    // Loop Rec invoice header information, to accumulate value  
    accInvQty = 0
    accInvAmount = 0
    
    purchaseLine = 0
    purchaseSuffix = 0
    recNumber = 0 
    List<DBContainer> ResultFGRECLHead = listFGRECL(company, division, purchaseOrder, purchaseLine, purchaseSuffix) 
    for (DBContainer RecLine : ResultFGRECLHead){ 
      // Accumulate quantity   
      RCAC = RecLine.get("F2RCAC") 
      SERA = RecLine.get("F2SERA")  
   
      accRecCostAmount =+ RCAC 
      accRecExcRate =+ SERA 
    }
     
    // Summarize to output value 
    if(accRecExcRate != 0){
      accResult = (accRecCostAmount / accRecExcRate)  
      BigDecimal RecConfirmedPrice  = BigDecimal.valueOf(accResult) 
      RecConfirmedPrice = RecConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
      if(RecConfirmedPrice == 0d){  
        outDEAH = String.valueOf(0)
      }else{ 
        outDEAH = String.valueOf(RecConfirmedPrice)
      } 
    }else{  
        outDEAH = String.valueOf(0)
    }  
   
     
    // Loop and send to output Rec invoice line information   
    outSUDO = ""  //A 20211115
    outRPQT = ""  //A 20211115
    outTRDT = ""  //A 20211115
    outREPN = ""  //A 20211115
    outGRIQ = ""  //A 20211115
    outGRAM = ""  //A 20211115
    outCOMP = ""  //A 20211115
    outDEAL = ""  //A 20211115

    alreadySentOut = false
    purchaseOrder = line.get("IBPUNO")   //A 20211115
    purchaseLine = line.get("IBPNLI") 
    purchaseSuffix = line.get("IBPNLS")  
    List<DBContainer> ResultFGRECL = listFGRECL(company, division, purchaseOrder, purchaseLine, purchaseSuffix) 
    for (DBContainer RecLine : ResultFGRECL){   
    
      // Output   
      outSUDO = String.valueOf(RecLine.get("F2SUDO")) 
      outRPQT = String.valueOf(RecLine.get("F2RPQT")) 
      outTRDT = String.valueOf(RecLine.get("F2TRDT"))  
      outREPN = String.valueOf(RecLine.get("F2REPN")) 
      outGRIQ = String.valueOf(RecLine.get("F2IVQT"))
      if(RecLine.get("F2IMST") == 9){ 
        outCOMP = "Yes" 
      }else{ 
        outCOMP = "No" 
      } 
        
      // Accumulate quantity   
      recCostAmount = RecLine.get("F2RCAC") 
      recExcRate = RecLine.get("F2SERA")   
   
      if(recExcRate != 0){
        result = (recCostAmount / recExcRate)  
        BigDecimal recConfirmedPrice  = BigDecimal.valueOf(result) 
        recConfirmedPrice = recConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
        if(recConfirmedPrice == 0d){  
          outDEAL = String.valueOf(0)  
          outGRAM = String.valueOf(0)  
        }else{ 
          outDEAL = String.valueOf(recConfirmedPrice)
          outGRAM = String.valueOf(recConfirmedPrice)
        } 
      }else{  
        outDEAL = String.valueOf(0)  
        outGRAM = String.valueOf(0) 
      } 
    
      outGRIQ = ""   //A 20211115
      outGRIA = ""   //A 20211115
      // Get Rec invoice line information  (FGINLI)  
      // - rec number level   
      List<DBContainer> ResultFGINLIRec = listFGINLI(company, purchaseOrder, purchaseLine, purchaseSuffix, recNumber) 
      for (DBContainer InvLine : ResultFGINLIRec){ 
         // Accumulate quantity   
         IVQT = InvLine.get("F5IVQT") 
         IVNA = InvLine.get("F5IVNA") 
   
         accInvQty =+ IVQT 
         accInvAmount =+ IVNA 
      }
     
      outGRIQ = String.valueOf(accInvQty)
      outGRIA = String.valueOf(accInvAmount)  
      
      // - line level 
      recNumber = 0
      outTIVA = ""  //A 20211115
      List<DBContainer> ResultFGINLILine = listFGINLI(company, purchaseOrder, purchaseLine, purchaseSuffix, recNumber) 
      for (DBContainer InvLine : ResultFGINLILine){  
        // Accumulate amount   
        IVNA = InvLine.get("F5IVNA") 
        accInvAmount =+ IVNA 
      } 
    
      outTIVA = String.valueOf(accInvAmount) 
    
      // - header level 
      purchaseLine = 0
      purchaseSuffix = 0
      recNumber = 0
      outIVNA = ""  //A 20211115
      List<DBContainer> ResultFGINLIHead = listFGINLI(company, purchaseOrder, purchaseLine, purchaseSuffix, recNumber) 
        for (DBContainer InvLine : ResultFGINLIHead){  
          // Accumulate quantity  
          IVNA = InvLine.get("F5IVNA") 
          accInvAmount =+ IVNA 
        }
      
      outIVNA = String.valueOf(accInvAmount)    
     
      // Send Output parameter, for all received lines 
      setOutput()
      mi.write()   
      alreadySentOut = true 
    } 
   
    // Send Output parameter when no receiving lines exist, send the lines information
    if(!alreadySentOut){  
      setOutput()
      mi.write() 
    } 
  }  
} 
}  