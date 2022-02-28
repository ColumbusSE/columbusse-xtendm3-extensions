//**************************************************************************** 
// New API - Update record in MGLINE with to-location and transaction qty 
// A/N    Date    Version     Developer 
// C03    210104  1.0         Susanna Kellander, Columbus
// C03    211123  2.0         Jessica Bjorklund, Columbus   Added TRQT
//**************************************************************************** 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Update extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program; 
  private final MICallerAPI miCaller; 
  private final LoggerAPI logger;  

  public double TRQT
  public String inTRQT
  public String inTWSL
  public boolean updateQuantity
  
  public Update(MIAPI mi, DatabaseAPI database,ProgramAPI program, MICallerAPI miCaller, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program; 
     this.miCaller = miCaller;
     this.logger = logger; 
  } 
    
    
  public void main() {  
    //Validate order and warehouse entered
    if(validateinput()) {
         mi.write()
         return
    } 
    // If OK, update record in MGLINE 
    updateRecord()
  }
 
  //***************************************************** 
  // validateinput - Validate order and warehouse entered
  //*****************************************************
   public boolean validateinput(){  
       String company = mi.inData.get("CONO")  
       String ordernumber = mi.inData.get("TRNR") 
       String ordernumberline = mi.inData.get("PONR") 
       String ordernumberlinesuffix = mi.inData.get("POSX")   
       
       // Validare orderline in MGLINE/MMS101
       List<String> order = validateOrder(company, ordernumber, ordernumberline, ordernumberlinesuffix)  
         
       // Use Warehouse from orderline in MGLINE   
       order.each{ warehouse ->  
       company = mi.inData.get("CONO") 
       inTWSL = mi.inData.get("TWSL") 
       updateQuantity = false
    
       inTRQT = mi.in.get("TRQT")  
       if(isNullOrEmpty(inTRQT)){ 
         TRQT = 0
       }else{
         TRQT = mi.in.get("TRQT")
         updateQuantity = true
       } 
    
       // Validate to location in MITPCE/MMS010
       validateLocation(company, warehouse, inTWSL) 
       } 
       // Ok, process continues
       return false
   }
   
   //***************************************************************************** 
   // validateLocation - Validare orderline in MGLINE/MMS101 and get its warehouse
   // Input 
   // Company - from API
   // ordernumber - from API
   // ordernumberline - from API
   // ordernumberlinesuffix - from API 
   //***************************************************************************** 
   def List<String> validateOrder(String company, String ordernumber, String ordernumberline, String ordernumberlinesuffix){   
     def parameter = [CONO: company, TRNR: ordernumber, PONR: ordernumberline, POSX: ordernumberlinesuffix] 
     List<String> result = []
     Closure<?> handler = {Map<String, String> response ->  
     if(response.containsKey('errorMsid')){
          mi.error("Error: "+response.errorMsid + " / " + response.errorMessage)  
     } 
     // Save the warehouse from MGLINE/MMS101
     result.push(response.WHLO)
     }
     // Validate the orderline via API
     miCaller.call("MMS100MI", "GetLine", parameter, handler)  
     return result
   } 
    
    
    
   //********************************************** 
   // validateLocation - validate location 
   // Input 
   // Company - from API
   // Warehouse - from MGLINE/MMS101
   // toLocation - from API
   //**********************************************
   def void validateLocation(String company, String warehouse, String toLocation){  
   // Validate location 
   def parameter = [CONO: company, WHLO: warehouse, WHSL: toLocation] 
   List <String> result = []
   Closure<?> handler = {Map<String, String> response ->  
   if(response.containsKey("errorMsid")){ 
       mi.error("Error: "+response.errorMsid + " / " + response.errorMessage)  
   }
   }
   // Get the location via API
   miCaller.call("MMS010MI", "GetLocation", parameter, handler) 
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
   
   //********************************************** 
   // UpdateRecord - update record in MGLINE/MMS101
   //**********************************************
   void updateRecord(){ 
     int companyNum = mi.in.get("CONO") 
     int orderline = mi.in.get("PONR") 
     int orderlinesuffix = mi.in.get("POSX") 
       
     DBAction action = database.table("MGLINE").index("00").selection("MRCONO", "MRTRNR", "MRPONR", "MRPOSX", "MRREFE", "MRTWSL", "MRALUN", "MRLMDT", "MRCHNO", "MRCHID").build()         //A 20210604
     DBContainer ext = action.getContainer() 
       
     ext.set("MRCONO", companyNum)
     ext.set("MRTRNR", mi.in.get("TRNR"))
     ext.set("MRPONR", orderline)
     ext.set("MRPOSX", orderlinesuffix) 
     
     // Read with lock
     action.readLock(ext, updateCallBack)  
     } 
      
     Closure<?> updateCallBack = { LockedResult lockedResult -> 
     // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1); 
     
     // Update Change Number
     int ChangeNo = lockedResult.get("MRCHNO") 
     int newChangeNo = ChangeNo + 1 
     lockedResult.set("MRCHNO", newChangeNo)  
     
     // Save old To location in field REFE
     String oldToLocation = lockedResult.get("MRTWSL") 
     String trimmedOldToLocation = oldToLocation.trim()
     String newToLocation = mi.inData.get("TWSL")
     String trimmedNewToLocation = newToLocation.trim()
     String compareLocation = "HBTEMP"
     
     // Update the field if filled 
     if(mi.inData.get("TWSL") != ' '){  //D 20211123
        if(trimmedOldToLocation.equals(compareLocation)){  //D 20211123
        } else {
          lockedResult.set("MRTWSL", mi.inData.get("TWSL"))
          lockedResult.set("MRREFE", oldToLocation)
        }
     } 
     
     int CONO = lockedResult.get("MRCONO")  //A 20211123     
     String ALUN = lockedResult.get("MRALUN")  //A 20211123
     String ITNO = lockedResult.get("MRITNO")  //A 20211123
     double transactionQtyAltUnit = 0          //A 20211123
     double COFA = 0                           //A 20211123
     int DMCF = 0                              //A 20211123
  
     //Update transaction qty if entered   //A 20211123
     if(updateQuantity){ 
        lockedResult.set("MRTRQT", TRQT)                //A 20211123
       
        // Calculate with alternativ unit   //A 20211123
        Optional<DBContainer> MITAUN = findMITAUN(CONO, ITNO, 1, ALUN)  //A 20211123
        if(MITAUN.isPresent()){                         //A 20211123
          // Record found, continue to get information  //A 20211123
          DBContainer containerMITAUN = MITAUN.get()    //A 20211123
          COFA = containerMITAUN.get("MUCOFA")          //A 20211123
          DMCF = containerMITAUN.get("MUDMCF")          //A 20211123
          if(DMCF == 1){                                //A 20211123
            if(COFA != 0){                              //A 20211123
              transactionQtyAltUnit = TRQT / COFA       //A 20211123
            }                                           //A 20211123
          }else {                                       //A 20211123
            if(COFA != 0){                              //A 20211123
              transactionQtyAltUnit = TRQT * COFA       //A 20211123
            }                                           //A 20211123
          }                                             //A 20211123
        }                                               //A 20211123
        lockedResult.set("MRTRQA", transactionQtyAltUnit)  //A 20211123
     }                                                     //A 20211123
     
     // Update changed data
     int changeddate=Integer.parseInt(formatDate)   
     lockedResult.set("MRLMDT", changeddate)  
     lockedResult.set("MRCHID", program.getUser())
     lockedResult.update()
     }
    
}