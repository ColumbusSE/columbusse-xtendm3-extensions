//**************************************************************************** 
// New API - Update record in MGLINE with to-location (no standard API exist)
// A/N    Date    Version     Developer 
// C03    210104  1.0         Susanna Kellander, Columbus
//**************************************************************************** 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Update extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program; 
  private final MICallerAPI miCaller; 
  
  
  public Update(MIAPI mi, DatabaseAPI database,ProgramAPI program, MICallerAPI miCaller) {
     this.mi = mi;
     this.database = database; 
     this.program = program; 
     this.miCaller = miCaller;
  } 
    
    
  public void main() {  
  //Validate order and warehouse entered
  if(validateinput()) {
       mi.write()
       return
  } 
  // If OK, update record in MGLINE 
  UpdateRecord()
  }
 
  //***************************************************** 
  // validateinput - Validate order and warehouse entered
  //*****************************************************
   boolean validateinput(){  
   String company = mi.inData.get("CONO")  
   String ordernumber = mi.inData.get("TRNR") 
   String ordernumberline = mi.inData.get("PONR") 
   String ordernumberlinesuffix = mi.inData.get("POSX")   
   
   // Validare orderline in MGLINE/MMS101
   List<String> order = validateOrder(company, ordernumber, ordernumberline, ordernumberlinesuffix)  
     
   // Use Warehouse from orderline in MGLINE   
   order.each{ Warehouse ->  
   company = mi.inData.get("CONO") 
   String toLocation = mi.inData.get("TWSL")  
   // Validate to location in MITPCE/MMS010
   validateLocation(company, Warehouse, toLocation)   
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
   def void validateLocation(String company, String Warehouse, String toLocation){  
   // Validate location 
   def parameter = [CONO: company, WHLO: Warehouse, WHSL: toLocation] 
   List <String> result = []
   Closure<?> handler = {Map<String, String> response ->  
   if(response.containsKey("errorMsid")){ 
       mi.error("Error: "+response.errorMsid + " / " + response.errorMessage)  
   }
   }
   // Get the location via API
   miCaller.call("MMS010MI", "GetLocation", parameter, handler) 
   } 
    
    
    
   //********************************************** 
   // UpdateRecord - update record in MGLINE/MMS101
   //**********************************************
   void UpdateRecord(){ 
   int companyNum = mi.in.get("CONO") 
   int orderline = mi.in.get("PONR") 
   int orderlinesuffix = mi.in.get("POSX") 
     
   DBAction action = database.table("MGLINE").index("00").selectAllFields().build()
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
   String OldToLocation = lockedResult.get("MRTWSL") 
   lockedResult.set("MRREFE", OldToLocation)
   
   // Update the field if filled 
   if(mi.inData.get("TWSL") != ' '){
      lockedResult.set("MRTWSL", mi.inData.get("TWSL"))
   } 
        
   // Update changed data
   int changeddate=Integer.parseInt(formatDate)   
   lockedResult.set("MRLMDT", changeddate)  
   lockedResult.set("MRCHID", program.getUser())
   lockedResult.update()
   }
    
}