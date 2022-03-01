import groovy.json.JsonException
import groovy.json.JsonSlurper
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;


public class BlockCO_MFS610 extends ExtendM3Trigger {
  
  private final DatabaseAPI database;
  private final TransactionAPI transaction;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  //Definition
  public String Facility
  public String CustomerNumber
  public String OrderNumber
  public String Division
  public int Company
  public String User
  
 
  //******************************************************************** 
  // Get Division
  //******************************************************************** 
  private Optional<DBContainer> findDivision(Integer CONO, String FACI){   
    DBAction query = database.table("CFACIL").index("00").selectAllFields().build()
    def CFACIL = query.getContainer()
    CFACIL.set("CFCONO", CONO)
    CFACIL.set("CFFACI", FACI)
    if(query.read(CFACIL))  { 
      return Optional.of(CFACIL)
    } 
    
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get MFS610
  //******************************************************************** 
  private Optional<DBContainer> findMFS610(Integer CONO, String CUNO, String DIVI){   
    DBAction query = database.table("CCUDIV").index("00").selectAllFields().build()
    def CCUDIV = query.getContainer()
    CCUDIV.set("OKCONO", CONO)
    CCUDIV.set("OKCUNO", CUNO)
    CCUDIV.set("OKDIVI", DIVI)
    if(query.read(CCUDIV))  { 
      return Optional.of(CCUDIV)
    } 
    
    return Optional.empty()
  }
  
  
  public BlockCO_MFS610(DatabaseAPI database, TransactionAPI transaction, ProgramAPI program, LoggerAPI logger) {
    this.logger = logger;
    this.program = program;
    this.transaction = transaction;
    this.database = database;
    
  }
  
  public void main() {
    this.OrderNumber = transaction.parameters.ORNO;
    this.Company = this.program.LDAZD.CONO as Integer;
    GetFacility()
  }
  
  void GetFacility()  {
     DBAction action = database.table("OXCNTR").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
    
     ext.set("EVCONO", this.Company)
     ext.set("EVORNO",  this.OrderNumber)
     action.readAll(ext, 2, releasedItemProcessor) 
  }
  
    
    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      this.Facility = ext.get("EVFACI")
      this.CustomerNumber = ext.get("EVCUNO")
      
      // Get Division
      Optional<DBContainer> CFACIL = findDivision(this.Company, this.Facility)
      if(CFACIL.isPresent()){
        // Record found, continue to get information  
        DBContainer containerCFACIL = CFACIL.get()    
        this.Division = containerCFACIL.getString("CFDIVI")
        
        // Get Email address for Buyer
        Optional<DBContainer> CCUDIV = findMFS610(this.Company, this.CustomerNumber,  this.Division)
        if(!CCUDIV.isPresent()){
          // Record found, continue to get information  
          DBAction action = database.table("TCERRM").index("00").selectAllFields().build()
          DBContainer ext2 = action.createContainer()
          ext2.set("EVCONO", this.Company)
          ext2.set("EVDIVI", "ALL")
          ext2.set("EVID01", "")
          ext2.set("EVID02", "")
          ext2.set("EVID03", this.OrderNumber)
          ext2.set("EVID04", program.getUser())
          ext2.set("EVID05", "OIS100MI")
          ext2.set("EVMSID", "WVR0303")
          ext2.set("EVPGNM", "OIS271")
          //ext2.set("EVLEVL",  40)
          ext2.set("EVMDTA", "Local exception for customer/division does not exist")
          
          LocalDateTime now = LocalDateTime.now();    
          DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
          String formatDate = now.format(format1);    
          DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");  
          String formatTime = now.format(format2);        
        
          //Converting String into int using Integer.parseInt()
          int regdate=Integer.parseInt(formatDate); 
          int regtime=Integer.parseInt(formatTime); 
          
          ext2.set("EVRGDT", regdate)
          ext2.set("EVRGTM", regtime)
          ext2.set("EVRGNR", 1)
          action.insert(ext2)
        } 
        
      }
      
   }
  
   
}
