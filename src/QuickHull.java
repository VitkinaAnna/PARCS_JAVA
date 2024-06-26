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
       
        
	ArrayList<ArrayList<Integer>> points = new ArrayList<>();

	try {
	    Scanner sc = new Scanner(new File(info.curtask.findFile("input.txt")));
	    while (sc.hasNextLine()) {
	        String line = sc.nextLine();
	        String[] coordinates = line.split(",");
	        ArrayList<Integer> point = new ArrayList<>();
	        for (String coord : coordinates) {
	            point.add(Integer.parseInt(coord.trim()));
	        }
	        points.add(point);
	    }
	    sc.close(); 
	} catch (IOException e) {
	    e.printStackTrace();
	    return;
	}
	

        System.err.println("Forwarding parts to workers...");
	    int numPoints = points.size();
	    int pointsPerWorker = numPoints / n;
	    int extraPoints = numPoints % n;
	    
       startTime = System.nanoTime();
        channel[] channels = new channel[n];
        for (int i = 0; i < n; i++) {
	    ArrayList<ArrayList<Integer>> partPoints = new ArrayList<>();
	    int start = i * pointsPerWorker + Math.min(i, extraPoints);
	    int end = start + pointsPerWorker + (i < extraPoints ? 1 : 0);
	    partPoints.addAll(points.subList(start, end));
	            
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("QuickHull");
            c.write(partPoints);
            channels[i] = c;
        }

        System.err.println("Getting results");
        ArrayList<ArrayList<Integer>>[] hulls = new ArrayList[n];

        for (int i = 0; i < n; i++) {
        	hulls[i] = (ArrayList<ArrayList<Integer>>) channels[i].readObject();
        }

        System.err.println("Calculation of the result");
     
        HashSet<ArrayList<Integer>> uniquePointsSet = new HashSet<>();

	for (int i = 0; i < n; i++) {
	    for (ArrayList<Integer> point : hulls[i]) {
	        uniquePointsSet.add(point);
	    }
	}
	ArrayList<ArrayList<Integer>> points_combined = new ArrayList<>(uniquePointsSet);
	// Compute convex hull on combined points
        ArrayList<ArrayList<Integer>> finalHull = printHull(points_combined);

        // Print combined convex hull
        System.out.println("Combined Convex Hull:");
        for (ArrayList<Integer> point : finalHull) {
            System.err.println("(" + point.get(0) + ", " + point.get(1) + ")");
        }
       
 	long endTime = System.nanoTime();
	
        //System.out.println("Result: " + hash.toString());
       
        
        long timeElapsed = endTime - startTime;
        double seconds = timeElapsed / 1_000_000_000.0;
        System.err.println("Time passed: " + seconds + " seconds.");
        
        
        curtask.end();
    }


    public void run(AMInfo info) {
     
         ArrayList<ArrayList<Integer>> substring = (ArrayList<ArrayList<Integer>>)info.parent.readObject();
         ArrayList<ArrayList<Integer>> subhash = printHull(substring);

        info.parent.write(subhash);
  
    }
}
