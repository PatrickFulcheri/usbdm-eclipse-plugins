package net.sourceforge.usbdm.cdt.ui.newProjectWizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.usbdm.deviceDatabase.Device;
import net.sourceforge.usbdm.packageParser.CreateFolderAction;
import net.sourceforge.usbdm.packageParser.ExcludeAction;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ProjectUtilities {
   
   static void listSourceEntries(ICSourceEntry[] sourceEntries) {
      for (ICSourceEntry cSourceEntry : sourceEntries) {
         System.err.println(String.format("entry = \"%s\" => ", 
               cSourceEntry.getFullPath()));
         for (IPath path : cSourceEntry.getExclusionPatterns()) {
            System.err.println(String.format("exclusion path = \"%s\" => ", path));
         }
      }
   }
   
   private static void changeExcludedItem(IProject project, String targetPath, boolean isFolder, boolean excluded, IProgressMonitor progressMonitor) 
         throws CoreException, BuildException {
      
//      System.err.println(String.format("changeExcludedItem(p=%s, f=%s, e=%s)", targetPath, Boolean.toString(isFolder), Boolean.toString(excluded)));

      IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
      // Add to Include search paths
      IConfiguration[] configurations = info.getManagedProject().getConfigurations();
      for (IConfiguration configuration : configurations) {
         // Exclude in each configuration
         IPath           path                  = project.getFolder(targetPath).getProjectRelativePath();
         ICSourceEntry[] sourceEntries         = configuration.getSourceEntries();
         ICSourceEntry[] modifiedSourceEntries = CDataUtil.setExcludedIfPossible(path, isFolder, excluded, sourceEntries);
         configuration.setSourceEntries(modifiedSourceEntries);
//         System.err.println("changeExcludedItem() - Path = " + path.toPortableString());
//         System.err.println("Before");
//         listSourceEntries(sourceEntries);
//         System.err.println("After #1");
//         listSourceEntries(x);
//         System.err.println("After #2");
//         listSourceEntries(sourceEntries);
      }
   }

   public static void excludeItem(IProject project, ExcludeAction action, IProgressMonitor monitor) 
         throws Exception {

      String  target      = action.getTarget();
      boolean isFolder    = action.isFolder();
      boolean isExcluded  = action.isExcluded();
      
      changeExcludedItem(project, target, isFolder, isExcluded, monitor);
   }
   
   public static void createIncludeFolder(IProject project, String targetPath, IProgressMonitor progressMonitor) throws CoreException, BuildException {
//      createSourceFolder(project, targetPath, progressMonitor);
      createFolder(project, targetPath, progressMonitor);
      IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
      // Add to Include search paths
      IConfiguration[] configs = info.getManagedProject().getConfigurations();
      for (IConfiguration config : configs) {
         // Creates include folder path that is portable (e.g. rename project doesn't break paths)
         String path = "\"${ProjDirPath}/" + project.getFolder(targetPath).getProjectRelativePath().toOSString() + "\"";

         IToolChain toolChain = config.getToolChain();
         setIncludePathOptionForConfig(path, config, toolChain.getOptions(), toolChain);

         ITool[] tools = config.getTools();
         for (ITool tool : tools) {
            setIncludePathOptionForConfig(path, config, tool.getOptions(), tool);
         }
      }
      ManagedBuildManager.saveBuildInfo(project, true);
   }

   private static void setIncludePathOptionForConfig(String path, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
      for (IOption option : options) {
         if (option.getValueType() == IOption.INCLUDE_PATH) {
            String[] includePaths = option.getIncludePaths();
            String[] newPaths = new String[includePaths.length + 1];
            System.arraycopy(includePaths, 0, newPaths, 0, includePaths.length);
                newPaths[includePaths.length] = path;
            ManagedBuildManager.setOption(config, optionHolder, option, newPaths);
         }
      }
   }
   
   /**
    * Creates source folder in the project recursively.
    *
    * @param projectHandle - project.
    * @param targetPath - relative path to the new folder.
    * @param monitor - progress monitor.
    * 
    * @throws CoreException 
    */
   public static void createSourceFolder(IProject projectHandle, String targetPath, IProgressMonitor monitor) throws CoreException {
      //If the targetPath is an empty string, there will be no source folder to create.
      // Also this is not an error. So just return gracefully.
      if (targetPath == null || targetPath.length()==0) {
         return;
      }
      createFolder(projectHandle, targetPath, monitor);

      IPath projPath = projectHandle.getFullPath();
      IFolder folder = projectHandle.getFolder(targetPath);

      ICProject cProject = CoreModel.getDefault().create(projectHandle);
      if (cProject != null) {
         if(CCorePlugin.getDefault().isNewStyleProject(cProject.getProject())){
            //create source folder for new style project
            createNewStyleProjectFolder(monitor, projectHandle, folder);
         } else {
            //create source folder for all other projects 
            createFolder(targetPath, monitor, projPath, cProject);
         }
      }
   }

   /**
    * Creates specified folder in the project recursively.
    *
    * @param project            - project.
    * @param targetPath         - relative path to the new folder.
    * @param progressMonitor    - progress monitor.
    * 
    * @throws CoreException 
    */
   public static void createFolder(IProject project, String targetPath, IProgressMonitor progressMonitor) throws CoreException {
      //If the targetPath is an empty string, there will be no folder to create.
      // Also this is not an error. So just return gracefully.
      if (targetPath == null || targetPath.length()==0) {
         return;
      }
      IPath path = new Path(targetPath);
      for (int i=1; i<=path.segmentCount(); i++) {
         IFolder subfolder = project.getFolder(path.uptoSegment(i));
         if (!subfolder.exists()) {
            subfolder.create(true, true, progressMonitor);
         }
      }
   }

   /**
    * @param monitor
    * @param projectHandle
    * @param folder
    * @throws CoreException
    * @throws WriteAccessException
    */
   private static void createNewStyleProjectFolder(IProgressMonitor monitor,
         IProject projectHandle, IFolder folder) throws CoreException,
         WriteAccessException {
      ICSourceEntry newEntry = new CSourceEntry(folder, null, 0);
      ICProjectDescription description = CCorePlugin.getDefault()
            .getProjectDescription(projectHandle);

      ICConfigurationDescription configs[] = description.getConfigurations();
      for (int i = 0; i < configs.length; i++) {
         ICConfigurationDescription config = configs[i];
         ICSourceEntry[] entries = config.getSourceEntries();
         Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
         for (int j = 0; j < entries.length; j++) {
            if (new Path(entries[j].getValue()).segmentCount() == 1)
               continue;
            set.add(entries[j]);
         }
         set.add(newEntry);
         config.setSourceEntries(set.toArray(new ICSourceEntry[set.size()]));
      }

      CCorePlugin.getDefault().setProjectDescription(projectHandle,
            description, false, monitor);
   }
   
   /**
    * @param targetPath
    * @param monitor
    * @param projPath
    * @param cProject
    * @throws CModelException
    */
   private static void createFolder(String targetPath, IProgressMonitor monitor, IPath projPath, ICProject cProject) throws CModelException {
      IPathEntry[] entries = cProject.getRawPathEntries();
      List<IPathEntry> newEntries = new ArrayList<IPathEntry>(entries.length + 1);

      int projectEntryIndex= -1;
      IPath path = projPath.append(targetPath);

      for (int i = 0; i < entries.length; i++) {
         IPathEntry curr = entries[i];
         if (path.equals(curr.getPath())) {
            // just return if this folder exists already
            return;
         }
         if (projPath.equals(curr.getPath())) {
            projectEntryIndex = i;
         }  
         newEntries.add(curr);
      }

      IPathEntry newEntry = CoreModel.newSourceEntry(path);

      if (projectEntryIndex != -1) {
         newEntries.set(projectEntryIndex, newEntry);
      } else {
         newEntries.add(CoreModel.newSourceEntry(path));
      }

      cProject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
   }

   public static void createFolder(IProject projectHandle, Device device, Map<String, String> variableMap, CreateFolderAction action, IProgressMonitor monitor) throws Exception {
      String target = MacroSubstitute.substitute(action.getTarget(), variableMap);
      
      if (target.isEmpty()) {
         return;
      }
//      System.err.println(String.format("ProjectUtilities.createFolder() \'%s\'", target));

      String type = action.getType();
      if (type == null) {
         type = "";
      }
      try {
         if (type.equalsIgnoreCase("source")) {
            createSourceFolder(projectHandle, target, monitor);
         }
         else if (type.equalsIgnoreCase("include")) {
            createIncludeFolder(projectHandle, target, monitor);
         }
         else {
            createFolder(projectHandle, target, monitor);
         }
      } catch (Exception e) {
         throw new Exception("Failed to create folder \'"+target+"\'", e);
      }
   }
   
}
