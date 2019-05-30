/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.searchFunctionality;

import edu.stanford.nlp.util.Sets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import photoSearch.models.AnnotationUnit;
import photoSearch.models.CandidatesUnit;
import photoSearch.models.Image;
import photoSearch.models.ResourceCandidate;
import photoSearch.models.SurfaceForm;
import photoSearch.tranformers.ParagraphTransformer;

/**
 * This class is used for Search over the Image dataset. It is also used for
 * taking advantage of other technologies like DBpedia Spotlight and Endpoint in
 * order to gather more information about the Images.
 *
 * @author Sgo
 */
public class Search {

    private HashMap<String, Image> images;
    private int collectionSize;
    private ParagraphTransformer trans;
    private DBpediaSpotlight spotlight;
    private DBpediaEndpoint endpoint;

    public Search(String filePath) {
        try {

            this.trans = new ParagraphTransformer(); // init paragraph transformer
            this.spotlight = new DBpediaSpotlight(); // init spotlight
            this.endpoint = new DBpediaEndpoint(); // init DBpediaEndpoint
            initDescriptions(filePath);

        } catch (IOException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public final void initDescriptions(String FilePath) throws FileNotFoundException, IOException {
        this.images = new HashMap<>();

        FileInputStream is = new FileInputStream("C:\\Users\\Sgo\\Desktop\\DATASETS\\GoogleImages\\googleImagesDev.tsv");
        Stream<String> stream = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines();

        stream.forEach(row -> insertImageRowData(row));

        this.collectionSize = this.images.size();
    }

    public void insertImageRowData(String row) {
        String id = UUID.randomUUID().toString();

        String[] data = row.split("\t");
        String description = data[0];
        String parseDescription = this.trans.listToSentence(this.trans.getCleanTokens(data[0]));
        String link = data[1];
        String parsedLink = trans.listToSentence(trans.getCleanTokens(trans.linkToSentence(data[1])));
        String linkNL = trans.linkToSentence(data[1]);

        Image img = new Image(id, description, parseDescription, link, parsedLink, linkNL);

        this.images.put(img.getId(), img);
    }

    /**
     * This method calculates the Jaccard Similarity between to Arrays of
     * Strings
     *
     * @param array of the string representations of the sentence terms
     * @param array of the string representations of the query terms
     * @return the Jaccard similarity of the input parameters in double format
     */
    public double JaccardSimilarity(String[] sentenceTerms, String[] queryTerms) {

        Set<String> sentenceTermsAsSet = new HashSet<>(Arrays.asList(sentenceTerms));
        Set<String> quesTermsAsSet = new HashSet<>(Arrays.asList(queryTerms));
        Set<String> union = Sets.union(sentenceTermsAsSet, quesTermsAsSet);
        Set<String> intersection = Sets.intersection(sentenceTermsAsSet, quesTermsAsSet);
        double jaccardSim = ((double) intersection.size()) / union.size();
        return jaccardSim;

    }

    /**
     * This method calculates the Similarity between to Arrays of Strings based
     * on the number of query terms that match with terms in the input sentence.
     *
     * @param array of the string representations of the sentence terms
     * @param array of the string representations of the query terms
     * @return
     */
    public double NormalizedMatchingWords(String[] sentenceTerms, String[] queryTerms) {
        double score = 0.0;
        for (int i = 0; i < queryTerms.length; i++) {
            for (int j = 0; j < sentenceTerms.length; j++) {
                if (queryTerms[i] == sentenceTerms[j]) {
                    score++;
                }
            }
        }

        return score / queryTerms.length;
    }

    /**
     * This method merges two arrays of String into one removing all duplicate
     * terms.
     *
     * @param array of terms
     * @param array of terms
     * @return merged array of terms
     */
    public String[] mergeArrays(String[] arr1, String[] arr2) {
        String[] merged = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, merged, 0, arr1.length);
        System.arraycopy(arr2, 0, merged, arr1.length, arr2.length);

        Set<String> nodupes = new HashSet<String>();

        for (int i = 0; i < merged.length; i++) {
            nodupes.add(merged[i]);
        }

        String[] nodupesarray = new String[nodupes.size()];
        int i = 0;
        Iterator<String> it = nodupes.iterator();
        while (it.hasNext()) {
            nodupesarray[i] = it.next();
            i++;
        }

        return nodupesarray;
    }

    /**
     * This method is used to score all images based on they similarity between
     * their descriptions/links and the input query.
     *
     * @param input query
     */
    public void scoreDescriptions(String query) {
        for (Image image : this.images.values()) {
            image.setScore(JaccardSimilarity(mergeArrays(image.getParseDescription().split(" "), image.getParsedLink().split(" ")), query.split(" ")));
            this.images.put(image.getId(), image);
        }
    }

    /**
     * Sorts the images in the input order based on their calculated similarity
     * and returns copy of the collection sorted.
     *
     * @param order i.e. false for decreasing or true for increasing order
     * @return a copy of the collection of images sorted in the input order
     */
    public HashMap<String, Image> SortImages(final boolean order) {

        List<Entry<String, Image>> list = new LinkedList<>(this.images.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Image>>() {
            @Override
            public int compare(Entry<String, Image> o1,
                    Entry<String, Image> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Image> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Image> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * This method score and sort the collection of images using SortImages and
     * scoreDescriptions methods and return the top k images.
     *
     * @param query
     * @param parameter k defines how many images will be returned
     * @return top k scored Images based on the input query
     */
    public JSONArray getMostSimilarDescriptions(String query, int k) {

        JSONArray mostSimDes = new JSONArray();

        int descIndex = 0;
        scoreDescriptions(this.trans.listToSentence(new ArrayList<>(new HashSet<>(this.trans.getCleanTokens(query)))));
        HashMap<String, Image> images_sorted = SortImages(false);
        for (Image image : images_sorted.values()) {
            if (descIndex < k && image.getScore() > 0.0) {
                JSONObject crntDes = new JSONObject();

                crntDes.put("id", image.getId());
                crntDes.put("description", image.getDescription());
                crntDes.put("link", image.getLink());
                crntDes.put("linkNL", image.getLinkNL());
                mostSimDes.put(crntDes);

                descIndex++;
            }
        }

        return mostSimDes;
    }

    /**
     * This is an extention of getMostSimilarDescriptions. It first extend the
     * query with the DBpedia abstract or Categories of the input URI and then
     * calls getMostSimilarDescriptions to score and sort the collection of
     * images using SortImages and scoreDescriptions methods and return the top
     * k images.
     *
     * @param query
     * @param uri the URI of the entity which will be used to extend the query
     * @param k parameter that defines how many images will be returned
     * @param expansion parameter that defines they type of the query expansion,
     * i.e type or abstract
     * @return top k scored Images based on the extended input query
     */
    public JSONArray advancedSearch(String query, String[] uris, int k, String expansion) {

        if (expansion.equals("type")) {

            HashSet<String> types = new HashSet<>();
            for (String uri : uris) {
                types.addAll(this.endpoint.getSubjects(uri));
            }

            HashSet<String> typesNL = new HashSet<>();
            for (String type : types) {
                typesNL.add(this.trans.linkToSentence(type).replace("Category:", ""));
            }

            String typesExpansion = typesNL.toString();
            query += typesExpansion;

        } else if (expansion.equals("abstract")) {

            HashSet<String> abstracts = new HashSet<>();

            for (String uri : uris) {
                abstracts.add(this.endpoint.getAbstract(uri));
            }

            String absExpansion = abstracts.toString();

            query += absExpansion;
        }

        return getMostSimilarDescriptions(query, k);
    }

    /**
     * Identifies and retrieves all entities in the image with the given id
     * using DBpediaSpotlight class.
     *
     * @param id of the image of which the entities will be retrieved
     * @return the identified entities in JSON format
     */
    public JSONArray getImageEntities(String id) {
        JSONArray entities = new JSONArray();

        try {
            Image img = this.images.get(id);
            String link = img.getLinkNL();
            AnnotationUnit anno = this.spotlight.get(link);

            if (anno.getResources() != null) {
                for (int i = 0; i < anno.getResources().size(); i++) {
                    JSONObject crntEntity = new JSONObject();
                    String surfaceForm = anno.getResources().get(i).getSurfaceForm();
                    crntEntity.put("surfaceForm", surfaceForm);
                    String uri = anno.getResources().get(i).getUri();
                    crntEntity.put("uri", uri);
                    crntEntity.put("abstract", this.endpoint.getAbstract(uri));
                    entities.put(crntEntity);
                }
            }

            return entities;

        } catch (IOException ex) {
            Logger.getLogger(Search.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method sort the input candidates with respect to the input order.
     *
     * @param list of candidate resources for a surface form
     * @param order true for increasing false for decreasing
     * @return the candidates sorted by their final score
     */
    public List<ResourceCandidate> sortCandidates(List<ResourceCandidate> candidates, boolean order) {

        // Sorting the list based on the candidates final scores
        Collections.sort(candidates, new Comparator<ResourceCandidate>() {
            @Override
            public int compare(ResourceCandidate o1, ResourceCandidate o2) {
                if (order) {
                    return o1.compareTo(o2);
                } else {
                    return o2.compareTo(o1);

                }
            }
        });

        return candidates;
    }

    /**
     * This method identify named entities in the input query and retrieves
     * candidate entities from DBpedia that are probable to refer to the
     * identified entities.
     *
     * @param query
     * @param confidence
     * @param support
     * @return an array with all identified entities and their candidate links
     * in DBpedia
     */
    public JSONArray getQueryCandidateEntities(String query, double confidence, int support) {
        JSONArray surfaceForms = new JSONArray();

        try {
            CandidatesUnit candidatesUnit = this.spotlight.getCandidates(query, confidence, support);

            for (SurfaceForm sf : candidatesUnit.getSurfaceForm()) {

                JSONObject crntEntity = new JSONObject();
                JSONArray candidateEntities = new JSONArray();

                for (ResourceCandidate rc : sortCandidates(sf.getResources(), false)) {

                    JSONObject crntCandidateEntity = new JSONObject();
                    crntCandidateEntity.put("uri", rc.getUri());
                    crntCandidateEntity.put("label", rc.getLabel());
                    crntCandidateEntity.put("types", rc.getTypes());

                    ArrayList<String> typesNL = new ArrayList<>();
                    for (String type : rc.getTypes().split(",")) {
                        typesNL.add(this.trans.linkToSentence(type));
                    }
                    crntCandidateEntity.put("typesNL", typesNL.toString().substring(1, typesNL.toString().length() - 1));
                    crntCandidateEntity.put("score", rc.getFinalScore());

                    candidateEntities.put(crntCandidateEntity);
                }

                crntEntity.put("name", sf.getName());
                crntEntity.put("candidates", candidateEntities);

                surfaceForms.put(crntEntity);
            }

            return surfaceForms;
        } catch (IOException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return null;
        }
    }

    /**
     * This method extract the abstract description of the input URI from
     * DBpedia and returns it as a JSON Object in order to be transferable
     * through the network.
     *
     * @param uri for which the abstract is needed.
     * @return the DBpedia abstract of the input URI as a JSON Object in order
     * to be transferable through the network
     */
    public JSONObject getAbstract(String uri) {
        JSONObject absJSON = new JSONObject();

        String abs = this.getEndpoint().getAbstract(uri);

        absJSON.put("abstract", abs);

        return absJSON;
    }

    /**
     * Get all Images.
     *
     * @return the collection of images.
     */
    public HashMap<String, Image> getImages() {
        return this.images;
    }

    /**
     * Get the number of images in the collection.
     *
     * @return the size of the collection
     */
    public int getCollectionSize() {
        return this.collectionSize;
    }

    /**
     * Return the ParagraphTransformer
     *
     * @return ParagraphTransformer
     */
    public ParagraphTransformer getTransformer() {
        return this.trans;
    }

    /**
     * Return the DBpediaEndpoint
     *
     * @return DBpediaEndpoint
     */
    public DBpediaEndpoint getEndpoint() {
        return this.endpoint;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\Sgo\\Desktop\\DATASETS\\GoogleImages\\googleImagesDev.tsv";
        Search API = new Search(filePath);
        ParagraphTransformer trans = new ParagraphTransformer();
        String query;
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Search: ");
            query = sc.nextLine();
            query = trans.listToSentence(trans.getCleanTokens(query));
            System.out.println(API.getMostSimilarDescriptions(query, 10));

        }

    }

}
