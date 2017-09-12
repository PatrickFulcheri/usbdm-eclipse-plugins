package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.FileUtility;
import net.sourceforge.usbdm.deviceEditor.information.IrqVariable;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.Variable;
import net.sourceforge.usbdm.deviceEditor.model.BaseModel;
import net.sourceforge.usbdm.deviceEditor.model.IModelChangeListener;
import net.sourceforge.usbdm.deviceEditor.model.IModelEntryProvider;
import net.sourceforge.usbdm.deviceEditor.model.ObservableModel;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML.CodeTemplate;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseMenuXML.MenuData;
import net.sourceforge.usbdm.deviceEditor.xmlParser.XmlDocumentUtilities;
import net.sourceforge.usbdm.peripheralDatabase.InterruptEntry;
import net.sourceforge.usbdm.peripheralDatabase.VectorTable;

public abstract class PeripheralWithState extends Peripheral implements IModelEntryProvider, IModelChangeListener {
   
   public static final String IRQ_HANDLER_INSTALLED_SYMBOL = "irqHandlingMethod";

   /** Data obtained from the Menu description file */
   protected MenuData fMenuData = null;
   
   /** Map of parameters for peripheral */
   protected HashMap<String, String> fParamMap = new HashMap<String,String>();

   /** Map of parameters for peripheral */
   protected HashMap<String, String> fConstantMap = new HashMap<String,String>();

   protected PeripheralWithState(String basename, String instance, DeviceInfo deviceInfo) {
      super(basename, instance, deviceInfo);
//      System.err.println("Creating "+basename+instance+" "+this.getClass());
   }

   /**
    * Load the models and validators for this class of peripheral
    * 
    * @return
    * @throws Exception 
    */
   public void loadModels() throws Exception {
      try {
         fMenuData = ParseMenuXML.parsePeriperalFile(getPeripheralModelName(), this);
      } catch (Exception e) {
         System.err.println("Warning: Failed to load model "+getPeripheralModelName()+" for peripheral " + getName() + ", Reason: " + e.getMessage());
      }
      if (fMenuData == null) {
         return;
      }
//      for (ParseMenuXML.ValidatorInformation v:fData.getValidators()) {
//         try {
//            String className = v.getClassName();
//            // Get validator class
//            Class<?> clazz = Class.forName(className);
//            PeripheralValidator validatorClass = (PeripheralValidator) clazz.getConstructor(PeripheralWithState.class, v.getParams().getClass()).newInstance(this, v.getParams());
//            addValidator(validatorClass);
//         } catch (Exception e) {
//            throw new Exception("Failed to add validator "+v.getClassName()+" for Peripheral " + getName(), e);
//         }
//      }
   }

   @Override
   public BaseModel getModels(BaseModel parent) {
      if (fMenuData == null) {
         return null;
      }
      BaseModel model = fMenuData.getRootModel();
      if (model != null) {
         model.setParent(parent);
      }
      return model;
   }

   @Override
   public void writeInfoConstants(DocumentUtilities pinMappingHeaderFile) throws IOException {
      super.writeInfoConstants(pinMappingHeaderFile);
      writeInfoTemplate(pinMappingHeaderFile);
   }

   public void writeInfoTemplate(DocumentUtilities pinMappingHeaderFile) throws IOException {
      pinMappingHeaderFile.write("   // Template:" + getPeripheralModelName()+"\n\n");
      if (fMenuData == null) {
//         System.err.println("No fData for " + getName());
         return;
      }
//      System.err.println("fData for " + getName());
      // Get default template for info class
      String template = fMenuData.getTemplate("info", "");
      if (template != null) {
         pinMappingHeaderFile.write(substitute(template));
      }
   }
   
   @Override
   public void writeNamespaceInfo(DocumentUtilities documentUtilities) throws IOException {
      super.writeNamespaceInfo(documentUtilities);
      if (fMenuData == null) {
         return;
      }
      String template = fMenuData.getTemplate("usbdm", "");
      if ((template != null) && (!template.isEmpty())) {
         documentUtilities.write(substitute(template));
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
      if (fMenuData == null) {
         return;
      }
      Map<String, String> symbolMap = addTemplatesToSymbolMap(fDeviceInfo.getSimpleSymbolMap());
      processProjectActions.process(project, fMenuData.getProjectActionList(), symbolMap, monitor);
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

      if (fMenuData == null) {
         return map;
      }
      // Load any named templates
      for (String key:fMenuData.getTemplates().keySet()) {
         if (key.isEmpty() || key.endsWith(".")) {
            // Discard unnamed templates
            continue;
         }
         CodeTemplate fileTemplate = fMenuData.getTemplates().get(key);

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
      super.addVariable(variable);
      variable.addListener(this);
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
   
   @Override
   public void modifyVectorTable(VectorTable vectorTable) {
      for (IrqVariable var : irqVariables) {
         modifyVectorTable(vectorTable, var, getClassName());
      }
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
   public void modifyVectorTable(VectorTable vectorTable, IrqVariable irqVariable, String className) {

      if ((irqVariable==null) || (irqVariable.getValueAsLong() == 0)) {
         // No modification
         return;
      }
      final String headerFileName = getBaseName().toLowerCase()+".h";
      boolean handlerSet = false;
      String pattern = irqVariable.getPattern().replaceAll("%i", getInstance()).replaceAll("%b", getBaseName());
      Pattern p = Pattern.compile(pattern);
      for (InterruptEntry entry:vectorTable.getEntries()) {
         if (entry != null) {
            Matcher m = p.matcher(entry.getName());
            if (m.matches()) {
               String modifier = "";
               if (m.groupCount() > 0) {
                  modifier = m.group(1);
               }
               String handlerName  = "Default_Handler";
               if (irqVariable.getValueAsLong() == 1) {
                  // Replace with name of class static method
                  String classHandler = irqVariable.getClassHandler().replaceAll("%i", getInstance()).replaceAll("%b", getBaseName());
                  handlerName = DeviceInfo.NAME_SPACE+"::"+className+"::"+classHandler;
               }
               if (irqVariable.getValueAsLong() == 2) {
                  handlerName = irqVariable.getHandlerName().replaceAll("%", modifier);
               }
               entry.setHandlerName(handlerName);
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
   protected void writeExtraXMLDefinitions(XmlDocumentUtilities documentUtilities) throws IOException {
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

   ArrayList<IrqVariable> irqVariables = new ArrayList<IrqVariable>();
   
   /**
    * Add a Variable describing a IRQ handler setting
    * 
    * @param variable
    */
   public void addIrqVariable(IrqVariable variable) {
      irqVariables.add(variable);
   }

   /**
    * Get list of Variables describing IRQ handler settings
    * 
    * @param variable
    */
   public List<IrqVariable> getIrqVariables(IrqVariable variable) {
      return irqVariables;
   }

}
