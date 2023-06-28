// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-19
// @version   1.0 
//
// Description 
// This API is used to update an agent record in table OOHEAC
// Transaction UpdAgentInfo
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230419  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.LocalDateTime  
import java.time.format.DateTimeFormatter

public class UpdAgentInfo extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  
	public Integer CONO
	public String inORNO
	public String inAGN2
  public String inAGN3
  public String inAGN4
  public String inAGN5
  public String inAGN6
  public String inAGN7

  
  public UpdAgentInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
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
     } else {
        DBContainer containerOCUSMAAgent2 = OCUSMAAgent2.get() 
        String inSTAT = containerOCUSMAAgent2.getString("OKSTAT")
        int inCUTP = containerOCUSMAAgent2.get("OKCUTP")

        if (inSTAT != "20") {
           mi.error("Customer status " + inSTAT + " is invalid")   
           return                                         
        }
        
        if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
           mi.error("Customer type " + inCUTP + " is invalid")   
           return                                         
        }
     }
     

     
     inAGN3 = mi.in.get("AGN3") 
     if (inAGN3 != null && inAGN3 != "" && inAGN3 != "?") {
       Optional<DBContainer> OCUSMAAgent3 = findOCUSMA(CONO, inAGN3)  
       if(!OCUSMAAgent3.isPresent()){                         
         mi.error("Agent 3 " + inAGN3 + " is invalid")   
         return                                         
       } else {
         DBContainer containerOCUSMAAgent3 = OCUSMAAgent3.get() 
         String inSTAT = containerOCUSMAAgent3.getString("OKSTAT")
         int inCUTP = containerOCUSMAAgent3.get("OKCUTP")

         if (inSTAT != "20") {
            mi.error("Customer status " + inSTAT + " is invalid")   
            return                                         
         }
        
         if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
            mi.error("Customer type " + inCUTP + " is invalid")   
            return                                         
         }

       } 
     }
     if (inAGN3 == "?") {
       inAGN3 = ""
     }


     inAGN4 = mi.in.get("AGN4")   
     if (inAGN4 != null && inAGN4 != "" && inAGN4 != "?") {
       Optional<DBContainer> OCUSMAAgent4 = findOCUSMA(CONO, inAGN4)  
       if(!OCUSMAAgent4.isPresent()){                         
         mi.error("Agent 4 " + inAGN4 + " is invalid")   
         return                                         
       } else {
         DBContainer containerOCUSMAAgent4 = OCUSMAAgent4.get() 
         String inSTAT = containerOCUSMAAgent4.getString("OKSTAT")
         int inCUTP = containerOCUSMAAgent4.get("OKCUTP")

         if (inSTAT != "20") {
            mi.error("Customer status " + inSTAT + " is invalid")   
            return                                         
         }
        
         if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
            mi.error("Customer type " + inCUTP + " is invalid")   
            return                                         
         }

       }   
     }
     if (inAGN4 == "?") {
       inAGN4 = ""
     }


     inAGN5 = mi.in.get("AGN5")
     if (inAGN5 != null && inAGN5 != "" && inAGN5 != "?") {
       Optional<DBContainer> OCUSMAAgent5 = findOCUSMA(CONO, inAGN5)  
       if(!OCUSMAAgent5.isPresent()){                         
         mi.error("Agent 5 " + inAGN5 + " is invalid")   
         return                                         
       } else {
         DBContainer containerOCUSMAAgent5 = OCUSMAAgent5.get() 
         String inSTAT = containerOCUSMAAgent5.getString("OKSTAT")
         int inCUTP = containerOCUSMAAgent5.get("OKCUTP")

         if (inSTAT != "20") {
            mi.error("Customer status " + inSTAT + " is invalid")   
            return                                         
         }
        
         if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
            mi.error("Customer type " + inCUTP + " is invalid")   
            return                                         
         }

       }  
     }
     if (inAGN5 == "?") {
       inAGN5 = ""
     }


     inAGN6 = mi.in.get("AGN6") 
     if (inAGN6 != null && inAGN6 != "" && inAGN6 != "?") {
       Optional<DBContainer> OCUSMAAgent6 = findOCUSMA(CONO, inAGN6)  
       if(!OCUSMAAgent6.isPresent()){                         
         mi.error("Agent 6 " + inAGN6 + " is invalid")   
         return                                         
       } else {
         DBContainer containerOCUSMAAgent6 = OCUSMAAgent6.get() 
         String inSTAT = containerOCUSMAAgent6.getString("OKSTAT")
         int inCUTP = containerOCUSMAAgent6.get("OKCUTP")

         if (inSTAT != "20") {
            mi.error("Customer status " + inSTAT + " is invalid")   
            return                                         
         }
        
         if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
            mi.error("Customer type " + inCUTP + " is invalid")   
            return                                         
         }

       }  
     }
     if (inAGN6 == "?") {
       inAGN6 = ""
     }


     inAGN7 = mi.in.get("AGN7")  
     if (inAGN7 != null && inAGN7 != "" && inAGN7 != "?") {
       Optional<DBContainer> OCUSMAAgent7 = findOCUSMA(CONO, inAGN7)  
       if(!OCUSMAAgent7.isPresent()){                         
         mi.error("Agent 7 " + inAGN7 + " is invalid")   
         return                                         
       } else {
         DBContainer containerOCUSMAAgent7 = OCUSMAAgent7.get() 
         String inSTAT = containerOCUSMAAgent7.getString("OKSTAT")
         int inCUTP = containerOCUSMAAgent7.get("OKCUTP")

         if (inSTAT != "20") {
            mi.error("Customer status " + inSTAT + " is invalid")   
            return                                         
         }
        
         if (inCUTP != 0 && inCUTP != 1 && inCUTP != 2) {
            mi.error("Customer type " + inCUTP + " is invalid")   
            return                                         
         }

       }   
     }
     if (inAGN7 == "?") {
       inAGN7 = ""
     }


     //Check if Agent Information record exists                                                    
     Optional<DBContainer> OOHEAC = findOOHEAC(CONO, inORNO)        
     if (!OOHEAC.isPresent()) {                                           
        //If record found, return error message 
        mi.error("Agent record doesn't exists")   
        return
     } else {
        // Write record 
        updRecord()    
     }  

  }


  //******************************************************************** 
  // Get Company record
  //******************************************************************** 
  private Optional<DBContainer> findCMNCMP(Integer CONO) {                             
      DBAction query = database.table("CMNCMP").index("00").build()   
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
  private Optional<DBContainer> findOCUSMA(int CONO, String AGNT) {  
    DBAction query = database.table("OCUSMA").index("00").selection("OKCUTP", "OKSTAT").build()   
    def OCUSMA = query.getContainer()
    OCUSMA.set("OKCONO", CONO)
    OCUSMA.set("OKCUNO", AGNT)
    
    if(query.read(OCUSMA))  { 
      return Optional.of(OCUSMA)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Validate if order exists in OOHEAD
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(int CONO, String ORNO) {     
      DBAction query = database.table("OOHEAD").index("00").build()   
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
  private Optional<DBContainer> findOOHEAC(int CONO, String ORNO) {     
      DBAction query = database.table("OOHEAC").index("00").build()   
      def OOHEAC = query.getContainer()
      OOHEAC.set("BECONO", CONO)
      OOHEAC.set("BEORNO", ORNO)

      if(query.read(OOHEAC))  { 
        return Optional.of(OOHEAC)
      } 
  
      return Optional.empty()
  }


  //******************************************************************** 
  // Update Agent information
  //******************************************************************** 
  void updRecord(){ 
     DBAction action = database.table("OOHEAC").index("00").build()
     DBContainer OOHEAC = action.createContainer()
     OOHEAC.set("BECONO", CONO)
     OOHEAC.set("BEORNO", inORNO)

     // Read with lock
     action.readLock(OOHEAC, updateCallBack)
  }

    
  Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now()    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd")  
     String formatDate = now.format(format1)    
     
     int changeNo = lockedResult.get("BECHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the fields if filled
     if(inAGN2 != null){  
        lockedResult.set("BEAGN2", inAGN2) 
     }
     
     // Update the fields if filled
     if(inAGN3 != null){  
        lockedResult.set("BEAGN3", inAGN3) 
     }

     // Update the fields if filled
     if(inAGN4 != null){  
        lockedResult.set("BEAGN4", inAGN4) 
     }

     // Update the fields if filled
     if(inAGN5 != null){  
        lockedResult.set("BEAGN5", inAGN5) 
     }

     // Update the fields if filled
     if(inAGN6 != null){  
        lockedResult.set("BEAGN6", inAGN6) 
     }

     // Update the fields if filled
     if(inAGN7 != null){  
        lockedResult.set("BEAGN7", inAGN7) 
     }

     // Update changed information
     int changeddate=Integer.parseInt(formatDate)   
     lockedResult.set("BELMDT", changeddate)  
      
     lockedResult.set("BECHNO", newChangeNo) 
     lockedResult.set("BECHID", program.getUser())
     lockedResult.update()
  }
    
}