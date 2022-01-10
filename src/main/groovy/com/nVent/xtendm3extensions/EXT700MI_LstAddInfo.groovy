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
  public int company  
  public int inCONO  
  public String inDIVI
  public String inINBN
  public int INBN
  public String inPEXN
  public int PEXN

    
  // Definition of output fields
  public String outCONO 
  public String outDIVI
  public String outINBN  
  public String outPEXN
  public String outPEXI
  public String outPEXS  


  
  // Constructor 
  public LstAddInfo(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger; 
  } 

     
  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company if not entered 
      int inCONO = getCONO()  
      
      inDIVI = mi.in.get("DIVI")  
      inINBN = mi.in.get("INBN")  
      INBN = mi.in.get("INBN") as Integer
      inPEXN = mi.in.get("PEXN") 

      if(isNullOrEmpty(inPEXN)){ 
        PEXN = 0
      }else{
        PEXN = mi.in.get("PEXN") as Integer
      } 

      // Start the listing in FAPIBA
      lstFAPIBARecord()
   
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
  void setOutPut() {
     
    mi.outData.put("CONO", outCONO) 
    mi.outData.put("DIVI", outDIVI)
    mi.outData.put("INBN", outINBN)
    mi.outData.put("PEXN", outPEXN)
    mi.outData.put("PEXI", outPEXI)  
    mi.outData.put("PEXS", outPEXS)  

  } 
    
    
  //******************************************************************** 
  // List all information for the Invoice Batch Number
  //********************************************************************  
   void lstFAPIBARecord(){   
     
     // List all Additional Info lines
     ExpressionFactory expression = database.getExpressionFactory("FAPIBA")
   
     // Depending on input value
     /*if(PEXN>0){
       expression = expression.eq("E7CONO", String.valueOf(CONO)).and(expression.eq("E7DIVI", inDIVI)).and(expression.eq("E7INBN", String.valueOf(INBN))).and(expression.eq("E7PEXN", String.valueOf(PEXN)))
     } else {
       expression = expression.eq("E7CONO", String.valueOf(CONO)).and(expression.eq("E7DIVI", inDIVI)).and(expression.eq("E7INBN", String.valueOf(INBN)))
     }*/
     
     // List Additional info lines 
     DBAction actionline = database.table("FAPIBA").index("00").matching(expression).selection("E7CONO", "E7DIVI", "E7INBN", "E7PEXN", "E7PEXI", "E7PEXS").build()  

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("E7CONO", CONO) 
	   line.set("E7DIVI", inDIVI)                      //A 20220109
	   line.set("E7INBN", INBN)                        //A 20220109
	   line.set("E7PEXN", PEXN)                        //A 20220109
	 
	   if (PEXN>0) {                                    //A 20220109
		   actionline.readAll(line, 4, releasedLineProcessor)     //A 20220109
	   } else {                                                                     //A 20220109
	     actionline.readAll(line, 3, releasedLineProcessor)      //A 20220109
	   }                                                                              //A 20220109
	   
	   //actionline.readAll(line, 1, releasedLineProcessor)     //D 20220109
   
   } 

    
  //******************************************************************** 
  // List Additional Information - main loop - FAPIBA
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Output selectAllFields 
  outCONO = String.valueOf(line.get("E7CONO")) 
  outDIVI = String.valueOf(line.get("E7DIVI"))  
  outINBN = String.valueOf(line.get("E7INBN"))  
  outPEXN = String.valueOf(line.get("E7PEXN"))
  outPEXI = String.valueOf(line.get("E7PEXI"))
  outPEXS = String.valueOf(line.get("E7PEXS"))
    

  // Send Output
  setOutPut()
  mi.write() 
} 
}
 