/**DNAApp
 *
 * Description: DNAApp is a app that will search useful informations in sets
 * of DNA sequences from a file, and print it into a file assigned by the user.
 *
 * Date: 10/07/2018
 * Lastest debug Date: 10/14/2018
 * @author: Joseph Chang
 */

import java.io.*;
import java.util.Scanner;

public class DNAApp
{
    private Scanner fileScanner;
    private String DNA;
    private int headerLength;
    private int resetSubHead;
    //The Coding region
    public final String startCode = "ATG";
    public final String endCode1 = "TAA";
    public final String endCode2 = "TGA";
    public final String endCode3 = "TAG";

    //readFile read the file and the data in it
    //readFile will call readFileIntoString to pass the data into String DNA
    public boolean readFile(String path)
    {
        try
        {
            fileScanner = new Scanner( new File(path));
            readFileIntoString();
        }
        catch(FileNotFoundException fnfe)
        {
            System.err.println("File not found!");
            return false;
        }
        return true;
    }

    //readFileIntoString will pass the data from fileScanner into String DNA
    private void readFileIntoString()
    {
        DNA = "";
        while (fileScanner.hasNextLine())
        {
            DNA += fileScanner.nextLine();
        }
    }

    //Because every file can have different format of header
    //lengthOfHeader assume the header always start at ">"
    //It let the user enter the last four char of the first header
    //Then it will find the length of the header and pass the length into headerLength
    public int lengthOfHeader(String lastFourChar)
    {
        int start = 0;
        while (DNA.charAt(start) != '>')
        {
            start++;
        }
        int end = start;
        while (!DNA.substring(end,end+4).equals(lastFourChar))
        {
            end++;
        }
        end += 3;

        headerLength = end-start+1;
        return headerLength;
    }

    //printString method print the string from the first index to the last index
    public void printString(int first, int last)
    {
        if (last > DNA.length()-1)
            System.err.println("Last out of range!");
        else if(first > last)
            System.err.println("First should be lower than last");
        else
            System.out.println(DNA.substring(first,last));

    }

    //searchHeader will search the next header from the index start base on ">"
    public int searchHeader(int start)
    {
        if (start > (DNA.length()-1))
        {
            System.err.println("Index Start out of range");
            return -1;
        }
        else
        {
            for(int i = start; i < DNA.length(); i++)
            {
                if (DNA.charAt(i) == '>')
                    return i;
            }
            //return -2 means there is not next header
            return -2;
        }
    }

    //searchCodingRegion will search the feature DNA sequence from the index first
    //until the next header or out of range. It will return the feature DNA it find
    //as a string
    public String searchCodingRegion(int start)
    {

        if (start > (DNA.length()-3))
        {
            System.err.println("Index Start out of range");
            return "";
        }
        else
        {
            int firstIndex = start;
            while (firstIndex < DNA.length()-6 && DNA.charAt(firstIndex+3) != '>' && !DNA.substring(firstIndex,firstIndex+3).equals(startCode))
            {
                firstIndex++;
            }

            if(!(firstIndex < DNA.length()-6))
            {
                return "End of search in this section";
            }
            else if (DNA.charAt(firstIndex+3) == '>')
            {
                return "End of search in this section";
            }

            if (DNA.substring(firstIndex,firstIndex+3).equals(startCode))
            {
                int lastIndex = firstIndex + 3;
                while (lastIndex < DNA.length()-3 && DNA.charAt(lastIndex) != '>'
                        && DNA.charAt(lastIndex+1) != '>' && DNA.charAt(lastIndex+2) != '>'
                        && !DNA.substring(lastIndex,lastIndex+3).equals(endCode1)
                        && !DNA.substring(lastIndex,lastIndex+3).equals(endCode2)
                        && !DNA.substring(lastIndex,lastIndex+3).equals(endCode3))
                {
                    lastIndex+=3;
                }

                if(lastIndex > DNA.length()-3 || DNA.charAt(lastIndex) == '>'
                        || DNA.charAt(lastIndex+1) == '>' || DNA.charAt(lastIndex+2) == '>')
                {
                    resetSubHead = lastIndex;
                    return "End of search in this section";
                }

                //lastIndex += 3;
                resetSubHead = lastIndex;
                return DNA.substring(firstIndex,lastIndex+3);

            }
            else
            {
                return "End of search in this section";
            }

        }
    }

    //changeCharToA will change any char which is not A, T, C, or G to A between headers
    //It uses search header to locate header
    //This must be called after headerLen is known
    public void changeCharToA()
    {
        int head = searchHeader(0); //head is a index move inside DNA to locate header
        while (head != -2) //-2 means there is not next header
        {
            int start = head + headerLength;
            int end = searchHeader(head + headerLength); //find next header
            if (end == -2)
                end = DNA.length();

            for (int i = start; i < end; i++)
            {
                switch(DNA.charAt(i))
                {
                    case 'A':
                    case 'T':
                    case 'C':
                    case 'G':
                        break;
                    default:
                        int len = DNA.length();
                        DNA = DNA.substring(0,i)+'A'+DNA.substring(i+1,len);
                }
            }

            if (end == DNA.length()) //set the head to the next header (or -2 to shutdown the loop)
                head = -2;
            else
                head = end;
        }
    }

    //searchAndPrint
    //This is the core of the class, most thing will be control here.
    //It will search the header and print it, and it will search the coding region and print it.
    //Make sure this is call after readFile, lengthOfHeader, and changeCharToA
    public void searchAndPrint()
    {
        int head = searchHeader(0); //head is a index move inside DNA to locate header
        while (head != -2) //-2 means there is not next header
        {
            printString(head,head+headerLength);

            int subHead = head+headerLength; //subHead is a head move inside a section
            String codingRegion = searchCodingRegion(subHead);
            while (!codingRegion.equals("End of search in this section"))
            {
                System.out.println(codingRegion);
                subHead = resetSubHead;
                codingRegion = searchCodingRegion(subHead);
            }
            System.out.println();

            head = searchHeader(subHead); //find next header
        }
    }

    //searchAndPrint(PrintWriter) is a overload of searchAndPrint
    //It will take a PrintWritter as a parameter.
    //It will then print the info in the file rather than print in the terminal.
    public void searchAndPrint(PrintWriter file)
    {
        int head = searchHeader(0); //head is a index move inside DNA to locate header
        while (head != -2) //-2 means there is not next header
        {
            file.println(DNA.substring(head,head+headerLength));

            int subHead = head+headerLength; //subHead is a head move inside a section
            String codingRegion = searchCodingRegion(subHead);
            while (!codingRegion.equals("End of search in this section"))
            {
                file.println(codingRegion);
                subHead = resetSubHead;
                codingRegion = searchCodingRegion(subHead);
            }
            file.println();

            head = searchHeader(subHead); //find next header
        }
        file.close();
    }

    public int getDNALength()
    {
        return DNA.length();
    }


    public static void main(String[] args)
    {
        DNAApp testApp = new DNAApp();
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Please enter file name:");
        String fileName = keyboard.nextLine();

        if(testApp.readFile(fileName))
        {
            testApp.lengthOfHeader("2004"); //Give the last four char of the first
                                                       // header to let the App to get the length of the header
            testApp.changeCharToA();
            testApp.searchAndPrint();
            //Not finish yet!
            System.out.println("\nEnter the name of the file you want to store in:");
            fileName = keyboard.nextLine();

            try
            {
                PrintWriter outPutFile = new PrintWriter(fileName);
                testApp.searchAndPrint(outPutFile);
                System.out.println("Done!");
            }
            catch (FileNotFoundException fnfe)
            {
                System.err.println("Cannot file the output file!");
            }


        }
        else
            System.out.println("Cannot find the file.");

    }
}
