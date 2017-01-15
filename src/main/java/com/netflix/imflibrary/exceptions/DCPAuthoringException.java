package com.netflix.imflibrary.exceptions;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public class DCPAuthoringException extends RuntimeException {

    public DCPAuthoringException()
    {
        super();
    }

    public DCPAuthoringException(String s)
    {
        super(s);
    }

    public DCPAuthoringException(Throwable t)
    {
        super(t);
    }

    public DCPAuthoringException(String s, Throwable t)
    {
        super(s,t);
    }
}
