/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.searchFunctionality;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import photoSearch.models.AnnotationUnit;
import photoSearch.models.CandidatesUnit;
import static photoSearch.models.Constants.EMPTY;
import static photoSearch.models.Prefixes.DBPEDIA_ONTOLOGY;
import static photoSearch.models.Prefixes.SCHEMA_ONTOLOGY;
import photoSearch.models.ResourceCandidate;
import photoSearch.models.ResourceItem;
import photoSearch.models.SurfaceForm;
import photoSearch.tranformers.ParagraphTransformer;

/**
 * This class is used for named entity recognition and linking in DBpedia open
 * RDF database.
 *
 * @author Sgo
 */
public class DBpediaSpotlight {

    private static final String URLCand = "http://api.dbpedia-spotlight.org/en/candidates";
    private static final String URLAnno = "https://api.dbpedia-spotlight.org/en/annotate";
    private final HttpClient client;
    private final HttpPost requestAnno;
    private final HttpPost requestCand;

    /**
     * Constructor. Creates a DBpediaSpotlight instance.
     */
    public DBpediaSpotlight() {

        client = HttpClientBuilder.create().build();
        requestAnno = new HttpPost(URLAnno);
        requestCand = new HttpPost(URLCand);

        init();

    }

    /**
     * This method sets all the appropriate headers for the HTTP communication
     * with DBpedia Spotlight REST API.
     */
    private void init() {

        requestAnno.addHeader(ACCEPT, "application/json");
        requestAnno.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1");

        requestCand.addHeader(ACCEPT, "application/json");
        requestCand.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1");
    }

    /**
     * This method is used to send request to DBpedia Spotlight REST API and
     * return response of the aforementioned API as an AnnotationUnit instance.
     *
     * @return annotation unit
     * @throws IOException
     */
    private AnnotationUnit get() throws IOException {
        Gson gson = new Gson();

        AnnotationUnit annotationUnit = gson.fromJson(getContent(requestAnno), AnnotationUnit.class);
        fixPrefixes(annotationUnit.getResources());

        return annotationUnit;
    }

    /**
     * This method returns the natural language representation of the input
     * value of type String.
     *
     * @param value
     * @return natural language representation of value
     */
    private String fixPrefixes(String value) {

        if (value != null && !value.isEmpty()) {
            return value.replace("Http", "http").
                    replace("DBpedia:", DBPEDIA_ONTOLOGY).
                    replace("Schema:", SCHEMA_ONTOLOGY);
        }
        return value;

    }

    /**
     * This method returns the natural language representation of the input
     * resource of type ResourceItem.
     *
     * @param resource
     */
    private void fixPrefixes(ResourceItem resource) {
        resource.setTypes(fixPrefixes(resource.getTypes()));
    }

    /**
     * This method returns the natural language representation of the input list
     * of resources of type ResourceItem.
     *
     * @param resources
     */
    private void fixPrefixes(List<ResourceItem> resources) {

        if (resources != null && !resources.isEmpty()) {

            resources.forEach(resourceItem -> fixPrefixes(resourceItem));

        }

    }

    private void fixPrefixes(ResourceCandidate resource) {
        resource.setTypes(fixPrefixes(resource.getTypes()));
    }

    /**
     * This method parses the input HTTP request and returns the string
     * representation of the response.
     *
     * @param request
     * @return string representation of the response
     * @throws IOException
     */
    private String getContent(HttpPost request) throws IOException {

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();

        String line = EMPTY;

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    /**
     * This method parses the input text and returns any identified Named
     * Entities in it as an AnnotationUnit instance.
     *
     * @param text
     * @return identified Named Entities in it as an AnnotationUnit instance
     * @throws IOException
     */
    public AnnotationUnit get(String text) throws IOException {

        text = text.replaceAll("_", " ").replaceAll("\\(", "").replace("\\)", "");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        requestAnno.setEntity(new UrlEncodedFormEntity(params));

        return get();
    }

    /**
     * This method parses the input text and returns candidate links for each of
     * the identified Named Entities in it as an CandidatesUnit instance.
     *
     * @param text
     * @param confidence
     * @param support
     * @return candidate links for each of the identified Named Entities in it
     * as an CandidatesUnit instance.
     * @throws IOException
     */
    public CandidatesUnit getCandidates(String text, double confidence, int support) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("confidence", String.valueOf(confidence)));
        params.add(new BasicNameValuePair("support", String.valueOf(support)));
        requestCand.setEntity(new UrlEncodedFormEntity(params));

        return getCandidates();
    }

    /**
     * Retrieves all the fields of a candidate resource
     *
     * @param resource
     * @return a ResourceCandidate instance
     */
    private ResourceCandidate getResourceCandidate(JsonElement resource) {
        String label = resource.getAsJsonObject().get("@label").toString();
        String uri = resource.getAsJsonObject().get("@uri").toString();
        String contextualScore = resource.getAsJsonObject().get("@contextualScore").toString();
        String percentageOfSecondRank = resource.getAsJsonObject().get("@percentageOfSecondRank").toString();
        String support = resource.getAsJsonObject().get("@support").toString();
        String priorScore = resource.getAsJsonObject().get("@priorScore").toString();
        String finalScore = resource.getAsJsonObject().get("@finalScore").toString();
        String types = resource.getAsJsonObject().get("@types").toString();

        ResourceCandidate resourceCandidate = new ResourceCandidate(uri, label, contextualScore, percentageOfSecondRank, support, priorScore, finalScore, types);
        return resourceCandidate;
    }

    /**
     * Returns candidate links for each of the identified Named Entities in it
     * as an CandidatesUnit instance.
     *
     * @return links for each of the identified Named Entities in it as an
     * CandidatesUnit instance.
     * @throws IOException
     */
    public CandidatesUnit getCandidates() throws IOException {
        JsonParser parser = new JsonParser();
        String json = getContent(requestCand);

        CandidatesUnit candidatesUnit = new CandidatesUnit();
        JsonElement jsonTree = parser.parse(json);
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonElement annotation = jsonObject.get("annotation");
            if (annotation.isJsonObject()) {
                String text = annotation.getAsJsonObject().get("@text").toString();
                candidatesUnit.setText(text);
                JsonElement surfaceForm = annotation.getAsJsonObject().get("surfaceForm");

                if (surfaceForm.isJsonArray()) {
                    for (JsonElement sf : surfaceForm.getAsJsonArray()) {
                        if (sf.isJsonObject()) {
                            String name = sf.getAsJsonObject().get("@name").toString();
                            int offset = sf.getAsJsonObject().get("@offset").getAsInt();
                            SurfaceForm surfaceFormObj = new SurfaceForm(name, offset);

                            JsonElement resource = sf.getAsJsonObject().get("resource");
                            if (resource.isJsonArray()) {
                                for (JsonElement rs : resource.getAsJsonArray()) {
                                    if (rs.isJsonObject()) {
                                        surfaceFormObj.addResource(getResourceCandidate(rs));
                                    }
                                }
                                candidatesUnit.addSurfaceForm(surfaceFormObj);
                            } else {
                                if (resource.isJsonObject()) {
                                    surfaceFormObj.addResource(getResourceCandidate(resource));
                                }
                                candidatesUnit.addSurfaceForm(surfaceFormObj);
                            }
                        }
                    }
                } else {
                    if (surfaceForm.isJsonObject()) {
                        String name = surfaceForm.getAsJsonObject().get("@name").toString();
                        int offset = surfaceForm.getAsJsonObject().get("@offset").getAsInt();
                        SurfaceForm surfaceFormObj = new SurfaceForm(name, offset);

                        JsonElement resource = surfaceForm.getAsJsonObject().get("resource");
                        if (resource.isJsonArray()) {
                            for (JsonElement rs : resource.getAsJsonArray()) {
                                if (rs.isJsonObject()) {
                                    surfaceFormObj.addResource(getResourceCandidate(rs));
                                }
                            }
                            candidatesUnit.addSurfaceForm(surfaceFormObj);
                        } else {
                            if (resource.isJsonObject()) {
                                surfaceFormObj.addResource(getResourceCandidate(resource));
                            }
                            candidatesUnit.addSurfaceForm(surfaceFormObj);
                        }
                    }
                }
            }
        }

        for (SurfaceForm sf : candidatesUnit.getSurfaceForm()) {
            for (ResourceCandidate rc : sf.getResources()) {
                fixPrefixes(rc);
            }
        }

        return candidatesUnit;
    }

    public static void main(String[] args) throws IOException {

        DBpediaSpotlight spotlight = new DBpediaSpotlight(); // init spotlight
        ParagraphTransformer trans = new ParagraphTransformer();

        String sentence = trans.linkToSentence("pug");
        AnnotationUnit annotationUnit = spotlight.get(sentence);

        if (annotationUnit.getResources() != null) {
            for (int i = 0; i < annotationUnit.getResources().size(); i++) {
                System.out.println(annotationUnit.getText());
                System.out.println(annotationUnit.getResources().get(i).getSurfaceForm());
                System.out.println(annotationUnit.getResources().get(i).getUri());
                System.out.println(annotationUnit.getResources().get(i).getSimilarityScore());
                System.out.println(annotationUnit.getResources().get(i).getSupport());
                System.out.println(annotationUnit.getResources().get(i).getTypes());
                System.out.println(annotationUnit.getResources().get(i).getOffSet());
                System.out.println(annotationUnit.getResources().get(i).beginIndex());
                System.out.println(annotationUnit.getResources().get(i).endIndex());
                System.out.println();
            }
        }

        System.out.println();
        System.out.println();

        CandidatesUnit candidatesUnit = spotlight.getCandidates(sentence, 0.0, 0);
        System.out.println(candidatesUnit.getSurfaceForm().size());
        for (SurfaceForm sf : candidatesUnit.getSurfaceForm()) {
            System.out.println(sf.getName());
            for (ResourceCandidate rc : sf.getResources()) {
                System.out.println(rc.getUri());
                System.out.println(rc.getLabel());
                System.out.println(rc.getTypes());
                System.out.println(rc.getContextualScore());
                System.out.println(rc.getFinalScore());
                System.out.println(rc.getPercentageOfSecondRank());
                System.out.println(rc.getPriorScore());
                System.out.println(rc.getSupport());
                System.out.println();
            }
        }
    }
}
