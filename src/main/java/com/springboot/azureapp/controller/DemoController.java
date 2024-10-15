package com.springboot.azureapp.controller;

import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import com.springboot.azureapp.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo")
public class  DemoController {

    @Autowired
    private DemoService demoService;

    @GetMapping("/healthcheck")
    public String healthCheck(){
        return "OK";
    }
    @GetMapping("/get/entries")
    public List<DemoModel> getAllEntries(){
        return demoService.getAllEntries();
    }

    @GetMapping("/get/entries/{id}")
    public DemoModel getEntries(@PathVariable Long id) throws DemoModelNotFoundException {
        return demoService.getEntries(id);
    }
    @PostMapping("/add/entries")
    public DemoModel addEntries(@RequestBody DemoModel demoModel){
       return demoService.addEntries(demoModel);
    }
    @GetMapping("/update/entries/name")
    public DemoModel getEntriesByName(@RequestParam String name){
        return demoService.getEntriesByName(name);
    }

}
