package net.sourceforge.usbdm.deviceEditor.parser;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.Peripheral;
import net.sourceforge.usbdm.deviceEditor.information.PeripheralFunction;
import net.sourceforge.usbdm.deviceEditor.information.PeripheralTemplateInformation;
import net.sourceforge.usbdm.deviceEditor.information.PinInformation;

/**
 * Class encapsulating the code for writing an instance of DigitalIO
 */
/**
 * @author podonoghue
 *
 */
public class WriterForLptmr extends Peripheral {

   public WriterForLptmr(String basename, String instance, PeripheralTemplateInformation template, DeviceInfo deviceInfo) {
      super(basename, instance, template, deviceInfo);
   }

   /* (non-Javadoc)
    * @see InstanceWriter#getInstanceName(MappingInfo, int)
    */
   @Override
   public String getInstanceName(MappingInfo mappingInfo, int fnIndex) {
      String instance = mappingInfo.getFunctions().get(fnIndex).getPeripheral().getInstance();
      String signal   = mappingInfo.getFunctions().get(fnIndex).getSignal();
      return getClassName()+instance+"_"+signal;
   }

   /** 
    * Get declaration as string e.g. 
    * <pre>
    * const USBDM::Gpio<b><i>A</b></i>&lt;<b><i>0</b></i>&gt;</b></i>
    * </pre>
    * @param mappingInfo    Mapping information (pin and peripheral function)
    * @param fnIndex        Index into list of functions mapped to pin
    */
   @Override
   protected String getDeclaration(MappingInfo mappingInfo, int fnIndex) {
      return null;
   }
   
   @Override
   public int getFunctionIndex(PeripheralFunction function) {
      final String signalNames[] = {"ALT0", "ALT1", "ALT2", "ALT3"};
      for (int signal=0; signal<signalNames.length; signal++) {
         if (function.getSignal().matches(signalNames[signal])) {
            return signal;
         }
      }
      throw new RuntimeException("Signal "+function.getSignal()+" does not match expected pattern ");
   }
   
   @Override
   public String getAliasDeclaration(String alias, MappingInfo mappingInfo, int fnIndex) {
      return null;
   }

   @Override
   public boolean useAliases(PinInformation pinInfo) {
      return false;
   }

   @Override
   public String getTitle() {
      return "Low Power Timer";
   }

   @Override
   public String getGroupBriefDescription() {
      return "Pins used for Low Power Timer";
   }

   @Override
   public String getAliasName(String signalName, String alias) {
      return null;
   }
}