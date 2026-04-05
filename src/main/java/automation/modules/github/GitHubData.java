package automation.modules.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GitHub API response POJO for user and repository data.
 * Supports JSON deserialization from GitHub REST API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubData
{
    // User fields
    @JsonProperty("login")
    private String login;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("company")
    private String company;

    @JsonProperty("location")
    private String location;

    @JsonProperty("email")
    private String email;

    @JsonProperty("public_repos")
    private Integer publicRepos;

    @JsonProperty("followers")
    private Integer followers;

    @JsonProperty("following")
    private Integer following;

    // Repository fields
    @JsonProperty("owner")
    private Owner owner;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("url")
    private String url;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("stargazers_count")
    private Integer starsCount;

    @JsonProperty("watchers_count")
    private Integer watchersCount;

    @JsonProperty("language")
    private String language;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("pushed_at")
    private String pushedAt;

    /**
     * Nested owner information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner
    {
        @JsonProperty("login")
        private String login;

        @JsonProperty("id")
        private Long id;

        @JsonProperty("avatar_url")
        private String avatarUrl;

        @JsonProperty("type")
        private String type;
    }
}
