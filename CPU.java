import java.io.File;
import java.util.Scanner;

public class CPU
{
   public static void main(String args[])throws Exception
   {
      String inputFileName;//holds input sample.txt file
      int timerInterruptValue;//value for interrupt timer
   
      //filename and timer interrupt value arguments required
      if(args.length <2)
      {
         System.out.println("Insufficient Arguments! Please include filename and timer-interrupt value.");
         System.exit(0);//kill the program
      }
      
      //parse command line arguments
      inputFileName=args[0];
      timerInterruptValue=Integer.parseInt(args[1]);//String to int conversion
      
      //open input file
      File file = new File(inputFileName);
      Scanner input = new Scanner(file);
      
      System.out.println(input.nextLine());
      
      //Runtime class allows application to interface with environment
      Runtime currentRunTime = Runtime.getRuntime();
   }
}
