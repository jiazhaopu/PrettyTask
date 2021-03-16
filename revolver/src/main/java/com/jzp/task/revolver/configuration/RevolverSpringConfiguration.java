package com.jzp.task.revolver.configuration;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.jzp.task.revolver"},
    lazyInit = true)
public class RevolverSpringConfiguration {


}
