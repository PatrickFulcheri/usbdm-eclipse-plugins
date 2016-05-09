package net.sourceforge.usbdm.deviceEditor.information;

public enum MuxSelection {
   unused(-4,"E"),
   fixed(-3,"F"),
   reset(-2,"R"),
   disabled(-1,"D"),
   mux0(0,"0"),
   mux1(1,"1"),
   mux2(2,"2"),
   mux3(3,"3"),
   mux4(4,"4"),
   mux5(5,"5"),
   mux6(6,"6"),
   mux7(7,"7");

   public final int     value;
   public final String  shortName;

   private MuxSelection(int value, String shortName) {
      this.value     = value;
      this.shortName = shortName;
   }
   /**
    * Maps an integer into a mux value
    * 
    * @param value Value to map
    * 
    * @return Corresponding mux value
    */
   public static MuxSelection valueOf(int value) {
      switch(value) {
      case -4 : return unused;
      case -3 : return fixed;
      case -2 : return reset;
      case -1 : return disabled;
      case 0  : return mux0;
      case 1  : return mux1;
      case 2  : return mux2;
      case 3  : return mux3;
      case 4  : return mux4;
      case 5  : return mux5;
      case 6  : return mux6;
      case 7  : return mux7;
      }
      throw new RuntimeException("No such enum, value = " + Integer.toString(value));
   }
   
   /**
    * Gets a short name for the mux values e.g. R = reset
    * 
    * @param value
    * @param shortName
    */
   public String getShortName() {
      return shortName;
   }
   /**
    * Indicates if the mux value is a mapped value i.e. 0...7<br>
    * rather than one of the sentinel values
    * 
    * @return
    */
   public boolean isMappedValue() {
      return this.ordinal() >= mux0.ordinal();
   }
   
   public String toString() {
      return super.toString()+"("+Integer.toString(value)+")";
   }
}