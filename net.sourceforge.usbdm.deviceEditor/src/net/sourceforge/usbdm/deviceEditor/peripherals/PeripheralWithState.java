package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.FileUtility;
import net.sourceforge.usbdm.deviceEditor.information.FileUtility.IKeyMaker;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.Variable;
import net.sourceforge.usbdm.deviceEditor.model.BaseModel;
import net.sourceforge.usbdm.deviceEditor.model.IModelChangeListener;
import net.sourceforge.usbdm.deviceEditor.model.IModelEntryProvider;
import net.sourceforge.usbdm.deviceEditor.model.ObservableModel;
import net.sourceforge.usbdm.deviceEditor.validators.Validator;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML.CodeTemplate;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML.Data;
import net.sourceforge.usbdm.deviceEditor.xmlParser.XmlDocumentUtilities;
import net.sourceforge.usbdm.peripheralDatabase.InterruptEntry;
import net.sourceforge.usbdm.peripheralDatabase.VectorTable;

public abstract class PeripheralWithState extends Peripheral implements IModelEntryProvider, IModelChangeListener {
   
   public static final String IRQ_HANDLER_INSTALLED_SYMBOL = "irqHandlerInstalled";

   /** Data about model loaded from file */
   protected Data fData = null;
   
   /** Map of parameters for peripheral */
   protected HashMap<String, String> fParamMap = new HashMap<String,String>();

   /** Map of parameters for peripheral */
   protected HashMap<String, String> fConstantMap = new HashMap<String,String>();

   protected PeripheralWithState(String basename, String instance, DeviceInfo deviceInfo) {
      super(basename, instance, deviceInfo);
   }

   private final class KeyMaker implements IKeyMaker {
      @Override
      public String makeKey(String name) {
         if (name.charAt(0) == '/') {
            return name;
         }
         return "/"+getName()+"/"+name;
      }
   }
   
   private final class IndexKeyMaker implements IKeyMaker {
//      private final String fIndex;
      
      public IndexKeyMaker(int index) {
//         fIndex = "[" + index + "]";
      }
      @Override
      public String makeKey(String name) {
         if (name.charAt(0) == '/') {
            return name;
         }
         return "/"+getName()+"/"+name;
      }
   }
   
   private final KeyMaker keyMaker = new KeyMaker();

   /**
    * Get key for variable owned by this peripheral
    * 
    * @param name
    * @return
    */
   public String makeKey(String name) {
      return keyMaker.makeKey(name);
   }

   /**
    * Load the models and validators for this class of peripheral
    * 
    * @return
    * @throws Exception 
    */
   public void loadModels() throws Exception {
      fData = loadModels(getPeripheralModelName());
      if (fData == null) {
         return;
      }
      for (ParseMenuXML.Validator v:fData.fValidators) {
         try {
            String className = v.getClassName();
            // Get validator class
            Class<?> clazz = Class.forName(className);
            Validator validatorClass = (Validator) clazz.getConstructor(PeripheralWithState.class, v.getParams().getClass()).newInstance(this, v.getParams());
            addValidator(validatorClass);
         } catch (Exception e) {
            throw new Exception("Failed to add validator "+v.getClassName()+" for Peripheral " + getName(), e);
         }
      }
   }

   /**
    * Load the models for this class of peripheral
    * 
    * @return
    * @throws Exception 
    */
   /**
    * 
    * @param modelName  Name of the model for this peripheral. Used as name of file to load.
    * 
    * @return 
    * @throws Exception
    */
   private final Data loadModels(String modelName) throws Exception {
      try {
         return ParseMenuXML.parseFile(modelName, null, this);
      } catch (Exception e) {
         throw new Exception("Failed to load model "+modelName+" for Peripheral " + getName(), e);
      }
   }
   
   @Override
   public BaseModel getModels(BaseModel parent) {
      return fData.fRootModel;
   }

   @Override
   public void writeInfoConstants(DocumentUtilities pinMappingHeaderFile) throws IOException {
      super.writeInfoConstants(pinMappingHeaderFile);
      writeInfoTemplate(pinMappingHeaderFile);
   }

   public void writeInfoTemplate(DocumentUtilities pinMappingHeaderFile) throws IOException {
      pinMappingHeaderFile.write("   // Template:" + getPeripheralModelName()+"\n\n");
      if (fData == null) {
//         System.err.println("No fData for " + getName());
         return;
      }
//      System.err.println("fData for " + getName());
      CodeTemplate template = fData.fTemplate.get("");
      if (template != null) {
         //TODO - add dimension
//         Variable dimension = template.getDimension();
//         if (dimension != null) {
//            int dim = (int)dimension.getValueAsLong();
//            for (int index=0; index<dim; index++) {
//               pinMappingHeaderFile.write(substitute(template.getTemplate()));
//            }
//         }
//         else {
         pinMappingHeaderFile.write(substitute(template.getTemplate()));
//         }
      }
   }
   
   /**
    * @param processProjectActions 
    * @param project
    * @param monitor
    * 
    * @throws Exception
    */
   public void regenerateProjectFiles(ProcessProjectActions processProjectActions, IProject project, IProgressMonitor monitor) throws Exception {
      Map<String, String> symbolMap = addTemplatesToSymbolMap(fDeviceInfo.getSimpleSymbolMap());
      processProjectActions.process(project, fData.fProjectActionList, symbolMap, monitor);
   }

   /**
    * Add extra templates to symbol map before doing other substitutions
    * 
    * @param map  Map to symbols add to
    *  
    * @return Modified map
    */
   protected Map<String, String> addTemplatesToSymbolMap(Map<String, String> map) {
      map.put("_instance", getInstance());
      map.put("_name",     getName());
      
      // Load any named templates
      for (String key:fData.fTemplate.keySet()) {
         if (key.isEmpty()) {
            continue;
         }
         CodeTemplate fileTemplate = fData.fTemplate.get(key);

         // Final template after substitutions
         String substitutedTemplate = null;
         
         // Check for dimension
         Variable dimension = fileTemplate.getDimension();
         if (dimension != null) {
            StringBuffer sb = new StringBuffer();
            int dim = (int)dimension.getValueAsLong();
            for (int index=0; index<dim; index++) {
               sb.append(FileUtility.substitute(fileTemplate.getTemplate(), map, new IndexKeyMaker(index)));
            }
            substitutedTemplate = sb.toString();
         }
         else {
            substitutedTemplate = FileUtility.substitute(fileTemplate.getTemplate(), map, keyMaker);
         }
         map.put(keyMaker.makeKey(key), substitutedTemplate);
      }
      return map;
   }
   
   /**
    * Create a variable
    * 
    * @param key     Key identifying variable
    * @param value   Initial value for variable
    * 
    * @throws Exception if variable already exists
    */
   public void addVariable(Variable variable) {
      fDeviceInfo.addVariable(variable.getKey(), variable);
      variable.addListener(this);
   }

   @Override
   public void setVariableValue(String key, String value) {
      fDeviceInfo.setVariableValue(key, value);
   }

   @Override
   public String getVariableValue(String key) throws Exception {
      try {
         return fDeviceInfo.getVariableValue(key);
      } catch (Exception e) {
         throw new Exception("Variable error in peripheral "+getName(), e);
      }
   }

   @Override
   public Variable getVariable(String key) throws Exception {
      try {
         return fDeviceInfo.getVariable(key);
      } catch (Exception e) {
         throw new Exception("Variable error in peripheral "+getName()+", var="+key, e);
      }
   }

   @Override
   public Variable safeGetVariable(String key) {
      try {
         return fDeviceInfo.getVariable(key);
      } catch (Exception e) {
         return null;
      }
   }

   /**
    * Does variable substitution in a string
    * 
    * @param input   String to process
    * @param map     Map of key->replacement values
    * 
    * @return Modified string or original if no changes
    * @throws Exception 
    */
   public String substitute(String input, Map<String, String> map) {
      return FileUtility.substitute(input, map, keyMaker);
   }

   /**
    * Does variable substitution in a string using the device variable map
    * 
    * @param input  String to process
    * 
    * @return Modified string or original if no changes
    */
   String substitute(String input) {
      Map<String, String> map = fDeviceInfo.getSimpleSymbolMap();
      map.put(makeKey("_instance"), getInstance());
      map.put(makeKey("_name"),     getName());
      return substitute(input, map);
   }
   
   /**
    * Checks if a variable is true when interpreted as a C value <br>
    * i.e. non-zero or "true"
    * 
    * @param key to Access variable
    * 
    * @return
    */
   public boolean isCTrueValue(String key) {
      String value = null;
      try {
         value = getVariable(keyMaker.makeKey(key)).getSubstitutionValue();
      } catch (Exception e1) {
      }
      if (value == null) {
         return false;
      }
      try {
         return Long.decode(value) != 0;
      }
      catch (NumberFormatException e){
      }
      return value.equalsIgnoreCase("true");
   }

   /**
    * Search vector table for handler and replace with class static method name.<br>
    * Uses class name to create handler name<br>
    * By default, matches any handler starting with the peripheral name e.g. FTM0<br> 
    * and replaces with class name e.g. <b>FTM0_IRQHandler</b> => <b>USBDM::Ftm0::irqHandler</b><br>
    * Overridden to do special replacement
    * 
    * @param vectorTable  Vector table to search
    */
   @Override
   public void modifyVectorTable(VectorTable vectorTable) {
      modifyVectorTable(vectorTable, "^"+fName+"((\\d+)?).*");
   }

   /**
    * Search vector table for handler and replace with class static method name.<br>
    * 
    * @param vectorTable  Vector table to search
    * @param pattern      Pattern to match against handler name
    */
   public void modifyVectorTable(VectorTable vectorTable, String pattern) {
      modifyVectorTable(vectorTable, pattern, getClassName());
   }

   /**
    * Search vector table for handler and replace with class static method name
    * 
    * @param vectorTable  Vector table to search
    * 
    * @param pattern      Pattern to look for e.g. "^FTM((\\d+)?).*". 
    *                     Must contain 1 group which is preserved
    * @param className    Base name of handler, usually class name e.g. Ftm2 
    */
   public void modifyVectorTable(VectorTable vectorTable, String pattern, String className) {
      if (!isCTrueValue(IRQ_HANDLER_INSTALLED_SYMBOL)) {
         return;
      }
      final String headerFileName = getBaseName().toLowerCase()+".h";
      boolean handlerSet = false;
      Pattern p = Pattern.compile(pattern);
      for (InterruptEntry entry:vectorTable.getEntries()) {
         if (entry != null) {
            Matcher m = p.matcher(entry.getName());
            if (m.matches()) {
               entry.setHandlerName(DeviceInfo.NAME_SPACE+"::"+className+"::irq"+m.group(1)+"Handler");
               entry.setClassMemberUsedAsHandler(true);
               handlerSet = true;
            }
         }
         if (handlerSet) {
            // Add include file
            vectorTable.addIncludeFile(headerFileName);
         }
      }
   }
   
   ArrayList<Validator> validators = new ArrayList<Validator>();
   
   public void addValidator(Validator validator) {
      validators.add(validator);
   }
   
   public void variableChanged(Variable variable) {
//      System.err.println("variableChanged()" + variable.toString());
      fDeviceInfo.setDirty(true);
      for (Validator v:validators) {
         v.variableChanged(variable);
      }
   }
   
   @Override
   public void modelElementChanged(ObservableModel observableModel) {
      if (observableModel instanceof Variable) {
         variableChanged((Variable)observableModel);
      }
   }

   @Override
   public void modelStructureChanged(ObservableModel observableModel) {
   }
   
   @Override
   public void elementStatusChanged(ObservableModel observableModel) {
   }

   /**
    * Get priority - a number used to order the instantiation of peripherals
    * 
    * @return Priority, larger is higher priority.
    */
   public int getPriority() {
      return 0;
   }

   @Override
   public String toString() {
      return this.getClassName()+"("+getName()+", "+getPeripheralModelName()+")";
   }

   /**
    * Add parameter
    * 
    * @param key
    * @param value
    */
   public void addParam(String key, String value) {
      fParamMap.put(key, value);
   }

   /**
    * Get parameter map
    * 
    * @return
    */
   public Map<String, String> getParamMap() {
      return fParamMap;
   }
   
   /**
    * Get parameter value
    * 
    * @param key Key to use for parameter<br>
    * If the key starts at root it is used unchanged otherwise the peripheral name will be pre-pended.<br>
    * e.g. xxx => /ftfl/xxx, /xxx => /xxx (unchanged)
    *  
    * @return parameter value or null if not present
    */
   public String getParam(String key) {
      if (!key.startsWith("/")) {
         key = "/"+getName()+"/"+key;
      }
      return fParamMap.get(key);
   }
   
   /**
    * Add constant
    * 
    * @param key
    * @param value
    */
   public void addConstant(String key, String value) {
      fConstantMap.put(key, value);
   }

   /**
    * Get constant map
    * 
    * @return
    */
   public Map<String, String> getConstantMap() {
      return fConstantMap;
   }
   
   @Override
   protected void writeExtraDefinitions(XmlDocumentUtilities documentUtilities) throws IOException {
      for (String key:getParamMap().keySet()) {
         String value = getParamMap().get(key);
         documentUtilities.openTag("param");
         documentUtilities.writeAttribute("key",   key);
         documentUtilities.writeAttribute("value", value);
         documentUtilities.closeTag();
      }
   }

   
   /**
    * Gets the model name for the peripheral<br>
    * Defaults to name based on peripheral e.g. ftm<br>
    * May be overridden by <b><i>peripheral_file</i></b> parameter from device file
    */
   public String getPeripheralModelName() {
      String peripheralFile = getParam("peripheral_file");
      if (peripheralFile != null) {
         return peripheralFile;
      }
      return super.getPeripheralModelName();
   }

}
