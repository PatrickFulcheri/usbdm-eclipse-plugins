package net.sourceforge.usbdm.cdt.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class GdbServerHandler extends AbstractHandler {
   GdbServerParameters serverParameters   = null;
   GdbServerInterface  gdbServerInterface = null;

   protected GdbServerHandler(GdbServerParameters serverParameters) {
      this.serverParameters = serverParameters;
      gdbServerInterface = new GdbServerInterface(serverParameters);
   }

   /**
    *  Start USBDM GDB Server
    */
   public Object execute(ExecutionEvent event) throws ExecutionException {
      IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

      System.err.println("GdbServerHandler.execute()\n");
      
      serverParameters.loadDefaultSettings();
      gdbServerInterface.startServer(window.getShell());

      return null;
   }
}


