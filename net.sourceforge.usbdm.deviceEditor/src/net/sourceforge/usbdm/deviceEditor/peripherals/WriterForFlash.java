package net.sourceforge.usbdm.deviceEditor.peripherals;
import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;

/**
 * Class encapsulating the code for writing an instance of LLWU
 */
public class WriterForFlash extends PeripheralWithState {

   public WriterForFlash(String basename, String instance, DeviceInfo deviceInfo) {
      super(basename, instance, deviceInfo);
   }

   @Override
   public String getTitle() {
      return "Flash Memory Module";
   }

}