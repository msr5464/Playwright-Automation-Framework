package automation.modules.demo;

public class DemoBuilder
{
    private String title;

    public DemoBuilder withTitle(String title) { this.title = title; return this; }

    public DemoBuilder withDefaults()
    {
        if (title == null) title = "Example Domain";
        return this;
    }

    public DemoData build()
    {
        withDefaults();
        DemoData data = new DemoData();
        data.setTitle(title);
        return data;
    }
}
