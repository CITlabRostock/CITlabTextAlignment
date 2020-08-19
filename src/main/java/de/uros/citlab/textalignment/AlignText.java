package de.uros.citlab.textalignment;

import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.confmat.ConfMat;
import de.uros.citlab.textalignment.types.LineMatch;
//import org.junit.Assert;
//import org.junit.Test;
import java.io.File;
import java.lang.NullPointerException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.*;

public class AlignText {

  private static Random r = new Random(1234);
  private static double propNaC = 0.5;
  private static double doubleChar = 0.5;
  private static double variance = 0.5;
  private static double offsetBP = 10.0;

  public static void main(String[] args) {

    long startTime = System.currentTimeMillis();

    TextAligner textAligner = new TextAligner(" ", 4.0, 0.2, 6.0, 0 // threshold 0.1: only very trustful matches, less than 0.01 = caution
    );
    Scanner s1;
    Scanner s2;
    ArrayList<String> references = new ArrayList<String>();
    ArrayList<String> predictions = new ArrayList<String>();
    
    try {
      s1 = new Scanner(new File(args[0]));

      while (s1.hasNextLine()) {
        references.add(s1.nextLine());
      }
      s1.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      s2 = new Scanner(new File(args[1]));
      while (s2.hasNextLine()) {
        predictions.add(s2.nextLine());
      }
      s2.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }


    CharMap cm = getCharMap(args[2]);

    List<ConfMat> predictionConfMatsList = new ArrayList<>();
    for (String pred : predictions) {
      predictionConfMatsList.add(generateConfMat(cm, pred, r));
    }
    List<LineMatch> alignmentResult = textAligner.getAlignmentResult(references, predictionConfMatsList);

    List<String> res = new LinkedList<>();
    for (int i = 0; i < alignmentResult.size(); i++) {
      LineMatch lineMatch = alignmentResult.get(i);
      res.add(lineMatch == null ? null : lineMatch.getReference());
    }

    int count = 0;
    boolean isNull;
    for (int i = 0; i < alignmentResult.size(); i++) {
      isNull = alignmentResult.get(i) == null;
      if (!isNull) {
        count += 1;
      }
    }
    System.out.printf("Number of aligned lines : %d out of %d \n", count, alignmentResult.size());

    for (int i = 0; i < alignmentResult.size(); i++) {
      try {
        LineMatch match = alignmentResult.get(i);
        String reference = match.getReference();
        double confidence = match.getConfidence();
        System.out.printf("line: %d prediction: %s reference: %s confidence: %s\n", i, predictions.get(i), reference,
            confidence);
      } catch (NullPointerException e) {
        // ignore this line
      }

    }

    long endTime = System.currentTimeMillis();
    System.out.println("Alignment took " + (endTime - startTime) + " milliseconds");

  }

  public static CharMap getCharMap(String chars) {
    CharMap res = new CharMap();
    for (int i=0; i < chars.length(); i++) {
      res.add(chars.charAt(i));
    }
    res.add(' ');
    return res;
  }

  private static ConfMat generateConfMat(CharMap cm, String reference, Random rnd) {
    return generateConfMat(cm, reference, rnd, propNaC, doubleChar, variance, offsetBP);
  }

  private static ConfMat generateConfMat(CharMap cm, String reference, Random rnd, double propNaC, double doubleChar,
      double variance, double offsetBP) {
    StringBuilder sb = new StringBuilder();
    char last = CharMap.NaC;
    for (int i = 0; i < reference.length(); i++) {
      char cur = reference.charAt(i);
      
      if (cm.get(cur) == null) {
        throw new RuntimeException("character '" + cur + "' is not in CharMap");
      }
      if (cur == last || rnd.nextDouble() < propNaC) {
        sb.append(CharMap.NaC);
      }
      sb.append(cur);
      if (rnd.nextDouble() < doubleChar) {
        sb.append(CharMap.NaC);
      }
      last = cur;
    }
    if (rnd.nextDouble() < propNaC) {
      sb.append(CharMap.NaC);
    }
    // BestPath ready
    String bp = sb.toString();
    double[][] mat = new double[bp.length()][cm.size()];
    for (int i = 0; i < mat.length; i++) {
      double[] vec = mat[i];
      for (int j = 0; j < vec.length; j++) {
        vec[j] = rnd.nextGaussian() * variance;
      }
      vec[cm.get(bp.charAt(i))] += offsetBP;
    }
    return new ConfMat(cm, mat);
  }
}
