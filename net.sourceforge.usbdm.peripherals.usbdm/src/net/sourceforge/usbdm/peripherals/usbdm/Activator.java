package net.sourceforge.usbdm.peripherals.usbdm;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

   /**
    * Get bundle context for this plug-in
    * 
    * @return Bundle context
    */
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
      System.err.println(String.format("[%s].start()", "net.sourceforge.usbdm.peripherals.usbdm.Activator"));
		context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
      System.err.println(String.format("[%s].start()", "net.sourceforge.usbdm.peripherals.usbdm.Activator"));
		context = null;
	}

}
