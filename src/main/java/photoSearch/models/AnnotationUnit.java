package photoSearch.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import static photoSearch.models.Constants.DBPEDIA;
import static photoSearch.models.Constants.HTTP;
import static photoSearch.models.Constants.SCHEMA;
import static photoSearch.models.Prefixes.DBPEDIA_ONTOLOGY;
import static photoSearch.models.Prefixes.SCHEMA_ONTOLOGY;

public class AnnotationUnit {

    @SerializedName("@text")
    private String text;

    @SerializedName("@confidence")
    private String confidence;

    @SerializedName("@support")
    private String support;

    @SerializedName("@types")
    private String types;

    @SerializedName("@sparql")
    private String sparql;

    @SerializedName("@policy")
    private String policy;

    @SerializedName("Resources")
    private List<ResourceItem> resources;

    public AnnotationUnit() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public void setResources(List<ResourceItem> resources) {
        this.resources = resources;
    }

    public String getText() {
        return text;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getSupport() {
        return support;
    }

    public String getSparql() {
        return sparql;
    }

    public String getPolicy() {
        return policy;
    }

    public List<ResourceItem> getResources() {
        return resources;
    }

    public Integer endIndex() {
        if (text != null) {
            return text.length();
        }
        return 0;
    }

    public String getTypes() {
        if (types != null && !types.isEmpty()) {
            return types.replace("Http", HTTP).
                    replace(DBPEDIA, DBPEDIA_ONTOLOGY).
                    replace(SCHEMA, SCHEMA_ONTOLOGY);
        }
        return types;
    }

    public Integer beginIndex() {
        return 1;
    }
}
