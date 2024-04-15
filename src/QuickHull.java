import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.math.BigInteger;
import parcs.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class QuickHull implements AM {
    private static long startTime = 0;
    private static final BigInteger MODULE = new BigInteger("2147483647");
    private static final BigInteger BASE =  new BigInteger("31");
        public static int findSide(ArrayList<Integer> p1, ArrayList<Integer> p2, ArrayList<Integer> p) {
        int val = (p.get(1) - p1.get(1)) * (p2.get(0) - p1.get(0)) - (p2.get(1) - p1.get(1)) * (p.get(0) - p1.get(0));

        if (val > 0) {
            return 1;
        } else if (val < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public static int lineDist(ArrayList<Integer> p1, ArrayList<Integer> p2, ArrayList<Integer> p) {
        return Math.abs((p.get(1) - p1.get(1)) * (p2.get(0) - p1.get(0)) - (p2.get(1) - p1.get(1)) * (p.get(0) - p1.get(0)));
    }

    public static void quickHull(ArrayList<ArrayList<Integer>> points, ArrayList<Integer> p1, ArrayList<Integer> p2, int side, Set<String> hull) {
        int ind = -1;
        int max_dist = 0;

        for (int i = 0; i < points.size(); i++) {
            ArrayList<Integer> p = points.get(i);
            int temp = lineDist(p1, p2, p);

            if ((findSide(p1, p2, p) == side) && (temp > max_dist)) {
                ind = i;
                max_dist = temp;
            }
        }

        if (ind == -1) {
            hull.add(p1.get(0) + "$" + p1.get(1));
            hull.add(p2.get(0) + "$" + p2.get(1));
            return;
        }

        quickHull(points, points.get(ind), p1, -findSide(points.get(ind), p1, p2), hull);
        quickHull(points, points.get(ind), p2, -findSide(points.get(ind), p2, p1), hull);
    }

    public static ArrayList<ArrayList<Integer>> printHull(ArrayList<ArrayList<Integer>> points) {
        Set<String> hull = new HashSet<>();
        ArrayList<ArrayList<Integer>> hullPoints = new ArrayList<>();

        if (points.size() < 3) {
            System.out.println("Convex hull not possible");
            return hullPoints;
        }

        ArrayList<Integer> min_x = points.get(0);
        ArrayList<Integer> max_x = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            ArrayList<Integer> p = points.get(i);
            if (p.get(0) < min_x.get(0)) {
                min_x = p;
            }
            if (p.get(0) > max_x.get(0)) {
                max_x = p;
            }
        }

        quickHull(points, min_x, max_x, 1, hull);
        quickHull(points, min_x, max_x, -1, hull);

        for (String element : hull) {
            String[] parts = element.split("\\$");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            ArrayList<Integer> hullPoint = new ArrayList<>();
            hullPoint.add(x);
            hullPoint.add(y);
            hullPoints.add(hullPoint);
        }

        return hullPoints;
    }
	 
	public static ArrayList<ArrayList<Integer>> computeHash(String str) {
		 ArrayList<ArrayList<Integer>> points = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(0, 0)),
                new ArrayList<>(Arrays.asList(0, 0))
       		 ));
		 BigInteger hashValue = BigInteger.ZERO;
	     BigInteger powBase = BigInteger.ONE;

	     for (int i = 0; i < str.length(); i++) {
	    	 char ch = str.charAt(i);
	         BigInteger charValue = BigInteger.valueOf(ch - 'a' + 1);
	         hashValue = (hashValue.add(charValue.multiply(powBase).mod(MODULE))).mod(MODULE);
	         powBase = powBase.multiply(BASE).mod(MODULE);
	        }
	        return points;
	 }


    public static void main(String[] args) throws Exception {
    	
    	
        System.err.println("Preparing...");
        
        if (args.length != 1) {
            System.err.println("Nnumber of workers not specified");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);

        task curtask = new task();
        curtask.addJarFile("QuickHull.jar");
        AMInfo info = new AMInfo(curtask, null);

        System.err.println("Reading input...");
       
        
	String S = "";
	try{
		Scanner sc = new Scanner(new File(info.curtask.findFile("input.txt")));
		S = sc.nextLine();
	}
	catch (IOException e) {e.printStackTrace(); return;}
        
        int len = S.length();
        int sub_len = (len + n - 1) / n;

        System.err.println("Forwarding parts to workers...");
       startTime = System.nanoTime();
        channel[] channels = new channel[n];
        for (int i = 0; i < n; i++) {
            String substring = "";
	    if (i * sub_len < S.length()) {
		substring = S.substring(i * sub_len, 
            		Math.min((i * sub_len + sub_len), S.length()));
		}
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("QuickHull");
            c.write(substring);
            channels[i] = c;
        }

        System.err.println("Getting results");
        ArrayList<ArrayList<Integer>>[] sub_hash = new ArrayList[n];

        for (int i = 0; i < n; i++) {
        	sub_hash[i] = (ArrayList<ArrayList<Integer>>) channels[i].readObject();
		// Printing the initialized 2x2 ArrayList
        	for (ArrayList<Integer> row : sub_hash[i]) {
        	    System.err.println(row);
     		   }
        }

        System.err.println("Calculation of the result");
     
        //BigInteger hash = resultСalculation(sub_hash, sub_len);
       
 	long endTime = System.nanoTime();
	
        //System.out.println("Result: " + hash.toString());
       
        
        long timeElapsed = endTime - startTime;
        double seconds = timeElapsed / 1_000_000_000.0;
        System.err.println("Time passed: " + seconds + " seconds.");
        
        
        curtask.end();
    }


    public void run(AMInfo info) {
     
        String substring = (String)info.parent.readObject();
         ArrayList<ArrayList<Integer>> subhash = computeHash(substring);

        info.parent.write(subhash);
  
    }

    public static BigInteger resultСalculation(BigInteger[] subhash, int sublen) {
        
        BigInteger delt = BigInteger.ONE;
        BigInteger step = BASE.modPow(BigInteger.valueOf(sublen), MODULE);
        BigInteger output = BigInteger.ZERO;
        
        for (BigInteger x : subhash) {
	     System.out.println(x.toString());
            output = output.add(x.multiply(delt).mod(MODULE)).mod(MODULE);
            delt = delt.multiply(step).mod(MODULE);
        }
       return output;
    }
}
