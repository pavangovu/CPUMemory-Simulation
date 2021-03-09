import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;
public class Project1
{
public static void main(String[] args)
{
if(args.length < 2)
{
System.err.println("Not enough arguments (requires input program and timer length)");
System.exit(0);
}
