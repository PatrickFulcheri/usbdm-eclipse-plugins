package net.sourceforge.usbdm.deviceEditor.validators;

import java.util.ArrayList;

import net.sourceforge.usbdm.deviceEditor.information.Variable;
import net.sourceforge.usbdm.deviceEditor.model.Message;
import net.sourceforge.usbdm.deviceEditor.model.Message.Severity;
import net.sourceforge.usbdm.deviceEditor.peripherals.PeripheralWithState;

/**
 * Class to determine MCG settings
 
 * Used for:
 *     mcg_mk_ics48mml
 *     mcg_mk
 */
public class ClockValidator_MCG_Lite extends BaseClockValidator {

   private boolean addedExternalVariables = false;
   private final static String[] externalVariables = {
         "/OSC0/oscclk_clock",
         "/OSC0/"+OscValidate.OSC_RANGE_KEY,
   };

   public ClockValidator_MCG_Lite(PeripheralWithState peripheral, ArrayList<Object> values) {
      super(peripheral);
   }

   /**
    * Inputs
    *    /OSC0/OscValidate.OSC_RANGE_KEY
    *    /OSC0/oscclk_clock
    *    /OSC0/range
    * 
    * Outputs 
    *    system_slow_irc_clock
    *    system_fast_irc_clock
    *    system_mcgirclk_clock
    *    system_mcgoutclk_clock
    *    system_mcgpclk_clock
    */
   @Override
   protected void validate(Variable variable) {
//      System.err.println(getSimpleClassName()+" Var = "+variable);

      if (!addedExternalVariables) {
         addToWatchedVariables(externalVariables);
         addedExternalVariables = true;
      }
      // C1
      //=================================
      Variable     mcg_c1_clksVar                  =  getVariable("mcg_c1_clks");
      Variable     mcg_c1_irclkenVar               =  getVariable("mcg_c1_irclken");
      Variable     mcg_c1_irefstenVar              =  getVariable("mcg_c1_irefsten");

      // C2
      //=================================
      Variable     mcg_c2_range0Var                =  getVariable("/OSC0/range"); // Maps to OSC0 range control
      Variable     mcg_c2_ircsVar                  =  getVariable("mcg_c2_ircs");

      // SC
      //=================================
      Variable     mcg_sc_fcrdivVar                =  safeGetVariable("mcg_sc_fcrdiv");

      // MC
      //=================================
      Variable     mcg_mc_hircenVar                =  safeGetVariable("mcg_mc_hircen");
      Variable     mcg_mc_lirc_div2Var             =  safeGetVariable("mcg_mc_lirc_div2");

      // LIRC
      //=================================
      Variable     system_slow_irc_clockVar        =  getVariable("system_slow_irc_clock");
      Variable     system_fast_irc_clockVar        =  getVariable("system_fast_irc_clock");
      Variable     system_lirc_clockVar            =  getVariable("system_lirc_clock");
      Variable     system_lirc_div1_clockVar       =  getVariable("system_lirc_div1_clock");
      Variable     system_mcgirclk_clockVar        =  getVariable("system_mcgirclk_clock");

      // Internal
      //=================================
      Variable     system_irc48m_clockVar          =  safeGetVariable("system_irc48m_clock");
//    Variable     usb_clkin_clockVar              =  getLongVariable("system_usb_clkin_clock");

      // Clocks and information from main oscillator
      //=================================
      Variable     oscclk_clockVar                 =  getVariable("/OSC0/oscclk_clock");
      Variable     oscRangeInVar                   =  getVariable("/OSC0/"+OscValidate.OSC_RANGE_KEY);

      //=================================
      Variable     clock_modeVar                   =  getVariable("clock_mode");

      //=================================
      Variable     system_mcgoutclk_clock_sourceVar =  getVariable("system_mcgoutclk_clock_source");
      Variable     system_mcgoutclk_clockVar        =  getVariable("system_mcgoutclk_clock");
      
      Variable     system_mcgpclk_clockVar         =  getVariable("system_mcgpclk_clock");

      
      long rangIn = oscRangeInVar.getValueAsLong();
      if (rangIn != OscValidate.UNCONSTRAINED_RANGE) {
         mcg_c2_range0Var.enable(true);
         mcg_c2_range0Var.setValue(oscRangeInVar.getValueAsLong());
      }
      else {
         mcg_c2_range0Var.enable(false);
      }
      
      // Main clock mode (MCGOUTCLK)
      //=============================
      ClockMode clock_mode = ClockMode.valueOf(clock_modeVar.getSubstitutionValue());
      
      switch (clock_mode) {
      default:
      case ClockMode_None:
         mcg_c1_clksVar.setValue(0);
         mcg_c2_ircsVar.setLocked(false);

         system_mcgoutclk_clockVar.setValue(system_slow_irc_clockVar.getValueAsLong());
         system_mcgoutclk_clockVar.setOrigin(system_slow_irc_clockVar.getOrigin());
         system_mcgoutclk_clockVar.setStatus(new Message("No clock settings are applied", Severity.WARNING));
         system_mcgoutclk_clock_sourceVar.setValue("LIRC2");
         break;
         
      case ClockMode_HIRC_48M:
         mcg_c1_clksVar.setValue(0);
         mcg_c2_ircsVar.setLocked(false);

         system_mcgoutclk_clockVar.setValue(system_irc48m_clockVar.getValueAsLong());
         system_mcgoutclk_clockVar.setOrigin(system_irc48m_clockVar.getOrigin());
         system_mcgoutclk_clockVar.setStatus((Message)null);
         system_mcgoutclk_clock_sourceVar.setValue("HIRC 48M (IRCLK48MCLK)");
         break;
      
      case ClockMode_LIRC_2M:
         mcg_c1_clksVar.setValue(1);
         mcg_c2_ircsVar.setValue(0);
         mcg_c2_ircsVar.setLocked(true);

         system_mcgoutclk_clockVar.setValue(system_slow_irc_clockVar.getValueAsLong());
         system_mcgoutclk_clockVar.setOrigin(system_slow_irc_clockVar.getOrigin());
         system_mcgoutclk_clockVar.setStatus((Message)null);
         system_mcgoutclk_clock_sourceVar.setValue("LIRC2");
         break;

      case ClockMode_LIRC_8M:
         mcg_c1_clksVar.setValue(1);
         mcg_c2_ircsVar.setValue(1);
         mcg_c2_ircsVar.setLocked(true);

         system_mcgoutclk_clockVar.setValue(system_fast_irc_clockVar.getValueAsLong());
         system_mcgoutclk_clockVar.setOrigin(system_fast_irc_clockVar.getOrigin());
         system_mcgoutclk_clockVar.setStatus((Message)null);
         system_mcgoutclk_clock_sourceVar.setValue("LIRC8");
         break;

      case ClockMode_EXT:
         mcg_c1_clksVar.setValue(2);
         mcg_c2_ircsVar.setLocked(false);

         system_mcgoutclk_clockVar.setValue(oscclk_clockVar.getValueAsLong());
         system_mcgoutclk_clockVar.setOrigin(oscclk_clockVar.getOrigin());
         system_mcgoutclk_clockVar.setStatus((Message)null);
         system_mcgoutclk_clock_sourceVar.setValue("External Clock (OSCCLK)");
         break;
      }     
      system_mcgoutclk_clock_sourceVar.setStatus(system_mcgoutclk_clockVar.getStatus());

      // HIRC related clocks
      //============================================
      if (mcg_mc_hircenVar.getValueAsBoolean() || (clock_mode == ClockMode.ClockMode_HIRC_48M)) {
         // HIRC Enabled 
         system_mcgpclk_clockVar.setValue(system_irc48m_clockVar.getValueAsLong());
         system_mcgpclk_clockVar.enable(true);
         system_mcgpclk_clockVar.setStatus((Message)null);
      }
      else {
         // HIRC Disabled 
         system_mcgpclk_clockVar.enable(false);
         system_mcgpclk_clockVar.setStatus(new Message("Disabled by mcg_mc_hircen", Severity.INFO));
      }
      
      // LIRC related clocks
      //========================================
      if (mcg_c1_irclkenVar.getValueAsBoolean() || 
          (clock_mode == ClockMode.ClockMode_LIRC_2M) || (clock_mode == ClockMode.ClockMode_LIRC_8M) ) {
         // LIRC Enabled
         mcg_c1_irefstenVar.enable(true);
         system_lirc_clockVar.enable(true);
         system_lirc_clockVar.setStatus((Message)null);
         if (mcg_c2_ircsVar.getValueAsBoolean()) {
            // Fast IRC selected
            system_lirc_clockVar.setValue(system_fast_irc_clockVar.getValueAsLong());
            system_lirc_clockVar.setOrigin(system_fast_irc_clockVar.getOrigin());
         }
         else {
            // Slow IRC selected
            system_lirc_clockVar.setValue(system_slow_irc_clockVar.getValueAsLong());
            system_lirc_clockVar.setOrigin(system_fast_irc_clockVar.getOrigin());
         }
         mcg_sc_fcrdivVar.enable(true);
         system_lirc_div1_clockVar.enable(true);
         mcg_mc_lirc_div2Var.enable(true);
         system_mcgirclk_clockVar.enable(true);
         system_mcgirclk_clockVar.setStatus((Message)null);
      }
      else {
         // LIRC Disabled
         mcg_c1_irefstenVar.enable(false);
         system_lirc_clockVar.enable(false);
         system_lirc_clockVar.setStatus(new Message("Disabled by mcg_c1_irclken", Severity.INFO));
         mcg_sc_fcrdivVar.enable(false);
         system_lirc_div1_clockVar.enable(false);
         mcg_mc_lirc_div2Var.enable(false);
         system_mcgirclk_clockVar.enable(false);
         system_mcgirclk_clockVar.setStatus(new Message("Disabled by mcg_c1_irclken", Severity.INFO));
      }
      
      long mcg_sc_fcrdiv = mcg_sc_fcrdivVar.getValueAsLong();
      system_lirc_div1_clockVar.setValue(system_lirc_clockVar.getValueAsLong()/(1<<mcg_sc_fcrdiv));
      system_lirc_div1_clockVar.setOrigin(system_lirc_clockVar.getOrigin()+"/LIRC_DIV1");
      
      long mcg_mc_lirc_div2 = mcg_mc_lirc_div2Var.getValueAsLong();
      system_mcgirclk_clockVar.setValue(system_lirc_div1_clockVar.getValueAsLong()/(1<<mcg_mc_lirc_div2));
      system_mcgirclk_clockVar.setOrigin(system_lirc_div1_clockVar.getOrigin()+"/LIRC_DIV2");
   }
}
