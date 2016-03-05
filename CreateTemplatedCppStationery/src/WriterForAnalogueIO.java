import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class encapsulating the code for writing an instance of AnalogueIO
 */
/**
 * @author podonoghue
 *
 */
class WriterForAnalogueIO extends InstanceWriter {      

   static final String ALIAS_BASE_NAME       = "adc_";
   static final String CLASS_BASE_NAME       = "Adc";
   static final String INSTANCE_BASE_NAME    = "adc";
   
   public WriterForAnalogueIO(boolean deviceIsMKE) {
      super(deviceIsMKE);
   }
   
   /* (non-Javadoc)
    * @see WriterForDigitalIO#getAliasName(java.lang.String)
    */
   @Override
   public String getAliasName(String signalName, String alias) {
      return ALIAS_BASE_NAME+alias;
   }

   /* (non-Javadoc)
    * @see WriterForDigitalIO#getInstanceName(MappingInfo, int)
    */
   @Override
   public String getInstanceName(MappingInfo mappingInfo, int fnIndex) {
      String instance = mappingInfo.functions.get(fnIndex).fPeripheral.fInstance;
      String signal   = mappingInfo.functions.get(fnIndex).fSignal;
      return INSTANCE_BASE_NAME+instance+"_se"+signal;
   }

   /** 
    * Get declaration as string e.g. 
    * <pre>
    * const USBDM::Adc<b><i>0</i></b>&lt;<b><i>19</i></b>>
    * </pre>
    * @param mappingInfo    Mapping information (pin and peripheral function)
    * @param fnIndex        Index into list of functions mapped to pin
    */
   @Override
   protected String getDeclaration(MappingInfo mappingInfo, int fnIndex) {
      int signal       = getFunctionIndex(mappingInfo.functions.get(fnIndex));
      StringBuffer sb = new StringBuffer();
      sb.append(String.format("const %s::%s<%d>", CreatePinDescription.NAME_SPACE, fOwner.fBaseName, signal));
      return sb.toString();
   }
   
   /* (non-Javadoc)
    * @see WriterForDigitalIO#needPcrTable()
    */
   @Override
   public boolean needPeripheralInformationClass() {
      return true;
   };

   @Override
   public boolean useGuard() {
      return true;
   }

   static final String TEMPLATE_DOCUMENTATION = 
         "/**\n"+
         " * Convenience templated class representing an ADC\n"+
         " *\n"+
         " * Example\n"+
         " * @code\n"+
         " *  // Instantiate ADC0 single-ended channel #8\n"+
         " *  const adc0<8> adc0_se8;\n"+
         " *\n"+
         " *  // Initialise ADC\n"+
         " *  adc0_se8.initialiseADC(USBDM::resolution_12bit_se);\n"+
         " *\n"+
         " *  // Set as analogue input\n"+
         " *  adc0_se8.setAnalogueInput();\n"+
         " *\n"+
         " *  // Read input\n"+
         " *  uint16_t value = adc0_se8.readAnalogue();\n"+
         " *  @endcode\n"+
         " *\n"+
         " * @tparam adcChannel    ADC channel\n"+
         " */\n";

   /* (non-Javadoc)
    * @see WriterForDigitalIO#getTemplate(FunctionTemplateInformation)
    */
   @Override
   public String getTemplate() {   
      return TEMPLATE_DOCUMENTATION+String.format(
            "template<uint8_t channel> using %s = Adc_T<%sInfo, channel>;\n\n",
            fOwner.fBaseName, fOwner.fBaseName);
   }

   @Override
   public int getFunctionIndex(PeripheralFunction function) {
      Pattern p = Pattern.compile("(SE)?(\\d+)(a|b)?");
      Matcher m = p.matcher(function.fSignal);
      if (!m.matches()) {
         throw new RuntimeException("Function "+function+", Signal " + function.fSignal + " does not match expected pattern");
      }
      int signalIndex = Integer.parseInt(m.group(2));
      return signalIndex;
   }

   @Override
   String getGroupName() {
      return "AnalogueIO_Group";
   }

   @Override
   String getGroupTitle() {
      return "Analogue Input";
   }

   @Override
   String getGroupBriefDescription() {
      return "Allows use of port pins as analogue inputs";
   }
}