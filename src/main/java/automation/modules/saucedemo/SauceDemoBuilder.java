package automation.modules.saucedemo;

import automation.core.DataGenerator;

/**
 * Fluent builder for SauceDemoData with sensible defaults.
 */
public class SauceDemoBuilder
{
    private Integer userId = 1;
    private String title;
    private String body;

    public SauceDemoBuilder withUserId(int userId)
    {
        this.userId = userId;
        return this;
    }

    public SauceDemoBuilder withTitle(String title)
    {
        this.title = title;
        return this;
    }

    public SauceDemoBuilder withBody(String body)
    {
        this.body = body;
        return this;
    }

    public SauceDemoBuilder withDefaults()
    {
        if (title == null) title = "Post_" + DataGenerator.randomAlphaString(6);
        if (body == null) body = "Body_" + DataGenerator.randomAlphaString(10);
        return this;
    }

    public SauceDemoData build()
    {
        withDefaults();
        SauceDemoData post = new SauceDemoData();
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }
}
