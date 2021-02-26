package cassandraIO;

import java.io.File;
import java.io.IOError;

public abstract class FSError extends IOError
{
    public final File path;

    public FSError(Throwable cause, File path)
    {
        super(cause);
        this.path = path;
    }

    /**
     * Unwraps the Throwable cause chain looking for an FSError instance
     * @param top the top-level Throwable to unwrap
     * @return FSError if found any, null otherwise
     */
    public static FSError findNested(Throwable top)
    {
        for (Throwable t = top; t != null; t = t.getCause())
        {
            if (t instanceof FSError)
                return (FSError) t;
        }

        return null;
    }
}
