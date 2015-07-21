 /**
  * @file     gpio.cpp
  *
  * @brief   Pin declarations for MK64F12
  */

#include "utilities.h"
#include "gpio.h"
#include "pin_mapping.h"

/**
 * @brief Create Timer Clock register name from timer number
 *
 * @param number Timer number e.g. 1 => FTM1_CLOCK_REG
 */
#define FTM_CLOCK_REG(number)  CONCAT3_(FTM,number,_CLOCK_REG)

/**
 * @brief Create Timer Clock register mask from timer number
 *
 * @param number Timer number e.g. 1 => FTM1_CLOCK_MASK
 */
#define FTM_CLOCK_MASK(number)  CONCAT3_(FTM,number,_CLOCK_MASK)

/**
 * @brief Create Timer Clock register name from timer number
 *
 * @param number Timer number e.g. 1 => TPM1_CLOCK_REG
 */
#define TPM_CLOCK_REG(number)  CONCAT3_(TPM,number,_CLOCK_REG)

/**
 * @brief Create Timer Clock register mask from timer number
 *
 * @param number Timer number e.g. 1 => TPM1_CLOCK_MASK
 */
#define TPM_CLOCK_MASK(number)  CONCAT3_(TPM,number,_CLOCK_MASK)

/**
 * @brief Create Timer Clock register name from timer number
 *
 * @param number Timer number e.g. 1 => ADC1_CLOCK_REG
 */
#define ADC_CLOCK_REG(number)  CONCAT3_(ADC,number,_CLOCK_REG)

/**
 * @brief Create Timer Clock register mask from timer number
 *
 * @param number Timer number e.g. 1 => ADC1_CLOCK_MASK
 */
#define ADC_CLOCK_MASK(number)  CONCAT3_(ADC,number,_CLOCK_MASK)

#define ADC(num)             CONCAT2_(ADC,num)   
#define FTM(num)             CONCAT2_(FTM,num)   
#define TPM(num)             CONCAT2_(TPM,num)   

const DigitalIO digitalIO_PTA0     = {&PCR(PTA0_PORT,   PTA0_NUM),  GPIO(PTA0_PORT),  PORT_CLOCK_MASK(PTA0_PORT),  (1UL<<PTA0_NUM)};
const DigitalIO digitalIO_PTA1     = {&PCR(PTA1_PORT,   PTA1_NUM),  GPIO(PTA1_PORT),  PORT_CLOCK_MASK(PTA1_PORT),  (1UL<<PTA1_NUM)};
const DigitalIO digitalIO_PTA2     = {&PCR(PTA2_PORT,   PTA2_NUM),  GPIO(PTA2_PORT),  PORT_CLOCK_MASK(PTA2_PORT),  (1UL<<PTA2_NUM)};
const DigitalIO digitalIO_PTA3     = {&PCR(PTA3_PORT,   PTA3_NUM),  GPIO(PTA3_PORT),  PORT_CLOCK_MASK(PTA3_PORT),  (1UL<<PTA3_NUM)};
const DigitalIO digitalIO_PTA4     = {&PCR(PTA4_PORT,   PTA4_NUM),  GPIO(PTA4_PORT),  PORT_CLOCK_MASK(PTA4_PORT),  (1UL<<PTA4_NUM)};
const DigitalIO digitalIO_PTA5     = {&PCR(PTA5_PORT,   PTA5_NUM),  GPIO(PTA5_PORT),  PORT_CLOCK_MASK(PTA5_PORT),  (1UL<<PTA5_NUM)};
const DigitalIO digitalIO_PTA6     = {&PCR(PTA6_PORT,   PTA6_NUM),  GPIO(PTA6_PORT),  PORT_CLOCK_MASK(PTA6_PORT),  (1UL<<PTA6_NUM)};
const DigitalIO digitalIO_PTA7     = {&PCR(PTA7_PORT,   PTA7_NUM),  GPIO(PTA7_PORT),  PORT_CLOCK_MASK(PTA7_PORT),  (1UL<<PTA7_NUM)};
const DigitalIO digitalIO_PTA8     = {&PCR(PTA8_PORT,   PTA8_NUM),  GPIO(PTA8_PORT),  PORT_CLOCK_MASK(PTA8_PORT),  (1UL<<PTA8_NUM)};
const DigitalIO digitalIO_PTA9     = {&PCR(PTA9_PORT,   PTA9_NUM),  GPIO(PTA9_PORT),  PORT_CLOCK_MASK(PTA9_PORT),  (1UL<<PTA9_NUM)};
const DigitalIO digitalIO_PTA10    = {&PCR(PTA10_PORT,  PTA10_NUM), GPIO(PTA10_PORT), PORT_CLOCK_MASK(PTA10_PORT), (1UL<<PTA10_NUM)};
const DigitalIO digitalIO_PTA11    = {&PCR(PTA11_PORT,  PTA11_NUM), GPIO(PTA11_PORT), PORT_CLOCK_MASK(PTA11_PORT), (1UL<<PTA11_NUM)};
const DigitalIO digitalIO_PTA12    = {&PCR(PTA12_PORT,  PTA12_NUM), GPIO(PTA12_PORT), PORT_CLOCK_MASK(PTA12_PORT), (1UL<<PTA12_NUM)};
const DigitalIO digitalIO_PTA13    = {&PCR(PTA13_PORT,  PTA13_NUM), GPIO(PTA13_PORT), PORT_CLOCK_MASK(PTA13_PORT), (1UL<<PTA13_NUM)};
const DigitalIO digitalIO_PTA14    = {&PCR(PTA14_PORT,  PTA14_NUM), GPIO(PTA14_PORT), PORT_CLOCK_MASK(PTA14_PORT), (1UL<<PTA14_NUM)};
const DigitalIO digitalIO_PTA15    = {&PCR(PTA15_PORT,  PTA15_NUM), GPIO(PTA15_PORT), PORT_CLOCK_MASK(PTA15_PORT), (1UL<<PTA15_NUM)};
const DigitalIO digitalIO_PTA16    = {&PCR(PTA16_PORT,  PTA16_NUM), GPIO(PTA16_PORT), PORT_CLOCK_MASK(PTA16_PORT), (1UL<<PTA16_NUM)};
const DigitalIO digitalIO_PTA17    = {&PCR(PTA17_PORT,  PTA17_NUM), GPIO(PTA17_PORT), PORT_CLOCK_MASK(PTA17_PORT), (1UL<<PTA17_NUM)};
const DigitalIO digitalIO_PTA18    = {&PCR(PTA18_PORT,  PTA18_NUM), GPIO(PTA18_PORT), PORT_CLOCK_MASK(PTA18_PORT), (1UL<<PTA18_NUM)};
const DigitalIO digitalIO_PTA19    = {&PCR(PTA19_PORT,  PTA19_NUM), GPIO(PTA19_PORT), PORT_CLOCK_MASK(PTA19_PORT), (1UL<<PTA19_NUM)};
const DigitalIO digitalIO_PTA24    = {&PCR(PTA24_PORT,  PTA24_NUM), GPIO(PTA24_PORT), PORT_CLOCK_MASK(PTA24_PORT), (1UL<<PTA24_NUM)};
const DigitalIO digitalIO_PTA25    = {&PCR(PTA25_PORT,  PTA25_NUM), GPIO(PTA25_PORT), PORT_CLOCK_MASK(PTA25_PORT), (1UL<<PTA25_NUM)};
const DigitalIO digitalIO_PTA26    = {&PCR(PTA26_PORT,  PTA26_NUM), GPIO(PTA26_PORT), PORT_CLOCK_MASK(PTA26_PORT), (1UL<<PTA26_NUM)};
const DigitalIO digitalIO_PTA27    = {&PCR(PTA27_PORT,  PTA27_NUM), GPIO(PTA27_PORT), PORT_CLOCK_MASK(PTA27_PORT), (1UL<<PTA27_NUM)};
const DigitalIO digitalIO_PTA28    = {&PCR(PTA28_PORT,  PTA28_NUM), GPIO(PTA28_PORT), PORT_CLOCK_MASK(PTA28_PORT), (1UL<<PTA28_NUM)};
const DigitalIO digitalIO_PTA29    = {&PCR(PTA29_PORT,  PTA29_NUM), GPIO(PTA29_PORT), PORT_CLOCK_MASK(PTA29_PORT), (1UL<<PTA29_NUM)};
const DigitalIO digitalIO_PTB0     = {&PCR(PTB0_PORT,   PTB0_NUM),  GPIO(PTB0_PORT),  PORT_CLOCK_MASK(PTB0_PORT),  (1UL<<PTB0_NUM)};
const DigitalIO digitalIO_PTB1     = {&PCR(PTB1_PORT,   PTB1_NUM),  GPIO(PTB1_PORT),  PORT_CLOCK_MASK(PTB1_PORT),  (1UL<<PTB1_NUM)};
const DigitalIO digitalIO_PTB2     = {&PCR(PTB2_PORT,   PTB2_NUM),  GPIO(PTB2_PORT),  PORT_CLOCK_MASK(PTB2_PORT),  (1UL<<PTB2_NUM)};
const DigitalIO digitalIO_PTB3     = {&PCR(PTB3_PORT,   PTB3_NUM),  GPIO(PTB3_PORT),  PORT_CLOCK_MASK(PTB3_PORT),  (1UL<<PTB3_NUM)};
const DigitalIO digitalIO_PTB4     = {&PCR(PTB4_PORT,   PTB4_NUM),  GPIO(PTB4_PORT),  PORT_CLOCK_MASK(PTB4_PORT),  (1UL<<PTB4_NUM)};
const DigitalIO digitalIO_PTB5     = {&PCR(PTB5_PORT,   PTB5_NUM),  GPIO(PTB5_PORT),  PORT_CLOCK_MASK(PTB5_PORT),  (1UL<<PTB5_NUM)};
const DigitalIO digitalIO_PTB6     = {&PCR(PTB6_PORT,   PTB6_NUM),  GPIO(PTB6_PORT),  PORT_CLOCK_MASK(PTB6_PORT),  (1UL<<PTB6_NUM)};
const DigitalIO digitalIO_PTB7     = {&PCR(PTB7_PORT,   PTB7_NUM),  GPIO(PTB7_PORT),  PORT_CLOCK_MASK(PTB7_PORT),  (1UL<<PTB7_NUM)};
const DigitalIO digitalIO_PTB8     = {&PCR(PTB8_PORT,   PTB8_NUM),  GPIO(PTB8_PORT),  PORT_CLOCK_MASK(PTB8_PORT),  (1UL<<PTB8_NUM)};
const DigitalIO digitalIO_PTB9     = {&PCR(PTB9_PORT,   PTB9_NUM),  GPIO(PTB9_PORT),  PORT_CLOCK_MASK(PTB9_PORT),  (1UL<<PTB9_NUM)};
const DigitalIO digitalIO_PTB10    = {&PCR(PTB10_PORT,  PTB10_NUM), GPIO(PTB10_PORT), PORT_CLOCK_MASK(PTB10_PORT), (1UL<<PTB10_NUM)};
const DigitalIO digitalIO_PTB11    = {&PCR(PTB11_PORT,  PTB11_NUM), GPIO(PTB11_PORT), PORT_CLOCK_MASK(PTB11_PORT), (1UL<<PTB11_NUM)};
const DigitalIO digitalIO_PTB12    = {&PCR(PTB12_PORT,  PTB12_NUM), GPIO(PTB12_PORT), PORT_CLOCK_MASK(PTB12_PORT), (1UL<<PTB12_NUM)};
const DigitalIO digitalIO_PTB13    = {&PCR(PTB13_PORT,  PTB13_NUM), GPIO(PTB13_PORT), PORT_CLOCK_MASK(PTB13_PORT), (1UL<<PTB13_NUM)};
const DigitalIO digitalIO_PTB16    = {&PCR(PTB16_PORT,  PTB16_NUM), GPIO(PTB16_PORT), PORT_CLOCK_MASK(PTB16_PORT), (1UL<<PTB16_NUM)};
const DigitalIO digitalIO_PTB17    = {&PCR(PTB17_PORT,  PTB17_NUM), GPIO(PTB17_PORT), PORT_CLOCK_MASK(PTB17_PORT), (1UL<<PTB17_NUM)};
const DigitalIO digitalIO_PTB18    = {&PCR(PTB18_PORT,  PTB18_NUM), GPIO(PTB18_PORT), PORT_CLOCK_MASK(PTB18_PORT), (1UL<<PTB18_NUM)};
const DigitalIO digitalIO_PTB19    = {&PCR(PTB19_PORT,  PTB19_NUM), GPIO(PTB19_PORT), PORT_CLOCK_MASK(PTB19_PORT), (1UL<<PTB19_NUM)};
const DigitalIO digitalIO_PTB20    = {&PCR(PTB20_PORT,  PTB20_NUM), GPIO(PTB20_PORT), PORT_CLOCK_MASK(PTB20_PORT), (1UL<<PTB20_NUM)};
const DigitalIO digitalIO_PTB21    = {&PCR(PTB21_PORT,  PTB21_NUM), GPIO(PTB21_PORT), PORT_CLOCK_MASK(PTB21_PORT), (1UL<<PTB21_NUM)};
const DigitalIO digitalIO_PTB22    = {&PCR(PTB22_PORT,  PTB22_NUM), GPIO(PTB22_PORT), PORT_CLOCK_MASK(PTB22_PORT), (1UL<<PTB22_NUM)};
const DigitalIO digitalIO_PTB23    = {&PCR(PTB23_PORT,  PTB23_NUM), GPIO(PTB23_PORT), PORT_CLOCK_MASK(PTB23_PORT), (1UL<<PTB23_NUM)};
const DigitalIO digitalIO_PTC0     = {&PCR(PTC0_PORT,   PTC0_NUM),  GPIO(PTC0_PORT),  PORT_CLOCK_MASK(PTC0_PORT),  (1UL<<PTC0_NUM)};
const DigitalIO digitalIO_PTC1     = {&PCR(PTC1_PORT,   PTC1_NUM),  GPIO(PTC1_PORT),  PORT_CLOCK_MASK(PTC1_PORT),  (1UL<<PTC1_NUM)};
const DigitalIO digitalIO_PTC2     = {&PCR(PTC2_PORT,   PTC2_NUM),  GPIO(PTC2_PORT),  PORT_CLOCK_MASK(PTC2_PORT),  (1UL<<PTC2_NUM)};
const DigitalIO digitalIO_PTC3     = {&PCR(PTC3_PORT,   PTC3_NUM),  GPIO(PTC3_PORT),  PORT_CLOCK_MASK(PTC3_PORT),  (1UL<<PTC3_NUM)};
const DigitalIO digitalIO_PTC4     = {&PCR(PTC4_PORT,   PTC4_NUM),  GPIO(PTC4_PORT),  PORT_CLOCK_MASK(PTC4_PORT),  (1UL<<PTC4_NUM)};
const DigitalIO digitalIO_PTC5     = {&PCR(PTC5_PORT,   PTC5_NUM),  GPIO(PTC5_PORT),  PORT_CLOCK_MASK(PTC5_PORT),  (1UL<<PTC5_NUM)};
const DigitalIO digitalIO_PTC6     = {&PCR(PTC6_PORT,   PTC6_NUM),  GPIO(PTC6_PORT),  PORT_CLOCK_MASK(PTC6_PORT),  (1UL<<PTC6_NUM)};
const DigitalIO digitalIO_PTC7     = {&PCR(PTC7_PORT,   PTC7_NUM),  GPIO(PTC7_PORT),  PORT_CLOCK_MASK(PTC7_PORT),  (1UL<<PTC7_NUM)};
const DigitalIO digitalIO_PTC8     = {&PCR(PTC8_PORT,   PTC8_NUM),  GPIO(PTC8_PORT),  PORT_CLOCK_MASK(PTC8_PORT),  (1UL<<PTC8_NUM)};
const DigitalIO digitalIO_PTC9     = {&PCR(PTC9_PORT,   PTC9_NUM),  GPIO(PTC9_PORT),  PORT_CLOCK_MASK(PTC9_PORT),  (1UL<<PTC9_NUM)};
const DigitalIO digitalIO_PTC10    = {&PCR(PTC10_PORT,  PTC10_NUM), GPIO(PTC10_PORT), PORT_CLOCK_MASK(PTC10_PORT), (1UL<<PTC10_NUM)};
const DigitalIO digitalIO_PTC11    = {&PCR(PTC11_PORT,  PTC11_NUM), GPIO(PTC11_PORT), PORT_CLOCK_MASK(PTC11_PORT), (1UL<<PTC11_NUM)};
const DigitalIO digitalIO_PTC12    = {&PCR(PTC12_PORT,  PTC12_NUM), GPIO(PTC12_PORT), PORT_CLOCK_MASK(PTC12_PORT), (1UL<<PTC12_NUM)};
const DigitalIO digitalIO_PTC13    = {&PCR(PTC13_PORT,  PTC13_NUM), GPIO(PTC13_PORT), PORT_CLOCK_MASK(PTC13_PORT), (1UL<<PTC13_NUM)};
const DigitalIO digitalIO_PTC14    = {&PCR(PTC14_PORT,  PTC14_NUM), GPIO(PTC14_PORT), PORT_CLOCK_MASK(PTC14_PORT), (1UL<<PTC14_NUM)};
const DigitalIO digitalIO_PTC15    = {&PCR(PTC15_PORT,  PTC15_NUM), GPIO(PTC15_PORT), PORT_CLOCK_MASK(PTC15_PORT), (1UL<<PTC15_NUM)};
const DigitalIO digitalIO_PTC16    = {&PCR(PTC16_PORT,  PTC16_NUM), GPIO(PTC16_PORT), PORT_CLOCK_MASK(PTC16_PORT), (1UL<<PTC16_NUM)};
const DigitalIO digitalIO_PTC17    = {&PCR(PTC17_PORT,  PTC17_NUM), GPIO(PTC17_PORT), PORT_CLOCK_MASK(PTC17_PORT), (1UL<<PTC17_NUM)};
const DigitalIO digitalIO_PTC18    = {&PCR(PTC18_PORT,  PTC18_NUM), GPIO(PTC18_PORT), PORT_CLOCK_MASK(PTC18_PORT), (1UL<<PTC18_NUM)};
const DigitalIO digitalIO_PTC19    = {&PCR(PTC19_PORT,  PTC19_NUM), GPIO(PTC19_PORT), PORT_CLOCK_MASK(PTC19_PORT), (1UL<<PTC19_NUM)};
const DigitalIO digitalIO_PTD0     = {&PCR(PTD0_PORT,   PTD0_NUM),  GPIO(PTD0_PORT),  PORT_CLOCK_MASK(PTD0_PORT),  (1UL<<PTD0_NUM)};
const DigitalIO digitalIO_PTD1     = {&PCR(PTD1_PORT,   PTD1_NUM),  GPIO(PTD1_PORT),  PORT_CLOCK_MASK(PTD1_PORT),  (1UL<<PTD1_NUM)};
const DigitalIO digitalIO_PTD2     = {&PCR(PTD2_PORT,   PTD2_NUM),  GPIO(PTD2_PORT),  PORT_CLOCK_MASK(PTD2_PORT),  (1UL<<PTD2_NUM)};
const DigitalIO digitalIO_PTD3     = {&PCR(PTD3_PORT,   PTD3_NUM),  GPIO(PTD3_PORT),  PORT_CLOCK_MASK(PTD3_PORT),  (1UL<<PTD3_NUM)};
const DigitalIO digitalIO_PTD4     = {&PCR(PTD4_PORT,   PTD4_NUM),  GPIO(PTD4_PORT),  PORT_CLOCK_MASK(PTD4_PORT),  (1UL<<PTD4_NUM)};
const DigitalIO digitalIO_PTD5     = {&PCR(PTD5_PORT,   PTD5_NUM),  GPIO(PTD5_PORT),  PORT_CLOCK_MASK(PTD5_PORT),  (1UL<<PTD5_NUM)};
const DigitalIO digitalIO_PTD6     = {&PCR(PTD6_PORT,   PTD6_NUM),  GPIO(PTD6_PORT),  PORT_CLOCK_MASK(PTD6_PORT),  (1UL<<PTD6_NUM)};
const DigitalIO digitalIO_PTD7     = {&PCR(PTD7_PORT,   PTD7_NUM),  GPIO(PTD7_PORT),  PORT_CLOCK_MASK(PTD7_PORT),  (1UL<<PTD7_NUM)};
const DigitalIO digitalIO_PTD8     = {&PCR(PTD8_PORT,   PTD8_NUM),  GPIO(PTD8_PORT),  PORT_CLOCK_MASK(PTD8_PORT),  (1UL<<PTD8_NUM)};
const DigitalIO digitalIO_PTD9     = {&PCR(PTD9_PORT,   PTD9_NUM),  GPIO(PTD9_PORT),  PORT_CLOCK_MASK(PTD9_PORT),  (1UL<<PTD9_NUM)};
const DigitalIO digitalIO_PTD10    = {&PCR(PTD10_PORT,  PTD10_NUM), GPIO(PTD10_PORT), PORT_CLOCK_MASK(PTD10_PORT), (1UL<<PTD10_NUM)};
const DigitalIO digitalIO_PTD11    = {&PCR(PTD11_PORT,  PTD11_NUM), GPIO(PTD11_PORT), PORT_CLOCK_MASK(PTD11_PORT), (1UL<<PTD11_NUM)};
const DigitalIO digitalIO_PTD12    = {&PCR(PTD12_PORT,  PTD12_NUM), GPIO(PTD12_PORT), PORT_CLOCK_MASK(PTD12_PORT), (1UL<<PTD12_NUM)};
const DigitalIO digitalIO_PTD13    = {&PCR(PTD13_PORT,  PTD13_NUM), GPIO(PTD13_PORT), PORT_CLOCK_MASK(PTD13_PORT), (1UL<<PTD13_NUM)};
const DigitalIO digitalIO_PTD14    = {&PCR(PTD14_PORT,  PTD14_NUM), GPIO(PTD14_PORT), PORT_CLOCK_MASK(PTD14_PORT), (1UL<<PTD14_NUM)};
const DigitalIO digitalIO_PTD15    = {&PCR(PTD15_PORT,  PTD15_NUM), GPIO(PTD15_PORT), PORT_CLOCK_MASK(PTD15_PORT), (1UL<<PTD15_NUM)};
const DigitalIO digitalIO_PTE0     = {&PCR(PTE0_PORT,   PTE0_NUM),  GPIO(PTE0_PORT),  PORT_CLOCK_MASK(PTE0_PORT),  (1UL<<PTE0_NUM)};
const DigitalIO digitalIO_PTE1     = {&PCR(PTE1_PORT,   PTE1_NUM),  GPIO(PTE1_PORT),  PORT_CLOCK_MASK(PTE1_PORT),  (1UL<<PTE1_NUM)};
const DigitalIO digitalIO_PTE2     = {&PCR(PTE2_PORT,   PTE2_NUM),  GPIO(PTE2_PORT),  PORT_CLOCK_MASK(PTE2_PORT),  (1UL<<PTE2_NUM)};
const DigitalIO digitalIO_PTE3     = {&PCR(PTE3_PORT,   PTE3_NUM),  GPIO(PTE3_PORT),  PORT_CLOCK_MASK(PTE3_PORT),  (1UL<<PTE3_NUM)};
const DigitalIO digitalIO_PTE4     = {&PCR(PTE4_PORT,   PTE4_NUM),  GPIO(PTE4_PORT),  PORT_CLOCK_MASK(PTE4_PORT),  (1UL<<PTE4_NUM)};
const DigitalIO digitalIO_PTE5     = {&PCR(PTE5_PORT,   PTE5_NUM),  GPIO(PTE5_PORT),  PORT_CLOCK_MASK(PTE5_PORT),  (1UL<<PTE5_NUM)};
const DigitalIO digitalIO_PTE6     = {&PCR(PTE6_PORT,   PTE6_NUM),  GPIO(PTE6_PORT),  PORT_CLOCK_MASK(PTE6_PORT),  (1UL<<PTE6_NUM)};
const DigitalIO digitalIO_PTE7     = {&PCR(PTE7_PORT,   PTE7_NUM),  GPIO(PTE7_PORT),  PORT_CLOCK_MASK(PTE7_PORT),  (1UL<<PTE7_NUM)};
const DigitalIO digitalIO_PTE8     = {&PCR(PTE8_PORT,   PTE8_NUM),  GPIO(PTE8_PORT),  PORT_CLOCK_MASK(PTE8_PORT),  (1UL<<PTE8_NUM)};
const DigitalIO digitalIO_PTE9     = {&PCR(PTE9_PORT,   PTE9_NUM),  GPIO(PTE9_PORT),  PORT_CLOCK_MASK(PTE9_PORT),  (1UL<<PTE9_NUM)};
const DigitalIO digitalIO_PTE10    = {&PCR(PTE10_PORT,  PTE10_NUM), GPIO(PTE10_PORT), PORT_CLOCK_MASK(PTE10_PORT), (1UL<<PTE10_NUM)};
const DigitalIO digitalIO_PTE11    = {&PCR(PTE11_PORT,  PTE11_NUM), GPIO(PTE11_PORT), PORT_CLOCK_MASK(PTE11_PORT), (1UL<<PTE11_NUM)};
const DigitalIO digitalIO_PTE12    = {&PCR(PTE12_PORT,  PTE12_NUM), GPIO(PTE12_PORT), PORT_CLOCK_MASK(PTE12_PORT), (1UL<<PTE12_NUM)};
const DigitalIO digitalIO_PTE24    = {&PCR(PTE24_PORT,  PTE24_NUM), GPIO(PTE24_PORT), PORT_CLOCK_MASK(PTE24_PORT), (1UL<<PTE24_NUM)};
const DigitalIO digitalIO_PTE25    = {&PCR(PTE25_PORT,  PTE25_NUM), GPIO(PTE25_PORT), PORT_CLOCK_MASK(PTE25_PORT), (1UL<<PTE25_NUM)};
const DigitalIO digitalIO_PTE26    = {&PCR(PTE26_PORT,  PTE26_NUM), GPIO(PTE26_PORT), PORT_CLOCK_MASK(PTE26_PORT), (1UL<<PTE26_NUM)};
const DigitalIO digitalIO_PTE27    = {&PCR(PTE27_PORT,  PTE27_NUM), GPIO(PTE27_PORT), PORT_CLOCK_MASK(PTE27_PORT), (1UL<<PTE27_NUM)};
const DigitalIO digitalIO_PTE28    = {&PCR(PTE28_PORT,  PTE28_NUM), GPIO(PTE28_PORT), PORT_CLOCK_MASK(PTE28_PORT), (1UL<<PTE28_NUM)};
const AnalogueIO analogueIO_ADC0_SE16   = {0,                ADC(ADC0_SE16_ADC_NUM),&ADC_CLOCK_REG(ADC0_SE16_ADC_NUM),ADC_CLOCK_MASK(ADC0_SE16_ADC_NUM),ADC0_SE16_ADC_CH};
const AnalogueIO analogueIO_ADC0_SE23   = {0,                ADC(ADC0_SE23_ADC_NUM),&ADC_CLOCK_REG(ADC0_SE23_ADC_NUM),ADC_CLOCK_MASK(ADC0_SE23_ADC_NUM),ADC0_SE23_ADC_CH};
const AnalogueIO analogueIO_ADC1_SE16   = {0,                ADC(ADC1_SE16_ADC_NUM),&ADC_CLOCK_REG(ADC1_SE16_ADC_NUM),ADC_CLOCK_MASK(ADC1_SE16_ADC_NUM),ADC1_SE16_ADC_CH};
const AnalogueIO analogueIO_ADC1_SE18   = {0,                ADC(ADC1_SE18_ADC_NUM),&ADC_CLOCK_REG(ADC1_SE18_ADC_NUM),ADC_CLOCK_MASK(ADC1_SE18_ADC_NUM),ADC1_SE18_ADC_CH};
const AnalogueIO analogueIO_ADC1_SE23   = {0,                ADC(ADC1_SE23_ADC_NUM),&ADC_CLOCK_REG(ADC1_SE23_ADC_NUM),ADC_CLOCK_MASK(ADC1_SE23_ADC_NUM),ADC1_SE23_ADC_CH};
const AnalogueIO analogueIO_PTA7        = {&digitalIO_PTA7,  ADC(PTA7_ADC_NUM),  &ADC_CLOCK_REG(PTA7_ADC_NUM),  ADC_CLOCK_MASK(PTA7_ADC_NUM),  PTA7_ADC_CH};
const AnalogueIO analogueIO_PTA8        = {&digitalIO_PTA8,  ADC(PTA8_ADC_NUM),  &ADC_CLOCK_REG(PTA8_ADC_NUM),  ADC_CLOCK_MASK(PTA8_ADC_NUM),  PTA8_ADC_CH};
const AnalogueIO analogueIO_PTA17       = {&digitalIO_PTA17, ADC(PTA17_ADC_NUM), &ADC_CLOCK_REG(PTA17_ADC_NUM), ADC_CLOCK_MASK(PTA17_ADC_NUM), PTA17_ADC_CH};
const AnalogueIO analogueIO_PTB0        = {&digitalIO_PTB0,  ADC(PTB0_ADC_NUM),  &ADC_CLOCK_REG(PTB0_ADC_NUM),  ADC_CLOCK_MASK(PTB0_ADC_NUM),  PTB0_ADC_CH};
const AnalogueIO analogueIO_PTB1        = {&digitalIO_PTB1,  ADC(PTB1_ADC_NUM),  &ADC_CLOCK_REG(PTB1_ADC_NUM),  ADC_CLOCK_MASK(PTB1_ADC_NUM),  PTB1_ADC_CH};
const AnalogueIO analogueIO_PTB2        = {&digitalIO_PTB2,  ADC(PTB2_ADC_NUM),  &ADC_CLOCK_REG(PTB2_ADC_NUM),  ADC_CLOCK_MASK(PTB2_ADC_NUM),  PTB2_ADC_CH};
const AnalogueIO analogueIO_PTB3        = {&digitalIO_PTB3,  ADC(PTB3_ADC_NUM),  &ADC_CLOCK_REG(PTB3_ADC_NUM),  ADC_CLOCK_MASK(PTB3_ADC_NUM),  PTB3_ADC_CH};
const AnalogueIO analogueIO_PTB4        = {&digitalIO_PTB4,  ADC(PTB4_ADC_NUM),  &ADC_CLOCK_REG(PTB4_ADC_NUM),  ADC_CLOCK_MASK(PTB4_ADC_NUM),  PTB4_ADC_CH};
const AnalogueIO analogueIO_PTB5        = {&digitalIO_PTB5,  ADC(PTB5_ADC_NUM),  &ADC_CLOCK_REG(PTB5_ADC_NUM),  ADC_CLOCK_MASK(PTB5_ADC_NUM),  PTB5_ADC_CH};
const AnalogueIO analogueIO_PTB6        = {&digitalIO_PTB6,  ADC(PTB6_ADC_NUM),  &ADC_CLOCK_REG(PTB6_ADC_NUM),  ADC_CLOCK_MASK(PTB6_ADC_NUM),  PTB6_ADC_CH};
const AnalogueIO analogueIO_PTB7        = {&digitalIO_PTB7,  ADC(PTB7_ADC_NUM),  &ADC_CLOCK_REG(PTB7_ADC_NUM),  ADC_CLOCK_MASK(PTB7_ADC_NUM),  PTB7_ADC_CH};
const AnalogueIO analogueIO_PTB10       = {&digitalIO_PTB10, ADC(PTB10_ADC_NUM), &ADC_CLOCK_REG(PTB10_ADC_NUM), ADC_CLOCK_MASK(PTB10_ADC_NUM), PTB10_ADC_CH};
const AnalogueIO analogueIO_PTB11       = {&digitalIO_PTB11, ADC(PTB11_ADC_NUM), &ADC_CLOCK_REG(PTB11_ADC_NUM), ADC_CLOCK_MASK(PTB11_ADC_NUM), PTB11_ADC_CH};
const AnalogueIO analogueIO_PTC0        = {&digitalIO_PTC0,  ADC(PTC0_ADC_NUM),  &ADC_CLOCK_REG(PTC0_ADC_NUM),  ADC_CLOCK_MASK(PTC0_ADC_NUM),  PTC0_ADC_CH};
const AnalogueIO analogueIO_PTC1        = {&digitalIO_PTC1,  ADC(PTC1_ADC_NUM),  &ADC_CLOCK_REG(PTC1_ADC_NUM),  ADC_CLOCK_MASK(PTC1_ADC_NUM),  PTC1_ADC_CH};
const AnalogueIO analogueIO_PTC2        = {&digitalIO_PTC2,  ADC(PTC2_ADC_NUM),  &ADC_CLOCK_REG(PTC2_ADC_NUM),  ADC_CLOCK_MASK(PTC2_ADC_NUM),  PTC2_ADC_CH};
const AnalogueIO analogueIO_PTC8        = {&digitalIO_PTC8,  ADC(PTC8_ADC_NUM),  &ADC_CLOCK_REG(PTC8_ADC_NUM),  ADC_CLOCK_MASK(PTC8_ADC_NUM),  PTC8_ADC_CH};
const AnalogueIO analogueIO_PTC9        = {&digitalIO_PTC9,  ADC(PTC9_ADC_NUM),  &ADC_CLOCK_REG(PTC9_ADC_NUM),  ADC_CLOCK_MASK(PTC9_ADC_NUM),  PTC9_ADC_CH};
const AnalogueIO analogueIO_PTC10       = {&digitalIO_PTC10, ADC(PTC10_ADC_NUM), &ADC_CLOCK_REG(PTC10_ADC_NUM), ADC_CLOCK_MASK(PTC10_ADC_NUM), PTC10_ADC_CH};
const AnalogueIO analogueIO_PTC11       = {&digitalIO_PTC11, ADC(PTC11_ADC_NUM), &ADC_CLOCK_REG(PTC11_ADC_NUM), ADC_CLOCK_MASK(PTC11_ADC_NUM), PTC11_ADC_CH};
const AnalogueIO analogueIO_PTD1        = {&digitalIO_PTD1,  ADC(PTD1_ADC_NUM),  &ADC_CLOCK_REG(PTD1_ADC_NUM),  ADC_CLOCK_MASK(PTD1_ADC_NUM),  PTD1_ADC_CH};
const AnalogueIO analogueIO_PTD5        = {&digitalIO_PTD5,  ADC(PTD5_ADC_NUM),  &ADC_CLOCK_REG(PTD5_ADC_NUM),  ADC_CLOCK_MASK(PTD5_ADC_NUM),  PTD5_ADC_CH};
const AnalogueIO analogueIO_PTD6        = {&digitalIO_PTD6,  ADC(PTD6_ADC_NUM),  &ADC_CLOCK_REG(PTD6_ADC_NUM),  ADC_CLOCK_MASK(PTD6_ADC_NUM),  PTD6_ADC_CH};
const AnalogueIO analogueIO_PTE24       = {&digitalIO_PTE24, ADC(PTE24_ADC_NUM), &ADC_CLOCK_REG(PTE24_ADC_NUM), ADC_CLOCK_MASK(PTE24_ADC_NUM), PTE24_ADC_CH};
const AnalogueIO analogueIO_PTE25       = {&digitalIO_PTE25, ADC(PTE25_ADC_NUM), &ADC_CLOCK_REG(PTE25_ADC_NUM), ADC_CLOCK_MASK(PTE25_ADC_NUM), PTE25_ADC_CH};
#if FTM0_5_SEL == 1
const PwmIO  pwmIO_PTA0      = {&digitalIO_PTA0,   (volatile FTM_Type*)FTM(PTA0_FTM_NUM),  PTA0_FTM_CH,   PORT_PCR_MUX(PTA0_FTM_FN),  &FTM_CLOCK_REG(PTA0_FTM_NUM),  FTM_CLOCK_MASK(PTA0_FTM_NUM)};
#endif
#if FTM0_6_SEL == 1
const PwmIO  pwmIO_PTA1      = {&digitalIO_PTA1,   (volatile FTM_Type*)FTM(PTA1_FTM_NUM),  PTA1_FTM_CH,   PORT_PCR_MUX(PTA1_FTM_FN),  &FTM_CLOCK_REG(PTA1_FTM_NUM),  FTM_CLOCK_MASK(PTA1_FTM_NUM)};
#endif
#if FTM0_7_SEL == 1
const PwmIO  pwmIO_PTA2      = {&digitalIO_PTA2,   (volatile FTM_Type*)FTM(PTA2_FTM_NUM),  PTA2_FTM_CH,   PORT_PCR_MUX(PTA2_FTM_FN),  &FTM_CLOCK_REG(PTA2_FTM_NUM),  FTM_CLOCK_MASK(PTA2_FTM_NUM)};
#endif
#if FTM0_0_SEL == 1
const PwmIO  pwmIO_PTA3      = {&digitalIO_PTA3,   (volatile FTM_Type*)FTM(PTA3_FTM_NUM),  PTA3_FTM_CH,   PORT_PCR_MUX(PTA3_FTM_FN),  &FTM_CLOCK_REG(PTA3_FTM_NUM),  FTM_CLOCK_MASK(PTA3_FTM_NUM)};
#endif
#if FTM0_1_SEL == 1
const PwmIO  pwmIO_PTA4      = {&digitalIO_PTA4,   (volatile FTM_Type*)FTM(PTA4_FTM_NUM),  PTA4_FTM_CH,   PORT_PCR_MUX(PTA4_FTM_FN),  &FTM_CLOCK_REG(PTA4_FTM_NUM),  FTM_CLOCK_MASK(PTA4_FTM_NUM)};
#endif
#if FTM0_2_SEL == 1
const PwmIO  pwmIO_PTA5      = {&digitalIO_PTA5,   (volatile FTM_Type*)FTM(PTA5_FTM_NUM),  PTA5_FTM_CH,   PORT_PCR_MUX(PTA5_FTM_FN),  &FTM_CLOCK_REG(PTA5_FTM_NUM),  FTM_CLOCK_MASK(PTA5_FTM_NUM)};
#endif
#if FTM0_3_SEL == 1
const PwmIO  pwmIO_PTA6      = {&digitalIO_PTA6,   (volatile FTM_Type*)FTM(PTA6_FTM_NUM),  PTA6_FTM_CH,   PORT_PCR_MUX(PTA6_FTM_FN),  &FTM_CLOCK_REG(PTA6_FTM_NUM),  FTM_CLOCK_MASK(PTA6_FTM_NUM)};
#endif
#if FTM0_4_SEL == 1
const PwmIO  pwmIO_PTA7      = {&digitalIO_PTA7,   (volatile FTM_Type*)FTM(PTA7_FTM_NUM),  PTA7_FTM_CH,   PORT_PCR_MUX(PTA7_FTM_FN),  &FTM_CLOCK_REG(PTA7_FTM_NUM),  FTM_CLOCK_MASK(PTA7_FTM_NUM)};
#endif
#if FTM1_0_SEL == 1
const PwmIO  pwmIO_PTA8      = {&digitalIO_PTA8,   (volatile FTM_Type*)FTM(PTA8_FTM_NUM),  PTA8_FTM_CH,   PORT_PCR_MUX(PTA8_FTM_FN),  &FTM_CLOCK_REG(PTA8_FTM_NUM),  FTM_CLOCK_MASK(PTA8_FTM_NUM)};
#endif
#if FTM1_1_SEL == 1
const PwmIO  pwmIO_PTA9      = {&digitalIO_PTA9,   (volatile FTM_Type*)FTM(PTA9_FTM_NUM),  PTA9_FTM_CH,   PORT_PCR_MUX(PTA9_FTM_FN),  &FTM_CLOCK_REG(PTA9_FTM_NUM),  FTM_CLOCK_MASK(PTA9_FTM_NUM)};
#endif
#if FTM2_0_SEL == 1
const PwmIO  pwmIO_PTA10     = {&digitalIO_PTA10,  (volatile FTM_Type*)FTM(PTA10_FTM_NUM), PTA10_FTM_CH,  PORT_PCR_MUX(PTA10_FTM_FN), &FTM_CLOCK_REG(PTA10_FTM_NUM), FTM_CLOCK_MASK(PTA10_FTM_NUM)};
#endif
#if FTM2_1_SEL == 1
const PwmIO  pwmIO_PTA11     = {&digitalIO_PTA11,  (volatile FTM_Type*)FTM(PTA11_FTM_NUM), PTA11_FTM_CH,  PORT_PCR_MUX(PTA11_FTM_FN), &FTM_CLOCK_REG(PTA11_FTM_NUM), FTM_CLOCK_MASK(PTA11_FTM_NUM)};
#endif
#if FTM1_0_SEL == 2
const PwmIO  pwmIO_PTA12     = {&digitalIO_PTA12,  (volatile FTM_Type*)FTM(PTA12_FTM_NUM), PTA12_FTM_CH,  PORT_PCR_MUX(PTA12_FTM_FN), &FTM_CLOCK_REG(PTA12_FTM_NUM), FTM_CLOCK_MASK(PTA12_FTM_NUM)};
#endif
#if FTM1_1_SEL == 2
const PwmIO  pwmIO_PTA13     = {&digitalIO_PTA13,  (volatile FTM_Type*)FTM(PTA13_FTM_NUM), PTA13_FTM_CH,  PORT_PCR_MUX(PTA13_FTM_FN), &FTM_CLOCK_REG(PTA13_FTM_NUM), FTM_CLOCK_MASK(PTA13_FTM_NUM)};
#endif
#if FTM1_0_SEL == 3
const PwmIO  pwmIO_PTB0      = {&digitalIO_PTB0,   (volatile FTM_Type*)FTM(PTB0_FTM_NUM),  PTB0_FTM_CH,   PORT_PCR_MUX(PTB0_FTM_FN),  &FTM_CLOCK_REG(PTB0_FTM_NUM),  FTM_CLOCK_MASK(PTB0_FTM_NUM)};
#endif
#if FTM1_1_SEL == 3
const PwmIO  pwmIO_PTB1      = {&digitalIO_PTB1,   (volatile FTM_Type*)FTM(PTB1_FTM_NUM),  PTB1_FTM_CH,   PORT_PCR_MUX(PTB1_FTM_FN),  &FTM_CLOCK_REG(PTB1_FTM_NUM),  FTM_CLOCK_MASK(PTB1_FTM_NUM)};
#endif
#if FTM1_0_SEL == 4
const PwmIO  pwmIO_PTB12     = {&digitalIO_PTB12,  (volatile FTM_Type*)FTM(PTB12_FTM_NUM), PTB12_FTM_CH,  PORT_PCR_MUX(PTB12_FTM_FN), &FTM_CLOCK_REG(PTB12_FTM_NUM), FTM_CLOCK_MASK(PTB12_FTM_NUM)};
#endif
#if FTM0_4_SEL == 2
const PwmIO  pwmIO_PTB12     = {&digitalIO_PTB12,  (volatile FTM_Type*)FTM(PTB12_FTM_NUM), PTB12_FTM_CH,  PORT_PCR_MUX(PTB12_FTM_FN), &FTM_CLOCK_REG(PTB12_FTM_NUM), FTM_CLOCK_MASK(PTB12_FTM_NUM)};
#endif
#if FTM1_1_SEL == 4
const PwmIO  pwmIO_PTB13     = {&digitalIO_PTB13,  (volatile FTM_Type*)FTM(PTB13_FTM_NUM), PTB13_FTM_CH,  PORT_PCR_MUX(PTB13_FTM_FN), &FTM_CLOCK_REG(PTB13_FTM_NUM), FTM_CLOCK_MASK(PTB13_FTM_NUM)};
#endif
#if FTM0_5_SEL == 2
const PwmIO  pwmIO_PTB13     = {&digitalIO_PTB13,  (volatile FTM_Type*)FTM(PTB13_FTM_NUM), PTB13_FTM_CH,  PORT_PCR_MUX(PTB13_FTM_FN), &FTM_CLOCK_REG(PTB13_FTM_NUM), FTM_CLOCK_MASK(PTB13_FTM_NUM)};
#endif
#if FTM2_0_SEL == 2
const PwmIO  pwmIO_PTB18     = {&digitalIO_PTB18,  (volatile FTM_Type*)FTM(PTB18_FTM_NUM), PTB18_FTM_CH,  PORT_PCR_MUX(PTB18_FTM_FN), &FTM_CLOCK_REG(PTB18_FTM_NUM), FTM_CLOCK_MASK(PTB18_FTM_NUM)};
#endif
#if FTM2_1_SEL == 2
const PwmIO  pwmIO_PTB19     = {&digitalIO_PTB19,  (volatile FTM_Type*)FTM(PTB19_FTM_NUM), PTB19_FTM_CH,  PORT_PCR_MUX(PTB19_FTM_FN), &FTM_CLOCK_REG(PTB19_FTM_NUM), FTM_CLOCK_MASK(PTB19_FTM_NUM)};
#endif
#if FTM0_0_SEL == 2
const PwmIO  pwmIO_PTC1      = {&digitalIO_PTC1,   (volatile FTM_Type*)FTM(PTC1_FTM_NUM),  PTC1_FTM_CH,   PORT_PCR_MUX(PTC1_FTM_FN),  &FTM_CLOCK_REG(PTC1_FTM_NUM),  FTM_CLOCK_MASK(PTC1_FTM_NUM)};
#endif
#if FTM0_1_SEL == 2
const PwmIO  pwmIO_PTC2      = {&digitalIO_PTC2,   (volatile FTM_Type*)FTM(PTC2_FTM_NUM),  PTC2_FTM_CH,   PORT_PCR_MUX(PTC2_FTM_FN),  &FTM_CLOCK_REG(PTC2_FTM_NUM),  FTM_CLOCK_MASK(PTC2_FTM_NUM)};
#endif
#if FTM0_2_SEL == 2
const PwmIO  pwmIO_PTC3      = {&digitalIO_PTC3,   (volatile FTM_Type*)FTM(PTC3_FTM_NUM),  PTC3_FTM_CH,   PORT_PCR_MUX(PTC3_FTM_FN),  &FTM_CLOCK_REG(PTC3_FTM_NUM),  FTM_CLOCK_MASK(PTC3_FTM_NUM)};
#endif
#if FTM0_3_SEL == 2
const PwmIO  pwmIO_PTC4      = {&digitalIO_PTC4,   (volatile FTM_Type*)FTM(PTC4_FTM_NUM),  PTC4_FTM_CH,   PORT_PCR_MUX(PTC4_FTM_FN),  &FTM_CLOCK_REG(PTC4_FTM_NUM),  FTM_CLOCK_MASK(PTC4_FTM_NUM)};
#endif
#if FTM0_2_SEL == 3
const PwmIO  pwmIO_PTC5      = {&digitalIO_PTC5,   (volatile FTM_Type*)FTM(PTC5_FTM_NUM),  PTC5_FTM_CH,   PORT_PCR_MUX(PTC5_FTM_FN),  &FTM_CLOCK_REG(PTC5_FTM_NUM),  FTM_CLOCK_MASK(PTC5_FTM_NUM)};
#endif
#if FTM3_4_SEL == 1
const PwmIO  pwmIO_PTC8      = {&digitalIO_PTC8,   (volatile FTM_Type*)FTM(PTC8_FTM_NUM),  PTC8_FTM_CH,   PORT_PCR_MUX(PTC8_FTM_FN),  &FTM_CLOCK_REG(PTC8_FTM_NUM),  FTM_CLOCK_MASK(PTC8_FTM_NUM)};
#endif
#if FTM3_5_SEL == 1
const PwmIO  pwmIO_PTC9      = {&digitalIO_PTC9,   (volatile FTM_Type*)FTM(PTC9_FTM_NUM),  PTC9_FTM_CH,   PORT_PCR_MUX(PTC9_FTM_FN),  &FTM_CLOCK_REG(PTC9_FTM_NUM),  FTM_CLOCK_MASK(PTC9_FTM_NUM)};
#endif
#if FTM3_6_SEL == 1
const PwmIO  pwmIO_PTC10     = {&digitalIO_PTC10,  (volatile FTM_Type*)FTM(PTC10_FTM_NUM), PTC10_FTM_CH,  PORT_PCR_MUX(PTC10_FTM_FN), &FTM_CLOCK_REG(PTC10_FTM_NUM), FTM_CLOCK_MASK(PTC10_FTM_NUM)};
#endif
#if FTM3_7_SEL == 1
const PwmIO  pwmIO_PTC11     = {&digitalIO_PTC11,  (volatile FTM_Type*)FTM(PTC11_FTM_NUM), PTC11_FTM_CH,  PORT_PCR_MUX(PTC11_FTM_FN), &FTM_CLOCK_REG(PTC11_FTM_NUM), FTM_CLOCK_MASK(PTC11_FTM_NUM)};
#endif
#if FTM3_0_SEL == 1
const PwmIO  pwmIO_PTD0      = {&digitalIO_PTD0,   (volatile FTM_Type*)FTM(PTD0_FTM_NUM),  PTD0_FTM_CH,   PORT_PCR_MUX(PTD0_FTM_FN),  &FTM_CLOCK_REG(PTD0_FTM_NUM),  FTM_CLOCK_MASK(PTD0_FTM_NUM)};
#endif
#if FTM3_1_SEL == 1
const PwmIO  pwmIO_PTD1      = {&digitalIO_PTD1,   (volatile FTM_Type*)FTM(PTD1_FTM_NUM),  PTD1_FTM_CH,   PORT_PCR_MUX(PTD1_FTM_FN),  &FTM_CLOCK_REG(PTD1_FTM_NUM),  FTM_CLOCK_MASK(PTD1_FTM_NUM)};
#endif
#if FTM3_2_SEL == 1
const PwmIO  pwmIO_PTD2      = {&digitalIO_PTD2,   (volatile FTM_Type*)FTM(PTD2_FTM_NUM),  PTD2_FTM_CH,   PORT_PCR_MUX(PTD2_FTM_FN),  &FTM_CLOCK_REG(PTD2_FTM_NUM),  FTM_CLOCK_MASK(PTD2_FTM_NUM)};
#endif
#if FTM3_3_SEL == 1
const PwmIO  pwmIO_PTD3      = {&digitalIO_PTD3,   (volatile FTM_Type*)FTM(PTD3_FTM_NUM),  PTD3_FTM_CH,   PORT_PCR_MUX(PTD3_FTM_FN),  &FTM_CLOCK_REG(PTD3_FTM_NUM),  FTM_CLOCK_MASK(PTD3_FTM_NUM)};
#endif
#if FTM0_4_SEL == 3
const PwmIO  pwmIO_PTD4      = {&digitalIO_PTD4,   (volatile FTM_Type*)FTM(PTD4_FTM_NUM),  PTD4_FTM_CH,   PORT_PCR_MUX(PTD4_FTM_FN),  &FTM_CLOCK_REG(PTD4_FTM_NUM),  FTM_CLOCK_MASK(PTD4_FTM_NUM)};
#endif
#if FTM0_5_SEL == 3
const PwmIO  pwmIO_PTD5      = {&digitalIO_PTD5,   (volatile FTM_Type*)FTM(PTD5_FTM_NUM),  PTD5_FTM_CH,   PORT_PCR_MUX(PTD5_FTM_FN),  &FTM_CLOCK_REG(PTD5_FTM_NUM),  FTM_CLOCK_MASK(PTD5_FTM_NUM)};
#endif
#if FTM0_6_SEL == 2
const PwmIO  pwmIO_PTD6      = {&digitalIO_PTD6,   (volatile FTM_Type*)FTM(PTD6_FTM_NUM),  PTD6_FTM_CH,   PORT_PCR_MUX(PTD6_FTM_FN),  &FTM_CLOCK_REG(PTD6_FTM_NUM),  FTM_CLOCK_MASK(PTD6_FTM_NUM)};
#endif
#if FTM0_7_SEL == 2
const PwmIO  pwmIO_PTD7      = {&digitalIO_PTD7,   (volatile FTM_Type*)FTM(PTD7_FTM_NUM),  PTD7_FTM_CH,   PORT_PCR_MUX(PTD7_FTM_FN),  &FTM_CLOCK_REG(PTD7_FTM_NUM),  FTM_CLOCK_MASK(PTD7_FTM_NUM)};
#endif
#if FTM3_0_SEL == 2
const PwmIO  pwmIO_PTE5      = {&digitalIO_PTE5,   (volatile FTM_Type*)FTM(PTE5_FTM_NUM),  PTE5_FTM_CH,   PORT_PCR_MUX(PTE5_FTM_FN),  &FTM_CLOCK_REG(PTE5_FTM_NUM),  FTM_CLOCK_MASK(PTE5_FTM_NUM)};
#endif
#if FTM3_1_SEL == 2
const PwmIO  pwmIO_PTE6      = {&digitalIO_PTE6,   (volatile FTM_Type*)FTM(PTE6_FTM_NUM),  PTE6_FTM_CH,   PORT_PCR_MUX(PTE6_FTM_FN),  &FTM_CLOCK_REG(PTE6_FTM_NUM),  FTM_CLOCK_MASK(PTE6_FTM_NUM)};
#endif
#if FTM3_2_SEL == 2
const PwmIO  pwmIO_PTE7      = {&digitalIO_PTE7,   (volatile FTM_Type*)FTM(PTE7_FTM_NUM),  PTE7_FTM_CH,   PORT_PCR_MUX(PTE7_FTM_FN),  &FTM_CLOCK_REG(PTE7_FTM_NUM),  FTM_CLOCK_MASK(PTE7_FTM_NUM)};
#endif
#if FTM3_3_SEL == 2
const PwmIO  pwmIO_PTE8      = {&digitalIO_PTE8,   (volatile FTM_Type*)FTM(PTE8_FTM_NUM),  PTE8_FTM_CH,   PORT_PCR_MUX(PTE8_FTM_FN),  &FTM_CLOCK_REG(PTE8_FTM_NUM),  FTM_CLOCK_MASK(PTE8_FTM_NUM)};
#endif
#if FTM3_4_SEL == 2
const PwmIO  pwmIO_PTE9      = {&digitalIO_PTE9,   (volatile FTM_Type*)FTM(PTE9_FTM_NUM),  PTE9_FTM_CH,   PORT_PCR_MUX(PTE9_FTM_FN),  &FTM_CLOCK_REG(PTE9_FTM_NUM),  FTM_CLOCK_MASK(PTE9_FTM_NUM)};
#endif
#if FTM3_5_SEL == 2
const PwmIO  pwmIO_PTE10     = {&digitalIO_PTE10,  (volatile FTM_Type*)FTM(PTE10_FTM_NUM), PTE10_FTM_CH,  PORT_PCR_MUX(PTE10_FTM_FN), &FTM_CLOCK_REG(PTE10_FTM_NUM), FTM_CLOCK_MASK(PTE10_FTM_NUM)};
#endif
#if FTM3_6_SEL == 2
const PwmIO  pwmIO_PTE11     = {&digitalIO_PTE11,  (volatile FTM_Type*)FTM(PTE11_FTM_NUM), PTE11_FTM_CH,  PORT_PCR_MUX(PTE11_FTM_FN), &FTM_CLOCK_REG(PTE11_FTM_NUM), FTM_CLOCK_MASK(PTE11_FTM_NUM)};
#endif
#if FTM3_7_SEL == 2
const PwmIO  pwmIO_PTE12     = {&digitalIO_PTE12,  (volatile FTM_Type*)FTM(PTE12_FTM_NUM), PTE12_FTM_CH,  PORT_PCR_MUX(PTE12_FTM_FN), &FTM_CLOCK_REG(PTE12_FTM_NUM), FTM_CLOCK_MASK(PTE12_FTM_NUM)};
#endif
