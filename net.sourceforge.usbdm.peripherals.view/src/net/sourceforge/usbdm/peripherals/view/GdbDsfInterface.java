package net.sourceforge.usbdm.peripherals.view;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.model.MemoryByte;

@SuppressWarnings("restriction")
public class GdbDsfInterface implements GdbCommonInterface {

   private DsfSession dsfSession  = null;

   GdbDsfInterface(DsfSession dsfSession) {
      this.dsfSession  = dsfSession;
   }

   /*
    * ========================================================
    *  Target access (via GDB)
    * ========================================================
    */
   /**
    *  Read from target memory
    *  
    *  @param address   address in target memory
    *  @param size      number of bytes to read
    *  
    *  @return byte[size] containing the data read 
    */
   private byte[] readMemory(long address, int size) throws TimeoutException {
      
      System.err.println("GDBInterface.readMemory(DSF)");
      if (dsfSession == null) {
         return null;
      }
      byte[] ret = null;
      DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), dsfSession.getId());

      final IGDBControl fGdb = (IGDBControl) tracker.getService(IGDBControl.class);
      if (fGdb == null) {
         return null;
      }
      CommandFactory factory = fGdb.getCommandFactory();
      final ICommand<MIDataReadMemoryInfo> info_rm = 
            factory.createMIDataReadMemory(fGdb.getContext(), 0L, Long.toString(address), 0, 1, 1, size, null);

      Query<MIDataReadMemoryInfo> query = new Query<MIDataReadMemoryInfo>() {
         @Override
         protected void execute(final DataRequestMonitor<MIDataReadMemoryInfo> rm) {
            DataRequestMonitor<MIDataReadMemoryInfo> dataRequestMonitor = new DataRequestMonitor<MIDataReadMemoryInfo>(fGdb.getExecutor(), null) {
               protected void handleCompleted() {
                  rm.setData((MIDataReadMemoryInfo) getData());
                  rm.done();
               }
            };
            fGdb.queueCommand(info_rm, dataRequestMonitor);
         }
      };
      ImmediateExecutor.getInstance().execute(query);
      MIDataReadMemoryInfo data = null;
      try {
         data = (MIDataReadMemoryInfo) query.get(maxWaitTimeInInMilliseconds, TimeUnit.MILLISECONDS);
         if (data.isError()) {
            data = null;
         }
         if (data != null) {
            MemoryByte[] bytes = data.getMIMemoryBlock();
            byte[] arraybytes = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
               arraybytes[i] = bytes[i].getValue();
            }
            ret = arraybytes;
         }
      } catch (InterruptedException localInterruptedException) {
      } catch (ExecutionException localExecutionException) {
      } catch (TimeoutException localTimeoutException) {
         throw new TimeoutException("readMemory - failed, ErrorMessage: waiting time of " + maxWaitTimeInInMilliseconds + " ms passed");
      } finally {
         tracker.dispose();
      }
      return ret;
   }
   
   private static int maxWaitTimeInInMilliseconds = 1000;

   /**
    * Wrapper that handles DSF or MI memory reads
    * 
    * @param address       Address to read from
    * @param iByteCount    Number of bytes to read
    * @param accessWidth   Access size (1,2,4) to use
    * 
    * @return              Data read
    * 
    * @throws Exception 
    */
   @Override
   public byte[] readMemory(long address, int iByteCount, int accessWidth) throws Exception {
      
      // TODO - This is a horrible hack - damn you GDB!
      switch (accessWidth) {
      case 8:  
         // Check if either address or size is odd
         // This will be OK as treated as byte access by USBDM
         if (((address|iByteCount)&0x1) == 0) {
            // Must split block otherwise seen as word/half-word size access
            byte[] data1 = readMemory(address,              1, accessWidth);
            byte[] data2 = readMemory(address+1, iByteCount-1, accessWidth);
            byte[] data = new byte[iByteCount];
            data[0] = data1[0];
            System.arraycopy(data2, 0, data, 1, iByteCount-1);
            return data;
         }
         break;
      case 16:
         // Check if either address or size is not a multiple of 4 - 
         // This will be OK as treated as byte/half-word access by USBDM
         if (((address|iByteCount)&0x3) == 0) {
            // Must split block otherwise seen as word size access
            byte[] data1 = readMemory(address,              2, accessWidth);
            byte[] data2 = readMemory(address+2, iByteCount-2, accessWidth);
            byte[] data = new byte[iByteCount];
            data[0] = data1[0];
            data[1] = data1[1];
            System.arraycopy(data2, 0, data, 2, iByteCount-2);
            return data;
         }
         break;
      case 32:
         // Always considered OK
         break;
      default: 
         throw new Exception("Illegal access size"); 
      }
//      System.err.println(String.format("GDBInterface.readMemory(0x%08X, %d)", address, iByteCount));
         return readMemory(address, iByteCount);
   }
   
   /**
    * @param address
    * @param data
    * 
    * @return Number of bytes written (-1 => error)
    */
   private int writeMemory(long address, byte[] data) {
      System.err.println("GDBInterface.writeMemory(DSF)");

      if ((dsfSession == null)) {
         return -1;
      }
      StringBuffer buffer = new StringBuffer(10+(2*data.length));
      buffer.append("0x");

      for (int index = data.length - 1; index >= 0; index--) {
         buffer.append(String.format("%02X", data[index]));
      }

      String value = buffer.toString();

      int ret = -1;
      DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), dsfSession.getId());
      final IGDBControl fGdb = tracker.getService(IGDBControl.class);
      if (fGdb == null) {
         return -1;
      }
      org.eclipse.cdt.dsf.mi.service.command.CommandFactory factory = fGdb.getCommandFactory();
      int format;
      if (value.contains("x")) {
         format = MIFormat.HEXADECIMAL;
      } else {
         format = MIFormat.DECIMAL;
      }
      final ICommand<MIDataWriteMemoryInfo> info_wm = 
            factory.createMIDataWriteMemory(fGdb.getContext(), 0L, Long.toString(address), format, data.length, value);
      Query<MIDataWriteMemoryInfo> query = new Query<MIDataWriteMemoryInfo>() {
         protected void execute(
            final DataRequestMonitor<MIDataWriteMemoryInfo> rm) {
               fGdb.queueCommand( info_wm, new DataRequestMonitor<MIDataWriteMemoryInfo>(fGdb.getExecutor(), null) {
                     protected void handleCompleted() {
                        rm.setData((MIDataWriteMemoryInfo) getData());
                        rm.done();
                     }
                  });
         }
      };
      ImmediateExecutor.getInstance().execute(query);
      MIDataWriteMemoryInfo data2 = null;
      try {
         data2 = (MIDataWriteMemoryInfo) query.get(maxWaitTimeInInMilliseconds,
               TimeUnit.MILLISECONDS);
         if (data2.isError()) {
            data2 = null;
         }
         ret = data2 != null ? data.length : -1;
      } catch (InterruptedException localInterruptedException) {
         System.err.println("GDBInterface.readMemory() - failed, ErrorMessage: Interrupted Exception");
      } catch (ExecutionException localExecutionException) {
         System.err.println("GDBInterface.readMemory() - failed, ErrorMessage: Execution Exception");
      } catch (TimeoutException localTimeoutException) {
         System.err.println("GDBInterface.readMemory() - failed, ErrorMessage: waiting time of " + maxWaitTimeInInMilliseconds + " ms expired");
      } finally {
         tracker.dispose();
      }
      return ret;
   }
       
   /**
    * Wrapper that handles DSF or MI memory writes
    * 
    * @param address    Address to write at
    * @param data       Data to write.  This must be 1, 2, 4 or 8 bytes due to limitations of underlying GDB command used
    * 
    * @throws TimeoutException
    */
   @Override
   public void writeMemory(long address, byte[] data, int accessWidth) throws TimeoutException {
      System.err.println(String.format("GDBInterface.writeMemory(0x%08X, %d)", address, data.length));
      writeMemory(address, data);
   }

}
