package de.uros.citlab.textalignment;

import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.confmat.ConfMat;
import de.uros.citlab.errorrate.normalizer.StringNormalizerDft;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.LineMatch;
import de.uros.citlab.tokenizer.TokenizerCategorizer;
import de.uros.citlab.tokenizer.categorizer.CategorizerWordMergeGroups;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.soap.Text;
import java.io.File;
import java.text.Normalizer;
import java.util.*;

public class TextAlignerTest {

    private double propNaC = 0.5;
    private double doubleChar = 0.5;
    private double variance = 0.5;
    private double offsetBP = 10.0;
    private static Random r = new Random(1234);

    @Test
    public void testToyExamples() {
        TextAligner textAligner = new TextAligner(
                " ",
                4.0,
                0.2,
                1.0
        );
//        textAligner.setHp(new HyphenationProperty(6.0, null));
        textAligner.setDebugOutput(1000, new File("out.png"));
        textAligner.setHp(null);
        textAligner.setThreshold(0.0);
        textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
        testCase(textAligner, Arrays.asList("line 1", "line 2"), Arrays.asList("line 1", "line 2"), Arrays.asList("line 1", "line 2"));
        testCase(textAligner, Arrays.asList("line 1", "line 2"), Arrays.asList("line 1 line 2"), Arrays.asList("line 1", "line 2"));
        testCase(textAligner,
                Arrays.asList(" ab", "c d", "ef "),
                Arrays.asList("ab", "cd", "ef "),
                Arrays.asList("ab", "cd", "ef"));

    }

    @Test
    public void getLargeSzenario() {
        TextAligner textAligner = new TextAligner(
                " ",
                4.0,
                0.2,
                5.0
        );
//        textAligner.setHp(new HyphenationProperty(6.0, null));
        textAligner.setDebugOutput(1000, new File("out.png"));
        textAligner.setHp(null);
        textAligner.setThreshold(0.0);
        offsetBP = 1.0;
        variance = 0.05;
        textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
        testCase(textAligner,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
        );
        testCase(textAligner,
                Arrays.asList("in normal cases the szenario has",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        null,
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
        );
        testCase(textAligner,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
                ,
                Arrays.asList("in normal cases the szenario has",
                        "the alignment problem is not that easy",
                        "a much longer context, so that",
                        "time to get a real szenario. In addition we have to",
                        "enter some more distotions like wrong words, missing",
                        "for the method. We have to copy these part some more",
                        "words, reading order errors, and - of course hypenations!")
        );
    }


    private void testCase(TextAligner textAligner, List<String> confMats, List<String> references, List<String> target) {
        Assert.assertEquals("number of targets have to match the number of confmats have to be the same - wrong test case", confMats.size(), target.size());
        List<ConfMat> confMatsList = new ArrayList<>();
        for (String cm : confMats) {
            confMatsList.add(generateConfMat(getCharMapAlphaNum(), cm, r));
        }
        List<LineMatch> alignmentResult = textAligner.getAlignmentResult(references, confMatsList);
        List<String> res = new LinkedList<>();
        for (int i = 0; i < alignmentResult.size(); i++) {
            LineMatch lineMatch = alignmentResult.get(i);
            res.add(lineMatch == null ? null : lineMatch.getReference());
        }
        Assert.assertEquals("T2I fails", target.toString(), res.toString());
    }


    public static CharMap getCharMapAlphaNum() {
        CharMap res = new CharMap();
        res.add(" ");
        res.add("-");
        res.add(",");
        res.add(".");
        res.add("!");
        for (char c = 'a'; c <= 'z'; c++) {
            res.add(c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            res.add(c);
        }
        for (char c = '0'; c <= '9'; c++) {
            res.add(c);
        }
        return res;
    }

    private ConfMat generateConfMat(CharMap cm, String reference, Random rnd) {
        return generateConfMat(cm, reference, rnd, propNaC, doubleChar, variance, offsetBP);
    }

    private static ConfMat generateConfMat(CharMap cm, String reference, Random rnd, double propNaC, double doubleChar, double varaiance, double offsetBP) {
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
        //BestPath ready
        String bp = sb.toString();
        double[][] mat = new double[bp.length()][cm.size()];
        for (int i = 0; i < mat.length; i++) {
            double[] vec = mat[i];
            for (int j = 0; j < vec.length; j++) {
                vec[j] = rnd.nextGaussian() * varaiance;
            }
            vec[cm.get(bp.charAt(i))] += offsetBP;
        }
        return new ConfMat(cm, mat);
    }
}