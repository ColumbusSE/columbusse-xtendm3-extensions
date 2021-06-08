import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;
  
 public class UpdMODF extends ExtendM3Transaction {
  private final MIAPI mi;
  private final ProgramAPI program;    
  private final DatabaseAPI database; 
  private final MICallerAPI miCaller; 
  private final LoggerAPI logger; 
  
  public UpdMODF(MIAPI mi, ProgramAPI program, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger) {
    this.mi = mi;
    this.program = program;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
  }
  
  public void main() {
    if(validateInput()) {
       mi.write()
       return
    } 
    UpdRecord() 
  }
  
  boolean validateInput(){
    String company = mi.in.get("CONO") 
    if(validateCompany(company )){  
       mi.error("Company " + company + " is invalid") 
       return true
    }
    String modf = mi.in.get("MODF")
    int cono = mi.in.get("CONO")
    if(!validateModf(cono, modf )){  
       mi.error("Mode of delivery " + modf + " is invalid") 
       return true
    }
    return false
  }
  
  boolean validateCompany(String company){  
    // Run MI program 
    def parameter = [CONO: company] 
    List <String> result = []
    Closure<?> handler = {Map<String, String> response -> 
         return response.CONO == 0} 
     miCaller.call("MNS095MI", "Get", parameter, handler)
  }
  
  boolean validateModf(int cono, String modf){  
     DBAction action = database.table("CSYTAB").index("00").selectAllFields().build()
     DBContainer sytab = action.createContainer()
     
     sytab.set("CTCONO", cono)
     sytab.set("CTSTCO", "MODL")
     sytab.set("CTSTKY", mi.in.get("MODF")) 
     sytab.set("CTLNCD", ' ')
     if (action.read(sytab)) {
       return true
     }
     return false
  }
  
  void UpdRecord(){
     int inbound = 1;
     int outbound = 2;
     int company = mi.in.get("CONO")
     long delivery =  mi.in.get("DLIX")
     String finalMode = mi.in.get("MODF")
     
     DBAction action = database.table("MHDISH").index("00").selectAllFields().build()
     DBContainer hdish = action.getContainer()
      
     
     hdish.set("OQCONO", company)
     hdish.set("OQINOU", inbound)
     hdish.set("OQDLIX", delivery)
     if (!action.read(hdish)) {
         hdish.set("OQINOU", outbound)
         action.read(hdish)
     }
     
     // Read with lock
     action.readLock(hdish, updateCallBack)
  }
  
   Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("OQCHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the fields if filled
     
     lockedResult.set("OQMODF", mi.in.get("MODF"))  
     
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("OQLMDT", changeddate)  
      
     lockedResult.set("OQCHNO", newChangeNo) 
     lockedResult.set("OQCHID", program.getUser())
     lockedResult.update()
     }
 }