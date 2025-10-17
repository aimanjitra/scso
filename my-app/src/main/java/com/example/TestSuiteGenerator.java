package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import com.example.rozmie.TWayUtil2D;
import com.example.rozmie.TestCase;

public class TestSuiteGenerator {
    public static double[] c = new double[360];
    public static final int MAX_ITER = 100;
    public static final double S = 20.0;
    public static final int numberOfPopulation = 10;
    public static final int MAX_BATCHES = 5;
    
    private int[] value;
    private int strength;
    
    public TestSuiteGenerator(int[] value, int strength) {
        this.value = value;
        this.strength = strength;
        initializeRoulleteWheel();
    }
    
    public String generateTestSuite() {
    StringBuilder testCasesOutput = new StringBuilder();

    TWayUtil2D tt = new TWayUtil2D(value);
    boolean[] pInvolve = new boolean[value.length];
    for (int i = 0; i < value.length; i++) pInvolve[i] = true;
    tt.addSetting(pInvolve, strength);

    int count = 1;
    while (!tt.allTuplesCovered()) {
        int t = 0;
        ArrayList<TestCase> pop = tt.createRandomPopulation(numberOfPopulation);
        TestCase.setDescendingOrder();
        Collections.sort(pop);

        while (pop.get(0).fitness == 0) {
            pop = tt.createRandomPopulation(numberOfPopulation);
            Collections.sort(pop);
        }

        TestCase bestTC = pop.get(0).clone();
        while (t < MAX_ITER) {
            double rg = S - ((S) * t / (MAX_ITER));
            double r = (new Random()).nextDouble() * rg;
            Iterator<TestCase> itr = pop.iterator();
            int bestX = bestTC.getX();
            int bestY = bestTC.getY();

            while (itr.hasNext()) {
                TestCase temp = itr.next();
                int currentX = temp.getX();
                int currentY = temp.getY();
                double R = ((2 * rg) * (new Random()).nextDouble()) - rg;
                int newX, newY;

                if ((R >= -1) && (R <= 1)) {
                    int theta = getTheta();
                    double randX = Math.abs(((new Random()).nextDouble() * bestX) - currentX);
                    double randY = Math.abs(((new Random()).nextDouble() * bestY) - currentY);
                    newX = (int) (bestX - (r * randX) * Math.cos(2 * Math.PI * theta));
                    newY = (int) (bestY - (r * randY) * Math.sin(2 * Math.PI * theta)); // Fixed cos → sin
                } else {
                    newX = (int) (r * (bestX - (new Random()).nextDouble() * currentX));
                    newY = (int) (r * (bestY - (new Random()).nextDouble() * currentY));
                }
                tt.updateTestCase(temp, newX, newY);
            }

            Collections.sort(pop);
            TestCase bestCandidate = pop.get(0);
            if (bestCandidate.fitness > bestTC.fitness)
                tt.updateTestCase(bestTC, bestCandidate.getPoint());
            t++;
        }

        // Store each test case output in a separate buffer
        testCasesOutput.append(count++)
                .append(") ")
                .append(getTestCaseAsString(bestTC.getTestCase()))
                .append("\n");

        tt.deleteTuples(bestTC.getTestCase());
    }

    // Now build final result with total first
    StringBuilder result = new StringBuilder();
    result.append("Total Test Cases Generated: ").append(count - 1).append("\n\n");
    result.append("Generated Result Set:\n");
    result.append(testCasesOutput);

    return result.toString();
}
    
    public static void initializeRoulleteWheel() {
        int[] degree = new int[360];
        int sum = 0;
        for (int i = 0; i < degree.length; i++) {
            degree[i] = i + 1;
            sum += degree[i];
        }
        double[] p = new double[360];
        for (int i = 0; i < degree.length; i++) {
            p[i] = (double) degree[i] / sum;
        }
        p[0] = c[0];
        for (int i = 1; i < degree.length; i++) {
            c[i] = p[i] + c[i - 1];
        }
    }
    
    public static int getTheta() {
        double r = (new Random()).nextDouble();
        for (int i = 0; i < c.length; i++) {
            if (r <= c[i]) return i;
        }
        return 360;
    }
    
    private String getTestCaseAsString(int[] tc) {
        StringBuilder sb = new StringBuilder();
        for (int val : tc) {
            sb.append(val).append(" ");
        }
        return sb.toString().trim();
    }
}
