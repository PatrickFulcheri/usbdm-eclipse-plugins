package net.sourceforge.usbdm.deviceEditor.validators;

import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.usbdm.deviceEditor.information.LongVariable;
import net.sourceforge.usbdm.deviceEditor.information.Variable;
import net.sourceforge.usbdm.deviceEditor.model.EngineeringNotation;
import net.sourceforge.usbdm.deviceEditor.model.Message;
import net.sourceforge.usbdm.deviceEditor.model.Message.Severity;
import net.sourceforge.usbdm.deviceEditor.peripherals.PeripheralWithState;

public class PllClockValidater extends BaseClockValidator {
   private final long PLL_IN_MIN;
   private final long PLL_IN_MAX;

   private final long PLL_OUT_MIN;
   private final long PLL_OUT_MAX;

   private final int  PRDIV_MIN;
   private final int  PRDIV_MAX;

   private final int  VDIV_MIN;
   private final int  VDIV_MAX;

   private final int  PLL_POST_DIV;

   /**
    * Validates PLL related variables
    * VDIV & PRDIV
    * 
    * @param peripheral
    * @param pllOutMin
    * @param pllOutMax
    * @param pllInMin
    * @param pllInMax
    * @param prDivMin
    * @param prDivMax
    * @param vDivMin
    * @param vDivMax
    * @param pllPostDiv
    */
   public PllClockValidater(PeripheralWithState peripheral, long pllOutMin, long pllOutMax, long pllInMin, long pllInMax, long prDivMin, long prDivMax, long vDivMin, long vDivMax, long pllPostDiv) {
      super(peripheral);
      PLL_OUT_MIN = pllOutMin;
      PLL_OUT_MAX = pllOutMax;
      PLL_IN_MIN  = pllInMin;
      PLL_IN_MAX  = pllInMax;
      PRDIV_MIN                 = (int) prDivMin;
      PRDIV_MAX                 = (int) prDivMax;
      VDIV_MIN                  = (int) vDivMin;
      VDIV_MAX                  = (int) vDivMax;
      PLL_POST_DIV              = (int) pllPostDiv;

      LongVariable pllTargetFrequencyNode = getLongVariable("pllTargetFrequency");
      pllTargetFrequencyNode.setMin(pllOutMin);
      pllTargetFrequencyNode.setMax(pllOutMax);

      LongVariable mcg_c5_prdiv0Node      = getLongVariable("mcg_c5_prdiv0");
      mcg_c5_prdiv0Node.setMin(prDivMin);
      mcg_c5_prdiv0Node.setMax(prDivMax);

      LongVariable mcg_c6_vdiv0Node       = getLongVariable("mcg_c6_vdiv0");
      mcg_c6_vdiv0Node.setMin(vDivMin);
      mcg_c6_vdiv0Node.setMax(vDivMax);
   }

   @Override
   protected void validate() {
      Variable mcg_erc_clockNode      = getVariable("mcg_erc_clock");
      Variable pllInputFrequencyNode  = getVariable("pllInputFrequency");
      Variable pllTargetFrequencyNode = getVariable("pllTargetFrequency");
      Variable mcg_c5_prdiv0Node      = getVariable("mcg_c5_prdiv0");
      Variable mcg_c6_vdiv0Node       = getVariable("mcg_c6_vdiv0");


      // Main clock used by FLL
      long mcg_erc_clock = mcg_erc_clockNode.getValueAsLong();

      long pllTargetFrequency = pllTargetFrequencyNode.getValueAsLong();

      //      System.err.println(String.format("\nPllClockValidater.validate(): mcg_erc_clock = %d, pllTargetFrequency = %d", mcg_erc_clock, pllTargetFrequency));

      int  mcg_prdiv = PRDIV_MIN;
      int  mcg_vdiv  = VDIV_MIN;

      boolean pllInputValid  = false;
      boolean plllOutputValid = false;

      Set<Long> pllFrequencies = new TreeSet<Long>(); 

      StringBuilder sb = new StringBuilder();
      long nearest_PllOutFrequency = Long.MAX_VALUE;

      // Try each prescale value
      for (int mcg_prdiv_probe = PRDIV_MIN; mcg_prdiv_probe <= PRDIV_MAX; mcg_prdiv_probe++) {
         if (sb.length()>0) {
            //            System.err.println(sb.toString());
            sb = new StringBuilder();
         }
         double pllInFrequency = mcg_erc_clock/mcg_prdiv_probe;
         sb.append(String.format("(prdiv = %d, pllIn=%f) => ", mcg_prdiv_probe, pllInFrequency));
         if (pllInFrequency>PLL_IN_MAX) {
            // Invalid as input to PLL
            sb.append("too high");
            continue;
         }
         if (pllInFrequency<PLL_IN_MIN) {
            // Invalid as input to PLL
            sb.append("too low");
            break;
         }
         pllInputValid = true;
         // Try each multiplier value
         for (int mcg_vdiv_probe=VDIV_MIN; mcg_vdiv_probe<=VDIV_MAX; mcg_vdiv_probe++) {
            long pllOutFrequency = Math.round((mcg_vdiv_probe*pllInFrequency)/PLL_POST_DIV);
            sb.append(pllOutFrequency);
            if (pllOutFrequency<PLL_OUT_MIN) {
               sb.append("<, ");
               continue;
            }
            if (pllOutFrequency>PLL_OUT_MAX) {
               sb.append(">, ");
               break;
            }
            sb.append("*,");
            pllFrequencies.add(pllOutFrequency);

            // Best so far
            if (Math.abs(pllOutFrequency-pllTargetFrequency)<Math.abs(nearest_PllOutFrequency-pllTargetFrequency))  {
               nearest_PllOutFrequency = pllOutFrequency;
               mcg_prdiv = mcg_prdiv_probe;
               mcg_vdiv  = mcg_vdiv_probe;
            }
            // Accept value within ~2.5% of desired
            if (Math.abs(pllOutFrequency - pllTargetFrequency) < (pllTargetFrequency/40)) {
               sb.append("=");
               plllOutputValid = true;
            }         
         }
         if (sb.length()>0) {
            sb = new StringBuilder();
         }
      }
      // Update with 'best value' - irrespective of whether they are acceptable
      mcg_c5_prdiv0Node.setValue(mcg_prdiv);
      mcg_c6_vdiv0Node.setValue(mcg_vdiv);

      pllInputFrequencyNode.setOrigin("("+mcg_erc_clockNode.getOrigin()+" via MCG_ERC)/PRDIV");
      pllTargetFrequencyNode.setOrigin(mcg_erc_clockNode.getOrigin()+" via PLL");

      if (!pllInputValid) {
         String msg = String.format("PLL not usable with input clock frequency %s Hz\nRange: [%s,%s]", 
               EngineeringNotation.convert(mcg_erc_clock,3),
               EngineeringNotation.convert(PLL_IN_MIN,3),EngineeringNotation.convert(PLL_IN_MAX,3));
         Message status = new Message(msg, Severity.WARNING);
         pllInputFrequencyNode.setStatus(status);
         pllTargetFrequencyNode.setStatus(status);
      }
      else {
         // PLL in is valid
         pllInputFrequencyNode.setStatus((Message)null);

         // Check PLL out
         StringBuilder status = new StringBuilder();
         Message.Severity severity = Severity.OK;
         if (!plllOutputValid) {
            // PLL Output invalid
            status.append("Not possible to generate desired PLL frequency from input clock\n");
            severity = Severity.WARNING;
            // Update PLL in case it was approximated
         }
         else {
            // PLL Output valid
            if (pllTargetFrequency != nearest_PllOutFrequency) {
               // Update PLL as it was approximated
               pllTargetFrequency = nearest_PllOutFrequency;
               pllTargetFrequencyNode.setValue(pllTargetFrequency);
            }
         }
         status.append("Possible values (Hz) = \n");
         boolean needComma = false;
         int lineCount = -1;
         for (Long freq : pllFrequencies) {
            if (needComma) {
               status.append(", ");
            }
            if (lineCount++>=10) {
               status.append("\n");
               lineCount = 0;
            }
            needComma = true;
            status.append(EngineeringNotation.convert(freq, 5)+"Hz");
         }
         pllTargetFrequencyNode.setStatus(new Message(status.toString(), severity));
      }
   }
}