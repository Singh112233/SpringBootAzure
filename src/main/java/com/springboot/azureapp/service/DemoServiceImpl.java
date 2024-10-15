package com.springboot.azureapp.service;

import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import com.springboot.azureapp.repository.DemoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DemoServiceImpl implements DemoService{

    @Autowired
    private DemoRepository demoRepository;
    @Override
    public DemoModel addEntries(DemoModel demoModel) {
        return demoRepository.save(demoModel);
    }

    @Override
    public DemoModel getEntries(Long id) throws DemoModelNotFoundException {
        Optional<DemoModel> demoModel = demoRepository.findById(id);
        if (!demoModel.isPresent()){
            throw new DemoModelNotFoundException("Demo Model Not Found ");
        }
        else
            return demoModel.get();
    }

    @Override
    public List<DemoModel> getAllEntries() {
        return demoRepository.findAll();
    }

    @Override
    public DemoModel getEntriesByName(String name) {
        return demoRepository.findByTitle(name);
    }

}
