package automation.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test context that stores entity objects created/used during a test.
 * Each entity type is stored in a labeled map for easy retrieval by label.
 */
public class TestContext
{

    // ---------------------------------------------------------------------------
    // Entity Maps – keyed by user-defined labels
    // ---------------------------------------------------------------------------
    private final Map<String, Object> cards = new LinkedHashMap<>();
    private final Map<String, Object> recipients = new LinkedHashMap<>();
    private final Map<String, Object> debitAccounts = new LinkedHashMap<>();
    private final Map<String, Object> transfers = new LinkedHashMap<>();
    private final Map<String, Object> budgets = new LinkedHashMap<>();
    private final Map<String, Object> claims = new LinkedHashMap<>();
    private final Map<String, Object> users = new LinkedHashMap<>();

    // ---------------------------------------------------------------------------
    // Cards
    // ---------------------------------------------------------------------------

    public void addCard(String label, Object card)
    {
        cards.put(label, card);
    }

    public Object getCard(String label)
    {
        return cards.get(label);
    }

    public Map<String, Object> getAllCards()
    {
        return Collections.unmodifiableMap(cards);
    }

    public boolean hasCard(String label)
    {
        return cards.containsKey(label);
    }

    public int cardCount()
    {
        return cards.size();
    }

    // ---------------------------------------------------------------------------
    // Recipients
    // ---------------------------------------------------------------------------

    public void addRecipient(String label, Object recipient)
    {
        recipients.put(label, recipient);
    }

    public Object getRecipient(String label)
    {
        return recipients.get(label);
    }

    public Map<String, Object> getAllRecipients()
    {
        return Collections.unmodifiableMap(recipients);
    }

    public boolean hasRecipient(String label)
    {
        return recipients.containsKey(label);
    }

    public int recipientCount()
    {
        return recipients.size();
    }

    // ---------------------------------------------------------------------------
    // Debit Accounts
    // ---------------------------------------------------------------------------

    public void addDebitAccount(String label, Object debitAccount)
    {
        debitAccounts.put(label, debitAccount);
    }

    public Object getDebitAccount(String label)
    {
        return debitAccounts.get(label);
    }

    public Map<String, Object> getAllDebitAccounts()
    {
        return Collections.unmodifiableMap(debitAccounts);
    }

    public boolean hasDebitAccount(String label)
    {
        return debitAccounts.containsKey(label);
    }

    public int debitAccountCount()
    {
        return debitAccounts.size();
    }

    // ---------------------------------------------------------------------------
    // Transfers
    // ---------------------------------------------------------------------------

    public void addTransfer(String label, Object transfer)
    {
        transfers.put(label, transfer);
    }

    public Object getTransfer(String label)
    {
        return transfers.get(label);
    }

    public Map<String, Object> getAllTransfers()
    {
        return Collections.unmodifiableMap(transfers);
    }

    public boolean hasTransfer(String label)
    {
        return transfers.containsKey(label);
    }

    public int transferCount()
    {
        return transfers.size();
    }

    // ---------------------------------------------------------------------------
    // Budgets
    // ---------------------------------------------------------------------------

    public void addBudget(String label, Object budget)
    {
        budgets.put(label, budget);
    }

    public Object getBudget(String label)
    {
        return budgets.get(label);
    }

    public Map<String, Object> getAllBudgets()
    {
        return Collections.unmodifiableMap(budgets);
    }

    public boolean hasBudget(String label)
    {
        return budgets.containsKey(label);
    }

    public int budgetCount()
    {
        return budgets.size();
    }

    // ---------------------------------------------------------------------------
    // Claims
    // ---------------------------------------------------------------------------

    public void addClaim(String label, Object claim)
    {
        claims.put(label, claim);
    }

    public Object getClaim(String label)
    {
        return claims.get(label);
    }

    public Map<String, Object> getAllClaims()
    {
        return Collections.unmodifiableMap(claims);
    }

    public boolean hasClaim(String label)
    {
        return claims.containsKey(label);
    }

    public int claimCount()
    {
        return claims.size();
    }

    // ---------------------------------------------------------------------------
    // Users
    // ---------------------------------------------------------------------------

    public void addUser(String label, Object user)
    {
        users.put(label, user);
    }

    public Object getUser(String label)
    {
        return users.get(label);
    }

    public Map<String, Object> getAllUsers()
    {
        return Collections.unmodifiableMap(users);
    }

    public boolean hasUser(String label)
    {
        return users.containsKey(label);
    }

    public int userCount()
    {
        return users.size();
    }

    // ---------------------------------------------------------------------------
    // Aggregate Operations
    // ---------------------------------------------------------------------------

    /**
     * Clear all stored entities. Useful for test cleanup.
     */
    public void clear()
    {
        cards.clear();
        recipients.clear();
        debitAccounts.clear();
        transfers.clear();
        budgets.clear();
        claims.clear();
        users.clear();
    }

    /**
     * Check if the context has no stored entities of any type.
     */
    public boolean isEmpty()
    {
        return cards.isEmpty()
                && recipients.isEmpty()
                && debitAccounts.isEmpty()
                && transfers.isEmpty()
                && budgets.isEmpty()
                && claims.isEmpty()
                && users.isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("TestContext {\n");
        appendSection(sb, "cards", cards);
        appendSection(sb, "recipients", recipients);
        appendSection(sb, "debitAccounts", debitAccounts);
        appendSection(sb, "transfers", transfers);
        appendSection(sb, "budgets", budgets);
        appendSection(sb, "claims", claims);
        appendSection(sb, "users", users);
        sb.append("}");
        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String name, Map<String, Object> map)
    {
        if (!map.isEmpty())
        {
            sb.append("  ").append(name).append(": {\n");
            map.forEach((label, obj) ->
                    sb.append("    ").append(label).append(" -> ").append(obj).append("\n"));
            sb.append("  }\n");
        }
    }
}
