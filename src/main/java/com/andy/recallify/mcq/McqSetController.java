package com.andy.recallify.mcq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path="api/mcq")
public class McqSetController {

    private final McqSetService mcqSetService;

    @Autowired
    public McqSetController(McqSetService mcqSetService) {
        this.mcqSetService = mcqSetService;
    }

}
