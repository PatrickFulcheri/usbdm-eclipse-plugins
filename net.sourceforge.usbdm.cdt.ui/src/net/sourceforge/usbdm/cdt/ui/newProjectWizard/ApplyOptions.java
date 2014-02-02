package net.sourceforge.usbdm.cdt.ui.newProjectWizard;

import java.util.Map;

import net.sourceforge.usbdm.deviceDatabase.Device;
import net.sourceforge.usbdm.deviceDatabase.Device.ProjectOption;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Adds project settings from USBDM device database
 * 
 * @author PODonoghue
 *
 */
public class ApplyOptions {

   private boolean modified = false;

   public void process(IProject projectHandle, Device device, Map<String, String> variableMap, ProjectOption projectOption, IProgressMonitor progressMonitor) throws Exception {

      System.err.println("ApplyOptions.process() - "+projectOption.toString());
      String id       = MacroSubstitute.substitute(projectOption.getId(),     variableMap);
      String path     = MacroSubstitute.substitute(projectOption.getPath(),   variableMap);
      String value[] = projectOption.getValue();
      for (int index=0; index<value.length; index++) {
         value[index] = MacroSubstitute.substitute(value[index], variableMap);
//         System.err.println("ApplyOptions.process() value[n] = "+value[index]);
      }
      try {
         modified |= setOptionValue(projectHandle, id, value, path, progressMonitor);
      } catch (BuildException e) {
         e.printStackTrace();
      }
   }
   
   public void updateConfigurations(IProject projectHandle) {
      if (modified) {
         IConfiguration[] projectConfigs = ManagedBuildManager.getBuildInfo(projectHandle).getManagedProject().getConfigurations();
         ManagedBuildManager.saveBuildInfo(projectHandle, true);
         for (IConfiguration config : projectConfigs) {
             ScannerConfigBuilder.build(config, ScannerConfigBuilder.PERFORM_CORE_UPDATE, new NullProgressMonitor());    
         }
      }
   }

   private static boolean setOptionValue(IProject projectHandle, String id, String[] value, String path, IProgressMonitor progressMonitor) throws Exception {
      IConfiguration[] projectConfigs = ManagedBuildManager.getBuildInfo(projectHandle).getManagedProject().getConfigurations();

      boolean resource = !(path == null || path.equals("") || path.equals("/")); //$NON-NLS-1$ //$NON-NLS-2$
      boolean modified = false;

//      System.err.println("ApplyOptions.setOptionValue(\n\t\t\t\t id="+id+",\n\t\t\t\t value="+value+",\n\t\t\t\t path="+path);

      for (IConfiguration config : projectConfigs) {
         IResourceConfiguration resourceConfig = null;
         if (resource) {
            resourceConfig = config.getResourceConfiguration(path);
            if (resourceConfig == null) {
               IFile file = projectHandle.getFile(path);
               if (file == null) {
                  throw new Exception("" + path); //$NON-NLS-1$
               }
               resourceConfig = config.createResourceConfiguration(file);
            }
            ITool[] tools = resourceConfig.getTools();
            for (ITool tool : tools) {
               modified |= addToOptionForResourceConfig(id, value, resourceConfig, tool.getOptions(), tool);
            }
         } else {
            IToolChain toolChain = config.getToolChain();
            modified |= addToOptionForConfig(id, value, config, toolChain.getOptions(), toolChain);

            ITool[] tools = config.getTools();
            for (ITool tool : tools) {
               modified |= addToOptionForConfig(id, value, config, tool.getOptions(), tool);
            }
         }
      }
      return modified;
   }

   private static boolean addToOptionForResourceConfig(String id, String value[], IResourceConfiguration resourceConfig, IOption[] options, IHoldsOptions optionHolder) 
         throws BuildException, ProcessFailureException {
      boolean modified = false;
      String lowerId = id.toLowerCase();
      for (IOption option : options) {
         if (option.getBaseId().toLowerCase().matches(lowerId)) {
            int optionType = option.getValueType();
            if ((optionType == IOption.STRING)) {
               String oldValue = option.getStringValue();
               String newValue = oldValue + value[0];
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, newValue);
               modified = true;
            }
            else if ((optionType == IOption.ENUMERATED)) {
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, value[0]);
               modified = true;
            }
            else if ((optionType == IOption.BOOLEAN)) {
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, value[0].equals("true"));
               modified = true;
            }
            else if ((optionType == IOption.INCLUDE_FILES)) {
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, value);
               modified = true;
            }
            else if ((optionType == IOption.INCLUDE_PATH)) {
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, value);
               modified = true;
            }
            else if ((optionType == IOption.PREPROCESSOR_SYMBOLS)) {
               OptionStringValue x[] = null;
               ManagedBuildManager.setOption(resourceConfig, optionHolder, option, x);
               modified = true;
            }
            else {
               throw new ProcessFailureException("Unexpected option type"+optionType); //$NON-NLS-1$ //$NON-NLS-2$
            }
         }
      }
      return modified;
   }

   private static boolean addToOptionForConfig(String id, String value[], IConfiguration config, IOption[] options, IHoldsOptions optionHolder) 
         throws Exception {
      boolean modified = false;
      String lowerId = id.toLowerCase();
      for (IOption option : options) {
         if (option.getBaseId().toLowerCase().matches(lowerId)) {
            int optionType = option.getValueType();
            if ((optionType == IOption.STRING)) {
               String oldValue = option.getStringValue();
               String newValue = oldValue + value[0];
               ManagedBuildManager.setOption(config, optionHolder, option, newValue);
               modified = true;
            
            }
            else if ((optionType == IOption.ENUMERATED)) {
               ManagedBuildManager.setOption(config, optionHolder, option, value[0]);
               modified = true;
            }
            else if ((optionType == IOption.BOOLEAN)) {
               ManagedBuildManager.setOption(config, optionHolder, option, value[0].equals("true"));
               modified = true;
            }
            else if ((optionType == IOption.INCLUDE_FILES)) {
               ManagedBuildManager.setOption(config, optionHolder, option, value);
               modified = true;
            }
            else if ((optionType == IOption.INCLUDE_PATH)) {
               ManagedBuildManager.setOption(config, optionHolder, option, value);
               modified = true;
            }
            else if ((optionType == IOption.PREPROCESSOR_SYMBOLS)) {
               ManagedBuildManager.setOption(config, optionHolder, option, value);
               modified = true;
            }
            else {
               throw new Exception("Unexpected option type"+optionType); //$NON-NLS-1$ //$NON-NLS-2$
            }
         }
      }
      return modified;
   }
}