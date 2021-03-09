
import java.io.*;
import java.lang.Runtime;

public class Proc 
{
   public static void main(String args[])
   {
      try
      {            
	 int x;
	 Runtime rt = Runtime.getRuntime();

	 Process proc = rt.exec("java Hello");
	 //Process proc = rt.exec("cat hello.java");

	 InputStream is = proc.getInputStream();
	 OutputStream os = proc.getOutputStream();

	 while ((x=is.read()) != -1)
	    System.out.println((char)x); 
	      
	 proc.waitFor();

         int exitVal = proc.exitValue();

         System.out.println("Process exited: " + exitVal);

      } 
      catch (Throwable t)
      {
	 t.printStackTrace();
      }
   }
}

