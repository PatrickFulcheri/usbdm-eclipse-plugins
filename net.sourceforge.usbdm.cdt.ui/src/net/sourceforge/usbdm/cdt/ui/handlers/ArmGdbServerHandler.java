package net.sourceforge.usbdm.cdt.ui.handlers;

import net.sourceforge.usbdm.constants.UsbdmSharedConstants.InterfaceType;
import net.sourceforge.usbdm.gdb.server.GdbServerParameters;


public class ArmGdbServerHandler extends GdbServerHandler {

   public ArmGdbServerHandler() throws Exception {
      super(GdbServerParameters.getDefaultServerParameters(InterfaceType.T_ARM));
   }
}
