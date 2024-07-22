package ns.project.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/opensearch")
public class OpenSearchController {

    private final OpenSearchClient openSearchClient;

    public OpenSearchController(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    /**
     * 전체 문서 조회 API
     *
     * @param indexName 조회할 인덱스명
     * @return 전체 문서 목록을 JSON으로 반환
     */
    @GetMapping("/documents/{indexName}")
    public List<JsonNode> getAllDocuments(@PathVariable String indexName) {
        try {
            // 전체 문서 조회를 위한 SearchRequest
            SearchRequest searchRequest = SearchRequest.of(s -> s.index(indexName));
            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 조건으로 문서 조회 API
     *
     * @param indexName 조회할 인덱스명
     * @param logGroup  검색할 log_group 값
     * @return 필터링된 문서 목록을 JSON으로 반환
     */
    @GetMapping("/documents/{indexName}/{logGroup}")
    public List<JsonNode> getDocumentsByLogGroup(@PathVariable String indexName, @PathVariable String logGroup) {
        try {
            // 특정 log_group 값을 기준으로 필터링하는 쿼리 작성
            Query query = Query.of(q -> q
                    .term(t -> t
                            .field("log_group")
                            .value(FieldValue.of(logGroup))
                    )
            );

            // SearchRequest에 쿼리 추가
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(query)
            );

            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit-> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e);
        }
    }

    @GetMapping("/cloudtrail-events/{indexName}/{eventName}")
    public List<JsonNode> getCloudTrailEventsByEventName(@PathVariable String indexName, @PathVariable String eventName) {
        try {
            // AWS CloudTrail 이벤트를 기준으로 필터링하는 쿼리 작성
            Query query = Query.of(q -> q
                    .term(t -> t
                            .field("eventName")
                            .value(FieldValue.of(eventName))
                    )
            );

            // SearchRequest에 쿼리 추가
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(query)
            );

            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving CloudTrail events from OpenSearch: " + e.getMessage(), e);
        }
    }

    @GetMapping("/waf-logs/{indexName}/{webAclId}")
    public List<JsonNode> getWafLogsByWebAclId(@PathVariable String indexName) {
        try {
            // AWS WAF 로그를 기준으로 필터링하는 쿼리 작성
            Query query = Query.of(q -> q
                    .term(t -> t
                            .field("webaclId")
                            .value(FieldValue.of("arn:aws:wafv2:ap-northeast-2:058264524253:regional/webacl/waf-logs/943e41aa-f12a-47fd-9450-ce2eaa2b9205"))
                    )
            );

            // SearchRequest에 쿼리 추가
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(query)
            );

            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving WAF logs from OpenSearch: " + e.getMessage(), e);
        }
    }

    @GetMapping("/check-field-waf/{indexName}")
    public List<JsonNode> checkFieldExistenceWAF(@PathVariable String indexName) {
        try {
            // 필드 존재 여부를 확인하는 쿼리 작성
            Query query = Query.of(q -> q
                    .exists(e -> e
                            .field("webaclId")
                    )
            );

            // SearchRequest에 쿼리 추가
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(query)
            );

            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error checking field existence in OpenSearch: " + e.getMessage(), e);
        }
    }

    @GetMapping("/check-field-cloudtrail/{indexName}")
    public List<JsonNode> checkFieldExistenceCloudTrail(@PathVariable String indexName) {
        try {
            // 필드 존재 여부를 확인하는 쿼리 작성
            Query query = Query.of(q -> q
                    .exists(e -> e
                            .field("eventName")
                    )
            );

            // SearchRequest에 쿼리 추가
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(query)
            );

            SearchResponse<JsonNode> searchResponse = openSearchClient.search(searchRequest, JsonNode.class);

            // JSON 응답을 그대로 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error checking field existence in OpenSearch: " + e.getMessage(), e);
        }
    }
}
