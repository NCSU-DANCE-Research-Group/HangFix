package cassandraIO;

import java.io.File;

public class FSReadError extends FSError
{
    public FSReadError(Throwable cause, File path)
    {
        super(cause, path);
    }

    public FSReadError(Throwable cause, String path)
    {
        this(cause, new File(path));
    }

    @Override
    public String toString()
    {
        return "FSReadError in " + path;
    }
}
