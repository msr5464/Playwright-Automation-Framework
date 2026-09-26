package automation.modules.aiteststudio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Talk to Tests request and reply ({@code POST /api/customer/query}).
 * Unset fields are left out of the request, so only the request fields that were set are sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TalkToTestsData
{
    // Request fields
    @JsonProperty("question")
    private String question;

    @JsonProperty("use_rag")
    private Boolean useRag;

    @JsonProperty("bypass_cache")
    private Boolean bypassCache;

    // Response fields
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("error")
    private String error;

    @JsonProperty("source_documents")
    private List<Map<String, Object>> sourceDocuments;

    @JsonProperty("metrics")
    private Map<String, Object> metrics;
}
