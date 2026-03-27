package automation.aiEval.orchestrator;

public class CIGateException extends RuntimeException
{
    public CIGateException(String message)
    {
        super(message);
    }

    public CIGateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
