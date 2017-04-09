package net.sourceforge.usbdm.deviceEditor.peripherals;
import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;

/**
 * Class encapsulating the code for writing an instance of LLWU
 */
public class WriterForFtfl extends PeripheralWithState {

   public WriterForFtfl(String basename, String instance, DeviceInfo deviceInfo) {
      super(basename, instance, deviceInfo);
   }

   @Override
   public String getTitle() {
      return "Flash Memory Module";
   }

}