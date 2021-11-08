package com.filecoinj.model;

import java.io.Serializable;
import java.util.List;

public class RpcPar implements Serializable {
    private String jsonrpc;
    private Integer id;
    private String method;
    private List<Object> params;
}
