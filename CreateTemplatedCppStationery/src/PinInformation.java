import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Information about a pin<br>
 * <li>Pin name
 * <li>Peripheral functions mapped to that pin
 */
public class PinInformation {
   
   /**
    * Pin used to denote a disabled mapping
    */
   public final static PinInformation DISABLED_PIN = new PinInformation("Disabled");
   
   /**
    * Map of all pins created<br>
    * May be searched by pin name
    */
   private static HashMap<String, PinInformation> fPins = new HashMap<String, PinInformation>();
   
   /**
    * Sorted list of pins - Created when needed
    */
   private static ArrayList<String> fPinNames = null;
   
   /**
    * Reset shared internal state
    */
   public static void reset() {
      fPins = new HashMap<String, PinInformation>();
      fPinNames = null;
   }
   
   /**
    * Get Map of all pins
    * 
    * @return
    */
   public static HashMap<String, PinInformation> getPins() {
      return fPins;
   }
   
   /**
    * Get sorted List of all pin names
    * 
    * @return ArrayList of names
    */
   public static ArrayList<String> getPinNames() {
      if (fPinNames == null) {
         fPinNames = new ArrayList<String>(PinInformation.getPins().keySet());
         Collections.sort(fPinNames, portNameComparator);
      }
      return fPinNames;
   }
   
   /**
    * Comparator for port names e.g. PTA13 c.f. PTB12
    * Treats the number separately as a number.
    */
   public static Comparator<String> portNameComparator = new Comparator<String>() {
      @Override
      public int compare(String arg0, String arg1) {
         Pattern p = Pattern.compile("([^\\d]*)(\\d*)(.*)");
         Matcher m0 = p.matcher(arg0);
         Matcher m1 = p.matcher(arg1);
         if (m0.matches() && m1.matches()) {
            String t0 = m0.group(1);
            String n0 = m0.group(2);
            String s0 = m0.group(3);
            String t1 = m1.group(1);
            String n1 = m1.group(2);
            String s1 = m1.group(3);
            int r = t0.compareTo(t1);
            if (r == 0) {
               int no0 = -1, no1 = -1;
               if (n0.length() > 0) {
                  no0 = Integer.parseInt(n0);
               }
               if (n1.length() > 0) {
                  no1 = Integer.parseInt(n1);
               }
               r = -no1 + no0;
            }
            if (r == 0) {
               Pattern pp = Pattern.compile("([^\\d]*)(\\d*)(.*)");
               Matcher mm0 = pp.matcher(s0);
               Matcher mm1 = pp.matcher(s1);
               if (mm0.matches() && mm1.matches()) {
                  String tt0 = mm0.group(1);
                  String nn0 = mm0.group(2);
                  String tt1 = mm1.group(1);
                  String nn1 = mm1.group(2);
                  r = tt0.compareTo(tt1);
                  if (r == 0) {
                     int no0 = -1, no1 = -1;
                     if (nn0.length() > 0) {
                        no0 = Integer.parseInt(nn0);
                     }
                     if (nn1.length() > 0) {
                        no1 = Integer.parseInt(nn1);
                     }
                     r = -no1 + no0;
                  }
               }
               else {
                  r = s0.compareTo(s1);
               }
            }
            return r;
         }
         return arg0.compareTo(arg1);
      }
   };

   /** Port instance e.g. PTA3 => A */
   private String fPortInstance = null;

   /** Pin instance e.g. PTA3 => 3 */
   private String fPortPin = null;

   /**
    * Get PCR register e.g. PORTA->PCR[3]
    * 
    * @return
    */
   public String getPCR() {
      if (fPortInstance == null) {
         return "0";
      }
      return String.format("&PORT%s->PCR[%s]", fPortInstance, fPortPin);
   }
   
   /**
    * Get Gpio class declaration e.g. GpioA<3>
    * 
    * @return
    */
   public String getGpioClass() {
      return String.format("Gpio%s<%s>", fPortInstance, fPortPin);
   }
   
   /**
    * Get PCR register e.g. PORTA->PCR[3]
    * 
    * @return
    */
   public String getPCRasInt() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("PORT%s_BasePtr+offsetof(PORT_Type,PCR[%s])", fPortInstance, fPortPin);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getClockMask() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("PORT%s_CLOCK_MASK", fPortInstance);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getGpioReg() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("GPIO%s_BasePtr", fPortInstance);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getGpioBitNum() {
      if (fPortInstance == null) {
         return null;
      }
      return fPortPin;
   }
   
   /** Name of the pin, usually the port name e.g. PTA1 */
   private String fName;
   
   /** Description of peripheral functions that may be mapped to this pin */
   private StringBuilder  fDescription = new StringBuilder();

   /**
    * Peripheral functions associated with this pin<br> 
    */
   private  ArrayList<PeripheralFunction> mappedPins = new ArrayList<PeripheralFunction>();

   /**
    * Peripheral functions associated with this pin arranged by function base name<br> 
    * e.g. multiple FTMs may be associated with a pin 
    */
   private HashMap<String, ArrayList<MappingInfo>> mappedPinsByFunction = new HashMap<String, ArrayList<MappingInfo>>();

   /**
    * Reset pin
    */
   private ArrayList<PeripheralFunction> fResetFunction = null;

   /**
    * Default functions on this pin
    */
   private ArrayList<PeripheralFunction> fDefaultFunction = null;
   
   /**
    * Factory to create pin from name<br>
    * 
    * @param name Name of pin
    *                      
    * @return Created pin
    * 
    * @throws Exception If the pin already exists
    */
   public static PinInformation createPin(String name) throws Exception {
      // Check for repeated pin
      PinInformation pinInformation = fPins.get(name);
      if (pinInformation != null) {
         throw new Exception("Pin already exists: " + name);
      }
      // Created pin
      pinInformation = new PinInformation(name);
      // Add to map
      fPins.put(name, pinInformation);
      // Name list is now invalid
      fPinNames = null;
      
      return pinInformation;
   }
   
   /**
    * Finds pin from name<br>
    * 
    * @param name Name of pin
    *                      
    * @return Pin found or null if not present
    */
   public static PinInformation find(String name) {
      if (name.equalsIgnoreCase(DISABLED_PIN.fName)) {
         return DISABLED_PIN;
      }
      return fPins.get(name);
   }
   
   /**
    * Create empty pin function for given pin
    * 
    * @param fName Name of the pin, usually the port name e.g. PTA1
    */
   private PinInformation(String name) {
      this.fName  = name;
      Pattern p = Pattern.compile("^\\s*PT(.)(\\d*)\\s*$");
      Matcher m = p.matcher(name);
      if (m.matches()) {
         fPortInstance = m.group(1);
         fPortPin      = m.group(2);
      }
      fResetFunction = null;
   }
   
   /**
    * Get pin name
    * 
    * @return
    */
   String getName() {
      return fName;
   }
   
   /**
    * Get description of functions mapped to this pin
    * 
    * @return Description
    */
   String getDescription() {
      if (fDescription.length() == 0) {
         return fName;
      }
      return fName + " = " + fDescription.toString();
   }
   
   /**
    * Gets list of peripheral functions mapped to this pin
    * 
    * @param   fBaseName
    * 
    * @return  List (may be empty, never null)
    */
   public ArrayList<PeripheralFunction> getMappedPeripheralFunctions() {
      return mappedPins;
   }
   
   /**
    * Gets sub-list of peripheral functions mapped to this pin
    * 
    * @param   baseName
    * 
    * @return  List (may be empty, never null)
    */
   public ArrayList<MappingInfo> createMappingList(String baseName) {
      ArrayList<MappingInfo> list = mappedPinsByFunction.get(baseName);
      if (list == null) {
         list = new ArrayList<MappingInfo>();
         mappedPinsByFunction.put(baseName, list);
      }
      return list;
   }
   
//   /**
//    * Adds a mapping of a peripheral function to the pin
//    * 
//    * @param mappingInfo        Mapping to add
//    * 
//    * @throws Exception 
//    */
//   public void addPeripheralFunctionMapping(MappingInfo mappingInfo) {
//      ArrayList<PeripheralFunction> peripheralFunction = mappingInfo.functions;
//      ArrayList<MappingInfo> elements = createMappingList(peripheralFunction.fPeripheral.fBaseName);
//      elements.add(mappingInfo);
//      mappedPins.add(peripheralFunction);
//      if (fDescription.length() > 0) {
//         fDescription.append(",");
//      }
//      fDescription.append(peripheralFunction.getName());
//      fPinNames = null;
//   }

   /**
    * Gets sub-list of peripheral functions mapped to this pin that have this basename
    * 
    * @param   baseName
    * 
    * @return  List (never null)
    */
   public ArrayList<MappingInfo> getMappingList(String baseName) {
      ArrayList<MappingInfo> list = mappedPinsByFunction.get(baseName);
      if (list == null) {
         list = new ArrayList<MappingInfo>();
      }
      return list;
   }
   
//   /**
//    * Get index of pin that this peripheral function is mapped to
//    * 
//    * @return index of function if mapped, -1 if not mapped to this pin
//    */
//   public int getMappedPinIndex(PeripheralFunction peripheralFunction) {
//      for (int index=0; index<7; index++) {
//         if (mappedPins[index] == peripheralFunction) {
//            return index;
//         }
//      }
//      return -1;
//   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString() {
      return getDescription();
   }

   /**
    * Sets the reset pin mapping
    * 
    * @param functions reset peripheral function on this pin
    */
   public void setResetValue(ArrayList<PeripheralFunction> functions) {
      fResetFunction = functions;
   }

   /**
    * Returns the reset pin mapping
    * 
    * @return reset peripheral function on this pin
    */
   public ArrayList<PeripheralFunction> getResetValue() {
      return fResetFunction;
   }

   /**
    * Sets the default pin mapping
    * 
    * @param defaultFunctions default peripheral function on this pin
    */
   public void setDefaultValue(ArrayList<PeripheralFunction> defaultFunctions) {
      fDefaultFunction = defaultFunctions;
   }

   /**
    * Returns the default pin mapping
    * 
    * @return default peripheral function on this pin
    */
   public ArrayList<PeripheralFunction> getDefaultValue() {
      return fDefaultFunction;
   }

   /**
    * Sets the default peripheral functions for the pin
    * 
    * @param defaultPeripheralName  Name of peripherals to look for e.g. <b><i>GPIOE_1/LLWU_P0</i></b>
    * 
    * @throws Exception If pin already has default or new default not found as available pin mapping
    */
   public void setDefaultPeripheralFunctions(final String defaultPeripheralName) throws Exception {
      final PinInformation pinInformation = this;
      if (pinInformation.getDefaultValue() != null) {
         throw new Exception("Pin "+pinInformation.getName()+" already has default value "+pinInformation.getDefaultValue());
      }
      HashMap<MuxSelection, MappingInfo> functionMappings = MappingInfo.getFunctions(pinInformation);
      functionMappings.forEach(new BiConsumer<MuxSelection, MappingInfo>() {
         @Override
         public void accept(MuxSelection muxSelection, MappingInfo mappingInfo) {
           if (mappingInfo.getFunctionList().equalsIgnoreCase(defaultPeripheralName) && (mappingInfo.mux != MuxSelection.Reset)) {
              pinInformation.setDefaultValue(mappingInfo.functions);
           }
         }
      } );
      if (pinInformation.getDefaultValue() == null) {
         throw new Exception("Peripheral "+defaultPeripheralName+" not found as option for pin " + getName());
      }
   }

   /**
    * Sets the reset peripheral functions for the pin
    * 
    * @param resetPeripheralName  Name of peripherals to look for e.g. <b><i>GPIOE_1/LLWU_P0</i></b>
    * 
    * @throws Exception If pin already has default or new default not found as available pin mapping
    */
   public void setResetPeripheralFunctions(final String resetPeripheralName) throws Exception {
      final PinInformation pinInformation = this;
      if (pinInformation.getResetValue() != null) {
         throw new Exception("Pin already has reset value: P = " + pinInformation.getName());
      }
      HashMap<MuxSelection, MappingInfo> functionMappings = MappingInfo.getFunctions(pinInformation);
      functionMappings.forEach(new BiConsumer<MuxSelection, MappingInfo>() {
         @Override
         public void accept(MuxSelection muxSelection, MappingInfo mappingInfo) {
           if (mappingInfo.getFunctionList().equalsIgnoreCase(resetPeripheralName)) {
              pinInformation.setResetValue(mappingInfo.functions);
           }
         }
      } );
      if (pinInformation.getResetValue() == null) {
         throw new Exception("Peripheral "+resetPeripheralName+" not found as option for pin " + getName());
      }
   }

}
