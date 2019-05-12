/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.tranformers;

import edu.stanford.nlp.simple.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import photoSearch.models.Image;
import photoSearch.searchFunctionality.Search;

/**
 * This Class is used to make all appropriate parsing and normalization in the
 * text i.e. Image descriptions, links and queries
 *
 * @author Sgo
 */
public class ParagraphTransformer {

    private List<String> stopWords; // list of stop-words

    /**
     * Creates a ParagraphTransformer instance
     */
    public ParagraphTransformer() {
        this.stopWords = initStopWords();
    }

    /**
     * This method filters out every word in the input list that belongs to the
     * stop-words list
     *
     * @param tokens list of words to be checked for stop-word removal
     * @return list with all words as in the input list but without the
     * stop-words
     */
    public List<String> filterOutStopWords(List<String> tokens) {
        List<String> cleanTokens = new ArrayList<>();
        for (String token : tokens) {
            if (this.stopWords.contains(token)) {
                continue;
            } else {
                cleanTokens.add(token);
            }
        }
        return cleanTokens;
    }

    /**
     * Initializes the list of stop-words based on the stopWords file in the
     * src/main/resources/public folder
     *
     * @return an ArrayList containing the stop-words
     */
    public ArrayList<String> initStopWords() {

        ArrayList<String> stopWords = new ArrayList<>();
        InputStream is = ParagraphTransformer.class.getResourceAsStream("/public/stopWords.txt");
        System.out.println(is);

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines()) {
            stream.forEach(word -> stopWords.add(word));
        } catch (IOException ex) {
            Logger.getLogger(ParagraphTransformer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stopWords;
    }

    /**
     * Is used to parse and clean all Image descriptions from the inputFilePath
     * and writes them in the outputFilePath. It is used to train the paragraph
     * vectors
     *
     * @param inputFilePath path and file of the Images collection
     * @param outputFilePath path and file to write the clean descriptions
     */
    public void produceRawSentences(String inputFilePath, String outputFilePath) {
        Search api = new Search(inputFilePath);
        File file = new File(outputFilePath);

        try {

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Image img : api.getImages().values()) {
                bw.write(this.listToSentence(this.getCleanTokens(img.getDescription())) + "\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Transforms all words in the input list to lower case.
     *
     * @param tokens list of tokens to be tranformed in to lower case
     * @return a copy of the input list with only lower case characters
     */
    public List<String> toLowerCase(List<String> tokens) {
        List<String> lowers = new ArrayList<String>();
        for (String token : tokens) {
            lowers.add(token.toLowerCase());
        }

        return lowers;
    }

    /**
     *
     * @param link
     * @return
     */
    public String linkToSentence(String link) {
        String[] segments = link.split("/");

        String str = segments[segments.length - 1];

        str = str.replaceAll("-", " ").replaceAll("_", " ").replaceAll("\\+", " ").replaceAll("%20", " ");
        String[] splitedTerms = str.split("\\s");

        if (splitedTerms.length == 0) {
            splitedTerms = new String[]{"no available link"};
        }

        return (listToSentence(Arrays.asList(splitedTerms)));
    }

    /**
     * This class lemmatizes the input text removes the stop-words and
     * punctuation marks and transform all characters to lower case.
     *
     * @param text to be processed
     * @return a list with all tokens in the input text without stop-words,
     * lemmatized and with lowercase characters only
     */
    public List<String> getCleanTokens(String text) {

        Sentence sent = new Sentence(text);

        List<String> tokens = sent.lemmas();
        tokens = this.filterOutStopWords(tokens);
        tokens = Arrays.asList(tokens.toString().replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+"));

        return tokens;
    }

    /**
     * Transforms a list of words in to a single string sentence with the words
     * in the same order as in the input list separated with space.
     *
     * @param tokens list of words
     * @return the words as a sentence
     */
    public String listToSentence(List<String> tokens) {
        String sentence = "";
        for (String token : tokens) {
            sentence += token + " ";
        }

        return sentence.trim();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ParagraphTransformer trans = new ParagraphTransformer();

        String inputFilePath = "C:\\Users\\Sgo\\Desktop\\DATASETS\\GoogleImages\\googleImagesDev.tsv";
        String outputFilePath = "src/main/resources/public/descriptionsRawData_v3.txt";

        trans.produceRawSentences(inputFilePath, outputFilePath);
    }

}
