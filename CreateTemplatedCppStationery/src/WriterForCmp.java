import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class encapsulating the code for writing an instance of DigitalIO
 */
/**
 * @author podonoghue
 *
 */
class WriterForCmp extends InstanceWriter {

   static final String ALIAS_BASE_NAME       = "cmp_";
   static final String CLASS_BASE_NAME       = "Cmp";
   static final String INSTANCE_BASE_NAME    = "cmp";

   public WriterForCmp(boolean deviceIsMKE) {
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
      int signal       = getFunctionIndex(mappingInfo.functions.get(fnIndex));
      StringBuffer sb = new StringBuffer();
      sb.append(String.format("const %s::%s<%d>", CreatePinDescription.NAME_SPACE, fOwner.fBaseName, signal));
      return sb.toString();
   }
   /* (non-Javadoc)
    * @see InstanceWriter#needPcrTable()
    */
   @Override
   public boolean needPeripheralInformationClass() {
      return true;
   }

   @Override
   public int getFunctionIndex(PeripheralFunction function) {
    Pattern p = Pattern.compile("IN(\\d+)");
    Matcher m = p.matcher(function.fSignal);
    if (m.matches()) {
       return Integer.parseInt(m.group(1));
    }
    final String signalNames[] = {"OUT"};
    for (int signal=0; signal<signalNames.length; signal++) {
       if (function.fSignal.matches(signalNames[signal])) {
          return 8+signal;
       }
    }
    throw new RuntimeException("function '" + function.fSignal + "' does not match expected pattern");
 }
   
   static final String TEMPLATE_DOCUMENTATION = 
         "/**\n"+
         " * Convenience templated class representing an CMP pin\n"+
         " *\n"+
         " * Example\n"+
         " * @code\n"+
         " * using cmp0_PCS0 = const USBDM::Cmp0Pin<3>;\n"+
         " * @endcode\n"+
         " *\n"+
         " * @tparam cmpPinNum    CMP pin number (index into CmpInfo[])\n"+
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
   String getGroupName() {
      return "CMP_Group";
   }

   @Override
   String getGroupTitle() {
      return "CMP, Analogue Comparator";
   }

   @Override
   String getGroupBriefDescription() {
      return "Pins used for Analogue Comparator";
   }
}