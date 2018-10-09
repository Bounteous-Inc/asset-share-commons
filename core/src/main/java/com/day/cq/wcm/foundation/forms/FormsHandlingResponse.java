package com.day.cq.wcm.foundation.forms;

import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;

public class FormsHandlingResponse
        extends SlingHttpServletResponseWrapper
{
    private ServletOutputStream nullstream;
    private PrintWriter nullwriter;

    public FormsHandlingResponse(SlingHttpServletResponse wrappedResponse)
    {
        super(wrappedResponse);
    }

    public PrintWriter getWriter()
    {
        if (this.nullwriter == null) {
            this.nullwriter = new PrintWriter(getOutputStream());
        }
        return this.nullwriter;
    }

    public ServletOutputStream getOutputStream()
    {
        if (this.nullstream == null) {
            this.nullstream = new ServletOutputStream()
            {
                public void write(int b) {}

                public void write(byte[] b) {}

                public void write(byte[] b, int off, int len) {}

                public void flush() {}

                public void close() {}
            };
        }
        return this.nullstream;
    }

    public void flushBuffer() {}

    public void reset() {}

    public void resetBuffer() {}
}
