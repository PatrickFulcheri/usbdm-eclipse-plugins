package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.io.IOException;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.Signal;
import net.sourceforge.usbdm.jni.UsbdmException;

/**
 * Class encapsulating the code for writing an instance of SPI
 */
public class WriterForSpi extends PeripheralWithState {
   
   public WriterForSpi(String basename, String instance, DeviceInfo deviceInfo) throws IOException, UsbdmException {
      super(basename, instance, deviceInfo);
   }

   @Override
   public String getTitle() {
      return "Serial Peripheral Interface";
   }

   @Override
   public int getSignalIndex(Signal function) {
      final String signalNames[] = {"SCK", "SIN|MISO", "SOUT|MOSI", "PCS0|PCS|SS|SS_b", "PCS1", "PCS2", "PCS3", "PCS4", "PCS5"};
      return getSignalIndex(function, signalNames);
   }
}