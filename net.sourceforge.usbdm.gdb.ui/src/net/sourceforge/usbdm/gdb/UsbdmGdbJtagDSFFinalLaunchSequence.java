/*******************************************************************************
 * Copyright (c) 2007 - 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Modified Peter O'Donoghue for USBDM
 *  
 * Based on work by:
 *     Ericsson - initial API and implementation this class is based on
 *     QNX Software Systems - Initial implementation for Jtag debugging
 *     Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (allow
 *                connections via serial ports and pipes).
 *     John Dallaway - Wrong groupId during initialization (Bug 349736)    
 *     Marc Khouzam (Ericsson) - Updated to extend FinalLaunchSequence instead of copying it (bug 324101)
 *     Andy Jin
 *******************************************************************************/


package net.sourceforge.usbdm.gdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.usbdm.gdb.GdbServerParameters.GdbServerType;
import net.sourceforge.usbdm.jni.UsbdmException;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * The final launch sequence for the Jtag hardware debugging using the
 * DSF/GDB debugger framework.
 * <p>
 * This class is based on the implementation of the standard DSF/GDB debugging
 * <code>org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence</code>
 * <p>
 * It adds Jtag hardware debugging specific steps to initialize remote target
 * and start the remote Jtag debugging.
 */
public class UsbdmGdbJtagDSFFinalLaunchSequence extends FinalLaunchSequence {

   /** utility method; cuts down on clutter */
   private void queueCommands(List<String> commands, RequestMonitor rm) {
      if (!commands.isEmpty()) { 
         fCommandControl.queueCommand(
               new CLICommand<MIInfo>(fCommandControl.getContext(), composeCommand(commands)),
               new DataRequestMonitor<MIInfo>(getExecutor(), rm));
      }
      else {
         rm.done();
      }
   }

   // The launchConfiguration attributes
   private Map<String, Object> fAttributes;
   private GdbServerParameters fGdbServerParameters;

   private DsfSession fSession;

   private IGDBControl fCommandControl;
   private IGDBBackend	fGDBBackend;
   private IMIProcesses fProcService;
   private UsbdmGdbInterface fGdbJtagDevice;

   private DsfServicesTracker fTracker;
   private IMIContainerDMContext fContainerCtx;

   public UsbdmGdbJtagDSFFinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm, GdbServerParameters gdbServerParameters) {
      super(session, attributes, rm);
      fSession = session;
      fAttributes = attributes;
      fGdbServerParameters = gdbServerParameters;		
   }

   public UsbdmGdbJtagDSFFinalLaunchSequence(DsfExecutor executor, GdbLaunch launch, SessionType sessionType, boolean attach, RequestMonitorWithProgress rm, GdbServerParameters gdbServerParameters) {
      this(launch.getSession(), getAttributes(launch), rm, gdbServerParameters);
   }

   @SuppressWarnings("unchecked")
   private static Map<String, Object> getAttributes(GdbLaunch launch) {
      try {
         return launch.getLaunchConfiguration().getAttributes();
      } catch (CoreException e) {
      }
      return new HashMap<String, Object>();
   }

   protected IMIContainerDMContext getContainerContext() {
      return fContainerCtx;
   }

   protected void setContainerContext(IMIContainerDMContext ctx) {
      fContainerCtx = ctx;
   }

   protected static final String GROUP_JTAG = "GROUP_JTAG";

   @Override
   protected String[] getExecutionOrder(String group) {
      if (GROUP_TOP_LEVEL.equals(group)) {
         // Initialize the list with the base class' steps
         // We need to create a list that we can modify, which is why we create our own ArrayList.
         List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

         // First, remove all steps of the base class that we don't want to use.
         orderList.removeAll(Arrays.asList(new String[] { 
               "stepNewProcess",   //$NON-NLS-1$
         }));

         // Now insert our steps before the data model initialized event is sent
         orderList.add(orderList.indexOf("stepDataModelInitializationComplete"), GROUP_JTAG);

         return orderList.toArray(new String[orderList.size()]);
      }

      // Finally, deal with our groups and their steps.
      if (GROUP_JTAG.equals(group)) {
         return new String[] {
               "stepInitializeJTAGFinalLaunchSequence",//$NON-NLS-1$
               "stepLaunchUsbdmGdbServer",//$NON-NLS-1$
               "stepUsbdmRetrieveJTAGDevice",   //$NON-NLS-1$
               "stepLoadSymbols",   //$NON-NLS-1$
               "stepConnectToTarget",   //$NON-NLS-1$
               "stepResetBoard",   //$NON-NLS-1$
               "stepDelayStartup",   //$NON-NLS-1$
               "stepHaltBoard",   //$NON-NLS-1$
               "stepUserInitCommands",   //$NON-NLS-1$
               "stepLoadImage",   //$NON-NLS-1$

               "stepUpdateContainer",   //$NON-NLS-1$

               "stepSetArguments",   //$NON-NLS-1$
               "stepSetEnvironmentVariables",   //$NON-NLS-1$
               "stepStartTrackingBreakpoints",   //$NON-NLS-1$

               "stepSetProgramCounter",   //$NON-NLS-1$
               "stepStopScript",   //$NON-NLS-1$
               "stepResumeScript",   //$NON-NLS-1$
               "stepUserDebugCommands",   //$NON-NLS-1$
               "stepJTAGCleanup",   //$NON-NLS-1$
         };
      }

      // For any subgroups of the base class
      return super.getExecutionOrder(group);
   }


   /** 
    * Initialize the members of the class.
    * This step is mandatory for the rest of the sequence to complete.
    */
   @Execute
   public void stepInitializeJTAGFinalLaunchSequence(RequestMonitor rm) {
      fTracker = new DsfServicesTracker(Activator.getBundleContext(), fSession.getId());
      fGDBBackend = fTracker.getService(IGDBBackend.class);
      if (fGDBBackend == null) {
         rm.done(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
         return;
      }

      fCommandControl = fTracker.getService(IGDBControl.class);
      if (fCommandControl == null) {
         rm.done(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
         return;
      }

      fProcService = fTracker.getService(IMIProcesses.class);
      if (fProcService == null) {
         rm.done(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot obtain process service", null)); //$NON-NLS-1$
         return;
      }

      // When we are starting to debug a new process, the container is the default process used by GDB.
      // We don't have a pid yet, so we can simply create the container with the UNIQUE_GROUP_ID
      setContainerContext(fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID));
      rm.done();
   }

   /** 
    * Rollback method for {@link #stepInitializeJTAGFinalLaunchSequence()}
    */
   @RollBack("stepInitializeJTAGFinalLaunchSequence")
   public void rollBackInitializeFinalLaunchSequence(RequestMonitor rm) {
      if (fTracker != null) fTracker.dispose();
      fTracker = null;
      rm.done();
   }

   /*
    * Create Local USBDM GDB Server (if needed)
    */
   @Execute
   public void stepLaunchUsbdmGdbServer(final RequestMonitor rm) {
//      System.err.println("stepLaunchUsbdmGdbServer.stepLaunchUsbdmGdbServer()");

      if (fGdbServerParameters == null) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1,"Unable to obtain server parameters", null)); //$NON-NLS-1$
         rm.done();
         return;
      }

      if (fGdbServerParameters.getServerType() == GdbServerType.SERVER_SOCKET) {
         GdbServerInterface gdbServerInterface = new GdbServerInterface(fGdbServerParameters);  
         if (gdbServerInterface != null) {
            try {
               gdbServerInterface.startServer();
            } catch (UsbdmException e1) {
               e1.printStackTrace();
               rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, e1.getMessage(), null)); //$NON-NLS-1$
               rm.done();
               return;
            }
         }
      }
      rm.done();
   }

   /*
    * Retrieve the IGDBJtagDevice instance
    */
   @Execute
   public void stepUsbdmRetrieveJTAGDevice(final RequestMonitor rm) {
      fGdbJtagDevice = new UsbdmGdbInterface();
      rm.done();
   }

   /*
    * Execute symbol loading
    */
   @Execute
   public void stepLoadSymbols(final RequestMonitor rm) {
      try {
         if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS)) {
            String symbolsFileName = null;

            // New setting in Helios. Default is true. Check for existence
            // in order to support older launch configs
            if (fAttributes.containsKey(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS) &&
                  CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS)) {
               IPath programFile = fGDBBackend.getProgramPath();
               if (programFile != null) {
                  symbolsFileName = programFile.toOSString();
               }
            }
            else {
               symbolsFileName = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
               if (symbolsFileName.length() > 0) {
                  symbolsFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(symbolsFileName);
               } else {
                  symbolsFileName = null;
               }
            }

            if (symbolsFileName == null) {
               rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "GDBJtagDebugger.err_no_img_file", null)); //$NON-NLS-1$
               rm.done();
               return;
            }

            // Escape windows path separator characters TWICE, once for Java and once for GDB.						
            symbolsFileName = symbolsFileName.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$

            String symbolsOffset = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);
            if (symbolsOffset.length() > 0) {
               symbolsOffset = "0x" + symbolsOffset;					
            }
            List<String> commands = new ArrayList<String>();
            fGdbJtagDevice.doLoadSymbol(symbolsFileName, symbolsOffset, commands);
            queueCommands(commands, rm);									

         } else {
            rm.done();
         }
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot load symbol", e)); //$NON-NLS-1$
         rm.done();
      }
   }

   /*
    * Hook up to remote target
    */
   @Execute
   public void stepConnectToTarget(final RequestMonitor rm) {

      if (fGdbServerParameters == null) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1,"Unable to obtain server parameters", null)); //$NON-NLS-1$
         rm.done();
         return;
      }

      ArrayList<String> serverCommandLine = fGdbServerParameters.getCommandLine();
      if (serverCommandLine == null) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1,"Unable to obtain server command line", null)); //$NON-NLS-1$
         rm.done();
         return;
      }

      String commandLine = new String();
      commandLine = serverCommandLine.get(0);
      for (int index=1; index<serverCommandLine.size(); index++) { 
         commandLine += " " + serverCommandLine.get(index);
      }
//      System.err.println("stepConnectToTarget() command = \'" + commandLine + "\'");

      List<String> commands = new ArrayList<String>();
      fGdbJtagDevice.doRemote(commandLine, commands);
      queueCommands(commands, rm);
      rm.done();
   }

   /*
    * Run device-specific code to reset the board
    */
   @Execute
   public void stepResetBoard(final RequestMonitor rm) {
      if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET)) {
         List<String> commands = new ArrayList<String>();
         fGdbJtagDevice.doReset(commands);
         queueCommands(commands, rm);
      } else {
         rm.done();
      }
   }

   /*
    * Run device-specific code to delay the startup
    */
   @Execute
   public void stepDelayStartup(final RequestMonitor rm) {
      int defaultDelay = fGdbJtagDevice.getDefaultDelay();
      List<String> commands = new ArrayList<String>();
      fGdbJtagDevice.doDelay(CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_DELAY, defaultDelay), commands);
      queueCommands(commands, rm);								
   }

   /*
    * Run device-specific code to halt the board
    */
   @Execute
   public void stepHaltBoard(final RequestMonitor rm) {
      if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_DO_HALT, IGDBJtagConstants.DEFAULT_DO_HALT)) {
         List<String> commands = new ArrayList<String>();
         fGdbJtagDevice.doHalt(commands);
         queueCommands(commands, rm);								
      } else {
         rm.done();
      }
   }

   /*
    * Execute any user defined init commands
    */
   @Execute
   public void stepUserInitCommands(final RequestMonitor rm) {
      try {
         String userCmd = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_INIT_COMMANDS, IGDBJtagConstants.DEFAULT_INIT_COMMANDS);
         userCmd = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(userCmd);
         if (userCmd.length() > 0) {
            String[] commands = userCmd.split("\\r?\\n"); //$NON-NLS-1$

            CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm);
            crm.setDoneCount(commands.length);
            for (int i = 0; i < commands.length; ++i) {
               fCommandControl.queueCommand(
                     new CLICommand<MIInfo>(fCommandControl.getContext(), commands[i]),
                     new DataRequestMonitor<MIInfo>(getExecutor(), crm));
            }
         }
         else {
            rm.done();
         }
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot run user defined init commands", e)); //$NON-NLS-1$
         rm.done();
      }
   }

   /*
    * Execute image loading
    */
   @Execute
   public void stepLoadImage(final RequestMonitor rm) {
      try {
         String imageFileName = null;
         if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE)) {
            // New setting in Helios. Default is true. Check for existence
            // in order to support older launch configs
            if (fAttributes.containsKey(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE) &&
                  CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE)) {
               IPath programFile = fGDBBackend.getProgramPath();
               if (programFile != null) {
                  imageFileName = programFile.toOSString();
               }
            }
            else {
               imageFileName = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME); 
               if (imageFileName.length() > 0) {
                  imageFileName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(imageFileName);
               } else {
                  imageFileName = null;
               }
            }

            if (imageFileName == null) {
               rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Error - No image file found", null)); //$NON-NLS-1$
               rm.done();
               return;
            }

            // Escape windows path separator characters TWICE, once for Java and once for GDB.						
            imageFileName = imageFileName.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$

            String imageOffset = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);
            if (imageOffset.length() > 0) {
               imageOffset = (imageFileName.endsWith(".elf")) ? "" : "0x" + CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_IMAGE_OFFSET); //$NON-NLS-2$ 
            }
            List<String> commands = new ArrayList<String>();
            fGdbJtagDevice.doLoadImage(imageFileName, imageOffset, commands);
            queueCommands(commands, rm);									
         } 
         else {
            rm.done();
         }
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot load image", e)); //$NON-NLS-1$
         rm.done();
      }
   }

   /**
    * Now that we are connected to the target, we should update
    * our container to properly fill in its pid.
    */
   @Execute
   public void stepUpdateContainer(RequestMonitor rm) {
      String groupId = getContainerContext().getGroupId();
      setContainerContext(fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), groupId));
      rm.done();
   }

   /**
    * Specify the arguments to the program that will be run.
    */
   @Execute
   public void stepSetArguments(RequestMonitor rm) {
      try {
         String args = CDebugUtils.getAttribute(
               fAttributes,
               ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
               ""); //$NON-NLS-1$

         if (args.length() != 0) {
            args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
            String[] argArray = CommandLineUtil.argumentsToArray(args);
            fCommandControl.queueCommand(
                  fCommandControl.getCommandFactory().createMIGDBSetArgs(getContainerContext(), argArray), 
                  new ImmediateDataRequestMonitor<MIInfo>(rm));
         } else {
            rm.done();
         }
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot get inferior arguments", e)); //$NON-NLS-1$
         rm.done();
      }    		
   }

   /**
    * Specify environment variables if needed
    */
   @Execute
   public void stepSetEnvironmentVariables(RequestMonitor rm) {
      boolean clear = false;
      Properties properties = new Properties();
      try {
         // here we need to pass the proper container context
         clear = fGDBBackend.getClearEnvironment();
         properties = fGDBBackend.getEnvironmentVariables();
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot get environment information", e)); //$NON-NLS-1$
         rm.done();
         return;
      }

      if (clear == true || properties.size() > 0) {
         fCommandControl.setEnvironment(properties, clear, rm);
      } else {
         rm.done();
      }
   }

   /* 
    * Start tracking the breakpoints once we know we are connected to the target (necessary for remote debugging) 
    */
   @Execute
   public void stepStartTrackingBreakpoints(final RequestMonitor rm) {
      MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
      IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(), IBreakpointsTargetDMContext.class);
      bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
   }

   /*
    * Set the program counter
    */
   @Execute
   public void stepSetProgramCounter(final RequestMonitor rm) {
      if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER)) {
         String pcRegister = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_PC_REGISTER, CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_PC_REGISTER)); 
         List<String> commands = new ArrayList<String>();
         fGdbJtagDevice.doSetPC(pcRegister, commands);
         queueCommands(commands, rm);								
      } else {
         rm.done();
      }
   }

   /*
    * Execute the stop script
    */
   @Execute
   public void stepStopScript(final RequestMonitor rm) {
      if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT)) {
         String stopAt = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_STOP_AT, IGDBJtagConstants.DEFAULT_STOP_AT); 
         List<String> commands = new ArrayList<String>();
         fGdbJtagDevice.doStopAt(stopAt, commands);
         queueCommands(commands, rm);								
      } else {
         rm.done();
      }
   }

   /*
    * Execute the resume script
    */
   @Execute
   public void stepResumeScript(final RequestMonitor rm) {
      if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME)) {
         List<String> commands = new ArrayList<String>();
         fGdbJtagDevice.doContinue(commands);
         queueCommands(commands, rm);									
      } else {
         rm.done();
      }
   }

   /*
    * Run any user defined commands to start debugging
    */
   @Execute
   public void stepUserDebugCommands(final RequestMonitor rm) {
      try {
         String userCmd = CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_RUN_COMMANDS, IGDBJtagConstants.DEFAULT_RUN_COMMANDS); 
         if (userCmd.length() > 0) {
            userCmd = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(userCmd);
            String[] commands = userCmd.split("\\r?\\n"); //$NON-NLS-1$

            CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm);
            crm.setDoneCount(commands.length);
            for (int i = 0; i < commands.length; ++i) {
               fCommandControl.queueCommand(
                     new CLICommand<MIInfo>(fCommandControl.getContext(), commands[i]),
                     new DataRequestMonitor<MIInfo>(getExecutor(), crm));
            }
         }
         else {
            rm.done();
         }
      } catch (CoreException e) {
         rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot run user defined run commands", e)); //$NON-NLS-1$
         rm.done();
      }
   }

   /**
    * @param commands
    * @return String commands in String format
    */
   private String composeCommand(Collection<String> commands) {
      if (commands.isEmpty())
         return null;
      StringBuffer sb = new StringBuffer();
      Iterator<String> it = commands.iterator();
      while (it.hasNext()) {
         sb.append(it.next());
      }
      return sb.toString();
   }

   /**
    * Cleanup now that the sequence has been run.
    */
   @Execute
   public void stepJTAGCleanup(final RequestMonitor requestMonitor) {
      fTracker.dispose();
      fTracker = null;
      requestMonitor.done();
   }
}