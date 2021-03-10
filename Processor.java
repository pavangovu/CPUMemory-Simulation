   /*
   Pavan Kumar Govu
      Prof. Ozbin
      CS 4348.001
      09 March 2021
   */

import java.util.*;
import java.io.*;

//Processor class simulates the CPU
public class Processor 
{
   //System Configuration
   private static int instructionCount=0;                //total number of instructions
   private static boolean inUserMode = true;             //whether or not system is in user mode
   private static boolean interruptsDisabled = false;    //Interrupts should be disabled during interrupt childProcessessing to avoid nested execution
   private static int userMemoryLimit=1000;              //0-999 for the user program
   private static int systemMemoryLimit=2000;            //1000-1999 for system code
   private static int alarm=0;                           //value for interrupt timer                                                             
   
   //System Registers
   private static int programCounter=0;                  //PC register
   private static int stackPointer=1000;                 //SPregister
   private static int instructionRegister=0;             //IR register
   private static int accumulator=0;                     //AC register
   private static int xRegister=0;                       //X register
   private static int yRegister=0;                       //Y register
   
   public static void main(String args[])throws Exception
   {
      String inputFileName;//holds input sample.txt file
               
      //filename and timer interrupt value arguments required
      if(args.length <2)
      {
         System.out.println("Insufficient Arguments! Please include filename and timer-interrupt value.");
         System.exit(0);//kill the program
      }
      
      //parse command line arguments
      inputFileName=args[0];
      alarm=Integer.parseInt(args[1]);//String to int conversion
   
      try
      {            
         Runtime currentRuntime = Runtime.getRuntime();
      
         //equivalent of UNIX fork command
         Process childProcess = currentRuntime.exec("java MainMemory");
      
         //set up communication between Main Memory and CPU
         InputStream inputStream = childProcess.getInputStream();
         Scanner memory_reader = new Scanner(inputStream);
         OutputStream outputStream = childProcess.getOutputStream();
         PrintWriter printWriter = new PrintWriter(outputStream);
      
         // Send file name to child childProcessess
         printWriter.printf(inputFileName + "\n");  //send filename to memory
         printWriter.flush();
         
         // this loop will keep the communication going between CPU and memory
         while (true)
         {
            
            // check to see if timer interrupt has occured
            if(instructionCount > 0 
                   && (instructionCount % alarm) == 0 && interruptsDisabled == false)
            {
               // childProcessess the interrupt
               interruptsDisabled = true;
               interruptFromTimer(printWriter, inputStream, memory_reader, outputStream);
            }
            
            // read instruction from memory
            int value = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            
            if (value != -1)
            {
               childProcessessInstruction(value, printWriter, inputStream, memory_reader, outputStream);
            }
            else
               break;
         }
         
         childProcess.waitFor();
         int exitVal = childProcess.exitValue();
         System.out.println("Process exited: " + exitVal);
      
      } 
      catch (IOException | InterruptedException t)
      {
         t.printStackTrace();
      }
   }

   // function to read data at given address from memory
   private static int readFromMemory(PrintWriter printWriter, InputStream inputStream, Scanner memory_reader, OutputStream outputStream, int address) 
   {
      checkMemoryViolation(address);
      printWriter.printf("1," + address + "\n");
      printWriter.flush();
      if (memory_reader.hasNext())
      {
         String temp = memory_reader.next();
         if(!temp.isEmpty())
         {
            int temp2 = Integer.parseInt(temp);
            return (temp2); 
         }
      
      }
      return -1;
   }
   
   //function to tell child childProcessess to write data at the given address in memory
   private static void writeToMemory(PrintWriter printWriter, InputStream inputStream, OutputStream outputStream, int address, int data) {
      printWriter.printf("2," + address + "," + data + "\n"); //2 at the stacurrentRuntime on string indicates write
      printWriter.flush();
   }

   // function to childProcessess an instruction received from the memory
   private static void childProcessessInstruction(int value, PrintWriter printWriter, InputStream inputStream, Scanner memory_reader, OutputStream outputStream) 
   {
      instructionRegister = value; //store instruction in Instruction register
      int operand;    //to store operand
      
      switch(instructionRegister)
      {
         case 1: //Load the value into the accumulator
            programCounter++; // increment counter to get operand
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            accumulator = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 2: // Load the value at the address into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, memory_reader, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
      
         case 3: // Load the value from the address found in the address into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, operand);
            accumulator = readFromMemory(printWriter, inputStream, memory_reader, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
             
         case 4: // Load the value at (address+xRegister) into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, memory_reader, outputStream, operand + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 5: //Load the value at (address+yRegister) into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, memory_reader, outputStream, operand + yRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 6: //Load from (Sp+xRegister) into the accumulator
            accumulator = readFromMemory(printWriter, inputStream, memory_reader, outputStream, stackPointer + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 7: //Store the value in the accumulator into the address
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            writeToMemory(printWriter, inputStream, outputStream, operand, accumulator);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 8: //Gets a random int from 1 to 100 into the accumulator
            Random r = new Random();
            int i = r.nextInt(100) + 1;
            accumulator = i;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 9: //If pocurrentRuntime=1, writes accumulator as an int to the screen
                //If pocurrentRuntime=2, writes accumulator as a char to the screen
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            if(operand == 1)
            {
               System.out.print(accumulator);
               if(interruptsDisabled == false) 
                  instructionCount++;
               programCounter++;
               break;
            
            }
            else if (operand == 2)
            {
               System.out.print((char)accumulator);
               if(interruptsDisabled == false) 
                  instructionCount++;
               programCounter++;
               break;
            }
            else
            {
               System.out.println("Error: PocurrentRuntime = " + operand);
               if(interruptsDisabled == false) 
                  instructionCount++;
               programCounter++;
               System.exit(0);
               break;
            }
            
         case 10: // Add the value in xRegister to the accumulator
            accumulator = accumulator + xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 11: //Add the value in yRegister to the accumulator
            accumulator = accumulator + yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 12: //Subtract the value in xRegister from the accumulator
            accumulator = accumulator - xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         case 13: //Subtract the value in yRegister from the accumulator
            accumulator = accumulator - yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 14: //Copy the value in the accumulator to xRegister
            xRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 15: //Copy the value in xRegister to the accumulator
            accumulator = xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 16: //Copy the value in the accumulator to yRegister
            yRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
             
         case 17: //Copy the value in yRegister to the accumulator
            accumulator = yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 18: //Copy the value in accumulator to the stackPointer
            stackPointer = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 19: //Copy the value in stackPointer to the accumulator 
            accumulator = stackPointer;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 20: // Jump to the address
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 21: // Jump to the address only if the value in the accumulator is zero
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            if (accumulator == 0) 
            {
               programCounter = operand;
               if(interruptsDisabled == false) 
                  instructionCount++;
               break;
            }
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
             
         case 22: // Jump to the address only if the value in the accumulator is not zero
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            if (accumulator != 0) 
            {
               programCounter = operand;
               if(interruptsDisabled == false) 
                  instructionCount++;
               break;
            }
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 23: //Push return address onto stack, jump to the address
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, memory_reader, outputStream, programCounter);
            pushValueToStack(printWriter, inputStream, outputStream,programCounter+1);
            userMemoryLimit = stackPointer;
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
             
         case 24: //Pop return address from the stack, jump to the address
            operand = popValueFromStack(printWriter, inputStream, memory_reader, outputStream);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 25: //Increment the value in xRegister
            xRegister++;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         case 26: //Decrement the value in xRegister
            xRegister--;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         case 27: // Push accumulator onto stack
            pushValueToStack(printWriter, inputStream, outputStream,accumulator);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 28: //Pop from stack into accumulator
            accumulator = popValueFromStack(printWriter, inputStream, memory_reader, outputStream);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 29: // Int call. Set system mode, switch stack, push stackPointer and programCounter, set new stackPointer and programCounter
            
            interruptsDisabled = true;
            inUserMode = false;
            operand = stackPointer;
            stackPointer = 2000;
            pushValueToStack(printWriter, inputStream, outputStream, operand);
            
            operand = programCounter + 1;
            programCounter = 1500;
            pushValueToStack(printWriter, inputStream, outputStream, operand);
            
            if(interruptsDisabled == false) 
               instructionCount++;
            
            break;
             
         case 30: //Restore registers, set user mode
            
            programCounter = popValueFromStack(printWriter, inputStream, memory_reader, outputStream);
            stackPointer = popValueFromStack(printWriter, inputStream, memory_reader, outputStream);
            inUserMode = true;
            instructionCount++;
            interruptsDisabled = false;
            break;
             
         case 50: // End Execution
            if(interruptsDisabled == false) 
               instructionCount++;
            System.exit(0);
            break;
         
         default:
            System.out.println("Unknown error - default");
            System.exit(0);
            break;
      
      }
   }

   // function to check if user program if trying to access system memory and stack
   private static void checkMemoryViolation(int address) 
   {
      if(inUserMode && address >= 1000)
      {
         System.out.println("Error: User tried to access system stack. Process exiting.");
         System.exit(0);
      }
      
   }

   // function to handle interrupts caused by the timer
   private static void interruptFromTimer(PrintWriter printWriter, InputStream inputStream, Scanner memory_reader, OutputStream outputStream) 
   {
      int operand;
      inUserMode = false;
      operand = stackPointer;
      stackPointer = systemMemoryLimit;
      pushValueToStack(printWriter, inputStream, outputStream, operand);
   
      operand = programCounter;
      programCounter = 1000;
      pushValueToStack(printWriter, inputStream, outputStream, operand);
      
   }

   // function to push a value to the appropriate stack
   private static void pushValueToStack(PrintWriter printWriter, InputStream inputStream, OutputStream outputStream, int value) 
   {
      stackPointer--;
      writeToMemory(printWriter, inputStream, outputStream, stackPointer, value);
   }

   // function to pop a value from the appropriate stack
   private static int popValueFromStack(PrintWriter printWriter, InputStream inputStream, Scanner memory_reader, OutputStream outputStream) 
   {
      int temp = readFromMemory(printWriter, inputStream, memory_reader, outputStream, stackPointer);
      writeToMemory(printWriter, inputStream, outputStream, stackPointer, 0);
      stackPointer++;
      return temp;
   }
}

class MainMemory
{   
   final static int [] memory = new int[2000]; // int array to implement memory
   
   public static void main(String args[])
   {
      try
      {
         //Create a File object and a scanner to read it
         Scanner CPU_reader = new Scanner(System.in);
         File file = null;
         if(CPU_reader.hasNextLine())    // read file name from CPU
         {
            file = new File(CPU_reader.nextLine());
            
            if(!file.exists()) //exit if file not found
            {
               System.out.println("File not found");
               System.exit(0);
            }
         }
         
         // call function to read file and initialize memory array
         readFile(file);
      
         String line;
         int temp2;
         /*
             This while loop will keep on reading instructions from the CPU childProcessess
             and perform the read or write function appropriately
         */
         while(true)
         {
            if(CPU_reader.hasNext())
            {
               line = CPU_reader.nextLine(); //read the comma delimited line sent by the CPU
               if(!line.isEmpty())
               {
                  String [] j = line.split(","); //split the line to get the necessary tokens
                  
                  //  if first token is 1 then CPU is requesting to read 
                  //  from an address
                  if(j[0].equals("1"))    
                  {
                     temp2 = Integer.parseInt(j[1]);
                     System.out.println(read(temp2));// send requested data to CPU 
                  }
                  
                  //  else it will be 2, which means CPu is requesting to 
                  //  write data at a pacurrentRuntimeicular address
                  else
                  {
                     int i1 = Integer.parseInt(j[1]);
                     int i2 = Integer.parseInt(j[2]);
                     write(i1,i2); // invoke the write function
                  }
               }
               else 
                  break;
            }
            else
               break;
         }
         
      } catch(NumberFormatException e)
      {
         e.printStackTrace();
      }
   
   }
   
   // function to read the data in the address provided by the CPU
   public static int read(int address)
   {
      return memory[address];
   }
   
   // function to write data into an address based on intruction given by CPU
   public static void write(int address, int data)
   {
      memory[address] = data;
   }

   // function to read instructions from file and initialize memory
   private static void readFile(File file) {
      
      try 
      {
         Scanner scanner = new Scanner(file);
         String temp;
         int temp_int;
         int i = 0;
      
         /*
         *   Read the file to load memory instructions
         */
         while(scanner.hasNext())
         {
            //if integer found then write to memory array
            if(scanner.hasNextInt())
            {
               temp_int = scanner.nextInt();
               memory[i++] = temp_int;
            }
            else
            {
               temp = scanner.next();
               // if token stacurrentRuntimes with ".", then move the counter appropriately
               if(temp.charAt(0) == '.')
               {
                  i = Integer.parseInt(temp.substring(1));
               }
               
               // else if the token is a comment then skip the line
               else if(temp.equals("//"))
               {
                  scanner.nextLine();
               }
               
               // skip the line if anything else
               else
                  scanner.nextLine();
            }
         }
      } 
      catch (FileNotFoundException ex) 
      {
         ex.printStackTrace();
      }
      
   }

}