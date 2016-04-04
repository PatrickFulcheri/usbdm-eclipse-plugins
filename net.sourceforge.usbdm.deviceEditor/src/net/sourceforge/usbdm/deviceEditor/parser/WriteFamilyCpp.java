package net.sourceforge.usbdm.deviceEditor.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.MuxSelection;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.PeripheralFunction;
import net.sourceforge.usbdm.deviceEditor.information.PeripheralTemplateInformation;
import net.sourceforge.usbdm.deviceEditor.information.PinInformation;

public class WriteFamilyCpp {

   private DeviceInfo fDeviceInfo;

   /** Base name for pin mapping file */
   private final static String pinMappingBaseFileName   = "pin_mapping";

   /** Base name for C++ files */
   private final static String gpioBaseFileName         = "gpio";

   /** Fixed GPIO mux function */
   private int      gpioFunctionMuxValue          = 1; 

   /** GPIO mux function varies with port */
   private boolean  gpioFunctionMuxValueChanged   = false;

   /** Fixed ADC mux function - default to mux setting 0*/
   private int      adcFunctionMuxValue           = 0;

   /** GPIO ADC function varies with port */
   private boolean  adcFunctionMuxValueChanged    = false;

   /** Fixed PORT clock enable register */
   private String   portClockRegisterValue        = "SCGC5";

   /** PORT clock enable register varies with port */
   private boolean  portClockRegisterChanged      = false;

   /*
    * Macros =============================================================================================
    */
   HashSet<String> aliases = null;
   
   /**
    * Records aliases used
    * 
    * @param aliasName Alias to record
    * 
    * @return true=> new (acceptable) alias
    */
   private boolean recordAlias(String aliasName) {
      if (aliases == null) {
         aliases = new HashSet<String>();
      }
      if (aliases.contains(aliasName)) {
         return false;
      }
      aliases.add(aliasName);
      return true;
   }

   /**
    * Writes macros describing common pin functions for all pins
    * e.g.<pre>
    * #undef FIXED_ADC_FN
    * #undef FIXED_GPIO_FN
    * #undef FIXED_PORT_CLOCK_REG
    * 
    * #define FIXED_ADC_FN         0                    // Fixed ADC Multiplexing value
    * #define FIXED_GPIO_FN        1                    // Fixed GPIO Multiplexing value
    * #define FIXED_PORT_CLOCK_REG SIM->SCGC5           // Fixed PORT Clock
    * </pre>
    * 
    * @param headerFile Header file to write to
    * @throws IOException 
    * 
    * @throws Exception 
    */
   private void writePinDefines(DocumentUtilities writer) throws IOException {
      writer.writeBanner("Common Mux settings for PCR");
      writer.writeMacroUnDefinition("FIXED_ADC_FN");
      writer.writeMacroUnDefinition("FIXED_GPIO_FN");
      writer.writeMacroUnDefinition("FIXED_PORT_CLOCK_REG");
      if (adcFunctionMuxValueChanged) {
         writer.writeMacroDefinition("ADC_FN_CHANGES", "", " Indicates ADC Multiplexing varies with pin");
      }
      else {
         writer.writeMacroDefinition("FIXED_ADC_FN", Integer.toString(adcFunctionMuxValue), " Fixed ADC Multiplexing value");
      }
      if (gpioFunctionMuxValueChanged) {
         writer.writeMacroDefinition("GPIO_FN_CHANGES", "", " Indicates GPIO Multiplexing varies with pin");
      }
      else {
         writer.writeMacroDefinition("FIXED_GPIO_FN", Integer.toString(gpioFunctionMuxValue), " Fixed GPIO Multiplexing value");
      }
      if (portClockRegisterChanged) {
         writer.writeMacroDefinition("PORT_CLOCK_REG_CHANGES", "", " Indicates PORT Clock varies with pin");
      }
      else {
         writer.writeMacroDefinition("FIXED_PORT_CLOCK_REG", portClockRegisterValue, " Fixed PORT Clock");
      }
      writer.write("\n");
   }

   /**
    * Gets pin name with appended location<br>
    * If no location a null is returned
    * 
    * @param pinInformation
    * @return name with aliases e.g. <b><i>PTE0 (Alias:D14)</b></i>
    */
   @SuppressWarnings("unused")
   private String getPinNameWithLocation(PinInformation pinInformation) {
      String pinName = pinInformation.getName();

      String location = fDeviceInfo.getDeviceVariant().getPackage().getLocation(pinInformation.getName());
      if (location == null) {
         return null;
      }
      if (!location.equalsIgnoreCase(pinInformation.getName())) {
         location = " (Alias:"+location.replaceAll("/", ", ")+")";
      }
      else {
         location = "";
      }
      return pinName+location;

   }

   /**
    * Writes all clock macros e.g.
    * <pre>
    * #define ADC0_CLOCK_REG       SIM->SCGC6          
    * #define ADC0_CLOCK_MASK      SIM_SCGC6_ADC0_MASK 
    * </pre>
    * 
    * @param writer  Where to write
    * 
    * @throws IOException 
    */
   private void writeClockMacros(DocumentUtilities writer) throws IOException {
      writer.writeBanner("Peripheral clock macros");
      writer.writeMacroDefinition("PORT_CLOCK_REG", portClockRegisterValue);
      writer.write("\n");
   }

   /**
    * Write an external declaration for a simple peripheral (GPIO,ADC,PWM) e.g.
    * 
    * <pre>
    * template&lt;uint8_t bitNum&gt; using GpioC = Gpio_T&lt;GpioCInfo, bitNum&gt;;
    * 
    * using gpio_p56             = const USBDM::GpioC&lt;11&gt;;
    * using gpio_p57             = const USBDM::GpioC&lt;12&gt;;
    * </pre>
    * 
    * @param peripheral         Template information
    * @param mappedFunction   Information about the pin and function being declared
    * @param fnIndex          Index into list of functions mapped to pin
    * @param writer   Where to write
    * 
    * @throws Exception 
    */
   private void writeExternDeclaration(Peripheral peripheral, MappingInfo mappedFunction, int fnIndex, DocumentUtilities writer) throws IOException {
      String definition = peripheral.getDefinition(mappedFunction, fnIndex);
      if (definition == null) {
         return;
      }
      String signalName = peripheral.getInstanceName(mappedFunction, fnIndex);
      String locations = fDeviceInfo.getDeviceVariant().getPackage().getLocation(mappedFunction.getPin());
      if ((locations != null) && (!locations.isEmpty())) {
         for (String location:locations.split("/")) {
            if (!location.equalsIgnoreCase(mappedFunction.getPin().getName())) {
               String aliasName = peripheral.getAliasName(signalName, location);
               if (aliasName!= null) {
                  String declaration = peripheral.getAliasDeclaration(aliasName, mappedFunction, fnIndex);
                  if ((declaration != null) && isFunctionMappedToPin(peripheral, mappedFunction)) {
                     if (!recordAlias(aliasName)) {
                        // Comment out repeated aliases
                        writer.write("//");
                     }
                     writer.write(declaration);
                  }
               }
            }
         }
      }
   }

   /**
    * Write all Peripheral Information Classes<br>
    * 
    * <pre>
    *  class Adc0Info {
    *     public:
    *        //! Hardware base pointer
    *        static constexpr uint32_t basePtr   = ADC0_BasePtr;
    * 
    *        //! Base value for PCR (excluding MUX value)
    *        static constexpr uint32_t pcrValue  = DEFAULT_PCR;
    * 
    *        //! Information for each pin of peripheral
    *        static constexpr PcrInfo  info[32] = {
    * 
    *   //         clockMask         pcrAddress      gpioAddress gpioBit muxValue
    *   /*  0 * /  { 0 },
    *   ...
    *   #if (ADC0_SE4b_PIN_SEL == 1)
    *    /*  4 * /  { PORTC_CLOCK_MASK, PORTC_BasePtr,  GPIOC_BasePtr,  2,  0 },
    *   #else
    *    /*  4 * /  { 0 },
    *   #endif
    *   ...
    *   };
    *   };
    * </pre>
    * @param pinMappingHeaderFile Where to write
    * 
    * @throws IOException 
    */
   private void writePeripheralInformationClasses(DocumentUtilities writer) throws IOException {
      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);
      writer.writeBanner("Peripheral Information Classes");

      writer.writeStartGroup( 
            "PeripheralPinTables", 
            "Peripheral Information Classes", 
            "Provides instance specific information about a peripheral");

      for (String key:fDeviceInfo.getPeripherals().keySet()) {
         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         peripheral.writeInfoClass(writer);
      }
      writer.writeCloseGroup();
      writer.writeCloseNamespace();
      writer.write("\n");
   }

   /**
    * Write GPIO Header file.<br>
    * This mostly contains the extern declarations for peripherals
    * 
    * <pre>
    * <b>#if</b> <i>PTC18_SEL</i> == 1
    * using <i>gpio_A5</i>  = const USBDM::<i>GpioC&lt;18&gt;</i>;
    * <b>#endif</b>
    * </pre>
    * 
    * @param gpioHeaderFile Where to write
    * 
    * @throws Exception 
    */
   private void writeDeclarations(DocumentUtilities writer) throws IOException {

      writer.write("\n");
      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);

      PeripheralTemplateInformation peripheralTemplateInformation = null;
      boolean groupDone = false;
      for (String key:fDeviceInfo.getPeripherals().keySet()) {
         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         if (peripheralTemplateInformation != peripheral.getPeripheralTemplate()) {
            peripheralTemplateInformation = peripheral.getPeripheralTemplate();
            if (groupDone) {
               // Terminate previous group
               writer.writeCloseGroup();
            }
            groupDone = false;
         }
         String declaration = peripheral.getCTemplate();
         if (declaration != null) {
            writer.writeStartGroup(peripheral);
            groupDone = true;
            writer.write(declaration);
         }
         for (String pinName:fDeviceInfo.getPins().keySet()) {
            PinInformation pin = fDeviceInfo.getPins().get(pinName);
            Map<MuxSelection, MappingInfo> mappedFunctions = pin.getMappedFunctions();
            if (mappedFunctions == null) {
               continue;
            }
            for (MuxSelection index:mappedFunctions.keySet()) {
               if (index == MuxSelection.reset) {
                  continue;
               }
               MappingInfo mappedFunction = mappedFunctions.get(index);
               for (int fnIndex=0; fnIndex<mappedFunction.getFunctions().size(); fnIndex++) {
                  PeripheralFunction function = mappedFunction.getFunctions().get(fnIndex);
                  if (function.getPeripheral() == peripheral) {
                     writeExternDeclaration(peripheral, mappedFunction, fnIndex, writer);
                  }
               }
            }
         }
      }
      if (groupDone) {
         // Terminate last group
         writer.writeCloseGroup();
      }

      writer.writeDocBanner("Used to configure pin-mapping before 1st use of peripherals");
      writer.write("extern void usbdm_PinMapping();\n");
      writer.writeCloseNamespace();
   }

   /**
    * Write Pin Mapping function to CPP file
    * 
    * @param cppFile    File to write to
    * 
    * @throws IOException
    */
   private void writePinMappingFunction(DocumentUtilities writer) throws IOException {

      writer.writeConditionalStart("DO_MAP_PINS_ON_RESET>0");
      writer.write(
            "struct PinInit {\n"+
                  "   uint32_t pcrValue;\n"+
                  "   uint32_t volatile *pcr;\n"+
                  "};\n\n"+
                  "static constexpr PinInit pinInit[] = {\n"
            );

      for (String pinName:fDeviceInfo.getPins().keySet()) {
         if (fDeviceInfo.getDeviceVariant().getPackage().getLocation(pinName) == null) {
            // Discard pin that is not available on this package
            continue;
         }
         Pattern p = Pattern.compile("PT([A-Z]+)([0-9]+)");
         Matcher m = p.matcher(pinName);
         if (m.matches()) {
            String instance = m.replaceAll("$1");
            String signal   = m.replaceAll("$2");
            writer.writeConditionalStart(String.format("%s_SIG_SEL>=0", pinName, pinName));
            writer.write(String.format("   { PORT_PCR_MUX(%s_SIG_SEL)|%s::DEFAULT_PCR, &PORT%s->PCR[%s]},\n", pinName, DeviceInfo.NAME_SPACE, instance, signal));
            writer.writeConditionalEnd();
         }
      }
      writer.write("};\n\n");

      writer.write(
            "/**\n" + 
                  " * Used to configure pin-mapping before 1st use of peripherals\n" + 
                  " */\n" + 
                  "void usbdm_PinMapping() {\n"
            );

      boolean firstExpression = true;
      String currentBasename = null;
      String  instance = "X";
      int conditionCounter = 0;
      for (String pinName:fDeviceInfo.getPins().keySet()) {
         if (fDeviceInfo.getDeviceVariant().getPackage().getLocation(pinName) == null) {
            // Discard pin that is not available on this package
            continue;
         }
         Pattern p = Pattern.compile("(PT([A-Z]))[0-9]+");
         Matcher m = p.matcher(pinName);
         if (m.matches()) {
            String basename = m.replaceAll("$1");
            if (!basename.equals(currentBasename)) {
               if (!firstExpression) {
                  writer.write(String.format("\n\n   SIM->FIXED_PORT_CLOCK_REG |= PORT%s_CLOCK_MASK;\n", instance));
                  writer.writeConditionalEnd();
               }
               currentBasename = basename;
               writer.write("#if ");
               firstExpression = false;
               instance = m.replaceAll("$2");
            }
            else {
               writer.write(" || ");
               if (++conditionCounter>=4) {
                  writer.write("\\\n    ");
                  conditionCounter = 0;
               }
            }
            writer.write(String.format("(%s_SIG_SEL>=0)", pinName));
         }
      }
      if (!firstExpression) {
         writer.write(String.format("\n   SIM->FIXED_PORT_CLOCK_REG |= PORT%s_CLOCK_MASK;\n", instance));
         writer.writeConditionalEnd();
      }

      writer.write(
            "\n"+
                  "   for (const PinInit *p=pinInit; p<(pinInit+(sizeof(pinInit)/sizeof(pinInit[0]))); p++) {\n"+   
                  "      *(p->pcr) = p->pcrValue;\n"+ 
                  "   }\n"
            );
      writer.write("}\n");
      writer.writeConditionalEnd();
   }

   /**
    * Checks if a function is mapped to a pin
    * 
    * @param peripheral
    * @param mappedFunction
    * 
    * @return True if mapped.
    */
   private boolean isFunctionMappedToPin(Peripheral peripheral, MappingInfo mappedFunction) {
      if (mappedFunction.getMux() == MuxSelection.fixed) {
         // Fixed mapping are always available
         return true;
      }
      PinInformation pin = mappedFunction.getPin();
      return (pin.getMuxSelection() == mappedFunction.getMux());
   }

   /**
    * Writes pin mapping header file
    * 
    * @param headerFile Header file to write to
    * 
    * @throws IOException 
    */
   private void writePinMappingHeaderFile(Path filePath) throws IOException {
      
      aliases = null;

      BufferedWriter headerFile = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(headerFile);
      
      writer.writeHeaderFilePreamble(
            filePath.getFileName().toString(), fDeviceInfo.getSourceFilename(),
            DeviceInfo.VERSION, 
            "Pin declarations for "+fDeviceInfo.getDeviceVariantName());

      writer.writeSystemHeaderFileInclude("stddef.h");
      writer.writeHeaderFileInclude("derivative.h");
      headerFile.write("\n");

      writePinDefines(writer);
      writeClockMacros(writer);
      writePeripheralInformationClasses(writer);

      writer.writeHeaderFileInclude("gpio_defs.h");

      writeDeclarations(writer);

      writer.writeHeaderFilePostamble(pinMappingBaseFileName+".h");

      writer.close();
   }

   /**                    
    * Write CPP file      
    *                     
    * @param cppFile      
    * 
    * @throws IOException 
    */                    
   private void writePinMappingCppFile(Path filePath) throws IOException {
      BufferedWriter cppFile = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(cppFile);
      
      writer.writeCppFilePreamble(
            filePath.getFileName().toString(), fDeviceInfo.getSourceFilename(),
            DeviceInfo.VERSION, 
            "Pin declarations for "+fDeviceInfo.getDeviceVariantName());


      writer.writeHeaderFileInclude("gpio.h");
      writer.write("\n");

      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);
      writePinMappingFunction(writer);
      writer.writeCppFilePostAmble();
      writer.writeCloseNamespace();
      writer.close();
   }

   /**
    * Process file
    * 
    * @param  directory    Parent director
    * @param  filename     Filename to use as base of files written
    * @param  deviceInfo   Device information to print to CPP files  
    * 
    * @throws IOException 
    */
   public void writeCppFiles(Path directory, String filename, DeviceInfo deviceInfo) throws IOException {
      if (!filename.isEmpty()) {
         filename = "-"+filename;
      }
      fDeviceInfo = deviceInfo;
      writePinMappingHeaderFile(directory.resolve("Project_Headers").resolve(pinMappingBaseFileName+filename+".h"));
      writePinMappingCppFile(directory.resolve("Sources").resolve(gpioBaseFileName+filename+".cpp"));
   }
   
   /**
    * Process file
    * 
    * @param  directory    Parent director
    * @param  deviceInfo   Device information to print to CPP files  
    * 
    * @throws IOException 
    */
   public void writeCppFiles(Path directory, DeviceInfo deviceInfo) throws IOException {
      for (String key:deviceInfo.getDeviceVariants().keySet()) {
         deviceInfo.setDeviceVariant(key);
         writeCppFiles(directory, deviceInfo.getDeviceVariantName(), deviceInfo);
      }
   }
}
