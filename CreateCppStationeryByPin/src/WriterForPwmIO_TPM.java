import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Class encapsulating the code for writing an instance of PwmIO (TPM)
 */
class WriterForPwmIO_TPM extends WriterForDigitalIO {     

   static final String INSTANCE_TEMPLATE = "extern const PwmIO %-24s //!< PwmIO on pin %s\n";
   static final String CLASS_NAME = "pwmIO_";
   
   public WriterForPwmIO_TPM(boolean deviceIsMKE) {
      super(deviceIsMKE, true);
   }

   @Override
   public String getInstanceName(MappingInfo mappingInfo, int fnIndex) {
//      return CLASS_NAME+mappingInfo.pin.getName(); // e.g. pwmIO_PTA0;
      return CLASS_NAME+mappingInfo.functions.get(fnIndex).getName(); // e.g. pwmIO_FTM0_CH1
   }

   @Override
   public String getAliasName(String alias) {
      return CLASS_NAME+alias;
   }

   @Override
   public void writeDeclaration(MappingInfo mappingInfo, int fnIndex, BufferedWriter cppFile) throws IOException {
      cppFile.write(String.format(INSTANCE_TEMPLATE, getInstanceName(mappingInfo, fnIndex)+";", mappingInfo.pin.getName()));
   }

   /** 
    * Write PwmIO instance for a TPM e.g. 
    * <pre>
    * const PwmIO  pwmIO_<b><i>PTA17</i></b> = {&digitalIO_<b><i>PTA17</i></b>, (volatile TPM_Type*)TPM(<b><i>PTA17</i></b>_TPM_NUM), <b><i>PTA17</i></b>_TPM_CH, PORT_PCR_MUX(<b><i>PTA17</i></b>_TPM_FN), &TPM_CLOCK_REG(<b><i>PTA17</i></b>_TPM_NUM), TPM_CLOCK_MASK(<b><i>PTA17</i></b>_TPM_NUM), <b><i>TPM0</b></i>_SC};
    * </pre>
    * @param mappingInfo    Mapping information (pin and peripheral function)
    * @param cppFile        Where to write
    * 
    * @throws IOException
    */
   @Override
   public void writeDefinition(MappingInfo mappingInfo, int fnIndex, BufferedWriter cppFile) throws IOException {

      String instance         = mappingInfo.functions.get(fnIndex).fPeripheral.fInstance;
      String signal           = mappingInfo.functions.get(fnIndex).fSignal;
      String muxValue         = Integer.toString(mappingInfo.mux.value);

      String instanceName     = getInstanceName(mappingInfo, fnIndex);                  // pwmIO_PTA0
      String ftmInstance      = String.format("(volatile TPM_Type*)TPM%s,", instance);  // (volatile TPM_Type*)TPMx,
      String ftmChannel       = String.format("%s,", signal);                           // n
      String ftmMuxValue      = String.format("PORT_PCR_MUX(%s),", muxValue);           // PORT_PCR_MUX(m),
      String ftmClockReg      = String.format("&TPM%s_CLOCK_REG,", instance);           // &FTMx_CLOCK_REG,
      String ftmClockMask     = String.format("TPM%s_CLOCK_MASK,", instance);           // FTMx_CLOCK_MASK,
      String ftmSCValue       = String.format("TPM%s_SC", instance);                    // FTMx_SC
      String pcrInit          = FunctionTemplateInformation.getPCRInitString(mappingInfo.pin);

      //         cppFile.write(String.format("#if %s == %s\n", pinName+"_SEL", Integer.toString(mappingInfo.mux)));
      cppFile.write(String.format("const PwmIO  %-15s = {", instanceName));
      cppFile.write(String.format("%s,%-28s%-6s%-6s%-20s%s %s};\n", 
            pcrInit, ftmInstance, ftmChannel, ftmMuxValue, ftmClockReg, ftmClockMask,ftmSCValue) );
      //         cppFile.write(String.format("#endif\n"));
   }
}