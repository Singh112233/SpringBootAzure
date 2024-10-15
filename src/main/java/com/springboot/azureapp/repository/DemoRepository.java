package com.springboot.azureapp.repository;


import com.springboot.azureapp.model.DemoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRepository extends JpaRepository<DemoModel, Long> {

    public DemoModel findByTitle(String title);
}
