package io.reactivej.dcf.node.log;

public class Utf8StringBuffer 
{
    StringBuffer _buffer;
    int _more;
    int _bits;
    boolean _errors;
    
    public Utf8StringBuffer()
    {
        _buffer=new StringBuffer();
    }
    
    public Utf8StringBuffer(int capacity)
    {
        _buffer=new StringBuffer(capacity);
    }

    public void append(byte[] b,int offset, int length)
    {
        int end=offset+length;
        for (int i=offset; i<end;i++)
            append(b[i]);
    }
    
    public void append(byte b)
    {
        if (b>=0)
        {
            if (_more>0)
            {
                _buffer.append('?');
                _more=0;
                _bits=0;
            }
            else
                _buffer.append((char)(0x7f&b));
        }
        else if (_more==0)
        {
            if ((b&0xc0)!=0xc0)
            {
                // 10xxxxxx
                _buffer.append('?');
                _more=0;
                _bits=0;
            }
            else if ((b & 0xe0) == 0xc0)
            {
                //110xxxxx
                _more=1;
                _bits=b&0x1f;
            }
            else if ((b & 0xf0) == 0xe0)
            {
                //1110xxxx
                _more=2;
                _bits=b&0x0f;
            }
            else if ((b & 0xf8) == 0xf0)
            {
                //11110xxx
                _more=3;
                _bits=b&0x07;
            }
            else if ((b & 0xfc) == 0xf8)
            {
                //111110xx
                _more=4;
                _bits=b&0x03;
            }
            else if ((b & 0xfe) == 0xfc) 
            {
                //1111110x
                _more=5;
                _bits=b&0x01;
            }
        }
        else
        {
            if ((b&0xc0)==0xc0)
            {    // 11??????
                _buffer.append('?');
                _more=0;
                _bits=0;
                _errors=true;
            }
            else
            {
                // 10xxxxxx
                _bits=(_bits<<6)|(b&0x3f);
                if (--_more==0)
                    _buffer.append((char)_bits);
            }
        }
    }
    
    public int length()
    {
        return _buffer.length();
    }
    
    public void reset()
    {
        _buffer.setLength(0);
        _more=0;
        _bits=0;
        _errors=false;
    }
    
    public StringBuffer getStringBuffer()
    {
        return _buffer;
    }
    
    public String toString()
    {
        return _buffer.toString();
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if there are non UTF-8 characters or incomplete UTF-8 characters in the buffer.
     */
    public boolean isError()
    {
        return _errors || _more>0;
    }
}