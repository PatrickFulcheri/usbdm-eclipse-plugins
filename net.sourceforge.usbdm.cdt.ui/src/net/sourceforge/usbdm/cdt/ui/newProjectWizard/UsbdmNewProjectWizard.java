package net.sourceforge.usbdm.cdt.ui.newProjectWizard;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.usbdm.cdt.ui.Activator;
import net.sourceforge.usbdm.constants.UsbdmSharedConstants;
import net.sourceforge.usbdm.constants.UsbdmSharedConstants.InterfaceType;
import net.sourceforge.usbdm.deviceDatabase.Device;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class UsbdmNewProjectWizard extends Wizard implements INewWizard, IRunnableWithProgress {
   
   UsbdmNewProjectPage              usbdmProjectSelectionPage  = null;
   UsbdmProjectParametersPage       usbdmProjectPage           = null;
   UsbdmProjectOptionsPage          usbdmProjectOptionsPage    = null;
   UsbdmToolSettingsPage            usbdmToolSettingsPage      = null;
   
   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection) {
      setNeedsProgressMonitor(true);
      setDefaultPageImageDescriptor(UsbdmSharedConstants.getUsbdmIcon());
      
      IDialogSettings settings = null;
      Activator plugin = Activator.getDefault();
      if (plugin == null) {
         System.err.println("*************************** plugin is null *********************");
      }
      if (plugin != null) {
         settings = plugin.getDialogSettings();
         if (settings == null) {
            System.err.println("*************************** settings is null *********************");
         }
      }
      setDialogSettings(settings);
   }

   @Override
   public boolean performFinish() {
      if (usbdmProjectSelectionPage != null) {
         usbdmProjectSelectionPage.saveSettings();
      }
      if (usbdmProjectPage != null) {
         usbdmProjectPage.saveSettings();
      }
      if (usbdmProjectOptionsPage != null) {
         usbdmProjectOptionsPage.saveSettings();
      }
      if (usbdmToolSettingsPage != null) {
         usbdmToolSettingsPage.saveSettings();
      }
      try {
         IWizardContainer container = getContainer();
         if (container != null) {
            container.run(false, true, this);
         }
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      return true;
   }

   @Override
   public void addPages() {
      usbdmProjectSelectionPage  = new UsbdmNewProjectPage();
      addPage(usbdmProjectSelectionPage);
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
    */
   @Override
   public IWizardPage getStartingPage() {
      return usbdmProjectSelectionPage;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#canFinish()
    */
   @Override
   public boolean canFinish() {
//      super.canFinish();
      return ((usbdmProjectSelectionPage != null) && usbdmProjectSelectionPage.isPageComplete()) &&
             ((usbdmProjectPage != null)          && usbdmProjectPage.isPageComplete()) &&
             ((usbdmProjectOptionsPage != null)   && usbdmProjectOptionsPage.isPageComplete()) &&
             ((usbdmToolSettingsPage != null)     && usbdmToolSettingsPage.isPageComplete()) &&
             (getContainer() != null) && 
             ((getContainer().getCurrentPage() == usbdmProjectOptionsPage) || 
              (getContainer().getCurrentPage() == usbdmToolSettingsPage));
 }

   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#needsPreviousAndNextButtons()
    */
   @Override
   public boolean needsPreviousAndNextButtons() {
      return true;
   }

   private Device lastSelectedDevice = null;
   
   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
    */
   @Override
   public IWizardPage getNextPage(IWizardPage page) {
      if (page == usbdmProjectSelectionPage) {
         InterfaceType interfaceType = usbdmProjectSelectionPage.getInterfaceType();
         // Create new project page if none or interface has changed
         if ((usbdmProjectPage == null) || (usbdmProjectPage.getInterfaceType() != interfaceType)) {
            usbdmProjectPage = new UsbdmProjectParametersPage(usbdmProjectSelectionPage);
            usbdmProjectPage.setWizard(this);
            usbdmProjectOptionsPage = null;
            usbdmToolSettingsPage   = null;
         }
         return usbdmProjectPage;
      }
      if (page == usbdmProjectPage) {
         Map<String, String> paramMap = new HashMap<String, String>();
         Device device = usbdmProjectPage.getDevice();
         // Create new option page if none or device has changed
         if ((usbdmProjectOptionsPage == null) || (device != lastSelectedDevice)) {

            try {
               usbdmProjectPage.getPageData(paramMap);
            } catch (Exception e) {
            }
            usbdmProjectOptionsPage = new UsbdmProjectOptionsPage(usbdmProjectPage, paramMap);
            usbdmProjectOptionsPage.setWizard(this);
            usbdmToolSettingsPage   = null;
         }
         lastSelectedDevice = device;
         return usbdmProjectOptionsPage;
      }
      if (page == usbdmProjectOptionsPage) {
         if (usbdmToolSettingsPage == null) {
            usbdmToolSettingsPage =  new UsbdmToolSettingsPage(usbdmProjectPage);
            usbdmToolSettingsPage.setWizard(this);
         }
         return usbdmToolSettingsPage;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
    */
   @Override
   public IWizardPage getPreviousPage(IWizardPage page) {
      if (page == usbdmToolSettingsPage) {
         return usbdmProjectOptionsPage;
      }
      if (page == usbdmProjectOptionsPage) {
         return usbdmProjectPage;
      }
      if (page == usbdmProjectPage) {
         return usbdmProjectSelectionPage;
      }
      return null;
   }

   @Override
   public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      Map<String, String> map = new HashMap<String, String>(); 
            
      try {
         usbdmProjectPage.getPageData(map);
         usbdmProjectOptionsPage.getPageData(map);
         usbdmToolSettingsPage.getPageData(map);
         map.put("projectName", usbdmProjectSelectionPage.getProjectName());

//         System.err.println("UsbdmNewProjectWizard.run()");
         new CDTProjectManager().createCDTProj(
               usbdmProjectSelectionPage.getProjectName(), 
               usbdmProjectSelectionPage.getProjectLocation(), 
               usbdmProjectSelectionPage.getInterfaceType(),
               usbdmProjectSelectionPage.isCCNature(),
               usbdmProjectPage.getDevice(),
               map,
               monitor);
      } catch (Exception e) {
         e.printStackTrace();
         throw new InvocationTargetException(e);
      }      
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.jface.wizard.Wizard#getPage(java.lang.String)
    */
   @Override
   public IWizardPage getPage(String name) {
      if ((usbdmProjectSelectionPage != null) && usbdmProjectSelectionPage.isPageComplete()) {
         return usbdmProjectSelectionPage;
      }
      if ((usbdmProjectPage != null)          && usbdmProjectPage.isPageComplete()) {
         return usbdmProjectPage;
      }
      if ((usbdmProjectOptionsPage != null)   && usbdmProjectOptionsPage.isPageComplete()) {
         return usbdmProjectOptionsPage;
      }
      if ((usbdmToolSettingsPage != null)     && usbdmToolSettingsPage.isPageComplete()) {
         return usbdmToolSettingsPage;
      }
      return null;
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      Display display = new Display();
      Shell shell = new Shell(display);

      // Instantiates and initialises the wizard
      UsbdmNewProjectWizard wizard = new UsbdmNewProjectWizard();
      wizard.init(null,null);
      
      // Instantiates the wizard container with the wizard and opens it
      WizardDialog dialog = new WizardDialog(shell, wizard);
      dialog.create();
      dialog.open();
   }

   
}