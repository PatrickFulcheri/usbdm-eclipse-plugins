/**
 * @file     HMC5883L.h
 * @brief    Interface for HMC5883L 3-axis magnetometer
 *
 * @version  V4.11.1.70
 * @date     18 June 2015
 */

#ifndef HMC5883L_H_
#define HMC5883L_H_

#include <stdint.h>
#include "I2C.h"

/**
 * @addtogroup HMC5883L_Group HMC5883L 3-axis magnetometer
 * @brief C++ Class allowing interface to HMC5883L
 * @{
 */

/**
 * @brief class representing an interface for HMC5883L 3-axis magnetometer over I2C
 *
 * <b>Example</b>
 * @code
 *  // Instantiate interface
 *      I2C      *i2c   = new I2C_0();
 *      HMC5883L *compass = new HMC5883L(i2c);
 *
 *      uint32_t id = compass->readID();
 *      printf("Compass ID = 0x%6lX (should be 0x483433)\n", id);
 *
 *      compass->setGain(1);
 *
 *      int16_t compassX,compassY,compassZ;
 *      compass->doMeasurement(&compassX, &compassY, &compassZ);
 *
 * @endcode
 */
class HMC5883L {

#define HMC5883L_CRA_MA(x) (((x)&0x3)<<5)    // # averages - 00=>1, 01=>2, 10=>4, 11=>8
#define HMC5883L_CRA_DO(x) (((x)&0x7)<<2)    // Data output rate (0.75, 1.5, 3, 7.5, 15, 30, 75) Hz
#define HMC5883L_CRA_MS(x) (((x)&0x3)<<0)    // Measurement configuration bits (Normal, +ve bias, -ve bias)

#define HMC5883L_CRB_GN(x) (((x)&0x7)<<5)    // Gain (0.88, 1.3, 1.9, 2.5, 4.0, 4.7, 5.6, 8.1) Gauss

#define HMC5883L_MR_MD(x)  (((x)&0x3)<<0)    // Mode (00=>continuous, 01=>single, 10,11=>idle)
#define HMC5883L_MR_DONE   (1<<7)            // Set to 1 internally after each single-measurement operation. Set to 0 when configuring mode register.

#define HMC5883L_SR_LOCK   (1<<1)   // Register values are locked when:
   //                               //  1. some, but not all of, the six data output registers have been read,
   //                               //  2. mode register has been read.
   //                               // Remains locked until:
   //                               //  1. all six result bytes have been read,
   //                               //  2. the mode register is changed,
   //                               //  3. the measurement configuration (CRA) is changed,
   //                               //  4. power is reset.
#define HMC5883L_SR_RDY    (1<<0)   // Ready Bit.
   //                               // Set when data is written to all six data registers.
   //                               // Cleared when device initiates a write to the data output registers and
   //                               // after one or more of the data output registers are written to.

private:
   I2C *i2c;
   static const uint8_t deviceAddress = 0x3C;

public:
   /**
    * Constructor
    *
    * @param i2c - I2C interface to use
    */
   HMC5883L(I2C *i2c);

   /**
    * Read ID from compass
    *
    * @return ID value as 24-bit number (0x483433 for HMC5883L)
    */
   uint32_t readID(void);

   /**
    * Set compass gain on all channels
    *
    * @param gain                                     \n
    * G    Recommended    Gain        Resolution       \n
    * 321  Sensor Range   (LSB/Gauss) (mGauss/LSB)     \n
    * 000   +/- 0.88 Ga    1370        0.73             \n
    * 001   +/- 1.3  Ga    1090        0.92 (default)   \n
    * 010   +/- 1.9  Ga     820        1.22             \n
    * 011   +/- 2.5  Ga     660        1.52             \n
    * 100   +/- 4.0  Ga     440        2.27             \n
    * 101   +/- 4.7  Ga     390        2.56             \n
    * 110   +/- 5.6  Ga     330        3.03             \n
    * 111   +/- 8.1  Ga     230        4.35
    */
   void setGain(uint8_t gain);

   /**
    * Set Control register values
    *
    * @param cra - Use HMC5883L_CRA_MA(), HMC5883L_CRA_DO(), HMC5883L_CRA_MS() macros to construct value
    * @param crb - Use HMC5883L_CRB_GN() macro to construct value
    */
   void setConfiguration(uint8_t cra, uint8_t crb);

   /**
    * Do a single triggered measurement of magnetic field
    *
    * @param x - X intensity
    * @param y - Y intensity
    * @param z - Z intensity
    */
   void doMeasurement(int16_t *x, int16_t *y, int16_t *z);

};

/**
 * @}
 */
#endif /* HMC5883L_H_ */
