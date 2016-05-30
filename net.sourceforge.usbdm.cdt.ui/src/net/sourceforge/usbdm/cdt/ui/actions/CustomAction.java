package net.sourceforge.usbdm.cdt.ui.actions;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface CustomAction {

   /**
    * 
    * @param wizard 
    * @param projectHandle    Handle of the project to modify
    * @param variableMap      Map of variables from project creation
    * @param progressMonitor  Progress monitor
    * @param parameters       Custom parameters
    * @return                 success/failure
    */ 
   public boolean action(
         IProject              projectHandle, 
         Map<String, String>   variableMap, 
         IProgressMonitor      progressMonitor, 
         String[]              parameters);
}
