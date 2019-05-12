/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.searchFunctionality;

import com.google.gson.Gson;
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
import static photoSearch.models.Constants.EMPTY;
import static photoSearch.models.Prefixes.DBPEDIA_ONTOLOGY;
import static photoSearch.models.Prefixes.SCHEMA_ONTOLOGY;
import photoSearch.models.ResourceItem;
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
     * @return
     * @throws IOException
     */
    public AnnotationUnit get(String text) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        requestAnno.setEntity(new UrlEncodedFormEntity(params));

        return get();
    }

    public static void main(String[] args) throws IOException {

        DBpediaSpotlight spotlight = new DBpediaSpotlight(); // init spotlight
        ParagraphTransformer trans = new ParagraphTransformer();

        String sentence = trans.linkToSentence("this song was played by Europe in the concert");
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
            }
        }
    }
}
