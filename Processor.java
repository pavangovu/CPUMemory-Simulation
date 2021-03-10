/*
   Pavan Kumar Govu
      Prof. Ozbin
      CS 4348.001
      09 March 2021
   */

import java.util.*;
import java.io.*;

//Processor class simulates the CPU
class MainMemory
{   
   private static int [] registers = new int[2000];//allocated registers
   
   public static void main(String args[])throws Exception
   {
      Scanner fetchFromCPU = new Scanner(System.in);//communicate with CPU  
      File file = new File(fetchFromCPU.nextLine());  
      
      String current;//if input is string
      int next;//if input is int
      
      int registerPosition = 0;//similar to stack pointer
      Scanner scanner = new Scanner(file);
     
      while(scanner.hasNext())
      {
         if(scanner.hasNextInt())//if the next input is an integer
         {
            next = scanner.nextInt();
            registers[registerPosition++] = next;
         }
         else//if the next input is a string
         {
            current = scanner.next();
            if(current.charAt(0) == '.')
            {
               String tempBuilder="";
               for(int i=1; i<current.length(); i++)
               {
                  tempBuilder=tempBuilder+current.charAt(i);
               }  
               registerPosition = Integer.parseInt(tempBuilder);
            }
               
            else if(current.equals("//"))
            {
               scanner.nextLine();
            }
            
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
                  System.out.println(registers[Integer.parseInt(split[1])]); 
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

//Processor class simulates the CPU
public class Processor 
{
   //System Configuration
   private static int instructionCount=0;                //total number of instructions
   private static boolean inUserMode = true;             //whether or not system is in user mode
   private static boolean interruptsDisabled = false;    //Interrupts should be disabled during interrupt childProcessessing to avoid nested execution
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
                  stackPointer = 2000;
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
   
   
   private static void storeValue(PrintWriter printWriter, InputStream inputStream, OutputStream outputStream, int address, int data) {
      printWriter.printf("2," + address + "," + data + "\n");
      printWriter.flush();
   }

   
   private static void instructionSet(int code, PrintWriter printWriter, InputStream inputStream, Scanner fetchFromMemory, OutputStream outputStream) 
   {
      instructionRegister = code;
      int operand;    
      
      switch(instructionRegister)
      {
         //Load value
         case 1:  
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, ++programCounter);
            accumulator = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Load addr 
         case 2:
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
      
         //LoadInd addr
         case 3:
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //LoadIdxX addr   
         case 4:
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //LoadIdxY addr    
         case 5: 
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, operand + yRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //LoadSpX    
         case 6: 
            accumulator = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, stackPointer + xRegister);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Store addr    
         case 7: 
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            storeValue(printWriter, inputStream, outputStream, operand, accumulator);
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Get    
         case 8:
            Random r = new Random();
            int i = r.nextInt(100) + 1;
            accumulator = i;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Put port    
         case 9: 
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
         
         //AddX   
         case 10: 
            accumulator = accumulator + xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //AddY    
         case 11:
            accumulator = accumulator + yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //SubX    
         case 12:
            accumulator = accumulator - xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
            
         //SubY
         case 13:
            accumulator = accumulator - yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //CopyToX    
         case 14:
            xRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //CopyFromX    
         case 15: 
            accumulator = xRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //CopyToY    
         case 16:
            yRegister = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
             
         //CopyFromY    
         case 17:
            accumulator = yRegister;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //CopyToSp    
         case 18:
            stackPointer = accumulator;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //CopyFromSp       
         case 19:
            accumulator = stackPointer;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Jump addr    
         case 20:
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
         
         //JumpIfEqual addr    
         case 21:
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
             
         //JumpIfNotEqual    
         case 22:
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
         
         //Call addr    
         case 23:
            programCounter++;
            operand = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, programCounter);
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, programCounter+1);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
             
         //Ret     
         case 24:
            operand = pop(printWriter, inputStream, fetchFromMemory, outputStream);
            programCounter = operand;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
         
         //IncX     
         case 25: 
            xRegister++;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //DecX 
         case 26: 
            xRegister--;
            if(interruptsDisabled == false) 
               instructionCount++;
            programCounter++;
            break;
         
         //Push
         case 27:
            stackPointer--;
            storeValue(printWriter, inputStream, outputStream, stackPointer, accumulator);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
         
         //Pop    
         case 28:
            accumulator = pop(printWriter, inputStream, fetchFromMemory, outputStream);
            programCounter++;
            if(interruptsDisabled == false) 
               instructionCount++;
            break;
         
         //Int    
         case 29:
            
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
         
         //IRet    
         case 30:
            
            programCounter = pop(printWriter, inputStream, fetchFromMemory, outputStream);
            stackPointer = pop(printWriter, inputStream, fetchFromMemory, outputStream);
            inUserMode = true;
            instructionCount++;
            interruptsDisabled = false;
            break;
         
         //End    
         case 50: 
            if(interruptsDisabled == false) 
               instructionCount++;
            System.exit(0);
            break;
        
        //Invalid Instruction 
         default:
            System.out.println("Invalid instruction encountered! Please check your input file and make sure your input file is correct.");
            System.exit(0);
            break;
      }
   }

   private static int pop(PrintWriter printWriter, InputStream inputStream, Scanner fetchFromMemory, OutputStream outputStream) 
   {
      int current = readFromMemory(printWriter, inputStream, fetchFromMemory, outputStream, stackPointer);
      storeValue(printWriter, inputStream, outputStream, stackPointer, 0);
      stackPointer++;
      return current;
   }
}