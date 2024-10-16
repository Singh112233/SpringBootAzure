package com.springboot.azureapp.service;


import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DemoService {

    DemoModel addEntries(DemoModel demoModel);

    kjkj
    DemoModel getEntries(Long id) throws DemoModelNotFoundException;

    List<DemoModel> getAllEntries();


    DemoModel getEntriesByName(String name);
}
