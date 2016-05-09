package net.sourceforge.usbdm.deviceEditor.peripherals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.Signal;

/**
 * Class encapsulating the code for writing an instance of RTC
 */
public class WriterForSdram extends Peripheral {

   public WriterForSdram(String basename, String instance, DeviceInfo deviceInfo) {
      super(basename, instance, deviceInfo);
   }

   @Override
   public String getTitle() {
      return "Synchronous DRAM Controller";
   }

   @Override
   public int getSignalIndex(Signal function) {
      Pattern p;
      Matcher m;
      int offset = 0;
      
      p = Pattern.compile("A(\\d+)");
      m = p.matcher(function.getSignalName());
      if (m.matches()) {
         return offset+Integer.parseInt(m.group(1));
      }
      offset += 32;

      p = Pattern.compile("D(\\d+)");
      m = p.matcher(function.getSignalName());
      if (m.matches()) {
         return offset+Integer.parseInt(m.group(1));
      }
      offset += 32;

      p = Pattern.compile("DQM(\\d+)");
      m = p.matcher(function.getSignalName());
      if (m.matches()) {
         return offset+Integer.parseInt(m.group(1));
      }
      offset += 4;

      final String signalNames[] = {"RAS(_b)?", "CAS(_b)?", "WE", "CKE", "CS0(_b)?", "CS1(_b)?"};
      return offset+getSignalIndex(function, signalNames);
   }
}