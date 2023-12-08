
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-08
// @version   1.0 
//
// Description 
// This API transacation LstPOData is used to send PO data to ESKAR from M3 
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math


public class LstPOData extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger  
    private final MICallerAPI miCaller
    
    // Definition 
    public Integer company  
    public String inCONO
    public String inPUNO 
    public int inPNLI 
    public int inPNLS 
    public String division
    public String supplier
    public String buyer 
    public double lineORQA 
    public double lineIVQA 
    public double lineRVQA   
    public double lineLNAM
    public double invLineIVNA 
    public String linePUUN 
    public double discount1 
    public double discount2  
    public double discount3 
    public double confirmedDiscount1 
    public double confirmedDiscount2 
    public double confirmedDiscount3 
    public double confirmedPrice  
    public double totalConfirmedDiscount
    public double totalDiscount
    public double purchasePrice
    public double linePrice
    public double lineAmount  
    public double invLineIVQT  
    public double accInvQty  
    public double accInvAmount 
    public double calcCOFA1   
    public double calcDMCF1   
    public double calcCOFA2   
    public double calcDMCF2 
    public double calcFACP
    public double calcFACO
    public double calcFACT
    public String linePPUN  
    public double invLineRPQA
    public double unitPrice
      
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
    public String outPUDT 
    public String outEMAL
    public String outUNPR
    public String outDEAL 
    public String outIVQT
    public String outTIVA
    public String outVTCD
    
    
    // Constructor 
    public LstPOData(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
       this.mi = mi
       this.database = database 
       this.program = program
       this.logger = logger 
       this.miCaller = miCaller
    } 
   
  
    //******************************************************************** 
    // Main 
    //********************************************************************  
    public void main() { 
      
      // Validate company
      company = mi.in.get("CONO")      
      if (company == null) {
        company = program.LDAZD.CONO as Integer
      } 
      inCONO = String.valueOf(company)
      inPUNO = mi.in.get("PUNO")
      inPNLI = mi.in.get("PNLI")
      inPNLS = mi.in.get("PNLS")
      
  
      clearOutput()
      lineORQA = 0d
      lineIVQA = 0d
      lineRVQA = 0d
      linePUUN = ""
      linePPUN = ""
      lineLNAM = 0d
      discount1 = 0d
      discount2 = 0d
      discount3 = 0d
      totalDiscount = 0d
      confirmedDiscount1 = 0d
      confirmedDiscount2 = 0d
      confirmedDiscount3 = 0d
      totalConfirmedDiscount = 0d
      confirmedPrice = 0d
      purchasePrice = 0d
      linePrice = 0d
      lineAmount = 0d
      unitPrice = 0d
  
      // Get Purchase order line
      Optional<DBContainer> MPLINE = findMPLINE(company, inPUNO, inPNLI, inPNLS)
      if (MPLINE.isPresent()) {
        // Record found, continue to get information  
        DBContainer containerMPLINE = MPLINE.get() 
  
        // Output selectAllFields 
        outPUNO = inPUNO
        outLPUD = containerMPLINE.get("IBLPUD")
        outPNLI = containerMPLINE.get("IBPNLI")
        outPNLS = containerMPLINE.get("IBPNLS") 
        outITNO = containerMPLINE.get("IBITNO")
        outPITD = containerMPLINE.get("IBPITD")
        outVTCD = containerMPLINE.get("IBVTCD")
        
        // Fields for calculation
        lineORQA = containerMPLINE.get("IBCFQA")
        if (lineORQA > 0) {
        } else {
           lineORQA = containerMPLINE.get("IBORQA")
        }
        lineIVQA = containerMPLINE.get("IBIVQA")
        lineRVQA = containerMPLINE.get("IBRVQA")
        linePUUN = containerMPLINE.get("IBPUUN")
        linePPUN = containerMPLINE.get("IBPPUN")
        lineLNAM = containerMPLINE.get("IBLNAM")
        
        discount1 = containerMPLINE.get("IBODI1")
        discount2 = containerMPLINE.get("IBODI2")
        discount3 = containerMPLINE.get("IBODI3")
        confirmedDiscount1 = containerMPLINE.get("IBCFD1")
        confirmedDiscount2 = containerMPLINE.get("IBCFD2")
        confirmedDiscount3 = containerMPLINE.get("IBCFD3")
        confirmedPrice = containerMPLINE.get("IBCPPR")
        purchasePrice = containerMPLINE.get("IBPUPR")
  
        //Calc the price
        linePrice = calcPrice(company, discount1, discount2, discount3, confirmedDiscount1, confirmedDiscount2, confirmedDiscount3, confirmedPrice, purchasePrice)
        
        lineAmount = linePrice * lineORQA
  
        //Get UoM factors
        getUoMFactors(company, outITNO, linePPUN, linePUUN)
          
  	    outORQT = lineORQA
  	    outRVQT = lineRVQA
  
        unitPrice = linePrice * calcFACT
        BigDecimal unitPriceRounded  = BigDecimal.valueOf(unitPrice) 
        unitPriceRounded = unitPriceRounded.setScale(4, RoundingMode.HALF_UP) 
        unitPrice = unitPriceRounded  
  
        outUNPR = String.valueOf(unitPrice) 
  
        //L Ordered Amount
        double lineOrderedAmount = lineORQA * (linePrice * calcFACT)
        BigDecimal lineOrderedAmountRounded  = BigDecimal.valueOf(lineOrderedAmount) 
        lineOrderedAmountRounded = lineOrderedAmountRounded.setScale(2, RoundingMode.HALF_UP) 
        lineOrderedAmount = lineOrderedAmountRounded  
        outLNAM = lineOrderedAmount
  
        //L Delivered Amount
        double lineDeliveredAmount = lineRVQA * (linePrice * calcFACT)
        BigDecimal lineDeliveredAmountRounded  = BigDecimal.valueOf(lineDeliveredAmount) 
        lineDeliveredAmountRounded = lineDeliveredAmountRounded.setScale(2, RoundingMode.HALF_UP) 
        lineDeliveredAmount = lineDeliveredAmountRounded  
        outDEAL = lineDeliveredAmount
  
        
        // Get Purchase order head
        Optional<DBContainer> MPHEAD = findMPHEAD(company, inPUNO)
        if(MPHEAD.isPresent()){
           // Record found, continue to get information  
          DBContainer containerMPHEAD = MPHEAD.get()  
          
          // output fields   
          outCONO = String.valueOf(containerMPHEAD.get("IACONO"))
          outDIVI = containerMPHEAD.getString("IADIVI")
          outPUNO = containerMPHEAD.getString("IAPUNO")
          outSUNO = containerMPHEAD.getString("IASUNO")   
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
        
        }
  
  
        // Get information from receiving invoice lines - sum per PO line level
        outTIVA = ""  
        outIVQT = ""
        invLineIVNA = 0d 
        accInvQty = 0d 
        invLineRPQA = 0d
        accInvAmount = 0d 
        invLineIVQT = 0d
        List<DBContainer> ResultFGINLIline = listFGINLIline(company, inPUNO, inPNLI, inPNLS) 
        for (DBContainer InvLineLine : ResultFGINLIline){
  	        invLineIVQT = InvLineLine.get("F5IVQA") 
            invLineIVNA = InvLineLine.get("F5IVNA") 
            accInvQty = accInvQty + invLineIVQT 
            accInvAmount = accInvAmount + invLineIVNA 
        }
        outTIVA = String.valueOf(accInvAmount) 
        outIVQT = String.valueOf(accInvQty)
  
      }
  
       // Output   
       setOutput()
       mi.write()   
  
    }
  
    
    //*******************************************************************************************
    // Calc the price
    //********************************************************************************************
    private double calcPrice(int CONO, double ODI1, double ODI2, double ODI3, double CFD1, double CFD2, double CFD3, double CPPR, double PUPR) { 
        totalDiscount = ODI1 + ODI2 + ODI3
        totalConfirmedDiscount = CFD1 + CFD2 + CFD3
  
        if (CPPR > 0) {
           linePrice = (CPPR * (100 - totalConfirmedDiscount))/100
        } else {
           linePrice = (PUPR * (100 - totalDiscount))/100
        }
        
        return linePrice
    }
    
    
    //**************************************************************************************** 
    // Get and set Unit of Measure Factors 
    //*****************************************************************************************
    void getUoMFactors(int CONO, String ITNO, String PPUN, String PUUN) {
  
        calcFACP = 0d
        calcFACO = 0d
        calcFACT = 0d
        
        // Calculate with Unit of measure factor   
        // Get COFA and DMCF from PPUN Price UoM            
        Optional<DBContainer> MITAUN1 = findMITAUN(company, outITNO, 2, linePPUN)
        if(MITAUN1.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMITAUN1 = MITAUN1.get() 
          calcCOFA1 = containerMITAUN1.get("MUCOFA")
          calcDMCF1 = containerMITAUN1.get("MUDMCF") 
        } else { 
          calcCOFA1 = 1
          calcDMCF1 = 1
        }
        if (calcDMCF1 == 1) {
          calcFACP = 1/calcCOFA1
        } else {
          calcFACP = calcCOFA1
        }
    
        // Get COFA and DMCF from PUUN Qty UoM           
        Optional<DBContainer> MITAUN2 = findMITAUN(company, outITNO, 2, linePUUN)
        if(MITAUN2.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMITAUN2 = MITAUN2.get() 
            calcCOFA2 = containerMITAUN2.get("MUCOFA")
            calcDMCF2 = containerMITAUN2.get("MUDMCF") 
          } else { 
            calcCOFA2 = 1
            calcDMCF2 = 1
          }
          if (calcDMCF2 == 1) {
            calcFACO = calcCOFA2
          } else {
            calcFACO = 1/calcCOFA2
          }
  
          calcFACT = calcFACP * calcFACO
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
    // Get Division information MPLINE
    //******************************************************************** 
    private Optional<DBContainer> findMPLINE(Integer CONO, String PUNO, Integer PNLI, Integer PNLS){  
      DBAction query = database.table("MPLINE").index("00").selection("IBPUPR", "IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBITNO", "IBLPUD", "IBLNAM", "IBPITD", "IBVTCD", "IBORQA", "IBIVQA", "IBRVQA", "IBPUUN", "IBPPUN", "IBODI1", "IBODI2", "IBODI3", "IBCFD1", "IBCFD2", "IBCFD3", "IBCPPR").build()    
      def MPLINE = query.getContainer()
      MPLINE.set("IBCONO", CONO)
      MPLINE.set("IBPUNO", PUNO)
      MPLINE.set("IBPNLI", PNLI)
      MPLINE.set("IBPNLS", PNLS)
      if(query.read(MPLINE))  { 
        return Optional.of(MPLINE)
      } 
    
      return Optional.empty()
    } 
  
  
  
    //******************************************************************** 
    // Get information from MPHEAD
    //******************************************************************** 
    private Optional<DBContainer> findMPHEAD(Integer CONO, String PUNO){  
      DBAction query = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI", "IASUNO", "IANTAM", "IAPUDT", "IABUYE", "IACOAM").build()    
      def MPHEAD = query.getContainer()
      MPHEAD.set("IACONO", CONO)
      MPHEAD.set("IAPUNO", PUNO)
      if(query.read(MPHEAD))  { 
        return Optional.of(MPHEAD)
      } 
    
      return Optional.empty()
    } 
    
      
  
    //******************************************************************** 
    // Accumulate value from FGINLI - PO line level
    //******************************************************************** 
    private List<DBContainer> listFGINLIline(Integer CONO, String PUNO, Integer PNLI, Integer PNLS){ 
      List<DBContainer>InvLine = new ArrayList() 
      ExpressionFactory expression = database.getExpressionFactory("FGINLI")
      DBAction query = database.table("FGINLI").index("20").selectAllFields().build() 
      DBContainer FGINLIline = query.getContainer() 
      FGINLIline.set("F5CONO", CONO)   
      FGINLIline.set("F5PUNO", PUNO) 
      FGINLIline.set("F5PNLI", PNLI) 
      FGINLIline.set("F5PNLS", PNLS) 
  
      int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      query.readAll(FGINLIline, 3, pageSize, { DBContainer record ->  
       InvLine.add(record) 
      })
  
      return InvLine
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
    // Clear Output data
    //******************************************************************** 
    void clearOutput() {
      outCONO = ""
      outDIVI = ""
      outPUNO = ""
      outSUNO = ""
      outLPUD = ""
      outPNLI = ""  
      outPNLS = "" 
      outITNO = "" 
      outLNAM = ""
      outPITD = "" 
      outORQT = ""
      outIVQT = ""
      outRVQT = ""    
      outPUDT = ""
      outEMAL = "" 
      outUNPR = ""
      outDEAL = ""
      outTIVA = ""
      outVTCD = ""   
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
      mi.outData.put("PUDT", outPUDT) 
      mi.outData.put("EMAL", outEMAL) 
      mi.outData.put("UNPR", outUNPR)
      mi.outData.put("DEAL", outDEAL) 
      mi.outData.put("TIVA", outTIVA)
      mi.outData.put("VTCD", outVTCD)    
    } 
    
}  