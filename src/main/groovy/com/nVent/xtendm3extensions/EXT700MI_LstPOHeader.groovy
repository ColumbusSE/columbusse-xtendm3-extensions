
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-08
// @version   1.0 
//
// Description 
// This API transacation LstPOHeader is used to send header level PO data to ESKAR from M3 
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math


public class LstPOHeader extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  private final MICallerAPI miCaller
  
  // Definition 
  public Integer company  
  public String inCONO
  public String inPUNO 
  public String inPNLI 
  public String inPNLS 
  public String division
  public String supplier
  public double orderedAmount
  public String orderedAmountString
  public double sumOrderedAmount
  public double deliveredAmount
  public String deliveredAmountString
  public double sumDeliveredAmount
  public double invoicedAmount
  public String invoicedAmountString
  public double sumInvoicedAmount
  public double sumOrderedPerLine
  public double sumDeliveredPerLine
  public double sumInvoicedPerLine
  public double ordAmt
  public double delAmt
  public double invAmt

    
  // Definition of output fields
  public String outPNLI  
  public String outPNLS  
  public String outCONO 
  public String outPUNO
  public String outSUNO   
  public String outNTAM  
  public String outDEAH
  public String outIVNA

  
  // Constructor 
  public LstPOHeader(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
    
    clearOutput()

    //List PO to sum line info
    List<DBContainer> ResultMPLINE = listMPLINE(company, inPUNO) 
    for (DBContainer RecLineMPLINE : ResultMPLINE){ 
        inPNLI = RecLineMPLINE.get("IBPNLI")
        inPNLS = RecLineMPLINE.get("IBPNLS")

        orderedAmountString = ""
        deliveredAmountString = ""
        invoicedAmountString = ""
        sumOrderedPerLine = 0d
        sumDeliveredPerLine = 0d
        sumInvoicedPerLine = 0d
        ordAmt = 0d
        delAmt = 0d
        invAmt = 0d
        
        lstPOInfoMI(inCONO, inPUNO, String.valueOf(inPNLI), String.valueOf(inPNLS))
        
        orderedAmount = Double.valueOf(orderedAmountString)
        deliveredAmount = Double.valueOf(deliveredAmountString)
        invoicedAmount = Double.valueOf(invoicedAmountString)

        sumOrderedAmount = sumOrderedAmount + orderedAmount
        sumDeliveredAmount = sumDeliveredAmount + deliveredAmount
        sumInvoicedAmount = sumInvoicedAmount + invoicedAmount
        
        outPUNO = inPUNO
        outNTAM = sumOrderedAmount
        outDEAH = sumDeliveredAmount
        outIVNA = sumInvoicedAmount
    }
    
    setOutput()
  } 


   //***************************************************************************** 
   // Get line info for calculation
   //***************************************************************************** 
   void lstPOInfoMI(String company, String purchaseOrder, String lineNumber, String lineSuffix){   
        def params = [CONO: company, PUNO: purchaseOrder, PNLI: lineNumber, PNLS: lineSuffix] 
        def callback = {
          Map<String, String> response ->
          if(response.LNAM != null){
             orderedAmountString = response.LNAM 
             ordAmt = Double.valueOf(orderedAmountString)
             deliveredAmountString = response.DEAL
             delAmt = Double.valueOf(deliveredAmountString)
             invoicedAmountString = response.TIVA
             invAmt = Double.valueOf(invoicedAmountString)
          }
          sumOrderedPerLine = sumOrderedPerLine + ordAmt
          sumDeliveredPerLine = sumDeliveredPerLine + delAmt
          sumInvoicedPerLine = sumInvoicedPerLine + invAmt
        }
        
        miCaller.call("EXT700MI","LstPOData", params, callback)
   } 
  
  
  
  //******************************************************************** 
  // Read all lines for entered PO
  //********************************************************************  
  private List<DBContainer> listMPLINE(int CONO, String PUNO){
      List<DBContainer>RecLineMPLINE = new ArrayList() 
      ExpressionFactory expression = database.getExpressionFactory("MPLINE")
      expression = expression.eq("IBCONO", String.valueOf(CONO)).and(expression.eq("IBPUNO", PUNO))
      
      def query = database.table("MPLINE").index("00").matching(expression).selection("IBPNLI", "IBPNLS").build()
      def MPLINE = query.createContainer()
      MPLINE.set("IBCONO", CONO)
      
      int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()    
      query.readAll(MPLINE, 1, pageSize, { DBContainer recordMPLINE ->  
         RecLineMPLINE.add(recordMPLINE.createCopy()) 
      })
  
      return RecLineMPLINE
  }
  

 
  //******************************************************************** 
  // Clear Output data
  //******************************************************************** 
  void clearOutput() {
      outCONO = ""
      outPUNO = ""
      outNTAM = ""
      outDEAH = ""
      outIVNA = ""
  }
  
  
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
      mi.outData.put("CONO", inCONO) 
      mi.outData.put("PUNO", inPUNO)
      mi.outData.put("NTAM", outNTAM)  
      mi.outData.put("DEAH", outDEAH)
      mi.outData.put("IVNA", outIVNA) 
  } 
    
}  