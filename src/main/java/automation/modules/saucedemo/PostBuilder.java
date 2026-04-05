package automation.modules.saucedemo;

import automation.core.DataGenerator;

/**
 * Fluent builder for PostData with sensible defaults.
 */
public class PostBuilder
{
    private Integer userId = 1;
    private String title;
    private String body;

    public PostBuilder withUserId(int userId)
    {
        this.userId = userId;
        return this;
    }

    public PostBuilder withTitle(String title)
    {
        this.title = title;
        return this;
    }

    public PostBuilder withBody(String body)
    {
        this.body = body;
        return this;
    }

    public PostBuilder withDefaults()
    {
        if (title == null) title = "Post_" + DataGenerator.randomAlphaString(6);
        if (body == null) body = "Body_" + DataGenerator.randomAlphaString(10);
        return this;
    }

    public PostData build()
    {
        withDefaults();
        PostData post = new PostData();
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }
}
