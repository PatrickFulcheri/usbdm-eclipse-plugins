package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.MuxSelection;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.Pin;
import net.sourceforge.usbdm.deviceEditor.information.Signal;

public class WriteFamilyCpp {

   /** Device information */
   private DeviceInfo fDeviceInfo;

   /** Include directory in C Project */
   private final static String INCLUDE_DIRECTORY = "Project_Headers";
   
   /** Source directory ion C project */
   private final static String SOURCE_DIRECTORY  = "Sources";
   
   /** Base name for pin mapping file */
   private final static String PIN_MAPPING_BASEFILENAME   = "pin_mapping";

   /** Base name for C++ files */
   private final static String HARDWARE_BASEFILENAME      = "hardware";

   /** Name of function to do pin mapping */
   private static final String DO_PIN_MAPPING_FUNCTION = "mapAllPins";

   /** Fixed GPIO multiplexor function */
   private int      gpioFunctionMuxValue          = 1; 

   /** GPIO multiplexor function varies with port */
   private boolean  gpioFunctionMuxValueChanged   = false;

   /** Fixed ADC multiplexor function - default to multiplexor setting 0*/
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
    * @param writer Where to write
    * 
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
    * Writes all clock macros e.g.
    * 
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
    * Write all Peripheral Information Classes<br>
    * 
    * <pre>
    *  class Adc0Info {
    *   ...
    *  };
    *  class Adc1Info {
    *   ...
    *  };
    * </pre>
    * @param writer Where to write
    * 
    * @throws IOException 
    */
   private void writePeripheralInformationClasses(DocumentUtilities writer) throws IOException {
      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);
      
      writer.write(
            "/** Class to static check signal mapping is valid */\n"+
            "template<class Info, int signalNum> class CheckSignal {\n"+
            "#ifdef DEBUG_BUILD\n"+
            "   static_assert((signalNum<Info::NUM_SIGNALS), \"Non-existent signal - Modify Configure.usbdm\");\n"+
            "   static_assert((signalNum>=Info::NUM_SIGNALS)||(Info::info[signalNum].gpioBit != UNMAPPED_PCR), \"Signal is not mapped to a pin - Modify Configure.usbdm\");\n"+
            "   static_assert((signalNum>=Info::NUM_SIGNALS)||(Info::info[signalNum].gpioBit != INVALID_PCR),  \"Signal doesn't exist in this device/package\");\n"+
            "   static_assert((signalNum>=Info::NUM_SIGNALS)||((Info::info[signalNum].gpioBit == UNMAPPED_PCR)||(Info::info[signalNum].gpioBit == INVALID_PCR)||(Info::info[signalNum].gpioBit >= 0)), \"Illegal signal\");\n"+
            "#endif\n"+
            "};\n\n"
            );
      
      writer.writeBanner("Peripheral Information Classes");

      DocumentationGroups groups = new DocumentationGroups(writer);
      for (String key:fDeviceInfo.getPeripherals().keySet()) {
         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         groups.openGroup(peripheral);
         peripheral.writeInfoClass(writer);
      }
      groups.closeGroup();
      writer.writeCloseNamespace();
      writer.write("\n");
   }

   /**
    * Write templates for simple peripheral functions (e.g. GPIO,ADC,PWM) mapped to locations  e.g.
    * 
    * <pre>
    *    using adc_p53              = const USBDM::Adc1&lt;4&gt;;
    *    using adc_p54              = const USBDM::Adc1&lt;5&gt;;
    * </pre>
    * 
    * @param peripheral       Peripheral information
    * @param mappedFunction   Information about the pin and function being declared
    * @param fnIndex          Index into list of functions mapped to pin
    * @param writer           Where to write
    * 
    * @throws IOException 
    */
   private String writeFunctionCTemplates(Peripheral peripheral, MappingInfo mappedFunction, int fnIndex) throws IOException {
      StringBuffer sb = null;
      
      if (!mappedFunction.isSelected()) {// && (mappedFunction.getMux()!=MuxSelection.mux1)) {
         return null;
      }
      String definition = peripheral.getDefinition(mappedFunction, fnIndex);
      if (definition == null) {
         return null;
      }
      String signalName = peripheral.getInstanceName(mappedFunction, fnIndex);
      String locations = fDeviceInfo.getDeviceVariant().getPackage().getLocation(mappedFunction.getPin());
      if ((locations != null) && (!locations.isEmpty())) {
         for (String location:locations.split("/")) {
            String aliasName = peripheral.getAliasName(signalName, location);
            if (aliasName!= null) {
               String declaration = peripheral.getAliasDeclaration(aliasName, mappedFunction, fnIndex);
               if (declaration != null) {
                  if (sb == null) {
                     sb = new StringBuffer();
                  }
                  if (!recordAlias(aliasName)) {
                     // Comment out repeated aliases
                     sb.append("//");
                  }
                  sb.append(declaration);
               }
            }
         }
      }
      if (sb == null) {
         return null;
      }
      return sb.toString();
   }

   private class DocumentationGroups {
      DocumentUtilities fWriter;
      
      public DocumentationGroups(DocumentUtilities writer) {
         fWriter = writer;
      }
      String groupName = null;
      public void openGroup(Peripheral peripheral) throws IOException {
         if (!peripheral.getGroupName().equals(groupName)) {
            if (groupName != null) {
               // Terminate previous group
               fWriter.writeCloseGroup();
            }
            groupName = peripheral.getGroupName();
            fWriter.writeStartGroup(peripheral);
         }
      }
      public void closeGroup() throws IOException {
         if (groupName != null) {
            // Terminate last group
            fWriter.writeCloseGroup();
         }
      }
   }
   
   private void writeIncludes(DocumentUtilities writer) throws IOException {
      if (fDeviceInfo.getPeripherals().containsKey("ADC0")||fDeviceInfo.getPeripherals().containsKey("ADC1")) {
         writer.writeHeaderFileInclude("adc.h");
      }
      if (fDeviceInfo.getPeripherals().containsKey("FTM0")||fDeviceInfo.getPeripherals().containsKey("FTM1")) {
         writer.writeHeaderFileInclude("ftm.h");
      }
      if (fDeviceInfo.getPeripherals().containsKey("TPM0")|fDeviceInfo.getPeripherals().containsKey("TPM1")) {
         writer.writeHeaderFileInclude("tpm.h");
      }
      writer.write("\n");
   }
   
   /**
    * Write C templates for peripherals and peripheral functions
    * 
    * <pre>
    *    /**
    *     * Convenience template class representing an ADC
    *     *
    *     * Example
    *     * @code
    *     *  // Instantiate ADC0 single-ended channel #8
    *     *  const adc0&lt;8&gt; adc0_se8;
    *     *
    *     *  // Initialise ADC
    *     *  adc0_se8.initialiseADC(USBDM::resolution_12bit_se);
    *     *
    *     *  // Set as analogue input
    *     *  adc0_se8.setAnalogueInput();
    *     *
    *     *  // Read input
    *     *  uint16_t value = adc0_se8.readAnalogue();
    *     *  @endcode
    *     *
    *     * @tparam adcChannel    ADC channel
    *     * /
    *    template&lt;uint8_t channel&gt; using Adc1 = Adc_T&lt;Adc1Info, channel&gt;;
    *    
    *    using adc_p53              = const USBDM::Adc1&lt;4&gt;;
    *    using adc_p54              = const USBDM::Adc1&lt;5&gt;;
    * </pre>
    * 
    * @param writer Where to write
    * 
    * @throws Exception 
    */
   private void writePeripheralCTemplates(DocumentUtilities writer) throws IOException {

      writeIncludes(writer);
      
      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);

      DocumentationGroups startGroup = new DocumentationGroups(writer);
      for (String key:fDeviceInfo.getPeripherals().keySet()) {

         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         String declaration = peripheral.getCTemplate();
         if (declaration != null) {
            startGroup.openGroup(peripheral);
            writer.write(declaration);
         }
         for (String pinName:fDeviceInfo.getPins().keySet()) {
            Pin pin = fDeviceInfo.getPins().get(pinName);
            Map<MuxSelection, MappingInfo> mappedFunctions = pin.getMappedSignals();
            if (mappedFunctions == null) {
               continue;
            }
            for (MuxSelection index:mappedFunctions.keySet()) {
               if (index == MuxSelection.reset) {
                  continue;
               }
               MappingInfo mappedFunction = mappedFunctions.get(index);
               for (int fnIndex=0; fnIndex<mappedFunction.getSignals().size(); fnIndex++) {
                  Signal function = mappedFunction.getSignals().get(fnIndex);
                  if (function.getPeripheral() == peripheral) {
                     String template = writeFunctionCTemplates(peripheral, mappedFunction, fnIndex);
                     if (template != null) {
                        startGroup.openGroup(peripheral);
                        writer.write(template);
                     }
                  }
               }
            }
         }
      }
      startGroup.closeGroup();
      writer.writeDocBanner("Used to configure pin-mapping before 1st use of peripherals");
      writer.write("extern void "+DO_PIN_MAPPING_FUNCTION+"();\n");
      writer.writeCloseNamespace();
   }

   /**
    * Write Pin Mapping function to CPP file
    * 
    * @param writer Where to write
    * 
    * @throws IOException
    */
   private void writePinMappingFunction(DocumentUtilities writer) throws IOException {

      writer.write(
            "struct PinInit {\n"+
                  "   uint32_t pcrValue;\n"+
                  "   uint32_t volatile *pcr;\n"+
                  "};\n\n"+
                  "static constexpr PinInit pinInit[] = {\n"
            );

      TreeSet<String> usedPcrs = new TreeSet<String>();
      for (String pinName:fDeviceInfo.getPins().keySet()) {
         if (fDeviceInfo.getDeviceVariant().getPackage().getLocation(pinName) == null) {
            // Discard pin that is not available on this package
            continue;
         }
         Pattern p = Pattern.compile("PT([A-Z]+)([0-9]+)");
         Matcher m = p.matcher(pinName);
         if (m.matches()) {
            Pin pin = fDeviceInfo.getPins().get(pinName);
            MuxSelection mux = pin.getMuxValue();
            if (!mux.isMappedValue()) {
               // Skip unused pin
               continue;
            }
            String instance = m.replaceAll("$1");
            String signal   = m.replaceAll("$2");
            usedPcrs.add(instance);
            writer.write(String.format(
                  " /* %-10s ==> %-25s */  { PORT_PCR_MUX(%d)|%s::DEFAULT_PCR, &PORT%s->%-8s },\n",
                  pin.getName(),
                  pin.getMappedSignal().getSignalList(),
                  mux.value, 
                  DeviceInfo.NAME_SPACE, 
                  instance, 
                  "PCR["+signal+"],"));
         }
      }
      writer.write("};\n\n");

      writer.write(
            "/**\n" + 
                  " * Used to configure pin-mapping before 1st use of peripherals\n" + 
                  " */\n" + 
                  "void "+DO_PIN_MAPPING_FUNCTION+"() {\n"
            );

      boolean maskWritten = false;
      for (String pcr:usedPcrs) {
         if (!maskWritten) {
            writer.write(String.format("\n   SIM->FIXED_PORT_CLOCK_REG |= PORT%s_CLOCK_MASK", pcr));
         }
         else {
            writer.write(String.format("|PORT%s_CLOCK_MASK", pcr));
         }
         maskWritten = true;
      }
      if (maskWritten) {
         writer.write(String.format(";\n"));
      }
      
      writer.write(
            "\n"+
                  "   for (const PinInit *p=pinInit; p<(pinInit+(sizeof(pinInit)/sizeof(pinInit[0]))); p++) {\n"+   
                  "      *(p->pcr) = p->pcrValue;\n"+ 
                  "   }\n"
            );
      writer.write("}\n");
   }

//   /**
//    * Checks if a function is mapped to a pin
//    * 
//    * @param peripheral
//    * @param mappedFunction
//    * 
//    * @return True if mapped.
//    */
//   private boolean isFunctionMappedToPin(Peripheral peripheral, MappingInfo mappedFunction) {
//      if (mappedFunction.getMux() == MuxSelection.fixed) {
//         // Fixed mapping are always available
//         return true;
//      }
//      Pin pin = mappedFunction.getPin();
//      return (pin.getMuxValue() == mappedFunction.getMux());
//   }

   
   private final String DOCUMENTATION_OPEN = 
         "/**\n"+
         " *\n"+
         " * @mainpage Summary\n";
      
   private final String TABLE_OPEN = 
         " *\n"+
         " * @section %s %s\n"+
         " *\n"+
         " *    Pin Name               |   Functions                                 |  Location                 |  Description  \n"+
         " *  ------------------------ | --------------------------------------------|---------------------------| ------------- \n";
      
   private final String DOCUMENTATION_TEMPLATE = 
      " *  %-20s     | %-40s    | %-21s     | %s       \n";
   
   private final String TABLE_CLOSE = 
         " *\n";
      
   private final String DOCUMENTATION_CLOSE = 
         " */\n";
      
   /**
    * Write pin mapping documentation 
    * 
    * @param writer Where to write
    * 
    * @throws IOException
    */
   private void writeDocumentation(DocumentUtilities writer) throws IOException {

      Map<String, Pin> pinsByLocation = new TreeMap<String, Pin>(Signal.comparator);
      Map<String, Pin> pinsByFunction = new TreeMap<String, Pin>(Signal.comparator);

      writer.write(DOCUMENTATION_OPEN);
      
      writer.write(String.format(TABLE_OPEN, "PinsByPinName", "Pins by Pin Name"));
      for (String pinName:fDeviceInfo.getPins().keySet()) {

         Pin pin = fDeviceInfo.getPins().get(pinName);

         if (!pin.isAvailableInPackage()) {
            // Discard pins without package location
            continue;
         }
         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         
         String signal = pin.getMappedSignals().get(pin.getMuxValue()).getSignalList();
         writer.write(String.format(DOCUMENTATION_TEMPLATE,
               pin.getName(), 
               signal,
               pin.getLocation(), 
               useDescription));
         if ((pin.getLocation() != null) && !pin.getLocation().isEmpty()) {
            pinsByLocation.put(pin.getLocation(), pin);
         }
         pinsByFunction.put(signal, pin);
      }
      writer.write(TABLE_CLOSE);
      writer.write(String.format(TABLE_OPEN, "PinsByLocation", "Pins by Location"));
      for (String pinName:pinsByLocation.keySet()) {
         
         Pin pin = pinsByLocation.get(pinName);
         
         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         String function = pin.getMappedSignals().get(pin.getMuxValue()).getSignalList();
         writer.write(String.format(DOCUMENTATION_TEMPLATE,
               pin.getName(), 
               function,
               pin.getLocation(), 
               useDescription));
         
      }
      writer.write(TABLE_CLOSE);
      writer.write(String.format(TABLE_OPEN, "PinsByFunction", "Pins by Function"));
      for (String pinName:pinsByFunction.keySet()) {
         
         Pin pin = pinsByFunction.get(pinName);
         
         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         writer.write(String.format(DOCUMENTATION_TEMPLATE,
               pin.getName(), 
               pin.getMappedSignals().get(pin.getMuxValue()).getSignalList(),
               pin.getLocation(), 
               useDescription));
      }
      writer.write(TABLE_CLOSE);
      writer.write(DOCUMENTATION_CLOSE);
   }
   
   /**
    * Writes pin mapping header file
    * 
    * @param filePath Header file to write to
    * 
    * @throws IOException 
    */
   private void writePinMappingHeaderFile(Path filePath) throws IOException {
      
      aliases = null;

      BufferedWriter headerFile = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(headerFile);
      
      writer.writeHeaderFilePreamble(
            PIN_MAPPING_BASEFILENAME+".h", fDeviceInfo.getSourceFilename(),
            DeviceInfo.VERSION, 
            "Pin declarations for "+fDeviceInfo.getDeviceVariantName());

      writer.writeSystemHeaderFileInclude("stddef.h");
      writer.writeHeaderFileInclude("derivative.h");
      headerFile.write("\n");
      writer.writeHeaderFileInclude("gpio.h");
      headerFile.write("\n");

      writePinDefines(writer);
      writeClockMacros(writer);
      writePeripheralInformationClasses(writer);

      writePeripheralCTemplates(writer);

      writeDocumentation(writer);
      
      writer.writeHeaderFilePostamble(PIN_MAPPING_BASEFILENAME+".h");

      writer.close();
   }

   /**                    
    * Write CPP file      
    *                     
    * @param path      Path to file for writing
    * 
    * @throws IOException 
    */                    
   private void writePinMappingCppFile(Path path) throws IOException {
      BufferedWriter cppFile = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(cppFile);
      
      writer.writeCppFilePreamble(
            HARDWARE_BASEFILENAME+".cpp", fDeviceInfo.getSourceFilename(),
            DeviceInfo.VERSION, 
            "Pin declarations for "+fDeviceInfo.getDeviceVariantName());


      writer.writeHeaderFileInclude(HARDWARE_BASEFILENAME+".h");
      writer.write("\n");

      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE);
      writePinMappingFunction(writer);
      writer.writeCppFilePostAmble();
      writer.writeCloseNamespace();
      writer.close();
   }

   /**
    * Generate CPP files (pin_mapping.h, gpio.h)<br>
    * Used for testing
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
      writePinMappingHeaderFile(directory.resolve("Project_Headers").resolve(PIN_MAPPING_BASEFILENAME+filename+".h"));
      writePinMappingCppFile(directory.resolve("Sources").resolve(HARDWARE_BASEFILENAME+filename+".cpp"));
   }
   
   /**
    * Generate CPP files (pin_mapping.h, gpio.h) for all variants<br>
    * For testing 
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

   /**
    * Generate CPP files (pin_mapping.h, gpio.h) within an Eclipse C++ project
    * 
    * @param project       Destination project 
    * @param  deviceInfo   Device information to print to CPP files  
    * 
    * @throws IOException
    * @throws CoreException 
    */
   public void writeCppFiles(IProject project, DeviceInfo deviceInfo, IProgressMonitor mon) throws IOException, CoreException {
      
      fDeviceInfo = deviceInfo;

      mon.beginTask("Generating files", IProgressMonitor.UNKNOWN);
      
      Path directory = Paths.get(project.getLocation().toPortableString());
      
      writePinMappingHeaderFile(directory.resolve(INCLUDE_DIRECTORY).resolve(PIN_MAPPING_BASEFILENAME+".h"));
      writePinMappingCppFile(directory.resolve(SOURCE_DIRECTORY).resolve(HARDWARE_BASEFILENAME+".cpp"));
      
      IFile file;
      
      file = project.getFile(INCLUDE_DIRECTORY+"/"+PIN_MAPPING_BASEFILENAME+".h");
      file.refreshLocal(IResource.DEPTH_ONE, mon);
      file.setDerived(true, mon);
      
      file = project.getFile(SOURCE_DIRECTORY+"/"+HARDWARE_BASEFILENAME+".cpp");
      file.refreshLocal(IResource.DEPTH_ONE, mon);
      file.setDerived(true, mon);
   }

}
