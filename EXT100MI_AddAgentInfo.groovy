// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-19
// @version   1.0 
//
// Description 
// This API is used to add an agent information to OOHEAC
// Transaction AddAgentInfo
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230419  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class AddAgentInfo extends ExtendM3Transaction {
	private final MIAPI mi
	private final ProgramAPI program
	private final DatabaseAPI database
	private final LoggerAPI logger
	
	public Integer CONO
	public String inORNO
	public String inAGN2
  public String inAGN3
  public String inAGN4
  public String inAGN5
  public String inAGN6
  public String inAGN7


  public AddAgentInfo(MIAPI mi, ProgramAPI program, DatabaseAPI database, LoggerAPI logger) {
		this.mi = mi
		this.program = program
		this.database = database
		this.logger = logger
  }
  
  public void main() { 
     // Validate company
     CONO = mi.in.get("CONO")      
     if (CONO == null || CONO == 0) {
        CONO = program.LDAZD.CONO as Integer
     } 
     Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
     if(!CMNCMP.isPresent()){                         
       mi.error("Company " + CONO + " is invalid")   
       return                                         
     } 
     
     inORNO= mi.in.get("ORNO") 
     if (inORNO == null || inORNO == "") {
        mi.error("Order Number must be entered")   
        return                                         
     }
     Optional<DBContainer> OOHEAD = findOOHEAD(CONO, inORNO)        
     if (!OOHEAD.isPresent()) {                                           
        //If no record found, return error message 
        mi.error("Order Number " + inORNO + " is invalid")   
        return
     } 
 
     inAGN2 = mi.in.get("AGN2")  
     if (inAGN2 == null || inAGN2 == "") {
          mi.error("Agent 2 must be entered")   
          return                                         
     }
     Optional<DBContainer> OCUSMAAgent2 = findOCUSMA(CONO, inAGN2)  
     if(!OCUSMAAgent2.isPresent()){                         
       mi.error("Agent 2 " + inAGN2 + " is invalid")   
       return                                         
     } 
     
     inAGN3 = mi.in.get("AGN3") 
     if (inAGN3 != null && inAGN3 != "") {
       Optional<DBContainer> OCUSMAAgent3 = findOCUSMA(CONO, inAGN3)  
       if(!OCUSMAAgent3.isPresent()){                         
         mi.error("Agent 3 " + inAGN3 + " is invalid")   
         return                                         
       }  
     }

     inAGN4 = mi.in.get("AGN4")   
     if (inAGN4 != null && inAGN4 != "") {
       Optional<DBContainer> OCUSMAAgent4 = findOCUSMA(CONO, inAGN4)  
       if(!OCUSMAAgent4.isPresent()){                         
         mi.error("Agent 4 " + inAGN4 + " is invalid")   
         return                                         
       }   
     }

     inAGN5 = mi.in.get("AGN5")
     if (inAGN5 != null && inAGN5 != "") {
       Optional<DBContainer> OCUSMAAgent5 = findOCUSMA(CONO, inAGN5)  
       if(!OCUSMAAgent5.isPresent()){                         
         mi.error("Agent 5 " + inAGN5 + " is invalid")   
         return                                         
       }  
     }

     inAGN6 = mi.in.get("AGN6") 
     if (inAGN6 != null && inAGN6 != "") {
       Optional<DBContainer> OCUSMAAgent6 = findOCUSMA(CONO, inAGN6)  
       if(!OCUSMAAgent6.isPresent()){                         
         mi.error("Agent 6 " + inAGN6 + " is invalid")   
         return                                         
       }  
     }

     inAGN7 = mi.in.get("AGN7")  
     if (inAGN7 != null && inAGN7 != "") {
       Optional<DBContainer> OCUSMAAgent7 = findOCUSMA(CONO, inAGN7)  
       if(!OCUSMAAgent7.isPresent()){                         
         mi.error("Agent 7 " + inAGN7 + " is invalid")   
         return                                         
       }   
     }

     //Check if Agent Information record exists                                                    
     Optional<DBContainer> OOHEAC = findOOHEAC(CONO, inORNO)        
     if (OOHEAC.isPresent()) {                                           
        //If record found, return error message 
        mi.error("Agent record already exists")   
        return
     } else {
        // Write record 
        addRecord(CONO, inORNO, inAGN2, inAGN3, inAGN4, inAGN5, inAGN6, inAGN7)    
     }  
  }
  
  //******************************************************************** 
  // Get Company record
  //******************************************************************** 
  private Optional<DBContainer> findCMNCMP(Integer CONO){                             
      DBAction query = database.table("CMNCMP").index("00").selection("JICONO").build()   
      DBContainer CMNCMP = query.getContainer()                                           
      CMNCMP.set("JICONO", CONO)                                                         
      if(query.read(CMNCMP))  {                                                           
        return Optional.of(CMNCMP)                                                        
      }                                                                                  
      return Optional.empty()                                                            
  } 
  
  //******************************************************************** 
  // Check Agent
  //******************************************************************** 
  private Optional<DBContainer> findOCUSMA(int CONO, String AGNT){  
    DBAction query = database.table("OCUSMA").index("00").selection("OKCONO", "OKDIVI", "OKCUNO", "OKPYNO").build()   
    def OCUSMA = query.getContainer()
    OCUSMA.set("OKCONO", CONO)
    OCUSMA.set("OKDIVI", "")
    OCUSMA.set("OKCUNO", AGNT)
    
    if(query.read(OCUSMA))  { 
      return Optional.of(OCUSMA)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Validate if order exists in OOHEAD
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(int CONO, String ORNO){     
      DBAction query = database.table("OOHEAD").index("00").selectAllFields().build()   
      def OOHEAD = query.getContainer()
      OOHEAD.set("OACONO", CONO)
      OOHEAD.set("OAORNO", ORNO)

      if(query.read(OOHEAD))  { 
        return Optional.of(OOHEAD)
      } 
  
      return Optional.empty()
  }

  //******************************************************************** 
  // Validate if record exists in OOHEAC
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAC(int CONO, String ORNO){     
      DBAction query = database.table("OOHEAC").index("00").selectAllFields().build()   
      def OOHEAC = query.getContainer()
      OOHEAC.set("BECONO", CONO)
      OOHEAC.set("BEORNO", ORNO)

      if(query.read(OOHEAC))  { 
        return Optional.of(OOHEAC)
      } 
  
      return Optional.empty()
  }
   
  //******************************************************************** 
  // Add OOHEAC record
  //********************************************************************     
  void addRecord(int CONO, String ORNO, String AGN2, String AGN3, String AGN4, String AGN5, String AGN6, String AGN7){     
     DBAction action = database.table("OOHEAC").index("00").selectAllFields().build()
     DBContainer OOHEAC = action.createContainer()
     OOHEAC.set("BECONO", CONO)
     OOHEAC.set("BEORNO", ORNO)
     OOHEAC.set("BEAGN2", AGN2)
     OOHEAC.set("BEAGN3", AGN3)
     OOHEAC.set("BEAGN4", AGN4)
     OOHEAC.set("BEAGN5", AGN5)
     OOHEAC.set("BEAGN6", AGN6)
     OOHEAC.set("BEAGN7", AGN7)

     OOHEAC.set("BECHID", program.getUser())
     OOHEAC.set("BECHNO", 1) 
     
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");  
     String formatTime = now.format(format2);  
     
     //Converting String into int using Integer.parseInt()
     int regdate=Integer.parseInt(formatDate); 
     int regtime=Integer.parseInt(formatTime); 
     OOHEAC.set("BERGDT", regdate) 
     OOHEAC.set("BEMDT", regdate) 
     OOHEAC.set("BERGTM", regtime)
     action.insert(OOHEAC)         
  } 
  

}