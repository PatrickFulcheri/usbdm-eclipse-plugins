package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.MuxSelection;
import net.sourceforge.usbdm.deviceEditor.information.PcrInitialiser;
import net.sourceforge.usbdm.deviceEditor.information.Pin;
import net.sourceforge.usbdm.deviceEditor.information.Signal;

public class WriteFamilyCpp {

   /** Device information */
   private DeviceInfo fDeviceInfo;

   /** Include directory in C Project */
   private final static String INCLUDE_DIRECTORY = "Project_Headers";

   /** Source directory ion C project */
   private final static String SOURCE_DIRECTORY = "Sources";

   /** Base name for pin mapping file */
   private final static String PIN_MAPPING_BASEFILENAME = "pin_mapping";

   /** Base name for C++ files */
   private final static String HARDWARE_BASEFILENAME = "hardware";

   /** Name of function to do pin mapping */
   private static final String DO_PIN_MAPPING_FUNCTION = "mapAllPins";

   /*
    * Macros
    * ==========================================================================
    * ===================
    */
   HashSet<String> aliases = null;

   /**
    * Records aliases used
    * 
    * @param aliasName
    *           Alias to record
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
    * Write Peripheral Information Class<br>
    * 
    * <pre>
    *  class Adc0Info {
    *   ...
    *  };
    *  class Adc1Info {
    *   ...
    *  };
    * </pre>
    * 
    * @param writer
    *           Where to write
    * 
    * @throws IOException
    */
   private void writePeripheralInformationClass(DocumentUtilities writer, DocumentationGroups groups, Peripheral peripheral) throws IOException {
      try {
         groups.openGroup(peripheral);
         peripheral.writeInfoClass(writer);
      } catch (Exception e) {
         System.err.println("Failed to write Info for peripheral " + peripheral);
         e.printStackTrace();
      }
      writer.flush();
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
    * 
    * @param writer
    *           Where to write
    * 
    * @throws IOException
    */
   private void writePeripheralInformationClasses(DocumentUtilities writer) throws IOException {
      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE, "Namespace enclosing USBDM classes");

      writer.openUsbdmDocumentationGroup();

//      writePortInfo(writer);

      fDeviceInfo.writeNamespaceInfo(writer);

      writer.writeBanner("Peripheral Information Classes");

      DocumentationGroups groups = new DocumentationGroups(writer);

      // Write these classes in order as they declare dependent information etc.
      ArrayList<String> priorityClasses = new ArrayList<String>();
      priorityClasses.add("OSC");
      priorityClasses.add("OSC0");
      priorityClasses.add("OSC_RF0");
      priorityClasses.add("SCG");
      priorityClasses.add("PCC");
      priorityClasses.add("RTC");
      priorityClasses.add("MCG");
      priorityClasses.add("SIM");
      for (String key : priorityClasses) {
         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         if (peripheral != null) {
            writePeripheralInformationClass(writer, groups, peripheral);
         }
      }
      for (String key : fDeviceInfo.getPeripherals().keySet()) {
         if (priorityClasses.contains(key)) {
            continue;
         }
         writePeripheralInformationClass(writer, groups, fDeviceInfo.getPeripherals().get(key));
      }
      groups.closeGroup();
      writer.closeDocumentationGroup();
      writer.writeCloseNamespace();
      writer.write("\n");
      writer.flush();
   }

   /**
    * Get declarations for simple peripheral signals (e.g. GPIO,ADC,PWM) that
    * are mapped to pins e.g.
    * 
    * <pre>
    *    using adc_p53              = const USBDM::Adc1&lt;4&gt;;
    *    using adc_p54              = const USBDM::Adc1&lt;5&gt;;
    * </pre>
    * 
    * @param peripheral
    *           Peripheral information
    * @param mappedSignal
    *           Information about the mapped signal being declared
    * @param fnIndex
    *           Index into list of multiple functions mapped to pin
    * 
    * @throws IOException
    */
   private String getMappedSignals(Peripheral peripheral, MappingInfo mappedSignal, int fnIndex) throws IOException {
      StringBuffer sb = null;

      if (!mappedSignal.isSelected()) {// &&
                                       // (mappedFunction.getMux()!=MuxSelection.mux1))
                                       // {
         return null;
      }
      String definition = peripheral.getDefinition(mappedSignal, fnIndex);
      if (definition == null) {
         return null;
      }
      String signalName = peripheral.getInstanceName(mappedSignal, fnIndex);
      String locations = fDeviceInfo.getVariant().getPackage().getLocation(mappedSignal.getPin());
      if ((locations != null) && (!locations.isEmpty())) {
         for (String location : locations.split("/")) {
            String aliasName = peripheral.getAliasName(signalName, location);
            if (aliasName != null) {
               String declaration = peripheral.getAliasDeclaration(aliasName, mappedSignal, fnIndex);
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
               fWriter.closeDocumentationGroup();
            }
            groupName = peripheral.getGroupName();
            fWriter.openDocumentationGroup(peripheral);
         }
      }

      public void closeGroup() throws IOException {
         if (groupName != null) {
            // Terminate last group
            fWriter.closeDocumentationGroup();
         }
      }
   }

   /**
    * Writes #includes for classes that have simple signal declarations e.g.
    * ADC, GPIO etc.<br>
    * 
    * Example:
    * 
    * <pre>
    *    #include "adc.h"
    *    #include "ftm.h"
    * </pre>
    * 
    * @param writer
    * @throws IOException
    */
   private void writeIncludes(DocumentUtilities writer) throws IOException {
      if (fDeviceInfo.getPeripherals().containsKey("ADC0") || fDeviceInfo.getPeripherals().containsKey("ADC1")) {
         writer.writeHeaderFileInclude("adc.h");
      }
      if (fDeviceInfo.getPeripherals().containsKey("FTM0") || fDeviceInfo.getPeripherals().containsKey("FTM1")) {
         writer.writeHeaderFileInclude("ftm.h");
      }
      if (fDeviceInfo.getPeripherals().containsKey("TPM0") | fDeviceInfo.getPeripherals().containsKey("TPM1")) {
         writer.writeHeaderFileInclude("tpm.h");
      }
      writer.writeHeaderFileInclude("gpio.h");
      writer.write("\n");
   }

   /**
    * Write declarations for simple peripheral signals (e.g. GPIO,ADC,PWM) that
    * are mapped to pins e.g.
    * 
    * <pre>
    *    using adc_p53              = const USBDM::Adc1&lt;4&gt;;
    *    using adc_p54              = const USBDM::Adc1&lt;5&gt;;
    * </pre>
    * 
    * @param writer
    *           Where to write
    * 
    * @throws Exception
    */
   private void writeMappedSignals(DocumentUtilities writer) throws IOException {

      writeIncludes(writer);

      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE, "Namespace enclosing USBDM classes");
      writer.openUsbdmDocumentationGroup();

      DocumentationGroups startGroup = new DocumentationGroups(writer);
      for (String key : fDeviceInfo.getPeripherals().keySet()) {
         Peripheral peripheral = fDeviceInfo.getPeripherals().get(key);
         for (Entry<String, Pin> pinEntry : fDeviceInfo.getPins().entrySet()) {
            Pin pin = pinEntry.getValue();
            Map<MuxSelection, MappingInfo> mappedSignals = pin.getMappableSignals();
            if (mappedSignals == null) {
               continue;
            }
            for (Entry<MuxSelection, MappingInfo> muxEntry : mappedSignals.entrySet()) {
               if (muxEntry.getKey() == MuxSelection.unassigned) {
                  continue;
               }
               MappingInfo mappedSignal = muxEntry.getValue();
               for (int fnIndex = 0; fnIndex < mappedSignal.getSignals().size(); fnIndex++) {
                  Signal function = mappedSignal.getSignals().get(fnIndex);
                  if (function.getPeripheral() == peripheral) {
                     String template = getMappedSignals(peripheral, mappedSignal, fnIndex);
                     if (template != null) {
                        startGroup.openGroup(peripheral);
                        writer.write(template);
                        writer.flush();
                     }
                  }
               }
            }
         }
      }
      startGroup.closeGroup();
      writer.closeDocumentationGroup();
      writer.writeCloseNamespace();
      writer.flush();
   }

   /**
    * Write Central Pin Mapping function to CPP file
    * 
    * <pre>
    * void mapAllPins() {
    *    enablePortClocks(PORTA_CLOCK_MASK|...);
    * 
    *    PORTA->GPCHR = 0x0000UL|PORT_GPCHR_GPWE(0x000CUL);
    *    ...
    *    PORTE->GPCHR = 0x0500UL|PORT_GPCHR_GPWE(0x0300UL);
    * }
    * </pre>
    * 
    * @param writer     Where to write (CPP file)
    * 
    * @throws Exception
    */
   private void writePinMappingFunction(DocumentUtilities writer) throws Exception {
      PcrInitialiser pcrInitialiser = new PcrInitialiser();

      for (String pinName : fDeviceInfo.getPins().keySet()) {
         Pin pin = fDeviceInfo.getPins().get(pinName);
         pcrInitialiser.addPin(pin);
      }
      writer.write("/**\n" + " * Used to configure pin-mapping before 1st use of peripherals\n" + " */\n" + "void " + DO_PIN_MAPPING_FUNCTION + "() {\n");
      writer.write(pcrInitialiser.getInitPortClocksStatement(""));
      writer.write(pcrInitialiser.getPcrInitStatements(""));
      writer.write("}\n");
   }

   private final String DOCUMENTATION_OPEN = "/**\n" + " *\n" + " * @page PinSummary Pin Mapping\n";

   private final String TABLE_OPEN = " *\n" + " * @section %s %s\n" + " *\n" + " *    Pin Name               |   Functions                                 |  Location                 |  Description  \n" + " *  ------------------------ | --------------------------------------------|---------------------------| ------------- \n";

   private final String DOCUMENTATION_TEMPLATE = " *  %-20s     | %-40s    | %-21s     | %s       \n";

   private final String TABLE_CLOSE = " *\n";

   private final String DOCUMENTATION_CLOSE = " */\n";

   /**
    * Write pin mapping documentation
    * 
    * @param writer
    *           Where to write
    * 
    * @throws IOException
    */
   private void writeDocumentation(DocumentUtilities writer) throws IOException {

      Map<String, Pin> pinsByLocation = new TreeMap<String, Pin>(Signal.comparator);
      Map<String, Pin> pinsByFunction = new TreeMap<String, Pin>(Signal.comparator);

      writer.write(DOCUMENTATION_OPEN);

      writer.write(String.format(TABLE_OPEN, "PinsByPinName", "Pins by Pin Name"));
      for (String pinName : fDeviceInfo.getPins().keySet()) {

         Pin pin = fDeviceInfo.getPins().get(pinName);

         if (!pin.isAvailableInPackage()) {
            // Discard pins without package location
            continue;
         }
         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         Map<MuxSelection, MappingInfo> mappableSignals = pin.getMappableSignals();
         MappingInfo mappedSignal = mappableSignals.get(pin.getMuxValue());
         String signalList;
         if (mappedSignal == null) {
            signalList = "-";
         } else {
            signalList = mappedSignal.getSignalList();
         }
         writer.write(String.format(DOCUMENTATION_TEMPLATE, pin.getName(), signalList, pin.getLocation(), useDescription));
         if ((pin.getLocation() != null) && !pin.getLocation().isEmpty()) {
            pinsByLocation.put(pin.getLocation(), pin);
         }
         pinsByFunction.put(signalList, pin);
      }
      writer.write(TABLE_CLOSE);
      writer.write(String.format(TABLE_OPEN, "PinsByLocation", "Pins by Location"));
      for (String pinName : pinsByLocation.keySet()) {

         Pin pin = pinsByLocation.get(pinName);

         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         Map<MuxSelection, MappingInfo> mappableSignals = pin.getMappableSignals();
         MappingInfo mappedSignal = mappableSignals.get(pin.getMuxValue());
         String signalList;
         if (mappedSignal == null) {
            signalList = "-";
         } else {
            signalList = mappedSignal.getSignalList();
         }
         writer.write(String.format(DOCUMENTATION_TEMPLATE, pin.getName(), signalList, pin.getLocation(), useDescription));

      }
      writer.write(TABLE_CLOSE);
      writer.write(String.format(TABLE_OPEN, "PinsByFunction", "Pins by Function"));
      for (String pinName : pinsByFunction.keySet()) {

         Pin pin = pinsByFunction.get(pinName);

         String useDescription = pin.getPinUseDescription();
         if (useDescription.isEmpty()) {
            useDescription = "-";
         }
         Map<MuxSelection, MappingInfo> mappableSignals = pin.getMappableSignals();
         MappingInfo mappedSignal = mappableSignals.get(pin.getMuxValue());
         String signalList;
         if (mappedSignal == null) {
            signalList = "-";
         } else {
            signalList = mappedSignal.getSignalList();
         }
         writer.write(String.format(DOCUMENTATION_TEMPLATE, pin.getName(), signalList, pin.getLocation(), useDescription));
      }
      writer.write(TABLE_CLOSE);
      writer.write(DOCUMENTATION_CLOSE);
      writer.flush();
   }

   /**
    * Writes pin mapping header file
    * 
    * @param filePath
    *           Header file to write to
    * 
    * @throws IOException
    */
   private void writePinMappingHeaderFile(Path filePath) throws IOException {

      aliases = null;

      BufferedWriter headerFile = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(headerFile);

      writer.writeHeaderFilePreamble(PIN_MAPPING_BASEFILENAME + ".h", fDeviceInfo.getSourceFilename(), DeviceInfo.VERSION, "Pin declarations for " + fDeviceInfo.getVariantName());

      writer.writeSystemHeaderFileInclude("stddef.h");
      writer.writeHeaderFileInclude("derivative.h");
      headerFile.write("\n");
      writer.writeHeaderFileInclude("pcr.h");
      headerFile.write("\n");

      writePeripheralInformationClasses(writer);

      writeMappedSignals(writer);

      writeDocumentation(writer);

      writer.writeHeaderFilePostamble(PIN_MAPPING_BASEFILENAME + ".h");

      writer.close();
   }

   /**
    * Write CPP file
    * 
    * @param path
    *           Path to file for writing
    * @throws Exception
    */
   private void writePinMappingCppFile(Path path) throws Exception {
      BufferedWriter cppFile = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
      DocumentUtilities writer = new DocumentUtilities(cppFile);

      writer.writeCppFilePreamble(HARDWARE_BASEFILENAME + ".cpp", fDeviceInfo.getSourceFilename(), DeviceInfo.VERSION, "Pin initialisation for " + fDeviceInfo.getVariantName());

      writer.writeHeaderFileInclude(HARDWARE_BASEFILENAME + ".h");
      writer.write("\n");

      writer.writeOpenNamespace(DeviceInfo.NAME_SPACE, "Namespace enclosing USBDM classes");
      writer.openUsbdmDocumentationGroup();
      writePinMappingFunction(writer);
      writer.writeCppFilePostAmble();
      writer.closeDocumentationGroup();
      writer.writeCloseNamespace();
      writer.close();
   }

   /**
    * Generate CPP files (pin_mapping.h, gpio.h)<br>
    * Used for testing
    * 
    * @param directory
    *           Parent director
    * @param filename
    *           Filename to use as base of files written
    * @param deviceInfo
    *           Device information to print to CPP files
    * @throws Exception
    */
   public void writeCppFiles(Path directory, String filename, DeviceInfo deviceInfo) throws Exception {
      if (!filename.isEmpty()) {
         filename = "-" + filename;
      }
      fDeviceInfo = deviceInfo;
      writePinMappingHeaderFile(directory.resolve("Project_Headers").resolve(PIN_MAPPING_BASEFILENAME + filename + ".h"));
      writePinMappingCppFile(directory.resolve("Sources").resolve(HARDWARE_BASEFILENAME + filename + ".cpp"));
   }

   /**
    * Generate CPP files (pin_mapping.h, gpio.h) for all variants<br>
    * For testing
    * 
    * @param directory
    *           Parent director
    * @param deviceInfo
    *           Device information to print to CPP files
    * @throws Exception
    */
   public void writeCppFiles(Path directory, DeviceInfo deviceInfo) throws Exception {
      for (String key : deviceInfo.getDeviceVariants().keySet()) {
         deviceInfo.setVariantName(key);
         writeCppFiles(directory, deviceInfo.getVariantName(), deviceInfo);
      }
   }

   /**
    * Generate CPP files (pin_mapping.h, gpio.h) within an Eclipse C++ project
    * 
    * @param project       Destination project 
    * @param  deviceInfo   Device information to print to CPP files  
    * @throws Exception 
    */
   public void writeCppFiles(IProject project, DeviceInfo deviceInfo, IProgressMonitor monitor) throws Exception {
      final String pinMappingFile = INCLUDE_DIRECTORY+"/"+PIN_MAPPING_BASEFILENAME+".h";
      final String hardwareFile   = SOURCE_DIRECTORY+"/"+HARDWARE_BASEFILENAME+".cpp";
      
      SubMonitor subMonitor = SubMonitor.convert(monitor, 100); 
      subMonitor.subTask("Generating device header file");

      fDeviceInfo = deviceInfo;
      
      Path directory = Paths.get(project.getLocation().toPortableString());
      
      writePinMappingHeaderFile(directory.resolve(pinMappingFile));
      subMonitor.worked(40);
      
      writePinMappingCppFile(directory.resolve(hardwareFile));
      subMonitor.worked(40);
      
      try {
         IFile file = project.getFile(pinMappingFile);
//         file.refreshLocal(IFile.DEPTH_ZERO, subMonitor.newChild(10));
         file.setDerived(true, subMonitor.newChild(10));
      } catch (Exception e) {
         // Ignore
         System.err.println("WARNING: Failed to refresh" + pinMappingFile);
      }
      
      try {
         IFile file = project.getFile(hardwareFile);
//         file.refreshLocal(IFile.DEPTH_ZERO, subMonitor.newChild(10));
         file.setDerived(true, subMonitor.newChild(10));
      } catch (Exception e) {
         // Ignore
         System.err.println("WARNING: Failed to refresh" + hardwareFile);
      }
   }

}
