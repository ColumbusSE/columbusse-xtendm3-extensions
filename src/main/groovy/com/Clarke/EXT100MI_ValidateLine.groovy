// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-02-08
// @version   1.0 
//
// Description 
// This API is used to validate the item and customer assortment and connected licenses
// Transaction ValidateLine
// 

//**************************************************************************** 
// Date    Version     Developer 
// 230208  1.0         Jessica Bjorklund, Columbus   New API transaction
//**************************************************************************** 

import java.time.LocalDate
import java.time.LocalDateTime   
import java.time.format.DateTimeFormatter 

public class ValidateLine extends ExtendM3Transaction {
  private final MIAPI mi  
  private final DatabaseAPI database  
  private final ProgramAPI program  
  private final MICallerAPI miCaller  
  private final LoggerAPI logger   

  //public double TEST
  public int inCONO
  public String inORNO
  public String inCUNO
  public String inITNO
  public int itemAssorted
  public int customerAssorted
  public String orderADID
  public String customerADID
  public String customerState
  public String addressState
  public String customerCountry
  public String addressCountry
  public String customerAssortment
  public String item
  public String itemAssortment
  public int itemSequence
  public String todaysDate
  public int todaysDateInt
  public String licensee
  public String licenseNumber
  public String licenseDate
  public String addressSource
  public String addressType
  public String statusOut
  public String messageOut

  public ValidateLine(MIAPI mi, DatabaseAPI database,ProgramAPI program, MICallerAPI miCaller, LoggerAPI logger) {
     this.mi = mi 
     this.database = database  
     this.program = program  
     this.miCaller = miCaller 
     this.logger = logger  
  } 
    
    
  public void main() {  
    validateInput()
    
    logger.debug("itemAssorted ${itemAssorted}") 
    logger.debug("customerAssorted ${customerAssorted}") 
    logger.debug("orderADID ${orderADID}") 
    logger.debug("customerADID ${customerADID}") 
    
    // If OK, validate the item assortment
    // If the item is NOT assorted, stop validation and allow adding the order line as standard
    if (itemAssorted == 1) {
      logger.debug("Item " + inITNO + " is assorted, continue validation") 
      // If item is assorted, validate the customer assortment
      // If the customer is NOT assorted, stop validation and allow adding the order line as standard
      if (customerAssorted == 1) {
            logger.debug("Customer " + inCUNO + " is assorted, continue validation") 
            //Find customer state
            //Get order state from the order id on the order header
            //Get state from OCUSAD
            addressState = "  "
            addressCountry = "  "
            Optional<DBContainer> OCUSAD = findOCUSAD(inCONO, inCUNO, 1, orderADID)
            if (OCUSAD.isPresent()) {
               logger.debug("Found OCUSAD") 
               DBContainer containerOCUSAD = OCUSAD.get() 
               addressState = containerOCUSAD.getString("OPECAR") 
               addressCountry = containerOCUSAD.getString("OPCSCD") 
               addressSource = "OCUSAD"
               addressType = "01"
            } else {
               //Use address from the customer record instead
               addressState = customerState 
               addressCountry = customerCountry 
               addressSource = "OCUSMA"
               addressType = ""
            }
               logger.debug("addressState ${addressState}") 
               logger.debug("addressCountry ${addressCountry}") 
               
               findAssortmentCustomer()
               
               logger.debug("After findAssortmentCustomer()") 
               logger.debug("customerAssortment ${customerAssortment}") 
               logger.debug("itemAssortment ${itemAssortment}") 
               
               if (customerAssortment == null && itemAssortment == null) {
                  mi.error("Assortment not found for customer " + inCUNO + ", Item " + inITNO + ", Assortment Prefix " + addressState)   
                  return             
               } else if (customerAssortment == null && itemAssortment != null) {
                  mi.error("Assortment not found for customer " + inCUNO + ", Assortment " + itemAssortment + "found for item " + inITNO)   
                  return             
               } else if (customerAssortment != null && itemAssortment == null) {
                  mi.error("Assortment not found for item " + inITNO + ", Assortment " + customerAssortment + "found for customer " + inCUNO)   
                  return             
               }
      } else {
        //Customer is not assorted, everything is ok, leave validation
        logger.debug("OK - Customer ${inCUNO} is not assorted") 
        statusOut = "OK"
        messageOut = "Order Number " + inORNO + ", Customer " + inCUNO + ", Not assorted"
        mi.outData.put("STAT", statusOut)
        mi.outData.put("MSGN", messageOut)
        return
      }
      
    } else {
      logger.debug("OK - Item ${inITNO} is not assorted") 
      statusOut = "OK"
      messageOut = "Order Number " + inORNO + ", Item " + inITNO + ", Not assorted"
      mi.outData.put("STAT", statusOut)
      mi.outData.put("MSGN", messageOut)
      return
    }
    
		mi.write()
  }
 
  //***************************************************** 
  // validateInput - Validate input values
  //*****************************************************
   public void validateInput() {  
       inCONO = program.LDAZD.CONO as Integer
       
       // Validate OOHEAD
       inORNO = mi.in.get("ORNO")  
       Optional<DBContainer> OOHEAD = findOOHEAD(inCONO, inORNO)
       if (!OOHEAD.isPresent()) {
          mi.error("Order Number " + inORNO + " doesn't exist")   
          return             
       } else {
          DBContainer containerOOHEAD = OOHEAD.get() 
          orderADID = "        "
          orderADID = containerOOHEAD.getString("OAADID")       
       }
       
       logger.debug("orderADID ${orderADID}") 
  
       // Validate MITMAS
       inITNO = mi.in.get("ITNO")  
       Optional<DBContainer> MITMAS = findMITMAS(inCONO, inITNO)
       if (!MITMAS.isPresent()) {
          mi.error("Item Number " + inITNO + " doesn't exist")   
          return             
       } else {
          DBContainer containerMITMAS = MITMAS.get() 
          itemAssorted = containerMITMAS.get("MMACHK") 
       }
       
       logger.debug("itemAssorted MITMAS ${itemAssorted}") 

       // Validate OCUSMA
       inCUNO = mi.in.get("CUNO")  
       Optional<DBContainer> OCUSMA = findOCUSMA(inCONO, inCUNO)
       if (!OCUSMA.isPresent()) {
          mi.error("Customer Number " + inCUNO + " doesn't exist")   
          return             
       } else {
          DBContainer containerOCUSMA = OCUSMA.get() 
          customerAssorted = containerOCUSMA.get("OKACHK") 
          customerState = "  "
          customerState = containerOCUSMA.getString("OKECAR") 
          customerCountry = "  "
          customerCountry = containerOCUSMA.getString("OKCSCD") 
          customerADID = "      "
          customerADID = containerOCUSMA.getString("OKADID") 
       }
       
       logger.debug("customerAssorted OCUSMA ${customerAssorted}") 
       logger.debug("customerState OCUSMA ${customerState}") 
       logger.debug("customerCountry OCUSMA ${customerCountry}") 
       logger.debug("customerADID OCUSMA ${customerADID}") 

       todaysDate = currentDateYMD8AsString()
       todaysDateInt = todaysDate as Integer
       logger.debug("todaysDate ${todaysDate}") 
       logger.debug("todaysDateInt ${todaysDateInt}") 
   }
   
   
  //******************************************************************** 
  // Get OOHEAD record
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(Integer CONO, String ORNO){  
     DBAction query = database.table("OOHEAD").index("00").selection("OAORNO", "OAOPRI", "OAADID").build()
     def OOHEAD = query.getContainer()
     OOHEAD.set("OACONO", CONO)
     OOHEAD.set("OAORNO", ORNO)
     if(query.read(OOHEAD))  { 
       return Optional.of(OOHEAD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get MITMAS record
  //******************************************************************** 
  private Optional<DBContainer> findMITMAS(Integer CONO, String ITNO){  
     DBAction query = database.table("MITMAS").index("00").selection("MMITNO", "MMACHK").build()
     def MITMAS = query.getContainer()
     MITMAS.set("MMCONO", CONO)
     MITMAS.set("MMITNO", ITNO)
     if(query.read(MITMAS))  { 
       return Optional.of(MITMAS)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get OCUSMA record
  //******************************************************************** 
  private Optional<DBContainer> findOCUSMA(Integer CONO, String CUNO){  
     DBAction query = database.table("OCUSMA").index("00").selection("OKCUNO", "OKACHK", "OKECAR", "OKCSCD", "OKADID").build()
     def OCUSMA = query.getContainer()
     OCUSMA.set("OKCONO", CONO)
     OCUSMA.set("OKCUNO", CUNO)
     if(query.read(OCUSMA))  { 
       return Optional.of(OCUSMA)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get OCUSAD record
  //******************************************************************** 
  private Optional<DBContainer> findOCUSAD(Integer CONO, String CUNO, Integer ADRT, String ADID){  
     DBAction query = database.table("OCUSAD").index("00").selection("OPCUNO", "OPADRT", "OPADID", "OPECAR", "OPCSCD").build()
     def OCUSAD = query.getContainer()
     OCUSAD.set("OPCONO", CONO)
     OCUSAD.set("OPCUNO", CUNO)
     OCUSAD.set("OPADRT", ADRT)
     OCUSAD.set("OPADID", ADID)
     if(query.read(OCUSAD))  { 
       return Optional.of(OCUSAD)
     } 
  
     return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get CUGEX1 record for license information
  //******************************************************************** 
  private Optional<DBContainer> findCUGEX1(Integer CONO, String FILE, String CUNO, String ADRT, String ADID){  
     DBAction query = database.table("CUGEX1").index("00").selection("F1FILE", "F1PK01", "F1PK02", "F1PK03", "F1A230", "F1A330", "F1DAT1").build()
     def CUGEX1 = query.getContainer()
     CUGEX1.set("F1CONO", CONO)
     CUGEX1.set("F1FILE", FILE)
     CUGEX1.set("F1PK01", CUNO)
     CUGEX1.set("F1PK02", ADRT)
     CUGEX1.set("F1PK03", ADID)
     if(query.read(CUGEX1))  { 
       return Optional.of(CUGEX1)
     } 
  
     return Optional.empty()
  }
  

  
  //******************************************************************** 
  // Find records in OASCUS
  //********************************************************************  
   void findAssortmentCustomer() {   
     // Find assortment customer
     ExpressionFactory expression = database.getExpressionFactory("OASCUS")
   
     String searchState = addressState + "%"
     
     if (addressState == "XX") {
       searchState = "XX-" + addressCountry + "%"
     }
     
     logger.debug("addressState ${addressState}") 
     logger.debug("searchState ${searchState}") 
     
     expression = expression.eq("OCCONO", String.valueOf(inCONO)).and(expression.eq("OCCUNO", inCUNO))
     .and(expression.like("OCASCD", searchState))
     
     // List Assortment Customer lines  
	   DBAction actionline = database.table("OASCUS").index("10").matching(expression).selection("OCCONO", "OCCUNO", "OCASCD", "OCFDAT", "OCTDAT").reverse().build()   
     DBContainer customerLine = actionline.getContainer()  
     
     // Read with one key  
     customerLine.set("OCCONO", inCONO) 
     customerLine.set("OCCUNO", inCUNO) 
     actionline.readAll(customerLine, 2, releasedLineProcessor)   
   } 
    
  //******************************************************************** 
  // List Customer Assortment line - main loop - OASCUS
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer customerLine ->   

    logger.debug("Start listing customer assortment lines")   
    logger.debug("customer ${customerLine.get("OCCUNO")}")   
    logger.debug("customer assortment ${customerLine.get("OCASCD")}") 
    logger.debug("customer assortment from date ${customerLine.get("OCFDAT")}") 
    
    
    int fromDate = customerLine.get("OCFDAT")
    int toDate = customerLine.get("OCTDAT")
    customerAssortment = customerLine.get("OCASCD")
    
    logger.debug("customerAssortment ${customerAssortment}") 
    
    if (customerAssortment != null) {

      if (fromDate <= todaysDateInt && toDate >= todaysDateInt) {
        // Check if Item exists in found customer Assortment 
        findAssortmentItem()
        
        
        logger.debug("itemAssortment ${itemAssortment}") 
        if (itemAssortment != null) {
          
          logger.debug("itemSequence ${itemSequence}") 
          
          if (itemSequence == 1) {
            logger.debug("RUP item ${item}") 
            //this is a RUP item, check license
            Optional<DBContainer> CUGEX1 = findCUGEX1(inCONO, addressSource, inCUNO, addressType, orderADID)
            if (CUGEX1.isPresent()) {
              DBContainer containerCUGEX1 = CUGEX1.get() 
              licensee = "          "
              licensee = containerCUGEX1.getString("F1A230") 
              logger.debug("licensee ${licensee}") 
              licenseNumber = "         "
              licenseNumber = containerCUGEX1.getString("F1A330") 
              logger.debug("licenseNumber ${licenseNumber}") 
              licenseDate = "            "
              licenseDate = containerCUGEX1.get("F1DAT1") 
              logger.debug("licenseDate ${licenseDate}") 
              if (licenseDate < todaysDate) {
                 mi.error("No valid license for Order Number " + inORNO + ", Customer " + inCUNO + ", Address number " + orderADID)   
                 return 
              } else {
                // Valid license found
                logger.debug("OK - Customer ${inCUNO} is assorted and Item ${inITNO} is assorted, RUP item, Valid license found ok") 
                statusOut = "OK"
                logger.debug("validLicense found status ${statusOut}") 
                messageOut = "Order Number " + inORNO + ", Customer " + inCUNO + ", Item " + inITNO + ", Assorted OK, RUP item, Valid license found"
                mi.outData.put("STAT", statusOut)
                mi.outData.put("MSGN", messageOut)
                return
              }
            } else {
              mi.error("No valid license for Order Number " + inORNO + ", Customer " + inCUNO + ", Address number " + orderADID)  
              return 
            }
          } else {
            // Not a RUP item, no need to check license
            logger.debug("OK - Customer ${inCUNO} is assorted and Item ${inITNO} is assorted, Item is not restricted use product") 
            statusOut = "OK"
            messageOut = "Order Number " + inORNO + ", Customer " + inCUNO + ", Item " + inITNO + ", Assorted OK, Item is not restricted use product"
            mi.outData.put("STAT", statusOut)
            mi.outData.put("MSGN", messageOut)
            return
          }
        } else {
          // If no item assortment found for this customer assortment, see if there are more customer assortment lines
        }
      }
    } else {
      logger.debug("Customer Assortment at the end of the loop") 
      logger.debug("customerAssortment ${customerAssortment}") 
    }

  }


  //******************************************************************** 
  // Find record in OASITN
  //********************************************************************  
   void findAssortmentItem() {   
     // Find assortment customer
     ExpressionFactory expression = database.getExpressionFactory("OASITN")
     expression = expression.eq("OICONO", String.valueOf(inCONO)).and(expression.eq("OIITNO", inITNO))
     .and(expression.like("OIASCD", customerAssortment)).and(expression.le("OIFDAT", todaysDate))
     .and(expression.ge("OITDAT", todaysDate))    

     // List Assortment Item lines  
	   DBAction actionline = database.table("OASITN").index("20").matching(expression).selection("OICONO", "OIITNO", "OIASCD", "OISEQN", "OIFDAT", "OITDAT").reverse().build()   
     DBContainer itemLine = actionline.getContainer()  
     
     // Read with one key  
     itemLine.set("OICONO", inCONO)  
     actionline.readAll(itemLine, 1, releasedLineProcessorItem)   
   } 
    
  //******************************************************************** 
  // List Item Assortment line - main loop - OASITN
  //********************************************************************  
  Closure<?> releasedLineProcessorItem = { DBContainer itemLine ->   
    logger.debug("Found item assortment for customer assortment")
    logger.debug("item ${itemLine.get("OIITNO")}")   
    logger.debug("key ${itemLine.get("OIASCD")}") 
    logger.debug("from date ${itemLine.get("OIFDAT")}") 
    
    
    item = "          "
    item = itemLine.get("OIITNO")
    itemAssortment = "   "
    itemAssortment = itemLine.get("OIASCD")
    itemSequence = 0
    itemSequence = itemLine.get("OISEQN")
    
    logger.debug("item ${item}") 
    logger.debug("itemAssortment ${itemAssortment}") 
    logger.debug("itemSequence ${itemSequence}") 
  }

  //******************************************************************** 
  // Get date in yyyyMMdd format
  // @return date
  //******************************************************************** 
  public String currentDateYMD8AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }



}