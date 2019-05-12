package photoSearch.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static photoSearch.models.Constants.COMMA;

public class ResourceItem {

    @SerializedName("@URI")
    private String uri;

    @SerializedName("@support")
    private String support;

    @SerializedName("@types")
    private String types;

    @SerializedName("@surfaceForm")
    private String surfaceForm;

    @SerializedName("@offset")
    private String offSet;

    @SerializedName("@similarityScore")
    private String similarityScore;

    @SerializedName("@percentageOfSecondRank")
    private String percentageOfSecondRank;

    public ResourceItem() {
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public void setSurfaceForm(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }

    public void setOffSet(String offSet) {
        this.offSet = offSet;
    }

    public void setSimilarityScore(String similarityScore) {
        this.similarityScore = similarityScore;
    }

    public void setPercentageOfSecondRank(String percentageOfSecondRank) {
        this.percentageOfSecondRank = percentageOfSecondRank;
    }

    public String getUri() {
        return uri;
    }

    public String getSupport() {
        return support;
    }

    public String getTypes() {
        return types;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public String getOffSet() {
        return offSet;
    }

    public String getSimilarityScore() {
        return similarityScore;
    }

    public String getPercentageOfSecondRank() {
        return percentageOfSecondRank;
    }

    public Integer beginIndex() {
        try {
            return Integer.valueOf(offSet);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Integer endIndex() {
        if (surfaceForm != null) {
            return beginIndex() + surfaceForm.length();
        }

        return 0;
    }

    public List<String> typesList() {

        if (types != null && !types.isEmpty()) {
            return Arrays.asList(types.split(COMMA));
        }

        return new ArrayList<>();
    }

    public Double score() {
        try {
            return Double.valueOf(similarityScore);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

}
