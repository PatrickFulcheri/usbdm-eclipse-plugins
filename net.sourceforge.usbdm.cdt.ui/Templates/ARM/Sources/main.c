/*
 ============================================================================
 Name        : main.c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Basic C Main
 ============================================================================
 */
#include "derivative.h"
#include "utilities.h"

volatile int count = 0;

/*
 * Example use of interrupt handler
 *
 * See vectors.c for interrupt names
 */
__attribute__((__interrupt__))
void HardFault_Handler(void) {
   for(;;) {
      __breakpoint();
   }
}

int main(void) {

   for(count =0; count < 100; count++) {
      __asm__("nop");
   }
   // Generate Access error (interrupt handler demonstration)
   // (*(unsigned int *) 101) = 100;

   for(;;) {
      __asm__("nop");
   }
}
