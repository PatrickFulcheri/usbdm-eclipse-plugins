/**
 * @file      hardware.cpp (generated from MKL46Z4.usbdmHardware)
 * @version   1.2.0
 * @brief     Pin declarations for FRDM_KL46Z4
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
 /* PTA2       ==> UART0_TX                       */  { PORT_PCR_MUX(2)|USBDM::DEFAULT_PCR, &PORTA->PCR[2],  },
 /* PTA3       ==> TPM0_CH0                       */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTA->PCR[3],  },
 /* PTA4       ==> TPM0_CH1                       */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTA->PCR[4],  },
 /* PTA15      ==> UART0_RX                       */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTA->PCR[15], },
 /* PTA17      ==> SPI0_MISO                      */  { PORT_PCR_MUX(2)|USBDM::DEFAULT_PCR, &PORTA->PCR[17], },
 /* PTA18      ==> UART1_RX                       */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTA->PCR[18], },
 /* PTB0       ==> LCD_P0/ADC0_SE8/TSI0_CH0       */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTB->PCR[0],  },
 /* PTB1       ==> LCD_P1/ADC0_SE9/TSI0_CH6       */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTB->PCR[1],  },
 /* PTB2       ==> LCD_P2/ADC0_SE12/TSI0_CH7      */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTB->PCR[2],  },
 /* PTB3       ==> LCD_P3/ADC0_SE13/TSI0_CH8      */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTB->PCR[3],  },
 /* PTC8       ==> TPM0_CH4                       */  { PORT_PCR_MUX(3)|USBDM::DEFAULT_PCR, &PORTC->PCR[8],  },
 /* PTD2       ==> TPM0_CH2                       */  { PORT_PCR_MUX(4)|USBDM::DEFAULT_PCR, &PORTD->PCR[2],  },
 /* PTD3       ==> TPM0_CH3                       */  { PORT_PCR_MUX(4)|USBDM::DEFAULT_PCR, &PORTD->PCR[3],  },
 /* PTD4       ==> LCD_P44                        */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTD->PCR[4],  },
 /* PTE19      ==> LCD_P58/ADC0_DM2/ADC0_SE6a     */  { PORT_PCR_MUX(0)|USBDM::DEFAULT_PCR, &PORTE->PCR[19], },
};

/**
 * Used to configure pin-mapping before 1st use of peripherals
 */
void mapAllPins() {

   SIM->FIXED_PORT_CLOCK_REG |= PORTA_CLOCK_MASK|PORTB_CLOCK_MASK|PORTC_CLOCK_MASK|PORTD_CLOCK_MASK|PORTE_CLOCK_MASK;

   for (const PinInit *p=pinInit; p<(pinInit+(sizeof(pinInit)/sizeof(pinInit[0]))); p++) {
      *(p->pcr) = p->pcrValue;
   }
}

} // End namespace USBDM
