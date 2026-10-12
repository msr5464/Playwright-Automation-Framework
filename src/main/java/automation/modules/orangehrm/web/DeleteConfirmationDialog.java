package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class DeleteConfirmationDialog extends BasePage
{
    private final Locator confirmationMessage;
    private final Locator yesDeleteButton;

    public DeleteConfirmationDialog(Config config)
    {
        super(config);
        confirmationMessage = page.locator("p.oxd-text.oxd-text--p.oxd-text--card-title");
        yesDeleteButton     = page.locator("button.oxd-button.oxd-button--medium.oxd-button--label-danger.orangehrm-button-margin");
        assertPageLoaded(confirmationMessage);
    }

    public String getConfirmationMessage()
    {
        return getText(confirmationMessage, "Confirmation message");
    }

    public EmployeeListPage confirmDelete()
    {
        Log.comment(config, "Confirming employee deletion");
        click(yesDeleteButton, "Yes, Delete button");
        WaitHelper.waitForElementToBeHidden(config, confirmationMessage, "Confirmation dialog");
        return new EmployeeListPage(config);
    }
}
