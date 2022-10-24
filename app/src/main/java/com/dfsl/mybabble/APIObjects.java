package com.dfsl.mybabble;

class NError
{
    public String message;
    public SubError errors;
}

class SubError
{
    public String[] email;
    public String[] password;
    public String[] username;
    public String[] verification_code;
}
