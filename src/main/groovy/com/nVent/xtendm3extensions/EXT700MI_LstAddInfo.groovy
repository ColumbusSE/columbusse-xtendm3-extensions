// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-06-21
// @version   1,0 
//
// Description 
// This API transacation LstAddInfo is used to send FAPIBA data to ESKAR from M3
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


public class LstAddInfo extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;  
  
  // Definition 
  public int Company  
  public int InCONO  
  public String InDIVI
  public String InINBN
  public int INBN
  public String InPEXN
  public int PEXN
  public String InPEXI
  public String InPEXS
  
    
  // Definition of output fields
  public String OutCONO 
  public String OutDIVI
  public String OutINBN  
  public String OutPEXN
  public String OutPEXI
  public String OutPEXS  


  
  // Constructor 
  public LstAddInfo(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company if not entered 
      int InCONO = getCONO()  
      
      InDIVI = mi.in.get("DIVI")  
      InINBN = mi.in.get("INBN")  
      INBN = mi.in.get("INBN") as Integer
      InPEXN = mi.in.get("PEXN") 

      if(isNullOrEmpty(InPEXN)){ 
        PEXN = 0
      }else{
        PEXN = mi.in.get("PEXN") as Integer
      } 

      // Start the listing in FAPIBA
      LstFAPIBARecord()
   
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
     
    mi.outData.put("CONO", OutCONO) 
    mi.outData.put("DIVI", OutDIVI)
    mi.outData.put("INBN", OutINBN)
    mi.outData.put("PEXN", OutPEXN)
    mi.outData.put("PEXI", OutPEXI)  
    mi.outData.put("PEXS", OutPEXS)  

  } 
    
    
  //******************************************************************** 
  // List all information for the Invoice Batch Number
  //********************************************************************  
   void LstFAPIBARecord(){   
     
     // List all Additional Info lines
     ExpressionFactory expression = database.getExpressionFactory("FAPIBA")
   
     // Depending on input value
     if(PEXN>0){
       expression = expression.eq("E7CONO", String.valueOf(CONO)).and(expression.eq("E7DIVI", InDIVI)).and(expression.eq("E7INBN", String.valueOf(INBN))).and(expression.eq("E7PEXN", String.valueOf(PEXN)))
     } else {
       expression = expression.eq("E7CONO", String.valueOf(CONO)).and(expression.eq("E7DIVI", InDIVI)).and(expression.eq("E7INBN", String.valueOf(INBN)))
     }
     
     // List Additional info lines 
     DBAction actionline = database.table("FAPIBA").index("00").matching(expression).selection("E7CONO", "E7DIVI", "E7INBN", "E7PEXN", "E7PEXI", "E7PEXS").build()  

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("E7CONO", CONO) 
     actionline.readAll(line, 1, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List Additional Information - main loop - FAPIBA
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Output selectAllFields 
  OutCONO = String.valueOf(line.get("E7CONO")) 
  OutDIVI = String.valueOf(line.get("E7DIVI"))  
  OutINBN = String.valueOf(line.get("E7INBN"))  
  OutPEXN = String.valueOf(line.get("E7PEXN"))
  OutPEXI = String.valueOf(line.get("E7PEXI"))
  OutPEXS = String.valueOf(line.get("E7PEXS"))
    

  // Send Output
  SetOutPut()
  mi.write() 
} 
}
 