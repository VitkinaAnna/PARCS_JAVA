import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import parcs.AM;
import parcs.AMInfo;
import parcs.point;
import parcs.channel;

public class Solver implements AM {
    private String input_file_name;
    private String output_file_name;
    private List<point> workers;

    public Solver(List<point> workers, String input_file_name, String output_file_name) {
        this.input_file_name = input_file_name;
        this.output_file_name = output_file_name;
        this.workers = workers;

        System.out.println("Inited");
    }

    public void solve(AMInfo info) {
        System.out.println("Job Started");
        System.out.println("Workers " + workers.size());

        List<List<Integer>> a = readInput();
        int num_elements = a.size();
        int part_size = num_elements / workers.size();
        int extra_elements = num_elements % workers.size();

        List<Integer> part_sizes = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            int size = part_size + (i < extra_elements ? 1 : 0);
            part_sizes.add(size);
        }

        List<List<List<Integer>>> divided_parts = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < workers.size(); i++) {
            int size = part_sizes.get(i);
            divided_parts.add(a.subList(start, start + size));
            start += size;
        }

        List<channel> channels = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            point p = info.createPoint();
            channel c = p.createChannel();
            p.execute("Bluck");

            c.write(divided_parts.get(i));
            channels.add(c);
        }

        List<List<List<Integer>>> points = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            points.add(channels.get(i).readObject());
        }

        List<List<Integer>> output = new ArrayList<>();
        for (List<List<Integer>> sublist : points) {
            for (List<Integer> item : sublist) {
                output.add(item);
            }
        }

        List<List<Integer>> result = printHull(output);

        writeOutput(result);

        info.parent().end();
    }

    public static List<List<Integer>> readInput(String filename) throws IOException {
        List<List<Integer>> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                List<Integer> point = new ArrayList<>();
                point.add(Integer.parseInt(parts[0]));
                point.add(Integer.parseInt(parts[1]));
                points.add(point);
            }
        }
        return points;
    }

    public static void writeOutput(List<List<Integer>> output, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (List<Integer> point : output) {
                writer.write(point.get(0) + ", " + point.get(1) + "\n");
            }
        }
        System.out.println("Output done");
    }

    public static List<List<Integer>> printHull(List<List<Integer>> a) {
        Set<List<Integer>> hull = new HashSet<>();
        int n = a.size();

        if (n < 3) {
            System.out.println("Convex hull not possible");
            return null;
        }

        int min_x = 0;
        int max_x = 0;
        for (int i = 1; i < n; i++) {
            if (a.get(i).get(0) < a.get(min_x).get(0)) {
                min_x = i;
            }
            if (a.get(i).get(0) > a.get(max_x).get(0)) {
                max_x = i;
            }
        }

        quickHull(a, n, a.get(min_x), a.get(max_x), 1, hull);
        quickHull(a, n, a.get(min_x), a.get(max_x), -1, hull);

        return new ArrayList<>(hull);
    }

    public static void quickHull(List<List<Integer>> a, int n, List<Integer> p1, List<Integer> p2, int side, Set<List<Integer>> hull) {
        int ind = -1;
        int max_dist = 0;

        for (int i = 0; i < n; i++) {
            int temp = lineDist(p1, p2, a.get(i));

            if (findSide(p1, p2, a.get(i)) == side && temp > max_dist) {
                ind = i;
                max_dist = temp;
            }
        }

        if (ind == -1) {
            hull.add(p1);
            hull.add(p2);
            return;
        }

        quickHull(a, n, a.get(ind), p1, -findSide(a.get(ind), p1, p2), hull);
        quickHull(a, n, a.get(ind), p2, -findSide(a.get(ind), p2, p1), hull);
    }

    public static int findSide(List<Integer> p1, List<Integer> p2, List<Integer> p) {
        int val = (p.get(1) - p1.get(1)) * (p2.get(0) - p1.get(0)) - (p2.get(1) - p1.get(1)) * (p.get(0) - p1.get(0));
        if (val > 0) {
            return 1;
        }
        if (val < 0) {
            return -1;
        }
        return 0;
    }

    public static int lineDist(List<Integer> p1, List<Integer> p2, List<Integer> p) {
        return Math.abs((p.get(1) - p1.get(1)) * (p2.get(0) - p1.get(0)) - (p2.get(1) - p1.get(1)) * (p.get(0) - p1.get(0)));
    }
}
