package com.gogitek.btl.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResponseData {
    Integer generation;
    List<ResultApi> responseData = new ArrayList<>();
}
