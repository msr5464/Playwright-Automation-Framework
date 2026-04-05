package automation.modules.github;

import automation.core.DataGenerator;

/**
 * Fluent builder for constructing GitHubData test payloads.
 * Used when creating test data for GitHub API tests.
 */
public class GitHubBuilder
{
    private String login;
    private Long id;
    private String name;
    private String bio;
    private String company;
    private String location;
    private String email;
    private Integer publicRepos;
    private Integer followers;
    private Integer following;
    private String fullName;
    private String description;
    private String language;

    public GitHubBuilder withLogin(String login)
    {
        this.login = login;
        return this;
    }

    public GitHubBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public GitHubBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public GitHubBuilder withBio(String bio)
    {
        this.bio = bio;
        return this;
    }

    public GitHubBuilder withCompany(String company)
    {
        this.company = company;
        return this;
    }

    public GitHubBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public GitHubBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public GitHubBuilder withPublicRepos(Integer publicRepos)
    {
        this.publicRepos = publicRepos;
        return this;
    }

    public GitHubBuilder withFollowers(Integer followers)
    {
        this.followers = followers;
        return this;
    }

    public GitHubBuilder withFollowing(Integer following)
    {
        this.following = following;
        return this;
    }

    public GitHubBuilder withFullName(String fullName)
    {
        this.fullName = fullName;
        return this;
    }

    public GitHubBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public GitHubBuilder withLanguage(String language)
    {
        this.language = language;
        return this;
    }

    /**
     * Set default values for optional fields.
     */
    public GitHubBuilder withDefaults()
    {
        if (name == null) name = "Test User " + DataGenerator.randomAlphaString(5);
        if (bio == null) bio = "Test bio " + DataGenerator.randomAlphaString(10);
        if (location == null) location = "Test Location";
        if (publicRepos == null) publicRepos = 0;
        return this;
    }

    /**
     * Build the GitHubData object.
     */
    public GitHubData build()
    {
        withDefaults();
        GitHubData data = new GitHubData();
        data.setLogin(login);
        data.setId(id);
        data.setName(name);
        data.setBio(bio);
        data.setCompany(company);
        data.setLocation(location);
        data.setEmail(email);
        data.setPublicRepos(publicRepos);
        data.setFollowers(followers);
        data.setFollowing(following);
        data.setFullName(fullName);
        data.setDescription(description);
        data.setLanguage(language);
        return data;
    }
}
