package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username", "status"})
public record DeleteStatusResponse(String username, String status){}
