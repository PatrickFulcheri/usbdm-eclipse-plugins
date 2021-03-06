package net.sourceforge.usbdm.cdt.ui.newProjectWizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexerSetupParticipant;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import net.sourceforge.usbdm.cdt.tools.UsbdmConstants;
import net.sourceforge.usbdm.cdt.ui.Activator;
import net.sourceforge.usbdm.cdt.ui.actions.ProcessProjectActions;
import net.sourceforge.usbdm.constants.UsbdmSharedConstants;
import net.sourceforge.usbdm.deviceDatabase.Device;
import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.packageParser.ProjectAction;
import net.sourceforge.usbdm.packageParser.ProjectActionList;
import net.sourceforge.usbdm.packageParser.ProjectActionList.Value;
import net.sourceforge.usbdm.packageParser.ProjectActionList.Visitor;
import net.sourceforge.usbdm.packageParser.ProjectActionList.Visitor.Result;
import net.sourceforge.usbdm.packageParser.ProjectActionList.Visitor.Result.Status;
import net.sourceforge.usbdm.packageParser.ProjectConstant;
import net.sourceforge.usbdm.packageParser.WizardPageInformation;

/**
 * @author pgo
 */
public class UsbdmNewProjectWizard extends Wizard implements INewWizard, IRunnableWithProgress {
   
   private UsbdmNewProjectPage_1                fUsbdmNewProjectPage_1 = null;
   private UsbdmDeviceSelectionPage_2           fUsbdmProjectParametersPage_2 = null;
   private ArrayList<UsbdmDynamicOptionPage_N>  fDynamicWizardPages  = null;
   private ArrayList<WizardPageInformation>     fWizardPageInformation = new ArrayList<WizardPageInformation>();
   private ProjectActionList                    fProjectActionList = null;
   /** Base parameters from pages 1 & 2 */
   private Map<String, String>                  fBaseParamMap = null;
   /** Parameters from all Pages */
   private Map<String, String>                  fParamMap = null;
   private Device                               fDevice = null;

   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection) {
      setNeedsProgressMonitor(true);
      setDefaultPageImageDescriptor(UsbdmSharedConstants.getUsbdmIcon());
      
      IDialogSettings settings = null;
      Activator activator = Activator.getDefault();
      if (activator == null) {
         System.err.println("*************************** plugin is null *********************");
      }
      if (activator != null) {
         settings = activator.getDialogSettings();
         if (settings == null) {
            System.err.println("*************************** settings is null *********************");
         }
      }
      setDialogSettings(settings);
   }

   @Override
   public void addPages() {
      fUsbdmNewProjectPage_1  = new UsbdmNewProjectPage_1(this);
      addPage(fUsbdmNewProjectPage_1);
   }

   @Override
   public IWizardPage getStartingPage() {
      return fUsbdmNewProjectPage_1;
   }

   @Override
   public boolean canFinish() {
//      super.canFinish();
      if ((fUsbdmNewProjectPage_1 == null)        || !fUsbdmNewProjectPage_1.isPageComplete() ||
          (fUsbdmProjectParametersPage_2 == null) || !fUsbdmProjectParametersPage_2.isPageComplete() ||
          fDynamicWizardPages == null) {
         return false;
      }
      if (fDynamicWizardPages.size() != fWizardPageInformation.size()) {
         // Haven't visited all pages
         return false;
      }
      for (UsbdmDynamicOptionPage_N page:fDynamicWizardPages) {
         if (!page.isPageComplete()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean needsPreviousAndNextButtons() {
      return true;
   }

   /**
    * Updates fParamMap from fBaseParamMap and buttons on dynamic pages
    */
   void updateParamMap() {
//      System.err.println("UsbdmNewProjectWizard.updateParamMap() - entry");

//      listParamMap("fBaseParamMap\n===========================================", fBaseParamMap);

      fParamMap = new HashMap<String, String>(fBaseParamMap);
      
      if (fUsbdmProjectParametersPage_2 == null) {
         return;
      }
      fUsbdmProjectParametersPage_2.getPageData(fParamMap);
//      listParamMap("fParamMap\n===========================================", fParamMap);
      fDevice = fUsbdmProjectParametersPage_2.getDevice();
      if (fDevice == null) {
         return;
      }
      fProjectActionList = fDevice.getProjectActionList(fParamMap);
      
      if (fDynamicWizardPages != null) {
         for (final UsbdmDynamicOptionPage_N p:fDynamicWizardPages) {
                  p.getPageData(fParamMap);
         }
      }
      updateMapConstants(fParamMap, fProjectActionList);
//      listParamMap("fParamMap\n===========================================", fParamMap);
//      System.err.println("UsbdmNewProjectWizard.updateParamMap() - exit");
   }

   /**
    * Collects information about dynamic wizard pages
    */
   void updateDynamicWizardPageInformation() {
//      System.err.println("updateDynamicWizardPageInformation()");
      fWizardPageInformation = new ArrayList<WizardPageInformation>();
      Visitor visitor = new ProjectActionList.Visitor() {
         @Override
         public Result applyTo(ProjectAction action, ProjectActionList.Value result, IProgressMonitor monitor) {
            if (action instanceof WizardPageInformation) {
               WizardPageInformation page = (WizardPageInformation) action;
//               System.err.println("Adding dynamic page info " + page.getName());
               fWizardPageInformation.add(page);
            }
            return CONTINUE;
         }
      };
      if (fProjectActionList != null) {
         fProjectActionList.visit(visitor, null);
      }
   }
   
   boolean hasChanged(IWizardPage currentPage) {
      boolean changed = false;
      if (fUsbdmNewProjectPage_1 != null) {
         changed = fUsbdmNewProjectPage_1.hasChanged();
      }
      if ((currentPage == fUsbdmNewProjectPage_1) || changed) {
         return changed;           
      }
      if (fUsbdmProjectParametersPage_2 != null) {
         changed = fUsbdmProjectParametersPage_2.hasChanged();
      }
      if ((currentPage == fUsbdmProjectParametersPage_2) || changed) {
         return changed;           
      }
      if (fDynamicWizardPages == null) {
         return false;
      }
      for (UsbdmDynamicOptionPage_N page:fDynamicWizardPages) {
         if (page.hasChanged()) {
            return true;
         }
         if (page == currentPage) {
            return false;
         }
      }
      return false;
   }

   /**
    * Visits the nodes of the projectActionList and add constants found 
    * 
    * @param paramMap            Map to add constants to
    * @param projectActionList   Action list to visit
    */
   void updateMapConstants(final Map<String, String> paramMap, ProjectActionList projectActionList) {
      Visitor visitor = new Visitor() {
         @Override
         public Result applyTo(ProjectAction action, Value result, IProgressMonitor monitor) {
            try {
               if (action instanceof ProjectActionList) {
                  ProjectActionList projectActionList = (ProjectActionList) action;
                  return projectActionList.appliesTo(fUsbdmProjectParametersPage_2.getDevice(), paramMap)?CONTINUE:PRUNE;
               }
               else if (action instanceof ProjectConstant) {
                  ProjectConstant projectConstant = (ProjectConstant) action;
//                  System.err.println(String.format("updateMapConstants(): Found constant %s => %s",  projectConstant.getId(), projectConstant.getValue()));
                  String value = paramMap.get(projectConstant.getId());
                  if (value != null) {
                     if (projectConstant.isWeak()) {
                        // Ignore - assume constant is a default that has been overwritten
                        return CONTINUE;
                     }
                     if (!projectConstant.doOverwrite() && !value.equals(projectConstant.getValue())) {
                        return new Result(new Exception("paramMap already contains constant " + projectConstant.getId()));
                     }
                  }
                  paramMap.put(projectConstant.getId(), projectConstant.getValue());
               }
               return CONTINUE;
            } catch (Exception e) {
               return new Result(e);
            }
         }
      };
      // Visit all enabled actions and collect constants
      Result result = fProjectActionList.visit(visitor, null);
      if (result.getStatus() == Status.EXCEPTION) {
         result.getException().printStackTrace();
      }
   }
 
   public void updateDynamicInformation(IWizardPage currentPage) {
//      System.err.println("updateDynamicInformation()");
      updateParamMap();
      updateDynamicWizardPageInformation();
      
      if (currentPage == fUsbdmProjectParametersPage_2) {
         // Re-create all dynamic pages
         UsbdmDynamicOptionPage_N newPage = new UsbdmDynamicOptionPage_N(fDevice, fProjectActionList, fWizardPageInformation.get(0));
         newPage.setWizard(this);
         fDynamicWizardPages = new ArrayList<UsbdmDynamicOptionPage_N>();
         fDynamicWizardPages.add(newPage);
      }
   }

   @Override
   public IWizardPage getNextPage(IWizardPage currentPage) {
//      System.err.println("getNextPage() " + currentPage.getTitle());
      if (currentPage == fUsbdmNewProjectPage_1) {
         // Create new project page if none or interface has changed
         if (hasChanged(currentPage) || (fUsbdmProjectParametersPage_2 == null)) {
            fBaseParamMap = new HashMap<String, String>();
            fUsbdmNewProjectPage_1.getPageData(fBaseParamMap);
            fUsbdmProjectParametersPage_2 = new UsbdmDeviceSelectionPage_2(fBaseParamMap, this);
            fDynamicWizardPages = null;
         }
         return fUsbdmProjectParametersPage_2;
      }
      if (currentPage == fUsbdmProjectParametersPage_2) {
         if (hasChanged(currentPage)) {
            updateDynamicInformation(currentPage);
         }
         if ((fDynamicWizardPages == null) || (fDynamicWizardPages.size() == 0)) {
            return null;
         }
         return fDynamicWizardPages.get(0);
      }
      // Check if current page is a dynamic page
      int dynamicPageIndex = -1;
      if (fDynamicWizardPages != null) {
         dynamicPageIndex = fDynamicWizardPages.indexOf(currentPage);
      }
      if (dynamicPageIndex < 0) {
         // Non-existent page!
         // Should be impossible
         return null;
      }
      int newDynamicPageIndex = dynamicPageIndex+1;
      if (hasChanged(currentPage)) {
         if (newDynamicPageIndex<fDynamicWizardPages.size()) {
            // Remove invalidated pages
            fDynamicWizardPages.subList(newDynamicPageIndex, fDynamicWizardPages.size()).clear();
         }
      }
      if (newDynamicPageIndex < fDynamicWizardPages.size()) {
         // Return existing page
         return fDynamicWizardPages.get(newDynamicPageIndex);
      }
      if (newDynamicPageIndex >= fWizardPageInformation.size()) {
         // No more dynamic pages to create
         return null;
      }
      // Create dynamic page
      updateDynamicWizardPageInformation();
      if (fDynamicWizardPages != null) {
         for (final UsbdmDynamicOptionPage_N p:fDynamicWizardPages) {
            Display.getDefault().syncExec(new Runnable() {
               public void run() {
                  p.getPageData(fParamMap);
               }
            });
         }
      }
      UsbdmDynamicOptionPage_N newPage = new UsbdmDynamicOptionPage_N(fUsbdmProjectParametersPage_2.getDevice(), fProjectActionList, fWizardPageInformation.get(newDynamicPageIndex));
      newPage.setWizard(this);
      fDynamicWizardPages.add(newPage);
      return newPage;
   }

   @Override
   public IWizardPage getPreviousPage(IWizardPage page) {
      if (page == fUsbdmProjectParametersPage_2) {
         return fUsbdmNewProjectPage_1;
      }
      if (fDynamicWizardPages != null) {
         int index = fDynamicWizardPages.indexOf(page);
         if (index == 0) {
            // Discard all dynamic pages
            return fUsbdmProjectParametersPage_2;
         }
         if (index > 0) {
            return fDynamicWizardPages.get(index-1);
         }
      }
      return null;
   }

   private void listParamMap(String title, final Map<String, String> paramMap) {
      System.err.println(title);
      ArrayList<String> keySet = new ArrayList<String>(paramMap.keySet());
      Collections.sort(keySet);
      for (String key:keySet) {
         if (key.equals("linkerInformation")) {
            continue;
         }
         if (key.equals("cVectorTable")) {
            continue;
         }
         if (key.startsWith("demo.KSDK")) {
            continue;
         }
         System.err.println(String.format("%-60s => %s", key, paramMap.get(key)));
      }
      System.err.println("================================================");
   }
   
   public void buildConfigurations(IProject project, IProgressMonitor monitor) {
      final int WORK_SCALE = 1000;
      try {
         ManagedBuildManager.saveBuildInfo(project, true);
         IConfiguration[] projectConfigs = ManagedBuildManager.getBuildInfo(project).getManagedProject().getConfigurations();
         monitor.beginTask("Build project", WORK_SCALE);
         try {
            ManagedBuildManager.buildConfigurations(projectConfigs, monitor);
         } catch (CoreException e) {
            e.printStackTrace();
         }
      }
      finally {
         monitor.done();
      }
   }

   /**
    * Re-index the C project
    * 
    * @param project Project to index
    * @param monitor Progress monitor
    */
   public void reindexProject(IProject project, IProgressMonitor monitor) {
      try {
         monitor.beginTask("Update configuration", IProgressMonitor.UNKNOWN);
         ICProject cproject = CoreModel.getDefault().getCModel().getCProject(project.getName());
         CCorePlugin.getIndexManager().reindex(cproject);
      }
      finally {
         monitor.done();
      }
   }

   @Override
   public boolean performFinish() {
      try {
         fUsbdmNewProjectPage_1.saveSettings();
         fUsbdmProjectParametersPage_2.saveSettings();
         for (UsbdmDynamicOptionPage_N page:fDynamicWizardPages) {
            page.saveSettings();
         }
         updateParamMap();
         listParamMap("fParamMap\n===========================================", fParamMap);
         
         getContainer().run(true, false, this);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return true;
   }

   /**
    * Used to suppress C indexing
    */
   static class MyIndexerSetupParticipant extends IndexerSetupParticipant {
      IProject fProject;
      
      MyIndexerSetupParticipant(IProject project) {
         fProject = project;
      }
      @Override
      public boolean postponeIndexerSetup(ICProject cProject) {
         IProject project = cProject.getProject() ;
         return project == fProject;
      }
   }
   
   @Override
   public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
      final int WORK_SCALE = 1000;
      SubMonitor monitor   = SubMonitor.convert(progressMonitor, WORK_SCALE);

//      System.err.println("UsbdmNewProjectWizard.run()");

      // Turn off Auto-build in workspace
      // TODO - should restore to original after project construction?
      IWorkspace            workspace     = ResourcesPlugin.getWorkspace();
      IWorkspaceDescription workspaceDesc = workspace.getDescription();
      workspaceDesc.setAutoBuilding(false);
      try {
         workspace.setDescription(workspaceDesc);
      } catch (CoreException e1) {
      }
      
      IndexerSetupParticipant indexerParticipant = null;
      IProject project = null;
      try {
         monitor.beginTask("Creating USBDM Project", WORK_SCALE*100);

         if (CCorePlugin.getDefault() == null) {
            Activator.log("CCorePlugin not found (for testing)");
            return;
         }
         // Create project
         project = new CDTProjectManager().createUSBDMProject(fParamMap, monitor.newChild(WORK_SCALE * 20));

         // Used to suppress indexing while project is constructed
         indexerParticipant = new MyIndexerSetupParticipant(project);
         
         // Suppress project indexing while project is constructed
         CCorePlugin.getIndexManager().addIndexerSetupParticipant(indexerParticipant);

         // Apply device project options etc
         ProcessProjectActions.process(this, project, fDevice, fProjectActionList, fParamMap, monitor.newChild(WORK_SCALE * 20));

         // Generate CPP code as needed
         DeviceInfo.generateFiles(project, monitor.newChild(WORK_SCALE * 5));

         project.refreshLocal(IResource.DEPTH_INFINITE, monitor.newChild(WORK_SCALE));

//         reindexProject(project, monitor.newChild(WORK_SCALE * 20));

         CoreModel.getDefault().updateProjectDescriptions(new IProject[]{project}, monitor.newChild(WORK_SCALE));

         // Allow indexing and re-index
         final IIndexManager indexManager = CCorePlugin.getIndexManager();
         final ICProject cProject = CoreModel.getDefault().create(project);
         indexerParticipant.notifyIndexerSetup(cProject);
         monitor.subTask("Refreshing Index...");
         indexManager.reindex(cProject);
         indexManager.joinIndexer(IIndexManager.FOREVER, monitor.newChild(WORK_SCALE)); 

         boolean       hasCCNature   = Boolean.valueOf(fParamMap.get(UsbdmConstants.HAS_CC_NATURE_KEY));
         if (hasCCNature) {
            System.err.println("Last ditch adding CC nature");
            CCProjectNature.addCCNature(project, monitor.newChild(WORK_SCALE));
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new InvocationTargetException(e);
      } finally {
         // Allow indexing if suspended
         if (indexerParticipant != null) {
            CCorePlugin.getIndexManager().removeIndexerSetupParticipant(indexerParticipant);
         }
         monitor.done();
      }
   }
   
   @Override
   public IWizardPage getPage(String name) {
      if (fUsbdmNewProjectPage_1.getName().equals(name)) {
         return fUsbdmNewProjectPage_1;
      }
      if (fUsbdmProjectParametersPage_2.getName().equals(name)) {
         return fUsbdmProjectParametersPage_2;
      }
      for (UsbdmDynamicOptionPage_N page:fDynamicWizardPages) {
         if (page.getName().equals(name)) {
            return page;
         }
      }
      return null;
   }

   public Map<String, String> getparamMap() {
      return fParamMap;
   }
   
}