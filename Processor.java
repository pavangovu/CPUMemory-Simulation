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
   private static int alarm=0;                           //code for interrupt timer                                                             
   
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
               
      //filename and timer interrupt code arguments required
      if(args.length <2)
      {
         System.out.println("Insufficient Arguments! Please include filename and timer-interrupt code.");
         System.exit(0);//kill the program
      }
      
      //parse command line arguments
      inputFileName=args[0];
      alarm=Integer.parseInt(args[1]);//String to int conversion
              
      Runtime currentRuntime = Runtime.getRuntime();
      
         //equivalent of UNIX fork command
      Process childProcess = currentRuntime.exec("java MainMemory");
      
         //set up communication between Main Memory and CPU
      InputStream inputStream = childProcess.getInputStream();
      Scanner fetchFromMemory = new Scanner(inputStream);
      OutputStream outputStream = childProcess.getOutputStream();
      PrintWriter printWriter = new PrintWriter(outputStream);
      
      
      printWriter.printf(inputFileName + "\n");
      printWriter.flush();
         
      boolean systemOn=true;
         
      while (systemOn)
      {
         if(instructionCount > 0)
            if((instructionCount % alarm) == 0)
               if(interruptsDisabled == false)
               {
                  interruptsDisabled = true;
                  int operand;
                  inUserMode = false;
                  operand = stackPointer;
                  stackPointer = systemMemoryLimit;
                  stackPointer--;
                  storeValue(printWriter, inputStream, outputStream, stackPointer, operand);
                  
                  operand = programCounter;
                  programCounter = 1000;
                  stackPointer--;
                  storeValue(printWriter, inputStream, outputStream, stackPointer, operand);
               }
            
         int code = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
         if(inUserMode && programCounter >= 1000)
         {
            System.out.println("Memory violation: accessing system address 1000 in user mode ");
            System.exit(0);
         }
         printWriter.printf("1," + programCounter + "\n");
         printWriter.flush();
         if (fetchFromMemory.hasNext())
         {
            String current = fetchFromMemory.next();
            if(!current.isEmpty())
            {
               int next = Integer.parseInt(current);
               code=next; 
            }
            
         }
         else
            code=-1;
            
         if (code != -1)
         {
            instructionSet(code, printWriter, inputStream, fetchFromMemory, outputStream);
         }
         else
            break;
      }
   }

   private static int readFromMemory(PrintWriter printWriter, InputStream inputStream, Scanner fetchFromMemory, OutputStream outputStream, int address) 
   {
      if(inUserMode && address >= 1000)
      {
         System.out.println("Memory violation: accessing system address 1000 in user mode ");
         System.exit(0);
      }
      printWriter.printf("1," + address + "\n");
      printWriter.flush();
      if (fetchFromMemory.hasNext())
      {
         String current = fetchFromMemory.next();
         if(!current.isEmpty())
         {
            int next = Integer.parseInt(current);
            return (next); 
         }
      
      }
      return -1;
   }
   
   //function to tell child childProcessess to write data at the given address in registers
   private static void storeValue(PrintWriter printWriter, InputStream inputStream, OutputStream outputStream, int address, int data) {
      printWriter.printf("2," + address + "," + data + "\n"); //2 at the stacurrentRuntime on string indicates write
      printWriter.flush();
   }

   // function to childProcessess an instruction received from the registers
   private static void instructionSet(int code, PrintWriter printWriter, InputStream inputStream, Scanner fetchFromMemory, OutputStream outputStream) 
   {
      instructionRegister = code; //store instruction in Instruction register
      int operand;    //to store operand
      
      switch(instructionRegister)
      {
         case 1: //Load the code into the accumulator
            programCounter++; // increment counter to get operand
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 2: // Load the code at the address into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
      
         case 3: // Load the code from the address found in the address into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
             
         case 4: // Load the code at (address+xRegister) into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 5: //Load the code at (address+yRegister) into the accumulator
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand + yRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 6: //Load from (Sp+xRegister) into the accumulator
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, stackPointer + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 7: //Store the code in the accumulator into the address
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            storeValue(printWriter, inputStream, outputStream, operand, accumulator);
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
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
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
            
         case 10: // Add the code in xRegister to the accumulator
            accumulator = accumulator + xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 11: //Add the code in yRegister to the accumulator
            accumulator = accumulator + yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 12: //Subtract the code in xRegister from the accumulator
            accumulator = accumulator - xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         case 13: //Subtract the code in yRegister from the accumulator
            accumulator = accumulator - yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 14: //Copy the code in the accumulator to xRegister
            xRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 15: //Copy the code in xRegister to the accumulator
            accumulator = xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 16: //Copy the code in the accumulator to yRegister
            yRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
             
         case 17: //Copy the code in yRegister to the accumulator
            accumulator = yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 18: //Copy the code in accumulator to the stackPointer
            stackPointer = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 19: //Copy the code in stackPointer to the accumulator 
            accumulator = stackPointer;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         case 20: // Jump to the address
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 21: // Jump to the address only if the code in the accumulator is zero
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
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
             
             
         case 22: // Jump to the address only if the code in the accumulator is not zero
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
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
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, programCounter+1);
            userMemoryLimit = stackPointer;
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
             
         case 24: //Pop return address from the stack, jump to the address
            operand = popValueFromStack(printWriter, inputStream, fetchFromMemory, outputStream);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 25: //Increment the code in xRegister
            xRegister++;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         case 26: //Decrement the code in xRegister
            xRegister--;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         case 27: // Push accumulator onto stack
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, accumulator);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 28: //Pop from stack into accumulator
            accumulator = popValueFromStack(printWriter, inputStream, fetchFromMemory, outputStream);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         case 29: // Int call. Set system mode, switch stack, push stackPointer and programCounter, set new stackPointer and programCounter
            
            interruptsDisabled = true;
            inUserMode = false;
            operand = stackPointer;
            stackPointer = 2000;
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, operand);
            
            operand = programCounter + 1;
            programCounter = 1500;
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, operand);
            
            if(interruptsDisabled == false) 
               instructionCount++;
            
            break;
             
         case 30: //Restore registers, set user mode
            
            programCounter = popValueFromStack(printWriter, inputStream, fetchFromMemory, outputStream);
            stackPointer = popValueFromStack(printWriter, inputStream, fetchFromMemory, outputStream);
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
            System.out.println("Invalid instruction encountered! Please check your input file and make sure your input file is correct.");
            System.exit(0);
            break;
      
      }
   }

   // function to pop a code from the appropriate stack
   private static int popValueFromStack(PrintWriter printWriter, InputStream inputStream, Scanner fetchFromMemory, OutputStream outputStream) 
   {
      int current = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, stackPointer);
      storeValue(printWriter, inputStream, outputStream, stackPointer, 0);
      stackPointer++;
      return current;
   }
}

class MainMemory
{   
   private static int [] registers = new int[2000]; // int array to implement registers
   
   public static void main(String args[])throws Exception
   {
      Scanner fetchFromCPU = new Scanner(System.in);  
      File file = new File(fetchFromCPU.nextLine());
      
      
      String current;
      int next;
      
      int registerPosition = 0;
      Scanner scanner = new Scanner(file);
     
      while(scanner.hasNext())
      {
            //if integer found then write to registers array
         if(scanner.hasNextInt())
         {
            next = scanner.nextInt();
            registers[registerPosition++] = next;
         }
         else
         {
            current = scanner.next();
               // if token stacurrentRuntimes with ".", then move the counter appropriately
            if(current.charAt(0) == '.')
            {
               registerPosition = Integer.parseInt(current.substring(1));
            }
               
               // else if the token is a comment then skip the input
            else if(current.equals("//"))
            {
               scanner.nextLine();
            }
               
               // skip the input if anything else
            else
               scanner.nextLine();
         }
      }
        
      String input;
      int position;
         
      boolean keepGoing=true;
      while(keepGoing)
      {
         if(fetchFromCPU.hasNext())
         {
            input = fetchFromCPU.nextLine(); 
            if(!input.isEmpty())
            {
               String [] split = input.split(",");                      
               if(split[0].equals("1"))    
               {
                  position = Integer.parseInt(split[1]);
                  System.out.println(registers[position]);
               }   
               else
                  registers[Integer.parseInt(split[1])]=Integer.parseInt(split[2]);
               
            }
            else 
               break;
         }
         else
            break;
      }
   }
}