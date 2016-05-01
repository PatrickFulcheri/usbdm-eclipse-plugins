/**
 * @file      hardware.cpp (generated from MK66F18.usbdmHardware)
 * @version   1.2.0
 * @brief     Pin declarations for FRDM_K66F
 *
 * *****************************
 * *** DO NOT EDIT THIS FILE ***
 * *****************************
 *
 * This file is generated automatically.
 * Any manual changes will be lost.
 */

#include "hardware.h"

namespace USBDM {

struct PinInit {
   uint32_t pcrValue;
   uint32_t volatile *pcr;
};

static constexpr PinInit pinInit[] = {
 /* PTA1       ==> TSI0_CH2                  */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTA->PCR[1],  },
 /* PTA3       ==> JTAG_TMS/SWD_DIO          */  { PORT_PCR_MUX(7)|USBDM::DEFAULT_PCR, &PORTA->PCR[3],  },
 /* PTA11      ==> Disabled                  */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTA->PCR[11], },
 /* PTB1       ==> FTM1_CH1                  */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTB->PCR[1],  },
 /* PTB4       ==> ADC1_SE10                 */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTB->PCR[4],  },
 /* PTB5       ==> ENET0_1588_TMR3           */  { PORT_PCR_MUX(4)|USBDM::DEFAULT_PCR, &PORTB->PCR[5],  },
 /* PTB8       ==> GPIOB_8                   */  { PORT_PCR_MUX(1)|USBDM::DEFAULT_PCR, &PORTB->PCR[8],  },
 /* PTB19      ==> FTM2_CH1                  */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTB->PCR[19], },
 /* PTC0       ==> PDB0_EXTRG                */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTC->PCR[0],  },
 /* PTC1       ==> FTM0_CH0                  */  { PORT_PCR_MUX(4)|USBDM::DEFAULT_PCR, &PORTC->PCR[1],  },
 /* PTC6       ==> CMP0_IN0                  */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTC->PCR[6],  },
 /* PTC11      ==> ADC1_SE7b                 */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTC->PCR[11], },
 /* PTD4       ==> FTM0_CH4                  */  { PORT_PCR_MUX(4)|USBDM::DEFAULT_PCR, &PORTD->PCR[4],  },
 /* PTD6       ==> ADC0_SE7b                 */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTD->PCR[6],  },
 /* PTE1       ==> ADC1_SE5a                 */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTE->PCR[1],  },
 /* PTE2       ==> ADC1_SE6a                 */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTE->PCR[2],  },
};

/**
 * Used to configure pin-mapping before 1st use of peripherals
 */
void usbdm_PinMapping() {

   SIM->FIXED_PORT_CLOCK_REG |= PORTA_CLOCK_MASK|PORTB_CLOCK_MASK|PORTC_CLOCK_MASK|PORTD_CLOCK_MASK|PORTE_CLOCK_MASK;

   for (const PinInit *p=pinInit; p<(pinInit+(sizeof(pinInit)/sizeof(pinInit[0]))); p++) {
      *(p->pcr) = p->pcrValue;
   }
}

} // End namespace USBDM
