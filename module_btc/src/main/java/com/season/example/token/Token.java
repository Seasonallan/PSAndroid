package com.season.example.token;

import java.io.Serializable;

public class Token implements Comparable<Token>, Serializable {

    final long serialVersionUID = 10086;

    public String name;
    public String desc;
    public String address;
    public String amount;

    public static Token SHIB() {
        Token token = new Token();
        token.name = "SHIB";
        token.desc = "SHIBA INU";
        token.address = "0x95ad61b0a150d79219dcf64e1e6cc01f0b64c4ce";
        return token;
    }

    @Override
    public int compareTo(Token o) {
        return name.compareTo(o.name);
    }
}