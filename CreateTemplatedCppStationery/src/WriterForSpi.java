/**
 * Class encapsulating the code for writing an instance of DigitalIO
 */
/**
 * @author podonoghue
 *
 */
class WriterForSpi extends InstanceWriter {

   static final String ALIAS_BASE_NAME       = "spi_";
   static final String CLASS_BASE_NAME       = "Spi";
   static final String INSTANCE_BASE_NAME    = "spi";

   public WriterForSpi(boolean deviceIsMKE) {
      super(deviceIsMKE);
   }

   /* (non-Javadoc)
    * @see InstanceWriter#getAliasName(java.lang.String)
    */
   @Override
   public String getAliasName(String signalName, String alias) {
      return ALIAS_BASE_NAME+alias;
   }

   /* (non-Javadoc)
    * @see InstanceWriter#getInstanceName(MappingInfo, int)
    */
   @Override
   public String getInstanceName(MappingInfo mappingInfo, int fnIndex) {
      String instance = mappingInfo.functions.get(fnIndex).fPeripheral.fInstance;
      String signal   = mappingInfo.functions.get(fnIndex).fSignal;
      return INSTANCE_BASE_NAME+instance+"_"+signal;
   }

   /** 
    * Get declaration as string e.g. 
    * <pre>
    * const USBDM::Gpio<b><i>A</b></i>&lt;<b><i>0</b></i>&gt;</b></i>
    * </pre>
    * @param mappingInfo    Mapping information (pin and peripheral function)
    * @param cppFile        Where to write
    */
   protected String getDeclaration(MappingInfo mappingInfo, int fnIndex) {
      String instance  = mappingInfo.functions.get(fnIndex).fPeripheral.fInstance;
      String signal    = Integer.toString(getFunctionIndex(mappingInfo.functions.get(fnIndex)));
      return "const " + CreatePinDescription.NAME_SPACE + "::PcrTable_T<" + CLASS_BASE_NAME + instance + "Info, " + signal + ">" ;
   }
//   /* (non-Javadoc)
//    * @see InstanceWriter#needPcrTable()
//    */
//   @Override
//   public boolean needPeripheralInformationClass() throws Exception {
//      boolean required = fOwner.getFunctions().size() > 0;
//      if (!required) {
//         if ((fOwner.getClockReg() != null) || (fOwner.getClockMask() != null)) {
//            throw new Exception("Unexpected clock information for non-present peripheral " + fOwner.peripheralName);
//         }
//      }
//      return required;
//   }

   @Override
   public int getFunctionIndex(PeripheralFunction function) {
      final String signalNames[] = {"SCK", "SIN|MISO", "SOUT|MOSI", "PCS0|PCS", "PCS1", "PCS2", "PCS3", "PCS4", "PCS5"};
      for (int signal=0; signal<signalNames.length; signal++) {
         if (function.fSignal.matches(signalNames[signal])) {
            return signal;
         }
      }
      throw new RuntimeException("Signal does not match expected pattern " + function.fSignal);
   }
   
   static final String TEMPLATE_DOCUMENTATION = 
         "/**\n"+
         " * Convenience templated class representing an SPI pin\n"+
         " *\n"+
         " * Example\n"+
         " * @code\n"+
         " * using spi0_PCS0 = const USBDM::Spi0Pin<3>;\n"+
         " * @endcode\n"+
         " *\n"+
         " * @tparam spiPinNum    SPI pin number (index into SpiInfo[])\n"+
         " */\n";

   @Override
   public String getAlias(String alias, MappingInfo mappingInfo, int fnIndex) {
      return null;
   }

   @Override
   public String getDefinition(MappingInfo mappingInfo, int fnIndex) {
      return super.getAlias(getInstanceName(mappingInfo, fnIndex), mappingInfo, fnIndex);
   }

   @Override
   public String getExternDeclaration(MappingInfo mappingInfo, int fnIndex) throws Exception {
      return "extern " + getDefinition(mappingInfo, fnIndex);
   }

   @Override
   public boolean useAliases(PinInformation pinInfo) {
      return false;
   }
   @Override
   public String getExtraDefinitions() {
      String name = fOwner.fBaseName;
      StringBuffer buff = new StringBuffer();
      for (int index=0; index<=5; index++) {
         buff.append(String.format("using %s_PCS%s = USBDM::PcrTable_T<USBDM::%sInfo, %s>;\n", name, index, name, index+3));
      }
      return buff.toString();
   }

   @Override
   String getGroupName() {
      return "SpiIO_Group";
   }

   @Override
   String getGroupTitle() {
      return "SPI, Serial Peripheral Interface";
   }

   @Override
   String getGroupBriefDescription() {
      return "Pins used for SPI functions";
   }
}