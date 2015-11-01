package net.sourceforge.usbdm.peripherals.configuration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.usbdm.peripherals.configuration"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

   @Override
   protected void initializeImageRegistry(ImageRegistry registry) {
       super.initializeImageRegistry(registry);
       Bundle bundle = Platform.getBundle(PLUGIN_ID);
       
       ImageDescriptor imageDescriptor;
       imageDescriptor = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/cog.png"), null));
       registry.put(ID_COG_IMAGE, imageDescriptor);
   }

   public final static String ID_COG_IMAGE                    = "cog-image";
   
   /**
    * Returns an image descriptor for the image file with the given key
    *
    * @param path the path
    * @return the image descriptor
    */
   public ImageDescriptor getImageDescriptor(String key) {
      return getImageRegistry().getDescriptor(key);
   }
   

}
