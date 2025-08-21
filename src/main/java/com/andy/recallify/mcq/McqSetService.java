package com.andy.recallify.mcq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class McqSetService {

    private final McqSetRepository mcqSetRepository;

    @Autowired
    public McqSetService(McqSetRepository mcqSetRepository) {
        this.mcqSetRepository = mcqSetRepository;
    }



}
